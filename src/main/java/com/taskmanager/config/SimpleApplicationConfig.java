package com.taskmanager.config;

import com.taskmanager.infrastructure.adapters.primary.rest.TaskController;
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
public class SimpleApplicationConfig extends ResourceConfig {
    
    public SimpleApplicationConfig() {
        // Usar el proveedor de dependencias
        super(DependencyProvider.class);
        
        // Registrar controladores REST
        register(TaskController.class);
        
        // Registrar filtros y mapeadores de excepción
        register(CorsFilter.class);
        register(GenericExceptionMapper.class);
        
        // Configurar Swagger/OpenAPI
        configureSwagger();
        
        // Configurar propiedades del servidor
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
        property(ServerProperties.MEDIA_TYPE_MAPPINGS, "json:application/json");
        
        System.out.println("Aplicación REST configurada en /api con Swagger");
    }
    
    private void configureSwagger() {
        // Configurar OpenAPI
        OpenAPI openAPI = new OpenAPI()
            .info(new Info()
                .title("Task Manager API - Arquitectura Hexagonal")
                .description("API REST para gestión de tareas con arquitectura hexagonal (puertos y adaptadores)")
                .version("2.0.0")
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
            .resourcePackages(Stream.of("com.taskmanager.infrastructure.adapters.primary.rest")
                .collect(Collectors.toSet()));
        
        // Registrar recursos de Swagger
        register(OpenApiResource.class);
        
        // Configurar propiedades de Swagger
        property("openApi.configuration", oasConfig);
    }
}