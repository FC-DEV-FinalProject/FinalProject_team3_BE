package com.investmetic.domain.user.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.investmetic.domain.user.dto.response.UserProfileDto;
import com.investmetic.domain.user.model.Role;
import com.investmetic.domain.user.model.UserState;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("회원 마이페이지 Service")
class UserMyPageServiceTest {

    private static final String BUCKET_NAME = "fastcampus-team3";

    @Autowired
    private UserMyPageService userMyPageService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //지금은 userRepository 사용하고 나중에 회원가입 생기면 userService로만 Test해보기.
    @Autowired
    private UserRepository userRepository;

    private User createOneUser() {
        User user = User.builder().userName("정룡우").nickname("jeongRyongWoo").email("jlwoo092513@gmail.com")
                .password(passwordEncoder.encode("123456"))
                .imageUrl("https://" + BUCKET_NAME + ".s3.ap-northeast-2.amazonaws.com/IMG-3925.JPG")
                .phone("01012345678").birthDate("000925").ipAddress("127.0.0.1").infoAgreement(Boolean.FALSE)
                .userState(UserState.ACTIVE).role(Role.INVESTOR_ADMIN).build();
        userRepository.save(user);
        return user;
    }

    @Test
    @DisplayName("회원 정보 조회 - DB에 Email이 있을 경우.")
    void provideUserInfoTest1() {
        User oneUser = createOneUser();

        UserProfileDto userProfileDto = userMyPageService.provideUserInfo(oneUser.getEmail());

        assertEquals(oneUser.getEmail(), userProfileDto.getEmail());
        assertEquals(oneUser.getImageUrl(), userProfileDto.getImageUrl());
        assertEquals(oneUser.getPhone(), userProfileDto.getPhone());
    }

    @Test
    @DisplayName("회원 정보 조회 - DB에 Email이 없을 경우")
    void provideUserInfoTest2() {

        User oneUser = createOneUser(); // 1명 DB에 생성

        UserProfileDto presentUserProfile = userMyPageService.provideUserInfo(oneUser.getEmail());
        assertNotNull(presentUserProfile); // DB에 방금 만든 1명이 있는지.

        BusinessException e = assertThrows(BusinessException.class,
                () -> userMyPageService.provideUserInfo("asdf@hanmail.com"));
        assertEquals(e.getErrorCode().getMessage(), ErrorCode.USER_INFO_NOT_FOUND.getMessage());
    }

    @Nested
    @DisplayName("회원 비밀번호 검증(개인 정보 수정 페이지)")
    class PasswordCheck {

        @Test
        @DisplayName("정상 응답")
        void passwordCheck1() {

            User oneUser = createOneUser();

            assertThatCode(
                    () -> userMyPageService.checkPassword(oneUser.getEmail(), "123456")).doesNotThrowAnyException();
        }


        @Test
        @DisplayName("email에 해당하는 회원 없음.")
        void passwordCheck2() {

            assertThatThrownBy(() -> userMyPageService.checkPassword("asdf", "123456")).isInstanceOf(
                    BusinessException.class).hasMessageContaining(ErrorCode.USERS_NOT_FOUND.getMessage());
        }


        @Test
        @DisplayName("비밀번호가 맞지 않음.")
        void passwordCheck3() {

            User oneUser = createOneUser();

            assertThatThrownBy(() -> {
                userMyPageService.checkPassword(oneUser.getEmail(), "notvalid");
            }).isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.PASSWORD_AUTHENTICATION_FAILED.getMessage());
        }
    }

}