package com.dev.tagashira.mapper;

import com.dev.tagashira.dto.response.UserResponse;
import org.mapstruct.Mapper;
import com.dev.tagashira.entity.User;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "email", target = "email")
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);
}
