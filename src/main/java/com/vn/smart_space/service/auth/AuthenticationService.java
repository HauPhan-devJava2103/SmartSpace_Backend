package com.vn.smart_space.service.auth;

import java.util.Date;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.vn.smart_space.dto.JwtInfo;
import com.vn.smart_space.dto.request.auth.IntrospectRequest;
import com.vn.smart_space.dto.request.auth.LoginRequest;
import com.vn.smart_space.dto.response.IntrospectResponse;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.exception.BadRequestException;
import com.vn.smart_space.model.InvalidatedToken;
import com.vn.smart_space.model.User;
import com.vn.smart_space.repository.InvalidatedTokenRepository;
import com.vn.smart_space.repository.UserRepository;
import com.vn.smart_space.service.jwt.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Introspect Token
    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;
        try {
            var signedJWT = jwtService.verifyToken(token);

            // Check blacklist
            String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
            if (invalidatedTokenRepository.existsById(jwtId)) {
                isValid = false;
            }
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();

    }

    // Login Basic
    @Override
    public LoginResponse loginBasic(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không chính xác"));

        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!isPasswordMatch) {
            throw new BadRequestException("Email hoặc mật khẩu không chính xác");
        }

        // Generate Token
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Logout
    @Override
    public void logout(String token) {
        JwtInfo jwtInfo = jwtService.parseToken(token);

        Date expiryTime = jwtInfo.getExpiryTime();
        if (expiryTime.before(new Date())) {
            return;
        }

        // Set TTL
        long ttlSeconds = (expiryTime.getTime() - System.currentTimeMillis()) / 1000;

        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .id(jwtInfo.getJwtId())
                .ttl(ttlSeconds)
                .build());
    }

}
