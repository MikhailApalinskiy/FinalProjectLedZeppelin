package com.finalProjectLedZeppelin.task.dto;

import com.finalProjectLedZeppelin.task.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        TaskStatus status,
        LocalDate deadline,
        Long assigneeId
) {
}
