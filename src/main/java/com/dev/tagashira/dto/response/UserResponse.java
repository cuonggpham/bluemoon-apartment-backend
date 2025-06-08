package com.dev.tagashira.dto.response;

import com.dev.tagashira.dto.RoleDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
public class UserResponse {
    long id;
    String name;
    String email;
    String authType;
    int isActive;
    List<RoleDTO> roles;
}
