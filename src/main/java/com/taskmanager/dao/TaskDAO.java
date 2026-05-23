package com.taskmanager.dao;

import com.taskmanager.database.DatabaseConnection;
import com.taskmanager.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    
    // Método para obtener todas las tareas usando el paquete PL/SQL
    public List<Task> getAllTasks() throws SQLException {
        List<Task> tasks = new ArrayList<>();
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
        return tasks;
    }
    
    // Método para obtener una tarea por ID usando el paquete PL/SQL
    public Task getTaskById(Long taskId) throws SQLException {
        Task task = null;
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
        return task;
    }
    
    // Método para crear una nueva tarea usando el paquete PL/SQL
    public Task createTask(Task task) throws SQLException {
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
    
    // Método para actualizar una tarea usando el paquete PL/SQL
    public Task updateTask(Long taskId, Task task) throws SQLException {
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
    
    // Método para eliminar una tarea usando el paquete PL/SQL
    public boolean deleteTask(Long taskId) throws SQLException {
        String sql = "{call TASK_PKG.DELETE_TASK(?)}";
        
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            
            cstmt.setLong(1, taskId);
            return cstmt.executeUpdate() > 0;
        }
    }
    
    // Método auxiliar para mapear ResultSet a objeto Task
    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setTaskId(rs.getLong("TASK_ID"));
        task.setTitle(rs.getString("TITLE"));
        task.setDescription(rs.getString("DESCRIPTION"));
        task.setCompleted(rs.getBoolean("COMPLETED"));
        task.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        task.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
        return task;
    }
    
    // Método para probar la conexión y el paquete PL/SQL
    public void testPLSQLPackage() {
        try {
            System.out.println("Probando conexión a la base de datos...");
            DatabaseConnection.testConnection();
            
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