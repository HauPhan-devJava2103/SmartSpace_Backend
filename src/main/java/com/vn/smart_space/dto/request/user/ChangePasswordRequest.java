package com.vn.smart_space.dto.request.user;

public record ChangePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword) {

}
