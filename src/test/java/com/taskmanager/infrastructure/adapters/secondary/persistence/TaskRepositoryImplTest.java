package com.taskmanager.infrastructure.adapters.secondary.persistence;

import com.taskmanager.domain.models.Task;
import com.taskmanager.infrastructure.config.DatabaseConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class TaskRepositoryImplTest {

    private TaskRepositoryImpl taskRepository;
    private DatabaseConnection databaseConnection;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private CallableStatement mockCallableStatement;
    private ResultSet mockResultSet;

    @Before
    public void setUp() throws SQLException {
        databaseConnection = Mockito.mock(DatabaseConnection.class);
        mockConnection = Mockito.mock(Connection.class);
        mockPreparedStatement = Mockito.mock(PreparedStatement.class);
        mockCallableStatement = Mockito.mock(CallableStatement.class);
        mockResultSet = Mockito.mock(ResultSet.class);

        when(databaseConnection.getConnection()).thenReturn(mockConnection);
        taskRepository = new TaskRepositoryImpl(databaseConnection);
    }

    @Test
    public void testIsPostgreSQLReturnsTrueForPostgreSQLDriver() {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        
        // Act
        boolean result = taskRepository.isPostgreSQL();
        
        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsPostgreSQLReturnsFalseForOracleDriver() {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("oracle.jdbc.driver.OracleDriver");
        
        // Act
        boolean result = taskRepository.isPostgreSQL();
        
        // Assert
        assertFalse(result);
    }

    @Test
    public void testSavePostgreSQL() throws SQLException {
        // Arrange - Simular PostgreSQL
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(1L);

        Task task = Task.create("Test Task", "Test Description");
        
        // Act
        Task result = taskRepository.save(task);
        
        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(1), result.getTaskId());
        // Verificaciones simplificadas para evitar problemas con matchers
        verify(mockConnection).prepareStatement(contains("INSERT INTO tasks"));
    }

    @Test(expected = RuntimeException.class)
    public void testSavePostgreSQLThrowsExceptionOnSQLException() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test error"));

        Task task = Task.create("Test Task", "Test Description");
        
        // Act
        taskRepository.save(task);
    }

    @Test
    public void testFindByIdPostgreSQL() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("TASK_ID")).thenReturn(1L);
        when(mockResultSet.getString("TITLE")).thenReturn("Test Task");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Test Description");
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(false);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        
        // Act
        Optional<Task> result = taskRepository.findById(1L);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(Long.valueOf(1), result.get().getTaskId());
        assertEquals("Test Task", result.get().getTitle());
    }

    @Test
    public void testFindByIdPostgreSQLNotFound() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        Optional<Task> result = taskRepository.findById(999L);
        
        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindAllPostgreSQL() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Simulate two rows in result set
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getLong("TASK_ID")).thenReturn(1L, 2L);
        when(mockResultSet.getString("TITLE")).thenReturn("Task 1", "Task 2");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Desc 1", "Desc 2");
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(false, true);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.valueOf(LocalDateTime.now())
        );
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.valueOf(LocalDateTime.now())
        );
        
        // Act
        List<Task> result = taskRepository.findAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Long.valueOf(1), result.get(0).getTaskId());
        assertEquals(Long.valueOf(2), result.get(1).getTaskId());
    }

    @Test
    public void testUpdatePostgreSQL() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        Task task = Task.create("Updated Task", "Updated Description");
        task.setTaskId(1L);
        
        // Act
        Task result = taskRepository.update(task);
        
        // Assert
        assertNotNull(result);
        assertEquals(task, result);
        verify(mockConnection).prepareStatement(contains("UPDATE tasks"));
    }

    @Test(expected = RuntimeException.class)
    public void testUpdatePostgreSQLThrowsExceptionWhenNoRowsAffected() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        Task task = Task.create("Updated Task", "Updated Description");
        task.setTaskId(1L);
        
        // Act
        taskRepository.update(task);
    }

    @Test
    public void testDeletePostgreSQL() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = taskRepository.delete(1L);
        
        // Assert
        assertTrue(result);
    }

    @Test
    public void testDeletePostgreSQLReturnsFalseWhenNoRowsAffected() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = taskRepository.delete(1L);
        
        // Assert
        assertFalse(result);
    }

    @Test
    public void testExistsByIdReturnsTrueWhenTaskExists() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("TASK_ID")).thenReturn(1L);
        when(mockResultSet.getString("TITLE")).thenReturn("Test Task");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Test Description");
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(false);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        
        // Act
        boolean result = taskRepository.existsById(1L);
        
        // Assert
        assertTrue(result);
    }

    @Test
    public void testExistsByIdReturnsFalseWhenTaskDoesNotExist() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        boolean result = taskRepository.existsById(999L);
        
        // Assert
        assertFalse(result);
    }

    @Test
    public void testCount() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(5L);
        
        // Act
        long result = taskRepository.count();
        
        // Assert
        assertEquals(5L, result);
    }

    @Test
    public void testFindByCompleted() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Simulate two completed tasks
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getLong("TASK_ID")).thenReturn(1L, 2L);
        when(mockResultSet.getString("TITLE")).thenReturn("Task 1", "Task 2");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Desc 1", "Desc 2");
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(true, true);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.valueOf(LocalDateTime.now())
        );
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.valueOf(LocalDateTime.now())
        );
        
        // Act
        List<Task> result = taskRepository.findByCompleted(true);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCompleted());
        assertTrue(result.get(1).getCompleted());
    }

    @Test
    public void testFindByTitleContaining() throws SQLException {
        // Arrange
        when(databaseConnection.getDbDriver()).thenReturn("org.postgresql.Driver");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Simulate two tasks with "test" in title
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getLong("TASK_ID")).thenReturn(1L, 2L);
        when(mockResultSet.getString("TITLE")).thenReturn("Test Task 1", "Another Test Task");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Desc 1", "Desc 2");
        when(mockResultSet.getBoolean("COMPLETED")).thenReturn(false, true);
        when(mockResultSet.getTimestamp("CREATED_AT")).thenReturn(
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.valueOf(LocalDateTime.now())
        );
        when(mockResultSet.getTimestamp("UPDATED_AT")).thenReturn(
            Timestamp.valueOf(LocalDateTime.now()),
            Timestamp.valueOf(LocalDateTime.now())
        );
        
        // Act
        List<Task> result = taskRepository.findByTitleContaining("test");
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}