package com.dev.tagashira.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    Long id;
    String name;
    String description;
    Integer isActive;
} 