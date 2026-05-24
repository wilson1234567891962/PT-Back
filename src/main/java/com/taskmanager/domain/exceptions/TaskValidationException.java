package com.taskmanager.domain.exceptions;

/**
 * Excepción de dominio cuando hay errores de validación en una tarea
 */
public class TaskValidationException extends RuntimeException {
    
    public TaskValidationException(String message) {
        super(message);
    }
}