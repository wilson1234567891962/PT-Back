package com.taskmanager.model;

import org.junit.Test;
import java.util.Date;
import static org.junit.Assert.*;

public class TaskTest {

    @Test
    public void testTaskConstructorAndGetters() {
        // Arrange
        Long taskId = 1L;
        String title = "Test Task";
        String description = "Test Description";
        Boolean completed = false;
        Date createdAt = new Date();
        Date updatedAt = new Date();
        
        // Act
        Task task = new Task(taskId, title, description, completed, createdAt, updatedAt);
        
        // Assert
        assertEquals(taskId, task.getTaskId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(completed, task.getCompleted());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(updatedAt, task.getUpdatedAt());
    }
    
    @Test
    public void testTaskDefaultConstructor() {
        // Act
        Task task = new Task();
        
        // Assert
        assertNull(task.getTaskId());
        assertNull(task.getTitle());
        assertNull(task.getDescription());
        assertNull(task.getCompleted());
        assertNull(task.getCreatedAt());
        assertNull(task.getUpdatedAt());
    }
    
    @Test
    public void testTaskSetters() {
        // Arrange
        Task task = new Task();
        Long taskId = 1L;
        String title = "Test Task";
        String description = "Test Description";
        Boolean completed = true;
        Date createdAt = new Date();
        Date updatedAt = new Date();
        
        // Act
        task.setTaskId(taskId);
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed);
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(updatedAt);
        
        // Assert
        assertEquals(taskId, task.getTaskId());
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertEquals(completed, task.getCompleted());
        assertEquals(createdAt, task.getCreatedAt());
        assertEquals(updatedAt, task.getUpdatedAt());
    }
    
    @Test
    public void testTaskToString() {
        // Arrange
        Task task = new Task(1L, "Test Task", "Test Description", false, new Date(), new Date());
        
        // Act
        String result = task.toString();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Test Task"));
        assertTrue(result.contains("Test Description"));
        assertTrue(result.contains("taskId=1"));
    }
    
    @Test
    public void testTaskEqualsAndHashCode() {
        // Arrange
        Task task1 = new Task(1L, "Task 1", "Desc 1", false, new Date(), new Date());
        Task task2 = new Task(1L, "Task 1", "Desc 1", false, new Date(), new Date());
        Task task3 = new Task(2L, "Task 2", "Desc 2", true, new Date(), new Date());
        
        // Assert - Not implementing equals/hashCode, so they should not be equal
        assertNotEquals(task1, task2); // Different objects
        assertNotEquals(task1.hashCode(), task2.hashCode()); // Different hash codes
    }
    
    @Test
    public void testTaskWithNullValues() {
        // Arrange
        Task task = new Task();
        
        // Act
        task.setTaskId(null);
        task.setTitle(null);
        task.setDescription(null);
        task.setCompleted(null);
        task.setCreatedAt(null);
        task.setUpdatedAt(null);
        
        // Assert
        assertNull(task.getTaskId());
        assertNull(task.getTitle());
        assertNull(task.getDescription());
        assertNull(task.getCompleted());
        assertNull(task.getCreatedAt());
        assertNull(task.getUpdatedAt());
    }
}