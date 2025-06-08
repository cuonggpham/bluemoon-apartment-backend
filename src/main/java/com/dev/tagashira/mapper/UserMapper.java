package com.dev.tagashira.mapper;

import com.dev.tagashira.dto.response.UserResponse;
import com.dev.tagashira.entity.User;

import java.util.List;

public interface UserMapper {
    UserResponse toUserResponse(User user);
    List<UserResponse> toUserResponseList(List<User> users);
}
