package com.vaccine.controller;

import com.vaccine.dto.AdminUserRow;
import com.vaccine.dto.AppointmentResponse;
import com.vaccine.service.AppointmentService;
import com.vaccine.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AppointmentService appointmentService;

    @GetMapping("/users")
    public List<AdminUserRow> allUsers() {
        return userService.getAllUsersForAdmin();
    }

    @PutMapping("/appointments/{id}/complete")
    public AppointmentResponse complete(@PathVariable("id") Long id) {
        return AppointmentResponse.from(appointmentService.markCompleted(id));
    }
}