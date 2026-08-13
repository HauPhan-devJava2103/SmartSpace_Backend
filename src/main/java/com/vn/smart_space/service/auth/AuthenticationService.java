package com.vn.smart_space.service.auth;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jwt.SignedJWT;
import com.vn.smart_space.consts.EOtpPurpose;
import com.vn.smart_space.dto.JwtInfo;
import com.vn.smart_space.dto.TokenPayload;
import com.vn.smart_space.dto.request.auth.IntrospectRequest;
import com.vn.smart_space.dto.request.auth.LoginRequest;
import com.vn.smart_space.dto.request.auth.RefreshTokenRequest;
import com.vn.smart_space.dto.request.auth.SendOtpRequest;
import com.vn.smart_space.dto.response.IntrospectResponse;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.exception.BadRequestException;
import com.vn.smart_space.exception.UnauthorizedException;
import com.vn.smart_space.model.InvalidatedToken;
import com.vn.smart_space.model.User;
import com.vn.smart_space.repository.InvalidatedTokenRepository;
import com.vn.smart_space.repository.UserRepository;
import com.vn.smart_space.service.jwt.IJwtService;
import com.vn.smart_space.service.mail.IMailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    private final IJwtService jwtService;
    private final IMailService mailService;

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
    @Transactional
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

    // Refresh Token
    @Override
    @Transactional
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

    // Send OTP
    @Override
    @Transactional
    public void sendOtp(SendOtpRequest request) {
        String email = request.getEmail();
        EOtpPurpose purpose = request.getPurpose();

        if (purpose == EOtpPurpose.FORGOT_PASSWORD) {
            userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadRequestException("Email không tồn tại trong hệ thống"));
        }
        if (purpose == EOtpPurpose.REGISTER && userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Email đã tồn tại trong hệ thống");
        }

        // Rate Limit
        String cooldownKey = "cooldown:otp:" + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BadRequestException("Vui lòng đợi 60 giây trước khi gửi lại OTP");
        }

        // Generate OTP
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        // Save OTP to Redis Hash
        String otpKey = "otp:" + email;
        Map<String, String> otpData = Map.of(
                "otp", otp,
                "attempts", "0",
                "purpose", purpose.name());
        stringRedisTemplate.opsForHash().putAll(otpKey, otpData);
        stringRedisTemplate.expire(otpKey, Duration.ofMinutes(5));

        // Set cooldown 60s
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(60));

        // Send OTP email
        mailService.sendOtpEmail(email, otp);
    }

    @Override
    public void saveRefreshTokenToRedis(
            String userId, TokenPayload refreshToken) {
        long ttlSeconds = (refreshToken.getExpiryTime().getTime()
                - System.currentTimeMillis()) / 1000;
        stringRedisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken.getToken(),
                Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void verifyOtp(String email, String inputOtp, EOtpPurpose expectedPurpose, EOtpPurpose actualPurpose) {
        if (actualPurpose != expectedPurpose) {
            throw new BadRequestException("Mục đích sử dụng OTP không hợp lệ");
        }
        String otpKey = "otp:" + email;
        // Check OTP is exists
        Boolean exists = stringRedisTemplate.hasKey(otpKey);

        if (!Boolean.TRUE.equals(exists)) {
            throw new BadRequestException(
                    "OTP không tồn tại hoặc đã hết hạn");
        }

        // Get Data OTP Redis
        Map<Object, Object> otpData = stringRedisTemplate.opsForHash().entries(otpKey);

        String savedOtp = (String) otpData.get("otp");
        String savedAttempts = (String) otpData.get("attempts");

        if (savedOtp == null || savedAttempts == null) {
            stringRedisTemplate.delete(otpKey);

            throw new BadRequestException(
                    "Dữ liệu OTP không hợp lệ");
        }

        int attempts = Integer.parseInt(savedAttempts);
        if (attempts >= 5) {
            stringRedisTemplate.delete(otpKey);

            throw new BadRequestException(
                    "Bạn đã nhập sai OTP quá 5 lần. "
                            + "Vui lòng lấy OTP mới");
        }

        // OTP Not Match
        if (!savedOtp.equals(inputOtp)) {

            Long newAttempts = stringRedisTemplate
                    .opsForHash()
                    .increment(
                            otpKey,
                            "attempts",
                            1);

            int attemptsAfter = newAttempts != null
                    ? newAttempts.intValue()
                    : attempts + 1;

            if (attemptsAfter >= 5) {
                stringRedisTemplate.delete(otpKey);

                throw new BadRequestException(
                        "Bạn đã nhập sai OTP quá 5 lần. "
                                + "Vui lòng lấy OTP mới");
            }

            int remaining = 5 - attemptsAfter;

            throw new BadRequestException(
                    "OTP không chính xác. Còn "
                            + remaining
                            + " lần thử");
        }

        // OTP Match
        stringRedisTemplate.delete(otpKey);
    }

}
