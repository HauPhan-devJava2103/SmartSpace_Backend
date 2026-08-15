package com.vn.smart_space.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vn.smart_space.consts.EOtpPurpose;
import com.vn.smart_space.consts.ERole;
import com.vn.smart_space.consts.EUserStatus;
import com.vn.smart_space.dto.TokenPayload;
import com.vn.smart_space.dto.request.auth.RegisterRequest;
import com.vn.smart_space.dto.request.auth.ResetPasswordRequest;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.exception.BadRequestException;
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
        authenticationService.verifyOtp(request.getEmail(), request.getOtp(), EOtpPurpose.register,
                request.getOtpPurpose());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(ERole.client)
                .status(EUserStatus.active)
                .fullName(request.getEmail().split("@")[0])
                .build();

        userRepository.save(user);

        // Login
        TokenPayload accessToken = jwtService.generateAccessToken(user);
        TokenPayload refreshToken = jwtService.generateRefreshToken(user);

        authenticationService.saveRefreshTokenToRedis(user.getId(), refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken.getToken())
                .refreshToken(refreshToken.getToken())
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
        authenticationService.verifyOtp(request.getEmail(), request.getOtp(),
                EOtpPurpose.forgot_password, request.getOtpPurpose());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

    }

}
