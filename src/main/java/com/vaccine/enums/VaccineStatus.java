package com.vaccine.enums;

public enum VaccineStatus {
    COMPLETED,   // done, certificate available
    BOOKED,      // appointment exists, waiting for the admin to complete it
    PENDING,     // can be booked now
    LOCKED       // can't be booked yet (B before A is done)
}