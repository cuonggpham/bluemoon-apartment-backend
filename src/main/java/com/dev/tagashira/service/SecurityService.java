package com.dev.tagashira.service;

import com.dev.tagashira.entity.User;
import com.dev.tagashira.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    
    private final UserRepository userRepository;

    /**
     * Check if current user can access user with given id
     * MANAGER can access all users
     * Other users can only access their own data
     */
    public boolean canAccessUser(Long userId, String currentUserEmail) {
        log.debug("Checking access for user {} to user id {}", currentUserEmail, userId);
        
        // Get current user
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null) {
            log.warn("Current user not found: {}", currentUserEmail);
            return false;
        }
        
        // Check if current user is MANAGER
        boolean isManager = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_MANAGER".equals(role.getName()));
        
        if (isManager) {
            log.debug("User {} is MANAGER, access granted", currentUserEmail);
            return true;
        }
        
        // Check if accessing own data
        boolean isOwnData = currentUser.getId() == userId;
        log.debug("User {} accessing own data: {}", currentUserEmail, isOwnData);
        
        return isOwnData;
    }

    /**
     * Check if current user can access apartment with given id
     * MANAGER can access all apartments
     * ACCOUNTANT can access all apartments for fee management
     */
    public boolean canAccessApartment(Long apartmentId, String currentUserEmail) {
        log.debug("Checking apartment access for user {} to apartment id {}", currentUserEmail, apartmentId);
        
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null) {
            log.warn("Current user not found: {}", currentUserEmail);
            return false;
        }
        
        // Check if current user has MANAGER or ACCOUNTANT role
        boolean hasAccess = currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_MANAGER".equals(role.getName()) || 
                                "ROLE_ACCOUNTANT".equals(role.getName()));
        
        log.debug("User {} has apartment access: {}", currentUserEmail, hasAccess);
        return hasAccess;
    }

    /**
     * Get current authenticated user email
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String roleName) {
        String currentUserEmail = getCurrentUserEmail();
        if (currentUserEmail == null) {
            return false;
        }
        
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null) {
            return false;
        }
        
        return currentUser.getRoles().stream()
                .anyMatch(role -> roleName.equals(role.getName()));
    }
} 