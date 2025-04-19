package com.dev.tagashira.controller;

import com.dev.tagashira.dto.request.UserCreateRequest;
import com.dev.tagashira.dto.response.UserResponse;
import com.dev.tagashira.exception.UserInfoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.dev.tagashira.entity.User;
import com.dev.tagashira.service.UserService;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "User Controller")
public class UserController {
    private final UserService userService;

    //fetch all users
    @Operation(summary = "Get list of users", description = "abcabc")
    @GetMapping()
    public ResponseEntity<List<UserResponse>> getAllUser() {
        List<UserResponse> userResponses = this.userService.fetchAllUserResponse();
        return ResponseEntity.status(HttpStatus.OK).body(userResponses);
    }

    //fetch user by id
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") long id) throws Exception {
        User fetchUser = this.userService.fetchUserById(id);
        UserResponse userResponse = this.userService.UserToUserResponse(fetchUser);
        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }

    //Create new user
    @PostMapping("/register")
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) throws Exception {
        User user = new User();
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    //Delete user by id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") long id) throws Exception {
        this.userService.deleteUser(id);
        return ResponseEntity.ok("deleted successfully");
    }

    //Update user
    @PutMapping()
    public ResponseEntity<User> updateUser(@RequestBody User user) throws Exception {
        User apiUser = this.userService.updateUser(user);
        return ResponseEntity.ok(apiUser);
    }

}