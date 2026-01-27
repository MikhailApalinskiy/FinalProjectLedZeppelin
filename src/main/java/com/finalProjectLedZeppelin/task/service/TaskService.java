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
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Log4j2
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
        log.info("Task create requested (assigneeId={}, deadline={})", req.assigneeId(), req.deadline());
        Task t = new Task();
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setDeadline(req.deadline());
        if (req.assigneeId() == null) {
            t.setAssignee(null);
            log.debug("Task create: assignee is null");
        } else {
            User assignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> {
                        log.warn("Task create failed: assignee not found (assigneeId={})", req.assigneeId());
                        return new IllegalArgumentException("User not found: " + req.assigneeId());
                    });
            t.setAssignee(assignee);
        }
        Task saved = taskRepository.save(t);
        log.info("Task created (taskId={}, assigneeId={}, status={})",
                saved.getId(),
                saved.getAssignee() != null ? saved.getAssignee().getId() : null,
                saved.getStatus()
        );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long userId, boolean isAdmin, Long taskId) {
        log.debug("Task get requested (taskId={}, userId={}, isAdmin={})", taskId, userId, isAdmin);
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task get failed: task not found (taskId={})", taskId);
                    return new NotFoundException("Task not found");
                });
        if (!isAdmin) {
            try {
                requireAssignee(userId, t);
            } catch (AccessDeniedException ex) {
                log.warn("Task get denied (taskId={}, userId={})", taskId, userId);
                throw ex;
            }
        }
        return toResponse(t);
    }

    public TaskResponse adminUpdate(Long taskId, TaskUpdateRequest req) {
        log.info("Task adminUpdate requested (taskId={}, assigneeId={}, status={}, deadline={})",
                taskId, req.assigneeId(), req.status(), req.deadline()
        );
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task adminUpdate failed: task not found (taskId={})", taskId);
                    return new NotFoundException("Task not found");
                });
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
                    .orElseThrow(() -> {
                        log.warn("Task adminUpdate failed: assignee not found (taskId={}, assigneeId={})",
                                taskId, req.assigneeId()
                        );
                        return new IllegalArgumentException("User not found: " + req.assigneeId());
                    });
            t.setAssignee(assignee);
        }
        log.info("Task adminUpdate success (taskId={}, assigneeId={}, status={})",
                t.getId(),
                t.getAssignee() != null ? t.getAssignee().getId() : null,
                t.getStatus()
        );
        return toResponse(t);
    }

    public TaskResponse updateStatus(Long userId, boolean isAdmin, Long taskId, TaskStatus newStatus) {
        log.info("Task status update requested (taskId={}, userId={}, isAdmin={}, newStatus={})",
                taskId, userId, isAdmin, newStatus
        );
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task status update failed: task not found (taskId={})", taskId);
                    return new NotFoundException("Task not found");
                });
        if (!isAdmin) {
            try {
                requireAssignee(userId, t);
            } catch (AccessDeniedException ex) {
                log.warn("Task status update denied (taskId={}, userId={}, newStatus={})", taskId, userId, newStatus);
                throw ex;
            }
        }
        t.setStatus(newStatus);
        log.info("Task status updated (taskId={}, status={})", taskId, newStatus);
        return toResponse(t);
    }

    public void delete(Long taskId) {
        log.info("Task delete requested (taskId={})", taskId);
        Task t = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task delete failed: task not found (taskId={})", taskId);
                    return new NotFoundException("Task not found");
                });
        taskRepository.delete(t);
        log.info("Task deleted (taskId={})", taskId);
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
        log.debug("Task list requested (userId={}, isAdmin={}, status={}, deadlineFrom={}, deadlineTo={}, page={}, size={}, sort={})",
                userId, isAdmin, status, deadlineFrom, deadlineTo,
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()
        );
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
        log.debug("Task list returned (userId={}, isAdmin={}, totalElements={})",
                userId, isAdmin, page.getTotalElements()
        );
        return page.map(this::toResponse);
    }

    private void requireAssignee(Long userId, Task t) {
        if (t.getAssignee() == null) {
            log.warn("Task access denied: task is not assigned (taskId={}, userId={})", t.getId(), userId);
            throw new AccessDeniedException("Task is not assigned");
        }
        if (!t.getAssignee().getId().equals(userId)) {
            log.warn("Task access denied: not your task (taskId={}, userId={}, assigneeId={})",
                    t.getId(), userId, t.getAssignee().getId()
            );
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