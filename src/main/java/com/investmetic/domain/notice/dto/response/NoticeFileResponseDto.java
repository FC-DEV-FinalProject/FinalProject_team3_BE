package com.investmetic.domain.notice.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class NoticeFileResponseDto {
    private Long NoticeFileId;
    private String fileName;

    @QueryProjection
    public NoticeFileResponseDto(Long NoticeFileId, String fileName) {
        this.NoticeFileId = NoticeFileId;
        this.fileName = fileName;
    }

}
