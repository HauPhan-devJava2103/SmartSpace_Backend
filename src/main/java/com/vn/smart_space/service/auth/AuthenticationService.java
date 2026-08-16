package com.vn.smart_space.service.auth;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jwt.SignedJWT;
import com.vn.smart_space.consts.ERegistrationStatus;
import com.vn.smart_space.consts.ERole;
import com.vn.smart_space.consts.EUserStatus;
import com.vn.smart_space.dto.JwtInfo;
import com.vn.smart_space.dto.TokenPayload;
import com.vn.smart_space.dto.request.auth.GoogleLoginRequest;
import com.vn.smart_space.dto.request.auth.IntrospectRequest;
import com.vn.smart_space.dto.request.auth.LoginRequest;
import com.vn.smart_space.dto.request.auth.RefreshTokenRequest;
import com.vn.smart_space.dto.response.IntrospectResponse;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.exception.BadRequestException;
import com.vn.smart_space.exception.UnauthorizedException;
import com.vn.smart_space.mapper.UserMapper;
import com.vn.smart_space.model.InvalidatedToken;
import com.vn.smart_space.model.User;
import com.vn.smart_space.repository.InvalidatedTokenRepository;
import com.vn.smart_space.repository.UserRepository;
import com.vn.smart_space.service.jwt.IJwtService;
import com.vn.smart_space.service.mail.IMailService;
import com.vn.smart_space.utils.OtpGenerator;

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

    private final UserMapper userMapper;

    @Value("${google.client-id}")
    private String googleClientId;

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

        // Generate Access Token
        TokenPayload accessToken = jwtService.generateAccessToken(user);

        // Generate Refresh Token
        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
        TokenPayload refreshToken = jwtService.generateRefreshToken(user, rememberMe);

        saveRefreshTokenToRedis(user.getId(), refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .registrationStatus(determineRegistrationStatus(user))
                .user(userMapper.toUserResponse(user))
                .build();
    }

    // Login Google
    @Override
    @Transactional
    public LoginResponse loginGoogle(GoogleLoginRequest request) {
        // Verify Google ID Token
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(request.getIdToken());
        } catch (GeneralSecurityException | IOException e) {
            throw new BadRequestException("Google token không hợp lệ");
        }

        if (idToken == null) {
            throw new BadRequestException("Google token không hợp lệ hoặc đã hết hạn");
        }

        // Extract info user
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String fullName = (String) payload.get("name");

        // Find or create User
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullName(fullName != null ? fullName : email.split("@")[0])
                            .role(ERole.client)
                            .status(EUserStatus.active)
                            .build();
                    return userRepository.save(newUser);
                });

        // Generate JWT tokens
        TokenPayload accessToken = jwtService.generateAccessToken(user);
        TokenPayload refreshToken = jwtService.generateRefreshToken(user, true);
        saveRefreshTokenToRedis(user.getId(), refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .registrationStatus(determineRegistrationStatus(user))
                .user(userMapper.toUserResponse(user))
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
        String refreshToken = request.getRefreshToken();

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

            Boolean rememberMe = (Boolean) signedJWT.getJWTClaimsSet().getClaim("rememberMe");
            boolean isRememberMe = Boolean.TRUE.equals(rememberMe);
            TokenPayload newRefreshToken = jwtService
                    .generateRefreshToken(user, isRememberMe);

            saveRefreshTokenToRedis(user.getId(), newRefreshToken);
            return LoginResponse.builder()
                    .accessToken(newAccessToken.getToken())
                    .refreshToken(newRefreshToken.getToken())
                    .registrationStatus(determineRegistrationStatus(user))
                    .user(userMapper.toUserResponse(user))
                    .build();
        } catch (ParseException e) {
            throw new UnauthorizedException("Token không hợp lệ");
        }
    }

    // SEND OTP FOR REGISTRATION
    @Override
    @Transactional
    public void sendOtpRegister(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Email đã tồn tại trong hệ thống");
        }
        sendOtp(email, "otp:register:", "cooldown:otp:");
    }

    // Send OTP Forgot password
    @Override
    public void sendOtpForgotPassword(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new BadRequestException("Email không tồn tại trong hệ thống");
        }
        sendOtp(email, "otp:forgot_password:", "cooldown:otp_forgot:");
    }

    @Override
    @Transactional
    public void verifyOtpRegister(String email, String otp) {

        String otpKey = "otp:register:" + email;
        verifyOtp(otpKey, otp);

        String verifiedKey = "otp_verified:register:" + email;
        stringRedisTemplate.opsForValue().set(verifiedKey, "true", Duration.ofMinutes(10));

    }

    @Override
    public void verifyOtpForgotPassword(String otpKey, String otp) {
        verifyOtp(otpKey, otp);
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
    public ERegistrationStatus determineRegistrationStatus(User user) {
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            return ERegistrationStatus.complete;
        }
        return ERegistrationStatus.incomplete;
    }

    private void sendOtp(String email, String otpKeyPrefix, String cooldownKeyPrefix) {
        String cooldownKey = cooldownKeyPrefix + email;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cooldownKey))) {
            throw new BadRequestException("Vui lòng đợi 60 giây trước khi gửi lại OTP");
        }

        String otp = OtpGenerator.generateOtp();
        // Save OTP to Redis
        String otpKey = otpKeyPrefix + email;
        Map<String, String> otpData = Map.of(
                "otp", otp,
                "attempts", "0");
        stringRedisTemplate.opsForHash().putAll(otpKey, otpData);
        stringRedisTemplate.expire(otpKey, Duration.ofMinutes(5));
        // Set cooldown 60s
        stringRedisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(60));
        // Send OTP email
        mailService.sendOtpEmail(email, otp);
    }

    private void verifyOtp(String otpKey, String otp) {
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
        if (!savedOtp.equals(otp)) {

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
