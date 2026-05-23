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

## Documentación de la API con Swagger/OpenAPI

La API incluye documentación automática generada con Swagger (OpenAPI 3). Para acceder a la documentación interactiva:

### Swagger UI
- **URL:** `http://localhost:8080/task-api/swagger-index.html`
- **Descripción:** Interfaz web interactiva para explorar y probar los endpoints de la API. Contiene una especificación OpenAPI estática con todos los endpoints documentados.

### Características de la documentación:
- **Documentación completa:** Todos los endpoints están documentados con descripciones, parámetros y respuestas.
- **Especificación estática:** La documentación está incrustada en la página HTML, no requiere conexión a `/openapi.json`.
- **Pruebas interactivas:** Permite probar los endpoints directamente desde el navegador.
- **Esquemas de datos:** Documenta los modelos de datos (Task) con sus propiedades y tipos.

### Características de la documentación:
- **Documentación completa:** Todos los endpoints están documentados con descripciones, parámetros y respuestas.
- **Ejemplos:** Incluye ejemplos de solicitudes y respuestas.
- **Pruebas interactivas:** Permite probar los endpoints directamente desde el navegador.
- **Esquemas de datos:** Documenta los modelos de datos (Task) con sus propiedades y tipos.

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

### 5. Ejecutar pruebas con cobertura (JaCoCo)
```bash
mvn verify
```

### 6. Verificar reporte de cobertura
Después de ejecutar `mvn verify`, se genera un reporte HTML de cobertura en:
```
target/site/jacoco/index.html
```

**Cobertura actual del proyecto:**
- **Líneas:** 86% (≥ 80% requerido)
- **Ramas:** 72% (≥ 70% requerido)
- **Métodos:** 94%
- **Clases:** 89%
- **Total de pruebas:** 69 pruebas unitarias

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
- Mockito (Testing con mocks)
- JaCoCo (Cobertura de código)
- Swagger/OpenAPI 3 (Documentación de API)

## Licencia

Este proyecto es para fines educativos y de demostración técnica.

## Despliegue con Docker

El proyecto incluye configuración completa para despliegue con Docker.

### Archivos Docker disponibles

- **`Dockerfile`**: Para despliegue con WAR pre-construido
- **`Dockerfile.build`**: Construcción completa desde código fuente (recomendado)
- **`docker-compose.yml`**: Despliegue con PostgreSQL (alternativa a Oracle)
- **`docker-compose-dev.yml`**: Modo desarrollo sin base de datos
- **`docker-compose-oracle-external.yml`**: Conexión a Oracle externa
- **`docker-commands.sh` / `docker-commands.bat`**: Scripts de utilidad

### 🚀 Guía rápida para desarrollo con base de datos

#### **Para pruebas de guardar datos con PostgreSQL:**

```bash
# 1. Ir al directorio del backend
cd PT-Back

# 2. Levantar todos los servicios (aplicación + PostgreSQL + pgAdmin)
docker-compose up -d --build

# 3. Verificar que todo esté corriendo
docker-compose ps

# 4. Probar que puedes guardar datos
curl -X POST "http://localhost:8080/task-api/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tarea de prueba Docker",
    "description": "Esta tarea se guarda en PostgreSQL",
    "completed": false
  }'

# 5. Listar tareas para verificar
curl http://localhost:8080/task-api/api/tasks
```

#### **Acceso a servicios:**

- **API REST:** `http://localhost:8080/task-api/api`
- **Swagger UI:** `http://localhost:8080/task-api/swagger-index.html`
- **PostgreSQL:** `localhost:5432` (usuario: taskuser, contraseña: taskpass)
- **pgAdmin (interfaz web):** `http://localhost:5050` (admin@taskmanager.com / admin123)

#### **Comandos útiles para desarrollo:**

```bash
# Ver logs en tiempo real
docker-compose logs -f task-api

# Reiniciar solo la aplicación
docker-compose restart task-api

# Acceder a la consola de PostgreSQL
docker-compose exec postgres psql -U taskuser -d taskdb

# Detener todo
docker-compose down

# Detener y eliminar datos (reinicia base de datos)
docker-compose down -v
```

### Modo desarrollo sin base de datos

```bash
# Para desarrollo rápido sin base de datos real
docker-compose -f docker-compose-dev.yml up --build
```

### Configuración para Oracle

Si necesitas conectar a Oracle Database:

1. Copiar el driver JDBC (`ojdbc8.jar`) a `oracle-driver/`
2. Configurar conexión en `docker-compose-oracle-external.yml`
3. Ejecutar: `docker-compose -f docker-compose-oracle-external.yml up --build`

### Usar scripts de utilidad

```bash
# Linux/Mac
chmod +x docker-commands.sh
./docker-commands.sh compose-up

# Windows
docker-commands.bat compose-up
```

Para más detalles, consulta [DOCKER-README.md](DOCKER-README.md).

## Pruebas y Cobertura

### Ejecutar pruebas con cobertura
```bash
mvn verify
```

### Ver reporte de cobertura
Después de ejecutar `mvn verify`, abre `target/site/jacoco/index.html` en tu navegador.

**Cobertura actual del proyecto:**
- **Líneas:** 86% (≥ 80% requerido)
- **Ramas:** 72% (≥ 70% requerido)
- **Métodos:** 94%
- **Clases:** 89%
- **Total de pruebas:** 69 pruebas unitarias

## Dependencias Maven

- Jersey (JAX-RS implementation)
- Oracle JDBC Driver
- Jackson (JSON processing)
- SLF4J (Logging)
- JUnit (Testing)
- Mockito (Testing con mocks)
- JaCoCo (Cobertura de código)
- Swagger/OpenAPI 3 (Documentación de API)

## Licencia

Este proyecto es para fines educativos y de demostración técnica.