package com.taskmanager.infrastructure.adapters.secondary.persistence;

import com.taskmanager.application.ports.TaskRepository;
import com.taskmanager.domain.models.Task;
import com.taskmanager.infrastructure.adapters.secondary.persistence.entities.TaskEntity;
import com.taskmanager.infrastructure.config.DatabaseConnection;

import javax.inject.Inject;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistencia que implementa TaskRepository
 * Maneja la conexión a base de datos (PostgreSQL/Oracle)
 */
public class TaskRepositoryImpl implements TaskRepository {
    
    private final DatabaseConnection databaseConnection;
    
    @Inject
    public TaskRepositoryImpl(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }
    
    @Override
    public Task save(Task task) {
        // Detectar si estamos usando PostgreSQL u Oracle
        boolean isPostgreSQL = isPostgreSQL();
        
        if (isPostgreSQL) {
            return savePostgreSQL(task);
        } else {
            return saveOracle(task);
        }
    }
    
    private Task savePostgreSQL(Task task) {
        String sql = "INSERT INTO tasks (title, description, completed, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?) RETURNING task_id";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.getCompleted() ? 1 : 0);
            pstmt.setTimestamp(4, Timestamp.valueOf(task.getCreatedAt()));
            pstmt.setTimestamp(5, Timestamp.valueOf(task.getUpdatedAt()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Long newTaskId = rs.getLong(1);
                    task.setTaskId(newTaskId);
                    return task;
                } else {
                    throw new SQLException("Failed to get generated task ID");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving task to PostgreSQL", e);
        }
    }
    
    private Task saveOracle(Task task) {
        String sql = "{call TASK_PKG.CREATE_TASK(?, ?, ?)}";
        
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setString(1, task.getTitle());
            cstmt.setString(2, task.getDescription());
            cstmt.registerOutParameter(3, Types.NUMERIC);
            
            cstmt.execute();
            
            Long newTaskId = cstmt.getLong(3);
            task.setTaskId(newTaskId);
            
            // Obtener la tarea completa para tener todos los campos
            return findById(newTaskId).orElse(task);
        } catch (SQLException e) {
            throw new RuntimeException("Error saving task to Oracle", e);
        }
    }
    
    @Override
    public Optional<Task> findById(Long taskId) {
        boolean isPostgreSQL = isPostgreSQL();
        
        if (isPostgreSQL) {
            return findByIdPostgreSQL(taskId);
        } else {
            return findByIdOracle(taskId);
        }
    }
    
    private Optional<Task> findByIdPostgreSQL(Long taskId) {
        String sql = "SELECT task_id, title, description, completed, created_at, updated_at " +
                    "FROM tasks WHERE task_id = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, taskId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding task by ID in PostgreSQL", e);
        }
    }
    
    private Optional<Task> findByIdOracle(Long taskId) {
        String sql = "{call TASK_PKG.GET_TASK_BY_ID(?, ?)}";
        
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setLong(1, taskId);
            cstmt.registerOutParameter(2, Types.REF_CURSOR);
            cstmt.execute();
            
            try (ResultSet rs = (ResultSet) cstmt.getObject(2)) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding task by ID in Oracle", e);
        }
    }
    
    @Override
    public List<Task> findAll() {
        boolean isPostgreSQL = isPostgreSQL();
        
        if (isPostgreSQL) {
            return findAllPostgreSQL();
        } else {
            return findAllOracle();
        }
    }
    
    private List<Task> findAllPostgreSQL() {
        String sql = "SELECT task_id, title, description, completed, created_at, updated_at " +
                    "FROM tasks ORDER BY created_at DESC";
        
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }
            return tasks;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all tasks in PostgreSQL", e);
        }
    }
    
    private List<Task> findAllOracle() {
        String sql = "{call TASK_PKG.GET_ALL_TASKS(?)}";
        
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.registerOutParameter(1, Types.REF_CURSOR);
            cstmt.execute();
            
            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
                return tasks;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all tasks in Oracle", e);
        }
    }
    
    @Override
    public Task update(Task task) {
        boolean isPostgreSQL = isPostgreSQL();
        
        if (isPostgreSQL) {
            return updatePostgreSQL(task);
        } else {
            return updateOracle(task);
        }
    }
    
    private Task updatePostgreSQL(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, completed = ?, updated_at = ? " +
                    "WHERE task_id = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.getCompleted() ? 1 : 0);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(5, task.getTaskId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Task not found with ID: " + task.getTaskId());
            }
            
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task in PostgreSQL", e);
        }
    }
    
    private Task updateOracle(Task task) {
        String sql = "{call TASK_PKG.UPDATE_TASK(?, ?, ?, ?)}";
        
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setLong(1, task.getTaskId());
            cstmt.setString(2, task.getTitle());
            cstmt.setString(3, task.getDescription());
            cstmt.setInt(4, task.getCompleted() ? 1 : 0);
            
            cstmt.execute();
            return task;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating task in Oracle", e);
        }
    }
    
    @Override
    public boolean delete(Long taskId) {
        boolean isPostgreSQL = isPostgreSQL();
        
        if (isPostgreSQL) {
            return deletePostgreSQL(taskId);
        } else {
            return deleteOracle(taskId);
        }
    }
    
    private boolean deletePostgreSQL(Long taskId) {
        String sql = "DELETE FROM tasks WHERE task_id = ?";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, taskId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting task in PostgreSQL", e);
        }
    }
    
    private boolean deleteOracle(Long taskId) {
        String sql = "{call TASK_PKG.DELETE_TASK(?)}";
        
        try (Connection conn = databaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setLong(1, taskId);
            return cstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting task in Oracle", e);
        }
    }
    
    @Override
    public boolean existsById(Long taskId) {
        return findById(taskId).isPresent();
    }
    
    @Override
    public long count() {
        boolean isPostgreSQL = isPostgreSQL();
        String sql = isPostgreSQL ? 
            "SELECT COUNT(*) FROM tasks" : 
            "SELECT COUNT(*) FROM TASKS";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error counting tasks", e);
        }
    }
    
    @Override
    public List<Task> findByCompleted(boolean completed) {
        boolean isPostgreSQL = isPostgreSQL();
        String sql = isPostgreSQL ? 
            "SELECT task_id, title, description, completed, created_at, updated_at " +
            "FROM tasks WHERE completed = ? ORDER BY created_at DESC" :
            "SELECT task_id, title, description, completed, created_at, updated_at " +
            "FROM TASKS WHERE COMPLETED = ? ORDER BY CREATED_AT DESC";
        
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, completed ? 1 : 0);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
                return tasks;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tasks by completion status", e);
        }
    }
    
    @Override
    public List<Task> findByTitleContaining(String title) {
        boolean isPostgreSQL = isPostgreSQL();
        String sql = isPostgreSQL ? 
            "SELECT task_id, title, description, completed, created_at, updated_at " +
            "FROM tasks WHERE LOWER(title) LIKE LOWER(?) ORDER BY created_at DESC" :
            "SELECT task_id, title, description, completed, created_at, updated_at " +
            "FROM TASKS WHERE LOWER(TITLE) LIKE LOWER(?) ORDER BY CREATED_AT DESC";
        
        List<Task> tasks = new ArrayList<>();
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + title + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
                return tasks;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tasks by title", e);
        }
    }
    
    // Helper method to map ResultSet to Task domain object
    public Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setTaskId(rs.getLong("TASK_ID"));
        task.setTitle(rs.getString("TITLE"));
        task.setDescription(rs.getString("DESCRIPTION"));
        
        // Handle both INTEGER (PostgreSQL) and NUMBER (Oracle) for completed field
        try {
            // Try to get as boolean first
            task.setCompleted(rs.getBoolean("COMPLETED"));
        } catch (SQLException e) {
            // If fails, get as integer and convert
            int completedInt = rs.getInt("COMPLETED");
            task.setCompleted(completedInt == 1);
        }
        
        task.setCreatedAt(rs.getTimestamp("CREATED_AT").toLocalDateTime());
        task.setUpdatedAt(rs.getTimestamp("UPDATED_AT").toLocalDateTime());
        return task;
    }
    
    // Helper method to detect database type
    public boolean isPostgreSQL() {
        String driver = databaseConnection.getDbDriver();
        return driver != null && driver.toLowerCase().contains("postgresql");
    }
}