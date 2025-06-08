package com.dev.tagashira.mapper;

import com.dev.tagashira.dto.RoleDTO;
import com.dev.tagashira.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    
    RoleDTO toDTO(Role role);
    
    List<RoleDTO> toDTO(List<Role> roles);
    
    Role toEntity(RoleDTO roleDTO);
    
    List<Role> toEntity(List<RoleDTO> roleDTOs);
} 