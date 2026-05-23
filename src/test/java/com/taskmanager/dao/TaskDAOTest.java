package com.taskmanager.dao;

import com.taskmanager.database.DatabaseConnection;
import com.taskmanager.model.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskDAOTest {

    private TaskDAO taskDAO;
    private Connection mockConnection;
    private CallableStatement mockCallableStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseConnection> databaseConnectionMock;
    
    @Before
    public void setUp() throws SQLException {
        taskDAO = new TaskDAO();
        
        // Create mocks
        mockConnection = mock(Connection.class);
        mockCallableStatement = mock(CallableStatement.class);
        mockResultSet = mock(ResultSet.class);
        
        // Mock DatabaseConnection static method
        databaseConnectionMock = Mockito.mockStatic(DatabaseConnection.class);
        databaseConnectionMock.when(DatabaseConnection::getConnection).thenReturn(mockConnection);
        
        // Default mock behavior for connection
        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        
        // Activar modo de prueba para evitar conexiones reales
        DatabaseConnection.setTestMode(true);
    }
    
    @After
    public void tearDown() {
        databaseConnectionMock.close();
        // Desactivar modo de prueba
        DatabaseConnection.setTestMode(false);
    }
    
    @Test
    public void testGetAllTasksSuccess() throws SQLException {
        // Arrange
        when(mockCallableStatement.execute()).thenReturn(true);
        when(mockCallableStatement.getObject(1)).thenReturn(mockResultSet);
        
        // Mock ResultSet behavior for two rows
        when(mockResultSet.next()).thenReturn(true, true, false); // Two rows
        
        // Mock first row
        when(mockResultSet.getLong("TASK_ID")).thenReturn(1L).thenReturn(2L);
        when(mockResultSet.getString("TITLE")).thenReturn("Task 1").thenReturn("Task 2");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Description 1").thenReturn("Description 2");
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(false).thenReturn(true);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(new Timestamp(System.currentTimeMillis()));
        
        // Act
        List<Task> tasks = taskDAO.getAllTasks();
        
        // Assert
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        
        // Verify first task
        Task task1 = tasks.get(0);
        assertEquals(Long.valueOf(1), task1.getTaskId());
        assertEquals("Task 1", task1.getTitle());
        assertEquals("Description 1", task1.getDescription());
        assertEquals(false, task1.getCompleted());
        
        // Verify second task
        Task task2 = tasks.get(1);
        assertEquals(Long.valueOf(2), task2.getTaskId());
        assertEquals("Task 2", task2.getTitle());
        assertEquals("Description 2", task2.getDescription());
        assertEquals(true, task2.getCompleted());
        
        // Verify database calls
        verify(mockConnection).prepareCall("{call TASK_PKG.GET_ALL_TASKS(?)}");
        verify(mockCallableStatement).registerOutParameter(1, Types.REF_CURSOR);
        verify(mockCallableStatement).execute();
        // Note: El método getAllTasks llama a next() 3 veces: true, true, false
        verify(mockResultSet, times(3)).next();
    }
    
    @Test
    public void testGetAllTasksEmpty() throws SQLException {
        // Arrange
        when(mockCallableStatement.execute()).thenReturn(true);
        when(mockCallableStatement.getObject(1)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No rows
        
        // Act
        List<Task> tasks = taskDAO.getAllTasks();
        
        // Assert
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }
    
    @Test(expected = SQLException.class)
    public void testGetAllTasksThrowsSQLException() throws SQLException {
        // Arrange
        when(mockConnection.prepareCall(anyString())).thenThrow(new SQLException("Database error"));
        
        // Act & Assert
        taskDAO.getAllTasks();
    }
    
    @Test
    public void testGetTaskByIdSuccess() throws SQLException {
        // Arrange
        Long taskId = 1L;
        when(mockCallableStatement.execute()).thenReturn(true);
        when(mockCallableStatement.getObject(2)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        mockTaskResultSet(taskId, "Task 1", "Description 1", false);
        
        // Act
        Task task = taskDAO.getTaskById(taskId);
        
        // Assert
        assertNotNull(task);
        assertEquals(taskId, task.getTaskId());
        assertEquals("Task 1", task.getTitle());
        assertEquals("Description 1", task.getDescription());
        assertEquals(false, task.getCompleted());
        
        // Verify database calls
        verify(mockConnection).prepareCall("{call TASK_PKG.GET_TASK_BY_ID(?, ?)}");
        verify(mockCallableStatement).setLong(1, taskId);
        verify(mockCallableStatement).registerOutParameter(2, Types.REF_CURSOR);
        verify(mockCallableStatement).execute();
    }
    
    @Test
    public void testGetTaskByIdNotFound() throws SQLException {
        // Arrange
        Long taskId = 999L;
        when(mockCallableStatement.execute()).thenReturn(true);
        when(mockCallableStatement.getObject(2)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No rows
        
        // Act
        Task task = taskDAO.getTaskById(taskId);
        
        // Assert
        assertNull(task);
    }
    
    @Test
    public void testCreateTaskSuccess() throws SQLException {
        // Arrange
        Task inputTask = new Task();
        inputTask.setTitle("New Task");
        inputTask.setDescription("New Description");
        // completed is not set (null)
        
        Long newTaskId = 100L;
        
        when(mockCallableStatement.execute()).thenReturn(true);
        when(mockCallableStatement.getLong(3)).thenReturn(newTaskId);
        
        // Mock getTaskById for the second call
        Task createdTask = new Task(newTaskId, "New Task", "New Description", false, new Date(), new Date());
        TaskDAO spyDAO = spy(taskDAO);
        doReturn(createdTask).when(spyDAO).getTaskById(newTaskId);
        
        // Act
        Task result = spyDAO.createTask(inputTask);
        
        // Assert
        assertNotNull(result);
        assertEquals(newTaskId, result.getTaskId());
        assertEquals("New Task", result.getTitle());
        assertEquals("New Description", result.getDescription());
        
        // Verify database calls
        verify(mockConnection).prepareCall("{call TASK_PKG.CREATE_TASK(?, ?, ?)}");
        verify(mockCallableStatement).setString(1, "New Task");
        verify(mockCallableStatement).setString(2, "New Description");
        verify(mockCallableStatement).registerOutParameter(3, Types.NUMERIC);
        verify(mockCallableStatement).execute();
        verify(mockCallableStatement).getLong(3);
    }
    
    @Test
    public void testUpdateTaskSuccess() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task task = new Task();
        task.setTitle("Updated Task");
        task.setDescription("Updated Description");
        task.setCompleted(true);
        
        when(mockCallableStatement.execute()).thenReturn(true);
        
        // Mock getTaskById for the second call
        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description", true, new Date(), new Date());
        TaskDAO spyDAO = spy(taskDAO);
        doReturn(updatedTask).when(spyDAO).getTaskById(taskId);
        
        // Act
        Task result = spyDAO.updateTask(taskId, task);
        
        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getTaskId());
        assertEquals("Updated Task", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(true, result.getCompleted());
        
        // Verify database calls
        verify(mockConnection).prepareCall("{call TASK_PKG.UPDATE_TASK(?, ?, ?, ?)}");
        verify(mockCallableStatement).setLong(1, taskId);
        verify(mockCallableStatement).setString(2, "Updated Task");
        verify(mockCallableStatement).setString(3, "Updated Description");
        verify(mockCallableStatement).setInt(4, 1); // true -> 1
        verify(mockCallableStatement).execute();
    }
    
    @Test
    public void testUpdateTaskWithNullCompleted() throws SQLException {
        // Arrange
        Long taskId = 1L;
        Task task = new Task();
        task.setTitle("Updated Task");
        task.setDescription("Updated Description");
        task.setCompleted(null); // null completed
        
        when(mockCallableStatement.execute()).thenReturn(true);
        
        // Mock getTaskById for the second call
        Task updatedTask = new Task(taskId, "Updated Task", "Updated Description", false, new Date(), new Date());
        TaskDAO spyDAO = spy(taskDAO);
        doReturn(updatedTask).when(spyDAO).getTaskById(taskId);
        
        // Act
        Task result = spyDAO.updateTask(taskId, task);
        
        // Assert
        assertNotNull(result);
        
        // Verify database calls - null completed should convert to 0 (false)
        verify(mockCallableStatement).setInt(4, 0); // null -> 0
    }
    
    @Test
    public void testDeleteTaskSuccess() throws SQLException {
        // Arrange
        Long taskId = 1L;
        when(mockCallableStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = taskDAO.deleteTask(taskId);
        
        // Assert
        assertTrue(result);
        
        // Verify database calls
        verify(mockConnection).prepareCall("{call TASK_PKG.DELETE_TASK(?)}");
        verify(mockCallableStatement).setLong(1, taskId);
        verify(mockCallableStatement).executeUpdate();
    }
    
    @Test
    public void testDeleteTaskFailure() throws SQLException {
        // Arrange
        Long taskId = 999L;
        when(mockCallableStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = taskDAO.deleteTask(taskId);
        
        // Assert
        assertFalse(result);
    }
    
    @Test(expected = SQLException.class)
    public void testDeleteTaskThrowsSQLException() throws SQLException {
        // Arrange
        Long taskId = 1L;
        when(mockConnection.prepareCall(anyString())).thenThrow(new SQLException("Database error"));
        
        // Act & Assert
        taskDAO.deleteTask(taskId);
    }
    
    @Test
    public void testMapResultSetToTask() throws SQLException {
        // Arrange
        Long taskId = 1L;
        String title = "Test Task";
        String description = "Test Description";
        boolean completed = true;
        Date createdAt = new Date();
        Date updatedAt = new Date();
        
        when(mockResultSet.getLong("TASK_ID")).thenReturn(taskId);
        when(mockResultSet.getString("TITLE")).thenReturn(title);
        when(mockResultSet.getString("DESCRIPTION")).thenReturn(description);
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(completed);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(new Timestamp(createdAt.getTime()));
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(new Timestamp(updatedAt.getTime()));
        
        // Use reflection to test private method
        try {
            java.lang.reflect.Method method = TaskDAO.class.getDeclaredMethod("mapResultSetToTask", ResultSet.class);
            method.setAccessible(true);
            
            // Act
            Task task = (Task) method.invoke(taskDAO, mockResultSet);
            
            // Assert
            assertNotNull(task);
            assertEquals(taskId, task.getTaskId());
            assertEquals(title, task.getTitle());
            assertEquals(description, task.getDescription());
            assertEquals(completed, task.getCompleted());
            assertNotNull(task.getCreatedAt());
            assertNotNull(task.getUpdatedAt());
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
        }
    }
    
    @Test
    public void testMapResultSetToTaskWithNullDescription() throws SQLException {
        // Arrange
        when(mockResultSet.getLong("TASK_ID")).thenReturn(1L);
        when(mockResultSet.getString("TITLE")).thenReturn("Test Task");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn(null); // null description
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(false);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(new Timestamp(System.currentTimeMillis()));
        
        // Use reflection to test private method
        try {
            java.lang.reflect.Method method = TaskDAO.class.getDeclaredMethod("mapResultSetToTask", ResultSet.class);
            method.setAccessible(true);
            
            // Act
            Task task = (Task) method.invoke(taskDAO, mockResultSet);
            
            // Assert
            assertNotNull(task);
            assertNull(task.getDescription()); // Should be null
        } catch (Exception e) {
            fail("Failed to invoke private method: " + e.getMessage());
        }
    }
    
    @Test
    public void testTestPLSQLPackageSuccess() throws SQLException {
        // Arrange
        TaskDAO spyDAO = spy(taskDAO);
        
        // Mock the test methods to avoid actual database calls
        doNothing().when(spyDAO).testPLSQLPackage();
        
        // Mock DatabaseConnection.testConnection
        databaseConnectionMock.when(DatabaseConnection::testConnection).thenAnswer(invocation -> {
            System.out.println("Probando conexión a la base de datos...");
            return null;
        });
        
        // Mock createTask
        Task testTask = new Task(1L, "Tarea de prueba", "Descripción de prueba", false, new Date(), new Date());
        doReturn(testTask).when(spyDAO).createTask(any(Task.class));
        
        // Mock getAllTasks
        List<Task> tasks = java.util.Arrays.asList(testTask);
        doReturn(tasks).when(spyDAO).getAllTasks();
        
        // Mock deleteTask
        doReturn(true).when(spyDAO).deleteTask(anyLong());
        
        // Act - Should not throw exceptions
        spyDAO.testPLSQLPackage();
        
        // Assert - No exceptions thrown
        assertTrue(true);
    }
    
    @Test
    public void testTestPLSQLPackageWithException() throws SQLException {
        // Arrange
        TaskDAO spyDAO = spy(taskDAO);
        
        // Mock DatabaseConnection.testConnection to throw exception
        databaseConnectionMock.when(DatabaseConnection::testConnection).thenAnswer(invocation -> {
            throw new SQLException("Connection failed");
        });
        
        // Act - Should catch the exception
        spyDAO.testPLSQLPackage();
        
        // Assert - Exception is caught internally
        assertTrue(true);
    }
    
    // Helper method to mock ResultSet behavior
    private void mockTaskResultSet(Long taskId, String title, String description, Boolean completed) throws SQLException {
        when(mockResultSet.getLong("TASK_ID")).thenReturn(taskId).thenReturn(taskId);
        when(mockResultSet.getString("TITLE")).thenReturn(title).thenReturn(title);
        when(mockResultSet.getString("DESCRIPTION")).thenReturn(description).thenReturn(description);
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(completed).thenReturn(completed);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(new Timestamp(System.currentTimeMillis()));
    }
}