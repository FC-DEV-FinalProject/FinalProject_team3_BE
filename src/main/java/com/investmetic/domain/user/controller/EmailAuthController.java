package com.investmetic.domain.user.controller;

import com.investmetic.domain.user.dto.request.EmailRequestDto;
import com.investmetic.domain.user.service.EmailService;
import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import com.investmetic.global.util.stibee.StibeeEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/authenticate")
@RequiredArgsConstructor
public class EmailAuthController {

    private final StibeeEmailService stibeeEmailService;

    private final EmailService emailService;

    // 이메일 인증 코드 전송
    @GetMapping("/email/{emailAddr}/authcode")
    public ResponseEntity<BaseResponse<String>> sendEmailPath(
            @PathVariable String emailAddr){

        // 코드 생성
        String code = emailService.createdCode(emailAddr); //코드 redis 저장 있어야함.

        // 코드 발송.
        stibeeEmailService.sendAuthenticationCode(emailAddr, code);

        return BaseResponse.success(SuccessCode.OK);
    }

    // 인증 코드 검증
    @PostMapping("/email/{emailAddr}/authcode")
    public ResponseEntity<BaseResponse<Boolean>> sendEmailAndCode(
            @PathVariable String emailAddr,
            @RequestBody EmailRequestDto emailRequestDto) {


        boolean response = emailService.verifyEmailCode(emailAddr, emailRequestDto.getCode());

        return BaseResponse.success(response);

    }
}