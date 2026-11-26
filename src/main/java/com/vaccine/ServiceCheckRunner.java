package com.vaccine;

import com.vaccine.entity.Appointment;
import com.vaccine.entity.User;
import com.vaccine.enums.VaccineStatus;
import com.vaccine.enums.VaccineType;
import com.vaccine.repository.UserRepository;
import com.vaccine.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Order(2)
@RequiredArgsConstructor
public class ServiceCheckRunner implements CommandLineRunner {

    private final UserRepository userRepo;
    private final AppointmentService service;

    @Override
    public void run(String... args) {
        User ravi = userRepo.findByEmail("ravi@mail.com").orElseThrow();
        User sita = userRepo.findByEmail("sita@mail.com").orElseThrow();
        User arun = userRepo.findByEmail("arun@mail.com").orElseThrow();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        System.out.println("\n===== DAY 3 SERVICE CHECK =====");

        System.out.println("\n--- Initial statuses ---");
        print("Ravi", ravi);   // expect A=COMPLETED B=COMPLETED FULLY_VACCINATED
        print("Sita", sita);   // expect A=COMPLETED B=PENDING   PARTIALLY_VACCINATED
        print("Arun", arun);   // expect A=PENDING   B=LOCKED    NOT_VACCINATED

        System.out.println("\n--- Illegal actions (each must be REJECTED) ---");
        attempt("Arun books B first",  () -> service.book(arun.getId(), VaccineType.B, tomorrow));
        attempt("Ravi books A again",  () -> service.book(ravi.getId(), VaccineType.A, tomorrow));
        attempt("Sita books A again",  () -> service.book(sita.getId(), VaccineType.A, tomorrow));
        attempt("Date in the past",    () -> service.book(sita.getId(), VaccineType.B, LocalDate.now().minusDays(1)));

        // Happy path runs only once (the first time), because it changes the data
        if (service.vaccineStatus(arun.getId(), VaccineType.A) == VaccineStatus.PENDING) {
            System.out.println("\n--- Happy path for Arun ---");
            Appointment a = service.book(arun.getId(), VaccineType.A, tomorrow);
            print("Arun after booking A", arun);        // A=BOOKED, B=LOCKED
            attempt("Arun books B while A is only BOOKED",
                    () -> service.book(arun.getId(), VaccineType.B, tomorrow)); // rejected
            service.markCompleted(a.getId());
            print("Arun after A completed", arun);      // A=COMPLETED, B=PENDING
            Appointment b = service.book(arun.getId(), VaccineType.B, tomorrow);
            service.markCompleted(b.getId());
            print("Arun after B completed", arun);      // both COMPLETED
        }
        System.out.println("\n===== END CHECK =====\n");
    }

    private void print(String label, User u) {
        System.out.println(label + " -> " + service.getStatus(u.getId()));
    }

    private void attempt(String label, Runnable action) {
        try {
            action.run();
            System.out.println("!! BUG: should have been rejected: " + label);
        } catch (RuntimeException e) {
            System.out.println("OK rejected: " + label + "  [" + e.getMessage() + "]");
        }
    }
}