package com.investmetic.global.util.stibee.client;


import com.investmetic.global.util.stibee.dto.object.EmailAndCode;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@Component
@HttpExchange
public interface AutoApiStibeeClient {

    @PostExchange("/NmEwMmU2ZTItNzU2Ni00MzNhLWJkODktNzAzMjljOTQ2Mjhl")
    void sendAuthenticationCode(@RequestBody EmailAndCode emailAndCode);


}
