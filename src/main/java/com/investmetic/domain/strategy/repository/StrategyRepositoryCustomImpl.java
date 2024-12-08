
package com.investmetic.domain.strategy.repository;

import static com.investmetic.domain.strategy.model.entity.QDailyAnalysis.dailyAnalysis;
import static com.investmetic.domain.strategy.model.entity.QStockType.stockType;
import static com.investmetic.domain.strategy.model.entity.QStockTypeGroup.stockTypeGroup;
import static com.investmetic.domain.strategy.model.entity.QStrategy.strategy;
import static com.investmetic.domain.strategy.model.entity.QStrategyStatistics.strategyStatistics;
import static com.investmetic.domain.strategy.model.entity.QTradeType.tradeType;
import static com.investmetic.domain.subscription.model.entity.QSubscription.subscription;
import static com.investmetic.domain.user.model.entity.QUser.user;

import com.investmetic.domain.strategy.dto.RangeDto;
import com.investmetic.domain.strategy.dto.StockTypeInfo;
import com.investmetic.domain.strategy.dto.request.SearchRequest;
import com.investmetic.domain.strategy.dto.response.AdminStrategyResponseDto;
import com.investmetic.domain.strategy.dto.response.MyStrategyDetailResponse;
import com.investmetic.domain.strategy.dto.response.QAdminStrategyResponseDto;
import com.investmetic.domain.strategy.dto.response.QMyStrategyDetailResponse;
import com.investmetic.domain.strategy.dto.response.QStrategyDetailResponse;
import com.investmetic.domain.strategy.dto.response.QTopRankingStrategyResponseDto;
import com.investmetic.domain.strategy.dto.response.StrategyDetailResponse;
import com.investmetic.domain.strategy.dto.response.TopRankingStrategyResponseDto;
import com.investmetic.domain.strategy.dto.response.common.MyStrategySimpleResponse;
import com.investmetic.domain.strategy.dto.response.common.QMyStrategySimpleResponse;
import com.investmetic.domain.strategy.dto.response.common.QStrategySimpleResponse;
import com.investmetic.domain.strategy.dto.response.common.StrategySimpleResponse;
import com.investmetic.domain.strategy.model.AlgorithmType;
import com.investmetic.domain.strategy.model.DurationRange;
import com.investmetic.domain.strategy.model.IsApproved;
import com.investmetic.domain.strategy.model.IsPublic;
import com.investmetic.domain.strategy.model.OperationCycle;
import com.investmetic.domain.strategy.model.ProfitRange;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class StrategyRepositoryCustomImpl implements StrategyRepositoryCustom {
    private final JPAQueryFactory queryFactory;


    @Override
    public StrategyDetailResponse findStrategyDetail(Long strategyId) {

        // 종목 정보 가져오기
        List<Tuple> stockTypes = queryFactory
                .select(stockType.stockTypeIconUrl, stockType.stockTypeName)
                .from(stockTypeGroup)
                .join(stockTypeGroup.stockType, stockType)
                .where(stockTypeGroup.strategy.strategyId.eq(strategyId))
                .fetch();

        // StockTypeInfo로 변환
        StockTypeInfo stockTypeInfo = getStockTypeInfo(stockTypes);

        return queryFactory
                .select(new QStrategyDetailResponse(
                        strategy.strategyName,
                        Expressions.constant(stockTypeInfo),
                        tradeType.tradeTypeIconUrl,
                        tradeType.tradeTypeName,
                        strategy.operationCycle,
                        strategy.strategyDescription,
                        strategyStatistics.cumulativeProfitRate,
                        strategyStatistics.maxDrawdownRate,
                        strategyStatistics.averageProfitLossRate,
                        strategyStatistics.profitFactor,
                        strategyStatistics.winRate,
                        strategy.subscriptionCount,
                        user.imageUrl,
                        user.nickname,
                        strategy.minimumInvestmentAmount,
                        strategyStatistics.initialInvestment, // 초기 투자금액은 원금이랑 동일
                        strategy.kpRatio,
                        strategy.smScore,
                        strategyStatistics.endDate,
                        strategy.createdAt))
                .from(strategy)
                .leftJoin(strategy.strategyStatistics, strategyStatistics)
                .join(strategy.tradeType, tradeType)
                .join(strategy.user, user)
                .where(strategy.strategyId.eq(strategyId))
                .fetchOne();
    }

    @Override
    public MyStrategyDetailResponse findMyStrategyDetail(Long strategyId) {

        // 종목 정보 가져오기
        List<Tuple> stockTypes = queryFactory
                .select(stockType.stockTypeIconUrl, stockType.stockTypeName)
                .from(stockTypeGroup)
                .join(stockTypeGroup.stockType, stockType)
                .where(stockTypeGroup.strategy.strategyId.eq(strategyId))
                .fetch();

        // StockTypeInfo로 변환
        StockTypeInfo stockTypeInfo = getStockTypeInfo(stockTypes);

        return queryFactory
                .select(new QMyStrategyDetailResponse(
                        strategy.strategyName,
                        tradeType.tradeTypeIconUrl,
                        Expressions.constant(stockTypeInfo),
                        tradeType.tradeTypeName,
                        strategy.operationCycle,
                        strategy.strategyDescription,
                        strategy.subscriptionCount,
                        user.imageUrl,
                        user.nickname,
                        strategy.minimumInvestmentAmount,
                        strategyStatistics.initialInvestment,
                        strategy.kpRatio,
                        strategy.smScore,
                        strategyStatistics.endDate,
                        strategy.createdAt,
                        strategy.isPublic,
                        strategy.isApproved))
                .from(strategy)
                .leftJoin(strategy.strategyStatistics, strategyStatistics)
                .join(strategy.tradeType, tradeType)
                .join(strategy.user, user)
                .where(strategy.strategyId.eq(strategyId))
                .fetchOne();
    }

    private @NotNull StockTypeInfo getStockTypeInfo(List<Tuple> stockTypes) {
        List<String> stockTypeIconUrls = stockTypes.stream()
                .map(tuple -> tuple.get(stockType.stockTypeIconUrl))
                .toList();

        List<String> stockTypeNames = stockTypes.stream()
                .map(tuple -> tuple.get(stockType.stockTypeName))
                .toList();

        return new StockTypeInfo(stockTypeIconUrls, stockTypeNames);
    }

    /***
     * - 항목 및 알고리즘 검색 조회 동적쿼리(운용방식, 운용주기, 운용종목, 기간, 수익률, 원금, MDD, SM Score, 알고리즘별 로 검색) <br>
     * - 검색어는 전략명으로 검색  <br>
     * - 공개중인 전략과 승인완료된 전략만 조회가능 <br>
     * - 기본은 수익률로 정렬, 알고리즘 선택시 알고리즘별로 정렬 <br>
     * - 페이징 <br>
     */
    @Override
    public Page<StrategySimpleResponse> searchBy(SearchRequest searchRequest, Pageable pageable) {
        List<StrategySimpleResponse> content = queryFactory
                .select(new QStrategySimpleResponse(
                        strategy.strategyId,
                        strategy.strategyName,
                        user.imageUrl,
                        user.nickname,
                        tradeType.tradeTypeIconUrl,
                        tradeType.tradeTypeName,
                        strategyStatistics.maxDrawdown,
                        strategy.smScore,
                        strategyStatistics.cumulativeProfitRate,
                        strategyStatistics.recentYearProfitRate,
                        strategy.subscriptionCount,
                        strategy.averageRating,
                        strategy.reviewCount
                ))
                .from(strategy)
                .leftJoin(strategy.strategyStatistics, strategyStatistics)
                .join(strategy.tradeType, tradeType)
                .join(strategy.user, user)
                .where(isApprovedAndPublic(), applyAllFilters(searchRequest))
                .orderBy(getOrderByAlgorithm(searchRequest.getAlgorithmType())) // 알고리즘 타입으로 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이징 count 쿼리 최적화
        JPAQuery<Long> countQuery = queryFactory
                .select(Wildcard.count)
                .from(strategy)
                .where(isApprovedAndPublic(), applyAllFilters(searchRequest));

        // 만약 페이지의 처음이나, 끝일때, 전체 데이터 크기가 pageSize보다 작은 경우 COUNT 쿼리가 실행되지 않음
        // 그 외 경우에만 fetchOne() 을 실행하여 전체 데이터 개수를 계산
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 나의 전략목록 조회(트레이더), 최신순 정렬
     *
     * @param userId 로그인한 트레이더 id
     */
    @Override
    public Page<MyStrategySimpleResponse> findMyStrategies(Long userId, Pageable pageable) {

        List<MyStrategySimpleResponse> content = queryFactory
                .select(new QMyStrategySimpleResponse(
                        strategy.strategyId,
                        strategy.strategyName,
                        user.imageUrl,
                        user.nickname,
                        tradeType.tradeTypeIconUrl,
                        tradeType.tradeTypeName,
                        strategyStatistics.maxDrawdown,
                        strategy.smScore,
                        strategyStatistics.cumulativeProfitRate,
                        strategyStatistics.recentYearProfitRate,
                        strategy.subscriptionCount,
                        strategy.averageRating,
                        strategy.reviewCount,
                        strategy.isPublic
                ))
                .from(strategy)
                .leftJoin(strategy.strategyStatistics, strategyStatistics)
                .join(strategy.tradeType, tradeType)
                .join(strategy.user, user)
                .where(user.userId.eq(userId))
                .orderBy(strategy.createdAt.desc()) // 최신순으로 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이징 count 쿼리 최적화
        JPAQuery<Long> countQuery = queryFactory
                .select(Wildcard.count)
                .from(strategy)
                .where(user.userId.eq(userId));

        // 만약 페이지의 처음이나, 끝일때, 전체 데이터 크기가 pageSize보다 작은 경우 COUNT 쿼리가 실행되지 않음
        // 그 외 경우에만 fetchOne() 을 실행하여 전체 데이터 개수를 계산
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

    }

    @Override
    public Page<StrategySimpleResponse> findSubscribedStrategies(Long userId, Pageable pageable) {

        List<StrategySimpleResponse> content = queryFactory
                .select(new QStrategySimpleResponse(
                        strategy.strategyId,
                        strategy.strategyName,
                        user.imageUrl,
                        user.nickname,
                        tradeType.tradeTypeIconUrl,
                        tradeType.tradeTypeName,
                        strategyStatistics.maxDrawdown,
                        strategy.smScore,
                        strategyStatistics.cumulativeProfitRate,
                        strategyStatistics.recentYearProfitRate,
                        strategy.subscriptionCount,
                        strategy.averageRating,
                        strategy.reviewCount
                ))
                .from(strategy)
                .leftJoin(strategy.strategyStatistics, strategyStatistics)
                .join(strategy.tradeType, tradeType)
                .join(strategy.user, user)
                .join(subscription).on(subscription.strategy.eq(strategy))  // 구독 테이블 조인
                .where(isApprovedAndPublic(), subscription.user.userId.eq(userId))
                .orderBy(subscription.createdAt.desc()) // 최근 구독순으로 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이징 count 쿼리 최적화
        JPAQuery<Long> countQuery = queryFactory
                .select(Wildcard.count)
                .from(strategy)
                .where(isApprovedAndPublic());

        // 만약 페이지의 처음이나, 끝일때, 전체 데이터 크기가 pageSize보다 작은 경우 COUNT 쿼리가 실행되지 않음
        // 그 외 경우에만 fetchOne() 을 실행하여 전체 데이터 개수를 계산
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }


    // ENUM에서 알고리즘 공식 처리 (전략 패턴)
    private OrderSpecifier<?> getOrderByAlgorithm(AlgorithmType algorithmType) {
        return algorithmType != null ? algorithmType.getOrderSpecifier(strategyStatistics)
                : strategyStatistics.cumulativeProfitRate.desc();
    }

    // 종목 아이콘목록 조회 배치 쿼리
    @Override
    public Map<Long, StockTypeInfo> findStockTypeInfoMap(List<Long> strategyIds) {
        // 종목 아이콘 조회
        List<Tuple> stockTypeInfos = queryFactory
                .select(stockTypeGroup.strategy.strategyId, stockType.stockTypeIconUrl, stockType.stockTypeName)
                .from(stockTypeGroup)
                .join(stockTypeGroup.stockType, stockType)
                .where(stockTypeGroup.strategy.strategyId.in(strategyIds))
                .fetch();

        // Map 생성
        Map<Long, StockTypeInfo> stockTypeInfoMap = new HashMap<>();

        for (Tuple stockTypeInfo : stockTypeInfos) {
            Long strategyId = stockTypeInfo.get(stockTypeGroup.strategy.strategyId);
            String iconUrl = stockTypeInfo.get(stockType.stockTypeIconUrl);
            String name = stockTypeInfo.get(stockType.stockTypeName);

            // StockTypeInfo 생성 및 누적
            stockTypeInfoMap.computeIfAbsent(strategyId, key -> new StockTypeInfo(new ArrayList<>(), new ArrayList<>()))
                    .getStockTypeIconUrls().add(iconUrl); // 아이콘 추가
            stockTypeInfoMap.get(strategyId).getStockTypeNames().add(name); // 이름 추가
        }

        return stockTypeInfoMap;
    }

    // 구독여부 조회 배치 쿼리
    @Override
    public Map<Long, Boolean> findBySubscriptionMap(Long userId, List<Long> strategyIds) {
        // 구독 여부 조회
        return queryFactory
                .select(subscription.strategy.strategyId, subscription.user.userId)
                .from(subscription)
                .where(subscription.user.userId.eq(userId), subscription.strategy.strategyId.in(strategyIds))
                .fetch()
                .stream().collect(Collectors.toMap(
                        tuple -> tuple.get(subscription.strategy.strategyId),
                        tuple -> true
                ));
    }

    // 메인 페이지 구독 순 조회 쿼리
    @Override
    public List<TopRankingStrategyResponseDto> findTopRankingStrategy(OrderSpecifier<?> orderBy, int limit) {
        return queryFactory
                .select(new QTopRankingStrategyResponseDto(
                        strategy.strategyId,
                        strategy.strategyName,
                        user.imageUrl,
                        user.nickname,
                        strategy.smScore,
                        strategyStatistics.cumulativeProfitRate,
                        strategy.subscriptionCount,
                        strategy.averageRating,
                        strategy.reviewCount
                ))
                .from(strategy)
                .join(strategy.strategyStatistics, strategyStatistics)
                .join(strategy.user, user)
                .where(isApprovedAndPublic())
                .orderBy(orderBy)
                .offset(0)
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Double> findProfitRateData(Long strategyId) {
        return queryFactory
                .select(dailyAnalysis.cumulativeProfitLossRate)
                .from(dailyAnalysis)
                .where(strategy.strategyId.eq(strategyId))
                .fetch();
    }

    @Override
    public Page<AdminStrategyResponseDto> findAdminStrategies(Pageable pageable, String searchWord,
                                                              IsApproved isApproved) {
        List<AdminStrategyResponseDto> strategies = queryFactory
                .select(new QAdminStrategyResponseDto(
                        strategy.createdAt,
                        strategy.strategyId,
                        strategy.strategyName,
                        strategy.user.nickname,
                        strategy.isPublic,
                        strategy.isApproved
                ))
                .from(strategy)
                .where(applySearchWordFilter(searchWord), applyIsApprovedFilter(isApproved))
                .orderBy(strategy.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return PageableExecutionUtils.getPage(strategies, pageable, strategies::size);
    }

    @Override
    public Page<StrategySimpleResponse> getTraderStrategies(Long traderId, Pageable pageable) {
        List<StrategySimpleResponse> content = queryFactory
                .select(new QStrategySimpleResponse(
                        strategy.strategyId,
                        strategy.strategyName,
                        user.imageUrl,
                        user.nickname,
                        tradeType.tradeTypeIconUrl,
                        tradeType.tradeTypeName,
                        strategyStatistics.maxDrawdown,
                        strategy.smScore,
                        strategyStatistics.cumulativeProfitRate,
                        strategyStatistics.recentYearProfitRate,
                        strategy.subscriptionCount,
                        strategy.averageRating,
                        strategy.reviewCount
                ))
                .from(strategy)
                .leftJoin(strategy.strategyStatistics, strategyStatistics)
                .join(strategy.tradeType, tradeType)
                .join(strategy.user, user)
                .where(strategy.user.userId.eq(traderId)
                        , isApprovedAndPublic())
                .orderBy(strategy.strategyId.desc()) // 최신순(id 내림차순) 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이징 count 쿼리 최적화
        JPAQuery<Long> countQuery = queryFactory
                .select(Wildcard.count)
                .from(strategy)
                .where(strategy.user.userId.eq(traderId)
                        , isApprovedAndPublic());

        // 만약 페이지의 처음이나, 끝일때, 전체 데이터 크기가 pageSize보다 작은 경우 COUNT 쿼리가 실행되지 않음
        // 그 외 경우에만 fetchOne() 을 실행하여 전체 데이터 개수를 계산
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 모든 필터 적용
    private BooleanBuilder applyAllFilters(SearchRequest searchRequest) {
        BooleanBuilder builder = new BooleanBuilder();

        // 조건 추가
        builder.and(applySearchWordFilter(searchRequest.getSearchWord()));
        builder.and(applyTradeTypeFilter(searchRequest.getTradeTypeNames()));
        builder.and(applyOperationCycleFilter(searchRequest.getOperationCycles()));
        builder.and(applyStockTypeFilter(searchRequest.getStockTypeNames()));
        builder.and(applyOperationPeriodFilter(searchRequest.getDurations()));
        builder.and(applyProfitRangeFilter(searchRequest.getProfitRanges()));
        builder.and(applyPrincipalRangeFilter(searchRequest.getPrincipalRange()));
        builder.and(applyMddRangeFilter(searchRequest.getMddRange()));
        builder.and(applySmScoreRangeFilter(searchRequest.getSmScoreRange()));

        return builder;
    }

    private BooleanExpression isApprovedAndPublic() {
        return strategy.isApproved.eq(IsApproved.APPROVED)
                .and(strategy.isPublic.eq(IsPublic.PUBLIC));
    }

    // 승인상태 필터
    private BooleanExpression applyIsApprovedFilter(IsApproved isApproved) {
        return isApproved == null ? null : strategy.isApproved.eq(isApproved);
    }

    // 전략명 검색어 필터
    private BooleanExpression applySearchWordFilter(String searchWord) {
        return searchWord == null || searchWord.isEmpty() ? null : strategy.strategyName.like("%" + searchWord + "%");
    }

    // 운용방식(매매유형) 필터
    private BooleanExpression applyTradeTypeFilter(List<String> tradeTypeNames) {
        return tradeTypeNames == null || tradeTypeNames.isEmpty() ? null : tradeType.tradeTypeName.in(tradeTypeNames);
    }

    // 운용주기 필터 (데이/포지션)
    private BooleanExpression applyOperationCycleFilter(List<OperationCycle> operationCycles) {
        return operationCycles == null || operationCycles.isEmpty() ? null
                : strategy.operationCycle.in(operationCycles);
    }

    // 운용종목 필터
    private BooleanExpression applyStockTypeFilter(List<String> stockTypeNames) {
        if (stockTypeNames == null || stockTypeNames.isEmpty()) {
            return null;
        }

        return strategy.strategyId.in(
                JPAExpressions
                        .select(stockTypeGroup.strategy.strategyId)
                        .from(stockTypeGroup)
                        .join(stockTypeGroup.stockType, stockType)
                        .where(stockType.stockTypeName.in(stockTypeNames))
        );
    }

    // 운용기간 필터
    private BooleanExpression applyOperationPeriodFilter(List<DurationRange> durationRanges) {
        if (durationRanges == null || durationRanges.isEmpty()) {
            return null;
        }

        BooleanExpression condition = null;
        for (DurationRange range : durationRanges) {
            BooleanExpression rangeCondition = strategyStatistics.operationPeriod.between(range.getMinDays(),
                    range.getMaxDays());
            condition = (condition == null) ? rangeCondition : condition.or(rangeCondition);
        }

        return condition;
    }

    // 수익률 필터
    private BooleanExpression applyProfitRangeFilter(List<ProfitRange> profitRanges) {
        if (profitRanges == null || profitRanges.isEmpty()) {
            return null;
        }

        BooleanExpression condition = null;
        for (ProfitRange range : profitRanges) {
            BooleanExpression rangeCondition = strategyStatistics.operationPeriod.between(range.getMinRate(),
                    range.getMaxRate());
            condition = (condition == null) ? rangeCondition : condition.or(rangeCondition);
        }

        return condition;

    }

    // 원금 필터
    private BooleanExpression applyPrincipalRangeFilter(RangeDto principalRange) {
        return principalRange == null ? null
                : strategyStatistics.principal.between(principalRange.getMin(), principalRange.getMax());
    }

    // MDD 필터
    private BooleanExpression applyMddRangeFilter(RangeDto mddRange) {
        return mddRange == null ? null
                : strategyStatistics.maxDrawdown.between(mddRange.getMin(), mddRange.getMax());
    }

    // SM Score 필터
    private BooleanExpression applySmScoreRangeFilter(RangeDto smScoreRange) {
        return smScoreRange == null ? null
                : strategy.smScore.between(smScoreRange.getMin(), smScoreRange.getMax());
    }

}

