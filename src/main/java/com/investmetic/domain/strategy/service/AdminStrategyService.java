package com.investmetic.domain.strategy.service;

import com.investmetic.domain.strategy.dto.response.AdminStrategyResponseDto;
import com.investmetic.domain.strategy.model.IsApproved;
import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.repository.StrategyRepository;
import com.investmetic.global.common.PageResponseDto;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStrategyService {
    private final StrategyRepository strategyRepository;

    @Transactional
    public void manageAproveState(Long strategyId, IsApproved isApproved) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STRATEGY_NOT_FOUND));
        strategy.setIsApproved(isApproved);
    }

    @Transactional
    public PageResponseDto<AdminStrategyResponseDto> getManageStrategies(Pageable pageable, String searchWord, IsApproved isApproved) {
        return new PageResponseDto<>(strategyRepository.findAdminStrategies(pageable, searchWord, isApproved));
    }
}
