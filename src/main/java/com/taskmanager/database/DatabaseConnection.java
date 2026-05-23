package com.taskmanager.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    // Usar variables de entorno con valores por defecto
    private static final String DB_URL = System.getenv().getOrDefault("DB_URL", "jdbc:oracle:thin:@localhost:1521:XE");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "system");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "password");
    private static final String DB_DRIVER = System.getenv().getOrDefault("DB_DRIVER", "oracle.jdbc.driver.OracleDriver");
    
    private static Connection connection = null;
    private static boolean testMode = false;
    
    static {
        try {
            // Cargar el driver dinámicamente según configuración
            Class.forName(DB_DRIVER);
            System.out.println("Driver cargado: " + DB_DRIVER);
            System.out.println("Conectando a: " + DB_URL);
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de base de datos (" + DB_DRIVER + "): " + e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (testMode) {
                // En modo de prueba, no intentamos conectarnos realmente
                throw new SQLException("Modo de prueba activado - No hay conexión a base de datos");
            }
            
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
                System.out.println("Conexión a base de datos establecida exitosamente");
                System.out.println("URL: " + DB_URL);
                System.out.println("Usuario: " + DB_USER);
                System.out.println("Driver: " + DB_DRIVER);
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar a base de datos: " + e.getMessage());
            System.err.println("URL: " + DB_URL);
            System.err.println("Usuario: " + DB_USER);
            System.err.println("Driver: " + DB_DRIVER);
            // En modo de prueba, no imprimimos el stack trace completo
            if (!testMode) {
                e.printStackTrace();
            }
        }
    }
    
    // Método para activar/desactivar modo de prueba (solo para testing)
    public static void setTestMode(boolean testMode) {
        DatabaseConnection.testMode = testMode;
    }
}