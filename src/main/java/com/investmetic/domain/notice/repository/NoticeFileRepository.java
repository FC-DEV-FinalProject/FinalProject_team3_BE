package com.investmetic.domain.notice.repository;

import com.investmetic.domain.notice.model.entity.NoticeFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
    // 공지사항 ID를 기반으로 첨부파일 리스트를 조회하는 메서드
    List<NoticeFile> findByNotice_NoticeId(Long noticeId);
}

