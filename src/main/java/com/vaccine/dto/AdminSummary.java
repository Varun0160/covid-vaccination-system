package com.vaccine.dto;

public record AdminSummary(long total, long fullyVaccinated,
                           long partiallyVaccinated, long notVaccinated) {}