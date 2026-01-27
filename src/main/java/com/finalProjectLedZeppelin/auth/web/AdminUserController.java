package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.UpdateUserRoleRequest;
import com.finalProjectLedZeppelin.auth.dto.UserAdminResponse;
import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.model.UserRole;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import com.finalProjectLedZeppelin.common.error.NotFoundException;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Log4j2
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
        log.debug(
                "Admin users list requested (query={}, page={}, size={}, sort={})",
                q,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort()
        );
        Page<User> page = (q == null || q.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCase(q.trim(), pageable);
        log.debug(
                "Admin users list returned (totalElements={})",
                page.getTotalElements()
        );
        return page.map(u -> new UserAdminResponse(
                u.getId(),
                u.getEmail(),
                u.getRole().name(),
                u.getCreatedAt()
        ));
    }

    @PatchMapping("/{id}/role")
    public UserAdminResponse updateRole(@PathVariable Long id, @RequestBody @Valid UpdateUserRoleRequest req) {
        log.info(
                "Admin role update requested (targetUserId={}, newRole={})",
                id, req.role()
        );
        User u = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "Admin role update failed: user not found (targetUserId={})",
                            id
                    );
                    return new NotFoundException("User not found");
                });
        u.setRole(UserRole.valueOf(req.role()));
        log.info(
                "Admin role updated (targetUserId={}, role={})",
                u.getId(), u.getRole()
        );
        return new UserAdminResponse(u.getId(), u.getEmail(), u.getRole().name(), u.getCreatedAt());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Long currentUserId = (Long) auth.getPrincipal();
        log.info(
                "Admin delete requested (targetUserId={}, adminId={})",
                id, currentUserId
        );
        if (id.equals(currentUserId)) {
            log.warn(
                    "Admin delete rejected: self-delete attempt (adminId={})",
                    currentUserId
            );
            throw new IllegalArgumentException("You can't delete yourself");
        }
        if (!userRepository.existsById(id)) {
            log.warn(
                    "Admin delete failed: user not found (targetUserId={})",
                    id
            );
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
        log.info(
                "Admin delete success (targetUserId={}, adminId={})",
                id, currentUserId
        );
    }
}
