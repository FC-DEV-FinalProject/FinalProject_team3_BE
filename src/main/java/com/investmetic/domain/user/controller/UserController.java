package com.investmetic.domain.user.controller;

import com.investmetic.domain.user.dto.object.TraderListSort;
import com.investmetic.domain.user.dto.request.UserModifyDto;
import com.investmetic.domain.user.dto.request.UserSignUpDto;
import com.investmetic.domain.user.dto.response.AvaliableDto;
import com.investmetic.domain.user.dto.response.FoundEmailDto;
import com.investmetic.domain.user.dto.response.TraderProfileDto;
import com.investmetic.domain.user.service.UserMyPageService;
import com.investmetic.domain.user.service.UserService;
import com.investmetic.global.common.PageResponseDto;
import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 기본 API", description = "사용자 관련 기본 기능 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMyPageService userMyPageService;

    @Operation(summary = "회원 가입",
            description = "<a href='https://www.notion.so/3b51884e19b2420e8800a18ee92c310c' target='_blank'>API 명세서</a>")
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<String>> signup(@Valid @RequestBody UserSignUpDto userSignUpDto) {
        // 회원가입시 오류 BusinessException(ErrorCode.SIGN_UP_FAILED)로
        userService.signUp(userSignUpDto);

        return BaseResponse.success(SuccessCode.CREATED);
    }

    //닉네임 중복 검사
    @Validated
    @Operation(summary = "닉네임 중복 확인",
            description = "<a href='https://www.notion.so/79c0ca49115946ff944e4ad53e2cd581' target='_blank'>API 명세서</a>")
    @GetMapping("/check/nickname")
    public ResponseEntity<BaseResponse<AvaliableDto>> checkNicknameDuplicate(
            @RequestParam
            @Pattern(regexp = "^[a-zA-Z가-힣0-9._-]{2,10}$", message = "닉네임은 2~10자 이내로 설정해야 하며, 특수문자는 ., _, -만 사용할 수 있습니다.")
            String nickname) {

        AvaliableDto response = userService.checkNicknameDuplicate(nickname);

        return BaseResponse.success(response);
    }

    // 이메일 중복 검사
    @Validated
    @Operation(summary = "이메일 중복 확인",
            description = "<a href='https://www.notion.so/c35335e2d75048e5a84bf50cbcb9098e' target='_blank'>API 명세서</a>")
    @GetMapping("/check/email")
    public ResponseEntity<BaseResponse<AvaliableDto>> checkEmailDuplicate(
            @RequestParam
            @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", message = "유효하지 않은 이메일 형식입니다.")
            String email) {

        AvaliableDto response = userService.checkEmailDuplicate(email);

        return BaseResponse.success(response);
    }

    // 전화번호 중복 검사
    @Validated
    @Operation(summary = "전화번호 중복 확인",
            description = "<a href='https://www.notion.so/b6445707925c40f1809ba1b92ffe3d01' target='_blank'>API 명세서</a>")
    @GetMapping("/check/phone")
    public ResponseEntity<BaseResponse<AvaliableDto>> checkPhoneDuplicate(
            @RequestParam
            @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "유효하지 않은 휴대번호 형식입니다.")
            String phone) {

        AvaliableDto response = userService.checkPhoneDuplicate(phone);

        return BaseResponse.success(response);
    }

    /**
     * 트레이더 목록 조회
     *
     * @param sort     정렬할 조건, null 가능
     * @param keyword  닉네임 검색 키워드, null 가능
     * @param pageable 현재 페이지, 사이즈 - 디자인에서는 12개로 확인됨.
     */
    @Operation(summary = "트레이더 목록 조회",
            description = "<a href='https://www.notion.so/3e75537ce988485d8477dc5bb5e14cfd' target='_blank'>API 명세서</a>")
    @GetMapping("/traders")
    public ResponseEntity<BaseResponse<PageResponseDto<TraderProfileDto>>> getTraderList(
            @RequestParam TraderListSort sort,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 9) Pageable pageable) {
        return BaseResponse.success(userService.getTraderList(sort, keyword, pageable));

    }

    // 전화번호를 통한 이메일 찾기
    @Operation(summary = "휴대번호를 통한 이메일 찾기",
            description = "<a href='https://www.notion.so/68f9f0bcdde94776a29155b7358b450f' target='_blank'>API 명세서</a>")
    @GetMapping("/email")
    public ResponseEntity<BaseResponse<FoundEmailDto>> findEmailByPhone(@RequestParam String phone) {

        return BaseResponse.success(userService.findEmailByPhone(phone));
    }

    //비밀번호 재설정
    @Operation(summary = "비밀번호 재설정",
            description = "<a href='https://www.notion.so/07310bcb25374550a8c0c3596e1251a6' target='_blank'>API 명세서</a>")
    @PatchMapping("/reissue/password")
    public ResponseEntity<BaseResponse<Void>> resetPassword(
            @RequestBody UserModifyDto userModifyDto) {

        userMyPageService.resetPassword(userModifyDto, userModifyDto.getEmail());
        return BaseResponse.success();
    }
}