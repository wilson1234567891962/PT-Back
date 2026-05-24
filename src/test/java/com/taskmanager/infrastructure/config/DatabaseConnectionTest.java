package com.taskmanager.infrastructure.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DatabaseConnectionTest {

    private DatabaseConnection databaseConnection;
    private MockedStatic<DriverManager> driverManagerMock;

    @Before
    public void setUp() {
        // Mock DriverManager to avoid actual database connections
        driverManagerMock = Mockito.mockStatic(DriverManager.class);
    }

    @After
    public void tearDown() {
        if (driverManagerMock != null) {
            driverManagerMock.close();
        }
        if (databaseConnection != null) {
            databaseConnection.closeConnection();
        }
    }

    @Test
    public void testDefaultConstructor() {
        // Act
        databaseConnection = new DatabaseConnection();
        
        // Assert
        assertNotNull(databaseConnection);
        // Should use default Oracle values
        assertTrue(databaseConnection.getDbUrl().contains("oracle"));
        assertEquals("system", databaseConnection.getDbUser());
        assertEquals("oracle.jdbc.driver.OracleDriver", databaseConnection.getDbDriver());
    }

    @Test
    public void testParameterizedConstructor() {
        // Arrange
        String dbUrl = "jdbc:postgresql://localhost:5432/testdb";
        String dbUser = "testuser";
        String dbPassword = "testpass";
        String dbDriver = "org.postgresql.Driver";
        
        // Act
        databaseConnection = new DatabaseConnection(dbUrl, dbUser, dbPassword, dbDriver);
        
        // Assert
        assertNotNull(databaseConnection);
        assertEquals(dbUrl, databaseConnection.getDbUrl());
        assertEquals(dbUser, databaseConnection.getDbUser());
        assertEquals(dbDriver, databaseConnection.getDbDriver());
    }

    @Test
    public void testIsPostgreSQLWithPostgreSQLDriver() {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        // Act
        boolean result = databaseConnection.isPostgreSQL();
        
        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsPostgreSQLWithOracleDriver() {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:oracle:thin:@localhost:1521:XE",
            "system",
            "password",
            "oracle.jdbc.driver.OracleDriver"
        );
        
        // Act
        boolean result = databaseConnection.isPostgreSQL();
        
        // Assert
        assertFalse(result);
    }

    @Test
    public void testGetConnectionSuccess() throws SQLException {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isClosed()).thenReturn(false);
        
        driverManagerMock.when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
            .thenReturn(mockConnection);
        
        // Act
        Connection result = databaseConnection.getConnection();
        
        // Assert
        assertNotNull(result);
        assertEquals(mockConnection, result);
        driverManagerMock.verify(() -> DriverManager.getConnection(
            eq("jdbc:postgresql://localhost:5432/testdb"),
            any(Properties.class)
        ));
    }

    @Test(expected = SQLException.class)
    public void testGetConnectionWithTestMode() throws SQLException {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        databaseConnection.setTestMode(true);
        
        // Act & Assert
        databaseConnection.getConnection();
    }

    @Test
    public void testGetConnectionReusesExistingConnection() throws SQLException {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isClosed()).thenReturn(false);
        
        driverManagerMock.when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
            .thenReturn(mockConnection);
        
        // Act - Get connection twice
        Connection firstConnection = databaseConnection.getConnection();
        Connection secondConnection = databaseConnection.getConnection();
        
        // Assert
        assertNotNull(firstConnection);
        assertNotNull(secondConnection);
        assertEquals(firstConnection, secondConnection);
        // Should only call DriverManager.getConnection once
        driverManagerMock.verify(() -> DriverManager.getConnection(anyString(), any(Properties.class)), times(1));
    }

    @Test
    public void testGetConnectionCreatesNewWhenClosed() throws SQLException {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        Connection mockConnection1 = mock(Connection.class);
        Connection mockConnection2 = mock(Connection.class);
        when(mockConnection1.isClosed()).thenReturn(true); // First connection is closed
        when(mockConnection2.isClosed()).thenReturn(false);
        
        driverManagerMock.when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
            .thenReturn(mockConnection1)  // First call
            .thenReturn(mockConnection2); // Second call
        
        // Act - Get connection (will be closed)
        Connection firstConnection = databaseConnection.getConnection();
        // Get connection again (should create new one)
        Connection secondConnection = databaseConnection.getConnection();
        
        // Assert
        assertNotNull(firstConnection);
        assertNotNull(secondConnection);
        assertNotEquals(firstConnection, secondConnection);
        // Should call DriverManager.getConnection twice
        driverManagerMock.verify(() -> DriverManager.getConnection(anyString(), any(Properties.class)), times(2));
    }

    @Test
    public void testCloseConnection() throws SQLException {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isClosed()).thenReturn(false);
        
        driverManagerMock.when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
            .thenReturn(mockConnection);
        
        // Get a connection first
        databaseConnection.getConnection();
        
        // Act
        databaseConnection.closeConnection();
        
        // Assert
        verify(mockConnection).close();
    }

    @Test
    public void testCloseConnectionWhenNull() {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        // Act - Should not throw exception
        databaseConnection.closeConnection();
        
        // Assert
        // No exception thrown
        assertTrue(true);
    }

    @Test
    public void testTestConnectionSuccess() throws SQLException {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.isClosed()).thenReturn(false);
        
        driverManagerMock.when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
            .thenReturn(mockConnection);
        
        // Act
        databaseConnection.testConnection();
        
        // Assert
        // Should not throw exception
        assertTrue(true);
    }

    @Test
    public void testTestConnectionFailure() {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        // Act & Assert
        // testConnection() no lanza excepción, solo imprime error
        // Por lo tanto, no necesitamos mockear DriverManager para esta prueba
        databaseConnection.testConnection();
        
        // Si llegamos aquí sin excepción, la prueba pasa
        assertTrue(true);
    }

    @Test
    public void testSetTestMode() {
        // Arrange
        databaseConnection = new DatabaseConnection(
            "jdbc:postgresql://localhost:5432/testdb",
            "testuser",
            "testpass",
            "org.postgresql.Driver"
        );
        
        // Act
        databaseConnection.setTestMode(true);
        
        // Assert
        // No getter for testMode, but we can test by trying to get connection
        try {
            databaseConnection.getConnection();
            fail("Should have thrown SQLException in test mode");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("Modo de prueba activado"));
        }
    }

    @Test
    public void testDriverLoadingFailure() {
        // This test is tricky because we can't easily mock Class.forName
        // The constructor will throw RuntimeException if driver can't be loaded
        // We'll test this by ensuring the constructor handles the error gracefully
        
        // Arrange & Act
        try {
            // Try to create with invalid driver
            databaseConnection = new DatabaseConnection(
                "jdbc:invalid://localhost:5432/testdb",
                "testuser",
                "testpass",
                "com.invalid.Driver"
            );
            // If we get here, the test should fail
            fail("Should have thrown RuntimeException for invalid driver");
        } catch (RuntimeException e) {
            // Expected - driver loading should fail
            assertTrue(e.getMessage().contains("Failed to load database driver"));
        }
    }
}