package com.investmetic.domain.user.controller;


import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import com.investmetic.global.security.service.ReIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Tag(name = "사용자 기본 API", description = "사용자 관련 기본 기능 API")
@Controller
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/users/reissue")
public class ReIssueController {

    private final ReIssueService reIssueService;

    @Operation(summary = "Re-Issue",
            description = "<a href='https://www.notion.so/6ab21f30c5d2472fa1493b9dc05dd207' target='_blank'>API 명세서</a>")
    @PostMapping("/refreshtoken")
    public ResponseEntity<BaseResponse<Void>> reissue(HttpServletRequest request, HttpServletResponse response) {
        reIssueService.reissueToken(request, response);
        return BaseResponse.success(SuccessCode.OK);
    }
}
