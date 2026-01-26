package com.finalProjectLedZeppelin.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull String role) {
}
