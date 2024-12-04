package com.investmetic.domain.strategy.service;

import com.investmetic.domain.TestEntity.TestEntityFactory;
import com.investmetic.domain.qna.repository.AnswerRepository;
import com.investmetic.domain.qna.repository.QuestionRepository;
import com.investmetic.domain.review.repository.ReviewRepository;
import com.investmetic.domain.strategy.dto.response.AdminStrategyResponseDto;
import com.investmetic.domain.strategy.model.IsApproved;
import com.investmetic.domain.strategy.model.IsPublic;
import com.investmetic.domain.strategy.model.MinimumInvestmentAmount;
import com.investmetic.domain.strategy.model.OperationCycle;
import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.model.entity.TradeType;
import com.investmetic.domain.strategy.repository.DailyAnalysisRepository;
import com.investmetic.domain.strategy.repository.MonthlyAnalysisRepository;
import com.investmetic.domain.strategy.repository.StockTypeGroupRepository;
import com.investmetic.domain.strategy.repository.StockTypeRepository;
import com.investmetic.domain.strategy.repository.StrategyRepository;
import com.investmetic.domain.strategy.repository.TradeTypeRepository;
import com.investmetic.domain.subscription.repository.SubscriptionRepository;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.common.PageResponseDto;
import java.util.List;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
public class AdminStrategyServiceTest {
    @Autowired
    private AdminStrategyService adminStrategyService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StrategyRepository strategyRepository;
    @Autowired
    private TradeTypeRepository tradeTypeRepository;
    @Autowired
    private DailyAnalysisRepository dailyAnalysisRepository;
    @Autowired
    private StockTypeRepository stockTypeRepository;
    @Autowired
    private MonthlyAnalysisRepository monthlyAnalysisRepository;
    @Autowired
    private StockTypeGroupRepository stockTypeGroupRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    User user;
    Strategy strategy;

    @BeforeEach
    void init() {
        questionRepository.deleteAll();
        answerRepository.deleteAll();
        reviewRepository.deleteAll();
        stockTypeGroupRepository.deleteAll();
        dailyAnalysisRepository.deleteAll();
        subscriptionRepository.deleteAll();
        monthlyAnalysisRepository.deleteAll();
        strategyRepository.deleteAll();
        // 더미 데이터 입력
        user = TestEntityFactory.createTestUser();
        userRepository.save(user);
        TradeType tradeType = TestEntityFactory.createTestTradeType();
        tradeTypeRepository.save(tradeType);
        strategy = Strategy.builder()
                .user(user)
                .tradeType(tradeType)
                .strategyName("매매 전략")
                .operationCycle(OperationCycle.DAY)
                .minimumInvestmentAmount(MinimumInvestmentAmount.ABOVE_100M)
                .strategyDescription("전략상세")
                .proposalFilePath("http://~")
                .isPublic(IsPublic.PUBLIC)
                .isApproved(IsApproved.PENDING)
                .subscriptionCount(100).build();
        strategyRepository.save(strategy);
    }
    @Test
    @DisplayName("승인 상태 변경 테스트")
    void userDenyApproveTest() {



        //기본 승인 유무 확인
        assertEquals(strategy.getIsApproved(), IsApproved.PENDING);

        // 승인 거부로 변경
        adminStrategyService.manageAproveState(strategy.getStrategyId(), IsApproved.DENY);
        List<Strategy> strategies = strategyRepository.findAll();

        assertEquals(strategies.get(strategies.size()-1).getIsApproved(), IsApproved.DENY);

        //승인으로 변경
        adminStrategyService.manageAproveState(strategy.getStrategyId(), IsApproved.APPROVED);
        strategies = strategyRepository.findAll();
        assertEquals(strategies.get(strategies.size()-1).getIsApproved(), IsApproved.APPROVED);
    }

    @Test
    @DisplayName("관리자페이지 전략 목록 조회 테스트")
    void getAdminStratiesTest(){
        Pageable pageable = PageRequest.of(0, 10);
        PageResponseDto<AdminStrategyResponseDto> strategies =adminStrategyService.getManageStrategies(pageable,null,IsApproved.PENDING);

        assertEquals(strategies.getContent().get(0).getIsApproved(), IsApproved.PENDING);
        assertEquals(strategies.getContent().get(0).getStrategyName(), strategy.getStrategyName());
    }
}
