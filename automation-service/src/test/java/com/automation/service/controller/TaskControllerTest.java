package com.automation.service.controller;

import com.automation.service.model.AutomationTask;
import com.automation.service.scheduler.DynamicTaskScheduler;
import com.automation.service.service.AutomationTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutomationTaskService taskService;

    @MockBean
    private DynamicTaskScheduler dynamicTaskScheduler;

    private AutomationTask buildTask(Long id, String name) {
        AutomationTask task = new AutomationTask();
        task.setId(id);
        task.setName(name);
        task.setType(AutomationTask.TaskType.MANUAL);
        task.setStatus(AutomationTask.TaskStatus.PENDING);
        return task;
    }

    @Test
    void list_returnsAllTasks() throws Exception {
        when(taskService.findAll()).thenReturn(List.of(buildTask(1L, "Task 1")));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Task 1"));
    }

    @Test
    void get_returnsTask_whenExists() throws Exception {
        when(taskService.findById(1L)).thenReturn(Optional.of(buildTask(1L, "Task 1")));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void get_returns404_whenNotExists() throws Exception {
        when(taskService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsCreated() throws Exception {
        AutomationTask task = buildTask(null, "New Task");
        AutomationTask saved = buildTask(1L, "New Task");
        when(taskService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void delete_returns204_whenExists() throws Exception {
        when(taskService.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404_whenNotExists() throws Exception {
        when(taskService.delete(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void execute_returnsTask_whenExists() throws Exception {
        AutomationTask task = buildTask(1L, "Task 1");
        task.setStatus(AutomationTask.TaskStatus.COMPLETED);
        when(taskService.execute(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(post("/api/tasks/1/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void execute_returns404_whenNotExists() throws Exception {
        when(taskService.execute(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks/99/execute"))
                .andExpect(status().isNotFound());
    }
}
