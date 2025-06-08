package com.dev.tagashira.controller;

import com.dev.tagashira.dto.RoleDTO;
import com.dev.tagashira.dto.request.UserCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.UserResponse;
import com.dev.tagashira.exception.UserInfoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.dev.tagashira.entity.User;
import com.dev.tagashira.service.UserService;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
@Tag(name = "User Management", description = "APIs for managing users and their roles")
@Slf4j
public class UserController {
    private final UserService userService;

    //fetch all users
    @Operation(summary = "Get list of users", description = "Retrieve all users in the system")
    @GetMapping()
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> getAllUser() {
        log.info("GET /api/users - Fetching all users");
        List<UserResponse> userResponses = this.userService.fetchAllUserResponse();
        return ResponseEntity.status(HttpStatus.OK).body(userResponses);
    }

    //fetch user by id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or @securityService.canAccessUser(#id, authentication.name)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") long id) throws UserInfoException {
        log.info("GET /api/users/{} - Fetching user by ID", id);
        User fetchUser = this.userService.fetchUserById(id);
        UserResponse userResponse = this.userService.UserToUserResponse(fetchUser);
        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }

    //Create new user
    @PostMapping()
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<User> createNewUser(@Valid @RequestBody UserCreateRequest apiUser) throws UserInfoException {
        log.info("POST /api/users - Creating new user: {}", apiUser.getUsername());
        User user = this.userService.createUser(apiUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    //Delete user by id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable("id") long id) throws UserInfoException {
        log.info("DELETE /api/users/{} - Deleting user", id);
        ApiResponse<String> response = this.userService.deleteUser(id);
        return ResponseEntity.ok(response);
    }

    //Update user
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or @securityService.canAccessUser(#id, authentication.name)")
    public ResponseEntity<User> updateUser(@PathVariable("id") long id, @RequestBody User user) throws UserInfoException {
        log.info("PUT /api/users/{} - Updating user", id);
        user.setId(id); // Ensure the ID matches the path parameter
        User apiUser = this.userService.updateUser(user);
        return ResponseEntity.ok(apiUser);
    }

    // Role management endpoints

    @GetMapping("/{id}/roles")
    @Operation(summary = "Get user roles", description = "Retrieve all roles assigned to a user")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<RoleDTO>> getUserRoles(@PathVariable("id") Long id) {
        log.info("GET /api/users/{}/roles - Fetching user roles", id);
        List<RoleDTO> roles = userService.getUserRoles(id);
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Assign roles to user", description = "Assign multiple roles to a user")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> assignRolesToUser(@PathVariable("id") Long id, @RequestBody List<Long> roleIds) {
        log.info("PUT /api/users/{}/roles - Assigning roles {} to user", id, roleIds);
        userService.assignRolesToUser(id, roleIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Add role to user", description = "Add a single role to a user")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> addRoleToUser(@PathVariable("id") Long id, @PathVariable("roleId") Long roleId) {
        log.info("POST /api/users/{}/roles/{} - Adding role to user", id, roleId);
        userService.addRoleToUser(id, roleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @Operation(summary = "Remove role from user", description = "Remove a role from a user")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable("id") Long id, @PathVariable("roleId") Long roleId) {
        log.info("DELETE /api/users/{}/roles/{} - Removing role from user", id, roleId);
        userService.removeRoleFromUser(id, roleId);
        return ResponseEntity.noContent().build();
    }

    // Keep the old register endpoint for backward compatibility
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserCreateRequest apiUser) throws UserInfoException {
        log.info("POST /api/users/register - Registering new user: {}", apiUser.getUsername());
        User user = this.userService.createUser(apiUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
