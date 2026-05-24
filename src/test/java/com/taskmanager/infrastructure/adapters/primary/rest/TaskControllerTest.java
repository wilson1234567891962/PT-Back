package com.taskmanager.infrastructure.adapters.primary.rest;

import com.taskmanager.application.ports.TaskService;
import com.taskmanager.domain.exceptions.TaskNotFoundException;
import com.taskmanager.domain.exceptions.TaskValidationException;
import com.taskmanager.domain.models.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskControllerTest {

    private TaskController taskController;
    private TaskService taskService;

    @Before
    public void setUp() {
        taskService = Mockito.mock(TaskService.class);
        taskController = new TaskController();
        taskController.setTaskService(taskService);
    }

    @Test
    public void testGetAllTasksSuccess() {
        // Arrange
        Task task1 = Task.create("Task 1", "Description 1");
        task1.setTaskId(1L);
        
        Task task2 = Task.create("Task 2", "Description 2");
        task2.setTaskId(2L);
        
        List<Task> tasks = Arrays.asList(task1, task2);
        
        when(taskService.getAllTasks()).thenReturn(tasks);
        
        // Act
        Response response = taskController.getAllTasks();
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).getAllTasks();
    }

    @Test
    public void testGetAllTasksInternalError() {
        // Arrange
        when(taskService.getAllTasks()).thenThrow(new RuntimeException("Database error"));
        
        // Act
        Response response = taskController.getAllTasks();
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof com.taskmanager.config.ErrorResponse);
        com.taskmanager.config.ErrorResponse error = (com.taskmanager.config.ErrorResponse) response.getEntity();
        assertTrue(error.getMessage().contains("Error al obtener las tareas"));
    }

    @Test
    public void testGetTaskByIdSuccess() {
        // Arrange
        Long taskId = 1L;
        Task task = Task.create("Test Task", "Test Description");
        task.setTaskId(taskId);
        
        when(taskService.getTaskById(taskId)).thenReturn(task);
        
        // Act
        Response response = taskController.getTaskById(taskId);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).getTaskById(taskId);
    }

    @Test
    public void testGetTaskByIdNotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskService.getTaskById(taskId)).thenThrow(new TaskNotFoundException(taskId));
        
        // Act
        Response response = taskController.getTaskById(taskId);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof com.taskmanager.config.ErrorResponse);
        com.taskmanager.config.ErrorResponse error = (com.taskmanager.config.ErrorResponse) response.getEntity();
        assertTrue(error.getMessage().contains("not found"));
    }

    @Test
    public void testCreateTaskSuccess() {
        // Arrange
        TaskController.TaskRequest request = new TaskController.TaskRequest();
        request.title = "New Task";
        request.description = "New Description";
        
        Task createdTask = Task.create(request.title, request.description);
        createdTask.setTaskId(1L);
        
        when(taskService.createTask(request.title, request.description)).thenReturn(createdTask);
        
        // Act
        Response response = taskController.createTask(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).createTask(request.title, request.description);
    }

    @Test
    public void testCreateTaskValidationError() {
        // Arrange
        TaskController.TaskRequest request = new TaskController.TaskRequest();
        request.title = ""; // Empty title
        
        when(taskService.createTask(request.title, request.description))
            .thenThrow(new TaskValidationException("Task title is required"));
        
        // Act
        Response response = taskController.createTask(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof com.taskmanager.config.ErrorResponse);
        com.taskmanager.config.ErrorResponse error = (com.taskmanager.config.ErrorResponse) response.getEntity();
        assertTrue(error.getMessage().contains("Task title is required"));
    }

    @Test
    public void testUpdateTaskSuccess() {
        // Arrange
        Long taskId = 1L;
        TaskController.TaskRequest request = new TaskController.TaskRequest();
        request.title = "Updated Task";
        request.description = "Updated Description";
        request.completed = true;
        
        Task updatedTask = Task.create(request.title, request.description);
        updatedTask.setTaskId(taskId);
        updatedTask.markAsCompleted();
        
        when(taskService.updateTask(taskId, request.title, request.description, request.completed))
            .thenReturn(updatedTask);
        
        // Act
        Response response = taskController.updateTask(taskId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).updateTask(taskId, request.title, request.description, request.completed);
    }

    @Test
    public void testUpdateTaskNotFound() {
        // Arrange
        Long taskId = 999L;
        TaskController.TaskRequest request = new TaskController.TaskRequest();
        request.title = "Updated Task";
        
        when(taskService.updateTask(taskId, request.title, request.description, request.completed))
            .thenThrow(new TaskNotFoundException(taskId));
        
        // Act
        Response response = taskController.updateTask(taskId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteTaskSuccess() {
        // Arrange
        Long taskId = 1L;
        doNothing().when(taskService).deleteTask(taskId);
        
        // Act
        Response response = taskController.deleteTask(taskId);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(taskService).deleteTask(taskId);
    }

    @Test
    public void testDeleteTaskNotFound() {
        // Arrange
        Long taskId = 999L;
        doThrow(new TaskNotFoundException(taskId)).when(taskService).deleteTask(taskId);
        
        // Act
        Response response = taskController.deleteTask(taskId);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testMarkTaskAsCompletedSuccess() {
        // Arrange
        Long taskId = 1L;
        Task task = Task.create("Test Task", "Test Description");
        task.setTaskId(taskId);
        task.markAsCompleted();
        
        when(taskService.markTaskAsCompleted(taskId)).thenReturn(task);
        
        // Act
        Response response = taskController.markTaskAsCompleted(taskId);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).markTaskAsCompleted(taskId);
    }

    @Test
    public void testMarkTaskAsPendingSuccess() {
        // Arrange
        Long taskId = 1L;
        Task task = Task.create("Test Task", "Test Description");
        task.setTaskId(taskId);
        
        when(taskService.markTaskAsPending(taskId)).thenReturn(task);
        
        // Act
        Response response = taskController.markTaskAsPending(taskId);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).markTaskAsPending(taskId);
    }

    @Test
    public void testGetCompletedTasksSuccess() {
        // Arrange
        Task task1 = Task.create("Completed Task 1", "Description 1");
        task1.setTaskId(1L);
        task1.markAsCompleted();
        
        Task task2 = Task.create("Completed Task 2", "Description 2");
        task2.setTaskId(2L);
        task2.markAsCompleted();
        
        List<Task> completedTasks = Arrays.asList(task1, task2);
        
        when(taskService.getCompletedTasks()).thenReturn(completedTasks);
        
        // Act
        Response response = taskController.getCompletedTasks();
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).getCompletedTasks();
    }

    @Test
    public void testGetPendingTasksSuccess() {
        // Arrange
        Task task1 = Task.create("Pending Task 1", "Description 1");
        task1.setTaskId(1L);
        
        Task task2 = Task.create("Pending Task 2", "Description 2");
        task2.setTaskId(2L);
        
        List<Task> pendingTasks = Arrays.asList(task1, task2);
        
        when(taskService.getPendingTasks()).thenReturn(pendingTasks);
        
        // Act
        Response response = taskController.getPendingTasks();
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).getPendingTasks();
    }

    @Test
    public void testSearchTasksByTitleSuccess() {
        // Arrange
        String searchQuery = "test";
        Task task1 = Task.create("Test Task 1", "Description 1");
        task1.setTaskId(1L);
        
        Task task2 = Task.create("Another Test Task", "Description 2");
        task2.setTaskId(2L);
        
        List<Task> searchResults = Arrays.asList(task1, task2);
        
        when(taskService.searchTasksByTitle(searchQuery)).thenReturn(searchResults);
        
        // Act
        Response response = taskController.searchTasksByTitle(searchQuery);
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        verify(taskService).searchTasksByTitle(searchQuery);
    }

    @Test
    public void testTestServiceSuccess() {
        // Arrange
        Task testTask = Task.create("Tarea de prueba hexagonal", "Esta es una tarea de prueba para la arquitectura hexagonal");
        testTask.setTaskId(10L);
        
        Task task1 = Task.create("Existing Task 1", "Description 1");
        task1.setTaskId(1L);
        
        Task task2 = Task.create("Existing Task 2", "Description 2");
        task2.setTaskId(2L);
        
        List<Task> tasks = Arrays.asList(task1, task2, testTask);
        
        when(taskService.createTask(anyString(), anyString())).thenReturn(testTask);
        when(taskService.getAllTasks()).thenReturn(tasks);
        doNothing().when(taskService).deleteTask(testTask.getTaskId());
        
        // Act
        Response response = taskController.testService();
        
        // Assert
        assertNotNull(response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof java.util.Map);
        java.util.Map<?, ?> result = (java.util.Map<?, ?>) response.getEntity();
        assertTrue(result.get("message").toString().contains("Servicio hexagonal probado exitosamente"));
        verify(taskService).createTask(anyString(), anyString());
        verify(taskService).getAllTasks();
        verify(taskService).deleteTask(testTask.getTaskId());
    }

    @Test
    public void testTaskResponseFromDomain() {
        // Arrange
        Task task = Task.create("Test Task", "Test Description");
        task.setTaskId(1L);
        task.markAsCompleted();
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        
        // Act
        TaskController.TaskResponse response = TaskController.TaskResponse.fromDomain(task);
        
        // Assert
        assertNotNull(response);
        assertEquals(Long.valueOf(1), response.taskId);
        assertEquals("Test Task", response.title);
        assertEquals("Test Description", response.description);
        assertTrue(response.completed);
        assertNotNull(response.createdAt);
        assertNotNull(response.updatedAt);
    }
}