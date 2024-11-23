package com.investmetic.global.util.stibee.dto.object;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeleteValue {
    private List<String> fail;
    private List<String> success;

    public DeleteValue(List<String> fail, List<String> success) {
        this.fail = fail;
        this.success = success;
    }
}
