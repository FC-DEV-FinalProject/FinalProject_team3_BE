package com.investmetic.domain.notice.dto.request;

import com.investmetic.domain.notice.model.entity.Notice;
import com.investmetic.domain.notice.model.entity.NoticeFile;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.global.util.s3.FilePath;
import com.investmetic.global.util.s3.S3FileService;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NoticeRegisterDto {

    private String title;
    private String content;
    private List<Integer> sizes;
    private List<String> filePaths;

    public Notice toEntity(User user) {
        return Notice.builder()
                .title(title)
                .content(content)
                .user(user)
                .build();
    }

    // NoticeFile 리스트 생
    public List<NoticeFile> toNoticeFiles(Notice notice, S3FileService s3FileService) {

        List<NoticeFile> noticeFiles = new ArrayList<>();

        for (int i = 0; i < filePaths.size(); i++) {
            String fileUrl = s3FileService.getS3Path(FilePath.NOTICE, filePaths.get(i), sizes.get(i));
            noticeFiles.add(NoticeFile.builder()
                    .notice(notice)
                    .fileUrl(fileUrl)
                    .fileName(filePaths.get(i))
                    .build());
        }
        return noticeFiles;
    }

}
