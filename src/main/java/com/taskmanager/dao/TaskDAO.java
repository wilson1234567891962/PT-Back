package com.taskmanager.dao;

import com.taskmanager.database.DatabaseConnection;
import com.taskmanager.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    
    // Método para obtener todas las tareas usando el paquete PL/SQL (Oracle)
    public List<Task> getAllTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
        
        // Detectar si estamos usando PostgreSQL u Oracle
        if (isPostgreSQL()) {
            // SQL estándar para PostgreSQL
            String sql = "SELECT task_id, title, description, completed, created_at, updated_at FROM tasks ORDER BY created_at DESC";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        } else {
            // Paquete PL/SQL para Oracle
            String sql = "{call TASK_PKG.GET_ALL_TASKS(?)}";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement cstmt = conn.prepareCall(sql)) {
                
                cstmt.registerOutParameter(1, Types.REF_CURSOR);
                cstmt.execute();
                
                try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                    while (rs.next()) {
                        tasks.add(mapResultSetToTask(rs));
                    }
                }
            }
        }
        return tasks;
    }
    
    // Método para obtener una tarea por ID usando el paquete PL/SQL (Oracle)
    public Task getTaskById(Long taskId) throws SQLException {
        Task task = null;
        
        // Detectar si estamos usando PostgreSQL u Oracle
        if (isPostgreSQL()) {
            // SQL estándar para PostgreSQL
            String sql = "SELECT task_id, title, description, completed, created_at, updated_at FROM tasks WHERE task_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, taskId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        task = mapResultSetToTask(rs);
                    }
                }
            }
        } else {
            // Paquete PL/SQL para Oracle
            String sql = "{call TASK_PKG.GET_TASK_BY_ID(?, ?)}";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement cstmt = conn.prepareCall(sql)) {
                
                cstmt.setLong(1, taskId);
                cstmt.registerOutParameter(2, Types.REF_CURSOR);
                cstmt.execute();
                
                try (ResultSet rs = (ResultSet) cstmt.getObject(2)) {
                    if (rs.next()) {
                        task = mapResultSetToTask(rs);
                    }
                }
            }
        }
        return task;
    }
    
    // Método para crear una nueva tarea usando el paquete PL/SQL (Oracle)
    public Task createTask(Task task) throws SQLException {
        // Detectar si estamos usando PostgreSQL u Oracle
        if (isPostgreSQL()) {
            // SQL estándar para PostgreSQL
            String sql = "INSERT INTO tasks (title, description, completed) VALUES (?, ?, ?) RETURNING task_id";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                // Para PostgreSQL, usar INTEGER (0 o 1) en lugar de BOOLEAN
                pstmt.setInt(3, task.getCompleted() != null && task.getCompleted() ? 1 : 0);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Long newTaskId = rs.getLong(1);
                        task.setTaskId(newTaskId);
                        
                        // Obtener la tarea creada con todos los campos
                        return getTaskById(newTaskId);
                    } else {
                        throw new SQLException("No se pudo obtener el ID de la tarea creada");
                    }
                }
            }
        } else {
            // Paquete PL/SQL para Oracle
            String sql = "{call TASK_PKG.CREATE_TASK(?, ?, ?)}";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement cstmt = conn.prepareCall(sql)) {
                
                cstmt.setString(1, task.getTitle());
                cstmt.setString(2, task.getDescription());
                cstmt.registerOutParameter(3, Types.NUMERIC);
                
                cstmt.execute();
                
                Long newTaskId = cstmt.getLong(3);
                task.setTaskId(newTaskId);
                
                // Obtener la tarea creada con todos los campos
                return getTaskById(newTaskId);
            }
        }
    }
    
    // Método para actualizar una tarea usando el paquete PL/SQL (Oracle)
    public Task updateTask(Long taskId, Task task) throws SQLException {
        // Detectar si estamos usando PostgreSQL u Oracle
        if (isPostgreSQL()) {
            // SQL estándar para PostgreSQL
            String sql = "UPDATE tasks SET title = ?, description = ?, completed = ? WHERE task_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, task.getTitle());
                pstmt.setString(2, task.getDescription());
                // Para PostgreSQL, usar INTEGER (0 o 1) en lugar de BOOLEAN
                pstmt.setInt(3, task.getCompleted() != null && task.getCompleted() ? 1 : 0);
                pstmt.setLong(4, taskId);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    return getTaskById(taskId);
                } else {
                    throw new SQLException("No se encontró la tarea con ID: " + taskId);
                }
            }
        } else {
            // Paquete PL/SQL para Oracle
            String sql = "{call TASK_PKG.UPDATE_TASK(?, ?, ?, ?)}";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement cstmt = conn.prepareCall(sql)) {
                
                cstmt.setLong(1, taskId);
                cstmt.setString(2, task.getTitle());
                cstmt.setString(3, task.getDescription());
                // Convertir Boolean a NUMBER (0 o 1) para Oracle
                cstmt.setInt(4, task.getCompleted() != null && task.getCompleted() ? 1 : 0);
                
                cstmt.execute();
                
                return getTaskById(taskId);
            }
        }
    }
    
    // Método para eliminar una tarea usando el paquete PL/SQL (Oracle)
    public boolean deleteTask(Long taskId) throws SQLException {
        // Detectar si estamos usando PostgreSQL u Oracle
        if (isPostgreSQL()) {
            // SQL estándar para PostgreSQL
            String sql = "DELETE FROM tasks WHERE task_id = ?";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, taskId);
                return pstmt.executeUpdate() > 0;
            }
        } else {
            // Paquete PL/SQL para Oracle
            String sql = "{call TASK_PKG.DELETE_TASK(?)}";
            
            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement cstmt = conn.prepareCall(sql)) {
                
                cstmt.setLong(1, taskId);
                return cstmt.executeUpdate() > 0;
            }
        }
    }
    
    // Método auxiliar para mapear ResultSet a objeto Task
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setTaskId(rs.getLong("TASK_ID"));
        task.setTitle(rs.getString("TITLE"));
        task.setDescription(rs.getString("DESCRIPTION"));
        
        // Manejar tanto INTEGER (PostgreSQL) como BOOLEAN/INTEGER (Oracle)
        // PostgreSQL usa INTEGER (0 o 1), Oracle puede usar NUMBER(1,0) o BOOLEAN
        try {
            // Intentar obtener como booleano primero
            task.setCompleted(rs.getBoolean("COMPLETED"));
        } catch (SQLException e) {
            // Si falla, intentar obtener como entero y convertir
            int completedInt = rs.getInt("COMPLETED");
            task.setCompleted(completedInt == 1);
        }
        
        task.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        task.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        return task;
    }
    
    // Método auxiliar para detectar si estamos usando PostgreSQL
    private boolean isPostgreSQL() {
        String driver = System.getenv().getOrDefault("DB_DRIVER", "oracle.jdbc.driver.OracleDriver");
        return driver != null && driver.toLowerCase().contains("postgresql");
    }
    
    // Método para probar la conexión y el paquete PL/SQL
    public void testPLSQLPackage() {
        try {
            System.out.println("Probando conexión a la base de datos...");
            DatabaseConnection.testConnection();
            
            System.out.println("Base de datos detectada: " + (isPostgreSQL() ? "PostgreSQL" : "Oracle"));
            
            System.out.println("Creando tarea de prueba...");
            Task testTask = new Task();
            testTask.setTitle("Tarea de prueba");
            testTask.setDescription("Descripción de prueba");
            // No establecemos completed ya que el procedimiento CREATE_TASK no lo recibe
            
            Task createdTask = createTask(testTask);
            System.out.println("Tarea creada: " + createdTask);
            
            System.out.println("Obteniendo todas las tareas...");
            List<Task> tasks = getAllTasks();
            System.out.println("Total de tareas: " + tasks.size());
            
            if (createdTask != null) {
                System.out.println("Eliminando tarea de prueba...");
                deleteTask(createdTask.getTaskId());
                System.out.println("Tarea eliminada exitosamente");
            }
            
        } catch (SQLException e) {
            System.err.println("Error al probar el paquete PL/SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}