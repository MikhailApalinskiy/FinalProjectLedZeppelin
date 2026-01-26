package com.finalProjectLedZeppelin.task.dto;

import com.finalProjectLedZeppelin.task.model.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        Long assigneeId,
        String assigneeEmail,
        String title,
        String description,
        TaskStatus status,
        LocalDate deadline,
        Instant createdAt,
        Instant updatedAt
) {
}
