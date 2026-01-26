package com.finalProjectLedZeppelin.task.repo;

import com.finalProjectLedZeppelin.task.model.Task;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findById(Long id);

    Page<Task> findAllByAssigneeId(Long assigneeId, Pageable pageable);

    Page<Task> findAllByAssigneeIdAndStatus(Long assigneeId, TaskStatus status, Pageable pageable);

    Page<Task> findAllByAssigneeIdAndDeadlineBetween(Long assigneeId, LocalDate from, LocalDate to, Pageable pageable);

    Page<Task> findAllByAssigneeIdAndStatusAndDeadlineBetween(Long assigneeId, TaskStatus status, LocalDate from, LocalDate to, Pageable pageable);

    Page<Task> findAllByAssigneeIsNull(Pageable pageable);

    Page<Task> findAllByAssigneeIsNullAndStatus(TaskStatus status, Pageable pageable);

    Page<Task> findAllByAssigneeIsNullAndDeadlineBetween(LocalDate from, LocalDate to, Pageable pageable);

    Page<Task> findAllByAssigneeIsNullAndStatusAndDeadlineBetween(TaskStatus status, LocalDate from, LocalDate to, Pageable pageable);

    Page<Task> findAllByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findAllByDeadlineBetween(LocalDate from, LocalDate to, Pageable pageable);

    Page<Task> findAllByStatusAndDeadlineBetween(TaskStatus status, LocalDate from, LocalDate to, Pageable pageable);
}
