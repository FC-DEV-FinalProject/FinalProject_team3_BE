package com.investmetic.domain.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.investmetic.domain.user.dto.object.ImageMetadata;
import com.investmetic.domain.user.dto.request.UserModifyDto;
import com.investmetic.domain.user.model.Role;
import com.investmetic.domain.user.model.UserState;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.config.S3MockConfig;
import io.findify.s3mock.S3Mock;
import jakarta.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;




/**
 * mockMvc로 통합 테스트 진행
 * */

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(S3MockConfig.class)
class UserMyPageControllerTest {

    private static final String BUCKET_NAME = "fastcampus-team3";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private EntityManager em;

    @Autowired
    private ObjectMapper objectMapper;





    @Test
    @DisplayName("회원 정보 조회 - DB에 Email 있는 경우")
    void provideUserInfoTest1() throws Exception {

        // DB에 유저 생성.
        User user = createOneUser();


        // DB에 생성한 유저의 email로 파라미터 설정.
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("email", user.getEmail());

        // MockMvc 이용 회원 정보 요청.
        ResultActions resultActions = mockMvc.perform(get("/api/users/mypage/profile")
                .params(multiValueMap)
        );

        resultActions.andExpect(status().isOk()) // 정상 상태 확인
                .andExpect(jsonPath("$.result.userName").value(user.getUserName())) // body에서 이름이 DB에 저장된 이름과 같은지 확인
                .andDo(print());
    }

    @Test
    @DisplayName("회원 정보 조회 - DB에 Email 없는 경우")
    void provideUserInfoTest2() throws Exception {

        // DB에 유저 생성.
        User user = createOneUser();

        // DB에 생성한 유저의 Email로 파라미터 설정.
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("email", user.getEmail());

        // MockMvc 이용 회원 정보 요청.
        ResultActions resultActions1 = mockMvc.perform(get("/api/users/mypage/profile")
                .params(multiValueMap)
        );

        resultActions1.andExpect(status().isOk()) // 정상 상태 확인
                .andExpect(jsonPath("$.result.userName").value(user.getUserName())) // body에서 이름이 DB에 저장된 이름과 같은지 확인
                .andDo(print());

        // MockMvc 이용 회원 정보 요청. - DB에 없는 이메일
        ResultActions resultActions2 = mockMvc.perform(get("/api/users/mypage/profile")
                .param("email", "NotFound@Email.com")
        );

        resultActions2.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(2001))// 실패 상태 확인
                .andDo(print());
    }

    /**
     * 개인 정보 수정은 s3 api 사용하므로 s3Mock을 통해 진행하겠습니다.
     * */
    @Nested
    @DisplayName("개인 정보 수정")
    class userUpdate{

        @BeforeAll
        static void setUp(@Autowired S3Mock s3Mock, @Autowired AmazonS3 amazonS3) {
            s3Mock.start();
            amazonS3.createBucket(BUCKET_NAME);
        }

        @AfterAll
        static void tearDown(@Autowired S3Mock s3Mock, @Autowired AmazonS3 amazonS3) {
            amazonS3.shutdown();
            s3Mock.stop();
        }



        @Test
        @DisplayName("개인 정보 수정 정상 동작 - 이미지 변경 시")
        void updateUserInfo1() throws Exception {
            // DB에 유저 생성.
            User user = createOneUser();

            oneUserImageUpload();


            UserModifyDto userModifyDto = UserModifyDto.builder()
                    .email("jlwoo092513@gmail.com")
                    .imageDto(new ImageMetadata("test.jpg","image/jpg",1024*1024))
                    .nickname("테스트")
                    .infoAgreement(Boolean.TRUE)
                    .password("asdf")
                    .phone("01012345678")
                    .imageChange(true) // primitive 타입으로
                    .build();

            ResultActions resultActions = mockMvc.perform(patch("/api/users/mypage/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userModifyDto))
            );

            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result", containsString(userModifyDto.getImageDto().getImageName())))
                    .andDo(print());
        }

        @Test
        @DisplayName("개인 정보 수정 정상 동작 - 이미지 미변경 시")
        void updateUserInfo2() throws Exception {
            // DB에 유저 생성.
            User user = createOneUser();

            oneUserImageUpload();


            UserModifyDto userModifyDto = UserModifyDto.builder()
                    .email("jlwoo092513@gmail.com")
                    .nickname("테스트")
                    .infoAgreement(Boolean.TRUE)
                    .password("asdf")
                    .phone("01012345678")
                    .imageChange(false)
                    .build();

            ResultActions resultActions = mockMvc.perform(patch("/api/users/mypage/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userModifyDto))
            );

            resultActions.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(print());
        }

        @Test
        @DisplayName("imageChange 가 null로 들어오는 경우")
        void updateUserInfo3() throws Exception {


            // imageChange null로 보냄
            UserModifyDto userModifyDto = UserModifyDto.builder()
                    .email("jlwoo092513@gmail.com")
                    .nickname("테스트")
                    .infoAgreement(Boolean.TRUE)
                    .password("asdf")
                    .phone("01012345678")
                    .build();


            //Valid Test throw MethodArgumentNotValidException
            ResultActions resultActions = mockMvc.perform(patch("/api/users/mypage/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userModifyDto))
            );

            resultActions.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("잘못된 입력 값"))
                    .andDo(print());
        }

        @Test
        @DisplayName("imageDto filname null, 빈 문자 테스트")
        void updateUserInfo4() throws Exception {


            // imageDto Valid Test
            UserModifyDto userModifyDto = UserModifyDto.builder()
                    .email("jlwoo092513@gmail.com")
                    .imageDto(new ImageMetadata("asdf.jpg","image/jpg",1024*1024*5))
                    .nickname("테스트")
                    .infoAgreement(Boolean.TRUE)
                    .password("asdf")
                    .phone("01012345678")
                    .imageChange(true) // primitive 타입으로
                    .build();

            //Valid Test throw MethodArgumentNotValidException
            ResultActions resultActions = mockMvc.perform(patch("/api/users/mypage/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userModifyDto))
            );

            resultActions.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("잘못된 입력 값"))
                    .andDo(print());
        }





    }

    private void oneUserImageUpload(){
        String key = "IMG-3925.JPG";
        String contentType = "image/jpg";

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);

        amazonS3.putObject(new PutObjectRequest(BUCKET_NAME, key,
                new ByteArrayInputStream("IMG-3925".getBytes(StandardCharsets.UTF_8)),
                objectMetadata));
    }


    private User createOneUser() {
        User user = User.builder()
                .userName("정룡우")
                .nickname("jeongRyongWoo")
                .email("jlwoo092513@gmail.com")
                .password("123456")
                .imageUrl("https://"+BUCKET_NAME+".s3.ap-northeast-2.amazonaws.com/IMG-3925.JPG")
                .phone("01012345678")
                .birthDate("000925")
                .ipAddress("127.0.0.1")
                .infoAgreement(Boolean.FALSE)
                .joinDate(LocalDate.now())
                .userState(UserState.ACTIVE)
                .role(Role.INVESTOR_ADMIN)
                .build();

        userRepository.save(user);
        return user;
    }


}