package com.investmetic.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.investmetic.domain.user.dto.request.UserModifyDto;
import com.investmetic.domain.user.dto.request.UserSignUpDto;
import com.investmetic.domain.user.dto.response.AvaliableDto;
import com.investmetic.domain.user.dto.response.FoundEmailDto;
import com.investmetic.domain.user.dto.response.UserProfileDto;
import com.investmetic.domain.user.model.Role;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import com.investmetic.global.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMyPageService userMyPageService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User createOneUser() {
        User user = User.builder()
                .userName("testUser")
                .nickname("testNickname")
                .phone("01012345678")
                .birthDate("19900101")
                .password("password")
                .email("test@example.com")
                .role(Role.INVESTOR)
                .infoAgreement(true)
                .build();

        userRepository.save(user);
        return user;
    }

    @Test
    @DisplayName("회원가입")
    void signUpTest1() {

        // given
        UserSignUpDto userSignUpDto = UserSignUpDto.builder()
                .username("testUser")
                .nickname("testNickname")
                .phone("01012345678")
                .birthdate("19900101")
                .password("password")
                .email("test@example.com")
                .role(Role.INVESTOR)
                .infoAgreement(true)
                .code("test")
                .build();
        // 인증코드 redis에 저장.
        redisUtil.setDataExpire(userSignUpDto.getEmail(), userSignUpDto.getCode(), 60);

        // when
        userService.signUp(userSignUpDto);

        // then - DB에 사용자 정보가 저장되었는지 확인
        UserProfileDto savedUser = userRepository.findByEmailUserInfo(userSignUpDto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND));
        assertEquals(userSignUpDto.getUsername(), savedUser.getUserName());
        assertEquals(userSignUpDto.getNickname(), savedUser.getNickname());
        assertEquals(userSignUpDto.getEmail(), savedUser.getEmail());
        assertEquals(userSignUpDto.getPhone(), savedUser.getPhone());
        assertEquals(userSignUpDto.getInfoAgreement(), savedUser.getInfoAgreement());

        //redis에서 인증코드가 삭제가 되어야함.
        assertThat(redisUtil.getData(userSignUpDto.getEmail())).isNotPresent();
    }

    @Test
    @DisplayName("닉네임 중복이 있을 때")
    void checkNicknameDuplicate_Duplicate() {

        User user = createOneUser();

        AvaliableDto result = userService.checkNicknameDuplicate(user.getNickname());

        // then
        assertFalse(result.getIsAvailable()); // 사용 불가능해야함
    }

    @Test
    @DisplayName("닉네임 중복이 없을 때")
    void checkNicknameDuplicate_NoDuplicate() {

        String nickname = "newNicknm"; // 존재하지 않는 닉네임
        // when
        AvaliableDto result = userService.checkNicknameDuplicate(nickname);

        // then
        assertTrue(result.getIsAvailable()); // 사용 가능해야 함
    }

    @Test
    @DisplayName("핸드폰 번호 중복 있을때")
    void checkPheonDuplicateTest1() {
        User user = createOneUser();

        AvaliableDto result = userService.checkPhoneDuplicate(user.getPhone());

        assertFalse(result.getIsAvailable()); // 사용 불가능해야함
    }

    @Test
    @DisplayName("핸드폰 번호 중복 없을때")
    void checkPhoneDuplicateTest2() {

        String phone = "01030913501";

        AvaliableDto result = userService.checkPhoneDuplicate(phone);

        assertTrue(result.getIsAvailable());
    }

    @Test
    @DisplayName("이메일 중복이다")
    void checkEmailDuplicateTest1() {

        User user = createOneUser();

        AvaliableDto result = userService.checkEmailDuplicate(user.getEmail());

        assertFalse(result.getIsAvailable());
    }

    @Test
    @DisplayName("이메일 중복 아니에요")
    void checkEmailDuplicateTest2() {

        String email = "없는 이메일";

        AvaliableDto result = userService.checkEmailDuplicate(email);

        assertTrue(result.getIsAvailable());
    }

    @Test
    @DisplayName("전화번호에 해당하는 이메일이 있을 때")
    void findEmailByPhoneTest1() {
        User user = createOneUser();

        FoundEmailDto result = userService.findEmailByPhone(user.getPhone());

        assertTrue(result.getIsFound());
    }

    @Test
    @DisplayName("전화번호에 해당하는 이메일이 없을 때")
    void findEmailByPhoneTest2() {
        String phone = "없는 번호임";

        FoundEmailDto result = userService.findEmailByPhone(phone);

        assertFalse(result.getIsFound());
    }

    @Test
    @DisplayName("비밀번호 재설정")
    void resetPassword1() {
        User user = createOneUser();
        String newPassword = "newpassword"; // 기존 비밀번호와 동일하게 설정

        // UserModifyDto 생성
        UserModifyDto userModifyDto = UserModifyDto.builder()
                .email(user.getEmail()) // 필요한 이메일 세팅
                .password(newPassword) // 기존 비밀번호와 동일한 새 비밀번호
                .build();

        User updatedUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND));

        userMyPageService.resetPassword(userModifyDto, userModifyDto.getEmail());
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword())); //비밀번호 재설정 됐는지 확인
        assertFalse(passwordEncoder.matches(user.getPassword(), updatedUser.getPassword())); //기존 비밀번호와 비교
    }

}