package com.finalProjectLedZeppelin.task.repo;

import com.finalProjectLedZeppelin.task.model.Task;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndUserId(Long id, Long userId);

    Page<Task> findAllByUserId(Long userId, Pageable pageable);

    Page<Task> findAllByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);

    Page<Task> findAllByUserIdAndDeadlineBetween(Long userId, LocalDate from, LocalDate to, Pageable pageable);

    Page<Task> findAllByUserIdAndStatusAndDeadlineBetween(
            Long userId,
            TaskStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );
}
