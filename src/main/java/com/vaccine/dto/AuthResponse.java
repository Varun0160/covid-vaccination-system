package com.vaccine.dto;

import com.vaccine.enums.Role;

public record AuthResponse(String token, String name, Role role) {}