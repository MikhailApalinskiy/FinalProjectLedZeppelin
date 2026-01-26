package com.finalProjectLedZeppelin.auth.dto;

import java.time.Instant;

public record UserAdminResponse(Long id, String email, String role, Instant createdAt) {
}
