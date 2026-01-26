package com.finalProjectLedZeppelin.auth.web;

import com.finalProjectLedZeppelin.auth.dto.UserOption;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserOption> search(@RequestParam(defaultValue = "") String q) {
        String query = q == null ? "" : q.trim();
        if (query.isEmpty()) return List.of();
        return userRepository
                .findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(query)
                .stream()
                .map(u -> new UserOption(u.getId(), u.getEmail()))
                .toList();
    }
}

