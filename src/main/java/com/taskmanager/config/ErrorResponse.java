package com.taskmanager.config;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Clase para respuestas de error estandarizadas en JSON
 */
public class ErrorResponse {
    
    @Schema(description = "Código de estado HTTP", example = "404")
    private int status;
    
    @Schema(description = "Mensaje de error", example = "Task not found with ID: 1")
    private String message;
    
    @Schema(description = "Tipo de error", example = "NOT_FOUND")
    private String error;
    
    @Schema(description = "Ruta de la solicitud", example = "/api/tasks/1")
    private String path;
    
    @Schema(description = "Marca de tiempo del error", example = "2026-05-24T21:43:51Z")
    private String timestamp;
    
    public ErrorResponse() {
    }
    
    public ErrorResponse(int status, String message, String error, String path) {
        this.status = status;
        this.message = message;
        this.error = error;
        this.path = path;
        this.timestamp = java.time.Instant.now().toString();
    }
    
    // Getters y setters
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}