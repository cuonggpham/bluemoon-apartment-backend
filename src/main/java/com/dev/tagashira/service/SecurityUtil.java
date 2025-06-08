package com.dev.tagashira.service;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.dev.tagashira.dto.request.UserLoginDTO;
import com.dev.tagashira.dto.response.ResLoginDTO;
import com.dev.tagashira.entity.User;
import com.dev.tagashira.exception.UserInfoException;
import com.dev.tagashira.repository.UserRepository;
import com.nimbusds.jose.util.Base64;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


@Service
@Slf4j
public class SecurityUtil {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public SecurityUtil(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, UserService userService, UserRepository userRepository, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    //Environment variable defined in application.yaml
    @Value("${authentication.jwt.base64-secret}")
    private String jwtKey;

    @Value("${authentication.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;

    @Value("${authentication.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    //Create new token
    public String createAccessToken(String email, ResLoginDTO.UserLogin dto, Collection<? extends GrantedAuthority> authorities) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        // Extract roles from authorities
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        log.info("Creating access token for user: {}", email);
        log.info("User authorities: {}", authorities);
        log.info("Extracted roles for JWT: {}", roles);

        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", dto)
                .claim("roles", roles)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }

    //Create new token
    public String createRefreshToken(String email, ResLoginDTO dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .claim("user", dto.getUser())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }

    //Create cookies bearing refresh token
    public ResponseCookie createCookie(String refresh_token) {
        // set cookies

        return ResponseCookie
                .from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length,
                JWT_ALGORITHM.getName());
    }

    // refresh token
    public ResLoginDTO getRefreshedUser(String refresh_token) throws Exception{
        if (refresh_token.equals("abc")) {
            throw new UserInfoException("There is no refresh token on cookie");
        }
        // check valid
        Jwt decodedToken;
        try {
            decodedToken = jwtDecoder.decode(refresh_token);
        } catch (JwtException ex) {
            // Check if the exception is due to token expiration
            if (ex instanceof JwtValidationException && ex.getMessage().contains("token_expired")) {
                throw new UserInfoException("Your session has expired");
            }
            throw new UserInfoException("Refresh token is not valid");
        }
        String email = decodedToken.getSubject();
        // check user by token + email
        User currentUser = this.userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new UserInfoException("Refresh token is not valid");
        }
        // issue new token/set refresh token as cookies
        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.getUserByUsername(email);
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName());
            res.setUser(userLogin);
        }
        
        // Load user authorities from roles
        Collection<GrantedAuthority> authorities = currentUserDB.getRoles().stream()
                .map(role -> (GrantedAuthority) () -> role.getName())
                .collect(Collectors.toList());
        
        // create access token
        String access_token = this.createAccessToken(email, res.getUser(), authorities);
        res.setAccessToken(access_token);
        // create refresh token
        String new_refresh_token = this.createRefreshToken(email, res);
        // update user
        this.userService.updateUserToken(new_refresh_token, email);
        return res;
    }

    //Get the login of the current user.
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }
    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }
    // Get the JWT of the current user.
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
                .filter(authentication -> authentication.getCredentials() instanceof String)
                .map(authentication -> (String) authentication.getCredentials());
    }

}

