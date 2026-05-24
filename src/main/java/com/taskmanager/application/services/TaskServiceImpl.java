package com.taskmanager.application.services;

import com.taskmanager.application.ports.TaskRepository;
import com.taskmanager.application.ports.TaskService;
import com.taskmanager.domain.exceptions.TaskNotFoundException;
import com.taskmanager.domain.exceptions.TaskValidationException;
import com.taskmanager.domain.models.Task;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Injections;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso TaskService
 * Contiene la lógica de negocio y orquesta las operaciones
 */
public class TaskServiceImpl implements TaskService {
    
    private final TaskRepository taskRepository;
    
    @Inject
    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    @Override
    public Task createTask(String title, String description) {
        // Validación de negocio
        if (title == null || title.trim().isEmpty()) {
            throw new TaskValidationException("Task title is required");
        }
        
        // Crear entidad de dominio
        Task task = Task.create(title, description);
        
        // Persistir
        return taskRepository.save(task);
    }
    
    @Override
    public Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }
    
    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    @Override
    public Task updateTask(Long taskId, String title, String description, Boolean completed) {
        // Obtener tarea existente
        Task existingTask = getTaskById(taskId);
        
        // Aplicar actualizaciones
        existingTask.update(title, description);
        
        if (completed != null) {
            if (completed) {
                existingTask.markAsCompleted();
            } else {
                existingTask.markAsPending();
            }
        }
        
        // Validar
        if (!existingTask.isValid()) {
            throw new TaskValidationException("Updated task is not valid");
        }
        
        // Persistir cambios
        return taskRepository.update(existingTask);
    }
    
    @Override
    public void deleteTask(Long taskId) {
        // Verificar que existe
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        
        // Eliminar
        taskRepository.delete(taskId);
    }
    
    @Override
    public Task markTaskAsCompleted(Long taskId) {
        Task task = getTaskById(taskId);
        task.markAsCompleted();
        return taskRepository.update(task);
    }
    
    @Override
    public Task markTaskAsPending(Long taskId) {
        Task task = getTaskById(taskId);
        task.markAsPending();
        return taskRepository.update(task);
    }
    
    @Override
    public List<Task> getCompletedTasks() {
        return taskRepository.findByCompleted(true);
    }
    
    @Override
    public List<Task> getPendingTasks() {
        return taskRepository.findByCompleted(false);
    }
    
    @Override
    public List<Task> searchTasksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return getAllTasks();
        }
        return taskRepository.findByTitleContaining(title);
    }
    
    @Override
    public boolean taskExists(Long taskId) {
        return taskRepository.existsById(taskId);
    }
}