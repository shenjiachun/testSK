package com.automation.service.service;

import com.automation.service.model.AutomationTask;
import com.automation.service.repository.AutomationTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutomationTaskServiceTest {

    @Mock
    private AutomationTaskRepository taskRepository;

    @Mock
    private ClaudeAIService claudeAIService;

    @InjectMocks
    private AutomationTaskService taskService;

    private AutomationTask sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new AutomationTask();
        sampleTask.setId(1L);
        sampleTask.setName("Test Task");
        sampleTask.setType(AutomationTask.TaskType.MANUAL);
        sampleTask.setStatus(AutomationTask.TaskStatus.PENDING);
    }

    @Test
    void findAll_returnsAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask));
        List<AutomationTask> result = taskService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Task");
    }

    @Test
    void findById_returnsTask_whenExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        Optional<AutomationTask> result = taskService.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void findById_returnsEmpty_whenNotExists() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<AutomationTask> result = taskService.findById(99L);
        assertThat(result).isEmpty();
    }

    @Test
    void create_setsStatusToPending() {
        AutomationTask newTask = new AutomationTask();
        newTask.setName("New Task");
        newTask.setType(AutomationTask.TaskType.MANUAL);
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AutomationTask created = taskService.create(newTask);
        assertThat(created.getStatus()).isEqualTo(AutomationTask.TaskStatus.PENDING);
    }

    @Test
    void execute_manualTask_setsStatusToCompleted() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<AutomationTask> result = taskService.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(AutomationTask.TaskStatus.COMPLETED);
        assertThat(result.get().getLastExecutedAt()).isNotNull();
    }

    @Test
    void execute_aiAssistedTask_callsClaudeAI() {
        sampleTask.setType(AutomationTask.TaskType.AI_ASSISTED);
        sampleTask.setAiPrompt("Summarize this report");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(claudeAIService.sendPrompt("Summarize this report")).thenReturn("AI response");

        Optional<AutomationTask> result = taskService.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getLastAiResponse()).isEqualTo("AI response");
        assertThat(result.get().getStatus()).isEqualTo(AutomationTask.TaskStatus.COMPLETED);
        verify(claudeAIService).sendPrompt("Summarize this report");
    }

    @Test
    void execute_failsGracefully_whenAIThrowsException() {
        sampleTask.setType(AutomationTask.TaskType.AI_ASSISTED);
        sampleTask.setAiPrompt("Some prompt");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(claudeAIService.sendPrompt(any())).thenThrow(new RuntimeException("API error"));

        Optional<AutomationTask> result = taskService.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(AutomationTask.TaskStatus.FAILED);
    }

    @Test
    void delete_returnsTrue_whenTaskExists() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        boolean deleted = taskService.delete(1L);
        assertThat(deleted).isTrue();
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void delete_returnsFalse_whenTaskNotExists() {
        when(taskRepository.existsById(99L)).thenReturn(false);
        boolean deleted = taskService.delete(99L);
        assertThat(deleted).isFalse();
        verify(taskRepository, never()).deleteById(any());
    }
}
