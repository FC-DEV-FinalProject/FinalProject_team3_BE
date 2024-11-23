package com.investmetic.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.investmetic.domain.user.dto.object.ImageMetadata;
import com.investmetic.domain.user.dto.request.UserModifyDto;
import com.investmetic.domain.user.model.Role;
import com.investmetic.domain.user.model.UserState;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.util.s3.FilePath;
import com.investmetic.global.util.s3.S3FileService;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserInfoModifyServiceTest {

    private static final String BUCKET_NAME = "https://fastcampus-team3.s3.ap-northeast-2.amazonaws.com/";


    @InjectMocks
    private UserMyPageService userMyPageService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3FileService s3FileService;

    // null 값에 대한 Test에서 사용될 인자.
    static Stream<Arguments> userModifyDtos() {
        return Stream.of(Arguments.arguments("이미지만 변경",
                        UserModifyDto.builder().email("jlwoo092513@gmail.com").imageChange(Boolean.TRUE)
                                .imageDto(new ImageMetadata("testImage.jpg", "image/jpg", 5000))
                                .build()),

                Arguments.arguments("닉네임만 변경",
                        UserModifyDto.builder().email("jlwoo092513@gmail.com").imageChange(Boolean.FALSE)
                                .nickname("테스트").build()),

                Arguments.arguments("핸드폰 번호 변경, 기존 이미지 삭제",
                        UserModifyDto.builder().email("jlwoo092513@gmail.com").imageChange(Boolean.TRUE)
                                .phone("01099999999").build()),

                Arguments.arguments("정보 수신 동의 변경",
                        UserModifyDto.builder().email("jlwoo092513@gmail.com").imageChange(Boolean.FALSE)
                                .infoAgreement(Boolean.FALSE).build()),

                Arguments.arguments("비밀 번호 변경",
                        UserModifyDto.builder().email("jlwoo092513@gmail.com").imageChange(Boolean.FALSE)
                                .password("testtest!!").build()));
    }

    private User createOneUser() {
        User user = User.builder().userName("정룡우").nickname("jeongRyongWoo").email("jlwoo092513@gmail.com")
                .password(passwordEncoder.encode("123456")).imageUrl(BUCKET_NAME + "IMG-3925.JPG").phone("01012345678")
                .birthDate("000925").ipAddress("127.0.0.1").infoAgreement(Boolean.FALSE).userState(UserState.ACTIVE)
                .role(Role.INVESTOR_ADMIN).build();
        userRepository.save(user);
        return user;
    }

    @Test
    @DisplayName("모든 값이 들어있을 떄.")
    void updateUserInfoTest1() {

        //given
        User oneUser = createOneUser();

        //이미지 메타데이터 설정
        ImageMetadata imageMetadata = new ImageMetadata("test.jpg", "image/jpg", 1024 * 500);

        // 변경된 회원값
        UserModifyDto userModifyDto = UserModifyDto.builder().email(oneUser.getEmail()).imageDto(imageMetadata)
                .infoAgreement(Boolean.FALSE).nickname("자자ㅏㅈ").phone("01012345678").password("9999")
                .imageChange(Boolean.TRUE).build();

        when(userRepository.findByEmail(userModifyDto.getEmail())).thenReturn(Optional.of(oneUser));

        when(s3FileService.getS3Path(FilePath.USER_PROFILE, imageMetadata.getImageName(), imageMetadata.getSize()))
                .thenReturn(BUCKET_NAME + imageMetadata.getImageName()); // uuid 제외

        when(s3FileService.getPreSignedUrl(anyString())).thenReturn(BUCKET_NAME + imageMetadata.getImageName());

        // when
        String presignedUrl = userMyPageService.changeUserInfo(userModifyDto, userModifyDto.getEmail());

        // then
        assertThat(presignedUrl).contains(imageMetadata.getImageName());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("userModifyDtos")
    @DisplayName("null 값에 대한 Test")
    void updateUserInfoTest2(String testName, UserModifyDto userModifyDto) {

        // given
        User oneUser = createOneUser();

        when(userRepository.findByEmail(userModifyDto.getEmail())).thenReturn(Optional.of(oneUser));

        //사진 변경시, 새로운 사진을 올리는 경우.
        if (Boolean.TRUE.equals(userModifyDto.getImageChange()) && userModifyDto.getImageDto() != null) {
            when(s3FileService.getS3Path(FilePath.USER_PROFILE, userModifyDto.getImageDto().getImageName(),
                    userModifyDto.getImageDto().getSize())).thenReturn(
                    BUCKET_NAME + userModifyDto.getImageDto().getImageName()); // uuid 제외

            when(s3FileService.getPreSignedUrl(anyString())).thenReturn(
                    BUCKET_NAME + userModifyDto.getImageDto().getImageName());
        }

        //패스워드 변경시.
        if (userModifyDto.getPassword() != null) {
            when(passwordEncoder.encode(anyString())).thenReturn(userModifyDto.getPassword());
        }

        String presignedUrl = userMyPageService.changeUserInfo(userModifyDto, userModifyDto.getEmail());

        // 새로운 이미지로 업데이트 하는 경우.
        if (userModifyDto.getImageDto() != null) {
            //새롭게 저장될 s3객체 URL에 새로운 이미지의 이름이 들어가 있는지 확인.
            assertThat(presignedUrl).contains(userModifyDto.getImageDto().getImageName());
        } else {
            //이미지 변경을 안하는 경우.
            assertThat(presignedUrl).isNull();
        }
    }
}
