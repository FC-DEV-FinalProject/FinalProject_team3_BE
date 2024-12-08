package com.investmetic.global.scheduler;

import com.investmetic.domain.strategy.model.entity.DailyAnalysis;
import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.model.entity.StrategyStatistics;
import com.investmetic.domain.strategy.repository.StrategyStatisticsRepository;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StrategyStatisticsScheduler {

    private final StrategyStatisticsRepository strategyStatisticsRepository;

    public void calculateStatistics(List<DailyAnalysis> dailyAnalyses) {

        // 처음 일간 분석 데이터
        DailyAnalysis firstDailyAnalysis = dailyAnalyses.stream()
                .min(Comparator.comparing(DailyAnalysis::getDailyDate))
                .orElseThrow(() -> new BusinessException(ErrorCode.DAILY_ANALYSIS_QUERY_FAILED));

        // 마지막 일간 분석 데이터
        DailyAnalysis lastDailyAnalysis = dailyAnalyses.stream()
                .max(Comparator.comparing(DailyAnalysis::getDailyDate))
                .orElseThrow(() -> new BusinessException(ErrorCode.DAILY_ANALYSIS_QUERY_FAILED));

        StrategyStatistics calculatedStatistics = createStrategyStatistics(dailyAnalyses, firstDailyAnalysis,
                lastDailyAnalysis);

        Optional<StrategyStatistics> optionalStrategyStatistics = strategyStatisticsRepository.findById(
                firstDailyAnalysis.getStrategy().getStrategyId());

        Strategy strategy = firstDailyAnalysis.getStrategy();

        // 이미 존재하는 통계일때
        if (optionalStrategyStatistics.isPresent()) {
            // 기존 통계가 있으면 업데이트
            StrategyStatistics existingStatistics = optionalStrategyStatistics.get();
            existingStatistics.updateExistingStatistics(calculatedStatistics);
            return;
        }

        // 새 통계 생성 후 설정
        strategy.setStrategyStatistics(calculatedStatistics);
        strategyStatisticsRepository.save(calculatedStatistics);

        // mdd, 수익률표준편차, 승률 순위 업데이트 쿼리
        strategyStatisticsRepository.updateRanks();
    }

    /**
     * 전략 통계 계산 및 엔티티 생성
     */
    private StrategyStatistics createStrategyStatistics(List<DailyAnalysis> dailyAnalyses,
                                                        DailyAnalysis firstDailyAnalysis,
                                                        DailyAnalysis lastDailyAnalysis) {

        // 시작일
        LocalDate startDate = firstDailyAnalysis.getDailyDate();

        // 종료일
        LocalDate endDate = lastDailyAnalysis.getDailyDate();

        int operationPeriod = (int) ChronoUnit.DAYS.between(firstDailyAnalysis.getDailyDate(),
                lastDailyAnalysis.getDailyDate());
        Double recentYearProfitRate = calculateRecentYearProfitRate(dailyAnalyses);
        int currentConsecutiveProfitLossDays = calculateCurrentConsecutiveProfitLossDays(dailyAnalyses);
        int maxConsecutiveProfitDays = calculateMaxConsecutiveDays(dailyAnalyses, true);
        int maxConsecutiveLossDays = calculateMaxConsecutiveDays(dailyAnalyses, false);
        int totalTradeDays = dailyAnalyses.size();
        double dailyProfitLossStdDev = calculateStdDev(
                dailyAnalyses.stream()
                        .map(DailyAnalysis::getDailyProfitLossRate)
                        .toList()
        );

        // 빌더를 이용해 객체 생성
        return StrategyStatistics.builder()
                .balance(lastDailyAnalysis.getBalance())
                .operationPeriod(operationPeriod)
                .cumulativeTransactionAmount(lastDailyAnalysis.getCumulativeTransactionAmount())
                .startDate(startDate)
                .principal(lastDailyAnalysis.getPrincipal())
                .endDate(endDate)
                .daysSincePeakUpdate(lastDailyAnalysis.getDaysSincePeak())
                .cumulativeProfitAmount(lastDailyAnalysis.getCumulativeProfitLoss()) // 누적 수익 금액
                .cumulativeProfitRate(lastDailyAnalysis.getCumulativeProfitLossRate() * 100) // 누적 수익률
                .recentYearProfitRate(recentYearProfitRate * 100) //최근 1년 수익률
                .maxCumulativeProfitAmount(lastDailyAnalysis.getMaxCumulativeProfitLoss())
                .maxCumulativeProfitRate(lastDailyAnalysis.getMaxCumulativeProfitLossRate() * 100) // 최대 누적 수익률
                .averageProfitLossAmount(lastDailyAnalysis.getAverageProfitLoss())
                .averageProfitLossRate(lastDailyAnalysis.getAverageProfitLossRatio() * 100) // 평균 손익률
                .maxDailyProfitAmount(lastDailyAnalysis.getMaxDailyProfit())
                .maxDailyProfitRate(lastDailyAnalysis.getMaxDailyProfitRate() * 100) // 최대 일간 수익률
                .maxDailyLossAmount(lastDailyAnalysis.getMaxDailyLoss())
                .maxDailyLossRate(lastDailyAnalysis.getMaxDailyLossRate() * 100) // 최대 일간 손실률
                .roa(lastDailyAnalysis.getRoa())
                .profitFactor(lastDailyAnalysis.getProfitFactor())
                .currentDrawdown(lastDailyAnalysis.getCurrentDrawdown())
                .currentDrawdownRate(lastDailyAnalysis.getCurrentDrawdownRate())
                .maxDrawdown(lastDailyAnalysis.getMaxDrawdown())
                .maxDrawdownRate(lastDailyAnalysis.getMaxDrawdownRate())
                .currentConsecutiveProfitLossDays(currentConsecutiveProfitLossDays)
                .totalProfitableDays(lastDailyAnalysis.getProfitableDays().intValue())
                .maxConsecutiveProfitDays(maxConsecutiveProfitDays)
                .totalLossDays(lastDailyAnalysis.getLossDays().intValue())
                .maxConsecutiveLossDays(maxConsecutiveLossDays)
                .winRate(lastDailyAnalysis.getWinRate() * 100)
                .totalTradeDays(totalTradeDays)
                .dailyProfitLossStdDev(dailyProfitLossStdDev)
                .initialInvestment(firstDailyAnalysis.getPrincipal())
                .build();
    }

    /**
     * 최근 1년 수익률
     */
    private Double calculateRecentYearProfitRate(List<DailyAnalysis> dailyAnalyses) {

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        List<DailyAnalysis> recentYearAnalyses = dailyAnalyses.stream()
                .filter(d -> !d.getDailyDate().isBefore(oneYearAgo))
                .toList();

        // 최근 1년 수익률이 없으면 0.0반환
        if (recentYearAnalyses.isEmpty()) {
            return 0.0;
        }

        double startBalance = recentYearAnalyses.stream()
                .min(Comparator.comparing(DailyAnalysis::getDailyDate))
                .map(DailyAnalysis::getBalance)
                .orElse(0L);

        double endBalance = recentYearAnalyses.stream()
                .max(Comparator.comparing(DailyAnalysis::getDailyDate))
                .map(DailyAnalysis::getBalance)
                .orElse(0L);

        return (endBalance - startBalance) / startBalance * 100;
    }

    /**
     * 현재 연속 손익일수
     */
    private int calculateCurrentConsecutiveProfitLossDays(List<DailyAnalysis> dailyAnalyses) {

        List<DailyAnalysis> sortedByDateDesc = dailyAnalyses.stream()
                .sorted(Comparator.comparing(DailyAnalysis::getDailyDate).reversed())
                .toList();

        int currentConsecutiveProfitLossDays = 0;

        for (DailyAnalysis dailyAnalysis : sortedByDateDesc) {
            if (dailyAnalysis.getDailyProfitLoss() > 0) {
                currentConsecutiveProfitLossDays++;
            } else {
                break;
            }
        }

        return currentConsecutiveProfitLossDays;
    }

    /**
     * 최대 연속 이익, 손실 일수 계산
     */
    private int calculateMaxConsecutiveDays(List<DailyAnalysis> dailyAnalyses, boolean isProfit) {
        int maxStreak = 0, currentStreak = 0;

        for (DailyAnalysis dailyAnalysis : dailyAnalyses) {
            if ((isProfit && dailyAnalysis.getDailyProfitLoss() > 0) ||
                    (!isProfit && dailyAnalysis.getDailyProfitLoss() < 0)) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return maxStreak;
    }


    /**
     * 수익률 표준편차 계산
     *
     * @param dailyProfits : 일간수익률 리스트
     * @return
     */
    private double calculateStdDev(List<Double> dailyProfits) {
        if (dailyProfits.isEmpty()) {
            return 0.0;
        }

        double avg = dailyProfits.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double variance = dailyProfits.stream()
                .mapToDouble(value -> Math.pow(value - avg, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }
}
