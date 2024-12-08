package com.investmetic.global.scheduler;

import com.investmetic.domain.strategy.model.entity.DailyAnalysis;
import com.investmetic.domain.strategy.model.entity.Proceed;
import com.investmetic.domain.strategy.repository.DailyAnalysisRepository;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import com.investmetic.global.util.RoundUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DailyAnalysisScheduler {
    private final DailyAnalysisRepository dailyAnalysisRepository;

    @Transactional
    public void calculateDailyAnalysis(DailyAnalysis currentAnalysis) {
        // 1. 전날 데이터 가져오기
        Optional<DailyAnalysis> previousAnalysisOpt = dailyAnalysisRepository.findLatestBefore(
                currentAnalysis.getStrategy().getStrategyId(),
                currentAnalysis.getDailyDate()
        );

        if (previousAnalysisOpt.isEmpty()) {
            // 원금 (현재 거래 금액을 가져옴)
            Long principal = currentAnalysis.getTransaction();

            // 잔고 계산 (일간 손익 + 원금)
            Long balance = currentAnalysis.getDailyProfitLoss() + principal;

            // 평가손익 계산 (원금 - 잔고)
            Long valuationProfitLoss = principal - balance;

            // 기준가 및 일간 손익률 계산
            // 기준가(referencePrice) = 잔고 / 원금 * 1000, 원금이 0인 경우 0 반환
            double referencePrice = principal != 0 ? (double) balance / principal * 1000 : 0;
            // 일간 손익률 = (기준가 - 1000) / 1000
            double dailyProfitLossRate = (referencePrice - 1000) / 1000;

            // 최대 일간 이익 및 최대 일간 손실률 계산
            // 최대 일간 이익 (손익이 양수일 경우 해당 값, 그렇지 않으면 0)
            Long maxDailyProfit = Math.max(currentAnalysis.getDailyProfitLoss(), 0);

            Long maxDailyLoss = Math.min(currentAnalysis.getDailyProfitLoss(), 0);

            // 최대 일간 손실률 (손익률이 음수일 경우 해당 값, 양수일 경우 0)
            double maxDailyLossRate = dailyProfitLossRate < 0 ? dailyProfitLossRate : 0;

            // 총 이익 및 이익일 수 계산
            // 총 이익 = 최대 일간 이익
            Long totalProfit = maxDailyProfit;
            // 이익일 수 (총 이익이 양수면 1, 아니면 0)
            Long profitableDays = totalProfit > 0 ? 1L : 0;

            // 평균 이익 계산 (0으로 나누기 방지, 이익일 수가 0인 경우 0)
            Long averageProfit = profitableDays > 0 ? totalProfit / profitableDays : 0L;

            // 총 손실 및 손실일 수 계산
            // 총 손실 (일간 손익이 음수일 경우 해당 값, 그렇지 않으면 0)
            Long totalLoss = currentAnalysis.getDailyProfitLoss() < 0 ? currentAnalysis.getDailyProfitLoss() : 0;
            // 손실일 수 (총 손실이 음수면 1, 그렇지 않으면 0)
            Long lossDays = totalLoss < 0 ? 1L : 0;

            // 평균 손실 계산 (0으로 나누기 방지, 손실일 수가 0인 경우 0)
            Long averageLoss = lossDays > 0 ? totalLoss / lossDays : 0L;

            // 누적 손익 및 누적 손익률 계산
            // 누적 손익 (현재 일간 손익)
            Long cumulativeProfitLoss = currentAnalysis.getDailyProfitLoss();
            // 누적 손익률 = (기준가 / 1000) - 1
            Double cumulativeProfitLossRate = (referencePrice / 1000) - 1;

            // 최대 누적 손익 및 최대 누적 손익률 계산
            // 최대 누적 손익 (현재까지의 최대 손익)
            Long maxCumulativeProfitLoss = Math.max(cumulativeProfitLoss, 0);
            // 최대 누적 손익률 (기준가를 기준으로 계산)
            Double maxCumulativeProfitLossRate = (referencePrice / 1000) - 1;

            // 평균 손익 및 평균 손익률 계산
            Long averageProfitLoss = cumulativeProfitLoss;
            // 평균 손익률 (누적 손익률이 양수인 경우 해당 값)
            Double averageProfitLossRatio = Math.max(cumulativeProfitLossRate, 0);

            // 고점 및 고점 비율 계산
            Long peak = Math.max(cumulativeProfitLoss, 0);
            Double peakRatio = Math.max(cumulativeProfitLossRate, 0);

            // 고점 이후 경과 일수 (초기값 0)
            Long daysSincePeak = 0L;

            // 현재 자본인하 계산 (누적 손익이 양수인 경우 누적 손익 - 최대 누적 손익)
            Long currentDrawdown = cumulativeProfitLoss > 0 ? cumulativeProfitLoss - maxCumulativeProfitLoss : 0;
            // 현재 자본인하율 (초기값 0.0)
            Double currentDrawdownRate = 0.0;

            // 최대 자본인하 및 자본인하율 계산
            // 최대 자본인하 (현재 자본인하 중 최솟값)
            Long maxDrawdown = Math.min(currentDrawdown, 0);
            Double maxDrawdownRate = 0.0;

            // 승률 계산 (이익일 수를 퍼센트로 환산)
            Double winRate = (double) (profitableDays) * 100;

            // Profit Factor 계산 (총 손실이 음수인 경우 총 이익 / 총 손실 절대값)
            Double profitFactor = totalLoss < 0 ? (double) totalProfit / Math.abs(totalLoss) : 0;

            // ROA (Return on Assets) 계산 (누적 손익을 최대 자본인하로 나눈 값의 음수)
            Double roa = maxDrawdown != 0 ? (double) cumulativeProfitLoss / maxDrawdown * -1 : 0;

            // 변동계수 (초기값 0.0)
            Double coefficientOfVariation = 0.0;

            // 샤프 비율 (초기값 0.0)
            Double sharpRatio = 0.0;

            DailyAnalysis dailyAnalysis = DailyAnalysis.builder()
                    .dailyAnalysisId(currentAnalysis.getDailyAnalysisId())
                    .tradingDays(1)
                    .strategy(currentAnalysis.getStrategy())
                    .dailyDate(currentAnalysis.getDailyDate())
                    .transaction(currentAnalysis.getTransaction())
                    .dailyProfitLoss(currentAnalysis.getDailyProfitLoss())
                    .principal(principal)
                    .balance(balance)
                    .valuationProfitLoss(valuationProfitLoss)
                    .referencePrice(RoundUtil.roundToFifth(referencePrice))
                    .cumulativeTransactionAmount(0L)
                    .deposit(0L)
                    .cumulativeDeposit(0L)
                    .withdrawal(0L)
                    .cumulativeWithdrawal(0L)
                    .maxDailyLoss(maxDailyLoss)
                    .dailyProfitLossRate(RoundUtil.roundToFifth(dailyProfitLossRate))
                    .maxDailyProfit(maxDailyProfit)
                    .maxDailyProfitRate(RoundUtil.roundToFifth(maxDailyLossRate))
                    .maxDailyLossRate(RoundUtil.roundToFifth(maxDailyLossRate))
                    .totalProfit(totalProfit)
                    .profitableDays(profitableDays)
                    .averageProfit(averageProfit)
                    .totalLoss(totalLoss)
                    .lossDays(lossDays)
                    .averageLoss(averageLoss)
                    .cumulativeProfitLoss(cumulativeProfitLoss)
                    .cumulativeProfitLossRate(RoundUtil.roundToFifth(cumulativeProfitLossRate))
                    .maxCumulativeProfitLoss(maxCumulativeProfitLoss)
                    .maxCumulativeProfitLossRate(RoundUtil.roundToFifth(maxCumulativeProfitLossRate))
                    .averageProfitLoss(averageProfitLoss)
                    .averageProfitLossRatio(RoundUtil.roundToFifth(averageProfitLossRatio))
                    .peak(peak)
                    .peakRatio(RoundUtil.roundToFifth(peakRatio))
                    .daysSincePeak(daysSincePeak)
                    .currentDrawdown(currentDrawdown)
                    .currentDrawdownRate(RoundUtil.roundToFifth(currentDrawdownRate))
                    .maxDrawdown(maxDrawdown)
                    .maxDrawdownRate(RoundUtil.roundToFifth(maxDrawdownRate))
                    .winRate(winRate)
                    .profitFactor(RoundUtil.roundToFifth(profitFactor))
                    .roa(RoundUtil.roundToFifth(roa))
                    .coefficientOfVariation(RoundUtil.roundToFifth(coefficientOfVariation))
                    .sharpRatio(RoundUtil.roundToFifth(sharpRatio))
                    .proceed(Proceed.YES)
                    .build();
            dailyAnalysisRepository.save(dailyAnalysis);
            return;
        }

        // 2. 이전 데이터 기반 계산

        // 해당 날짜 이전 전략 일간 분석 모든 데이터
        List<DailyAnalysis> beforeDatas = dailyAnalysisRepository.findAllByStrategyAndDateBefore(
                currentAnalysis.getStrategy().getStrategyId(),
                currentAnalysis.getDailyDate());

        LocalDate dailyDate = currentAnalysis.getDailyDate();
        Long transaction = currentAnalysis.getTransaction();
        Long dailyProfitLoss = currentAnalysis.getDailyProfitLoss();
        int previousTradingDays = previousAnalysisOpt.map(DailyAnalysis::getTradingDays)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.STRATEGY_NOT_FOUND));

        Long previousPrincipal = previousAnalysisOpt.map(DailyAnalysis::getPrincipal)
                .orElse(0L);

        Long previousPeak = previousAnalysisOpt.map(DailyAnalysis::getPeak)
                .orElse(0L);

        Long previousDaysSincePeak = previousAnalysisOpt.map(DailyAnalysis::getDaysSincePeak)
                .orElse(0L);

        Long previousBalance = previousAnalysisOpt.map(DailyAnalysis::getBalance)
                .orElse(0L);

        double previousDailyProfitLossRate = previousAnalysisOpt.map(DailyAnalysis::getDailyProfitLossRate)
                .orElse(0.0);

        double previousCurrentDrawdownRate = previousAnalysisOpt.map(DailyAnalysis::getCurrentDrawdownRate)
                .orElse(0.0);

        double previousReferencePrice = previousAnalysisOpt.map(DailyAnalysis::getReferencePrice)
                .orElse(0.0);

        // 원금
        Long principal = (previousPrincipal != 0 && previousBalance != 0 && transaction != 0)
                ? previousPrincipal + (long) (transaction / ((double) previousBalance / previousPrincipal))
                : previousPrincipal;

        // 잔고
        Long balance = previousBalance + transaction + dailyProfitLoss;

        // 평가손익
        Long valuationProfitLoss = principal - balance;

        // 기준가
        double referencePrice = (principal != 0) ? (double) balance / principal * 1000 : 0.0;

        // 입출금 합
        // 누적 입출금
        Long cumulativeTransactionAmount = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getTransaction)
                .sum();

        // 입금
        Long deposit = transaction > 0 ? transaction : 0;

        // 누적입금
        Long cumulativeDeposit = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getDeposit)
                .sum();

        // 출금
        Long withdrawal = transaction < 0 ? transaction : 0;

        // 누적출금
        Long cumulativeWithdrawal = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getWithdrawal)
                .sum();

        // 일간 손익률
        double dailyProfitLossRate = (previousReferencePrice != 0)
                ? (referencePrice - previousReferencePrice) / previousReferencePrice
                : 0.0;

        // 최대 일간 이익
        Long maxDailyProfit = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getDailyProfitLoss)
                .filter(profitLoss -> profitLoss > 0)
                .max()
                .orElse(0L);

        // 최대 일간 이익률
        double maxDailyProfitRate = beforeDatas.stream()
                .mapToDouble(DailyAnalysis::getDailyProfitLossRate)
                .filter(rate -> rate > 0)
                .max()
                .orElse(0.0);

        // 최대 일간 손실
        long maxDailyLoss = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getDailyProfitLoss)
                .filter(profitLoss -> profitLoss < 0)
                .min()
                .orElse(0L);

        // 최대 일간 손실률
        double maxDailyLossRate = beforeDatas.stream()
                .mapToDouble(DailyAnalysis::getDailyProfitLossRate)
                .filter(rate -> rate < 0)
                .min()
                .orElse(0.0);

        // 총 이익
        long totalProfit = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getDailyProfitLoss)
                .filter(profit -> profit > 0)
                .sum();

        // 이익일수
        long profitableDays = beforeDatas.stream()
                .filter(data -> data.getDailyProfitLoss() > 0)
                .count();

        // 평균이익
        long averageProfit = profitableDays > 0 ? totalProfit / profitableDays : 0;

        // 총 손실
        long totalLoss = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getDailyProfitLoss)
                .filter(profit -> profit < 0)
                .sum();

        // 손실일수
        long lossDays = beforeDatas.stream()
                .filter(data -> data.getDailyProfitLoss() < 0)
                .count();

        // 평균 손실
        long averageLoss = lossDays > 0 ? totalLoss / lossDays : 0;

        // 누적손익
        long cumulativeProfitLoss = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getDailyProfitLoss)
                .sum();

        // 누적손익률
        double cumulativeProfitLossRate = referencePrice / 1000 - 1;

        // 최대 누적 손익
        long maxCumulativeProfitLoss = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getCumulativeProfitLoss)
                .filter(value -> value > 0)
                .max()
                .orElse(0L);

        // 최대 누적 손익률
        double maxCumulativeProfitLossRate = beforeDatas.stream()
                .mapToDouble(DailyAnalysis::getCumulativeProfitLossRate)
                .filter(value -> value > 0)
                .max()
                .orElse(0.0);

        // 평균 손익
        long averageProfitLoss = cumulativeProfitLoss / (previousTradingDays + 1);

        // 평균 손익 비율
        double averageProfitLossRatio = cumulativeProfitLossRate / (previousTradingDays + 1);

        // 최고값
        long peak = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getCumulativeProfitLoss)
                .filter(value -> value > 0)
                .max()
                .orElse(0L);

        // 최고값 비율
        double peakRatio = beforeDatas.stream()
                .mapToDouble(DailyAnalysis::getCumulativeProfitLossRate)
                .filter(value -> value > 0)
                .max()
                .orElse(0.0);

        // 현재 자본 인하 금액
        long currentDrawdown = cumulativeProfitLoss > 0 ? cumulativeProfitLoss - maxCumulativeProfitLoss : 0;

        // 현재 자본인하율
        double maxReferencePrice = beforeDatas.stream()
                .mapToDouble(DailyAnalysis::getReferencePrice)
                .filter(value -> value > 0)
                .max()
                .orElse(0.0);
        double currentDrawdownRate = referencePrice - 1000 > 0 ? (referencePrice - maxReferencePrice) / 100 : 0;

        // 최대 자본인하금액
        Long maxDrawdown = beforeDatas.stream()
                .mapToLong(DailyAnalysis::getCurrentDrawdown)
                .filter(value -> value < 0)
                .min()
                .orElse(0L);

        // 최대 자본인하율
        double maxDrawdownRate = beforeDatas.stream()
                .mapToDouble(DailyAnalysis::getCurrentDrawdownRate)
                .filter(value -> value < 0)
                .min()
                .orElse(0.0);

        // 승률
        double winRate = (double) profitableDays / (previousTradingDays + 1);

        // profitFactor
        double profitFactor = totalLoss < 0 ? totalProfit / Math.abs(totalLoss) : 0;

        // roa
        double roa = maxDrawdown != 0
                ? (double) cumulativeProfitLoss / maxDrawdown * -1
                : 0.0;

        // 고점 후 경과일
        Long daysSincePeak = (peak == previousPeak && peak > 0)
                ? previousDaysSincePeak + 1
                : 0;

        // 변동계수
        double coefficientOfVariation = (averageProfitLoss != 0)
                ? Math.sqrt(beforeDatas.stream()
                .mapToDouble(DailyAnalysis::getDailyProfitLoss)
                .map(x -> Math.pow(x - beforeDatas.stream()
                        .mapToDouble(DailyAnalysis::getDailyProfitLoss)
                        .average()
                        .orElse(0.0), 2))
                .average()
                .orElse(0.0)) / averageProfitLoss * 100
                : 0.0;

        // sharp 비율
        double standardDeviation = Math.sqrt(
                beforeDatas.stream()
                        .mapToDouble(DailyAnalysis::getDailyProfitLoss)
                        .map(x -> Math.pow(x - beforeDatas.stream()
                                .mapToDouble(DailyAnalysis::getDailyProfitLoss)
                                .average()
                                .orElse(0.0), 2))
                        .average()
                        .orElse(0.0)
        );

        double sharpRatio = (standardDeviation != 0) ? (double) averageProfitLoss / standardDeviation : 0.0;

        Long previousDrawDownPeriod = previousAnalysisOpt.map(DailyAnalysis::getDrawDownPeriod)
                .orElse(0L);

        Long drawDownPeriod = previousCurrentDrawdownRate >= 0 ? 1 : previousDrawDownPeriod + 1;

        List<DailyAnalysis> specificDailyAnalyses = dailyAnalysisRepository.findSpecificDailyAnalyses(
                currentAnalysis.getStrategy().getStrategyId(),
                currentAnalysis.getDailyDate(),
                drawDownPeriod);

        double maxDrawDownInRate = Math.min(specificDailyAnalyses.stream()
                .mapToDouble(DailyAnalysis::getCurrentDrawdownRate)
                .filter(value -> value < 0)
                .min()
                .orElse(0.0), currentDrawdownRate);

        // 9. 새로운 DailyAnalysis 객체 생성 및 반환
        DailyAnalysis dailyAnalysis = DailyAnalysis.builder()
                .strategy(currentAnalysis.getStrategy())
                .dailyAnalysisId(currentAnalysis.getDailyAnalysisId())
                .dailyDate(dailyDate)
                .transaction(transaction)
                .dailyProfitLoss(dailyProfitLoss)
                .tradingDays(previousTradingDays + 1)
                .principal(principal)
                .balance(balance)
                .valuationProfitLoss(valuationProfitLoss)
                .referencePrice(RoundUtil.roundToFifth(referencePrice))
                .cumulativeTransactionAmount(cumulativeTransactionAmount)
                .deposit(deposit)
                .cumulativeDeposit(cumulativeDeposit)
                .withdrawal(withdrawal)
                .cumulativeWithdrawal(cumulativeWithdrawal)
                .dailyProfitLossRate(Math.round(dailyProfitLossRate * 10000) / 10000.0)
                .maxDailyProfit(maxDailyProfit)
                .maxDailyProfitRate(RoundUtil.roundToFifth(maxDailyProfitRate))
                .maxDailyLossRate(RoundUtil.roundToFifth(maxDailyLossRate))
                .totalProfit(totalProfit)
                .profitableDays(profitableDays)
                .averageProfit(averageProfit)
                .totalLoss(totalLoss)
                .lossDays(lossDays)
                .averageLoss(averageLoss)
                .cumulativeProfitLoss(cumulativeProfitLoss)
                .cumulativeProfitLossRate(RoundUtil.roundToFifth(cumulativeProfitLossRate))
                .maxCumulativeProfitLoss(maxCumulativeProfitLoss)
                .maxCumulativeProfitLossRate(RoundUtil.roundToFifth(maxCumulativeProfitLossRate))
                .averageProfitLoss(averageProfitLoss)
                .averageProfitLossRatio(RoundUtil.roundToFifth(averageProfitLossRatio))
                .peak(peak)
                .daysSincePeak(daysSincePeak)
                .peakRatio(RoundUtil.roundToFifth(peakRatio))
                .currentDrawdown(currentDrawdown)
                .currentDrawdownRate(RoundUtil.roundToFifth(currentDrawdownRate))
                .maxDrawdown(maxDrawdown)
                .maxDrawdownRate(RoundUtil.roundToFifth(maxDrawdownRate))
                .winRate(RoundUtil.roundToFifth(winRate))
                .profitFactor(RoundUtil.roundToFifth(profitFactor))
                .roa(RoundUtil.roundToFifth(roa))
                .coefficientOfVariation(RoundUtil.roundToFifth(coefficientOfVariation))
                .sharpRatio(RoundUtil.roundToFifth(sharpRatio))
                .maxDrawDownInRate(RoundUtil.roundToFifth(maxDrawDownInRate))
                .drawDownPeriod(drawDownPeriod)
                .maxDailyLoss(maxDailyLoss)
                .proceed(Proceed.YES)
                .build();

        dailyAnalysisRepository.save(dailyAnalysis);
    }
}
