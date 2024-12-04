package com.investmetic.domain.qna.service;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.investmetic.domain.qna.dto.request.AnswerRequestDto;
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
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private StrategyRepository strategyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnswerService answerService;

    @InjectMocks
    private QuestionService questionService;

    @Test
    @DisplayName("문의 답변 생성 성공")
    void createAnswer_Success() {
        // Given
        Long questionId = 1L;
        Long traderId = 1L;

        AnswerRequestDto requestDto = AnswerRequestDto.builder()
                .content("This is a test answer.")
                .build();

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        Strategy mockStrategy = mock(Strategy.class); // Strategy Mock 생성
        User mockTrader = mock(User.class); // Trader Mock 생성

        // Mock 설정
        when(mockTrader.getUserId()).thenReturn(traderId); // Trader ID 설정
        when(mockStrategy.getUser()).thenReturn(mockTrader); // Strategy에 Trader 설정
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy); // Question에 Strategy 설정
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));

        // When & Then
        assertDoesNotThrow(() -> answerService.createAnswer(questionId, traderId, requestDto));

        // 검증
        verify(questionRepository).findById(questionId);
        verify(mockQuestion).updateQnaState(QnaState.COMPLETED); // 문의 상태 변경 확인
        verify(answerRepository).save(any(Answer.class)); // 답변 저장 확인
    }

    @Test
    @DisplayName("문의 답변 생성 실패 - 문의가 존재하지 않음")
    void createAnswer_Failure_QuestionNotFound() {
        // Given
        Long questionId = 1L;
        Long traderId = 1L;

        AnswerRequestDto requestDto = AnswerRequestDto.builder()
                .content("This is a test answer.")
                .build();

        // 문의가 존재하지 않을 때
        when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                answerService.createAnswer(questionId, traderId, requestDto));

        assertEquals(ErrorCode.QUESTION_NOT_FOUND, exception.getErrorCode());
        verify(questionRepository).findById(questionId);
        verify(answerRepository, never()).save(any()); // 답변 저장 호출되지 않아야 함
    }

    @Test
    @DisplayName("문의 답변 생성 실패 - 권한 없음")
    void createAnswer_Failure_Unauthorized() {
        // Given
        Long questionId = 1L;
        Long traderId = 1L;
        Long unauthorizedTraderId = 2L;

        AnswerRequestDto requestDto = AnswerRequestDto.builder()
                .content("This is a test answer.")
                .build();

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        Strategy mockStrategy = mock(Strategy.class); // Mock Strategy 생성
        User mockAuthorizedTrader = mock(User.class); // 권한 있는 트레이더 생성

        // Mock 설정
        when(mockAuthorizedTrader.getUserId()).thenReturn(1L); // 권한 있는 트레이더 ID 설정
        when(mockStrategy.getUser()).thenReturn(mockAuthorizedTrader); // Strategy에 Trader 설정
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy); // Question에 Strategy 설정
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () ->
                answerService.createAnswer(questionId, unauthorizedTraderId, requestDto));

        assertEquals(ErrorCode.FORBIDDEN_ACCESS, exception.getErrorCode());
        verify(questionRepository).findById(questionId);
        verify(answerRepository, never()).save(any()); // 답변 저장 호출되지 않아야 함
    }

    @Test
    @DisplayName("문의 삭제 성공 - 답변 있음")
    void deleteQuestion_Success_WithAnswer() {
        // Given
        Long strategyId = 1L;
        Long questionId = 1L;
        Long userId = 1L;

        // Mock User
        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(userId);
        when(mockUser.getRole()).thenReturn(Role.INVESTOR);

        // Mock Strategy
        Strategy mockStrategy = mock(Strategy.class);

        // Mock Answer
        Answer mockAnswer = mock(Answer.class);

        // Mock Question
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getUser()).thenReturn(mockUser);
        when(mockQuestion.getAnswer()).thenReturn(mockAnswer); // 답변이 존재하는 경우

        // Repository 설정
        when(strategyRepository.findById(strategyId)).thenReturn(Optional.of(mockStrategy));
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // When
        assertDoesNotThrow(() -> questionService.deleteQuestion(strategyId, questionId, userId));

        // Then
        verify(strategyRepository).findById(strategyId);
        verify(questionRepository).findById(questionId);
        verify(userRepository).findById(userId);
        verify(answerRepository).delete(mockAnswer); // 답변 삭제 검증
        verify(questionRepository).delete(mockQuestion); // 문의 삭제 검증
    }

    @Test
    @DisplayName("문의 삭제 실패 - 답변이 존재하지 않는 경우")
    void deleteQuestion_Failure_AnswerNotFound() {
        // Given
        Long questionId = 1L;
        Long answerId = 1L;
        Long traderId = 2L; // 트레이더 ID

        // Mock Question
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getAnswer()).thenReturn(null); // 답변이 존재하지 않음

        // Repository 설정
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            answerService.deleteTraderAnswer(answerId, questionId, traderId);
        });

        // 검증
        assertEquals(ErrorCode.ANSWER_NOT_FOUND, exception.getErrorCode()); // 적절한 오류 코드 확인
        verify(questionRepository).findById(questionId); // 문의 조회 호출 확인
        verify(answerRepository, never()).delete(any(Answer.class)); // 답변 삭제 호출되지 않음
    }

    @Test
    @DisplayName("문의 삭제 실패 - 트레이더 권한 없음")
    void deleteQuestion_Failure_ForbiddenAccess() {
        // Given
        Long questionId = 1L;
        Long answerId = 1L;
        Long unauthorizedTraderId = 2L; // 권한 없는 트레이더 ID
        Long authorizedTraderId = 3L;  // 올바른 트레이더 ID

        // Mock Question
        Question mockQuestion = mock(Question.class);

        // Mock Answer
        Answer mockAnswer = mock(Answer.class);
        when(mockAnswer.getAnswerId()).thenReturn(answerId);
        when(mockQuestion.getAnswer()).thenReturn(mockAnswer);

        // Mock Strategy
        Strategy mockStrategy = mock(Strategy.class);
        User authorizedTrader = mock(User.class);
        when(authorizedTrader.getUserId()).thenReturn(authorizedTraderId);
        when(mockStrategy.getUser()).thenReturn(authorizedTrader);

        // 연결 설정
        when(mockQuestion.getStrategy()).thenReturn(mockStrategy);

        // Repository 설정
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            answerService.deleteTraderAnswer(answerId, questionId, unauthorizedTraderId);
        });

        // 검증
        assertEquals(ErrorCode.FORBIDDEN_ACCESS, exception.getErrorCode()); // 권한 없음 오류 코드 확인
        verify(questionRepository).findById(questionId); // 문의 조회 호출 확인
        verify(answerRepository, never()).delete(any(Answer.class)); // 답변 삭제 호출되지 않음
        verify(questionRepository, never()).delete(any(Question.class)); // 문의 삭제 호출되지 않음
    }

}
