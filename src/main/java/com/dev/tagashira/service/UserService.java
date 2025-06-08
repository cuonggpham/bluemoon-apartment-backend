package com.dev.tagashira.service;


import com.dev.tagashira.constant.RoleConstant;
import com.dev.tagashira.dto.RoleDTO;
import com.dev.tagashira.dto.request.UserCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.UserResponse;
import com.dev.tagashira.entity.Role;
import com.dev.tagashira.entity.User;
import com.dev.tagashira.exception.ResourceNotFoundException;
import com.dev.tagashira.exception.UserInfoException;
import com.dev.tagashira.mapper.RoleMapper;
import com.dev.tagashira.mapper.UserMapper;
import com.dev.tagashira.repository.RoleRepository;
import com.dev.tagashira.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Slf4j
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private PasswordEncoder passwordEncoder;

    //Logic fetch all user
    public List<User> fetchAllUser() {
        return this.userRepository.findAll();
    }

    public List<UserResponse> fetchAllUserResponse() {
        List<User> users = this.userRepository.findAll();
        List<UserResponse> userResponses = this.userMapper.toUserResponseList(users);
        
        // Manually map roles for each user
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            UserResponse userResponse = userResponses.get(i);
            userResponse.setRoles(user.getRoles().stream()
                    .map(roleMapper::toDTO)
                    .collect(Collectors.toList()));
        }
        
        return userResponses;
    }

    //fetch user by id
    public User fetchUserById(long id) throws UserInfoException {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new UserInfoException("User with id " + id + " is not found"));
        if (user.getIsActive() == 0) {
            throw new UserInfoException("User with id " + id + " is not active");
        }
        return user;
    }

    //Logic get user by email
    public User getUserByUsername(String username) {
        return this.userRepository.findByEmail(username);
    }

    //Logic delete user by id
    public ApiResponse<String> deleteUser(long id) throws UserInfoException {
        User currentUser = this.fetchUserById(id);
        if(currentUser != null) {
            currentUser.setIsActive(0);
            this.userRepository.save(currentUser);
        }
        else throw new UserInfoException("User with id " + id + " is not found");
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("delete user success");
        response.setData(null);
        return response;
    }

    //Logic update user
    public User updateUser(User reqUser) throws UserInfoException {
        User currentUser = this.fetchUserById(reqUser.getId());
        if (currentUser != null) {
            currentUser.setEmail(reqUser.getEmail());
            currentUser.setName(reqUser.getName());
            String hashPassword = this.passwordEncoder.encode(reqUser.getPassword());
            currentUser.setPassword(hashPassword);
            // update
            currentUser = this.userRepository.save(currentUser);
        }
        else throw new UserInfoException("User with id " + reqUser.getId() + " is not found");
        return currentUser;
    }
    //Check existed email
    public boolean isEmailExist(String email) {
        return this.userRepository.findByEmail(email) != null;
    }
    //Logic create user
    public User createUser(UserCreateRequest userCreateRequest) throws UserInfoException {
        if (isEmailExist(userCreateRequest.getUsername())) {
            User existingUser = getUserByUsername(userCreateRequest.getUsername());
            // If user existed and isActive = 1, throw exception
            if (existingUser.getIsActive() == 1) {
                throw new UserInfoException("User with email " + userCreateRequest.getUsername() + " already exists");
            }
            // If user existed but isActive = 0, set isActive = 1 and return updated user
            existingUser.setIsActive(1);
            return this.userRepository.save(existingUser);
        }
        // If email is not found, create a new user
        User user = new User();
        user.setName(userCreateRequest.getName());
        String hashPassword = this.passwordEncoder.encode(userCreateRequest.getPassword());
        user.setPassword(hashPassword);
        user.setEmail(userCreateRequest.getUsername());
        user.setAuthType("normal");
        
        // Assign default role (ROLE_ACCOUNTANT) to new users
        Role defaultRole = roleRepository.findByName(RoleConstant.ROLE_ACCOUNTANT)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));
        user.getRoles().add(defaultRole);
        
        return this.userRepository.save(user);
    }
    public void updateUserToken(String token, String email) {
        User currentUser = this.getUserByUsername(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }
    public User getUserByRefreshTokenAndEmail(String token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(token, email);
    }
    public UserResponse UserToUserResponse(User user) {
        UserResponse userResponse = this.userMapper.toUserResponse(user);
        // Manually map roles
        userResponse.setRoles(user.getRoles().stream()
                .map(roleMapper::toDTO)
                .collect(Collectors.toList()));
        return userResponse;
    }

    /**
     * Get user roles
     */
    public List<RoleDTO> getUserRoles(Long userId) {
        log.info("Fetching roles for user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return user.getRoles().stream()
                .map(roleMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Assign roles to user
     */
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        log.info("Assigning roles {} to user id: {}", roleIds, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Set<Role> roles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId)))
                .collect(Collectors.toSet());
        
        user.setRoles(roles);
        userRepository.save(user);
        
        log.info("Successfully assigned {} roles to user id: {}", roles.size(), userId);
    }

    /**
     * Add role to user
     */
    public void addRoleToUser(Long userId, Long roleId) {
        log.info("Adding role {} to user id: {}", roleId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        
        user.getRoles().add(role);
        userRepository.save(user);
        
        log.info("Successfully added role {} to user id: {}", roleId, userId);
    }

    /**
     * Remove role from user
     */
    public void removeRoleFromUser(Long userId, Long roleId) {
        log.info("Removing role {} from user id: {}", roleId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        
        user.getRoles().remove(role);
        userRepository.save(user);
        
        log.info("Successfully removed role {} from user id: {}", roleId, userId);
    }
}
