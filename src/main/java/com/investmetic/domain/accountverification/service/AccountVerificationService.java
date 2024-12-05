package com.investmetic.domain.accountverification.service;

import com.investmetic.domain.accountverification.dto.response.AccountImagesResponseDto;
import com.investmetic.domain.accountverification.model.entity.AccountVerification;
import com.investmetic.domain.accountverification.repository.AccountVerificationRepository;
import com.investmetic.domain.strategy.dto.request.AccountImageRequestDto;
import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.repository.StrategyRepository;
import com.investmetic.global.common.PageResponseDto;
import com.investmetic.global.dto.MultiPresignedUrlResponseDto;
import com.investmetic.global.dto.PresignedUrlResponseDto;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import com.investmetic.global.util.s3.FilePath;
import com.investmetic.global.util.s3.S3FileService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountVerificationService {
    private final S3FileService s3FileService;
    private final StrategyRepository strategyRepository;
    private final AccountVerificationRepository accountVerificationRepository;

    @Transactional
    public MultiPresignedUrlResponseDto registerStrategyAccountImages(
            Long strategyId,
            List<AccountImageRequestDto> requestDtoList,
            Long userId) {
        // 트레이더 정보 확인
        Strategy strategy = findStrategyById(strategyId);

        verifyUserPermission(strategy, userId);

        // 결과를 담을 리스트 생성
        List<PresignedUrlResponseDto> presignedUrlList = new ArrayList<>();

        // 요청된 각 파일에 대해 Presigned URL 생성 및 추가
        for (AccountImageRequestDto accountImageRequestDto : requestDtoList) {
            String filePath = s3FileService.getS3StrategyPath(
                    FilePath.STRATEGY_IMAGE,
                    strategyId,
                    accountImageRequestDto.getFileName(),
                    accountImageRequestDto.getFileSize()
            );

            // Presigned URL 생성 (예제 URL 생성)
            String presignedUrl = s3FileService.getPreSignedUrl(filePath);

            // PresignedUrlResponseDto 객체 생성 및 리스트에 추가
            PresignedUrlResponseDto presignedUrlResponseDto = PresignedUrlResponseDto.builder()
                    .presignedUrl(presignedUrl)
                    .build();

            AccountVerification accountVerification = AccountVerification.builder()
                    .strategy(strategy)
                    .title(accountImageRequestDto.getTitle())
                    .accountVerificationUrl(filePath)
                    .build();

            accountVerificationRepository.save(accountVerification);

            presignedUrlList.add(presignedUrlResponseDto);
        }

        return MultiPresignedUrlResponseDto.builder()
                .presignedUrls(presignedUrlList)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AccountImagesResponseDto> getAccountImagesByStrategyId(Long strategyId, Pageable pageable,
                                                                                  Long userId) {
        Strategy strategy = findStrategyById(strategyId);

        verifyUserPermission(strategy, userId);

        return new PageResponseDto<>(accountVerificationRepository.findByStrategyId(strategyId, pageable)
                .map(AccountImagesResponseDto::from));
    }

    @Transactional
    public void deleteStrategyAccountImages(Long strategyId, List<Long> requestDtoList, Long userId) {
        for (Long requestDtoId : requestDtoList) {
            AccountVerification targetImage = accountVerificationRepository.findById(requestDtoId).orElseThrow(() ->
                    new BusinessException(ErrorCode.ACCOUNT_IMAGE_NOT_FOUND));

            // 해당 실계좌 인증 이미지의 전략아이디가 요청한 전략아이디와 일치하지 않을경우
            if (!Objects.equals(targetImage.getStrategy().getStrategyId(), strategyId)) {
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
            }

            Strategy strategy = findStrategyById(targetImage.getStrategy().getStrategyId());

            verifyUserPermission(strategy, userId);

            accountVerificationRepository.delete(targetImage);
        }
    }

    private void verifyUserPermission(Strategy strategy, Long userId) {
        if (!strategy.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private Strategy findStrategyById(Long strategyId) {
        return strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STRATEGY_NOT_FOUND));
    }
}
