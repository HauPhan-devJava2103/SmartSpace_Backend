package com.vn.smart_space.service.user;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.smart_space.consts.ERegistrationStatus;
import com.vn.smart_space.consts.ERole;
import com.vn.smart_space.consts.EUserStatus;
import com.vn.smart_space.dto.TokenPayload;
import com.vn.smart_space.dto.request.auth.DevCreateAccountRequest;
import com.vn.smart_space.dto.request.auth.RegisterRequest;
import com.vn.smart_space.dto.request.auth.ResetPasswordRequest;
import com.vn.smart_space.dto.request.user.ChangePasswordRequest;
import com.vn.smart_space.dto.request.user.UpdateProfileRequest;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.dto.response.user.UserResponse;
import com.vn.smart_space.exception.BadRequestException;
import com.vn.smart_space.mapper.UserMapper;
import com.vn.smart_space.model.User;
import com.vn.smart_space.repository.UserRepository;
import com.vn.smart_space.service.auth.IAuthenticationService;
import com.vn.smart_space.service.jwt.IJwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IAuthenticationService authenticationService;
    private final IJwtService jwtService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final StringRedisTemplate stringRedisTemplate;

    private final UserMapper userMapper;

    // Create New User
    @Override
    @Transactional
    public LoginResponse createUser(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        // Check OTP is verified
        String verifiedKey = "otp_verified:register:" + request.getEmail();
        String verified = stringRedisTemplate.opsForValue().get(verifiedKey);
        if (!"true".equals(verified)) {
            throw new BadRequestException("Email chưa được xác thực OTP");
        }
        stringRedisTemplate.delete(verifiedKey);

        // Create Default Avatar
        String emailPrefix = request.getEmail().split("@")[0];

        String nameAvatar = emailPrefix.length() >= 2
                ? emailPrefix.substring(0, 2).toUpperCase()
                : emailPrefix.toUpperCase();
        String defaultAvatar = "https://ui-avatars.com/api/?name=" + nameAvatar
                + "&background=6366f1&color=fff&size=200&bold=true&font-size=0.4";

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(ERole.client)
                .status(EUserStatus.active)
                .fullName(emailPrefix)
                .avatarUrl(defaultAvatar)
                .build();

        userRepository.save(user);

        // Login
        TokenPayload accessToken = jwtService.generateAccessToken(user);

        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
        TokenPayload refreshToken = jwtService.generateRefreshToken(user, rememberMe);

        authenticationService.saveRefreshTokenToRedis(user.getId(), refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
                .registrationStatus(ERegistrationStatus.incomplete)
                .user(userMapper.toUserResponse(user))
                .build();

    }

    @Override
    public User findUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new BadRequestException("User not found"));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        User user = findUserByEmail(request.getEmail());

        String otpKey = "otp:forgot_password:" + request.getEmail();
        authenticationService.verifyOtpForgotPassword(otpKey, request.getOtp());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }

    // Update Profile
    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findUserByEmail(email);

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    // Dev API: Create user directly
    @Override
    @Transactional
    public void devCreateAccount(DevCreateAccountRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        String emailPrefix = request.getEmail().split("@")[0];

        String nameAvatar = emailPrefix.length() >= 2
                ? emailPrefix.substring(0, 2).toUpperCase()
                : emailPrefix.toUpperCase();
        String defaultAvatar = "https://ui-avatars.com/api/?name=" + nameAvatar
                + "&background=6366f1&color=fff&size=200&bold=true&font-size=0.4";

        ERole role;
        try {
            role = ERole.valueOf(request.getRole().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(EUserStatus.active)
                .fullName(emailPrefix)
                .avatarUrl(defaultAvatar)
                .build();

        userRepository.save(user);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {

        User user = findUserByEmail(email);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không chính xác");
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

    }

}
