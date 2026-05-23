# Task Manager API - Backend

API REST para gestión de tareas desarrollada en Java 8 con integración PL/SQL para Oracle Database.

## Requisitos

- Java 8 o superior
- Maven 3.6+
- Oracle Database 11g/12c/19c
- Servidor de aplicaciones (Tomcat 8+, GlassFish, WildFly)

## Estructura del Proyecto

```
src/main/java/com/taskmanager/
├── config/                    # Configuración de la aplicación
│   ├── ApplicationConfig.java # Configuración JAX-RS
│   ├── CorsFilter.java       # Filtro CORS
│   └── GenericExceptionMapper.java # Manejador de excepciones
├── database/                  # Conexión a base de datos
│   └── DatabaseConnection.java
├── dao/                       # Acceso a datos
│   └── TaskDAO.java          # DAO para operaciones PL/SQL
├── model/                     # Modelos de datos
│   └── Task.java             # Modelo Task
└── rest/                      # Recursos REST
    └── TaskResource.java     # Endpoints de la API

src/main/resources/sql/
└── create_tables_and_package.sql # Script PL/SQL

src/main/webapp/WEB-INF/
└── web.xml                   # Configuración web
```

## Configuración de la Base de Datos

### 1. Ejecutar el script PL/SQL

```sql
-- Conectarse a Oracle como sysdba o usuario con privilegios
sqlplus system/password@localhost:1521/XE

-- Ejecutar el script
@src/main/resources/sql/create_tables_and_package.sql
```

### 2. Configurar conexión en DatabaseConnection.java

Modificar las credenciales en `DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String DB_USER = "system";
private static final String DB_PASSWORD = "password";
```

## Endpoints de la API

### Base URL: `http://localhost:8080/task-api/api`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/tasks` | Obtener todas las tareas |
| GET | `/tasks/{id}` | Obtener una tarea por ID |
| POST | `/tasks` | Crear una nueva tarea |
| PUT | `/tasks/{id}` | Actualizar una tarea existente |
| DELETE | `/tasks/{id}` | Eliminar una tarea |
| GET | `/tasks/test` | Probar el servicio |

### Ejemplos de Uso

#### Obtener todas las tareas
```bash
curl -X GET "http://localhost:8080/task-api/api/tasks"
```

#### Obtener una tarea por ID
```bash
curl -X GET "http://localhost:8080/task-api/api/tasks/1"
```

#### Crear una nueva tarea
```bash
curl -X POST "http://localhost:8080/task-api/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Nueva tarea",
    "description": "Descripción de la tarea",
    "completed": false
  }'
```

#### Actualizar una tarea
```bash
curl -X PUT "http://localhost:8080/task-api/api/tasks/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tarea actualizada",
    "description": "Descripción actualizada",
    "completed": true
  }'
```

#### Eliminar una tarea
```bash
curl -X DELETE "http://localhost:8080/task-api/api/tasks/1"
```

## Compilación y Despliegue

### 1. Compilar con Maven
```bash
mvn clean compile
```

### 2. Empaquetar como WAR
```bash
mvn package
```

### 3. Desplegar en Tomcat
```bash
# Copiar el archivo WAR a la carpeta webapps de Tomcat
cp target/task-api.war /ruta/a/tomcat/webapps/
```

### 4. Ejecutar pruebas
```bash
mvn test
```

## Configuración CORS

La API está configurada para permitir solicitudes desde cualquier origen (`*`). Los headers CORS están configurados en `CorsFilter.java`.

## Estructura PL/SQL

### Tabla TASKS
```sql
CREATE TABLE TASKS (
    TASK_ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TITLE VARCHAR2(200) NOT NULL,
    DESCRIPTION VARCHAR2(1000),
    COMPLETED NUMBER(1) DEFAULT 0 NOT NULL CHECK (COMPLETED IN (0, 1)),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
```

### Paquete TASK_PKG
El paquete PL/SQL contiene los siguientes procedimientos:
- `GET_ALL_TASKS` - Obtener todas las tareas
- `GET_TASK_BY_ID` - Obtener una tarea por ID
- `CREATE_TASK` - Crear una nueva tarea
- `UPDATE_TASK` - Actualizar una tarea existente
- `DELETE_TASK` - Eliminar una tarea

## Dependencias Maven

- Jersey (JAX-RS implementation)
- Oracle JDBC Driver
- Jackson (JSON processing)
- SLF4J (Logging)
- JUnit (Testing)

## Licencia

Este proyecto es para fines educativos y de demostración técnica.