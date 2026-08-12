package com.vn.smart_space.service.jwt;

import com.nimbusds.jwt.SignedJWT;
import com.vn.smart_space.dto.JwtInfo;
import com.vn.smart_space.model.User;

public interface IJwtService {
    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    SignedJWT verifyToken(String token);

    String buildScope(User user);

    JwtInfo parseToken(String token);

}
