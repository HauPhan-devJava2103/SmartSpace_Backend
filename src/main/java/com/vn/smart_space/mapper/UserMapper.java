package com.vn.smart_space.mapper;

import org.mapstruct.Mapper;

import com.vn.smart_space.dto.response.user.UserResponse;
import com.vn.smart_space.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);
}
