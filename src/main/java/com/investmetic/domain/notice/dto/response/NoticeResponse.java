package com.investmetic.domain.notice.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class NoticeResponse {
    private final Long noticeId;
    private final String title;
    private final String nickName;
    private final LocalDateTime createdAt;

}
