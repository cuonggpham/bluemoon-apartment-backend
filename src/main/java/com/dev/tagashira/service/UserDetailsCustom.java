package com.dev.tagashira.service;

import com.dev.tagashira.exception.UserInfoException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component("userDetailsService")
public class UserDetailsCustom implements UserDetailsService {

    private final UserService userService;

    public UserDetailsCustom(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.dev.tagashira.entity.User user = this.userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Username/password is not valid");
        }
        if (user.getIsActive() == 0) {
            try {
                throw new UserInfoException("User is not active");
            } catch (UserInfoException e) {
                throw new RuntimeException(e);
            }
        }
        
        // Convert roles to authorities
        var authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
        
        return new User(
                user.getEmail(),
                user.getPassword(),
                authorities);
    }
}

