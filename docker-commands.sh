#!/bin/bash
# Script de utilidad para Docker - Backend Task Manager
# Ejecutar: ./docker-commands.sh [comando]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir mensajes
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Función para mostrar ayuda
show_help() {
    echo "Uso: $0 [comando]"
    echo ""
    echo "Comandos disponibles:"
    echo "  build           Construir imagen Docker"
    echo "  run             Ejecutar contenedor individual"
    echo "  compose-up      Iniciar con docker-compose (PostgreSQL)"
    echo "  compose-dev     Iniciar modo desarrollo (sin DB)"
    echo "  compose-oracle  Iniciar con Oracle externa"
    echo "  stop            Detener todos los contenedores"
    echo "  clean           Limpiar recursos Docker"
    echo "  logs            Ver logs de la API"
    echo "  logs-db         Ver logs de la base de datos"
    echo "  status          Ver estado de contenedores"
    echo "  test            Probar la API"
    echo "  help            Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  $0 compose-up   # Iniciar con PostgreSQL"
    echo "  $0 logs         # Ver logs de la API"
    echo "  $0 test         # Probar endpoints de la API"
}

# Función para construir imagen
build_image() {
    print_info "Construyendo imagen Docker..."
    docker build -f Dockerfile.build -t task-manager-api:latest .
    print_success "Imagen construida: task-manager-api:latest"
}

# Función para ejecutar contenedor individual
run_container() {
    print_info "Ejecutando contenedor individual..."
    docker run -p 8080:8080 --name task-api-standalone task-manager-api:latest
}

# Función para iniciar con docker-compose (PostgreSQL)
compose_up() {
    print_info "Iniciando con docker-compose (PostgreSQL)..."
    docker-compose up --build
}

# Función para iniciar modo desarrollo
compose_dev() {
    print_info "Iniciando modo desarrollo (sin base de datos)..."
    docker-compose -f docker-compose-dev.yml up --build
}

# Función para iniciar con Oracle externa
compose_oracle() {
    print_warning "Asegúrate de tener el driver JDBC de Oracle en ./oracle-driver/"
    print_info "Iniciando con Oracle externa..."
    docker-compose -f docker-compose-oracle-external.yml up --build
}

# Función para detener contenedores
stop_containers() {
    print_info "Deteniendo contenedores..."
    
    # Detener contenedores de docker-compose
    docker-compose down 2>/dev/null || true
    docker-compose -f docker-compose-dev.yml down 2>/dev/null || true
    docker-compose -f docker-compose-oracle-external.yml down 2>/dev/null || true
    
    # Detener contenedores individuales
    docker stop task-api-standalone 2>/dev/null || true
    docker rm task-api-standalone 2>/dev/null || true
    
    print_success "Contenedores detenidos"
}

# Función para limpiar recursos
clean_resources() {
    print_warning "Esta acción eliminará contenedores, imágenes y volúmenes no utilizados"
    read -p "¿Continuar? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Limpiando recursos Docker..."
        
        # Detener contenedores primero
        stop_containers
        
        # Eliminar recursos no utilizados
        docker system prune -a -f --volumes
        
        print_success "Recursos Docker limpiados"
    else
        print_info "Operación cancelada"
    fi
}

# Función para ver logs
show_logs() {
    print_info "Mostrando logs de la API..."
    docker-compose logs -f task-api 2>/dev/null || \
    docker-compose -f docker-compose-dev.yml logs -f task-api 2>/dev/null || \
    docker-compose -f docker-compose-oracle-external.yml logs -f task-api 2>/dev/null || \
    docker logs -f task-api-standalone 2>/dev/null || \
    print_error "No se encontraron contenedores de la API en ejecución"
}

# Función para ver logs de base de datos
show_db_logs() {
    print_info "Mostrando logs de la base de datos..."
    docker-compose logs -f postgres 2>/dev/null || \
    print_error "No se encontró contenedor de base de datos en ejecución"
}

# Función para ver estado
show_status() {
    print_info "Estado de contenedores:"
    echo ""
    
    # Contenedores relacionados con Task Manager
    docker ps --filter "name=task" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    echo ""
    print_info "Imágenes:"
    docker images --filter "reference=task-manager*" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
    
    echo ""
    print_info "Volúmenes:"
    docker volume ls --filter "name=task" --format "table {{.Name}}\t{{.Driver}}"
}

# Función para probar la API
test_api() {
    print_info "Probando API..."
    
    # Esperar un momento para que la API esté lista
    sleep 3
    
    # Probar endpoint de health check
    if curl -s -f "http://localhost:8080/task-api/api/tasks/test" > /dev/null; then
        print_success "API está funcionando correctamente"
        
        # Probar obtener todas las tareas
        echo ""
        print_info "Probando endpoint GET /tasks:"
        curl -s "http://localhost:8080/task-api/api/tasks" | jq . 2>/dev/null || \
        curl -s "http://localhost:8080/task-api/api/tasks"
        
        # Probar Swagger UI
        echo ""
        print_info "Swagger UI disponible en: http://localhost:8080/task-api/swagger-ui/"
        
    else
        print_error "API no responde. Verifica que esté ejecutándose."
        print_info "Intenta ejecutar: $0 compose-up"
    fi
}

# Manejo de comandos
case "$1" in
    "build")
        build_image
        ;;
    "run")
        run_container
        ;;
    "compose-up")
        compose_up
        ;;
    "compose-dev")
        compose_dev
        ;;
    "compose-oracle")
        compose_oracle
        ;;
    "stop")
        stop_containers
        ;;
    "clean")
        clean_resources
        ;;
    "logs")
        show_logs
        ;;
    "logs-db")
        show_db_logs
        ;;
    "status")
        show_status
        ;;
    "test")
        test_api
        ;;
    "help"|"--help"|"-h"|"")
        show_help
        ;;
    *)
        print_error "Comando no reconocido: $1"
        echo ""
        show_help
        exit 1
        ;;
esac

exit 0