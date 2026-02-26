package com.automation.service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "automation_tasks")
@Data
@NoArgsConstructor
public class AutomationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task name is required")
    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @NotNull(message = "Task type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * Cron expression for scheduled tasks (e.g. "0 0 * * * *" for every hour).
     * Only applicable when type is SCHEDULED.
     */
    @Column
    private String cronExpression;

    /**
     * Prompt sent to Claude AI for AI-powered automation tasks.
     * Only applicable when type is AI_ASSISTED.
     */
    @Column(length = 4000)
    private String aiPrompt;

    /**
     * The most recent response received from Claude AI.
     */
    @Column(length = 8000)
    private String lastAiResponse;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastExecutedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TaskType {
        MANUAL,
        SCHEDULED,
        AI_ASSISTED
    }

    public enum TaskStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        DISABLED
    }
}
