package com.investmetic.domain.strategy.model.entity;

import com.investmetic.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StrategyStatistics extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long strategyStatisticsId;

    private Long balance; // 잔고

    private Integer operationPeriod; // 운용 기간(일수로 변경)

    private Long cumulativeTransactionAmount; // 누적 입출금액

    private LocalDate startDate; // 시작일

    private Long principal; // 원금

    private LocalDate endDate; // 종료일

    private Long daysSincePeakUpdate; // 최고점 이후 경과일

    private Long cumulativeProfitAmount; // 누적 수익금액

    private Double cumulativeProfitRate; // 누적 수익률

    private Double recentYearProfitRate; // 최근 1년 수익률

    private Long maxCumulativeProfitAmount; // 최대 누적 수익금액

    private Double maxCumulativeProfitRate; // 최대 누적 수익률

    private Long averageProfitLossAmount; // 평균 손익 금액

    private Double averageProfitLossRate; // 평균 손익률

    private Long maxDailyProfitAmount; // 최대 일수익 금액

    private Double maxDailyProfitRate; // 최대 일 수익률

    private Long maxDailyLossAmount; // 최대 일손실 금액

    private Double maxDailyLossRate; // 최대 일 손실률

    private Double roa; // ROA

    private Double profitFactor; // Profit Factor

    private Long currentDrawdown; // 현재 자본 인하 금액

    private Double currentDrawdownRate; // 현재 자본 인하율

    private Long maxDrawdown; // 최대 자본 인하 금액

    private Double maxDrawdownRate; // 최대 자본 인하율

    private Integer currentConsecutiveProfitLossDays; // 현재 연속 손익일수

    private Integer totalProfitableDays; // 총 이익 일수

    private Integer maxConsecutiveProfitDays; // 최대 연속 이익 일수

    private Integer totalLossDays; // 총 손실 일수

    private Integer maxConsecutiveLossDays; // 최대 연속 손실 일수

    private Double winRate; // 승률

    private Integer totalTradeDays; // 총 매매일수

    private Double dailyProfitLossStdDev; // 수익률 표준편차 (방어형 전략 알고리즘 필요)

    private Integer mddRank; // MDD 순위 (방어형 전략 알고리즘 필요)

    private Integer stdDevRank; // 표준편차 순위 (방어형 전략 알고리즘 필요)

    private Integer winRateRank; // 승률 순위 (방어형 전략 알고리즘 필요)

    private Long initialInvestment; // 최초 투자금액

    @Builder
    public StrategyStatistics(Long strategyStatisticsId, Long balance, Integer operationPeriod,
                              Long cumulativeTransactionAmount, LocalDate startDate,
                              Long principal, LocalDate endDate, Long daysSincePeakUpdate,
                              Long cumulativeProfitAmount,
                              Double cumulativeProfitRate, Double recentYearProfitRate, Long maxCumulativeProfitAmount,
                              Double maxCumulativeProfitRate, Long averageProfitLossAmount,
                              Double averageProfitLossRate,
                              Long maxDailyProfitAmount, Double maxDailyProfitRate, Long maxDailyLossAmount,
                              Double maxDailyLossRate, Double roa, Double profitFactor, Long currentDrawdown,
                              Double currentDrawdownRate, Long maxDrawdown, Double maxDrawdownRate,
                              Integer currentConsecutiveProfitLossDays, Integer totalProfitableDays,
                              Integer maxConsecutiveProfitDays, Integer totalLossDays, Integer maxConsecutiveLossDays,
                              Double winRate, Integer totalTradeDays, Double dailyProfitLossStdDev,
                              Integer mddRank, Integer stdDevRank, Integer winRateRank, Long initialInvestment) {
        this.strategyStatisticsId = strategyStatisticsId;
        this.balance = balance;
        this.operationPeriod = operationPeriod;
        this.cumulativeTransactionAmount = cumulativeTransactionAmount;
        this.startDate = startDate;
        this.principal = principal;
        this.endDate = endDate;
        this.daysSincePeakUpdate = daysSincePeakUpdate;
        this.cumulativeProfitAmount = cumulativeProfitAmount;
        this.cumulativeProfitRate = cumulativeProfitRate;
        this.recentYearProfitRate = recentYearProfitRate;
        this.maxCumulativeProfitAmount = maxCumulativeProfitAmount;
        this.maxCumulativeProfitRate = maxCumulativeProfitRate;
        this.averageProfitLossAmount = averageProfitLossAmount;
        this.averageProfitLossRate = averageProfitLossRate;
        this.maxDailyProfitAmount = maxDailyProfitAmount;
        this.maxDailyProfitRate = maxDailyProfitRate;
        this.maxDailyLossAmount = maxDailyLossAmount;
        this.maxDailyLossRate = maxDailyLossRate;
        this.roa = roa;
        this.profitFactor = profitFactor;
        this.currentDrawdown = currentDrawdown;
        this.currentDrawdownRate = currentDrawdownRate;
        this.maxDrawdown = maxDrawdown;
        this.maxDrawdownRate = maxDrawdownRate;
        this.currentConsecutiveProfitLossDays = currentConsecutiveProfitLossDays;
        this.totalProfitableDays = totalProfitableDays;
        this.maxConsecutiveProfitDays = maxConsecutiveProfitDays;
        this.totalLossDays = totalLossDays;
        this.maxConsecutiveLossDays = maxConsecutiveLossDays;
        this.winRate = winRate;
        this.totalTradeDays = totalTradeDays;
        this.dailyProfitLossStdDev = dailyProfitLossStdDev;
        this.mddRank = mddRank;
        this.stdDevRank = stdDevRank;
        this.winRateRank = winRateRank;
        this.initialInvestment = initialInvestment;
    }

    public void updateExistingStatistics(StrategyStatistics updated) {
        this.operationPeriod = updated.getOperationPeriod();
        this.cumulativeTransactionAmount = updated.getCumulativeTransactionAmount();
        this.startDate = updated.getStartDate();
        this.principal = updated.getPrincipal();
        this.endDate = updated.getEndDate();
        this.daysSincePeakUpdate = updated.getDaysSincePeakUpdate();
        this.cumulativeProfitAmount = updated.getCumulativeProfitAmount();
        this.recentYearProfitRate = updated.getRecentYearProfitRate();
        this.maxCumulativeProfitAmount = updated.getMaxCumulativeProfitAmount();
        this.maxCumulativeProfitRate = updated.getMaxCumulativeProfitRate();
        this.averageProfitLossAmount = updated.getAverageProfitLossAmount();
        this.averageProfitLossRate = updated.getAverageProfitLossRate();
        this.maxDailyProfitAmount = updated.getMaxDailyProfitAmount();
        this.maxDailyProfitRate = updated.getMaxDailyProfitRate();
        this.maxDailyLossRate = updated.getMaxDailyLossRate();
        this.roa = updated.getRoa();
        this.profitFactor = updated.getProfitFactor();
        this.currentDrawdown = updated.getCurrentDrawdown();
        this.currentDrawdownRate = updated.getCurrentDrawdownRate();
        this.maxDrawdown = updated.getMaxDrawdown();
        this.maxDrawdownRate = updated.getMaxDrawdownRate();
        this.currentConsecutiveProfitLossDays = updated.getCurrentConsecutiveProfitLossDays();
        this.totalProfitableDays = updated.getTotalProfitableDays();
        this.maxConsecutiveProfitDays = updated.getMaxConsecutiveProfitDays();
        this.totalLossDays = updated.getTotalLossDays();
        this.maxConsecutiveLossDays = updated.getMaxConsecutiveLossDays();
        this.winRate = updated.getWinRate();
        this.totalTradeDays = updated.getTotalTradeDays();
        this.dailyProfitLossStdDev = updated.getDailyProfitLossStdDev();
        this.initialInvestment = updated.initialInvestment;
    }
}