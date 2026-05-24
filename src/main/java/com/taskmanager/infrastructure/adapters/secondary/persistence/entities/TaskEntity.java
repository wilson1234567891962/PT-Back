package com.taskmanager.infrastructure.adapters.secondary.persistence.entities;

import com.taskmanager.domain.models.Task;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia (JPA/Hibernate) para Task
 * Adapta la entidad de dominio a la capa de persistencia
 */
public class TaskEntity {
    private Long taskId;
    private String title;
    private String description;
    private Integer completed; // 0 = false, 1 = true (para compatibilidad con Oracle/PostgreSQL)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public TaskEntity() {
    }

    public TaskEntity(Long taskId, String title, String description, Integer completed, 
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Método para convertir de dominio a entidad
    public static TaskEntity fromDomain(Task task) {
        return new TaskEntity(
            task.getTaskId(),
            task.getTitle(),
            task.getDescription(),
            task.getCompleted() ? 1 : 0,
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    // Método para convertir de entidad a dominio
    public Task toDomain() {
        return new Task(
            taskId,
            title,
            description,
            completed == 1,
            createdAt,
            updatedAt
        );
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
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
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
}