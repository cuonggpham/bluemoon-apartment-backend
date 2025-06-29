package com.dev.tagashira.controller;

import com.dev.tagashira.service.AuthService;
import com.dev.tagashira.service.SecurityUtil;
import com.dev.tagashira.dto.response.ResLoginDTO;
import com.dev.tagashira.dto.request.UserLoginDTO;
import com.dev.tagashira.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class  AuthController {
    private final AuthService authService;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@RequestBody UserLoginDTO loginDto) {
        ResLoginDTO res = this.authService.getLogin(loginDto);

        // create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(loginDto.getUsername(), res);

        // update refresh token for user
        this.userService.updateUserToken(refresh_token, loginDto.getUsername());

        ResponseCookie resCookies = this.securityUtil.createCookie(refresh_token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @GetMapping("/account")
    public ResponseEntity<ResLoginDTO.UserLogin> getAccount() {
        ResLoginDTO.UserLogin userLogin= this.authService.getUserLogin();
        return ResponseEntity.ok().body(userLogin);
    }

    // When access token expired, frontend calls this endpoint to get a new access token and refresh token
    @GetMapping("/refresh")
    public ResponseEntity<ResLoginDTO> getRefreshToken(
            @CookieValue(name = "refresh_token", defaultValue = "abc") String refresh_token) throws Exception {

        // get refreshed user
        ResLoginDTO res = this.securityUtil.getRefreshedUser(refresh_token);

        // set cookies
        ResponseCookie resCookies = this.securityUtil.createCookie(refresh_token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                .body(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() throws Exception {
        ResponseCookie deleteSpringCookie = this.authService.handleLogout();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString())
                .body(null);
    }

}
