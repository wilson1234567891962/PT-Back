package com.taskmanager.config;

import com.taskmanager.application.ports.TaskRepository;
import com.taskmanager.application.ports.TaskService;
import com.taskmanager.application.services.TaskServiceImpl;
import com.taskmanager.infrastructure.adapters.secondary.persistence.TaskRepositoryImpl;
import com.taskmanager.infrastructure.config.DatabaseConnection;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import javax.inject.Singleton;

/**
 * Proveedor de dependencias simple para la aplicación
 */
public class DependencyProvider extends ResourceConfig {
    
    public DependencyProvider() {
        // Configurar el binder de dependencias
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                // Configuración de infraestructura
                bindFactory(DatabaseConnectionFactory.class)
                    .to(DatabaseConnection.class)
                    .in(Singleton.class);
                
                // Adaptadores secundarios (persistencia)
                bindFactory(TaskRepositoryFactory.class)
                    .to(TaskRepository.class)
                    .in(Singleton.class);
                
                // Servicios de aplicación (casos de uso)
                bindFactory(TaskServiceFactory.class)
                    .to(TaskService.class)
                    .in(Singleton.class);
            }
        });
    }
    
    // Factory para DatabaseConnection
    public static class DatabaseConnectionFactory implements Factory<DatabaseConnection> {
        @Override
        public DatabaseConnection provide() {
            return new DatabaseConnection();
        }
        
        @Override
        public void dispose(DatabaseConnection instance) {
            if (instance != null) {
                instance.closeConnection();
            }
        }
    }
    
    // Factory para TaskRepository
    public static class TaskRepositoryFactory implements Factory<TaskRepository> {
        private final DatabaseConnection databaseConnection;
        
        public TaskRepositoryFactory(DatabaseConnection databaseConnection) {
            this.databaseConnection = databaseConnection;
        }
        
        @Override
        public TaskRepository provide() {
            return new TaskRepositoryImpl(databaseConnection);
        }
        
        @Override
        public void dispose(TaskRepository instance) {
            // No hay nada que limpiar
        }
    }
    
    // Factory para TaskService
    public static class TaskServiceFactory implements Factory<TaskService> {
        private final TaskRepository taskRepository;
        
        public TaskServiceFactory(TaskRepository taskRepository) {
            this.taskRepository = taskRepository;
        }
        
        @Override
        public TaskService provide() {
            return new TaskServiceImpl(taskRepository);
        }
        
        @Override
        public void dispose(TaskService instance) {
            // No hay nada que limpiar
        }
    }
}