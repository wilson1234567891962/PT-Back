package com.taskmanager.rest;

import com.taskmanager.dao.TaskDAO;
import com.taskmanager.model.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskResourceTest {

    @Mock
    private TaskDAO taskDAO;
    
    @InjectMocks
    private TaskResource taskResource;
    
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void testGetAllTasksSuccess() throws SQLException {
        // Arrange
        Task task1 = new Task(1L, "Task 1", "Description 1", false, new Date(), new Date());
        Task task2 = new Task(2L, "Task 2", "Description 2", true, new Date(), new Date());
        List<Task> tasks = Arrays.asList(task1, task2);
        
        when(taskDAO.getAllTasks()).thenReturn(tasks);
        
        // Act
        Response response = taskResource.getAllTasks();
        
        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(tasks, response.getEntity());
        verify(taskDAO, times(1)).getAllTasks();
    }
    
    @Test
    public void testGetAllTasksDatabaseError() throws SQLException {
        // Arrange
        when(taskDAO.getAllTasks()).thenThrow(new SQLException("Database error"));
        
        // Act
        Response response = taskResource.getAllTasks();
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener las tareas"));
        verify(taskDAO, times(1)).getAllTasks();
    }
    
    @Test
    public void testGetTaskByIdSuccess() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task task = new Task(taskId, "Task 1", "Description 1", false, new Date(), new Date());
        
        when(taskDAO.getTaskById(taskId)).thenReturn(task);
        
        // Act
        Response response = taskResource.getTaskById(taskId);
        
        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(task, response.getEntity());
        verify(taskDAO, times(1)).getTaskById(taskId);
    }
    
    @Test
    public void testGetTaskByIdNotFound() throws SQLException {
        // Arrange
        Long taskId = 999L;
        when(taskDAO.getTaskById(taskId)).thenReturn(null);
        
        // Act
        Response response = taskResource.getTaskById(taskId);
        
        // Assert
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Tarea no encontrada"));
        verify(taskDAO, times(1)).getTaskById(taskId);
    }
    
    @Test
    public void testGetTaskByIdDatabaseError() throws SQLException {
        // Arrange
        Long taskId = 1L;
        when(taskDAO.getTaskById(taskId)).thenThrow(new SQLException("Database error"));
        
        // Act
        Response response = taskResource.getTaskById(taskId);
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al obtener la tarea"));
        verify(taskDAO, times(1)).getTaskById(taskId);
    }
    
    @Test
    public void testCreateTaskSuccess() throws SQLException {
        // Arrange
        Task inputTask = new Task();
        inputTask.setTitle("New Task");
        inputTask.setDescription("New Description");
        
        Task createdTask = new Task(1L, "New Task", "New Description", false, new Date(), new Date());
        
        when(taskDAO.createTask(any(Task.class))).thenReturn(createdTask);
        
        // Act
        Response response = taskResource.createTask(inputTask);
        
        // Assert
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(createdTask, response.getEntity());
        verify(taskDAO, times(1)).createTask(inputTask);
    }
    
    @Test
    public void testCreateTaskMissingTitle() throws SQLException {
        // Arrange
        Task inputTask = new Task();
        inputTask.setTitle(""); // Empty title
        inputTask.setDescription("New Description");
        
        // Act
        Response response = taskResource.createTask(inputTask);
        
        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("El título es requerido"));
        verify(taskDAO, never()).createTask(any(Task.class));
    }
    
    @Test
    public void testCreateTaskNullTitle() throws SQLException {
        // Arrange
        Task inputTask = new Task();
        inputTask.setTitle(null); // Null title
        inputTask.setDescription("New Description");
        
        // Act
        Response response = taskResource.createTask(inputTask);
        
        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("El título es requerido"));
        verify(taskDAO, never()).createTask(any(Task.class));
    }
    
    @Test
    public void testCreateTaskDatabaseError() throws SQLException {
        // Arrange
        Task inputTask = new Task();
        inputTask.setTitle("New Task");
        inputTask.setDescription("New Description");
        
        when(taskDAO.createTask(any(Task.class))).thenThrow(new SQLException("Database error"));
        
        // Act
        Response response = taskResource.createTask(inputTask);
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al crear la tarea"));
        verify(taskDAO, times(1)).createTask(inputTask);
    }
    
    @Test
    public void testUpdateTaskSuccess() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task inputTask = new Task();
        inputTask.setTitle("Updated Task");
        inputTask.setDescription("Updated Description");
        inputTask.setCompleted(true);
        
        Task existingTask = new Task(taskId, "Old Task", "Old Description", false, new Date(), new Date());
        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description", true, new Date(), new Date());
        
        when(taskDAO.getTaskById(taskId)).thenReturn(existingTask);
        when(taskDAO.updateTask(eq(taskId), any(Task.class))).thenReturn(updatedTask);
        
        // Act
        Response response = taskResource.updateTask(taskId, inputTask);
        
        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
        assertEquals(updatedTask, response.getEntity());
        verify(taskDAO, times(1)).getTaskById(taskId);
        verify(taskDAO, times(1)).updateTask(eq(taskId), any(Task.class));
    }
    
    @Test
    public void testUpdateTaskNotFound() throws SQLException {
        // Arrange
        Long taskId = 999L;
        Task inputTask = new Task();
        inputTask.setTitle("Updated Task");
        inputTask.setDescription("Updated Description");
        
        when(taskDAO.getTaskById(taskId)).thenReturn(null);
        
        // Act
        Response response = taskResource.updateTask(taskId, inputTask);
        
        // Assert
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Tarea no encontrada"));
        verify(taskDAO, times(1)).getTaskById(taskId);
        verify(taskDAO, never()).updateTask(anyLong(), any(Task.class));
    }
    
    @Test
    public void testUpdateTaskMissingTitle() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task inputTask = new Task();
        inputTask.setTitle(""); // Empty title
        inputTask.setDescription("Updated Description");
        
        Task existingTask = new Task(taskId, "Old Task", "Old Description", false, new Date(), new Date());
        
        when(taskDAO.getTaskById(taskId)).thenReturn(existingTask);
        
        // Act
        Response response = taskResource.updateTask(taskId, inputTask);
        
        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("El título es requerido"));
        verify(taskDAO, times(1)).getTaskById(taskId);
        verify(taskDAO, never()).updateTask(anyLong(), any(Task.class));
    }
    
    @Test
    public void testUpdateTaskDatabaseError() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task inputTask = new Task();
        inputTask.setTitle("Updated Task");
        inputTask.setDescription("Updated Description");
        
        Task existingTask = new Task(taskId, "Old Task", "Old Description", false, new Date(), new Date());
        
        when(taskDAO.getTaskById(taskId)).thenReturn(existingTask);
        when(taskDAO.updateTask(eq(taskId), any(Task.class))).thenThrow(new SQLException("Database error"));
        
        // Act
        Response response = taskResource.updateTask(taskId, inputTask);
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al actualizar la tarea"));
        verify(taskDAO, times(1)).getTaskById(taskId);
        verify(taskDAO, times(1)).updateTask(eq(taskId), any(Task.class));
    }
    
    @Test
    public void testDeleteTaskSuccess() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task existingTask = new Task(taskId, "Task to delete", "Description", false, new Date(), new Date());
        
        when(taskDAO.getTaskById(taskId)).thenReturn(existingTask);
        when(taskDAO.deleteTask(taskId)).thenReturn(true);
        
        // Act
        Response response = taskResource.deleteTask(taskId);
        
        // Assert
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
        verify(taskDAO, times(1)).getTaskById(taskId);
        verify(taskDAO, times(1)).deleteTask(taskId);
    }
    
    @Test
    public void testDeleteTaskNotFound() throws SQLException {
        // Arrange
        Long taskId = 999L;
        when(taskDAO.getTaskById(taskId)).thenReturn(null);
        
        // Act
        Response response = taskResource.deleteTask(taskId);
        
        // Assert
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Tarea no encontrada"));
        verify(taskDAO, times(1)).getTaskById(taskId);
        verify(taskDAO, never()).deleteTask(anyLong());
    }
    
    @Test
    public void testDeleteTaskDatabaseError() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task existingTask = new Task(taskId, "Task to delete", "Description", false, new Date(), new Date());
        
        when(taskDAO.getTaskById(taskId)).thenReturn(existingTask);
        when(taskDAO.deleteTask(taskId)).thenThrow(new SQLException("Database error"));
        
        // Act
        Response response = taskResource.deleteTask(taskId);
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al eliminar la tarea"));
        verify(taskDAO, times(1)).getTaskById(taskId);
        verify(taskDAO, times(1)).deleteTask(taskId);
    }
    
    @Test
    public void testDeleteTaskReturnsFalse() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task existingTask = new Task(taskId, "Task to delete", "Description", false, new Date(), new Date());
        
        when(taskDAO.getTaskById(taskId)).thenReturn(existingTask);
        when(taskDAO.deleteTask(taskId)).thenReturn(false);
        
        // Act
        Response response = taskResource.deleteTask(taskId);
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error al eliminar la tarea"));
        verify(taskDAO, times(1)).getTaskById(taskId);
        verify(taskDAO, times(1)).deleteTask(taskId);
    }
    
    @Test
    public void testTestServiceSuccess() throws SQLException {
        // Arrange
        TaskResource spyResource = spy(taskResource);
        TaskDAO mockTaskDAO = mock(TaskDAO.class);
        
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field field = TaskResource.class.getDeclaredField("taskDAO");
            field.setAccessible(true);
            field.set(spyResource, mockTaskDAO);
        } catch (Exception e) {
            fail("Failed to set private field: " + e.getMessage());
        }
        
        // Act
        Response response = spyResource.testService();
        
        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Servicio probado exitosamente"));
    }
    
    @Test
    public void testTestServiceWithException() throws SQLException {
        // Arrange
        TaskResource spyResource = spy(taskResource);
        TaskDAO mockTaskDAO = mock(TaskDAO.class);
        
        // Use reflection to set the private field
        try {
            java.lang.reflect.Field field = TaskResource.class.getDeclaredField("taskDAO");
            field.setAccessible(true);
            field.set(spyResource, mockTaskDAO);
        } catch (Exception e) {
            fail("Failed to set private field: " + e.getMessage());
        }
        
        // Mock testPLSQLPackage to throw exception
        doThrow(new RuntimeException("Test error")).when(mockTaskDAO).testPLSQLPackage();
        
        // Act
        Response response = spyResource.testService();
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity().toString().contains("Error en la prueba"));
    }
    
    @Test
    public void testTaskResourceConstructor() {
        // This test verifies that the TaskResource can be instantiated
        TaskResource resource = new TaskResource();
        assertNotNull(resource);
    }
}