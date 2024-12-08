package com.investmetic.global.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.investmetic.domain.strategy.model.entity.DailyAnalysis;
import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.model.entity.StrategyStatistics;
import com.investmetic.domain.strategy.repository.StrategyStatisticsRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StrategyStatisticsSchedulerTest {

    @InjectMocks
    private StrategyStatisticsScheduler strategyStatisticsScheduler;

    @Mock
    private StrategyStatisticsRepository strategyStatisticsRepository;

    private List<DailyAnalysis> dailyAnalyses;

    private Strategy testStrategy;

    @BeforeEach
    void setUp() {
        testStrategy = Strategy.builder()
                .strategyId(1L) // 테스트용 전략 ID
                .build();

        dailyAnalyses = new ArrayList<>();

        // 현재 날짜 기준 3일치 간단한 테스트 데이터를 생성
        dailyAnalyses.add(DailyAnalysis.builder()
                .strategy(testStrategy)
                .dailyDate(LocalDate.now().minusDays(2)) // 첫 번째 데이터: 2일 전
                .transaction(100L) // 입출금 거래
                .dailyProfitLoss(50L) // 일간 손익
                .balance(1000L) // 잔고
                .cumulativeTransactionAmount(100L) // 누적 입출금
                .principal(1000L) // 원금
                .dailyProfitLossRate(5.0) // 일간 손익률
                .cumulativeProfitLoss(50L) // 누적 손익
                .maxDailyProfit(50L) // 최대 일간 이익
                .build());

        dailyAnalyses.add(DailyAnalysis.builder()
                .strategy(testStrategy)
                .dailyDate(LocalDate.now().minusDays(1)) // 두 번째 데이터: 1일 전
                .transaction(200L) // 입출금 거래
                .dailyProfitLoss(100L) // 일간 손익
                .balance(1200L) // 잔고
                .cumulativeTransactionAmount(300L) // 누적 입출금
                .principal(1000L) // 원금
                .dailyProfitLossRate(10.0) // 일간 손익률
                .cumulativeProfitLoss(150L) // 누적 손익
                .maxDailyProfit(100L) // 최대 일간 이익
                .build());

        dailyAnalyses.add(DailyAnalysis.builder()
                .strategy(testStrategy)
                .dailyDate(LocalDate.now()) // 세 번째 데이터: 오늘
                .transaction(300L) // 입출금 거래
                .dailyProfitLoss(150L) // 일간 손익
                .balance(1500L) // 잔고
                .cumulativeTransactionAmount(600L) // 누적 입출금
                .principal(1000L) // 원금
                .dailyProfitLossRate(15.0) // 일간 손익률
                .cumulativeProfitLoss(300L) // 누적 손익
                .maxDailyProfit(150L) // 최대 일간 이익
                .build());
    }

    @DisplayName("통계 계산 데이터값 테스트")
    @Test
    void testCalculateStatistics() {
        // When
        strategyStatisticsScheduler.calculateStatistics(dailyAnalyses);

        // Then
        ArgumentCaptor<StrategyStatistics> captor = ArgumentCaptor.forClass(StrategyStatistics.class);
        Mockito.verify(strategyStatisticsRepository).save(captor.capture());

        StrategyStatistics savedStatistics = captor.getValue();

        // 검증
        assertEquals(1500L,savedStatistics.getBalance()); // 현재 원금
        assertEquals(2, savedStatistics.getOperationPeriod()); // 운용 기간 (2일)
        assertEquals(600L, savedStatistics.getCumulativeTransactionAmount()); // 누적 입출금
        assertEquals(LocalDate.now().minusDays(2), savedStatistics.getStartDate()); // 시작일
        assertEquals(LocalDate.now(), savedStatistics.getEndDate()); // 종료일
        assertEquals(300L, savedStatistics.getCumulativeProfitAmount()); // 누적 수익
        assertEquals(5000.0, savedStatistics.getRecentYearProfitRate()); // 최근 1년 수익률
        assertEquals(150L, savedStatistics.getMaxDailyProfitAmount()); // 최대 일간 이익
        assertEquals(3, savedStatistics.getTotalTradeDays()); // 총 거래일수
        assertEquals(3, savedStatistics.getCurrentConsecutiveProfitLossDays()); // 현재 연속 손익일수
        assertEquals(3, savedStatistics.getMaxConsecutiveProfitDays()); // 최대 연속 이익 일수
        assertEquals(4.082, savedStatistics.getDailyProfitLossStdDev(), 0.001); // 표준편차

    }

    @DisplayName("통계가 존재할때 새로운 통계가 생성되지 않음")
    @Test
    void 통계존재_테스트1(){
        // Given
        Mockito.when(strategyStatisticsRepository.findById(testStrategy.getStrategyId()))
                .thenReturn(Optional.empty()); // 통계가 없는 경우

        // 계산
        strategyStatisticsScheduler.calculateStatistics(dailyAnalyses);

        // save 1번 호출
        Mockito.verify(strategyStatisticsRepository, Mockito.times(1))
                .save(Mockito.any(StrategyStatistics.class));

    }

    @DisplayName("통계가 존재하지않을때 통계가 새로 생성됨")
    @Test
    void 통계존재_테스트2(){
        // 이미 통계 존재
        StrategyStatistics existingStatistics = StrategyStatistics.builder()
                .strategyStatisticsId(1L)
                .build();

        Mockito.when(strategyStatisticsRepository.findById(testStrategy.getStrategyId()))
                .thenReturn(Optional.of(existingStatistics)); // 통계가 있는 경우

        // 계산
        strategyStatisticsScheduler.calculateStatistics(dailyAnalyses);

        // 통계가 없어서 save 호출x
        Mockito.verify(strategyStatisticsRepository, Mockito.times(0))
                .save(Mockito.any(StrategyStatistics.class));

    }
}