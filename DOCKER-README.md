# Dockerización del Backend Task Manager

Este documento describe cómo desplegar la API REST de Task Manager usando Docker.

## Archivos Docker disponibles

### 1. **Dockerfile** (`Dockerfile`)
- **Propósito:** Para despliegue cuando ya existe el archivo WAR construido
- **Uso:** Copia el archivo `task-api.war` ya construido al contenedor Tomcat
- **Requisito:** El archivo `target/task-api.war` debe existir previamente

### 2. **Dockerfile.build** (`Dockerfile.build`)
- **Propósito:** Construcción completa desde el código fuente (recomendado)
- **Características:**
  - Etapa 1: Construye la aplicación con Maven
  - Etapa 2: Crea imagen de producción con Tomcat
- **Ventaja:** No requiere tener el WAR construido localmente

### 3. **docker-compose.yml** (`docker-compose.yml`)
- **Propósito:** Despliegue completo con PostgreSQL (alternativa a Oracle)
- **Servicios incluidos:**
  - `postgres`: Base de datos PostgreSQL 15
  - `task-api`: API REST de Task Manager
  - `pgadmin`: Interfaz web para administrar PostgreSQL (opcional)
- **Puertos:**
  - API: `http://localhost:8080/task-api/api`
  - PostgreSQL: `localhost:5432`
  - pgAdmin: `http://localhost:5050` (usuario: admin@taskmanager.com, contraseña: admin123)

### 4. **docker-compose-oracle-external.yml** (`docker-compose-oracle-external.yml`)
- **Propósito:** Conectar a una base de datos Oracle externa
- **Configuración:** Modificar las variables de entorno para apuntar a tu Oracle
- **Nota:** Requiere tener el driver JDBC de Oracle montado en el contenedor

### 5. **docker-compose-dev.yml** (`docker-compose-dev.yml`)
- **Propósito:** Desarrollo local con modo de prueba (sin base de datos real)
- **Características:**
  - Usa `DatabaseConnection.setTestMode(true)`
  - Monta el código fuente para desarrollo en caliente
  - Incluye puerto para debugging remoto (8000)

## Instrucciones de uso

### Opción 1: Usar PostgreSQL (recomendado para desarrollo)

```bash
# Construir y ejecutar con docker-compose
cd PT-Back
docker-compose up --build

# Ejecutar en segundo plano
docker-compose up -d --build

# Ver logs
docker-compose logs -f task-api

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

### Opción 2: Modo desarrollo (sin base de datos)

```bash
# Usar configuración de desarrollo
cd PT-Back
docker-compose -f docker-compose-dev.yml up --build
```

### Opción 3: Conectar a Oracle externa

1. **Preparar el driver JDBC de Oracle:**
   ```bash
   mkdir -p PT-Back/oracle-driver
   # Copiar ojdbc8.jar a PT-Back/oracle-driver/
   ```

2. **Configurar conexión:** Editar `docker-compose-oracle-external.yml`:
   ```yaml
   environment:
     DB_URL: jdbc:oracle:thin:@tu-host:1521:tu-sid
     DB_USER: tu-usuario
     DB_PASSWORD: tu-contraseña
   ```

3. **Ejecutar:**
   ```bash
   cd PT-Back
   docker-compose -f docker-compose-oracle-external.yml up --build
   ```

### Opción 4: Construir imagen Docker manualmente

```bash
# Construir imagen desde Dockerfile.build
cd PT-Back
docker build -f Dockerfile.build -t task-manager-api:latest .

# Ejecutar contenedor
docker run -p 8080:8080 --name task-api task-manager-api:latest

# Ejecutar con variables de entorno
docker run -p 8080:8080 \
  -e DB_URL="jdbc:oracle:thin:@localhost:1521:XE" \
  -e DB_USER="system" \
  -e DB_PASSWORD="oracle" \
  --name task-api \
  task-manager-api:latest
```

## Acceso a la aplicación

- **API REST:** `http://localhost:8080/task-api/api`
- **Swagger UI:** `http://localhost:8080/task-api/swagger-ui/`
- **Endpoint de prueba:** `http://localhost:8080/task-api/api/tasks/test`
- **Health check:** `http://localhost:8080/task-api/api/tasks` (GET)

## Variables de entorno configurables

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `DB_URL` | URL de conexión JDBC | `jdbc:postgresql://postgres:5432/taskdb` |
| `DB_USER` | Usuario de base de datos | `taskuser` |
| `DB_PASSWORD` | Contraseña de base de datos | `taskpass` |
| `DB_DRIVER` | Driver JDBC | `org.postgresql.Driver` |
| `JAVA_OPTS` | Opciones de JVM | `-Xmx512m -Xms256m` |
| `TZ` | Zona horaria | `America/Bogota` |
| `ENVIRONMENT` | Entorno (dev/prod) | `development` |
| `TEST_MODE` | Modo de prueba | `false` |

## Solución de problemas

### 1. Puerto 8080 ya en uso
```bash
# Cambiar puerto en docker-compose.yml
ports:
  - "8081:8080"  # Usar puerto 8081 en host
```

### 2. Error de conexión a base de datos
- Verificar que PostgreSQL esté ejecutándose: `docker-compose ps`
- Revisar logs: `docker-compose logs postgres`
- Verificar credenciales en variables de entorno

### 3. Problemas de permisos en volúmenes
```bash
# En Linux/Mac
sudo chown -R $USER:$USER .

# En Windows, ejecutar Docker Desktop como administrador
```

### 4. Limpiar recursos Docker
```bash
# Eliminar contenedores detenidos
docker container prune

# Eliminar imágenes no utilizadas
docker image prune

# Eliminar volúmenes no utilizados
docker volume prune

# Eliminar todo (cuidado)
docker system prune -a
```

## Notas importantes

1. **Oracle Database:** No hay imagen oficial de Oracle en Docker Hub. Se recomienda:
   - Usar PostgreSQL para desarrollo (configuración incluida)
   - Conectar a Oracle externa existente
   - Usar modo de prueba para desarrollo rápido

2. **Persistencia de datos:** Los datos de PostgreSQL se guardan en volumen `postgres_data`

3. **Seguridad:** Cambiar contraseñas por defecto en producción

4. **Rendimiento:** Ajustar `JAVA_OPTS` según recursos disponibles

5. **Logs:** Los logs de Tomcat están disponibles en volumen `api_logs`