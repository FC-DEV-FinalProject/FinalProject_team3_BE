package com.investmetic.domain.notice.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class NoticeFileResponseDto {
    private Long noticeFileId;
    private String fileName;
    @QueryProjection
    public NoticeFileResponseDto(Long noticeFileId, String fileName) {
        this.noticeFileId = noticeFileId;
        this.fileName = fileName;
    }

}
