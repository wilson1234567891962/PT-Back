package com.taskmanager.application.ports;

import com.taskmanager.domain.models.Task;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (secondary port) para operaciones de persistencia
 * Define el contrato que deben implementar los adaptadores de persistencia
 */
public interface TaskRepository {
    
    // CRUD operations
    Task save(Task task);
    Optional<Task> findById(Long taskId);
    List<Task> findAll();
    Task update(Task task);
    boolean delete(Long taskId);
    
    // Additional operations
    boolean existsById(Long taskId);
    long count();
    
    // Business operations
    List<Task> findByCompleted(boolean completed);
    List<Task> findByTitleContaining(String title);
}