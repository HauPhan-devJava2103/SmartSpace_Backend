package com.vn.smart_space.dto.request.auth;

import com.vn.smart_space.consts.EOtpPurpose;
import com.vn.smart_space.validation.EnumValue;
import com.vn.smart_space.validation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;

    @NotBlank(message = "OTP không được để trống")
    @Size(min = 6, max = 6, message = "OTP phải có 6 chữ số")
    String otp;

    @NotNull(message = "Mục đích OTP không được để trống")
    @EnumValue(enumClass = EOtpPurpose.class)
    EOtpPurpose otpPurpose;

    @NotBlank(message = "Mật khẩu không được để trống")
    @StrongPassword
    String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    String confirmPassword;
}
