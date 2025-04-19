package com.dev.tagashira.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {
    @NotBlank(message = "username không được để trống")
    private String username;

    @NotBlank(message = "password không được để trống")
    private String password;
}
