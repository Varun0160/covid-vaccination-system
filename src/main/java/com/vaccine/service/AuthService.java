package com.vaccine.service;

import com.vaccine.dto.AuthResponse;
import com.vaccine.dto.LoginRequest;
import com.vaccine.dto.RegisterRequest;
import com.vaccine.dto.UserResponse;
import com.vaccine.entity.User;
import com.vaccine.enums.Role;
import com.vaccine.exception.BusinessRuleException;
import com.vaccine.exception.UnauthorizedException;
import com.vaccine.repository.UserRepository;
import com.vaccine.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BAD_LOGIN = "Invalid email or password";

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new BusinessRuleException("Email is already registered");
        }
        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setPhone(req.phone());
        u.setAge(req.age());
        u.setRole(Role.USER);              // public registration can NEVER create an admin
        u = userRepo.save(u);
        return new UserResponse(u.getId(), u.getName(), u.getEmail(),
                u.getPhone(), u.getAge(), u.getRole());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req, Role expectedRole) {
        String email = req.email().trim().toLowerCase();
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(BAD_LOGIN));

        boolean passwordOk = passwordEncoder.matches(req.password(), u.getPassword());
        if (!passwordOk || u.getRole() != expectedRole) {
            throw new UnauthorizedException(BAD_LOGIN);   // same message on purpose
        }
        return new AuthResponse(jwtUtil.generate(u), u.getName(), u.getRole());
    }
}