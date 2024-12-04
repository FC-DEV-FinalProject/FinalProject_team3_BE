package com.investmetic.domain.strategy.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.investmetic.domain.qna.model.entity.Answer;
import com.investmetic.domain.qna.repository.AnswerRepository;
import com.investmetic.domain.qna.repository.QuestionRepository;
import com.investmetic.domain.review.dto.response.ReviewDetailResponse;
import com.investmetic.domain.review.repository.ReviewRepository;
import com.investmetic.domain.strategy.dto.request.TradeTypeRequestDTO;
import com.investmetic.domain.strategy.dto.response.TradeTypeResponseDTO;
import com.investmetic.domain.strategy.model.entity.TradeType;
import com.investmetic.domain.strategy.repository.DailyAnalysisRepository;
import com.investmetic.domain.strategy.repository.MonthlyAnalysisRepository;
import com.investmetic.domain.strategy.repository.StockTypeGroupRepository;
import com.investmetic.domain.strategy.repository.StockTypeRepository;
import com.investmetic.domain.strategy.repository.StrategyRepository;
import com.investmetic.domain.strategy.repository.TradeTypeRepository;
import com.investmetic.domain.subscription.repository.SubscriptionRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest
@Transactional
class TradeTypeServiceTest {
    @Autowired
    private TradeTypeService tradeTypeService;
    @Autowired
    private TradeTypeRepository tradeTypeRepository;

    private ArrayList<TradeTypeRequestDTO> tradeTypeRequestDtoList;
    @Autowired
    private StrategyRepository strategyRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private DailyAnalysisRepository dailyAnalysisRepository;
    @Autowired
    private MonthlyAnalysisRepository monthlyAnalysisRepository;
    @Autowired
    private StockTypeGroupRepository stockTypeGroupRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private StockTypeRepository stockTypeRepository;
    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private QuestionRepository questionRepository;

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
        answerRepository.deleteAll();
        reviewRepository.deleteAll();
        stockTypeGroupRepository.deleteAll();
        dailyAnalysisRepository.deleteAll();
        subscriptionRepository.deleteAll();
        monthlyAnalysisRepository.deleteAll();
        strategyRepository.deleteAll();
        tradeTypeRequestDtoList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            TradeTypeRequestDTO tradetype = TradeTypeRequestDTO.builder()
                    .tradeTypeName("Sample_Trade" + i)
                    .tradeTypeIconUrl(String.format("/icons/sample-icon%d.png", i))
                    .size(1200)
                    .build();

            tradeTypeRequestDtoList.add(tradetype);
        }
        tradeTypeService.saveTradeType(tradeTypeRequestDtoList.get(0));
    }

    @Test
    @DisplayName("매매유형 등록 테스트")
    void registerTradeType() {
        TradeTypeRequestDTO tradeType = tradeTypeRequestDtoList.get(0);
        String savedTradeType = tradeTypeService.saveTradeType(tradeType);
        assertThat(savedTradeType).isNotNull();
    }

    @Test
    @DisplayName("매매유형 상태 변경 테스트")
    void changeStockTypes() {
        // 페이지 조회(true) : 활성 상태 페이지 불러오기
        List<TradeTypeResponseDTO> dtolist = tradeTypeService.getTradeTypes(true);

        // 페이지 첫번째 dto 가져오기
        TradeTypeResponseDTO dto = dtolist.get(0);
        tradeTypeService.changeActivateState(dto.getTradeTypeId());
        TradeType tradeType = tradeTypeRepository.findByTradeTypeId(dto.getTradeTypeId()).orElse(null);
        assertThat(tradeType.getActivateState()).isEqualTo(false);
    }
}
