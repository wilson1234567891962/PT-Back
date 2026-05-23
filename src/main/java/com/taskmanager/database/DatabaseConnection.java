package com.taskmanager.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER = "system";
    private static final String DB_PASSWORD = "password";
    
    private static Connection connection = null;
    
    static {
        try {
            // Cargar el driver de Oracle
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de Oracle: " + e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            Properties connectionProps = new Properties();
            connectionProps.put("user", DB_USER);
            connectionProps.put("password", DB_PASSWORD);
            
            // Configuraciones adicionales para mejor rendimiento
            connectionProps.put("defaultRowPrefetch", "20");
            connectionProps.put("defaultBatchValue", "10");
            
            connection = DriverManager.getConnection(DB_URL, connectionProps);
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexión a Oracle establecida exitosamente");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar a Oracle: " + e.getMessage());
            e.printStackTrace();
        }
    }
}