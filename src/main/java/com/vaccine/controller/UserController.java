package com.vaccine.controller;

import com.vaccine.dto.AppointmentResponse;
import com.vaccine.dto.CertificateFile;
import com.vaccine.dto.StatusResponse;
import com.vaccine.dto.UserResponse;
import com.vaccine.enums.VaccineType;
import com.vaccine.service.AppointmentService;
import com.vaccine.service.CertificateService;
import com.vaccine.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final CertificateService certificateService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Long userId) {
        return userService.getUser(userId);
    }

    @GetMapping("/status")
    public StatusResponse status(@AuthenticationPrincipal Long userId) {
        return appointmentService.getStatus(userId);
    }

    @GetMapping("/appointments")
    public List<AppointmentResponse> appointments(@AuthenticationPrincipal Long userId) {
        return appointmentService.listForUser(userId);
    }

    @PostMapping("/book")
    public ResponseEntity<AppointmentResponse> book(
            @AuthenticationPrincipal Long userId,
            @RequestParam("type") VaccineType type,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        var created = appointmentService.book(userId, type, date);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(created));
    }

    @GetMapping("/certificate/{type}")
    public ResponseEntity<byte[]> certificate(@AuthenticationPrincipal Long userId,
                                              @PathVariable("type") VaccineType type) {
        CertificateFile file = certificateService.generate(userId, type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.content());
    }
}