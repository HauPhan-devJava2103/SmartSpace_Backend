package com.vn.smart_space.service.user;

import com.vn.smart_space.dto.request.auth.RegisterRequest;
import com.vn.smart_space.dto.request.auth.ResetPasswordRequest;
import com.vn.smart_space.dto.response.auth.LoginResponse;
import com.vn.smart_space.model.User;

public interface IUserService {

    // Create New User
    LoginResponse createUser(RegisterRequest request);

    // Reset Password
    void resetPassword(ResetPasswordRequest request);

    // Find User By Id
    User findUserById(String id);

    // Find User By Email
    User findUserByEmail(String email);

}
