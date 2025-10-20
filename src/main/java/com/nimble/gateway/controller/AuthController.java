package com.nimble.gateway.controller;

import com.nimble.gateway.dto.request.LoginRequest;
import com.nimble.gateway.dto.request.UserRequest;
import com.nimble.gateway.dto.response.AuthResponse;
import com.nimble.gateway.dto.response.UserResponse;
import com.nimble.gateway.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}