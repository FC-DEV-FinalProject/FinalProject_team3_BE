package com.investmetic.global.util.stibee.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StibeeSubscribeResponse<T> {
    @JsonProperty("Ok")
    private boolean ok;

    @JsonProperty("Error")
    private StibeeError error;

    @JsonProperty("Value")
    private T value;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class StibeeError {
        private String code;
        private int httpStatusCode;
        private String message;
    }
}
