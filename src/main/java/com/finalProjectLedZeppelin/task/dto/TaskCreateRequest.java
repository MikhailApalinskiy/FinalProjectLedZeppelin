package com.finalProjectLedZeppelin.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        LocalDate deadline,
        Long assigneeId
) {
}