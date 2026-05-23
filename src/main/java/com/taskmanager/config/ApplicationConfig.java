package com.taskmanager.config;

import com.taskmanager.rest.TaskResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.ApplicationPath;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationPath("/api")
public class ApplicationConfig extends ResourceConfig {
    
    public ApplicationConfig() {
        // Registrar recursos REST
        register(TaskResource.class);
        
        // Configurar Swagger/OpenAPI
        configureSwagger();
        
        // Configurar propiedades del servidor
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
        
        // Habilitar CORS
        register(CorsFilter.class);
        
        // Configurar para devolver JSON por defecto
        property(ServerProperties.MEDIA_TYPE_MAPPINGS, "json:application/json");
        
        // Configurar para manejar excepciones
        register(GenericExceptionMapper.class);
        
        System.out.println("Aplicación REST configurada en /api con Swagger");
    }
    
    private void configureSwagger() {
        // Configurar OpenAPI
        OpenAPI openAPI = new OpenAPI()
            .info(new Info()
                .title("Task Manager API")
                .description("API REST para gestión de tareas con Oracle PL/SQL y Java")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Task Manager Team")
                    .email("support@taskmanager.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")));
        
        // Configurar Swagger
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
            .openAPI(openAPI)
            .prettyPrint(true)
            .resourcePackages(Stream.of("com.taskmanager.rest")
                .collect(Collectors.toSet()));
        
        // Registrar recursos de Swagger
        register(OpenApiResource.class);
        
        // Configurar propiedades de Swagger
        property("openApi.configuration", oasConfig);
    }
}