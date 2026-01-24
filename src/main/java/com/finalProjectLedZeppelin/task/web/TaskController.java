package com.finalProjectLedZeppelin.task.web;

import com.finalProjectLedZeppelin.task.dto.TaskCreateRequest;
import com.finalProjectLedZeppelin.task.dto.TaskResponse;
import com.finalProjectLedZeppelin.task.dto.TaskUpdateRequest;
import com.finalProjectLedZeppelin.task.model.TaskStatus;
import com.finalProjectLedZeppelin.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    private static Long userIdFromHeader(String header) {
        return Long.parseLong(header);
    }

    @PostMapping
    public TaskResponse create(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @Valid @RequestBody TaskCreateRequest req
    ) {
        return taskService.create(userIdFromHeader(userIdHeader), req);
    }

    @GetMapping("/{id}")
    public TaskResponse get(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @PathVariable Long id
    ) {
        return taskService.get(userIdFromHeader(userIdHeader), id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest req
    ) {
        return taskService.update(userIdFromHeader(userIdHeader), id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @PathVariable Long id
    ) {
        taskService.delete(userIdFromHeader(userIdHeader), id);
    }

    @GetMapping
    public Page<TaskResponse> list(
            @RequestHeader("X-USER-ID") String userIdHeader,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,
            Pageable pageable
    ) {
        return taskService.list(userIdFromHeader(userIdHeader), status, deadlineFrom, deadlineTo, pageable);
    }
}
