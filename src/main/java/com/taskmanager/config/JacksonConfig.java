package com.taskmanager.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * Configuración personalizada de Jackson para la aplicación
 */
@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
    
    private final ObjectMapper objectMapper;
    
    public JacksonConfig() {
        objectMapper = new ObjectMapper();
        
        // Configurar para ignorar propiedades desconocidas
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Configurar para aceptar números como booleanos (0=false, 1=true)
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        // Configurar para manejar fechas Java 8
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // Configurar para ser tolerante con formatos de entrada
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }
    
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}