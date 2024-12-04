package com.investmetic.domain.qna.service;

import com.investmetic.domain.qna.dto.request.AnswerRequestDto;
import com.investmetic.domain.qna.model.QnaState;
import com.investmetic.domain.qna.model.entity.Answer;
import com.investmetic.domain.qna.model.entity.Question;
import com.investmetic.domain.qna.repository.AnswerRepository;
import com.investmetic.domain.qna.repository.QuestionRepository;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    /**
     * 문의 답변 등록
     *
     * @param questionId       문의 ID
     * @param traderId         트레이더 ID
     * @param answerRequestDto 답변 요청 DTO
     */
    public void createAnswer(Long questionId, Long traderId, AnswerRequestDto answerRequestDto) {
        // 문의 조회
        Question question = findQuestionById(questionId);

        // 권한 검증
        validateTraderAuthorization(question, traderId);

        // 답변 생성 및 저장
        Answer answer = Answer.builder()
                .question(question)
                .content(answerRequestDto.getContent())
                .build();
        answerRepository.save(answer);

        // 문의 상태 업데이트
        updateQuestionState(question, QnaState.COMPLETED);
    }

    /**
     * 트레이더 전용 답변 삭제
     *
     * @param answerId   답변 ID
     * @param questionId 문의 ID
     * @param traderId   트레이더 ID
     */
    public void deleteTraderAnswer(Long answerId, Long questionId, Long traderId) {
        Question question = findQuestionById(questionId);
        Answer answer = validateAndGetAnswer(answerId, question);

        // 권한 검증
        validateTraderAuthorization(question, traderId);

        // 연관 관계 제거
        question.removeAnswer();
        questionRepository.save(question); // 상태 변경 저장

        // 답변 삭제
        deleteAnswer(answer, question);
    }

    /**
     * 관리자 전용 답변 삭제
     *
     * @param answerId   답변 ID
     * @param questionId 문의 ID
     */
    public void deleteAdminAnswer(Long answerId, Long questionId) {
        Question question = findQuestionById(questionId);
        Answer answer = validateAndGetAnswer(answerId, question);

        // 연관 관계 제거
        question.removeAnswer();
        questionRepository.save(question); // 상태 변경 저장

        // 답변 삭제
        deleteAnswer(answer, question);
    }

    /**
     * 답변 삭제 및 문의 상태 업데이트
     *
     * @param answer   삭제할 답변
     * @param question 답변이 속한 문의
     */
    private void deleteAnswer(Answer answer, Question question) {
        answerRepository.delete(answer);
        updateQuestionState(question, QnaState.WAITING);
    }

    /**
     * 문의 상태 업데이트
     *
     * @param question 문의
     * @param state    업데이트할 상태
     */
    private void updateQuestionState(Question question, QnaState state) {
        question.updateQnaState(state);
        questionRepository.save(question);
    }

    /**
     * 트레이더 권한 검증
     *
     * @param question 문의
     * @param traderId 트레이더 ID
     */
    private void validateTraderAuthorization(Question question, Long traderId) {
        if (!question.getStrategy().getUser().getUserId().equals(traderId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }


    /**
     * 답변 확인 및 예외 처리
     *
     * @param answerId 답변 ID
     * @param question 답변이 속한 문의
     * @return 답변 엔티티
     */
    private Answer validateAndGetAnswer(Long answerId, Question question) {
        Answer answer = question.getAnswer();
        if (answer == null || !answer.getAnswerId().equals(answerId)) {
            throw new BusinessException(ErrorCode.ANSWER_NOT_FOUND);
        }
        return answer;
    }

    /**
     * 문의 조회
     *
     * @param questionId 문의 ID
     * @return 문의 엔티티
     */
    private Question findQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
    }

    /**
     * 답변 조회
     *
     * @param answerId 답변 ID
     * @return 답변 엔티티
     */
    private Answer findAnswerById(Long answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANSWER_NOT_FOUND));
    }
}
