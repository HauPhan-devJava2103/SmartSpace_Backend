package com.vn.smart_space.service.auth;

import com.vn.smart_space.dto.request.auth.IntrospectRequest;
import com.vn.smart_space.dto.request.auth.LoginRequest;
import com.vn.smart_space.dto.request.auth.RefreshTokenRequest;
import com.vn.smart_space.dto.response.IntrospectResponse;
import com.vn.smart_space.dto.response.auth.LoginResponse;

public interface IAuthenticationService {

    // Introspect Token
    IntrospectResponse introspect(IntrospectRequest request);

    // Login Basic
    LoginResponse loginBasic(LoginRequest request);

    // Refresh Token
    LoginResponse refreshToken(RefreshTokenRequest request);

    // Logout
    void logout(String token);

}
