package com.vaccine.dto;

import com.vaccine.entity.Appointment;
import com.vaccine.enums.AppointmentStatus;
import com.vaccine.enums.VaccineType;

import java.time.LocalDate;

public record AppointmentResponse(Long id, VaccineType vaccineType,
                                  LocalDate appointmentDate, AppointmentStatus status,
                                  LocalDate completedDate, String certificateNumber) {

    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(a.getId(), a.getVaccineType(),
                a.getAppointmentDate(), a.getStatus(),
                a.getCompletedDate(), a.getCertificateNumber());
    }
}