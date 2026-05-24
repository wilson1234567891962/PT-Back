package com.taskmanager.domain.exceptions;

/**
 * Excepción de dominio cuando una tarea no es encontrada
 */
public class TaskNotFoundException extends RuntimeException {
    
    public TaskNotFoundException(Long taskId) {
        super("Task not found with ID: " + taskId);
    }
    
    public TaskNotFoundException(String message) {
        super(message);
    }
}