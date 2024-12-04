package com.investmetic.domain.qna.controller;

import com.investmetic.domain.qna.dto.request.AnswerRequestDto;
import com.investmetic.domain.qna.service.AnswerService;
import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import com.investmetic.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "문의(답변) API", description = "문의(답변) 관련 API")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    //문의 답변 등록
    @PostMapping("/trader/questions/{questionId}/answers")
    @PreAuthorize("hasRole('ROLE_INVESTOR')")
    @Operation(summary = "문의 답변 등록", description = "<a href='https://field-sting-eff.notion.site/7d8679f1a17846a3bd1ce2f8aca1a306?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<Void>> addAnswer(
            @PathVariable Long questionId,
            @RequestBody @Valid AnswerRequestDto answerRequestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        answerService.createAnswer(questionId, customUserDetails.getUserId(), answerRequestDto);
        return BaseResponse.success(SuccessCode.CREATED);
    }

    //문의 답변 삭제 (트레이더)
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @Operation(summary = "문의 답변 삭제", description = "<a href='https://field-sting-eff.notion.site/4947f0c29cea466690b79c743e33efe4?pvs=4'>API 명세서</a>")
    @DeleteMapping("/trader/questions/{questionId}/answers/{answerId}")
    public ResponseEntity<BaseResponse<Void>> deleteTraderAnswer(
            @PathVariable Long questionId,
            @PathVariable Long answerId,
            @RequestParam Long userId) {

        answerService.deleteTraderAnswer(answerId, questionId, userId);
        return BaseResponse.success(SuccessCode.DELETED);
    }

    //문의 답변 삭제 (관리자)
    @PreAuthorize("hasAnyRole('ROLE_TRADER_ADMIN', 'ROLE_INVESTOR_ADMIN')")
    @Operation(summary = "문의 답변 삭제", description = "<a href='https://field-sting-eff.notion.site/bf3ee54b67434b1fad4f9b3c10492c13?pvs=4' target='_blank'>API 명세서</a>")
    @DeleteMapping("/admin/questions/{questionId}/answers/{answerId}")
    public ResponseEntity<BaseResponse<Void>> deleteAdminAnswer(
            @PathVariable Long questionId,
            @PathVariable Long answerId) {

        answerService.deleteAdminAnswer(answerId, questionId);
        return BaseResponse.success(SuccessCode.DELETED);
    }
}
