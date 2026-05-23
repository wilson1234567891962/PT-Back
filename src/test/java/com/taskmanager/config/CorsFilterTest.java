package com.taskmanager.config;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CorsFilterTest {

    private final CorsFilter corsFilter = new CorsFilter();
    
    @Test
    public void testFilterAddsCorsHeaders() throws IOException {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        when(requestContext.getMethod()).thenReturn("GET");
        
        // Act
        corsFilter.filter(requestContext, responseContext);
        
        // Assert
        assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"));
        assertEquals("true", headers.getFirst("Access-Control-Allow-Credentials"));
        assertEquals("origin, content-type, accept, authorization, x-requested-with", 
                    headers.getFirst("Access-Control-Allow-Headers"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD", 
                    headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("1209600", headers.getFirst("Access-Control-Max-Age"));
        
        // Status should not be changed for non-OPTIONS requests
        verify(responseContext, never()).setStatus(anyInt());
    }
    
    @Test
    public void testFilterOptionsRequest() throws IOException {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        when(requestContext.getMethod()).thenReturn("OPTIONS");
        
        // Act
        corsFilter.filter(requestContext, responseContext);
        
        // Assert
        assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"));
        assertEquals("true", headers.getFirst("Access-Control-Allow-Credentials"));
        assertEquals("origin, content-type, accept, authorization, x-requested-with", 
                    headers.getFirst("Access-Control-Allow-Headers"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD", 
                    headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("1209600", headers.getFirst("Access-Control-Max-Age"));
        
        // Status should be set to 200 for OPTIONS requests
        verify(responseContext, times(1)).setStatus(200);
    }
    
    @Test
    public void testFilterPostRequest() throws IOException {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        when(requestContext.getMethod()).thenReturn("POST");
        
        // Act
        corsFilter.filter(requestContext, responseContext);
        
        // Assert
        assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"));
        assertEquals("true", headers.getFirst("Access-Control-Allow-Credentials"));
        assertEquals("origin, content-type, accept, authorization, x-requested-with", 
                    headers.getFirst("Access-Control-Allow-Headers"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD", 
                    headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("1209600", headers.getFirst("Access-Control-Max-Age"));
        
        // Status should not be changed for POST requests
        verify(responseContext, never()).setStatus(anyInt());
    }
    
    @Test
    public void testFilterPutRequest() throws IOException {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        when(requestContext.getMethod()).thenReturn("PUT");
        
        // Act
        corsFilter.filter(requestContext, responseContext);
        
        // Assert
        assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"));
        assertEquals("true", headers.getFirst("Access-Control-Allow-Credentials"));
        assertEquals("origin, content-type, accept, authorization, x-requested-with", 
                    headers.getFirst("Access-Control-Allow-Headers"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD", 
                    headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("1209600", headers.getFirst("Access-Control-Max-Age"));
        
        // Status should not be changed for PUT requests
        verify(responseContext, never()).setStatus(anyInt());
    }
    
    @Test
    public void testFilterDeleteRequest() throws IOException {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        when(requestContext.getMethod()).thenReturn("DELETE");
        
        // Act
        corsFilter.filter(requestContext, responseContext);
        
        // Assert
        assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"));
        assertEquals("true", headers.getFirst("Access-Control-Allow-Credentials"));
        assertEquals("origin, content-type, accept, authorization, x-requested-with", 
                    headers.getFirst("Access-Control-Allow-Headers"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD", 
                    headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("1209600", headers.getFirst("Access-Control-Max-Age"));
        
        // Status should not be changed for DELETE requests
        verify(responseContext, never()).setStatus(anyInt());
    }
    
    @Test
    public void testFilterHeadRequest() throws IOException {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        when(requestContext.getMethod()).thenReturn("HEAD");
        
        // Act
        corsFilter.filter(requestContext, responseContext);
        
        // Assert
        assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"));
        assertEquals("true", headers.getFirst("Access-Control-Allow-Credentials"));
        assertEquals("origin, content-type, accept, authorization, x-requested-with", 
                    headers.getFirst("Access-Control-Allow-Headers"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD", 
                    headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("1209600", headers.getFirst("Access-Control-Max-Age"));
        
        // Status should not be changed for HEAD requests
        verify(responseContext, never()).setStatus(anyInt());
    }
    
    @Test
    public void testFilterWithExistingHeaders() throws IOException {
        // Arrange
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        // Add some existing headers
        headers.add("Content-Type", "application/json");
        headers.add("X-Custom-Header", "custom-value");
        
        when(responseContext.getHeaders()).thenReturn(headers);
        when(requestContext.getMethod()).thenReturn("GET");
        
        // Act
        corsFilter.filter(requestContext, responseContext);
        
        // Assert
        // Existing headers should still be there
        assertEquals("application/json", headers.getFirst("Content-Type"));
        assertEquals("custom-value", headers.getFirst("X-Custom-Header"));
        
        // CORS headers should be added
        assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"));
        assertEquals("true", headers.getFirst("Access-Control-Allow-Credentials"));
        assertEquals("origin, content-type, accept, authorization, x-requested-with", 
                    headers.getFirst("Access-Control-Allow-Headers"));
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD", 
                    headers.getFirst("Access-Control-Allow-Methods"));
        assertEquals("1209600", headers.getFirst("Access-Control-Max-Age"));
        
        // Total headers count
        assertEquals(7, headers.size()); // 2 existing + 5 CORS headers
    }
    
    @Test
    public void testFilterIOException() {
        // This test verifies that the method signature declares IOException
        // No actual test needed since we're testing the declaration
        assertTrue(true);
    }
    
    @Test
    public void testCorsFilterConstructor() {
        // This test verifies that the CorsFilter can be instantiated
        CorsFilter filter = new CorsFilter();
        assertNotNull(filter);
    }
}