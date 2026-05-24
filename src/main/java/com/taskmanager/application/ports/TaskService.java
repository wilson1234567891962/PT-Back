package com.taskmanager.application.ports;

import com.taskmanager.domain.models.Task;
import java.util.List;

/**
 * Puerto de entrada (primary port) para operaciones de negocio
 * Define el contrato que deben implementar los casos de uso
 */
public interface TaskService {
    
    // CRUD operations
    Task createTask(String title, String description);
    Task getTaskById(Long taskId);
    List<Task> getAllTasks();
    Task updateTask(Long taskId, String title, String description, Boolean completed);
    void deleteTask(Long taskId);
    
    // Business operations
    Task markTaskAsCompleted(Long taskId);
    Task markTaskAsPending(Long taskId);
    List<Task> getCompletedTasks();
    List<Task> getPendingTasks();
    List<Task> searchTasksByTitle(String title);
    
    // Validation
    boolean taskExists(Long taskId);
}