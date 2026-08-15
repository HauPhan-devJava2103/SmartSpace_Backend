package com.vn.smart_space.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.smart_space.validation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Mật khẩu không được để trống")
    @StrongPassword
    String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    @JsonProperty("confirm_password")
    String confirmPassword;

    Boolean rememberMe;

}
