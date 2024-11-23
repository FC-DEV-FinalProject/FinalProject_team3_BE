package com.investmetic.global.util.stibee.dto.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupErrorValue {
    @JsonProperty("Code")
    private String code;

    @JsonProperty("Message")
    private String message;

    public GroupErrorValue(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
