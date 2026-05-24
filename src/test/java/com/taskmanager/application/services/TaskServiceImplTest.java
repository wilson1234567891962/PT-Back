package com.taskmanager.application.services;

import com.taskmanager.application.ports.TaskRepository;
import com.taskmanager.domain.exceptions.TaskNotFoundException;
import com.taskmanager.domain.exceptions.TaskValidationException;
import com.taskmanager.domain.models.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskServiceImplTest {

    private TaskServiceImpl taskService;
    private TaskRepository taskRepository;

    @Before
    public void setUp() {
        taskRepository = Mockito.mock(TaskRepository.class);
        taskService = new TaskServiceImpl(taskRepository);
    }

    @Test
    public void testCreateTaskSuccess() {
        // Arrange
        String title = "Test Task";
        String description = "Test Description";
        Task savedTask = Task.create(title, description);
        savedTask.setTaskId(1L);
        
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        
        // Act
        Task result = taskService.createTask(title, description);
        
        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(1), result.getTaskId());
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        verify(taskRepository).save(any(Task.class));
    }

    @Test(expected = TaskValidationException.class)
    public void testCreateTaskWithNullTitle() {
        // Act & Assert
        taskService.createTask(null, "Description");
    }

    @Test(expected = TaskValidationException.class)
    public void testCreateTaskWithEmptyTitle() {
        // Act & Assert
        taskService.createTask("", "Description");
    }

    @Test
    public void testGetTaskByIdSuccess() {
        // Arrange
        Long taskId = 1L;
        Task task = Task.create("Test Task", "Test Description");
        task.setTaskId(taskId);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        
        // Act
        Task result = taskService.getTaskById(taskId);
        
        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getTaskId());
        verify(taskRepository).findById(taskId);
    }

    @Test(expected = TaskNotFoundException.class)
    public void testGetTaskByIdNotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        
        // Act & Assert
        taskService.getTaskById(taskId);
    }

    @Test
    public void testGetAllTasks() {
        // Arrange
        Task task1 = Task.create("Task 1", "Description 1");
        task1.setTaskId(1L);
        Task task2 = Task.create("Task 2", "Description 2");
        task2.setTaskId(2L);
        List<Task> tasks = Arrays.asList(task1, task2);
        
        when(taskRepository.findAll()).thenReturn(tasks);
        
        // Act
        List<Task> result = taskService.getAllTasks();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository).findAll();
    }

    @Test
    public void testUpdateTaskSuccess() {
        // Arrange
        Long taskId = 1L;
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        Boolean completed = true;
        
        Task existingTask = Task.create("Original Title", "Original Description");
        existingTask.setTaskId(taskId);
        
        Task updatedTask = Task.create(newTitle, newDescription);
        updatedTask.setTaskId(taskId);
        updatedTask.markAsCompleted();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.update(any(Task.class))).thenReturn(updatedTask);
        
        // Act
        Task result = taskService.updateTask(taskId, newTitle, newDescription, completed);
        
        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getTaskId());
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        assertTrue(result.getCompleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).update(any(Task.class));
    }

    @Test
    public void testUpdateTaskWithNullCompleted() {
        // Arrange
        Long taskId = 1L;
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        
        Task existingTask = Task.create("Original Title", "Original Description");
        existingTask.setTaskId(taskId);
        
        Task updatedTask = Task.create(newTitle, newDescription);
        updatedTask.setTaskId(taskId);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.update(any(Task.class))).thenReturn(updatedTask);
        
        // Act
        Task result = taskService.updateTask(taskId, newTitle, newDescription, null);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.getCompleted()); // Should remain false
    }

    @Test(expected = TaskNotFoundException.class)
    public void testUpdateTaskNotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        
        // Act & Assert
        taskService.updateTask(taskId, "Title", "Description", true);
    }

    @Test
    public void testDeleteTaskSuccess() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);
        
        // Act
        taskService.deleteTask(taskId);
        
        // Assert
        verify(taskRepository).existsById(taskId);
        verify(taskRepository).delete(taskId);
    }

    @Test(expected = TaskNotFoundException.class)
    public void testDeleteTaskNotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.existsById(taskId)).thenReturn(false);
        
        // Act & Assert
        taskService.deleteTask(taskId);
    }

    @Test
    public void testMarkTaskAsCompleted() {
        // Arrange
        Long taskId = 1L;
        Task task = Task.create("Test Task", "Test Description");
        task.setTaskId(taskId);
        
        Task completedTask = Task.create("Test Task", "Test Description");
        completedTask.setTaskId(taskId);
        completedTask.markAsCompleted();
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.update(any(Task.class))).thenReturn(completedTask);
        
        // Act
        Task result = taskService.markTaskAsCompleted(taskId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getCompleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).update(any(Task.class));
    }

    @Test
    public void testMarkTaskAsPending() {
        // Arrange
        Long taskId = 1L;
        Task task = Task.create("Test Task", "Test Description");
        task.setTaskId(taskId);
        task.markAsCompleted();
        
        Task pendingTask = Task.create("Test Task", "Test Description");
        pendingTask.setTaskId(taskId);
        
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.update(any(Task.class))).thenReturn(pendingTask);
        
        // Act
        Task result = taskService.markTaskAsPending(taskId);
        
        // Assert
        assertNotNull(result);
        assertFalse(result.getCompleted());
        verify(taskRepository).findById(taskId);
        verify(taskRepository).update(any(Task.class));
    }

    @Test
    public void testGetCompletedTasks() {
        // Arrange
        Task task1 = Task.create("Completed Task 1", "Description 1");
        task1.setTaskId(1L);
        task1.markAsCompleted();
        
        Task task2 = Task.create("Completed Task 2", "Description 2");
        task2.setTaskId(2L);
        task2.markAsCompleted();
        
        List<Task> completedTasks = Arrays.asList(task1, task2);
        
        when(taskRepository.findByCompleted(true)).thenReturn(completedTasks);
        
        // Act
        List<Task> result = taskService.getCompletedTasks();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCompleted());
        assertTrue(result.get(1).getCompleted());
        verify(taskRepository).findByCompleted(true);
    }

    @Test
    public void testGetPendingTasks() {
        // Arrange
        Task task1 = Task.create("Pending Task 1", "Description 1");
        task1.setTaskId(1L);
        
        Task task2 = Task.create("Pending Task 2", "Description 2");
        task2.setTaskId(2L);
        
        List<Task> pendingTasks = Arrays.asList(task1, task2);
        
        when(taskRepository.findByCompleted(false)).thenReturn(pendingTasks);
        
        // Act
        List<Task> result = taskService.getPendingTasks();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.get(0).getCompleted());
        assertFalse(result.get(1).getCompleted());
        verify(taskRepository).findByCompleted(false);
    }

    @Test
    public void testSearchTasksByTitle() {
        // Arrange
        String searchQuery = "test";
        Task task1 = Task.create("Test Task 1", "Description 1");
        task1.setTaskId(1L);
        
        Task task2 = Task.create("Another Test Task", "Description 2");
        task2.setTaskId(2L);
        
        List<Task> searchResults = Arrays.asList(task1, task2);
        
        when(taskRepository.findByTitleContaining(searchQuery)).thenReturn(searchResults);
        
        // Act
        List<Task> result = taskService.searchTasksByTitle(searchQuery);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository).findByTitleContaining(searchQuery);
    }

    @Test
    public void testSearchTasksByTitleWithEmptyQuery() {
        // Arrange
        Task task1 = Task.create("Task 1", "Description 1");
        task1.setTaskId(1L);
        
        Task task2 = Task.create("Task 2", "Description 2");
        task2.setTaskId(2L);
        
        List<Task> allTasks = Arrays.asList(task1, task2);
        
        when(taskRepository.findAll()).thenReturn(allTasks);
        
        // Act
        List<Task> result = taskService.searchTasksByTitle("");
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository).findAll();
        verify(taskRepository, never()).findByTitleContaining(anyString());
    }

    @Test
    public void testTaskExists() {
        // Arrange
        Long taskId = 1L;
        when(taskRepository.existsById(taskId)).thenReturn(true);
        
        // Act
        boolean result = taskService.taskExists(taskId);
        
        // Assert
        assertTrue(result);
        verify(taskRepository).existsById(taskId);
    }

    @Test
    public void testTaskDoesNotExist() {
        // Arrange
        Long taskId = 999L;
        when(taskRepository.existsById(taskId)).thenReturn(false);
        
        // Act
        boolean result = taskService.taskExists(taskId);
        
        // Assert
        assertFalse(result);
        verify(taskRepository).existsById(taskId);
    }
}