package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.UpdateUserRoleRequest;
import com.finalProjectLedZeppelin.auth.dto.UserAdminResponse;
import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import com.finalProjectLedZeppelin.common.error.NotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Page<UserAdminResponse> list(
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        Page<User> page = (q == null || q.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCase(q.trim(), pageable);
        return page.map(u -> new UserAdminResponse(
                u.getId(),
                u.getEmail(),
                u.getRole().name(),
                u.getCreatedAt()
        ));
    }

    @PatchMapping("/{id}/role")
    public UserAdminResponse updateRole(@PathVariable Long id, @RequestBody @Valid UpdateUserRoleRequest req) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        u.setRole(UserRole.valueOf(req.role()));
        return new UserAdminResponse(u.getId(), u.getEmail(), u.getRole().name(), u.getCreatedAt());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Long currentUserId = (Long) auth.getPrincipal();
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("You can't delete yourself");
        }
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}
