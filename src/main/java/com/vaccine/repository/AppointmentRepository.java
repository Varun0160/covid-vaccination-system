package com.vaccine.repository;

import com.vaccine.entity.Appointment;
import com.vaccine.enums.AppointmentStatus;
import com.vaccine.enums.VaccineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserId(Long userId);

    boolean existsByUserIdAndVaccineTypeAndStatus(
            Long userId, VaccineType vaccineType, AppointmentStatus status);

    Optional<Appointment> findByUserIdAndVaccineTypeAndStatus(
            Long userId, VaccineType vaccineType, AppointmentStatus status);
}