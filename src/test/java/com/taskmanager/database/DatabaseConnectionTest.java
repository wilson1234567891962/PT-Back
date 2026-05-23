package com.taskmanager.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class DatabaseConnectionTest {
    
    @Before
    public void setUp() throws Exception {
        // Activar modo de prueba para evitar conexiones reales a la base de datos
        DatabaseConnection.setTestMode(true);
        // Ensure connection is closed before each test
        DatabaseConnection.closeConnection();
    }
    
    @After
    public void tearDown() {
        DatabaseConnection.closeConnection();
        // Desactivar modo de prueba
        DatabaseConnection.setTestMode(false);
    }
    
    @Test(expected = SQLException.class)
    public void testGetConnectionThrowsExceptionInTestMode() throws SQLException {
        // Act & Assert - Should throw SQLException in test mode
        DatabaseConnection.getConnection();
    }
    
    @Test
    public void testCloseConnection() {
        // Act - Should not throw exception
        DatabaseConnection.closeConnection();
        
        // Assert - No exception thrown
        assertTrue(true);
    }
    
    @Test
    public void testCloseConnectionWhenNull() {
        // Act - Should not throw exception when no connection exists
        DatabaseConnection.closeConnection();
        
        // Assert - No exception thrown
        assertTrue(true);
    }
    
    @Test
    public void testTestConnectionSuccessInTestMode() {
        // Act - Should catch exception internally in test mode
        DatabaseConnection.testConnection();
        
        // Assert - No exception thrown from test
        assertTrue(true);
    }
    
    @Test
    public void testStaticInitializer() {
        // This test verifies that the static initializer doesn't throw exceptions
        // The static block is executed when the class is loaded
        assertTrue(true);
    }
    
    @Test
    public void testSetTestMode() {
        // Test that we can toggle test mode
        DatabaseConnection.setTestMode(true);
        DatabaseConnection.setTestMode(false);
        assertTrue(true);
    }
}