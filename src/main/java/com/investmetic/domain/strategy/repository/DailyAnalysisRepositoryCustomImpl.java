package com.investmetic.domain.strategy.repository;

import static com.investmetic.domain.strategy.model.entity.QDailyAnalysis.dailyAnalysis;

import com.investmetic.domain.strategy.dto.response.StrategyAnalysisResponse;
import com.investmetic.domain.strategy.model.AnalysisOption;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DailyAnalysisRepositoryCustomImpl implements DailyAnalysisRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public StrategyAnalysisResponse findStrategyAnalysis(Long strategyId, AnalysisOption option1,
                                                         AnalysisOption option2) {

        // x축 데이터 조회
        List<String> xAxis = findXAxis(strategyId);

        // y축 데이터 조회
        List<Double> firstYAxis = findYAxis(strategyId, option1);
        List<Double> SecondYAxis = findYAxis(strategyId, option2);

        Map<String, List<Double>> yaxis = Map.of(
                option1.name(), firstYAxis,
                option2.name(), SecondYAxis
        );

        return StrategyAnalysisResponse.builder()
                .xAxis(xAxis)
                .yAxis(yaxis)
                .build();
    }

    @Override
    public List<String> findXAxis(Long strategyId) {
        return queryFactory
                .select(dailyAnalysis.dailyDate.stringValue())
                .from(dailyAnalysis)
                .where(dailyAnalysis.strategy.strategyId.eq(strategyId))
                .orderBy(dailyAnalysis.dailyDate.asc())
                .fetch();
    }

    @Override
    public List<Double> findYAxis(Long strategyId, AnalysisOption option) {
        return queryFactory
                .select(findByOption(option))
                .from(dailyAnalysis)
                .where(dailyAnalysis.strategy.strategyId.eq(strategyId))
                .fetch();
    }

    private NumberExpression<Double> findByOption(AnalysisOption option) {
        switch (option) {
            case BALANCE -> {
                return dailyAnalysis.balance.doubleValue();
            }
            case PRINCIPAL -> {
                return dailyAnalysis.principal.doubleValue();
            }
            case CUMULATIVE_TRANSACTION_AMOUNT -> {
                return dailyAnalysis.cumulativeTransactionAmount.doubleValue();
            }
            case TRANSACTION -> {
                return dailyAnalysis.transaction.doubleValue();
            }

            case DAILY_PROFIT_LOSS -> {
                return dailyAnalysis.dailyProfitLoss.doubleValue();
            }
            case DAILY_PROFIT_LOSS_RATE -> {
                return dailyAnalysis.dailyProfitLossRate;
            }

            case CUMULATIVE_PROFIT_LOSS -> {
                return dailyAnalysis.cumulativeProfitLoss.doubleValue();
            }
            case CUMULATIVE_PROFIT_LOSS_RATE -> {
                return dailyAnalysis.cumulativeProfitLossRate;
            }
            case CURRENT_DRAWDOWN -> {
                return dailyAnalysis.currentDrawdown.doubleValue();
            }
            case CURRENT_DRAWDOWN_RATE -> {
                return dailyAnalysis.currentDrawdownRate;
            }
            case AVERAGE_PROFIT_LOSS -> {
                return dailyAnalysis.averageProfitLoss.doubleValue();
            }
            case AVERAGE_PROFIT_LOSS_RATIO -> {
                return dailyAnalysis.averageProfitLossRatio;
            }
            case WIN_RATE -> {
                return dailyAnalysis.winRate;
            }
            case PROFIT_FACTOR -> {
                return dailyAnalysis.profitFactor;
            }
            case ROA -> {
                return dailyAnalysis.roa;
            }
            case TOTAL_PROFIT -> {
                return dailyAnalysis.totalProfit.doubleValue();
            }
            case TOTAL_LOSS -> {
                return dailyAnalysis.totalLoss.doubleValue();
            }
            default -> {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }

        }

    }

}