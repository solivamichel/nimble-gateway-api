package com.nimble.gateway.service.impl;

import com.nimble.gateway.service.AuthorizerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AuthorizerClientImpl implements AuthorizerClient {

    private final WebClient webClient;

    public AuthorizerClientImpl(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://zsy6tx7aql.execute-api.sa-east-1.amazonaws.com").build();
    }

    @Override
    public boolean isApproved() {
        try {
            var response = webClient.get()
                    .uri("/authorizer")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return false;


            /* acesso o data.authorized
                {
                "status": "success",
                "data": {
                "authorized": true
                }
            }*/
            Object data = response.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                Object authorized = dataMap.get("authorized");
                return Boolean.TRUE.equals(authorized);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}