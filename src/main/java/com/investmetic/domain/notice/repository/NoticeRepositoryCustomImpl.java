package com.investmetic.domain.notice.repository;

import static com.investmetic.domain.notice.model.entity.QNotice.notice;
import static com.investmetic.domain.notice.model.entity.QNoticeFile.noticeFile;

import com.investmetic.domain.notice.dto.response.NoticeDetailResponseDto;
import com.investmetic.domain.notice.dto.response.NoticeFileResponseDto;
import com.investmetic.domain.notice.dto.response.QNoticeDetailResponseDto;
import com.investmetic.domain.notice.dto.response.QNoticeFileResponseDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoticeRepositoryCustomImpl implements NoticeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public NoticeDetailResponseDto findByNoticeId(Long noticeId) {
        List<NoticeFileResponseDto> files = queryFactory
                .select(new QNoticeFileResponseDto(noticeFile.noticeFileId, noticeFile.fileName))
                .from(noticeFile)
                .where(noticeFile.notice.noticeId.eq(noticeId))
                .fetch();

        NoticeDetailResponseDto noticeDetail = queryFactory
                .select(new QNoticeDetailResponseDto(
                        notice.title,
                        notice.content,
                        notice.createdAt
                ))
                .from(notice)
                .where(notice.noticeId.eq(noticeId))
                .fetchOne();

        noticeDetail.updateFiles(files);

        return noticeDetail;
    }
}
