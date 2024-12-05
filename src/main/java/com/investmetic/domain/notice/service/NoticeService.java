package com.investmetic.domain.notice.service;


import com.investmetic.domain.notice.dto.request.NoticeRegisterDto;
import com.investmetic.domain.notice.dto.request.NoticeRegistDto;
import com.investmetic.domain.notice.dto.request.NoticeRequestDto;
import com.investmetic.domain.notice.dto.response.NoticeDetailResponseDto;
import com.investmetic.domain.notice.model.entity.Notice;
import com.investmetic.domain.notice.model.entity.NoticeFile;
import com.investmetic.domain.notice.repository.NoticeFileRepository;
import com.investmetic.domain.notice.repository.NoticeRepository;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.model.entity.User;
import com.investmetic.domain.user.repository.UserRepository;
import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import com.investmetic.global.util.s3.S3FileService;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final NoticeFileRepository noticeFileRepository;
    private final S3FileService s3FileService;
    private final UserRepository userRepository;

    @Transactional
    public void updateNotice(Long noticeId, Long userId, NoticeRequestDto noticeRequestDto) {
        // 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        // 작성자 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USERS_NOT_FOUND));

        if (!notice.getUser().getUserId().equals(user.getUserId())) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED); // 작성자 확인 예외 처리
        }

        // 공지사항 업데이트
        notice.update(noticeRequestDto.getTitle(), noticeRequestDto.getContent());

        // 기존 첨부파일 삭제
        List<NoticeFile> existingFiles = noticeFileRepository.findByNotice_NoticeId(noticeId);
        existingFiles.forEach(file -> {
            s3FileService.deleteFromS3(file.getFileUrl()); // S3에서 파일 삭제
            noticeFileRepository.delete(file);           // DB에서 파일 삭제
        });

        // 새로운 첨부파일 저장
        noticeRequestDto.getFileUrl().forEach(filePath -> {
            String fileUrl = s3FileService.getS3Path(FilePath.NOTICE, filePath, filePath.length());
            NoticeFile noticeFile = new NoticeFile(notice, fileUrl, filePath);
            noticeFileRepository.save(noticeFile);
        });

        // 공지사항 저장
        noticeRepository.save(notice);
    }


    @Transactional
    public List<String> saveNotice(NoticeRegisterDto noticeRegisterDto, Long userId) {

        User user = User.builder().userId(userId).build();

        Notice notice = noticeRepository.save(noticeRegisterDto.toEntity(user));

        List<NoticeFile> noticeFiles = noticeRegisterDto.toNoticeFiles(notice, s3FileService);
        noticeFileRepository.saveAll(noticeFiles);

        return noticeFiles.stream()
                .map(file -> s3FileService.getPreSignedUrl(file.getFileUrl()))
                .toList();
    }

    public NoticeDetailResponseDto getNoticeDetail(Long noticeId) {
        noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        return noticeRepository.findByNoticeId(noticeId);
    }
}

