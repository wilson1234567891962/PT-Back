@echo off
REM Script de utilidad para Docker - Backend Task Manager (Windows)
REM Ejecutar: docker-commands.bat [comando]

setlocal enabledelayedexpansion

REM Colores (solo para PowerShell, en CMD se muestran sin color)
set RED=
set GREEN=
set YELLOW=
set BLUE=
set NC=

REM Función para imprimir mensajes
:print_info
    echo [INFO] %*
    goto :eof

:print_success
    echo [SUCCESS] %*
    goto :eof

:print_warning
    echo [WARNING] %*
    goto :eof

:print_error
    echo [ERROR] %*
    goto :eof

REM Función para mostrar ayuda
:show_help
    echo Uso: docker-commands.bat [comando]
    echo.
    echo Comandos disponibles:
    echo   build           Construir imagen Docker
    echo   run             Ejecutar contenedor individual
    echo   compose-up      Iniciar con docker-compose (PostgreSQL)
    echo   compose-dev     Iniciar modo desarrollo (sin DB)
    echo   compose-oracle  Iniciar con Oracle externa
    echo   stop            Detener todos los contenedores
    echo   clean           Limpiar recursos Docker
    echo   logs            Ver logs de la API
    echo   logs-db         Ver logs de la base de datos
    echo   status          Ver estado de contenedores
    echo   test            Probar la API
    echo   help            Mostrar esta ayuda
    echo.
    echo Ejemplos:
    echo   docker-commands.bat compose-up   REM Iniciar con PostgreSQL
    echo   docker-commands.bat logs         REM Ver logs de la API
    echo   docker-commands.bat test         REM Probar endpoints de la API
    goto :eof

REM Función para construir imagen
:build_image
    call :print_info "Construyendo imagen Docker..."
    docker build -f Dockerfile.build -t task-manager-api:latest .
    call :print_success "Imagen construida: task-manager-api:latest"
    goto :eof

REM Función para ejecutar contenedor individual
:run_container
    call :print_info "Ejecutando contenedor individual..."
    docker run -p 8080:8080 --name task-api-standalone task-manager-api:latest
    goto :eof

REM Función para iniciar con docker-compose (PostgreSQL)
:compose_up
    call :print_info "Iniciando con docker-compose (PostgreSQL)..."
    docker-compose up --build
    goto :eof

REM Función para iniciar modo desarrollo
:compose_dev
    call :print_info "Iniciando modo desarrollo (sin base de datos)..."
    docker-compose -f docker-compose-dev.yml up --build
    goto :eof

REM Función para iniciar con Oracle externa
:compose_oracle
    call :print_warning "Asegúrate de tener el driver JDBC de Oracle en .\oracle-driver\"
    call :print_info "Iniciando con Oracle externa..."
    docker-compose -f docker-compose-oracle-external.yml up --build
    goto :eof

REM Función para detener contenedores
:stop_containers
    call :print_info "Deteniendo contenedores..."
    
    REM Detener contenedores de docker-compose
    docker-compose down 2>nul
    docker-compose -f docker-compose-dev.yml down 2>nul
    docker-compose -f docker-compose-oracle-external.yml down 2>nul
    
    REM Detener contenedores individuales
    docker stop task-api-standalone 2>nul
    docker rm task-api-standalone 2>nul
    
    call :print_success "Contenedores detenidos"
    goto :eof

REM Función para limpiar recursos
:clean_resources
    call :print_warning "Esta acción eliminará contenedores, imágenes y volúmenes no utilizados"
    set /p response="¿Continuar? (y/N): "
    if /i "!response!"=="y" (
        call :print_info "Limpiando recursos Docker..."
        
        REM Detener contenedores primero
        call :stop_containers
        
        REM Eliminar recursos no utilizados
        docker system prune -a -f --volumes
        
        call :print_success "Recursos Docker limpiados"
    ) else (
        call :print_info "Operación cancelada"
    )
    goto :eof

REM Función para ver logs
:show_logs
    call :print_info "Mostrando logs de la API..."
    docker-compose logs -f task-api 2>nul
    if errorlevel 1 (
        docker-compose -f docker-compose-dev.yml logs -f task-api 2>nul
        if errorlevel 1 (
            docker-compose -f docker-compose-oracle-external.yml logs -f task-api 2>nul
            if errorlevel 1 (
                docker logs -f task-api-standalone 2>nul
                if errorlevel 1 (
                    call :print_error "No se encontraron contenedores de la API en ejecución"
                )
            )
        )
    )
    goto :eof

REM Función para ver logs de base de datos
:show_db_logs
    call :print_info "Mostrando logs de la base de datos..."
    docker-compose logs -f postgres 2>nul
    if errorlevel 1 (
        call :print_error "No se encontró contenedor de base de datos en ejecución"
    )
    goto :eof

REM Función para ver estado
:show_status
    call :print_info "Estado de contenedores:"
    echo.
    
    REM Contenedores relacionados con Task Manager
    docker ps --filter "name=task" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    echo.
    call :print_info "Imágenes:"
    docker images --filter "reference=task-manager*" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
    
    echo.
    call :print_info "Volúmenes:"
    docker volume ls --filter "name=task" --format "table {{.Name}}\t{{.Driver}}"
    goto :eof

REM Función para probar la API
:test_api
    call :print_info "Probando API..."
    
    REM Esperar un momento para que la API esté lista
    timeout /t 3 /nobreak >nul
    
    REM Probar endpoint de health check
    curl -s -f "http://localhost:8080/task-api/api/tasks/test" >nul
    if not errorlevel 1 (
        call :print_success "API está funcionando correctamente"
        
        echo.
        call :print_info "Probando endpoint GET /tasks:"
        curl -s "http://localhost:8080/task-api/api/tasks"
        
        echo.
        echo.
        call :print_info "Swagger UI disponible en: http://localhost:8080/task-api/swagger-ui/"
        
    ) else (
        call :print_error "API no responde. Verifica que esté ejecutándose."
        call :print_info "Intenta ejecutar: docker-commands.bat compose-up"
    )
    goto :eof

REM Manejo de comandos
if "%1"=="" goto show_help

if "%1"=="build" goto build_image
if "%1"=="run" goto run_container
if "%1"=="compose-up" goto compose_up
if "%1"=="compose-dev" goto compose_dev
if "%1"=="compose-oracle" goto compose_oracle
if "%1"=="stop" goto stop_containers
if "%1"=="clean" goto clean_resources
if "%1"=="logs" goto show_logs
if "%1"=="logs-db" goto show_db_logs
if "%1"=="status" goto show_status
if "%1"=="test" goto test_api
if "%1"=="help" goto show_help
if "%1"=="--help" goto show_help
if "%1"=="-h" goto show_help

call :print_error "Comando no reconocido: %1"
echo.
call :show_help
exit /b 1

:show_help
    call :show_help
    exit /b 0

:build_image
    call :build_image
    exit /b 0

:run_container
    call :run_container
    exit /b 0

:compose_up
    call :compose_up
    exit /b 0

:compose_dev
    call :compose_dev
    exit /b 0

:compose_oracle
    call :compose_oracle
    exit /b 0

:stop_containers
    call :stop_containers
    exit /b 0

:clean_resources
    call :clean_resources
    exit /b 0

:show_logs
    call :show_logs
    exit /b 0

:show_db_logs
    call :show_db_logs
    exit /b 0

:show_status
    call :show_status
    exit /b 0

:test_api
    call :test_api
    exit /b 0