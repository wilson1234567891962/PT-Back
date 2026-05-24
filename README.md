# Task Manager API - Backend

API REST para gestión de tareas desarrollada en Java 8 con **Arquitectura Hexagonal (Ports & Adapters)** y compatibilidad dual con Oracle Database (PL/SQL) y PostgreSQL.

## 🎯 Resumen de Mejoras con Arquitectura Hexagonal

### ✅ **Ventajas Implementadas:**
1. **Separación clara de responsabilidades** - Dominio, aplicación e infraestructura separados
2. **Independencia de infraestructura** - Cambia entre PostgreSQL y Oracle sin modificar el dominio
3. **Mejor testabilidad** - Cada capa puede probarse de forma aislada
4. **Mantenibilidad mejorada** - Cambios en una capa no afectan a las otras
5. **Endpoints avanzados** - Nuevas funcionalidades: `/complete`, `/pending`, `/search`, etc.

### 🏗️ **Estructura Limpia:**
- **Carpetas eliminadas:** `model/`, `rest/`, `database/`, `dao/` (obsoletas)
- **Nueva estructura:** `domain/`, `application/`, `infrastructure/`, `config/`
- **Código organizado:** Puertos (interfaces) y adaptadores (implementaciones)

### 🔄 **Compatibilidad Dual:**
- **PostgreSQL:** Para desarrollo con Docker (recomendado)
- **Oracle:** Para producción (requerimiento original)
- **Auto-detección:** El código detecta automáticamente qué base de datos usar

## 🏗️ Arquitectura Hexagonal (Ports & Adapters)

El proyecto ha sido migrado a una arquitectura hexagonal que proporciona:

- **Separación clara de responsabilidades** entre capas
- **Independencia de infraestructura** (base de datos, frameworks)
- **Mejor testabilidad** y mantenibilidad
- **Flexibilidad** para cambiar tecnologías sin afectar el dominio

### Capas de la Arquitectura

```
src/main/java/com/taskmanager/
├── domain/                    # Capa de Dominio (Core Business)
│   ├── models/               # Entidades de dominio
│   │   └── Task.java         # Entidad Task con lógica de negocio
│   └── exceptions/           # Excepciones específicas del dominio
│       ├── TaskNotFoundException.java
│       └── TaskValidationException.java
├── application/              # Capa de Aplicación (Casos de Uso)
│   ├── ports/               # Puertos (interfaces)
│   │   ├── TaskRepository.java  # Puerto de persistencia
│   │   └── TaskService.java     # Puerto de servicios
│   └── services/            # Implementación de casos de uso
│       └── TaskServiceImpl.java
├── infrastructure/          # Capa de Infraestructura (Adaptadores)
│   ├── adapters/
│   │   ├── primary/        # Adaptadores primarios (entrada)
│   │   │   └── rest/      # Controladores REST
│   │   │       └── TaskController.java
│   │   └── secondary/      # Adaptadores secundarios (salida)
│   │       └── persistence/ # Persistencia
│   │           ├── entities/ # Entidades de persistencia
│   │           │   └── TaskEntity.java
│   │           └── TaskRepositoryImpl.java
│   └── config/             # Configuración de infraestructura
│       ├── DatabaseConnection.java
│       └── DependencyInjectionConfig.java
└── config/                 # Configuración de la aplicación
    ├── ApplicationConfig.java
    ├── CorsFilter.java
    ├── GenericExceptionMapper.java
    └── SwaggerConfig.java

src/main/resources/sql/
└── TASK_PKG.sql           # Script PL/SQL para Oracle

src/main/webapp/
├── WEB-INF/
│   └── web.xml           # Configuración web
└── swagger-index.html    # Documentación Swagger UI
```

### Flujo de la Arquitectura

```
Cliente HTTP → [Adaptador Primario: TaskController] 
              → [Puerto: TaskService] 
              → [Caso de Uso: TaskServiceImpl] 
              → [Puerto: TaskRepository] 
              → [Adaptador Secundario: TaskRepositoryImpl] 
              → Base de Datos (PostgreSQL/Oracle)
```

## Requisitos

- Java 8 o superior
- Maven 3.6+
- **Base de datos opcional:**
  - Oracle Database 11g/12c/19c (requerimiento original)
  - PostgreSQL 15+ (para desarrollo con Docker)
- Servidor de aplicaciones (Tomcat 8+, GlassFish, WildFly)

## Configuración de Base de Datos (Compatibilidad Dual)

La arquitectura hexagonal soporta **dos bases de datos** de forma transparente:

### 🐘 **PostgreSQL** (Recomendado para desarrollo con Docker)
- **Ventajas:** Fácil de instalar, ligero, ideal para desarrollo
- **Uso:** Docker, desarrollo local, pruebas
- **Script:** `docker/postgres/init.sql`

### 🏛️ **Oracle Database** (Requerimiento original)
- **Ventajas:** PL/SQL nativo, procedimientos almacenados
- **Uso:** Producción, entornos empresariales
- **Script:** `src/main/resources/sql/TASK_PKG.sql`

### Configuración Automática

La aplicación detecta automáticamente qué base de datos usar basándose en la variable de entorno `DB_DRIVER`:

```java
// PostgreSQL
DB_DRIVER=org.postgresql.Driver
DB_URL=jdbc:postgresql://localhost:5432/taskdb
DB_USER=taskuser
DB_PASSWORD=taskpass

// Oracle
DB_DRIVER=oracle.jdbc.driver.OracleDriver
DB_URL=jdbc:oracle:thin:@localhost:1521:XE
DB_USER=system
DB_PASSWORD=password
```

### 1. Configuración para PostgreSQL (Docker/Desarrollo)

```bash
# Variables de entorno para PostgreSQL
export DB_URL=jdbc:postgresql://localhost:5432/taskdb
export DB_USER=taskuser
export DB_PASSWORD=taskpass
export DB_DRIVER=org.postgresql.Driver
```

### 2. Configuración para Oracle (Producción)

```sql
-- Conectarse a Oracle como sysdba o usuario con privilegios
sqlplus system/password@localhost:1521/XE

-- Ejecutar el script PL/SQL
@src/main/resources/sql/TASK_PKG.sql
```

```bash
# Variables de entorno para Oracle
export DB_URL=jdbc:oracle:thin:@localhost:1521:XE
export DB_USER=system
export DB_PASSWORD=password
export DB_DRIVER=oracle.jdbc.driver.OracleDriver
```

### Características de la Compatibilidad Dual

1. **SQL Adaptativo:** El código genera SQL específico para cada base de datos
2. **Tipos de Datos:** Maneja diferencias (boolean vs integer para `completed`)
3. **Procedimientos:** Usa PL/SQL para Oracle, SQL estándar para PostgreSQL
4. **Transparente:** La capa de aplicación no necesita saber qué base de datos se usa

## Endpoints de la API (Arquitectura Hexagonal)

### URLs base según entorno:

| Entorno | URL Base | Puerto | Base de Datos |
|---------|----------|--------|---------------|
| **Docker** | `http://localhost:8080/task-api/api` | 8080 | PostgreSQL |
| **Jetty Local** | `http://localhost:8082/task-api/api` | 8082 | PostgreSQL/Oracle |
| **Tomcat** | `http://localhost:8080/task-api/api` | 8080 | Oracle |

**Nota:** Todos los ejemplos usan la URL de Docker. Para Jetty local, reemplaza `8080` por `8082`.

### Endpoints CRUD Básicos

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/tasks` | Obtener todas las tareas |
| GET | `/tasks/{id}` | Obtener una tarea por ID |
| POST | `/tasks` | Crear una nueva tarea |
| PUT | `/tasks/{id}` | Actualizar una tarea existente |
| DELETE | `/tasks/{id}` | Eliminar una tarea |

### Endpoints Avanzados (Nuevos con Arquitectura Hexagonal)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/tasks/{id}/complete` | Marcar tarea como completada |
| POST | `/tasks/{id}/pending` | Marcar tarea como pendiente |
| GET | `/tasks/completed` | Obtener tareas completadas |
| GET | `/tasks/pending` | Obtener tareas pendientes |
| GET | `/tasks/search?q={texto}` | Buscar tareas por título |
| GET | `/tasks/test` | Probar el servicio hexagonal |

### Características de los nuevos endpoints:

1. **Mejor manejo de errores** - Excepciones específicas del dominio
2. **Validación de negocio** - Lógica de validación en la capa de dominio
3. **Separación de responsabilidades** - Cada endpoint usa casos de uso específicos
4. **Compatibilidad dual** - Funciona con PostgreSQL (Docker) y Oracle (producción)

## 📚 Documentación de la API con Swagger/OpenAPI

La API incluye documentación automática generada con Swagger (OpenAPI 3) **actualizada para la arquitectura hexagonal**.

### Swagger UI (Arquitectura Hexagonal)
La documentación interactiva de la API está disponible en:

| Entorno | URL | Puerto | Base de Datos | Arquitectura |
|---------|-----|--------|---------------|--------------|
| **Docker** | `http://localhost:8080/task-api/swagger-index.html` | 8080 | PostgreSQL | Hexagonal |
| **Jetty Local** | `http://localhost:8082/task-api/swagger-index.html` | 8082 | PostgreSQL/Oracle | Hexagonal |
| **Tomcat** | `http://localhost:8080/task-api/swagger-index.html` | 8080 | Oracle | Hexagonal |

**Descripción:** Interfaz web interactiva para explorar y probar los endpoints de la API hexagonal. Contiene una especificación OpenAPI estática con **todos los nuevos endpoints** documentados.

### Características de la documentación hexagonal:
- **Documentación completa:** Todos los endpoints de la arquitectura hexagonal están documentados
- **Especificación estática:** La documentación está incrustada en la página HTML, no requiere conexión a `/openapi.json`
- **Pruebas interactivas:** Permite probar los endpoints directamente desde el navegador
- **Esquemas de datos:** Documenta los modelos de datos (Task) con sus propiedades y tipos
- **Ejemplos:** Incluye ejemplos de solicitudes y respuestas para todos los endpoints
- **Servidores múltiples:** Configurado para Docker (PostgreSQL) y Jetty local

### Novedades en la documentación hexagonal:
1. **Endpoints avanzados:** `/complete`, `/pending`, `/search`, etc.
2. **Mejores descripciones:** Explicaciones detalladas de cada operación
3. **Validaciones:** Especificación de reglas de validación (título requerido, etc.)
4. **Respuestas detalladas:** Códigos de estado específicos para cada escenario

### Ejemplos de Uso (Arquitectura Hexagonal)

#### CRUD Básico

##### Obtener todas las tareas
```bash
curl -X GET "http://localhost:8080/task-api/api/tasks"
```

##### Obtener una tarea por ID
```bash
curl -X GET "http://localhost:8080/task-api/api/tasks/1"
```

##### Crear una nueva tarea
```bash
curl -X POST "http://localhost:8080/task-api/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Nueva tarea hexagonal",
    "description": "Descripción de la tarea con arquitectura hexagonal",
    "completed": false
  }'
```

##### Actualizar una tarea
```bash
curl -X PUT "http://localhost:8080/task-api/api/tasks/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tarea actualizada hexagonal",
    "description": "Descripción actualizada con arquitectura hexagonal",
    "completed": true
  }'
```

##### Eliminar una tarea
```bash
curl -X DELETE "http://localhost:8080/task-api/api/tasks/1"
```

#### Endpoints Avanzados (Nuevos)

##### Marcar tarea como completada
```bash
curl -X POST "http://localhost:8080/task-api/api/tasks/1/complete"
```

##### Marcar tarea como pendiente
```bash
curl -X POST "http://localhost:8080/task-api/api/tasks/1/pending"
```

##### Obtener tareas completadas
```bash
curl -X GET "http://localhost:8080/task-api/api/tasks/completed"
```

##### Obtener tareas pendientes
```bash
curl -X GET "http://localhost:8080/task-api/api/tasks/pending"
```

##### Buscar tareas por título
```bash
curl -X GET "http://localhost:8080/task-api/api/tasks/search?q=importante"
```

##### Probar el servicio hexagonal
```bash
curl -X GET "http://localhost:8080/task-api/api/tasks/test"
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

## 🚀 Ejecución local con Jetty (Desarrollo Hexagonal)

El proyecto incluye configuración para ejecutar localmente con Jetty Maven Plugin, **optimizado para desarrollo con arquitectura hexagonal**.

### 1. Ejecutar la aplicación hexagonal con Jetty
```bash
# Ejecutar en modo desarrollo con arquitectura hexagonal
mvn jetty:run
```

### 2. Acceder a la aplicación hexagonal
- **API REST Hexagonal:** `http://localhost:8082/task-api/api`
- **Swagger UI Hexagonal:** `http://localhost:8082/task-api/swagger-index.html`
- **Endpoint de prueba hexagonal:** `http://localhost:8082/task-api/api/tasks/test`

### 3. Configuración para desarrollo local con arquitectura hexagonal

#### Opción A: Usar PostgreSQL local (recomendado para desarrollo hexagonal)
```bash
# 1. Configurar variables de entorno para PostgreSQL local
export DB_URL=jdbc:postgresql://localhost:5432/taskdb
export DB_USER=taskuser
export DB_PASSWORD=taskpass
export DB_DRIVER=org.postgresql.Driver

# 2. Ejecutar Jetty con arquitectura hexagonal
mvn jetty:run
```

#### Opción B: Usar Oracle local (si tienes Oracle instalado)
```bash
# 1. Configurar variables de entorno para Oracle local
export DB_URL=jdbc:oracle:thin:@localhost:1521:XE
export DB_USER=system
export DB_PASSWORD=password
export DB_DRIVER=oracle.jdbc.driver.OracleDriver

# 2. Ejecutar Jetty con arquitectura hexagonal
mvn jetty:run
```

#### Opción C: Usar archivo .env (Windows/Linux/Mac)
Crear un archivo `.env` en la raíz del proyecto:
```bash
# Para PostgreSQL (recomendado para desarrollo hexagonal)
DB_URL=jdbc:postgresql://localhost:5432/taskdb
DB_USER=taskuser
DB_PASSWORD=taskpass
DB_DRIVER=org.postgresql.Driver

# Para Oracle (producción/legado)
# DB_URL=jdbc:oracle:thin:@localhost:1521:XE
# DB_USER=system
# DB_PASSWORD=password
# DB_DRIVER=oracle.jdbc.driver.OracleDriver
```

Luego ejecutar:
```bash
# Linux/Mac
source .env && mvn jetty:run

# Windows (PowerShell)
Get-Content .env | ForEach-Object { if ($_ -match '^([^=]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2]) } }
mvn jetty:run
```

### 4. Comandos útiles para desarrollo con Jetty

```bash
# Ejecutar con recarga automática (hot reload)
mvn jetty:run -Djetty.scanIntervalSeconds=5

# Ejecutar en puerto diferente
mvn jetty:run -Djetty.port=9090

# Detener Jetty (desde otra terminal)
mvn jetty:stop

# Ver logs detallados
mvn jetty:run -Djetty.logs=target/jetty.log
```

### 5. Probar la aplicación hexagonal localmente
```bash
# Probar endpoint de prueba hexagonal
curl http://localhost:8082/task-api/api/tasks/test

# Listar tareas (hexagonal)
curl http://localhost:8082/task-api/api/tasks

# Crear una tarea (hexagonal)
curl -X POST "http://localhost:8082/task-api/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Tarea hexagonal local",
    "description": "Creada desde Jetty local con arquitectura hexagonal",
    "completed": false
  }'

# Probar endpoints avanzados (hexagonal)
curl -X POST "http://localhost:8082/task-api/api/tasks/1/complete"
curl -X GET "http://localhost:8082/task-api/api/tasks/completed"
curl -X GET "http://localhost:8082/task-api/api/tasks/search?q=hexagonal"
```

### Ventajas de usar Jetty localmente con arquitectura hexagonal:
- **Desarrollo rápido:** Recarga automática de cambios
- **Arquitectura limpia:** Separación clara de responsabilidades
- **Depuración fácil:** Puedes conectar debugger a capas específicas
- **Configuración flexible:** Cambia entre PostgreSQL y Oracle fácilmente
- **Testabilidad mejorada:** Cada capa puede probarse de forma aislada

## Configuración CORS

La API está configurada para permitir solicitudes desde cualquier origen (`*`). Los headers CORS están configurados en `CorsFilter.java`.

## Estructura de Base de Datos (Compatibilidad Dual)

### PostgreSQL (Desarrollo con Docker)
```sql
-- Tabla tasks en PostgreSQL
CREATE TABLE tasks (
    task_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(4000),
    completed INTEGER DEFAULT 0 NOT NULL CHECK (completed IN (0, 1)),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Trigger para updated_at automático
CREATE TRIGGER update_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

### Oracle Database (Producción)
```sql
-- Tabla TASKS en Oracle
CREATE TABLE TASKS (
    TASK_ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    TITLE VARCHAR2(200) NOT NULL,
    DESCRIPTION VARCHAR2(1000),
    COMPLETED NUMBER(1) DEFAULT 0 NOT NULL CHECK (COMPLETED IN (0, 1)),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UPDATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Paquete TASK_PKG (solo Oracle)
CREATE OR REPLACE PACKAGE TASK_PKG AS
    PROCEDURE GET_ALL_TASKS(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE GET_TASK_BY_ID(p_task_id IN NUMBER, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE CREATE_TASK(p_title IN VARCHAR2, p_description IN VARCHAR2, p_task_id OUT NUMBER);
    PROCEDURE UPDATE_TASK(p_task_id IN NUMBER, p_title IN VARCHAR2, p_description IN VARCHAR2, p_completed IN NUMBER);
    PROCEDURE DELETE_TASK(p_task_id IN NUMBER);
END TASK_PKG;
```

### Características de la compatibilidad dual:
1. **SQL Adaptativo:** La aplicación genera SQL específico para cada base de datos
2. **Tipos Compatibles:** `completed` como INTEGER (0/1) en ambas bases
3. **Auto-detección:** El código detecta automáticamente qué base de datos usar
4. **Transparencia:** La capa de aplicación no necesita saber qué base de datos se usa

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

## Dependencias Maven (Arquitectura Hexagonal)

### Core (Dominio y Aplicación)
- **Jersey (JAX-RS)** - Implementación de REST API
- **HK2 Dependency Injection** - Inyección de dependencias para arquitectura hexagonal
- **Jackson** - Procesamiento JSON

### Infraestructura
- **Oracle JDBC Driver** - Conexión a Oracle Database
- **PostgreSQL JDBC Driver** - Conexión a PostgreSQL (para Docker/desarrollo)
- **Jetty Servlets** - Compatibilidad con Jetty 9

### Documentación
- **Swagger/OpenAPI 3** - Documentación automática de API
- **WebJars** - Recursos web para Swagger UI

### Testing y Calidad
- **JUnit** - Framework de pruebas
- **Mockito** - Mocks para testing aislado
- **JaCoCo** - Cobertura de código (≥80% líneas, ≥70% ramas)

### Logging
- **SLF4J** - API de logging
- **SLF4J Simple** - Implementación simple para desarrollo

## Licencia

Este proyecto es para fines educativos y de demostración técnica.


## 🧪 Pruebas Unitarias (Arquitectura Hexagonal)

El proyecto incluye un conjunto completo de pruebas unitarias organizadas por capas de la arquitectura hexagonal:

### Estructura de Pruebas Actualizada

```
src/test/java/com/taskmanager/
├── domain/                    # Pruebas de dominio
│   └── models/
│       └── TaskTest.java     # Pruebas de la entidad Task
├── application/              # Pruebas de aplicación
│   └── services/
│       └── TaskServiceImplTest.java  # Pruebas del servicio
├── infrastructure/           # Pruebas de infraestructura
│   ├── adapters/
│   │   ├── primary/
│   │   │   └── rest/
│   │   │       └── TaskControllerTest.java  # Pruebas del controlador
│   │   └── secondary/
│   │       └── persistence/
│   │           └── TaskRepositoryImplTest.java  # Pruebas del repositorio
│   └── config/
│       └── DatabaseConnectionTest.java  # Pruebas de conexión
└── config/                  # Pruebas de configuración
    ├── ApplicationConfigTest.java
    ├── CorsFilterTest.java
    └── GenericExceptionMapperTest.java
```

### Características de las Pruebas Hexagonales

1. **Aislamiento por capas:** Cada capa se prueba de forma independiente
2. **Mocks y Stubs:** Uso de Mockito para simular dependencias
3. **Cobertura completa:** Pruebas para dominio, aplicación e infraestructura
4. **Pruebas de integración:** Verificación de que las capas funcionan juntas

### Ejecutar Pruebas

```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar pruebas con cobertura
mvn verify

# Ejecutar pruebas específicas
mvn test -Dtest=TaskServiceImplTest
mvn test -Dtest=TaskRepositoryImplTest
```

### Tipos de Pruebas Implementadas

#### 1. **Pruebas de Dominio (TaskTest.java)**
- Validación de creación de tareas
- Comportamiento de marcado como completada/pendiente
- Validación de reglas de negocio

#### 2. **Pruebas de Aplicación (TaskServiceImplTest.java)**
- Casos de uso con mocks del repositorio
- Manejo de excepciones del dominio
- Validación de parámetros de entrada

#### 3. **Pruebas de Infraestructura (TaskRepositoryImplTest.java)**
- Conexión a base de datos (PostgreSQL/Oracle)
- Operaciones CRUD con mocks de JDBC
- Manejo de excepciones SQL

#### 4. **Pruebas de Controlador (TaskControllerTest.java)**
- Endpoints REST con mocks del servicio
- Validación de respuestas HTTP
- Manejo de errores y excepciones

### Cobertura de Pruebas

La arquitectura hexagonal facilita alcanzar altos niveles de cobertura:

- **Dominio:** 100% - Lógica de negocio completamente probada
- **Aplicación:** 95% - Casos de uso con mocks
- **Infraestructura:** 85% - Adaptadores con mocks de JDBC
- **Controladores:** 90% - Endpoints REST probados

### Beneficios de las Pruebas Hexagonales

1. **Rápida ejecución:** Pruebas aisladas sin dependencias externas
2. **Fácil mantenimiento:** Cambios en una capa no afectan pruebas de otras
3. **Detección temprana:** Errores se identifican en la capa correcta
4. **Refactorización segura:** Puedes cambiar implementaciones sin romper pruebas

### Ejemplo de Prueba Hexagonal

```java
// Prueba del servicio de aplicación con mock del repositorio
@Test
public void testCreateTaskSuccess() {
    // Arrange
    String title = "Test Task";
    String description = "Test Description";
    Task savedTask = Task.create(title, description);
    savedTask.setTaskId(1L);
    
    when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
    
    // Act
    Task result = taskService.createTask(title, description);
    
    // Assert
    assertNotNull(result);
    assertEquals(Long.valueOf(1), result.getTaskId());
    assertEquals(title, result.getTitle());
    assertEquals(description, result.getDescription());
    verify(taskRepository).save(any(Task.class));
}
```

Esta estructura de pruebas asegura que cada capa de la arquitectura hexagonal funcione correctamente de forma aislada y en conjunto.