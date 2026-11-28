package com.vaccine.controller;

import com.vaccine.dto.AuthResponse;
import com.vaccine.dto.LoginRequest;
import com.vaccine.dto.RegisterRequest;
import com.vaccine.dto.UserResponse;
import com.vaccine.enums.Role;
import com.vaccine.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/user/login")
    public AuthResponse userLogin(@Valid @RequestBody LoginRequest req) {
        return authService.login(req, Role.USER);
    }

    @PostMapping("/admin/login")
    public AuthResponse adminLogin(@Valid @RequestBody LoginRequest req) {
        return authService.login(req, Role.ADMIN);
    }
}