package com.investmetic.domain.notice.controller;


import com.investmetic.domain.notice.dto.request.NoticeRegisterDto;
import com.investmetic.domain.notice.dto.request.NoticeRegistDto;
import com.investmetic.domain.notice.dto.request.NoticeRequestDto;
import com.investmetic.domain.notice.dto.response.NoticeDetailResponseDto;
import com.investmetic.domain.notice.service.NoticeService;
import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import com.investmetic.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "전략 관리 페이지 API", description = "전략 관리 페이지 관련 API")
public class NoticeController {
    private final NoticeService noticeService;

    @PostMapping("/admin/notices")
    @Operation(summary = "공지사항 등록 기능",
            description = "<a href='https://www.notion.so/47f085979b85479f88d4ac8c3a534e09' target='_blank'>API 명세서</a>")
    @PreAuthorize("hasAnyRole('ROLE_TRADER_ADMIN', 'ROLE_INVESTOR_ADMIN')")
    public ResponseEntity<BaseResponse<List<String>>> addNotice(@RequestBody NoticeRegisterDto noticeRegisterDto,
                                                                @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return BaseResponse.success(noticeService.saveNotice(noticeRegisterDto, customUserDetails.getUserId()));
    }

    @PatchMapping("/admin/notices/{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_TRADER_ADMIN', 'ROLE_INVESTOR_ADMIN')")
    @Operation(summary = "공지사항 수정", description = "<a href='https://field-sting-eff.notion.site/a5f446e31c564356bb36184e1963712a?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<Void>> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeRequestDto noticeRequestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        noticeService.updateNotice(noticeId, customUserDetails.getUserId(), noticeRequestDto);
        return BaseResponse.success(SuccessCode.UPDATED);
    }

    @GetMapping("/notice/{noticeId}")
    @Operation(summary = "공지사항 상세 조회 기능",
            description = "<a href='https://www.notion.so/47f085979b85479f88d4ac8c3a534e09' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<NoticeDetailResponseDto>> getNotice(@PathVariable Long noticeId) {
        return BaseResponse.success(noticeService.getNoticeDetail(noticeId));
    }
}
