package com.vaccine.dto;

import com.vaccine.enums.Role;

public record UserResponse(Long id, String name, String email,
                           String phone, int age, Role role) {}