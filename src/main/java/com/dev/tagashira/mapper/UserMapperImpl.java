package com.dev.tagashira.mapper;

import com.dev.tagashira.dto.response.UserResponse;
import com.dev.tagashira.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setAuthType(user.getAuthType());
        userResponse.setIsActive(user.getIsActive());
        // Roles will be set manually in service
        
        return userResponse;
    }

    @Override
    public List<UserResponse> toUserResponseList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }
} 