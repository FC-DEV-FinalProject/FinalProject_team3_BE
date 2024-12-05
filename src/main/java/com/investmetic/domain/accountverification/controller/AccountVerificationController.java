package com.investmetic.domain.accountverification.controller;

import com.investmetic.domain.accountverification.dto.response.AccountImagesResponseDto;
import com.investmetic.domain.accountverification.service.AccountVerificationService;
import com.investmetic.domain.strategy.dto.request.AccountImageRequestDto;
import com.investmetic.global.common.PageResponseDto;
import com.investmetic.global.dto.MultiPresignedUrlResponseDto;
import com.investmetic.global.exception.BaseResponse;
import com.investmetic.global.exception.SuccessCode;
import com.investmetic.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my-strategies")
@RequiredArgsConstructor
@Tag(name = "마이페이지 트레이더 API", description = "마이페이지 트레이더 전략 관련 API")
public class AccountVerificationController {
    private final AccountVerificationService accountVerificationService;

    @PostMapping("/{strategyId}/account-images")
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @Operation(summary = "트레이더 전략 실계좌 인증 이미지 등록 기능", description = "<a href='https://field-sting-eff.notion.site/fe8187f51c9c4a199622b932a1985458?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<MultiPresignedUrlResponseDto>> registerStrategyAccountImages(
            @PathVariable Long strategyId,
            @RequestBody List<AccountImageRequestDto> requestDto,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return BaseResponse.success(accountVerificationService.registerStrategyAccountImages(strategyId, requestDto,
                customUserDetails.getUserId()));
    }

    @GetMapping("/{strategyId}/account-images")
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @Operation(summary = "트레이더 전략 실계좌 인증 조회 기능", description = "<a href='https://field-sting-eff.notion.site/57b368c56b1340b1bd9c72ca52090c51?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<PageResponseDto<AccountImagesResponseDto>>> fetchAccountImages(
            @PathVariable Long strategyId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        return BaseResponse.success(accountVerificationService.getAccountImagesByStrategyId(strategyId, pageable,
                customUserDetails.getUserId()));
    }

    @PostMapping("/{strategyId}/delete-account-images")
    @PreAuthorize("hasRole('ROLE_TRADER')")
    @Operation(summary = "트레이더 전략 실계좌 인증 이미지 삭제 기능", description = "<a href='https://field-sting-eff.notion.site/3f07500f850b40e29eb70ccf5fe83ba1?pvs=4' target='_blank'>API 명세서</a>")
    public ResponseEntity<BaseResponse<Void>> deleteAccountImages(
            @PathVariable Long strategyId,
            @RequestBody List<Long> imageIds,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        accountVerificationService.deleteStrategyAccountImages(strategyId, imageIds, customUserDetails.getUserId());
        return BaseResponse.success(SuccessCode.DELETED);
    }
}
