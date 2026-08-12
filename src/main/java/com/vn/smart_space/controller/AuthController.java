package com.vn.smart_space.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.request.auth.LoginRequest;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.service.auth.IAuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthenticationService authenticationService;

    // 1. Login Basic
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request) {

        LoginResponse loginResponse = authenticationService.loginBasic(request);
        return ResponseEntity.ok(ApiResponse.builder()
                .data(loginResponse)
                .message("Login success")
                .build());

    }

    // 2. Refresh Token
    // @PostMapping("/refresh")

    // 3. Logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("Logout success")
                .build());

    }

}
