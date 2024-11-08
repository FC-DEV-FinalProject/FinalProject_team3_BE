package com.investmetic.global.util.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilePath {
    USER_PROFILE("user-profile/"),
    STRATEGY_EXCEL("strategy/excel/"),
    STRATEGY_IMAGE("strategy/image/"),
    NOTICE("notice/"),
    QNA("qna/");

    private final String path;

}
