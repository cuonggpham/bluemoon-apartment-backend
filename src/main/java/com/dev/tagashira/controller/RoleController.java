package com.dev.tagashira.controller;

import com.dev.tagashira.dto.RoleDTO;
import com.dev.tagashira.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "APIs for managing user roles")
public class RoleController {
    
    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieve all roles in the system")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("GET /api/v1/roles - Fetching all roles");
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role by its ID")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        log.info("GET /api/v1/roles/{} - Fetching role by ID", id);
        RoleDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get role by name", description = "Retrieve a specific role by its name")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<RoleDTO> getRoleByName(@PathVariable String name) {
        log.info("GET /api/v1/roles/name/{} - Fetching role by name", name);
        RoleDTO role = roleService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    @PostMapping
    @Operation(summary = "Create new role", description = "Create a new role in the system")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("POST /api/v1/roles - Creating new role: {}", roleDTO.getName());
        RoleDTO createdRole = roleService.createRole(roleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Update an existing role")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO roleDTO) {
        log.info("PUT /api/v1/roles/{} - Updating role", id);
        RoleDTO updatedRole = roleService.updateRole(id, roleDTO);
        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Delete a role from the system")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("DELETE /api/v1/roles/{} - Deleting role", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
} 