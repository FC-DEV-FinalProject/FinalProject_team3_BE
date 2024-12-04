package com.investmetic.domain.qna.service;

import static com.investmetic.domain.qna.model.entity.QQuestion.question;

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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final StrategyRepository strategyRepository;
    private final JPAQueryFactory queryFactory;
    private final AnswerRepository answerRepository;

    /**
     * 문의 등록
     */
    @Transactional
    public void createQuestion(Long userId, Long strategyId, QuestionRequestDto questionRequestDto) {

        User user = findUserById(userId);
        Strategy strategy = findStrategyById(strategyId);

        Question question = Question.from(user, strategy, questionRequestDto);
        questionRepository.save(question);
    }

    /**
     * 문의 삭제
     */
    @Transactional
    public void deleteQuestion(Long strategyId, Long questionId, Long userId) {
        Question question = findQuestionById(questionId);
        Strategy strategy = findStrategyById(strategyId);
        User user = findUserById(userId);
        validateAccess(user, question, userId);
        // 답변 삭제
        Answer answer = question.getAnswer();
        if (answer != null) {
            answerRepository.delete(answer); // 답변 삭제
        }
        questionRepository.delete(question);
    }

    /**
     * 투자자 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<QuestionsResponse> getInvestorQuestions(Long userId, Role userRole,
                                                                   String keyword,
                                                                   SearchCondition searchCondition,
                                                                   StateCondition stateCondition,
                                                                   Pageable pageable) {
        return searchQuestions(userId, keyword, searchCondition, stateCondition, pageable, userRole);
    }

    /**
     * 트레이더 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<QuestionsResponse> getTraderQuestions(Long userId, Role userRole,
                                                                 String keyword,
                                                                 SearchCondition searchCondition,
                                                                 StateCondition stateCondition,
                                                                 Pageable pageable) {
        return searchQuestions(userId, keyword, searchCondition, stateCondition, pageable, userRole);
    }

    /**
     * 관리자 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<QuestionsResponse> getAdminQuestions(Long userId, Role userRole,
                                                                String keyword,
                                                                SearchCondition searchCondition,
                                                                StateCondition stateCondition,
                                                                Pageable pageable) {
        // 관리자 조회는 userId를 null로 전달하여 모든 데이터를 조회
        return searchQuestions(null, keyword, searchCondition, stateCondition, pageable, userRole);
    }

    /**
     * 문의 상세 조회 (투자자/트레이더)
     */
    @Transactional(readOnly = true)
    public QuestionsDetailResponse getQuestionDetail(Long questionId, Long userId, Role role) {
        Question question = findQuestionById(questionId);
        User user = findUserById(userId);

        // 접근 권한 검증
        validateAccess(user, question, userId);

        // DTO 생성
        return createQuestionsDetailResponse(question, role);
    }

    /**
     * 관리자 전용 문의 상세 조회
     */
    @Transactional(readOnly = true)
    public QuestionsDetailResponse getAdminQuestionDetail(Long questionId, Role userRole) {
        // 문의 데이터 조회
        Question question = findQuestionById(questionId);

        // DTO 생성
        return createQuestionsDetailResponse(question, userRole);
    }

    /**
     * QuestionsDetailResponse 생성
     */
    private QuestionsDetailResponse createQuestionsDetailResponse(Question question, Role role) {
        Answer answer = question.getAnswer();

        // 역할에 따른 정보를 설정
        String profileImageUrl = "http://default-image-url.com/default.jpg"; // 기본 이미지 URL
        String nickname;

        if (Role.isInvestor(role)) {
            // 투자자: 트레이더 정보 반환
            profileImageUrl = question.getUser().getImageUrl(); // 투자자 이미지
            nickname = question.getUser().getNickname();       // 투자자 닉네임
        } else if (Role.isTrader(role)) {
            // 트레이더: 투자자 정보 반환
            profileImageUrl = question.getStrategy().getUser().getImageUrl();              // 트레이더 이미지
            nickname = question.getStrategy().getUser().getNickname();                    // 트레이더 닉네임
        } else {
            // 관리자: 기본 정보 반환
            profileImageUrl = question.getUser().getImageUrl();              // 관리자 이미지
            nickname = question.getUser().getNickname();                    // 관리자 닉네임
        }

        return QuestionsDetailResponse.builder()
                .questionId(question.getQuestionId())
                .title(question.getTitle())
                .questionContent(question.getContent())
                .answerContent(answer != null ? answer.getContent() : "답변 없음")
                .strategyName(question.getStrategy() != null ? question.getStrategy().getStrategyName() : "전략 없음")
                .profileImageUrl(profileImageUrl)
                .nickname(nickname)
                .state(question.getQnaState().name())
                .questionCreatedAt(question.getCreatedAt())
                .answerCreatedAt(answer != null ? answer.getCreatedAt() : null)
                .build();
    }

    // ================== Private Methods ==================

    /**
     * 사용자 조회 및 예외 처리 userId가 null인 경우 USERS_NOT_FOUND 예외를 던짐
     */
    private User findUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.USERS_NOT_FOUND);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USERS_NOT_FOUND));
    }

    /**
     * 전략 조회 및 예외 처리
     */
    private Strategy findStrategyById(Long strategyId) {
        return strategyRepository.findById(strategyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STRATEGY_NOT_FOUND));
    }

    /**
     * 질문 조회 및 예외 처리
     */
    private Question findQuestionById(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
    }

    /**
     * 답변 조회 및 예외 처리
     */
    private Answer findAnswerById(Long answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANSWER_NOT_FOUND));
    }

    /**
     * 접근 권한 검증
     */
    private void validateAccess(User user, Question question, Long userId) {
        switch (user.getRole()) {
            case INVESTOR:
                validateInvestorAccess(user, question, userId);
                break;
            case TRADER:
                validateTraderAccess(user, question, userId);
                break;
            case INVESTOR_ADMIN:
            case TRADER_ADMIN:
            case SUPER_ADMIN:
                validateAdminAccess(user);
                break;
            default:
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    /**
     * 투자자 접근 권한 검증
     */
    private void validateInvestorAccess(User user, Question question, Long userId) {
        if (!question.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    /**
     * 트레이더 접근 권한 검증
     */
    private void validateTraderAccess(User user, Question question, Long userId) {
        if (!question.getStrategy().getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    /**
     * 관리자 접근 권한 검증
     */
    private void validateAdminAccess(User user) {
        if (!Role.isAdmin(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }


    public PageResponseDto<QuestionsResponse> searchQuestions(Long userId, String keyword,
                                                              SearchCondition searchCondition,
                                                              StateCondition stateCondition,
                                                              Pageable pageable, Role role) {
        // 검색 조건 생성
        List<BooleanExpression> conditions = buildConditions(
                userId,
                keyword,
                searchCondition,
                stateCondition,
                role
        );

        // DTO 변환
        Page<Question> questions = questionRepository.searchByConditions(conditions, pageable, queryFactory);
        return new PageResponseDto<>(questions.map(q -> {
            // 프로필 이미지 URL 및 닉네임 설정
            String profileImageUrl = null;
            String nickname = null;

            if (role == Role.TRADER && q.getStrategy() != null && q.getStrategy().getUser() != null) {
                profileImageUrl = q.getStrategy().getUser().getImageUrl(); // 전략의 유저 이미지
                nickname = q.getStrategy().getUser().getNickname();        // 전략의 유저 닉네임
            } else if (q.getUser() != null) {
                profileImageUrl = q.getUser().getImageUrl();              // 문의 작성자 이미지
                nickname = q.getUser().getNickname();                    // 문의 작성자 닉네임
            }

            return QuestionsResponse.builder()
                    .questionId(q.getQuestionId())
                    .title(q.getTitle())
                    .strategyName(q.getStrategy() != null ? q.getStrategy().getStrategyName() : "전략 없음")
                    .profileImageUrl(profileImageUrl != null ? profileImageUrl : "이미지 없음")
                    .nickname(nickname != null ? nickname : "닉네임 없음")
                    .stateCondition(q.getQnaState().name())
                    .createdAt(q.getCreatedAt())
                    .build();
        }));
    }


    /**
     * 검색 조건 빌드
     */
    private List<BooleanExpression> buildConditions(Long userId, String keyword, SearchCondition searchCondition,
                                                    StateCondition stateCondition, Role role) {
        List<BooleanExpression> conditions = new ArrayList<>();

        // 역할에 따른 기본 조건 추가
        if (Role.isInvestor(role) && userId != null) {
            // 투자자 또는 투자자 관리자
            conditions.add(question.user.userId.eq(userId));
        } else if (Role.isTrader(role) && userId != null) {
            // 트레이더 또는 트레이더 관리자
            conditions.add(question.strategy.user.userId.eq(userId));
        } else if (Role.isAdmin(role)) {
            // 관리자 (슈퍼 관리자, 투자자 관리자, 트레이더 관리자)
        }

        // 키워드 검색 조건 추가
        if (keyword != null && !keyword.isBlank()) {
            switch (searchCondition) {
                case TITLE:
                    conditions.add(question.title.containsIgnoreCase(keyword));
                    break;
                case CONTENT:
                    conditions.add(question.content.containsIgnoreCase(keyword));
                    break;
                case TITLE_OR_CONTENT:
                    conditions.add(
                            question.title.containsIgnoreCase(keyword)
                                    .or(question.content.containsIgnoreCase(keyword))
                    );
                    break;
                case STRATEGY_NAME:
                    conditions.add(question.strategy.strategyName.containsIgnoreCase(keyword));
                    break;
                case INVESTOR_NAME:
                    if (Role.isTrader(role) || Role.isAdmin(role)) {
                        conditions.add(question.user.nickname.containsIgnoreCase(keyword));
                    }
                    break;
                case TRADER_NAME:
                    if (Role.isInvestor(role) || Role.isAdmin(role)) {
                        conditions.add(question.strategy.user.nickname.containsIgnoreCase(keyword));
                    }
                    break;
                default:
                    break;
            }
        }

        // 상태 조건 추가
        if (stateCondition != null) {
            switch (stateCondition) {
                case WAITING:
                    conditions.add(question.qnaState.eq(QnaState.WAITING));
                    break;
                case COMPLETED:
                    conditions.add(question.qnaState.eq(QnaState.COMPLETED));
                    break;
                default:
                    break;
            }
        }

        return conditions;
    }

}
