package com.taskmanager.config;

import com.taskmanager.application.ports.TaskRepository;
import com.taskmanager.application.ports.TaskService;
import com.taskmanager.application.services.TaskServiceImpl;
import com.taskmanager.infrastructure.adapters.primary.rest.TaskController;
import com.taskmanager.infrastructure.adapters.secondary.persistence.TaskRepositoryImpl;
import com.taskmanager.infrastructure.config.DatabaseConnection;
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
public class ManualDependencyConfig extends ResourceConfig {
    
    public ManualDependencyConfig() {
        // Crear instancias manualmente
        DatabaseConnection databaseConnection = new DatabaseConnection();
        TaskRepository taskRepository = new TaskRepositoryImpl(databaseConnection);
        TaskService taskService = new TaskServiceImpl(taskRepository);
        
        // Crear controlador con dependencias inyectadas manualmente
        TaskController taskController = new TaskController();
        taskController.setTaskService(taskService);
        
        // Registrar el controlador
        register(taskController);
        
        // Registrar filtros y mapeadores de excepción
        register(CorsFilter.class);
        register(GenericExceptionMapper.class);
        register(JacksonConfig.class);
        
        // Configurar Swagger/OpenAPI
        configureSwagger();
        
        // Configurar propiedades del servidor
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
        property(ServerProperties.MEDIA_TYPE_MAPPINGS, "json:application/json");
        
        // Configurar Jackson para ignorar propiedades desconocidas
        property("jersey.config.server.disableAutoDiscovery", "true");
        property("jersey.config.server.provider.classnames", 
            "org.glassfish.jersey.jackson.JacksonFeature," +
            "org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider");
        
        System.out.println("Aplicación REST con dependencias manuales configurada en /api");
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