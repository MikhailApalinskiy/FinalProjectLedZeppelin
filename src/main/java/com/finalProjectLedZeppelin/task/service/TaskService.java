package com.finalProjectLedZeppelin.task.service;

import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import com.finalProjectLedZeppelin.common.error.NotFoundException;
import com.finalProjectLedZeppelin.task.dto.TaskCreateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskResponse;
import com.finalProjectLedZeppelin.task.dto.TaskUpdateRequest;
import com.finalProjectLedZeppelin.task.model.Task;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import com.finalProjectLedZeppelin.task.repo.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse create(TaskCreateRequest req) {
        Task t = new Task();
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setDeadline(req.deadline());
        if (req.assigneeId() == null) {
            t.setAssignee(null);
        } else {
            User assignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.assigneeId()));
            t.setAssignee(assignee);
        }
        return toResponse(taskRepository.save(t));
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long userId, boolean isAdmin, Long taskId) {
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        if (!isAdmin) {
            requireAssignee(userId, t);
        }
        return toResponse(t);
    }

    public TaskResponse adminUpdate(Long taskId, TaskUpdateRequest req) {
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        t.setTitle(req.title());
        t.setDescription(req.description());
        if (req.status() != null) {
            t.setStatus(req.status());
        }
        t.setDeadline(req.deadline());
        if (req.assigneeId() == null) {
            t.setAssignee(null);
        } else {
            User assignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.assigneeId()));
            t.setAssignee(assignee);
        }
        return toResponse(t);
    }

    public TaskResponse updateStatus(Long userId, boolean isAdmin, Long taskId, TaskStatus newStatus) {
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        if (!isAdmin) {
            requireAssignee(userId, t);
        }
        t.setStatus(newStatus);
        return toResponse(t);
    }

    public void delete(Long taskId) {
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));
        taskRepository.delete(t);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(
            Long userId,
            boolean isAdmin,
            TaskStatus status,
            LocalDate deadlineFrom,
            LocalDate deadlineTo,
            Pageable pageable
    ) {
        boolean hasStatus = status != null;
        boolean hasRange = deadlineFrom != null && deadlineTo != null;
        Page<Task> page;
        if (isAdmin) {
            if (hasStatus && hasRange) {
                page = taskRepository.findAllByStatusAndDeadlineBetween(status, deadlineFrom, deadlineTo, pageable);
            } else if (hasStatus) {
                page = taskRepository.findAllByStatus(status, pageable);
            } else if (hasRange) {
                page = taskRepository.findAllByDeadlineBetween(deadlineFrom, deadlineTo, pageable);
            } else {
                page = taskRepository.findAll(pageable);
            }
        } else {
            if (hasStatus && hasRange) {
                page = taskRepository.findAllByAssigneeIdAndStatusAndDeadlineBetween(userId, status, deadlineFrom, deadlineTo, pageable);
            } else if (hasStatus) {
                page = taskRepository.findAllByAssigneeIdAndStatus(userId, status, pageable);
            } else if (hasRange) {
                page = taskRepository.findAllByAssigneeIdAndDeadlineBetween(userId, deadlineFrom, deadlineTo, pageable);
            } else {
                page = taskRepository.findAllByAssigneeId(userId, pageable);
            }
        }
        return page.map(this::toResponse);
    }

    private void requireCanView(Long userId, Task t) {
        requireAssignee(userId, t);
    }

    private void requireAssignee(Long userId, Task t) {
        if (t.getAssignee() == null) {
            throw new AccessDeniedException("Task is not assigned");
        }
        if (!t.getAssignee().getId().equals(userId)) {
            throw new AccessDeniedException("Not your task");
        }
    }

    private TaskResponse toResponse(Task t) {
        User a = t.getAssignee();
        return new TaskResponse(
                t.getId(),
                a != null ? a.getId() : null,
                a != null ? a.getEmail() : null,
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getDeadline(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}