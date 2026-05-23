package com.taskmanager.config;

import com.taskmanager.rest.TaskResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {
    
    public ApplicationConfig() {
        // Registrar recursos REST
        register(TaskResource.class);
        
        // Configurar propiedades del servidor
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
        
        // Habilitar CORS
        register(CorsFilter.class);
        
        // Configurar para devolver JSON por defecto
        property(ServerProperties.MEDIA_TYPE_MAPPINGS, "json:application/json");
        
        // Configurar para manejar excepciones
        register(GenericExceptionMapper.class);
        
        System.out.println("Aplicación REST configurada en /api");
    }
}