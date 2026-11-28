package com.vaccine.service;

import com.vaccine.dto.AdminSummary;
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
import java.util.Locale;

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
    public List<AdminUserRow> getAllUsersForAdmin(String search, OverallStatus status) {
        String q = (search == null || search.isBlank()) ? null : search.trim().toLowerCase(Locale.ROOT);

        return userRepo.findByRole(Role.USER).stream()
                .filter(u -> q == null
                        || u.getName().toLowerCase(Locale.ROOT).contains(q)
                        || u.getEmail().toLowerCase(Locale.ROOT).contains(q))
                .map(this::toRow)
                .filter(row -> status == null || row.overall() == status)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminSummary getSummary() {
        List<AdminUserRow> rows = getAllUsersForAdmin(null, null);
        return new AdminSummary(
                rows.size(),
                rows.stream().filter(r -> r.overall() == OverallStatus.FULLY_VACCINATED).count(),
                rows.stream().filter(r -> r.overall() == OverallStatus.PARTIALLY_VACCINATED).count(),
                rows.stream().filter(r -> r.overall() == OverallStatus.NOT_VACCINATED).count());
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