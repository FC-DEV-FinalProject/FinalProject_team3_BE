package com.investmetic.domain.user.service;

import com.investmetic.domain.user.dto.request.UserModifyDto;
import com.investmetic.domain.user.dto.response.UserProfileDto;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import com.investmetic.global.util.s3.FilePath;
import com.investmetic.global.util.s3.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMyPageService {

    private final UserRepository userRepository;

    private final S3FileService s3FileService;

    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * 개인 정보 제공
     */
    public UserProfileDto provideUserInfo(String email) {

        //BaseResponse.fail를 사용할 만한 것들은 일단 다 예외로 던지기.
        return userRepository.findByEmailUserInfo(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND));
    }


    /**
     * 회원 정보 수정 Transactional(readOnly = false), 이메일은 변경 불가합니다.
     */
    @Transactional
    public String changeUserInfo(UserModifyDto userModifyDto, String email) {

        // S3Path 받을 변수 설정.
        String s3Path = null;

        // TODO : jwt, securityContext 에서 email 가져오기.

        // dto에 담긴 email과 토큰상의 email이 같은지 확인.
        if (!userModifyDto.getEmail().equals(email)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        //DB에서 email에 해당하는 회원 정보 가져오기(Dirty Checking), 해당 회원이 있는지 확인.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USERS_NOT_FOUND));

        // 회원이 기존 사진을 변경하는 경우.
        if (Boolean.TRUE.equals(userModifyDto.getImageChange())) {
            // 1.1 새로운 사진을 올리는 경우.
            if (userModifyDto.getImageDto() != null) {

                String filename = userModifyDto.getImageDto().getImageName(); // 확장자 포함 파일 이름 - 프론트랑 상의하기.
                int fileSize = userModifyDto.getImageDto().getSize(); // 일단 바이트

                s3Path = s3FileService.getS3Path(FilePath.USER_PROFILE, filename, fileSize);
                /*
                 * 이미지 유효성 검사 후 s3경로 반환, 불통 시 RuntimeException 반환
                 * 유효성 검사에서 예외날 수 있으므로 이미지 파일 검사 후 s3delete 요청하기.
                 * */
            }
            // 1.2 새로운 사진을 안올리는 경우는 프로필을 기본 프로필 이미지로 바꾸겠다는 의미.

            // XXX : presignedUrl 성공 후에 삭제 될 수 있게 생각해보기(가장 안전할 듯.)
            // 2.1 기존의 프로필 사진이 있다면 기존의 프로필 사진 제거.
            if (user.getImageUrl() != null) {
                s3FileService.deleteFromS3(user.getImageUrl()); //실패 시 RuntimeException
            }

            // 2.2 기존의 프로필 사진이 없다면 그대로 통과 (s3Path 값을 가지고 있음)
        }
        //비밀 번호가 있으면 암호화 하여 저장.
        if (userModifyDto.getPassword() != null) {
            if (passwordEncoder.matches(userModifyDto.getPassword(), user.getPassword())) { //기존 비밀번호와 같다면 예외처리
                throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
            }
            user.changePassword(passwordEncoder.encode(userModifyDto.getPassword()));
        }

        // 영속화된 User 객체에 회원 정보 update
        user.updateUser(userModifyDto, s3Path);

        return s3Path == null ? null : s3FileService.getPreSignedUrl(s3Path);
    }


    public void checkPassword(String email, String rawPassword) {

        // 이메일에 해당하는 패스워드 찾아오기.
        String encodedPassword = userRepository.findPasswordByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USERS_NOT_FOUND));

        //패스워드가 일치하지 않으면 throw
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_AUTHENTICATION_FAILED);
        }
    }

    @Transactional
    public void resetPassword(UserModifyDto userModifyDto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USERS_NOT_FOUND));

        //비밀 번호가 있으면 암호화 하여 저장.
        if (passwordEncoder.matches(userModifyDto.getPassword(), user.getPassword())) { //기존 비밀번호와 같다면 예외처리
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }
        user.changePassword(passwordEncoder.encode(userModifyDto.getPassword()));
    }
}
