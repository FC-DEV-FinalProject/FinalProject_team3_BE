package com.investmetic.domain.user.service;

import static com.investmetic.domain.user.dto.object.ColumnCondition.EMAIL;
import static com.investmetic.domain.user.dto.object.ColumnCondition.NICKNAME;
import static com.investmetic.domain.user.dto.object.ColumnCondition.PHONE;

import com.investmetic.domain.user.dto.object.ColumnCondition;
import com.investmetic.domain.user.dto.object.TraderListSort;
import com.investmetic.domain.user.dto.request.UserSignUpDto;
import com.investmetic.domain.user.dto.response.AvaliableDto;
import com.investmetic.domain.user.dto.response.FoundEmailDto;
import com.investmetic.domain.user.dto.response.TraderProfileDto;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.domain.user.repository.UserRepositoryCustomImpl;
import com.investmetic.global.common.PageResponseDto;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import com.investmetic.global.util.RedisUtil;
import com.investmetic.global.util.s3.S3FileService;
import com.investmetic.global.util.stibee.StibeeEmailService;
import java.security.SecureRandom;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {


    private final UserRepository userRepository;
    private final UserRepositoryCustomImpl userRepositoryCustom;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final StibeeEmailService emailService;
    private final S3FileService s3FileService;
    private final RedisUtil redisUtil;
    private final SecureRandom secureRandom = new SecureRandom();
    private final TaskScheduler taskScheduler;


    //회원 가입
    @Transactional
    public void signUp(UserSignUpDto userSignUpDto) {
        try {
            // 비밀번호 인증코드 검증시 사용하는 메서드 재사용.(Redis에서 삭제)
            verifyEmailCode(userSignUpDto.getEmail(), userSignUpDto.getCode());

            //중복 검증
            extracted(userSignUpDto);

            User createUser = UserSignUpDto.toEntity(userSignUpDto, bCryptPasswordEncoder);

            //명시적 세이브...
            userRepository.save(createUser);

            // 스티비 주소록에 회원 추가.
            emailService.addSubscriber(createUser);

        } catch (BusinessException e) {

            // 실패시 모두 되돌리고 회원가입 실패 에러 보내주기.(모두 초기화)
            throw new BusinessException(ErrorCode.SIGN_UP_FAILED);
        }
    }


    // 이메일 찾기 시 인증코드 발송.
    public void sendAuthenticationCode(String email) {
        // 인증 코드 생성.
        String code = createdCode();

        // 해당 이메일로 인증코드 발송.
        emailService.sendAuthenticationCode(email, code);

        // code를 redis에 저장(30 minute)
        redisUtil.setDataExpire(email, code, 60 * 30L);
    }

    // 비로그인 유저에게 인증코드를 발송하기위한 메서드.
    public void sendSignUpCode(String email) {
        // 인증 코드 생성.
        String code = createdCode();

        // 해당 이메일로 인증코드 발송.
        if (!emailService.sendSignUpCode(email, code)) {

            //비로그인 회원이 임시 주소록에 추가되지 않은경우.
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }

        // 임시 주소록에서 해당 회원 삭제.
        taskScheduler.schedule(() -> emailService.deleteTemporalSubscriber(email),
                Instant.now().plusSeconds(8));

        // code를 redis에 저장(30 minute)
        redisUtil.setDataExpire(email, code, 60 * 30L);
    }


    public AvaliableDto checkNicknameDuplicate(String nickname) {

        boolean isDuplicate = userRepository.existsByNickname(nickname);

        if (isDuplicate) {
            return AvaliableDto.builder()
                    .isAvailable(false)
                    .message("이미 사용 중인 닉네임입니다. 다른 닉네임을 입력해주세요.")
                    .build();
        } else {
            return AvaliableDto.builder()
                    .isAvailable(true)
                    .message("사용 가능한 닉네임입니다.")
                    .build();
        }
    }

    public AvaliableDto checkEmailDuplicate(String email) {

        boolean isDuplicate = userRepository.existsByEmail(email);

        if (isDuplicate) {
            return AvaliableDto.builder()
                    .isAvailable(false)
                    .message("이미 사용 중인 이메일입니다. 다른 이메일을 입력해주세요.")
                    .build();
        } else {
            return AvaliableDto.builder()
                    .isAvailable(true)
                    .message("사용 가능한 이메일입니다.")
                    .build();
        }
    }

    public AvaliableDto checkPhoneDuplicate(String phone) {

        boolean isDuplicate = userRepository.existsByPhone(phone);

        if (isDuplicate) {
            return AvaliableDto.builder()
                    .isAvailable(false)
                    .message("이미 사용 중인 휴대번호입니다. 다른 휴대번호를 입력해주세요.")
                    .build();
        } else {
            return AvaliableDto.builder()
                    .isAvailable(true)
                    .message("사용 가능한 휴대번호입니다.")
                    .build();
        }
    }

    /**
     * 트레이더 목록 조회
     *
     * @param sort    null일 때 구독순
     * @param keyword null일 때 키워드 검색 x
     */
    public PageResponseDto<TraderProfileDto> getTraderList(TraderListSort sort, String keyword, Pageable pageable) {

        Page<TraderProfileDto> page = userRepository.getTraderListPage(sort, keyword, pageable);

        return new PageResponseDto<>(page);
    }


    // 회원 가입시에 새로 생겼을지도 모르는 중복 금지 데이터 다시 검증.
    private void extracted(UserSignUpDto userSignUpDto) {
        validateDuplicate(NICKNAME, userSignUpDto.getNickname(), userRepository::existsByNickname);
        validateDuplicate(EMAIL, userSignUpDto.getEmail(), userRepository::existsByEmail);
        validateDuplicate(PHONE, userSignUpDto.getPhone(), userRepository::existsByPhone);
    }


    // 중복 검증 공통 로직
    private void validateDuplicate(ColumnCondition columnName, String value, ValidationFunction validationFunction) {
        if (validationFunction.exists(value)) {
            throw new BusinessException(getErrorCodeForField(columnName));
        }
    }


    // 필드 이름에 따른 에러 코드 매핑
    private ErrorCode getErrorCodeForField(ColumnCondition columnName) {
        return switch (columnName) {
            case NICKNAME -> ErrorCode.INVALID_NICKNAME;
            case EMAIL -> ErrorCode.INVALID_EMAIL;
            case PHONE -> ErrorCode.INVALID_PHONE;
            default -> throw new BusinessException(ErrorCode.INVALID_TYPE_VALUE);
        };
    }

    private String createdCode() {

        int leftLimit = 48; // number '0'
        int rightLimit = 122; // alphabet 'z'
        int targetStringLength = 6;

        return secureRandom.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    // 코드 검증
    public void verifyEmailCode(String email, String code) {

        /*
         * 저장된 인증코드 가져오기.
         * 30분 이후 시간이 지나므로 nullPointException 방지.
         * */
        String codeFoundByEmail = redisUtil.getData(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_FAILED));

        // 입력코드된 인증코드가 저장된 인증코드와 다를때.
        if (!codeFoundByEmail.equals(code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_FAILED);
        }

        //성공 하고 나면 해당 데이터 메모리에서 삭제
        redisUtil.deleteData(email);
    }

    // 회원가입시 인증번호 검증
    public void verifySignUpEmailCode(String email, String code) {

        // 저장된 인증코드 가져오기.
        String codeFoundByEmail = redisUtil.getData(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_FAILED));

        // 입력코드된 인증코드가 저장된 인증코드와 다를때.
        if (!codeFoundByEmail.equals(code)) {
            throw new BusinessException(ErrorCode.VERIFICATION_FAILED);
        }

    }

    //휴대번호를 통한 이메일 찾기
    public FoundEmailDto findEmailByPhone(String phone) {
        String email = userRepository.findEmailByPhone(phone)
                .orElse(null);  //이메일이 없어도 요청은 성공이므로 예외처리하지 않음

        // 이메일이 없으면 isFound = false, email = null로 반환
        if (email == null) {
            return new FoundEmailDto(false, null);
        }

        return new FoundEmailDto(true, emailMasking(email));
    }

    //이메일 마스킹 처리
    private String emailMasking(String email) {

        String localPart = email.substring(0, email.indexOf('@'));

        if (localPart.length() > 3) {
            return localPart.substring(0, 3) + "*".repeat(localPart.length() - 3);
        }
        return email;
    }


    @FunctionalInterface
    private interface ValidationFunction {
        boolean exists(String value);
    }
}