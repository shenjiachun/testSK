package com.automation.service.service;

import com.automation.service.model.AutomationTask;
import com.automation.service.repository.AutomationTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationTaskService {

    private final AutomationTaskRepository taskRepository;
    private final ClaudeAIService claudeAIService;

    public List<AutomationTask> findAll() {
        return taskRepository.findAll();
    }

    public Optional<AutomationTask> findById(Long id) {
        return taskRepository.findById(id);
    }

    public List<AutomationTask> findByStatus(AutomationTask.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    public List<AutomationTask> findByType(AutomationTask.TaskType type) {
        return taskRepository.findByType(type);
    }

    @Transactional
    public AutomationTask create(AutomationTask task) {
        task.setStatus(AutomationTask.TaskStatus.PENDING);
        return taskRepository.save(task);
    }

    @Transactional
    public Optional<AutomationTask> update(Long id, AutomationTask updated) {
        return taskRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setType(updated.getType());
            existing.setCronExpression(updated.getCronExpression());
            existing.setAiPrompt(updated.getAiPrompt());
            return taskRepository.save(existing);
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Execute a task by its ID. For AI_ASSISTED tasks, calls Claude AI with the configured prompt.
     */
    @Transactional
    public Optional<AutomationTask> execute(Long id) {
        return taskRepository.findById(id).map(task -> {
            log.info("Executing task [{}]: {}", task.getId(), task.getName());
            task.setStatus(AutomationTask.TaskStatus.RUNNING);
            task.setLastExecutedAt(LocalDateTime.now());
            taskRepository.save(task);

            try {
                if (task.getType() == AutomationTask.TaskType.AI_ASSISTED && task.getAiPrompt() != null) {
                    String response = claudeAIService.sendPrompt(task.getAiPrompt());
                    task.setLastAiResponse(response);
                }
                task.setStatus(AutomationTask.TaskStatus.COMPLETED);
                log.info("Task [{}] completed successfully", task.getId());
            } catch (Exception e) {
                task.setStatus(AutomationTask.TaskStatus.FAILED);
                log.error("Task [{}] failed: {}", task.getId(), e.getMessage());
            }

            return taskRepository.save(task);
        });
    }
}
