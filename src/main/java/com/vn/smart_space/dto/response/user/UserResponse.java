package com.vn.smart_space.dto.response.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vn.smart_space.consts.ERole;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserResponse {

    String id;

    String email;

    @JsonProperty("fullname")
    String fullName;

    @JsonProperty("avatar_url")
    String avatarUrl;

    ERole role;

}
