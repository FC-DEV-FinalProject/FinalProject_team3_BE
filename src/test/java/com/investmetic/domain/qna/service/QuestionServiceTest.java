package com.investmetic.domain.qna.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.investmetic.domain.TestEntity.TestEntityFactory;
import com.investmetic.domain.qna.dto.SearchCondition;
import com.investmetic.domain.qna.dto.StateCondition;
import com.investmetic.domain.qna.dto.request.QuestionRequestDto;
import com.investmetic.domain.qna.dto.response.QuestionsDetailResponse;
import com.investmetic.domain.qna.dto.response.QuestionsResponse;
import com.investmetic.domain.qna.model.QnaState;
import com.investmetic.domain.qna.model.entity.Answer;
import com.investmetic.domain.qna.model.entity.Question;
import com.investmetic.domain.qna.repository.AnswerRepository;
import com.investmetic.domain.qna.repository.QuestionRepository;
import com.investmetic.domain.strategy.model.entity.Strategy;
import com.investmetic.domain.strategy.repository.StrategyRepository;
import com.investmetic.domain.user.model.Role;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.common.PageResponseDto;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StrategyRepository strategyRepository;

    @Mock
    private AnswerRepository answerRepository; // 답변 레포지토리 추가

    @InjectMocks
    private QuestionService questionService;

    @Test
    @DisplayName("문의 등록 성공")
    void createQuestion_Success() {
        // Given
        Long userId = 1L;
        Long strategyId = 1L;

        QuestionRequestDto requestDto = QuestionRequestDto.builder()
                .keyword("키워드")
                .title("제목")
                .content("문의 내용") // 필수 필드 추가
                .build();

        // Mock User 생성
        User mockUser = TestEntityFactory.createTestUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Mock Strategy 생성
        User mockTrader = TestEntityFactory.createTestUser(); // 트레이더 유저
        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getUser()).thenReturn(mockTrader); // 트레이더 연결
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(mockStrategy));

        // Mock Question 생성
        when(questionRepository.save(any(Question.class))).thenReturn(mock(Question.class));

        // When
        assertDoesNotThrow(() -> questionService.createQuestion(userId, strategyId, requestDto));

        // Then
        verify(userRepository).findById(userId);
        verify(strategyRepository).findById(strategyId);
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    @DisplayName("문의 등록 실패 - 사용자 존재하지 않음")
    void createQuestion_Failure_UserNotFound() {
        // Given
        Long userId = 1L;
        Long strategyId = 1L;
        QuestionRequestDto requestDto = QuestionRequestDto.builder()
                .keyword("키워드")
                .title("제목")
                .build();

        // 사용자 존재하지 않는 경우 설정
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            questionService.createQuestion(userId, strategyId, requestDto);
        });

        // 검증
        assertEquals(ErrorCode.USERS_NOT_FOUND, exception.getErrorCode());
        verify(userRepository).findById(userId);
        verify(strategyRepository, never()).findById(any()); // 전략 조회가 호출되지 않음을 검증
        verify(questionRepository, never()).save(any());    // 문의 저장이 호출되지 않음을 검증
    }

    @Test
    @DisplayName("문의 삭제 성공 - 투자자 권한")
    void deleteQuestion_Success_Investor() {
        // Given
        Long strategyId = 1L;
        Long questionId = 1L;
        Long userId = 1L;

        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(userId);
        when(mockUser.getRole()).thenReturn(Role.INVESTOR);

        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getStrategyId()).thenReturn(strategyId);

        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getUser()).thenReturn(mockUser);
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(mockStrategy)); // 전략 조회 추가

        // When & Then
        assertDoesNotThrow(() -> questionService.deleteQuestion(strategyId, questionId, userId));

        // Verify interactions
        verify(strategyRepository).findById(strategyId); // 전략 조회 호출 검증
        verify(userRepository).findById(userId);
        verify(questionRepository).findById(questionId);
        verify(questionRepository).delete(mockQuestion);
    }

    @Test
    @DisplayName("문의 삭제 실패 - 질문이 존재하지 않음")
    void deleteQuestion_Failure_QuestionNotFound() {
        // Given
        Long strategyId = 1L;
        Long questionId = 1L;
        Long userId = 1L;

        // 질문이 존재하지 않는 경우 설정
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            questionService.deleteQuestion(strategyId, questionId, userId);
        });

        // 검증
        assertEquals(ErrorCode.QUESTION_NOT_FOUND, exception.getErrorCode());
        verify(questionRepository).findById(questionId);
        verify(strategyRepository, never()).findById(any()); // 전략 조회 호출되지 않음
        verify(userRepository, never()).findById(any());    // 사용자 조회 호출되지 않음
        verify(questionRepository, never()).delete(any()); // 삭제 호출되지 않음
    }

    @Test
    @DisplayName("문의 삭제 실패 - 전략이 존재하지 않음")
    void deleteQuestion_Failure_StrategyNotFound() {
        // Given
        Long strategyId = 1L;
        Long questionId = 1L;
        Long userId = 1L;

        // 질문은 존재하지만 전략이 존재하지 않는 경우 설정
        Question mockQuestion = mock(Question.class);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.empty()); // 전략이 없는 경우
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            questionService.deleteQuestion(strategyId, questionId, userId);
        });

        // 검증
        assertEquals(ErrorCode.STRATEGY_NOT_FOUND, exception.getErrorCode());
        verify(questionRepository).findById(questionId); // 질문 조회는 호출됨
        verify(strategyRepository).findById(strategyId); // 전략 조회 호출됨
        verify(userRepository, never()).findById(any()); // 사용자 조회 호출되지 않음

    }


    @Test
    @DisplayName("투자자 문의 목록 조회 성공")
    void getInvestorQuestions_Success() {
        // Given
        Long userId = 1L;
        Role userRole = Role.INVESTOR;

        String keyword = "test"; // 키워드
        SearchCondition searchCondition = SearchCondition.TITLE; // 검색 조건
        StateCondition stateCondition = StateCondition.WAITING; // 상태 조건

        Pageable pageable = PageRequest.of(0, 4);

        // Mock Investor 생성
        User mockInvestor = mock(User.class); // 투자자 Mock
        when(mockInvestor.getNickname()).thenReturn("Investor Nickname"); // 투자자 닉네임 설정
        when(mockInvestor.getImageUrl()).thenReturn("http://example.com/investor.jpg"); // 투자자 프로필 이미지 설정

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getQuestionId()).thenReturn(1L);
        when(mockQuestion.getTitle()).thenReturn("Test Question");
        when(mockQuestion.getQnaState()).thenReturn(QnaState.WAITING);
        when(mockQuestion.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockQuestion.getUser()).thenReturn(mockInvestor); // 투자자 연결

        // Mock Page
        Page<Question> mockPage = new PageImpl<>(List.of(mockQuestion), pageable, 1);
        when(questionRepository.searchByConditions(anyList(), any(Pageable.class), any())).thenReturn(mockPage);

        // When
        PageResponseDto<QuestionsResponse> response = questionService.getInvestorQuestions(
                userId, userRole, keyword, searchCondition, stateCondition, pageable
        );

        // Then
        assertEquals(1, response.getContent().size()); // 문의 목록 크기 확인
        assertEquals("Test Question", response.getContent().get(0).getTitle()); // 문의 제목 확인
        assertEquals("WAITING", response.getContent().get(0).getStateCondition()); // 상태 확인
        assertEquals("Investor Nickname", response.getContent().get(0).getNickname()); // 닉네임 확인
        assertEquals("http://example.com/investor.jpg",
                response.getContent().get(0).getProfileImageUrl()); // 이미지 URL 확인

        // Verify interactions
        verify(questionRepository).searchByConditions(anyList(), any(Pageable.class), any());
    }

    @Test
    @DisplayName("문의 삭제 실패 - 다른 사용자가 삭제 시도")
    void deleteQuestion_Failure_UnauthorizedUser() {
        // Given
        Long strategyId = 1L;
        Long questionId = 1L;
        Long unauthorizedUserId = 2L; // 삭제를 시도한 권한 없는 사용자 ID
        Long questionOwnerId = 1L;    // 문의 작성자 ID

        // Mock 사용자 (권한 없는 사용자)
        User mockUnauthorizedUser = mock(User.class);
        when(mockUnauthorizedUser.getUserId()).thenReturn(unauthorizedUserId);
        when(mockUnauthorizedUser.getRole()).thenReturn(Role.INVESTOR); // 투자자 역할

        // Mock 문의 작성자
        User mockQuestionOwner = mock(User.class);
        when(mockQuestionOwner.getUserId()).thenReturn(questionOwnerId); // 문의 작성자 ID

        // Mock 전략
        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getUser()).thenReturn(mockQuestionOwner); // 전략의 작성자는 문의 작성자

        // Mock 질문
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getUser()).thenReturn(mockQuestionOwner); // 문의 작성자는 mockQuestionOwner
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy);  // 문의에 연결된 전략

        // Repository 설정
        when(userRepository.findById(unauthorizedUserId)).thenReturn(Optional.of(mockUnauthorizedUser)); // 삭제 시도자
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion)); // 문의 조회
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(mockStrategy)); // 전략 조회

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            questionService.deleteQuestion(strategyId, questionId, unauthorizedUserId);
        });

        // 검증
        assertEquals(ErrorCode.FORBIDDEN_ACCESS, exception.getErrorCode()); // 권한 없음 오류 발생 확인
        verify(userRepository).findById(unauthorizedUserId); // 사용자 조회 호출 확인
        verify(questionRepository).findById(questionId); // 문의 조회 호출 확인
        verify(strategyRepository).findById(strategyId); // 전략 조회 호출 확인
        verify(questionRepository, never()).delete(any(Question.class)); // 삭제 호출되지 않음 검증
    }

    @Test
    @DisplayName("투자자 문의 목록 조회 성공 - 키워드 필터링")
    void getInvestorQuestions_Success_WithKeywordFiltering() {
        // Given
        Long userId = 1L;
        Role userRole = Role.INVESTOR;

        String keyword = "filtered"; // 검색 키워드
        SearchCondition searchCondition = SearchCondition.TITLE; // 검색 조건
        StateCondition stateCondition = StateCondition.COMPLETED; // 상태 조건

        Pageable pageable = PageRequest.of(0, 10);

        // Mock Investor 생성
        User mockInvestor = mock(User.class);
        when(mockInvestor.getNickname()).thenReturn("Filtered Investor");
        when(mockInvestor.getImageUrl()).thenReturn("http://example.com/filtered.jpg");

        // Mock Strategy 생성
        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getUser()).thenReturn(mockInvestor);

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getQuestionId()).thenReturn(2L);
        when(mockQuestion.getTitle()).thenReturn("Filtered Question");
        when(mockQuestion.getQnaState()).thenReturn(QnaState.COMPLETED);
        when(mockQuestion.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy);
        when(mockQuestion.getUser()).thenReturn(mockInvestor);

        // Mock Page
        Page<Question> mockPage = new PageImpl<>(List.of(mockQuestion), pageable, 1);
        when(questionRepository.searchByConditions(anyList(), eq(pageable), any())).thenReturn(mockPage);

        // When
        PageResponseDto<QuestionsResponse> response = questionService.getInvestorQuestions(
                userId, userRole, keyword, searchCondition, stateCondition, pageable
        );

        // Then
        assertEquals(1, response.getContent().size()); // 문의 목록 크기 확인
        assertEquals("Filtered Question", response.getContent().get(0).getTitle()); // 문의 제목 확인
        assertEquals("COMPLETED", response.getContent().get(0).getStateCondition()); // 상태 확인
        assertEquals("Filtered Investor", response.getContent().get(0).getNickname()); // 닉네임 확인
        assertEquals("http://example.com/filtered.jpg",
                response.getContent().get(0).getProfileImageUrl()); // 이미지 URL 확인

        // Verify interactions
        verify(questionRepository).searchByConditions(anyList(), eq(pageable), any());
    }

    @Test
    @DisplayName("문의 목록 상태 필터링 성공 - WAITING 상태")
    void getQuestionsByStateCondition_Waiting() {
        // Given
        Long userId = 1L;
        Role userRole = Role.INVESTOR;

        String keyword = "filtered"; // 검색 키워드
        SearchCondition searchCondition = SearchCondition.TITLE; // 검색 조건
        StateCondition stateCondition = StateCondition.COMPLETED; // 상태 조건

        Pageable pageable = PageRequest.of(0, 10);

        // Mock User 생성
        User mockUser = mock(User.class);
        when(mockUser.getNickname()).thenReturn("Investor Nickname");
        when(mockUser.getImageUrl()).thenReturn("http://example.com/investor.jpg");

        // Mock Strategy 생성
        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getUser()).thenReturn(mockUser);

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getQuestionId()).thenReturn(1L);
        when(mockQuestion.getTitle()).thenReturn("Test Question");
        when(mockQuestion.getQnaState()).thenReturn(QnaState.WAITING);
        when(mockQuestion.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy);
        when(mockQuestion.getUser()).thenReturn(mockUser); // 투자자 연결

        // Mock Page
        Page<Question> mockPage = new PageImpl<>(List.of(mockQuestion), pageable, 1);
        when(questionRepository.searchByConditions(anyList(), any(Pageable.class), any())).thenReturn(mockPage);

        // When
        PageResponseDto<QuestionsResponse> response = questionService.getInvestorQuestions(
                userId, userRole, keyword, searchCondition, stateCondition, pageable
        );

        // Then
        assertEquals(1, response.getContent().size()); // 문의 목록 크기 확인
        assertEquals("Test Question", response.getContent().get(0).getTitle()); // 문의 제목 확인
        assertEquals("WAITING", response.getContent().get(0).getStateCondition()); // 상태 확인
        assertEquals("Investor Nickname", response.getContent().get(0).getNickname()); // 닉네임 확인
        assertEquals("http://example.com/investor.jpg",
                response.getContent().get(0).getProfileImageUrl()); // 이미지 URL 확인

        // Verify interactions
        verify(questionRepository).searchByConditions(anyList(), any(Pageable.class), any());
    }

    @Test
    @DisplayName("문의 상세 조회 성공 - 투자자")
    void getQuestionDetail_Success_Investor() {
        // Given
        Long questionId = 1L;
        Long userId = 1L;
        Role userRole = Role.INVESTOR;

        // Mock User (Investor)
        User mockInvestor = mock(User.class);
        when(mockInvestor.getUserId()).thenReturn(userId);
        when(mockInvestor.getRole()).thenReturn(Role.INVESTOR);
        when(mockInvestor.getNickname()).thenReturn("Investor Nickname");
        when(mockInvestor.getImageUrl()).thenReturn("http://example.com/investor.jpg");

        // Mock Trader
        User mockTrader = mock(User.class);
        when(mockTrader.getNickname()).thenReturn("Trader Nickname");
        when(mockTrader.getImageUrl()).thenReturn("http://example.com/trader.jpg");

        // Mock Strategy
        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getUser()).thenReturn(mockTrader);
        when(mockStrategy.getStrategyName()).thenReturn("Sample Strategy");

        // Mock Question
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getQuestionId()).thenReturn(questionId);
        when(mockQuestion.getUser()).thenReturn(mockInvestor);
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy);
        when(mockQuestion.getTitle()).thenReturn("Test Question Title");
        when(mockQuestion.getContent()).thenReturn("Test Question Content");
        when(mockQuestion.getQnaState()).thenReturn(QnaState.WAITING);
        when(mockQuestion.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockQuestion.getAnswer()).thenReturn(null); // No answer provided

        // Mock Repository Behavior
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockInvestor));

        // When
        QuestionsDetailResponse response = questionService.getQuestionDetail(questionId, userId, userRole);

        // Then
        assertEquals(questionId, response.getQuestionId());
        assertEquals("Test Question Title", response.getTitle());
        assertEquals("Test Question Content", response.getQuestionContent());
        assertEquals("Sample Strategy", response.getStrategyName());
        assertEquals("Investor Nickname", response.getNickname()); // 투자자 닉네임
        assertEquals("http://example.com/investor.jpg", response.getProfileImageUrl()); // 투자자 프로필 이미지
        assertEquals("WAITING", response.getState());
        assertEquals("답변 없음", response.getAnswerContent()); // 답변 없음이 반환되는지 확인
        verify(questionRepository).findById(questionId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("문의 상세 조회 성공 - 트레이더")
    void getQuestionDetail_Success_Trader() {
        // Given
        Long questionId = 1L;
        Long userId = 2L; // 트레이더 ID
        Role userRole = Role.TRADER;

        // Mock Trader
        User mockTrader = mock(User.class);
        when(mockTrader.getUserId()).thenReturn(userId);
        when(mockTrader.getRole()).thenReturn(Role.TRADER);
        when(mockTrader.getNickname()).thenReturn("Trader Nickname");
        when(mockTrader.getImageUrl()).thenReturn("http://example.com/trader.jpg");

        // Mock Investor
        User mockInvestor = mock(User.class);
        when(mockInvestor.getNickname()).thenReturn("Investor Nickname");
        when(mockInvestor.getImageUrl()).thenReturn("http://example.com/investor.jpg");

        // Mock Strategy
        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getUser()).thenReturn(mockTrader);
        when(mockStrategy.getStrategyName()).thenReturn("Sample Strategy");

        // Mock Question
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getQuestionId()).thenReturn(questionId);
        when(mockQuestion.getUser()).thenReturn(mockTrader);
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy);
        when(mockQuestion.getTitle()).thenReturn("Test Question Title");
        when(mockQuestion.getContent()).thenReturn("Test Question Content");
        when(mockQuestion.getQnaState()).thenReturn(QnaState.WAITING);
        when(mockQuestion.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockQuestion.getAnswer()).thenReturn(null); // No answer provided

        // Mock Repository Behavior
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockTrader));

        // When
        QuestionsDetailResponse response = questionService.getQuestionDetail(questionId, userId, userRole);

        // Then
        assertEquals(questionId, response.getQuestionId());
        assertEquals("Test Question Title", response.getTitle());
        assertEquals("Test Question Content", response.getQuestionContent());
        assertEquals("Sample Strategy", response.getStrategyName());
        assertEquals("Trader Nickname", response.getNickname()); // 투자자 닉네임
        assertEquals("http://example.com/trader.jpg", response.getProfileImageUrl()); // 투자자 프로필 이미지
        assertEquals("WAITING", response.getState());
        assertEquals("답변 없음", response.getAnswerContent()); // 답변 없음이 반환되는지 확인
        verify(questionRepository).findById(questionId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("문의 상세 조회 성공 - 관리자")
    void getQuestionDetail_Success_Admin() {
        // Given
        Long questionId = 1L;
        Long adminId = 3L; // 관리자 ID
        Role adminRole = Role.SUPER_ADMIN; // 관리자 역할

        // Mock Admin
        User mockAdmin = mock(User.class);
        when(mockAdmin.getUserId()).thenReturn(adminId);
        when(mockAdmin.getRole()).thenReturn(Role.SUPER_ADMIN);

        // Mock Investor
        User mockInvestor = mock(User.class);
        when(mockInvestor.getNickname()).thenReturn("Investor Nickname");
        when(mockInvestor.getImageUrl()).thenReturn("http://example.com/investor.jpg");

        // Mock Strategy
        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getUser()).thenReturn(mockInvestor);
        when(mockStrategy.getStrategyName()).thenReturn("Sample Strategy");

        // Mock Question
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getQuestionId()).thenReturn(questionId);
        when(mockQuestion.getUser()).thenReturn(mockInvestor); // 문의 작성자는 투자자
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy);
        when(mockQuestion.getTitle()).thenReturn("Test Question Title");
        when(mockQuestion.getContent()).thenReturn("Test Question Content");
        when(mockQuestion.getQnaState()).thenReturn(QnaState.WAITING);
        when(mockQuestion.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(mockQuestion.getAnswer()).thenReturn(null); // 답변 없음

        // Mock Repository Behavior
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(mockAdmin));

        // When
        QuestionsDetailResponse response = questionService.getAdminQuestionDetail(questionId, adminRole);

        // Then
        assertEquals(questionId, response.getQuestionId());
        assertEquals("Test Question Title", response.getTitle());
        assertEquals("Test Question Content", response.getQuestionContent());
        assertEquals("Sample Strategy", response.getStrategyName());
        assertEquals("Investor Nickname", response.getNickname()); // 문의 작성자 닉네임 반환
        assertEquals("http://example.com/investor.jpg", response.getProfileImageUrl()); // 문의 작성자 프로필 이미지
        assertEquals("WAITING", response.getState());
        assertEquals("답변 없음", response.getAnswerContent()); // 답변 없음이 반환되는지 확인
        verify(questionRepository).findById(questionId);
        verify(userRepository, never()).findById(adminId); // 관리자 정보 조회 호출하지 않음
    }

    @Test
    @DisplayName("문의 목록 조회 성공 - 상태 조건이 null일 경우 모든 상태 조회")
    void getQuestionsByStateCondition_Null() {
        // Given
        Long userId = 1L;
        Role userRole = Role.INVESTOR;

        String keyword = "filtered"; // 검색 키워드
        SearchCondition searchCondition = SearchCondition.TITLE; // 검색 조건
        StateCondition stateCondition = StateCondition.COMPLETED; // 상태 조건

        Pageable pageable = PageRequest.of(0, 10);

        // Mock 데이터 생성
        Question mockQuestion1 = mock(Question.class);
        when(mockQuestion1.getQuestionId()).thenReturn(1L);
        when(mockQuestion1.getTitle()).thenReturn("Test Question 1");
        when(mockQuestion1.getQnaState()).thenReturn(QnaState.WAITING);
        when(mockQuestion1.getCreatedAt()).thenReturn(LocalDateTime.now());

        Question mockQuestion2 = mock(Question.class);
        when(mockQuestion2.getQuestionId()).thenReturn(2L);
        when(mockQuestion2.getTitle()).thenReturn("Test Question 2");
        when(mockQuestion2.getQnaState()).thenReturn(QnaState.COMPLETED);
        when(mockQuestion2.getCreatedAt()).thenReturn(LocalDateTime.now());

        // Mock Page 생성
        Page<Question> mockPage = new PageImpl<>(List.of(mockQuestion1, mockQuestion2), pageable, 2);
        when(questionRepository.searchByConditions(anyList(), eq(pageable), any())).thenReturn(mockPage);

        // When
        PageResponseDto<QuestionsResponse> response = questionService.getInvestorQuestions(
                userId, userRole, keyword, searchCondition, stateCondition, pageable
        );

        // Then
        assertEquals(2, response.getContent().size()); // 두 개의 문의가 반환되었는지 확인
        assertEquals("Test Question 1", response.getContent().get(0).getTitle());
        assertEquals("WAITING", response.getContent().get(0).getStateCondition());
        assertEquals("Test Question 2", response.getContent().get(1).getTitle());
        assertEquals("COMPLETED", response.getContent().get(1).getStateCondition());

        verify(questionRepository).searchByConditions(anyList(), eq(pageable), any());
    }

    @Test
    @DisplayName("문의 삭제 성공 - 답변도 함께 삭제")
    void deleteQuestion_Success_WithAnswer() {
        // Given
        Long strategyId = 1L;
        Long questionId = 1L;
        Long userId = 1L;

        // Mock 사용자 (문의 작성자)
        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(userId);
        when(mockUser.getRole()).thenReturn(Role.INVESTOR);

        // Mock 전략
        Strategy mockStrategy = mock(Strategy.class);
        when(mockStrategy.getUser()).thenReturn(mockUser); // 전략의 작성자 설정

        // Mock 답변
        Answer mockAnswer = mock(Answer.class);
        when(mockAnswer.getAnswerId()).thenReturn(1L); // 답변 ID 설정

        // Mock 질문
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getUser()).thenReturn(mockUser); // 문의 작성자는 mockUser
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy); // 문의에 연결된 전략
        when(mockQuestion.getAnswer()).thenReturn(mockAnswer); // 답변이 존재

        // Repository 설정
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(mockStrategy));

        // When
        assertDoesNotThrow(() -> questionService.deleteQuestion(strategyId, questionId, userId));

        // Then
        verify(userRepository).findById(userId); // 사용자 조회 호출 확인
        verify(questionRepository).findById(questionId); // 문의 조회 호출 확인
        verify(strategyRepository).findById(strategyId); // 전략 조회 호출 확인
        verify(answerRepository).delete(mockAnswer); // 답변 삭제 호출 확인
        verify(questionRepository).delete(mockQuestion); // 문의 삭제 호출 확인
    }


}