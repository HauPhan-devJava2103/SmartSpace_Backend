package com.vn.smart_space.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vn.smart_space.dto.ApiResponse;
import com.vn.smart_space.dto.request.auth.LoginRequest;
import com.vn.smart_space.dto.request.auth.RefreshTokenRequest;
import com.vn.smart_space.dto.request.auth.RegisterRequest;
import com.vn.smart_space.dto.request.auth.ResetPasswordRequest;
import com.vn.smart_space.dto.request.auth.SendOtpRequest;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.service.auth.IAuthenticationService;
import com.vn.smart_space.service.user.IUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

        private final IAuthenticationService authenticationService;
        private final IUserService userService;

        // 1. Login Basic
        @PostMapping("/login")
        public ResponseEntity<ApiResponse> login(@RequestBody @Valid LoginRequest request) {

                LoginResponse loginResponse = authenticationService.loginBasic(request);
                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .data(loginResponse)
                                .message("Login success")
                                .build());

        }

        // 2. Refresh Token
        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse> refresh(
                        @RequestBody @Valid RefreshTokenRequest request) {
                LoginResponse loginResponse = authenticationService
                                .refreshToken(request);
                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .data(loginResponse)
                                .message("Refresh token success")
                                .build());
        }

        // 3. Logout
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authHeader) {
                String token = authHeader.replace("Bearer ", "");
                authenticationService.logout(token);
                return ResponseEntity.ok(ApiResponse.success("Logout success", null));

        }

        // 4. Register
        @PostMapping("/register")
        public ResponseEntity<ApiResponse> register(@RequestBody @Valid RegisterRequest request) {

                LoginResponse loginResponse = userService.createUser(request);
                return ResponseEntity.ok(ApiResponse.success("User registered successfully", loginResponse));

        }

        // 5. Send OTP
        @PostMapping("/send-otp")
        public ResponseEntity<ApiResponse> sendOtp(@RequestBody @Valid SendOtpRequest request) {
                authenticationService.sendOtp(request);
                return ResponseEntity.ok(ApiResponse.success("Send OTP successfully", null));
        }

        // 6. Reset Password
        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
                userService.resetPassword(request);
                return ResponseEntity.ok(ApiResponse.success("Reset password successfully", null));
        }

}
