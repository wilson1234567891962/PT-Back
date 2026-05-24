package com.taskmanager.infrastructure.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Configuración de conexión a base de datos para arquitectura hexagonal
 * Compatible con inyección de dependencias
 */
public class DatabaseConnection {
    
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String dbDriver;
    
    private Connection connection = null;
    private boolean testMode = false;
    
    public DatabaseConnection() {
        // Usar variables de entorno con valores por defecto
        this.dbUrl = System.getenv().getOrDefault("DB_URL", "jdbc:oracle:thin:@localhost:1521:XE");
        this.dbUser = System.getenv().getOrDefault("DB_USER", "system");
        this.dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "password");
        this.dbDriver = System.getenv().getOrDefault("DB_DRIVER", "oracle.jdbc.driver.OracleDriver");
        
        initializeDriver();
    }
    
    // Constructor para testing
    public DatabaseConnection(String dbUrl, String dbUser, String dbPassword, String dbDriver) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbDriver = dbDriver;
        
        initializeDriver();
    }
    
    private void initializeDriver() {
        try {
            // Cargar el driver dinámicamente según configuración
            Class.forName(dbDriver);
            System.out.println("Driver cargado: " + dbDriver);
            System.out.println("Conectando a: " + dbUrl);
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de base de datos (" + dbDriver + "): " + e.getMessage());
            throw new RuntimeException("Failed to load database driver: " + dbDriver, e);
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (testMode) {
                // En modo de prueba, no intentamos conectarnos realmente
                throw new SQLException("Modo de prueba activado - No hay conexión a base de datos");
            }
            
            Properties connectionProps = new Properties();
            connectionProps.put("user", dbUser);
            connectionProps.put("password", dbPassword);
            
            // Configuraciones adicionales para mejor rendimiento
            connectionProps.put("defaultRowPrefetch", "20");
            connectionProps.put("defaultBatchValue", "10");
            
            connection = DriverManager.getConnection(dbUrl, connectionProps);
        }
        return connection;
    }
    
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    public void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexión a base de datos establecida exitosamente");
                System.out.println("URL: " + dbUrl);
                System.out.println("Usuario: " + dbUser);
                System.out.println("Driver: " + dbDriver);
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar a base de datos: " + e.getMessage());
            System.err.println("URL: " + dbUrl);
            System.err.println("Usuario: " + dbUser);
            System.err.println("Driver: " + dbDriver);
            // En modo de prueba, no imprimimos el stack trace completo
            if (!testMode) {
                e.printStackTrace();
            }
        }
    }
    
    // Método para activar/desactivar modo de prueba (solo para testing)
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }
    
    // Getters para información de conexión
    public String getDbUrl() {
        return dbUrl;
    }
    
    public String getDbUser() {
        return dbUser;
    }
    
    public String getDbDriver() {
        return dbDriver;
    }
    
    // Método para detectar si estamos usando PostgreSQL
    public boolean isPostgreSQL() {
        return dbDriver != null && dbDriver.toLowerCase().contains("postgresql");
    }
}