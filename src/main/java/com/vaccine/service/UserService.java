package com.vaccine.service;

import com.vaccine.dto.AdminUserRow;
import com.vaccine.dto.UserResponse;
import com.vaccine.entity.Appointment;
import com.vaccine.entity.User;
import com.vaccine.enums.*;
import com.vaccine.exception.ResourceNotFoundException;
import com.vaccine.repository.AppointmentRepository;
import com.vaccine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final AppointmentRepository appointmentRepo;
    private final AppointmentService appointmentService;

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponse(u.getId(), u.getName(), u.getEmail(),
                u.getPhone(), u.getAge(), u.getRole());
    }

    @Transactional(readOnly = true)
    public List<AdminUserRow> getAllUsersForAdmin() {
        return userRepo.findByRole(Role.USER).stream().map(this::toRow).toList();
    }

    private AdminUserRow toRow(User u) {
        Long id = u.getId();
        return new AdminUserRow(
                id, u.getName(), u.getEmail(), u.getPhone(), u.getAge(),
                appointmentService.vaccineStatus(id, VaccineType.A),
                appointmentService.vaccineStatus(id, VaccineType.B),
                appointmentService.overallStatus(id),
                bookedId(id, VaccineType.A),
                bookedId(id, VaccineType.B));
    }

    private Long bookedId(Long userId, VaccineType type) {
        return appointmentRepo
                .findByUserIdAndVaccineTypeAndStatus(userId, type, AppointmentStatus.BOOKED)
                .map(Appointment::getId)
                .orElse(null);
    }
}