package com.finalProjectLedZeppelin.auth.repo;

import com.finalProjectLedZeppelin.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(String q);

    Page<User> findByEmailContainingIgnoreCase(String q, Pageable pageable);

    boolean existsByEmail(String email);
}
