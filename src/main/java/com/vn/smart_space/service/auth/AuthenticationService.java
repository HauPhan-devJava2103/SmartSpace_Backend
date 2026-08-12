package com.vn.smart_space.service.auth;

import java.text.ParseException;
import java.time.Duration;
import java.util.Date;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jwt.SignedJWT;
import com.vn.smart_space.dto.JwtInfo;
import com.vn.smart_space.dto.TokenPayload;
import com.vn.smart_space.dto.request.auth.IntrospectRequest;
import com.vn.smart_space.dto.request.auth.LoginRequest;
import com.vn.smart_space.dto.request.auth.RefreshTokenRequest;
import com.vn.smart_space.dto.response.IntrospectResponse;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.exception.BadRequestException;
import com.vn.smart_space.exception.UnauthorizedException;
import com.vn.smart_space.model.InvalidatedToken;
import com.vn.smart_space.model.User;
import com.vn.smart_space.repository.InvalidatedTokenRepository;
import com.vn.smart_space.repository.UserRepository;
import com.vn.smart_space.service.jwt.IJwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    private final IJwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private final StringRedisTemplate stringRedisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

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
        TokenPayload accessToken = jwtService.generateAccessToken(user);
        TokenPayload refreshToken = jwtService.generateRefreshToken(user);

        saveRefreshTokenToRedis(user.getId(), refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
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

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String email = signedJWT.getJWTClaimsSet().getSubject();
            userRepository.findByEmail(email).ifPresent(user -> stringRedisTemplate.delete(
                    REFRESH_TOKEN_PREFIX + user.getId()));
        } catch (ParseException e) {
            // Access token đã blacklist
        }
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getToken();

        SignedJWT signedJWT = jwtService.verifyToken(refreshToken);
        try {
            // Check refresh Token
            String tokenType = (String) signedJWT.getJWTClaimsSet()
                    .getClaim("tokenType");

            if (!"refresh".equals(tokenType)) {
                throw new UnauthorizedException("Token không hợp lệ");
            }

            String email = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException(
                            "User không tồn tại"));

            String redisKey = REFRESH_TOKEN_PREFIX + user.getId();
            String storedToken = stringRedisTemplate.opsForValue()
                    .get(redisKey);
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                throw new UnauthorizedException(
                        "Refresh token đã bị thu hồi");
            }
            TokenPayload newAccessToken = jwtService
                    .generateAccessToken(user);
            TokenPayload newRefreshToken = jwtService
                    .generateRefreshToken(user);

            saveRefreshTokenToRedis(user.getId(), newRefreshToken);
            return LoginResponse.builder()
                    .accessToken(newAccessToken.getToken())
                    .refreshToken(newRefreshToken.getToken())
                    .build();
        } catch (ParseException e) {
            throw new UnauthorizedException("Token không hợp lệ");
        }
    }

    // Helpers
    private void saveRefreshTokenToRedis(
            String userId, TokenPayload refreshToken) {
        long ttlSeconds = (refreshToken.getExpiryTime().getTime()
                - System.currentTimeMillis()) / 1000;
        stringRedisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken.getToken(),
                Duration.ofSeconds(ttlSeconds));
    }

}
