package com.automation.service.controller;

import com.automation.service.model.AutomationTask;
import com.automation.service.scheduler.DynamicTaskScheduler;
import com.automation.service.service.AutomationTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final AutomationTaskService taskService;
    private final DynamicTaskScheduler dynamicTaskScheduler;

    @GetMapping
    public List<AutomationTask> list(
            @RequestParam(required = false) AutomationTask.TaskStatus status,
            @RequestParam(required = false) AutomationTask.TaskType type) {
        if (status != null) {
            return taskService.findByStatus(status);
        }
        if (type != null) {
            return taskService.findByType(type);
        }
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AutomationTask> get(@PathVariable Long id) {
        return taskService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AutomationTask> create(@Valid @RequestBody AutomationTask task) {
        AutomationTask created = taskService.create(task);
        if (created.getType() == AutomationTask.TaskType.SCHEDULED) {
            dynamicTaskScheduler.schedule(created);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AutomationTask> update(
            @PathVariable Long id, @Valid @RequestBody AutomationTask task) {
        return taskService.update(id, task).map(updated -> {
            if (updated.getType() == AutomationTask.TaskType.SCHEDULED
                    && updated.getStatus() != AutomationTask.TaskStatus.DISABLED) {
                dynamicTaskScheduler.schedule(updated);
            } else {
                dynamicTaskScheduler.cancel(updated.getId());
            }
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dynamicTaskScheduler.cancel(id);
        return taskService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<AutomationTask> execute(@PathVariable Long id) {
        return taskService.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/scheduled")
    public ResponseEntity<Boolean> isScheduled(@PathVariable Long id) {
        if (taskService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dynamicTaskScheduler.isScheduled(id));
    }
}
