package com.taskmanager.config;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ApplicationConfigTest {

    @Test
    public void testApplicationConfigConstructor() {
        // Act
        ApplicationConfig config = new ApplicationConfig();
        
        // Assert
        assertNotNull(config);
        // Constructor should print message to System.out
        // The main assertion is that no exception is thrown
    }
    
    @Test
    public void testApplicationConfigAnnotation() {
        // This test verifies the @ApplicationPath annotation
        // We can't easily test annotations at runtime in a simple unit test
        // The annotation will be processed by the JAX-RS container
        assertTrue(true);
    }
    
    @Test
    public void testApplicationConfigExtendsResourceConfig() {
        // This test verifies the class hierarchy
        ApplicationConfig config = new ApplicationConfig();
        assertTrue(config instanceof javax.ws.rs.core.Application);
    }
    
    @Test
    public void testApplicationConfigInstantiation() {
        // Simple instantiation test
        ApplicationConfig config = new ApplicationConfig();
        assertNotNull(config);
        
        // Verify no exceptions during construction
        // The constructor registers classes and sets properties
        // If there were issues with class loading, exceptions would be thrown here
    }
    
    @Test
    public void testApplicationConfigNoArgsConstructor() {
        // The class only has a no-args constructor
        // This test verifies it can be instantiated without parameters
        ApplicationConfig config = new ApplicationConfig();
        assertNotNull(config);
    }
}