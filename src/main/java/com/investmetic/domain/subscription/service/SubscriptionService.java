package com.investmetic.domain.subscription.service;

import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.repository.StrategyRepository;
import com.investmetic.domain.subscription.model.entity.Subscription;
import com.investmetic.domain.subscription.repository.SubscriptionRepository;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final StrategyRepository strategyRepository;

    @Transactional
    public void SubScribe(Long strategyId, Long userId) {
        Strategy strategy = strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STRATEGY_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND));

        Optional<Subscription> existingSubscription = subscriptionRepository.findByStrategyIdAndUserId(strategyId, userId);
        if (existingSubscription.isPresent()) {
            strategy.minusSubscriptionCount();
            strategyRepository.save(strategy);
            subscriptionRepository.delete(existingSubscription.get());
            return;
        }
        strategy.plusSubscriptionCount();
        strategyRepository.save(strategy);
        Subscription subscription = Subscription.builder()
                .user(user)
                .strategy(strategy)
                .build();
        subscriptionRepository.save(subscription);
    }
}