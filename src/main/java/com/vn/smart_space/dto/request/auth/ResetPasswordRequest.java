package com.vn.smart_space.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.smart_space.validation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "OTP không được để trống")
    @Size(min = 6, max = 6, message = "OTP phải có 6 chữ số")
    private String otp;

    @JsonProperty("new_password")
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    @StrongPassword
    private String newPassword;

    @JsonProperty("confirm_password")
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
