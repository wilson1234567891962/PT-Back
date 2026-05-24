package com.taskmanager.domain.models;

import org.junit.Test;
import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class TaskTest {

    @Test
    public void testCreateTask() {
        // Arrange
        String title = "Test Task";
        String description = "Test Description";
        
        // Act
        Task task = Task.create(title, description);
        
        // Assert
        assertNotNull(task);
        assertNull(task.getTaskId()); // ID should be null until saved
        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
        assertFalse(task.getCompleted());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
        assertTrue(task.isValid());
    }

    @Test
    public void testCreateTaskWithNullDescription() {
        // Arrange
        String title = "Test Task";
        
        // Act
        Task task = Task.create(title, null);
        
        // Assert
        assertNotNull(task);
        assertEquals(title, task.getTitle());
        assertNull(task.getDescription());
        assertFalse(task.getCompleted());
        assertTrue(task.isValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTaskWithNullTitle() {
        // Act & Assert
        Task.create(null, "Description");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTaskWithEmptyTitle() {
        // Act & Assert
        Task.create("", "Description");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTaskWithBlankTitle() {
        // Act & Assert
        Task.create("   ", "Description");
    }

    @Test
    public void testUpdateTask() {
        // Arrange
        Task task = Task.create("Original Title", "Original Description");
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        
        // Act
        task.update(newTitle, newDescription);
        
        // Assert
        assertEquals(newTitle, task.getTitle());
        assertEquals(newDescription, task.getDescription());
        assertNotNull(task.getUpdatedAt());
        assertTrue(task.isValid());
    }

    @Test
    public void testUpdateTaskWithNullDescription() {
        // Arrange
        Task task = Task.create("Original Title", "Original Description");
        
        // Act
        task.update("Updated Title", null);
        
        // Assert
        assertEquals("Updated Title", task.getTitle());
        assertEquals("Original Description", task.getDescription()); // Should keep original description
        assertTrue(task.isValid());
    }

    @Test
    public void testUpdateTaskWithNullTitle() {
        // Arrange
        Task task = Task.create("Original Title", "Original Description");
        
        // Act
        task.update(null, "Updated Description");
        
        // Assert
        assertEquals("Original Title", task.getTitle()); // Should keep original title
        assertEquals("Updated Description", task.getDescription());
        assertTrue(task.isValid());
    }

    @Test
    public void testMarkAsCompleted() {
        // Arrange
        Task task = Task.create("Test Task", "Test Description");
        assertFalse(task.getCompleted());
        
        // Act
        task.markAsCompleted();
        
        // Assert
        assertTrue(task.getCompleted());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    public void testMarkAsPending() {
        // Arrange
        Task task = Task.create("Test Task", "Test Description");
        task.markAsCompleted();
        assertTrue(task.getCompleted());
        
        // Act
        task.markAsPending();
        
        // Assert
        assertFalse(task.getCompleted());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    public void testIsValid() {
        // Arrange
        Task validTask = Task.create("Valid Task", "Description");
        Task invalidTask = new Task();
        // Don't set title - leave it null
        
        // Act & Assert
        assertTrue(validTask.isValid());
        assertFalse(invalidTask.isValid());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyTitleThrowsException() {
        // Arrange
        Task task = new Task();
        
        // Act & Assert
        task.setTitle(""); // Should throw IllegalArgumentException
    }

    @Test
    public void testEqualsAndHashCode() {
        // Arrange
        Task task1 = new Task();
        task1.setTaskId(1L);
        task1.setTitle("Task 1");
        
        Task task2 = new Task();
        task2.setTaskId(1L);
        task2.setTitle("Task 1"); // Same title and ID
        
        Task task3 = new Task();
        task3.setTaskId(2L);
        task3.setTitle("Task 1"); // Same title, different ID
        
        // Act & Assert
        // Note: Task doesn't override equals/hashCode, so uses Object's implementation
        // Two different objects with same values are not equal
        assertNotEquals(task1, task2); // Different objects, even with same values
        assertNotEquals(task1, task3); // Different objects
        assertNotEquals(task1.hashCode(), task2.hashCode()); // Different hash codes
        assertNotEquals(task1.hashCode(), task3.hashCode());
    }

    @Test
    public void testToString() {
        // Arrange
        Task task = Task.create("Test Task", "Test Description");
        task.setTaskId(1L);
        
        // Act
        String toString = task.toString();
        
        // Assert
        assertNotNull(toString);
        assertTrue(toString.startsWith("Task{"));
        assertTrue(toString.contains("taskId=1"));
        assertTrue(toString.contains("title='Test Task'"));
        assertTrue(toString.contains("description='Test Description'"));
        assertTrue(toString.contains("completed=false"));
    }
}