package com.vaccine.dto;

import com.vaccine.enums.OverallStatus;
import com.vaccine.enums.VaccineStatus;

public record AdminUserRow(Long id, String name, String email, String phone, int age,
                           VaccineStatus vaccineA, VaccineStatus vaccineB,
                           OverallStatus overall,
                           Long bookedAppointmentIdA, Long bookedAppointmentIdB) {}