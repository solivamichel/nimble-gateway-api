package com.nimble.gateway.service;

import com.nimble.gateway.dto.request.UserRequest;
import com.nimble.gateway.dto.response.UserResponse;
import com.nimble.gateway.entity.User;

import java.math.BigDecimal;

public interface UserService {

    UserResponse register(UserRequest request);
    String login(String username, String password);
    User getByCpfOrEmail(String username);
    void addBalance(User user, BigDecimal amount);
}

