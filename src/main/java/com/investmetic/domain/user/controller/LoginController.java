package com.investmetic.domain.user.controller;


import com.investmetic.domain.user.dto.request.PasswordDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 기본 API", description = "사용자 관련 기본 기능 API")
@RestController
@RequestMapping("/api/users/login")
public class LoginController {

    @Operation(summary = "로그인 기능",
            description = "<a href='https://www.notion.so/789758335eef4feb879460367a16cf90' target='_blank'>API 명세서</a>")
    @PostMapping
    public void login(@RequestBody PasswordDto passwordDto){

        // 여기까지 안들어옴 그전에 필터에서 반환. 보여주기용.
    }

}
