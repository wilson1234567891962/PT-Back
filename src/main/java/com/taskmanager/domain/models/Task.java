package com.taskmanager.domain.models;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Task - Representa el núcleo de negocio
 * No tiene dependencias de frameworks o infraestructura
 */
public class Task {
    private Long taskId;
    private String title;
    private String description;
    private Boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public Task() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.completed = false;
    }

    public Task(Long taskId, String title, String description, Boolean completed, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.completed = completed != null ? completed : false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Factory method para creación
    public static Task create(String title, String description) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        return task;
    }

    // Business logic methods
    public void markAsCompleted() {
        this.completed = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsPending() {
        this.completed = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, String description) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Validations
    public boolean isValid() {
        return title != null && !title.trim().isEmpty();
    }

    // Getters y Setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        this.title = title;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed != null ? completed : false;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId=" + taskId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}