package com.automation.service.repository;

import com.automation.service.model.AutomationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationTaskRepository extends JpaRepository<AutomationTask, Long> {

    List<AutomationTask> findByStatus(AutomationTask.TaskStatus status);

    List<AutomationTask> findByType(AutomationTask.TaskType type);

    List<AutomationTask> findByTypeAndStatus(AutomationTask.TaskType type, AutomationTask.TaskStatus status);
}
