package com.daily.app.backend.controller;

import com.daily.app.backend.dto.auth.AuthResponse;
import com.daily.app.backend.dto.auth.ForgotPasswordRequest;
import com.daily.app.backend.dto.auth.LoginRequest;
import com.daily.app.backend.dto.auth.OtpSentResponse;
import com.daily.app.backend.dto.auth.ResetPasswordRequest;
import com.daily.app.backend.dto.auth.SignupOtpRequest;
import com.daily.app.backend.dto.auth.SignupRequest;
import com.daily.app.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup/otp")
    public ResponseEntity<OtpSentResponse> sendSignupOtp(@Valid @RequestBody SignupOtpRequest request) {
        return ResponseEntity.ok(authService.sendSignupOtp(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<OtpSentResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.sendPasswordResetOtp(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
