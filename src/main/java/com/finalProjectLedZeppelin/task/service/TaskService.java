package com.finalProjectLedZeppelin.task.service;

import com.finalProjectLedZeppelin.auth.model.User;
import com.finalProjectLedZeppelin.auth.repo.UserRepository;
import com.finalProjectLedZeppelin.task.dto.TaskCreateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskResponse;
import com.finalProjectLedZeppelin.task.dto.TaskUpdateRequest;
import com.finalProjectLedZeppelin.task.model.Task;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import com.finalProjectLedZeppelin.task.repo.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public TaskResponse create(Long userId, TaskCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Task t = new Task();
        t.setUser(user);
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setDeadline(req.deadline());
        Task saved = taskRepository.save(t);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long userId, Long taskId) {
        Task t = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        return toResponse(t);
    }

    public TaskResponse update(Long userId, Long taskId, TaskUpdateRequest req) {
        Task t = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        t.setTitle(req.title());
        t.setDescription(req.description());
        if (req.status() != null) t.setStatus(req.status());
        t.setDeadline(req.deadline());
        return toResponse(t);
    }

    public void delete(Long userId, Long taskId) {
        Task t = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        taskRepository.delete(t);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(
            Long userId,
            TaskStatus status,
            LocalDate deadlineFrom,
            LocalDate deadlineTo,
            Pageable pageable
    ) {
        Page<Task> page;
        boolean hasStatus = status != null;
        boolean hasRange = deadlineFrom != null && deadlineTo != null;
        if (hasStatus && hasRange) {
            page = taskRepository.findAllByUserIdAndStatusAndDeadlineBetween(userId, status, deadlineFrom, deadlineTo, pageable);
        } else if (hasStatus) {
            page = taskRepository.findAllByUserIdAndStatus(userId, status, pageable);
        } else if (hasRange) {
            page = taskRepository.findAllByUserIdAndDeadlineBetween(userId, deadlineFrom, deadlineTo, pageable);
        } else {
            page = taskRepository.findAllByUserId(userId, pageable);
        }
        return page.map(this::toResponse);
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(
                t.getId(),
                t.getUser().getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getDeadline(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }
}
