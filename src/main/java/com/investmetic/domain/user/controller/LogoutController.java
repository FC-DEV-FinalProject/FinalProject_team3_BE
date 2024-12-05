package com.investmetic.domain.user.controller;

import com.investmetic.domain.user.service.LogoutService;
import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 기본 API", description = "사용자 관련 기본 기능 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class LogoutController {

    private final LogoutService logoutService;

    @Operation(summary = "로그아웃 기능",
            description = "<a href='https://www.notion.so/6ab21f30c5d2472fa1493b9dc05dd207' target='_blank'>API 명세서</a>")
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        logoutService.logout(request, response);
        return BaseResponse.success(SuccessCode.OK);
    }
}
