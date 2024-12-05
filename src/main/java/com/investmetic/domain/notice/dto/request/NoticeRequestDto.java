package com.investmetic.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeRequestDto {
    @NotBlank(message = "공지사항 제목을 입력하세요.")
    private final String title;
    @NotBlank(message = "공지사항 내용을 입력하세요.")
    private final String content;
    private final String nickName;
    private final List<String> fileUrl;

}

