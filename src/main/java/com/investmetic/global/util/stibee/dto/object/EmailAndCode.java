package com.investmetic.global.util.stibee.dto.object;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailAndCode {

    // 이메일
    private String subscriber;

    //인증 코드
    private String code;

    public EmailAndCode(String email, String code) {
        this.subscriber = email;
        this.code = code;
    }
}
