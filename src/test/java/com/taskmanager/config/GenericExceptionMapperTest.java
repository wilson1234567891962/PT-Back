package com.taskmanager.config;

import org.junit.Test;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class GenericExceptionMapperTest {

    private final GenericExceptionMapper mapper = new GenericExceptionMapper();
    
    @Test
    public void testToResponseSQLException() {
        // Arrange
        SQLException exception = new SQLException("Database connection failed");
        
        // Act
        Response response = mapper.toResponse(exception);
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        
        GenericExceptionMapper.ErrorResponse errorResponse = (GenericExceptionMapper.ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getMessage().contains("Error de base de datos"));
        assertTrue(errorResponse.getMessage().contains("Database connection failed"));
        assertTrue(errorResponse.getTimestamp() > 0);
    }
    
    @Test
    public void testToResponseIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid parameter");
        
        // Act
        Response response = mapper.toResponse(exception);
        
        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        
        GenericExceptionMapper.ErrorResponse errorResponse = (GenericExceptionMapper.ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getMessage().contains("Parámetros inválidos"));
        assertTrue(errorResponse.getMessage().contains("Invalid parameter"));
        assertTrue(errorResponse.getTimestamp() > 0);
    }
    
    @Test
    public void testToResponseNotFoundException() {
        // Arrange
        NotFoundException exception = new NotFoundException("Resource not found");
        
        // Act
        Response response = mapper.toResponse(exception);
        
        // Assert
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        
        GenericExceptionMapper.ErrorResponse errorResponse = (GenericExceptionMapper.ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getMessage().contains("Recurso no encontrado"));
        assertTrue(errorResponse.getMessage().contains("Resource not found"));
        assertTrue(errorResponse.getTimestamp() > 0);
    }
    
    @Test
    public void testToResponseGenericException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Generic error");
        
        // Act
        Response response = mapper.toResponse(exception);
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        
        GenericExceptionMapper.ErrorResponse errorResponse = (GenericExceptionMapper.ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getMessage().contains("Error interno del servidor"));
        assertTrue(errorResponse.getMessage().contains("Generic error"));
        assertTrue(errorResponse.getTimestamp() > 0);
    }
    
    @Test
    public void testToResponseNullException() {
        // Arrange
        NullPointerException exception = new NullPointerException("Null pointer");
        
        // Act
        Response response = mapper.toResponse(exception);
        
        // Assert
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
        
        GenericExceptionMapper.ErrorResponse errorResponse = (GenericExceptionMapper.ErrorResponse) response.getEntity();
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getMessage().contains("Error interno del servidor"));
        assertTrue(errorResponse.getMessage().contains("Null pointer"));
        assertTrue(errorResponse.getTimestamp() > 0);
    }
    
    @Test
    public void testErrorResponseConstructorAndGetters() {
        // Arrange
        String message = "Test error message";
        
        // Act
        GenericExceptionMapper.ErrorResponse errorResponse = new GenericExceptionMapper.ErrorResponse(message);
        
        // Assert
        assertEquals(message, errorResponse.getMessage());
        assertTrue(errorResponse.getTimestamp() > 0);
    }
    
    @Test
    public void testErrorResponseSetters() {
        // Arrange
        GenericExceptionMapper.ErrorResponse errorResponse = new GenericExceptionMapper.ErrorResponse("Initial message");
        String newMessage = "Updated message";
        long newTimestamp = 1234567890L;
        
        // Act
        errorResponse.setMessage(newMessage);
        errorResponse.setTimestamp(newTimestamp);
        
        // Assert
        assertEquals(newMessage, errorResponse.getMessage());
        assertEquals(newTimestamp, errorResponse.getTimestamp());
    }
    
    @Test
    public void testErrorResponseToString() {
        // Arrange
        GenericExceptionMapper.ErrorResponse errorResponse = new GenericExceptionMapper.ErrorResponse("Test error");
        
        // Act
        String result = errorResponse.toString();
        
        // Assert
        assertNotNull(result);
        // Note: We're not testing the exact format since toString() might change
    }
}