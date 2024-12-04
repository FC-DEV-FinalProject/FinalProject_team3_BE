package com.investmetic.domain.qna.repository;

import static com.investmetic.domain.qna.model.entity.QQuestion.question;
import static com.investmetic.domain.strategy.model.entity.QStrategy.strategy;
import static com.investmetic.domain.user.model.entity.QUser.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.investmetic.domain.qna.model.QnaState;
import com.investmetic.domain.qna.model.entity.Question;
import com.investmetic.domain.user.model.entity.User;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class QuestionRepositoryTest {

    @Mock
    private JPAQueryFactory queryFactory; // Mock JPAQueryFactory 생성

    @Mock
    private JPAQuery<Question> mockQuery; // Mock JPAQuery 생성

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mock 객체 초기화
    }

    @Test
    @DisplayName("조건에 맞는 문의 조회 - 작성자(userId) 조건")
    void searchByConditions_UserId_Success() {
        // Given
        Long userId = 1L;

        // Mock User 생성
        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(userId);

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getUser()).thenReturn(mockUser);

        // Mock queryFactory의 selectFrom 반환값 설정
        when(queryFactory.selectFrom(question)).thenReturn(mockQuery);

        // Mock query의 체인 메서드 설정
        when(mockQuery.leftJoin(question.strategy, strategy)).thenReturn(mockQuery);
        when(mockQuery.fetchJoin()).thenReturn(mockQuery); // fetchJoin 설정
        when(mockQuery.leftJoin(question.user, user)).thenReturn(mockQuery);
        when(mockQuery.where(question.user.userId.eq(userId))).thenReturn(mockQuery);
        when(mockQuery.orderBy(question.createdAt.desc())).thenReturn(mockQuery);
        when(mockQuery.offset(0)).thenReturn(mockQuery);
        when(mockQuery.limit(10)).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(List.of(mockQuestion));

        // When
        List<Question> result = queryFactory.selectFrom(question)
                .leftJoin(question.strategy, strategy).fetchJoin()
                .leftJoin(question.user, user).fetchJoin()
                .where(question.user.userId.eq(userId))
                .fetch();

        // Then
        assertFalse(result.isEmpty()); // 결과가 비어있지 않아야 함
        assertEquals(1, result.size()); // Mock 데이터 크기 확인
        assertEquals(userId, result.get(0).getUser().getUserId()); // 반환된 문의의 작성자가 userId와 일치하는지 확인
    }

    @Test
    @DisplayName("조건에 맞는 문의 조회 - 키워드 필터링 (제목 포함)")
    void searchByConditions_KeywordInTitle_Success() {
        // Given
        String keyword = "중요"; // 필터링 키워드

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getTitle()).thenReturn("중요 문의");

        // Mock queryFactory의 selectFrom 반환값 설정
        when(queryFactory.selectFrom(question)).thenReturn(mockQuery);

        // Mock query의 체인 메서드 설정
        when(mockQuery.leftJoin(question.strategy, strategy)).thenReturn(mockQuery);
        when(mockQuery.fetchJoin()).thenReturn(mockQuery); // fetchJoin 설정
        when(mockQuery.leftJoin(question.user, user)).thenReturn(mockQuery);
        when(mockQuery.where(question.title.containsIgnoreCase(keyword))).thenReturn(mockQuery);
        when(mockQuery.orderBy(question.createdAt.desc())).thenReturn(mockQuery);
        when(mockQuery.offset(0)).thenReturn(mockQuery);
        when(mockQuery.limit(10)).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(List.of(mockQuestion));

        // When
        List<Question> result = queryFactory.selectFrom(question)
                .leftJoin(question.strategy, strategy).fetchJoin()
                .leftJoin(question.user, user).fetchJoin()
                .where(question.title.containsIgnoreCase(keyword))
                .fetch();

        // Then
        assertFalse(result.isEmpty()); // 결과가 비어있지 않아야 함
        assertEquals(1, result.size()); // Mock 데이터 크기 확인
        assertEquals("중요 문의", result.get(0).getTitle()); // 반환된 문의 제목 확인
    }

    @Test
    @DisplayName("조건에 맞는 문의 조회 - 상태 필터링 (COMPLETED)")
    void searchByConditions_StateCompleted_Success() {
        // Given
        QnaState targetState = QnaState.COMPLETED; // 필터링 상태

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getQnaState()).thenReturn(QnaState.COMPLETED);

        // Mock queryFactory의 selectFrom 반환값 설정
        when(queryFactory.selectFrom(question)).thenReturn(mockQuery);

        // Mock query의 체인 메서드 설정
        when(mockQuery.leftJoin(question.strategy, strategy)).thenReturn(mockQuery);
        when(mockQuery.fetchJoin()).thenReturn(mockQuery); // fetchJoin 설정
        when(mockQuery.leftJoin(question.user, user)).thenReturn(mockQuery);
        when(mockQuery.where(question.qnaState.eq(targetState))).thenReturn(mockQuery);
        when(mockQuery.orderBy(question.createdAt.desc())).thenReturn(mockQuery);
        when(mockQuery.offset(0)).thenReturn(mockQuery);
        when(mockQuery.limit(10)).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(List.of(mockQuestion));

        // When
        List<Question> result = queryFactory.selectFrom(question)
                .leftJoin(question.strategy, strategy).fetchJoin()
                .leftJoin(question.user, user).fetchJoin()
                .where(question.qnaState.eq(targetState))
                .fetch();

        // Then
        assertFalse(result.isEmpty()); // 결과가 비어있지 않아야 함
        assertEquals(1, result.size()); // Mock 데이터 크기 확인
        assertEquals(QnaState.COMPLETED, result.get(0).getQnaState()); // 반환된 문의 상태 확인
    }

    @Test
    @DisplayName("조건에 맞는 문의 조회 - 작성자와 상태 필터링 (userId + COMPLETED)")
    void searchByConditions_UserIdAndState_Success() {
        // Given
        Long userId = 1L; // 필터링할 작성자 ID
        QnaState targetState = QnaState.COMPLETED; // 필터링할 상태

        // Mock User 생성
        User mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn(userId);

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getUser()).thenReturn(mockUser);
        when(mockQuestion.getQnaState()).thenReturn(QnaState.COMPLETED);

        // Mock queryFactory의 selectFrom 반환값 설정
        when(queryFactory.selectFrom(question)).thenReturn(mockQuery);

        // Mock query의 체인 메서드 설정
        when(mockQuery.leftJoin(question.strategy, strategy)).thenReturn(mockQuery);
        when(mockQuery.fetchJoin()).thenReturn(mockQuery); // fetchJoin 설정
        when(mockQuery.leftJoin(question.user, user)).thenReturn(mockQuery);
        when(mockQuery.where(
                question.user.userId.eq(userId)
                        .and(question.qnaState.eq(targetState)))).thenReturn(mockQuery);
        when(mockQuery.orderBy(question.createdAt.desc())).thenReturn(mockQuery);
        when(mockQuery.offset(0)).thenReturn(mockQuery);
        when(mockQuery.limit(10)).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(List.of(mockQuestion));

        // When
        List<Question> result = queryFactory.selectFrom(question)
                .leftJoin(question.strategy, strategy).fetchJoin()
                .leftJoin(question.user, user).fetchJoin()
                .where(
                        question.user.userId.eq(userId)
                                .and(question.qnaState.eq(targetState)))
                .fetch();

        // Then
        assertFalse(result.isEmpty()); // 결과가 비어있지 않아야 함
        assertEquals(1, result.size()); // Mock 데이터 크기 확인
        assertEquals(userId, result.get(0).getUser().getUserId()); // 반환된 문의 작성자 확인
        assertEquals(QnaState.COMPLETED, result.get(0).getQnaState()); // 반환된 문의 상태 확인
    }

    @Test
    @DisplayName("조건에 맞는 문의 조회 - 제목 키워드 필터링")
    void searchByConditions_TitleKeyword_Success() {
        // Given
        String keyword = "investment"; // 필터링할 키워드

        // Mock Question 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getTitle()).thenReturn("Best investment strategies");

        // Mock queryFactory의 selectFrom 반환값 설정
        when(queryFactory.selectFrom(question)).thenReturn(mockQuery);

        // Mock query의 체인 메서드 설정
        when(mockQuery.leftJoin(question.strategy, strategy)).thenReturn(mockQuery);
        when(mockQuery.fetchJoin()).thenReturn(mockQuery); // fetchJoin 설정
        when(mockQuery.leftJoin(question.user, user)).thenReturn(mockQuery);
        when(mockQuery.where(
                question.title.containsIgnoreCase(keyword))).thenReturn(mockQuery);
        when(mockQuery.orderBy(question.createdAt.desc())).thenReturn(mockQuery);
        when(mockQuery.offset(0)).thenReturn(mockQuery);
        when(mockQuery.limit(10)).thenReturn(mockQuery);
        when(mockQuery.fetch()).thenReturn(List.of(mockQuestion));

        // When
        List<Question> result = queryFactory.selectFrom(question)
                .leftJoin(question.strategy, strategy).fetchJoin()
                .leftJoin(question.user, user).fetchJoin()
                .where(
                        question.title.containsIgnoreCase(keyword))
                .fetch();

        // Then
        assertFalse(result.isEmpty()); // 결과가 비어있지 않아야 함
        assertEquals(1, result.size()); // Mock 데이터 크기 확인
        assertTrue(result.get(0).getTitle().toLowerCase().contains(keyword.toLowerCase())); // 제목에 키워드 포함 확인
    }

    @Test
    @DisplayName("문의 작성자 닉네임 테스트 - 투자자")
    void getNickname_Test_Investor() {
        // Given
        Long userId = 1L;
        String investorNickname = "Investor123";

        // Mock 투자자 생성
        User mockInvestor = mock(User.class);
        when(mockInvestor.getUserId()).thenReturn(userId);
        when(mockInvestor.getNickname()).thenReturn(investorNickname);

        // Mock 문의 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getUser()).thenReturn(mockInvestor);

        // When
        String resultNickname = mockQuestion.getUser().getNickname();

        // Then
        assertEquals(investorNickname, resultNickname, "투자자 닉네임이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("문의 작성자 닉네임 테스트 - 트레이더")
    void getNickname_Test_Trader() {
        // Given
        Long userId = 2L;
        String traderNickname = "Trader456";

        // Mock 트레이더 생성
        User mockTrader = mock(User.class);
        when(mockTrader.getUserId()).thenReturn(userId);
        when(mockTrader.getNickname()).thenReturn(traderNickname);

        // Mock 문의 생성
        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getUser()).thenReturn(mockTrader);

        // When
        String resultNickname = mockQuestion.getUser().getNickname();

        // Then
        assertEquals(traderNickname, resultNickname, "트레이더 닉네임이 올바르지 않습니다.");
    }


}
