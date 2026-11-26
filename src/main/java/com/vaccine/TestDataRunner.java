package com.vaccine;

import com.vaccine.entity.Appointment;
import com.vaccine.entity.User;
import com.vaccine.enums.*;
import com.vaccine.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Order(1)
public class TestDataRunner implements CommandLineRunner {

    private final UserRepository userRepo;
    private final AppointmentRepository apptRepo;

    public TestDataRunner(UserRepository userRepo, AppointmentRepository apptRepo) {
        this.userRepo = userRepo;
        this.apptRepo = apptRepo;
    }

    @Override
    public void run(String... args) {
        if (userRepo.existsByEmail("ravi@mail.com")) return;   // don't insert twice

        User ravi = save("Ravi", "ravi@mail.com");
        User sita = save("Sita", "sita@mail.com");
        save("Arun", "arun@mail.com");

        // User 1: A and B completed
        appt(ravi, VaccineType.A, AppointmentStatus.COMPLETED);
        appt(ravi, VaccineType.B, AppointmentStatus.COMPLETED);
        // User 2: only A completed
        appt(sita, VaccineType.A, AppointmentStatus.COMPLETED);
        // User 3 (Arun): nothing

        System.out.println("Test data created: Ravi, Sita, Arun");
    }

    private User save(String name, String email) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword("temp123");   // plain text only for now; BCrypt on Day 5
        u.setPhone("9999999999");
        u.setAge(25);
        u.setRole(Role.USER);
        return userRepo.save(u);
    }

    private void appt(User u, VaccineType t, AppointmentStatus s) {
        Appointment a = new Appointment();
        a.setUser(u);
        a.setVaccineType(t);
        a.setAppointmentDate(LocalDate.now());
        a.setStatus(s);
        if (s == AppointmentStatus.COMPLETED) a.setCompletedDate(LocalDate.now());
        apptRepo.save(a);
    }
}