package com.dev.tagashira.service;

import com.dev.tagashira.constant.RoleConstant;
import com.dev.tagashira.dto.RoleDTO;
import com.dev.tagashira.entity.Role;
import com.dev.tagashira.exception.ResourceNotFoundException;
import com.dev.tagashira.mapper.RoleMapper;
import com.dev.tagashira.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public List<RoleDTO> getAllRoles() {
        log.info("Fetching all roles");
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toDTO(roles);
    }

    public RoleDTO getRoleById(Long id) {
        log.info("Fetching role with id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return roleMapper.toDTO(role);
    }

    public RoleDTO getRoleByName(String name) {
        log.info("Fetching role with name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return roleMapper.toDTO(role);
    }

    public RoleDTO createRole(RoleDTO roleDTO) {
        log.info("Creating new role: {}", roleDTO.getName());
        
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role with name " + roleDTO.getName() + " already exists");
        }
        
        Role role = roleMapper.toEntity(roleDTO);
        Role savedRole = roleRepository.save(role);
        log.info("Role created successfully with id: {}", savedRole.getId());
        
        return roleMapper.toDTO(savedRole);
    }

    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        log.info("Updating role with id: {}", id);
        
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!existingRole.getName().equals(roleDTO.getName()) && 
            roleRepository.existsByName(roleDTO.getName())) {
            throw new IllegalArgumentException("Role with name " + roleDTO.getName() + " already exists");
        }
        
        existingRole.setName(roleDTO.getName());
        existingRole.setDescription(roleDTO.getDescription());
        existingRole.setIsActive(roleDTO.getIsActive());
        
        Role updatedRole = roleRepository.save(existingRole);
        log.info("Role updated successfully with id: {}", updatedRole.getId());
        
        return roleMapper.toDTO(updatedRole);
    }

    public void deleteRole(Long id) {
        log.info("Deleting role with id: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        
        roleRepository.delete(role);
        log.info("Role deleted successfully with id: {}", id);
    }

    /**
     * Initialize default roles if they don't exist
     */
    public void initializeDefaultRoles() {
        log.info("Initializing default roles");
        
        for (RoleConstant.RoleName roleName : RoleConstant.RoleName.values()) {
            Optional<Role> existingRole = roleRepository.findByName(roleName.getName());
            
            if (existingRole.isEmpty()) {
                Role role = Role.builder()
                        .name(roleName.getName())
                        .description(roleName.getDescription())
                        .isActive(1)
                        .build();
                
                roleRepository.save(role);
                log.info("Created default role: {}", roleName.getName());
            }
        }
    }
} 