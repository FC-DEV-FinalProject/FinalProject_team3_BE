package com.investmetic.global.util.stibee.dto.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnsubscribeValue {

    //구독자 수신거부 성공 리스트
    @JsonProperty("success")
    private List<String> successUsers;

    //구독자 실패 또는 이미 수신거부 중 리스트
    @JsonProperty("failOrAlreadyUnsubscribe")
    private List<String> failUsers;

}
