package com.vaccine.service;

import com.vaccine.dto.AppointmentResponse;
import com.vaccine.dto.StatusResponse;
import com.vaccine.entity.Appointment;
import com.vaccine.entity.User;
import com.vaccine.enums.*;
import com.vaccine.exception.BusinessRuleException;
import com.vaccine.exception.ResourceNotFoundException;
import com.vaccine.repository.AppointmentRepository;
import com.vaccine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final UserRepository userRepo;

    // ---------- BOOK ----------
    @Transactional
    public Appointment book(Long userId, VaccineType type, LocalDate date) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (date == null || date.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Appointment date cannot be in the past");
        }
        if (has(userId, type, AppointmentStatus.COMPLETED)) {
            throw new BusinessRuleException("Vaccine " + type + " is already completed");
        }
        if (has(userId, type, AppointmentStatus.BOOKED)) {
            throw new BusinessRuleException("Vaccine " + type + " is already booked");
        }
        if (type == VaccineType.B && !has(userId, VaccineType.A, AppointmentStatus.COMPLETED)) {
            throw new BusinessRuleException("Complete Vaccine A before booking Vaccine B");
        }

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setVaccineType(type);
        appointment.setAppointmentDate(date);
        appointment.setStatus(AppointmentStatus.BOOKED);
        return appointmentRepo.save(appointment);
    }

    // ---------- ADMIN: MARK COMPLETED ----------
    @Transactional
    public Appointment markCompleted(Long appointmentId) {
        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.BOOKED) {
            throw new BusinessRuleException("Only a BOOKED appointment can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCompletedDate(LocalDate.now());
        appointment.setCertificateNumber("CERT-" + appointment.getVaccineType()
                + "-" + appointment.getUser().getId() + "-" + System.currentTimeMillis());
        return appointmentRepo.save(appointment);
    }

    // ---------- STATUS (calculated, never stored) ----------
    @Transactional(readOnly = true)
    public VaccineStatus vaccineStatus(Long userId, VaccineType type) {
        if (has(userId, type, AppointmentStatus.COMPLETED)) return VaccineStatus.COMPLETED;
        if (has(userId, type, AppointmentStatus.BOOKED))    return VaccineStatus.BOOKED;
        if (type == VaccineType.B && !has(userId, VaccineType.A, AppointmentStatus.COMPLETED)) {
            return VaccineStatus.LOCKED;
        }
        return VaccineStatus.PENDING;
    }

    @Transactional(readOnly = true)
    public OverallStatus overallStatus(Long userId) {
        boolean aDone = vaccineStatus(userId, VaccineType.A) == VaccineStatus.COMPLETED;
        boolean bDone = vaccineStatus(userId, VaccineType.B) == VaccineStatus.COMPLETED;
        if (aDone && bDone) return OverallStatus.FULLY_VACCINATED;
        if (aDone)          return OverallStatus.PARTIALLY_VACCINATED;
        return OverallStatus.NOT_VACCINATED;
    }

    @Transactional(readOnly = true)
    public StatusResponse getStatus(Long userId) {
        userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        VaccineStatus a = vaccineStatus(userId, VaccineType.A);
        VaccineStatus b = vaccineStatus(userId, VaccineType.B);
        return new StatusResponse(a, b, overallStatus(userId),
                a == VaccineStatus.COMPLETED,
                b == VaccineStatus.COMPLETED);
    }

    // ---------- helper ----------

    @Transactional(readOnly = true)
    public List<AppointmentResponse> listForUser(Long userId) {
        return appointmentRepo.findByUserId(userId).stream()
                .map(AppointmentResponse::from)
                .toList();
    }
    private boolean has(Long userId, VaccineType type, AppointmentStatus status) {
        return appointmentRepo.existsByUserIdAndVaccineTypeAndStatus(userId, type, status);
    }
}