package com.investmetic.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode API 응답에서 발생할 수 있는 오류 코드와 메시지 enum 각 오류 상황에 대한 HttpStatus, 오류 코드, 설명 메시지
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 공통(Common) 오류
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, 1001, "잘못된 값을 입력했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 1002, "허용되지 않은 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.BAD_REQUEST, 1003, "엔티티를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1004, "서버 오류가 발생하였습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, 1005, "잘못된 유형 값을 입력하였습니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, 1006, "액세스가 거부되었습니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, 1007, "비정상적 접근입니다."),
    INVALID_DATE(HttpStatus.BAD_REQUEST, 1008, "날짜 형식을 확인해주세요."),
    EMPTY_PATH_VARIABLE(HttpStatus.BAD_REQUEST, 1008, "필수 경로 변수가 누락되었습니다. 요청 경로에 올바른 값을 입력해 주세요."),
    NOT_SUPPORTED_TYPE(HttpStatus.BAD_REQUEST, 1009, "잘못된 형식 파일입니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 1010, "파일 삭제 중 오류가 발생했습니다."),

    //사용자 관련 오류
    USER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, 2001, "해당 회원의 정보를 찾을 수 없습니다."),
    USERS_NOT_FOUND(HttpStatus.NOT_FOUND, 2002, "조회된 회원이 없습니다."),
    SIGN_UP_FAILED(HttpStatus.BAD_REQUEST, 2003, "회원가입에 실패하였습니다."),
    ACCOUNT_DELETION_FAILED(HttpStatus.NOT_FOUND, 2004, "탈퇴에 실패하였습니다."),
    PASSWORD_AUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST, 2101, "패스워드 인증에 실패하셨습니다."),
    LOGIN_FAILED(HttpStatus.BAD_REQUEST, 2102, "등록되지 않은 이메일이거나, 이메일 혹은 비밀번호를 잘못 입력하였습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, 2103, "해당 작업을 수행하기 위한 권한이 부족합니다."),
    VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, 2104, "인증에 실패하였습니다. 다시 입력해 주세요."),
    PERSONAL_INFO_UPDATE_FAILED(HttpStatus.BAD_REQUEST, 2201, "개인정보 수정/등록에 실패하였습니다."),
    PASSWORD_RESET_FAILED(HttpStatus.BAD_REQUEST, 2202, "비밀번호 재설정에 실패하였습니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, 2203, "사용할 수 없는 닉네임입니다."),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, 2204, "사용할 수 없는 이메일입니다."),
    INVALID_PHONE(HttpStatus.BAD_REQUEST, 2207, "사용할 수 없는 전화번호입니다."),
    EMAIL_NOT_FOUND_FOR_PHONE_NUMBER(HttpStatus.BAD_REQUEST, 2205, "입력한 핸드폰 번호에 해당하는 이메일을 찾을 수 없습니다. 다시 입력해 주세요."),
    USER_STATISTICS_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, 2301, "사용자 통계 데이터를 조회할 수 없습니다."),
    TRADER_LIST_RETRIEVAL_FAILED(HttpStatus.NOT_FOUND, 2302, "트레이더 목록 조회에 실패하였습니다."),
    EMAIL_SEND_FAILED(HttpStatus.BAD_REQUEST, 2401, "이메일 전송이 실패하였습니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST, 2501, "Refresh token is missing."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 2502, "Refresh token has expired."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, 2503, "Invalid refresh token."),
    AUTHORIZATION_DENIED(HttpStatus.FORBIDDEN, 2504, "권한이 없습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.BAD_REQUEST, 2208, "기존 비밀번호와 동일합니다."),


    // 전략 관련 오류
    PROPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, 3001, "해당 제안서를 찾을 수 없습니다."),
    STRATEGY_REGISTER_FAILED(HttpStatus.BAD_REQUEST, 3002, "전략 등록에 실패했습니다."),
    STRATEGY_NOT_FOUND(HttpStatus.NOT_FOUND, 3003, "전략을 찾을 수 없습니다."),
    STRATEGY_QUERY_ERROR(HttpStatus.BAD_REQUEST, 3005, "전략 조회 중 오류가 발생했습니다."),
    PROPOSAL_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, 3006, "제안서 업로드에 실패했습니다."),
    SUBSCRIPTION_CANCEL_FAILED(HttpStatus.BAD_REQUEST, 3007, "구독 취소에 실패했습니다."),
    FAVORITE_QUERY_FAILED(HttpStatus.BAD_REQUEST, 3008, "관심 전략 조회에 실패했습니다."),
    VISIBILITY_UPDATE_FAILED(HttpStatus.BAD_REQUEST, 3009, "전략 공개 여부 수정에 실패했습니다."),
    DAILY_ANALYSIS_QUERY_FAILED(HttpStatus.BAD_REQUEST, 3010, "전략 일간 분석 조회에 실패했습니다."),
    DAILY_ANALYSIS_UPDATE_FAILED(HttpStatus.BAD_REQUEST, 3011, "전략 일간 분석 등록/수정에 실패했습니다."),
    DAILY_ANALYSIS_DELETE_FAILED(HttpStatus.BAD_REQUEST, 3012, "전략 일간 분석 삭제에 실패했습니다."),
    DAILY_ANALYSIS_EXCEL_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, 3013, "일간 분석 엑셀 업로드에 실패했습니다."),
    DAILY_ANALYSIS_REGISTER_FAILED(HttpStatus.BAD_REQUEST, 3014, "해당 날짜 데이터 삭제를 실패했습니다."),
    ACCOUNT_IMAGE_QUERY_FAILED(HttpStatus.BAD_REQUEST, 3015, "실계좌 이미지 조회에 실패했습니다."),
    ACCOUNT_IMAGE_SAVE_FAILED(HttpStatus.BAD_REQUEST, 3016, "실계좌 이미지 등록/수정에 실패했습니다."),
    ACCOUNT_IMAGE_DELETE_FAILED(HttpStatus.BAD_REQUEST, 3017, "실계좌 이미지 삭제에 실패했습니다."),
    STATISTICS_NOT_FOUND(HttpStatus.BAD_REQUEST, 3018, "전략 통계가 존재하지 않습니다."),
    ANALYSIS_OPTION_NOT_FOUND(HttpStatus.BAD_REQUEST, 3019, "유효하지 않은 옵션입니다."),
    DAILY_ANALYSIS_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, 3020, "해당 날짜의 전략 통계가 이미 존재합니다."),
    DAILY_ANALYSIS_NOT_FOUND(HttpStatus.BAD_REQUEST, 3021, "해당 날짜의 전략 통계가 존재하지 않습니다."),

    STOCKTYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, 3021, "해당 종목이 존재하지 않습니다."),
    TRADETYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, 3022, "해당 매매 유형이 존재하지 않습니다."),
    DUPLICATE_DATE_IN_REQUEST(HttpStatus.BAD_REQUEST, 3023, "중복된 날짜의 요청이 존재합니다."),
    ACCOUNT_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, 3030, "해당 실계좌 인증 이미지가 존재하지 않습니다."),
    EXCEL_CREATE_ERROR(HttpStatus.NOT_FOUND, 3031, "Excel 생성 중 오류가 발생했습니다."),
    EXCEL_DOWNLOAD_ERROR(HttpStatus.NOT_FOUND, 3032, "Excel 다운로드 중 오류가 발생했습니다."),
    SELF_SUBSCRIPTION_NOT_ALLOWED(HttpStatus.FORBIDDEN, 3033, "본인 전략에는 구독할 수 없습니다."),


    // 전략리뷰 관련오류(3300번대 );
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, 3301, "해당 전략의 리뷰를 찾을 수 없습니다."),
    CANNOT_REVIEW_OWN_STRATEGY(HttpStatus.FORBIDDEN, 3302, "본인전략에는 리뷰를 달 수 없습니다."),
    DUPLICATE_REVIEW(HttpStatus.FORBIDDEN, 3303, "리뷰는 한번만 가능합니다."),

    //문의 관련 오류(5000번대 );
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, 5001, "해당 문의를 찾을 수 없습니다."),
    INVALID_SORT_PARAMETER(HttpStatus.BAD_REQUEST, 5002, "정렬 조건이 잘못되었습니다."),
    EMPTY_QUESTION_LIST(HttpStatus.NOT_FOUND, 5003, "조회 가능한 문의가 없습니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, 5004, "해당 답변을 찾을 수 없습니다."),

    // 공지사항 관련 오류(6000번대 );
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, 6001, "해당 공지사항을 찾을 수 없습니다.");

    private final HttpStatus status; // HTTP 상태 코드
    private final int code; // 고유 오류 코드
    private final String message; // 오류 메시지
}