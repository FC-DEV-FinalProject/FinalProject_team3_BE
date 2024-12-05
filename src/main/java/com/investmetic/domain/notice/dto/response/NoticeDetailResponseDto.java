package com.investmetic.domain.notice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;

@Getter
public class NoticeDetailResponseDto {
    private String title;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private List<NoticeFileResponseDto> files;

    @QueryProjection
    public NoticeDetailResponseDto(String title, String content, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public void updateFiles(List<NoticeFileResponseDto> files) {
        this.files = files;
    }
}
