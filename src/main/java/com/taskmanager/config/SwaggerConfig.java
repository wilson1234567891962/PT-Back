package com.taskmanager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

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
public class SwaggerConfig {
    // Esta clase solo contiene anotaciones de OpenAPI
    // La configuración principal está en ApplicationConfig.java
}