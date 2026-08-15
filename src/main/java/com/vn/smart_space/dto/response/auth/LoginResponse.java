package com.vn.smart_space.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.smart_space.consts.ERegistrationStatus;
import com.vn.smart_space.dto.response.user.UserResponse;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("refresh_token")
    String refreshToken;

    @JsonProperty("registration_status")
    ERegistrationStatus registrationStatus;

    UserResponse user;

}
