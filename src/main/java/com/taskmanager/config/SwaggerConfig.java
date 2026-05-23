package com.taskmanager.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@OpenAPIDefinition(
    info = @Info(
        title = "Task Manager API",
        version = "1.0.0",
        description = "API REST para gestión de tareas con Oracle PL/SQL y Java",
        contact = @Contact(
            name = "Task Manager Team",
            email = "support@taskmanager.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            description = "Servidor de desarrollo",
            url = "http://localhost:8080/task-api/api"
        )
    }
)
@ApplicationPath("api")
public class SwaggerConfig extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        
        // Agregar recursos de Swagger
        resources.add(OpenApiResource.class);
        
        return resources;
    }
}