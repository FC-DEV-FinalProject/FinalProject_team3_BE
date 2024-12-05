package com.investmetic.domain.notice.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class NoticeDetailResponse {
    private final Long noticeId;
    private final String title;
    private final String content;
    private final String nickName;
    private final List<String> fileUrls;
    private final LocalDateTime createdAt;

}
