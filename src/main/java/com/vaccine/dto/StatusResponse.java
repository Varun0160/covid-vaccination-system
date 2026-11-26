package com.vaccine.dto;

import com.vaccine.enums.OverallStatus;
import com.vaccine.enums.VaccineStatus;

public record StatusResponse(
        VaccineStatus vaccineA,
        VaccineStatus vaccineB,
        OverallStatus overall,
        boolean canDownloadA,
        boolean canDownloadB
) {}