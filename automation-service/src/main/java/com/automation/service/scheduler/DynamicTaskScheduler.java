package com.automation.service.scheduler;

import com.automation.service.model.AutomationTask;
import com.automation.service.service.AutomationTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Manages dynamic scheduling of automation tasks based on their cron expressions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicTaskScheduler {

    private final TaskScheduler taskScheduler;
    private final AutomationTaskService automationTaskService;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * Schedule a task using its configured cron expression.
     * If the task is already scheduled, it is rescheduled with the new cron expression.
     */
    public void schedule(AutomationTask task) {
        if (task.getCronExpression() == null || task.getCronExpression().isBlank()) {
            log.warn("Task [{}] has no cron expression; skipping scheduling", task.getId());
            return;
        }
        cancel(task.getId());

        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> automationTaskService.execute(task.getId()),
                new CronTrigger(task.getCronExpression())
        );
        scheduledTasks.put(task.getId(), future);
        log.info("Scheduled task [{}] with cron '{}'", task.getId(), task.getCronExpression());
    }

    /**
     * Cancel the scheduled execution of a task.
     */
    public void cancel(Long taskId) {
        ScheduledFuture<?> existing = scheduledTasks.remove(taskId);
        if (existing != null) {
            existing.cancel(false);
            log.info("Cancelled scheduled task [{}]", taskId);
        }
    }

    /**
     * Returns true if the task is currently scheduled.
     */
    public boolean isScheduled(Long taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        return future != null && !future.isDone();
    }
}
