package com.taskmanager.infrastructure.config;

import com.taskmanager.application.ports.TaskRepository;
import com.taskmanager.application.ports.TaskService;
import com.taskmanager.application.services.TaskServiceImpl;
import com.taskmanager.infrastructure.adapters.primary.rest.TaskController;
import com.taskmanager.infrastructure.adapters.secondary.persistence.TaskRepositoryImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Configuración de inyección de dependencias para arquitectura hexagonal
 * Usa HK2 (Jersey's DI) para conectar todas las capas
 */
public class DependencyInjectionConfig extends ResourceConfig {
    
    public DependencyInjectionConfig() {
        // Registrar el controlador REST
        register(TaskController.class);
        
        // Registrar las implementaciones directamente
        register(TaskServiceImpl.class);
        register(TaskRepositoryImpl.class);
        register(DatabaseConnection.class);
        
        // Configurar el binder de dependencias
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                // Configuración de infraestructura
                bind(DatabaseConnection.class).to(DatabaseConnection.class);
                
                // Adaptadores secundarios (persistencia)
                bind(TaskRepositoryImpl.class).to(TaskRepository.class);
                
                // Servicios de aplicación (casos de uso)
                bind(TaskServiceImpl.class).to(TaskService.class);
            }
        });
    }
}