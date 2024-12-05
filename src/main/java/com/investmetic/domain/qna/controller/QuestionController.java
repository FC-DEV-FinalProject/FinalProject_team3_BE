package com.investmetic.domain.qna.controller;


import com.investmetic.domain.qna.dto.SearchCondition;
import com.investmetic.domain.qna.dto.StateCondition;
import com.investmetic.domain.qna.dto.request.QuestionRequestDto;
import com.investmetic.domain.qna.dto.response.QuestionsDetailResponse;
import com.investmetic.domain.qna.dto.response.QuestionsResponse;
import com.investmetic.domain.qna.service.QuestionService;
import com.investmetic.global.common.PageResponseDto;
import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import com.investmetic.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * QuestionController 문의 관련 API를 처리하는 컨트롤러 클래스 역할(투자자, 트레이더, 관리자)에 따라 접근 권한을 구분하여 처리
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /**
     * 문의 등록
     *
     * @param strategyId         등록할 문의가 속한 전략 ID
     * @param questionRequestDto 문의 제목 및 내용 등의 요청 데이터
     * @return 문의 등록 성공 응답
     */
    @PostMapping("/strategies/{strategyId}/questions")
    @PreAuthorize("hasRole('ROLE_INVESTOR')")
    @Operation(summary = "문의 등록", description = "<a href='https://field-sting-eff.notion.site/c6367252bec4489c8137cd48204aeead?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<Void>> createQuestion(
            @PathVariable Long strategyId,
            @RequestBody @Valid QuestionRequestDto questionRequestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        questionService.createQuestion(customUserDetails.getUserId(), strategyId, questionRequestDto);
        return BaseResponse.success(SuccessCode.CREATED);
    }

    /**
     * 문의 삭제
     *
     * @param strategyId 삭제할 문의가 속한 전략 ID
     * @param questionId 삭제할 문의 ID
     * @return 문의 삭제 성공 응답
     */
    @DeleteMapping("/strategies/{strategyId}/questions/{questionId}")
    @PreAuthorize("hasRole('ROLE_INVESTOR')")
    public ResponseEntity<BaseResponse<Void>> deleteQuestion(
            @PathVariable Long strategyId,
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        questionService.deleteQuestion(strategyId, questionId, customUserDetails.getUserId());

        return BaseResponse.success(SuccessCode.DELETED);
    }

    /**
     * 투자자 문의 목록 조회
     *
     * @param pageable 페이지네이션 정보 (페이지 크기, 정렬 등)
     * @return 투자자 문의 목록
     */
    @GetMapping("/investor/questions")
    @PreAuthorize("hasRole('ROLE_INVESTOR')")
    @Operation(summary = "투자자 문의 목록 조회", description = "<a href='https://field-sting-eff.notion.site/133746b97cc345ca83510b74ebb898ee?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<PageResponseDto<QuestionsResponse>>> getInvestorQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam SearchCondition searchCondition,
            @RequestParam StateCondition stateCondition,
            @PageableDefault(size = 4, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 추후 시큐리티 적용 시 Authentication 객체로부터 userId와 userRole을 추출
        PageResponseDto<QuestionsResponse> response = questionService.getInvestorQuestions(
                customUserDetails.getUserId(), customUserDetails.getRole(), keyword, searchCondition,
                stateCondition,
                pageable);
        return BaseResponse.success(response);
    }

    /**
     * 트레이더 문의 목록 조회
     *
     * @param pageable 페이지네이션 정보 (페이지 크기, 정렬 등)
     * @return 트레이더 문의 목록
     */
    @GetMapping("/trader/questions")
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @Operation(summary = "트레이더 문의 목록 조회", description = "<a href='https://field-sting-eff.notion.site/c4688305598b475eb587a910f5d205c5?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<PageResponseDto<QuestionsResponse>>> getTraderQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam SearchCondition searchCondition,
            @RequestParam StateCondition stateCondition,
            @PageableDefault(size = 4, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 추후 시큐리티 적용 시 Authentication 객체로부터 userId와 userRole을 추출
        PageResponseDto<QuestionsResponse> response = questionService.getTraderQuestions(customUserDetails.getUserId(),
                customUserDetails.getRole(), keyword, searchCondition, stateCondition,
                pageable);
        return BaseResponse.success(response);
    }

    /**
     * 관리자 문의 목록 조회
     *
     * @param pageable 페이지네이션 정보 (페이지 크기, 정렬 등)
     * @return 관리자 문의 목록
     */
    @GetMapping("/admin/questions")
    @PreAuthorize("hasAnyRole('ROLE_TRADER_ADMIN', 'ROLE_INVESTOR_ADMIN')")
    @Operation(summary = "관리자 문의 목록 조회", description = "<a href='https://field-sting-eff.notion.site/54f8361fe76b4cb0b6d80c2efec928e7?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<PageResponseDto<QuestionsResponse>>> getAdminQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam SearchCondition searchCondition,
            @RequestParam StateCondition stateCondition,
            @PageableDefault(size = 4, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 추후 시큐리티 적용 시 Authentication 객체로부터 userId와 userRole을 추출
        PageResponseDto<QuestionsResponse> response = questionService.getAdminQuestions(customUserDetails.getUserId(),
                customUserDetails.getRole(), keyword, searchCondition, stateCondition,
                pageable);
        return BaseResponse.success(response);
    }

    /**
     * 문의 상세 조회
     *
     * @param questionId 상세 조회할 문의 ID
     * @return 문의 상세 정보
     */
    @GetMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('ROLE_INVESTOR', 'ROLE_TRADER')")
    @Operation(summary = "문의 상세 조회", description = "<a href='https://field-sting-eff.notion.site/c0e3dea22c704a8da2ed57d23001dce4?pvs=4' target='_blank'>API 명세서(투자자)</a><a href='https://field-sting-eff.notion.site/feb7905cbbee4261aeca2b4e5afcbd50?pvs=4' target='_blank'>API 명세서(트레이더)</a>")
    public ResponseEntity<BaseResponse<QuestionsDetailResponse>> getQuestionDetail(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        // 사용자 역할에 따라 처리
        QuestionsDetailResponse response = questionService.getQuestionDetail(questionId, customUserDetails.getUserId(),
                customUserDetails.getRole());
        return BaseResponse.success(response);
    }

    /**
     * 관리자 문의 상세 조회
     *
     * @param questionId 상세 조회할 문의 ID
     * @return 문의 상세 정보
     */
    @GetMapping("/admin/questions/{questionId}")
    @PreAuthorize("hasAnyRole('ROLE_TRADER_ADMIN', 'ROLE_INVESTOR_ADMIN')")
    @Operation(summary = "관리자 문의 상세 조회", description = "<a href='https://field-sting-eff.notion.site/b0f3ad4383584c7c966484802b1d48b1?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<QuestionsDetailResponse>> getAdminQuestionDetail(
            @PathVariable Long questionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        QuestionsDetailResponse response = questionService.getAdminQuestionDetail(questionId,
                customUserDetails.getRole());

        return BaseResponse.success(response);
    }
}
