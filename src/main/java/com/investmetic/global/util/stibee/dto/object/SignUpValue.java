package com.investmetic.global.util.stibee.dto.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpValue {
    private List<Detail> failDuplicatedEmail;
    private List<Detail> failDuplicatedPhone;
    private List<Detail> failExistEmail;
    private List<Detail> failExistPhone;
    private List<Detail> failNoEmail;
    private List<Detail> failUnknown;
    private List<Detail> failValidation;
    private List<Detail> failValidationDateTime;
    private List<Detail> failWrongEmail;
    private List<Detail> failWrongPhone;
    private List<Detail> success;
    private List<Detail> update;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Detail {
        @JsonProperty("$createdTime")
        private String createdTime;

        @JsonProperty("$modifiedTime")
        private String modifiedTime;

        @JsonProperty("$status")
        private String status;

        @JsonProperty("$type")
        private String type;

        private String email;

        private String name;

        @JsonProperty("stb_ad_agreement")
        private boolean stbAdAgreement;
    }
}
