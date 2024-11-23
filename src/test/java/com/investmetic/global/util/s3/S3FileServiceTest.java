package com.investmetic.global.util.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.investmetic.global.exception.BusinessException;
import com.investmetic.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class S3FileServiceTest {

    /*
     * api요청은 postman으로 합시다.
     * */
    @Autowired
    S3FileService s3FileService;

    @Value("${cloud.aws.s3.defaultImgPath}")
    private String bucketPath; // "https://{버킷 이름}.s3.{region}.amazonaws.com/"

    // fileName만 받을 건지, ContentType이나 MimeType도 받을건지에 따라 test코드도 달라질 듯

    @Test
    @DisplayName("정상 이미지 확장자랑 사이즈 판별")
    void validatePassImageCase() {

        String fileName = "testImage.jpg";
        int size = 1000;

        //객체 URL 획득
        String s3Path = s3FileService.getS3Path(FilePath.STRATEGY_IMAGE, fileName, size);

        assertTrue(s3Path.contains(FilePath.STRATEGY_IMAGE.getPath()));
        assertTrue(s3Path.contains(fileName));
    }

    @Test
    @DisplayName("이미지 확장자 다를 때")
    void noPassImageCaseExtension() {

        String fileName = "testImage.webp"; // 확장자 불충족.
        int size = 1000; //약 1KB

        // 확장자 다르면 RuntimeException던짐.
        BusinessException exception = assertThrows(BusinessException.class,
                () -> s3FileService.getS3Path(FilePath.STRATEGY_IMAGE, fileName, size));

        assertEquals(ErrorCode.NOT_SUPPORTED_TYPE, exception.getErrorCode());
    }

    @Test
    @DisplayName("이미지 사이즈 2MB 이상")
    void noPassImageCaseSize() {
        String fileName = "testImage.jpg"; // 확장자 충족.
        int size = 1024 * 1024 * 2; //2MB -> 2MB미만만 가능.

        // 2MB 이상이면  RuntimeException던짐.
        BusinessException exception = assertThrows(BusinessException.class,
                () -> s3FileService.getS3Path(FilePath.STRATEGY_IMAGE, fileName, size));

        assertEquals(ErrorCode.NOT_SUPPORTED_TYPE, exception.getErrorCode());
    }

    @Test
    @DisplayName("정상 엑셀 확장자랑 사이즈 판별")
    void passExcelCase() {

        String fileName = "testExcel.xlsx"; // 확장자 충족.
        int size = 1024 * 1024 * 100; //약 100MB

        //객체 URL 획득
        String s3Path = s3FileService.getS3Path(FilePath.STRATEGY_EXCEL, fileName, size);

        assertTrue(s3Path.contains(FilePath.STRATEGY_EXCEL.getPath()));

        assertTrue(s3Path.contains(fileName));
    }

    @Test
    @DisplayName("공지사항 정상 문서 확장자랑 사이즈 판별")
    void passNoticeCase1() {

        String fileName = "testDocs.docx"; // 확장자 충족
        int size = 1024 * 1024; // 1MB

        String s3Path = s3FileService.getS3Path(FilePath.NOTICE, fileName, size);

        assertTrue(s3Path.contains(FilePath.NOTICE.getPath()));
        assertTrue(s3Path.contains(fileName));
    }

    @Test
    @DisplayName("공지사항 정상 이미지")
    void passNoticeCase2() {

        String fileName = "testImage.jpg"; // 확장자 충족
        int size = 1024 * 1024; // 1MB

        String s3Path = s3FileService.getS3Path(FilePath.NOTICE, fileName, size);

        assertTrue(s3Path.contains(FilePath.NOTICE.getPath()));
        assertTrue(s3Path.contains(fileName));
    }


    @Test
    @DisplayName("공지사항 문서 확장자 다를 때")
    void noPassNoticeCase1() {
        String fileName = "testDocs.docc"; //docc 으로 철자 다르게 함.

        int size = 1024 * 1024; //1MB

        BusinessException exception = assertThrows(BusinessException.class,
                () -> s3FileService.getS3Path(FilePath.NOTICE, fileName, size));

        assertEquals(ErrorCode.NOT_SUPPORTED_TYPE, exception.getErrorCode());

    }

    @Test
    @DisplayName("공지사항에 이미지 이상한거 들어갔을 때")
    void noPassNoticeCase2() {
        String fileName = "testDocs.jpa"; //jpa 으로 철자 다르게 함.

        int size = 1024 * 1024; //1MB

        // 확장자 틀릴 때 RuntimeException던짐.
        BusinessException exception = assertThrows(BusinessException.class,
                () -> s3FileService.getS3Path(FilePath.NOTICE, fileName, size));

        assertEquals(ErrorCode.NOT_SUPPORTED_TYPE, exception.getErrorCode());
    }


}


