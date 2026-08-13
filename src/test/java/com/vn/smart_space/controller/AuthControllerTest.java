package com.vn.smart_space.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vn.smart_space.dto.request.auth.LoginRequest;
import com.vn.smart_space.dto.request.auth.RefreshTokenRequest;
import com.vn.smart_space.dto.request.auth.RegisterRequest;
import com.vn.smart_space.dto.request.auth.ResetPasswordRequest;
import com.vn.smart_space.dto.request.auth.SendOtpRequest;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.exception.BadRequestException;
import com.vn.smart_space.exception.GlobalExceptionHandler;
import com.vn.smart_space.exception.UnauthorizedException;
import com.vn.smart_space.service.auth.IAuthenticationService;
import com.vn.smart_space.service.user.IUserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private IAuthenticationService authenticationService;

    @Mock
    private IUserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authenticationService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_withValidRequest_returnsTokens() throws Exception {
        when(authenticationService.loginBasic(any(LoginRequest.class)))
                .thenReturn(tokens("access-token", "refresh-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "Secret@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login success"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(authenticationService).loginBasic(any(LoginRequest.class));
    }

    @Test
    void login_withWrongCredentials_returnsBadRequest() throws Exception {
        when(authenticationService.loginBasic(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Email hoặc mật khẩu không chính xác"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "Wrong@123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Email hoặc mật khẩu không chính xác"));
    }

    @Test
    void login_withInvalidEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invalid-email",
                                  "password": "Secret@123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).loginBasic(any(LoginRequest.class));
    }

    @Test
    void login_withShortPassword_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).loginBasic(any(LoginRequest.class));
    }

    @Test
    void refresh_withValidToken_returnsRotatedTokens() throws Exception {
        when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                .thenReturn(tokens("new-access-token", "new-refresh-token"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "valid-refresh-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Refresh token success"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));

        verify(authenticationService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void refresh_withRevokedToken_returnsUnauthorized() throws Exception {
        when(authenticationService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new UnauthorizedException("Refresh token đã bị thu hồi"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": "revoked-refresh-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh token đã bị thu hồi"));
    }

    @Test
    void refresh_withBlankToken_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token": ""}
                                """))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void logout_withBearerToken_passesRawTokenToService() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout success"));

        verify(authenticationService).logout("access-token");
    }

    @Test
    void logout_withoutAuthorizationHeader_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).logout(any(String.class));
    }

    @Test
    void register_withValidRequest_returnsTokens() throws Exception {
        when(userService.createUser(any(RegisterRequest.class)))
                .thenReturn(tokens("access-token", "refresh-token"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.user@example.com",
                                  "otp": "123456",
                                  "otpPurpose": "REGISTER",
                                  "password": "Secret@123",
                                  "confirmPassword": "Secret@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(userService).createUser(any(RegisterRequest.class));
    }

    @Test
    void register_withWeakPassword_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.user@example.com",
                                  "otp": "123456",
                                  "otpPurpose": "REGISTER",
                                  "password": "123456",
                                  "confirmPassword": "123456"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(RegisterRequest.class));
    }

    @Test
    void sendOtp_withRegisterPurpose_returnsOk() throws Exception {
        mockMvc.perform(post("/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new.user@example.com",
                                  "purpose": "REGISTER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Send OTP successfully"));

        verify(authenticationService).sendOtp(any(SendOtpRequest.class));
    }

    @Test
    void sendOtp_withInvalidEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "purpose": "REGISTER"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).sendOtp(any(SendOtpRequest.class));
    }

    @Test
    void sendOtp_withoutPurpose_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "new.user@example.com"}
                                """))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).sendOtp(any(SendOtpRequest.class));
    }

    @Test
    void resetPassword_withValidRequest_returnsOk() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "otp": "123456",
                                  "otpPurpose": "FORGOT_PASSWORD",
                                  "newPassword": "NewSecret@123",
                                  "confirmPassword": "NewSecret@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Reset password successfully"));

        verify(userService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void resetPassword_withInvalidOtpLength_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "otp": "123",
                                  "otpPurpose": "FORGOT_PASSWORD",
                                  "newPassword": "NewSecret@123",
                                  "confirmPassword": "NewSecret@123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).resetPassword(any(ResetPasswordRequest.class));
    }

    private LoginResponse tokens(String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
