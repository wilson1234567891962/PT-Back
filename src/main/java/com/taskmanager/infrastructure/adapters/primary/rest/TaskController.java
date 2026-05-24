package com.taskmanager.infrastructure.adapters.primary.rest;

import com.taskmanager.application.ports.TaskService;
import com.taskmanager.config.ErrorResponse;
import com.taskmanager.domain.exceptions.TaskNotFoundException;
import com.taskmanager.domain.exceptions.TaskValidationException;
import com.taskmanager.domain.models.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST (Adaptador Primario) para Task
 * Expone los endpoints HTTP y adapta las requests/responses
 */
@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Tasks", description = "Operaciones CRUD para gestión de tareas")
public class TaskController {
    
    private TaskService taskService;
    
    public TaskController() {
        // Constructor sin parámetros para Jersey
    }
    
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }
    
    // DTO para request de creación/actualización
    public static class TaskRequest {
        @Schema(description = "ID único de la tarea", example = "1")
        public Long taskId;
        
        @Schema(description = "Título de la tarea", required = true, example = "Nueva tarea")
        public String title;
        
        @Schema(description = "Descripción de la tarea", example = "Descripción detallada")
        public String description;
        
        @Schema(description = "Estado de completado", example = "false")
        public Boolean completed;
    }
    
    // DTO para response
    public static class TaskResponse {
        @Schema(description = "ID único de la tarea", example = "1")
        public Long taskId;
        
        @Schema(description = "Título de la tarea", example = "Nueva tarea")
        public String title;
        
        @Schema(description = "Descripción de la tarea", example = "Descripción detallada")
        public String description;
        
        @Schema(description = "Estado de completado", example = "false")
        public Boolean completed;
        
        @Schema(description = "Fecha de creación", example = "2024-01-01T12:00:00")
        public String createdAt;
        
        @Schema(description = "Fecha de última actualización", example = "2024-01-01T12:00:00")
        public String updatedAt;
        
        public static TaskResponse fromDomain(Task task) {
            TaskResponse response = new TaskResponse();
            response.taskId = task.getTaskId();
            response.title = task.getTitle();
            response.description = task.getDescription();
            response.completed = task.getCompleted();
            response.createdAt = task.getCreatedAt() != null ? task.getCreatedAt().toString() : null;
            response.updatedAt = task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null;
            return response;
        }
    }
    
    @GET
    @Operation(
        summary = "Obtener todas las tareas",
        description = "Retorna una lista de todas las tareas almacenadas en la base de datos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tareas obtenida exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class, type = "array"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al obtener las tareas"
        )
    })
    public Response getAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            List<TaskResponse> responses = tasks.stream()
                .map(TaskResponse::fromDomain)
                .collect(Collectors.toList());
            return Response.ok(responses).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al obtener las tareas: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @GET
    @Path("/{id}")
    @Operation(
        summary = "Obtener una tarea por ID",
        description = "Retorna una tarea específica basada en su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tarea encontrada exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID proporcionado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al obtener la tarea"
        )
    })
    public Response getTaskById(
            @Parameter(description = "ID de la tarea a obtener", required = true, example = "1")
            @PathParam("id") Long id) {
        try {
            Task task = taskService.getTaskById(id);
            return Response.ok(TaskResponse.fromDomain(task)).build();
        } catch (TaskNotFoundException e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.NOT_FOUND.getStatusCode(),
                e.getMessage(),
                "NOT_FOUND",
                "/api/tasks/" + id
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al obtener la tarea: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/" + id
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @POST
    @Operation(
        summary = "Crear una nueva tarea",
        description = "Crea una nueva tarea en la base de datos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Tarea creada exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida - El título es requerido"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al crear la tarea"
        )
    })
    public Response createTask(
            @Parameter(description = "Objeto Task a crear", required = true,
                schema = @Schema(implementation = TaskRequest.class))
            TaskRequest taskRequest) {
        try {
            Task task = taskService.createTask(taskRequest.title, taskRequest.description);
            return Response.status(Response.Status.CREATED)
                    .entity(TaskResponse.fromDomain(task))
                    .build();
        } catch (TaskValidationException e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                e.getMessage(),
                "BAD_REQUEST",
                "/api/tasks"
            );
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al crear la tarea: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Operation(
        summary = "Actualizar una tarea existente",
        description = "Actualiza una tarea existente basada en su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tarea actualizada exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Solicitud inválida - El título es requerido"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID proporcionado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al actualizar la tarea"
        )
    })
    public Response updateTask(
            @Parameter(description = "ID de la tarea a actualizar", required = true, example = "1")
            @PathParam("id") Long id,
            @Parameter(description = "Objeto Task con los datos actualizados", required = true,
                schema = @Schema(implementation = TaskRequest.class))
            TaskRequest taskRequest) {
        try {
            Task task = taskService.updateTask(id, taskRequest.title, taskRequest.description, taskRequest.completed);
            return Response.ok(TaskResponse.fromDomain(task)).build();
        } catch (TaskNotFoundException e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.NOT_FOUND.getStatusCode(),
                e.getMessage(),
                "NOT_FOUND",
                "/api/tasks/" + id
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        } catch (TaskValidationException e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.BAD_REQUEST.getStatusCode(),
                e.getMessage(),
                "BAD_REQUEST",
                "/api/tasks/" + id
            );
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al actualizar la tarea: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/" + id
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "Eliminar una tarea",
        description = "Elimina una tarea específica basada en su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Tarea eliminada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID proporcionado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al eliminar la tarea"
        )
    })
    public Response deleteTask(
            @Parameter(description = "ID de la tarea a eliminar", required = true, example = "1")
            @PathParam("id") Long id) {
        try {
            taskService.deleteTask(id);
            return Response.noContent().build();
        } catch (TaskNotFoundException e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.NOT_FOUND.getStatusCode(),
                e.getMessage(),
                "NOT_FOUND",
                "/api/tasks/" + id
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al eliminar la tarea: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/" + id
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @POST
    @Path("/{id}/complete")
    @Operation(
        summary = "Marcar tarea como completada",
        description = "Marca una tarea específica como completada"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tarea marcada como completada exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID proporcionado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public Response markTaskAsCompleted(
            @Parameter(description = "ID de la tarea a marcar como completada", required = true, example = "1")
            @PathParam("id") Long id) {
        try {
            Task task = taskService.markTaskAsCompleted(id);
            return Response.ok(TaskResponse.fromDomain(task)).build();
        } catch (TaskNotFoundException e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.NOT_FOUND.getStatusCode(),
                e.getMessage(),
                "NOT_FOUND",
                "/api/tasks/" + id + "/complete"
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al marcar tarea como completada: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/" + id + "/complete"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @POST
    @Path("/{id}/pending")
    @Operation(
        summary = "Marcar tarea como pendiente",
        description = "Marca una tarea específica como pendiente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tarea marcada como pendiente exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID proporcionado"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public Response markTaskAsPending(
            @Parameter(description = "ID de la tarea a marcar como pendiente", required = true, example = "1")
            @PathParam("id") Long id) {
        try {
            Task task = taskService.markTaskAsPending(id);
            return Response.ok(TaskResponse.fromDomain(task)).build();
        } catch (TaskNotFoundException e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.NOT_FOUND.getStatusCode(),
                e.getMessage(),
                "NOT_FOUND",
                "/api/tasks/" + id + "/pending"
            );
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al marcar tarea como pendiente: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/" + id + "/pending"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @GET
    @Path("/completed")
    @Operation(
        summary = "Obtener tareas completadas",
        description = "Retorna una lista de todas las tareas completadas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tareas completadas obtenida exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class, type = "array"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public Response getCompletedTasks() {
        try {
            List<Task> tasks = taskService.getCompletedTasks();
            List<TaskResponse> responses = tasks.stream()
                .map(TaskResponse::fromDomain)
                .collect(Collectors.toList());
            return Response.ok(responses).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al obtener tareas completadas: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/completed"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @GET
    @Path("/pending")
    @Operation(
        summary = "Obtener tareas pendientes",
        description = "Retorna una lista de todas las tareas pendientes"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tareas pendientes obtenida exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class, type = "array"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public Response getPendingTasks() {
        try {
            List<Task> tasks = taskService.getPendingTasks();
            List<TaskResponse> responses = tasks.stream()
                .map(TaskResponse::fromDomain)
                .collect(Collectors.toList());
            return Response.ok(responses).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al obtener tareas pendientes: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/pending"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    @GET
    @Path("/search")
    @Operation(
        summary = "Buscar tareas por título",
        description = "Busca tareas que contengan el texto especificado en el título"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de tareas encontradas exitosamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = TaskResponse.class, type = "array"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public Response searchTasksByTitle(
            @Parameter(description = "Texto a buscar en el título", required = true, example = "tarea")
            @QueryParam("q") String query) {
        try {
            List<Task> tasks = taskService.searchTasksByTitle(query);
            List<TaskResponse> responses = tasks.stream()
                .map(TaskResponse::fromDomain)
                .collect(Collectors.toList());
            return Response.ok(responses).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error al buscar tareas: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/search"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
    
    // Endpoint adicional para probar el servicio
    @GET
    @Path("/test")
    @Operation(
        summary = "Probar el servicio y conexión a base de datos",
        description = "Endpoint de prueba para verificar que el servicio y la conexión a la base de datos funcionan correctamente"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Servicio probado exitosamente"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error en la prueba del servicio"
        )
    })
    public Response testService() {
        try {
            // Crear una tarea de prueba
            Task testTask = taskService.createTask("Tarea de prueba hexagonal", "Esta es una tarea de prueba para la arquitectura hexagonal");
            
            // Obtener todas las tareas
            List<Task> tasks = taskService.getAllTasks();
            
            // Eliminar la tarea de prueba
            taskService.deleteTask(testTask.getTaskId());
            
            // Construir respuesta JSON estructurada
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("status", "success");
            result.put("message", "Servicio hexagonal probado exitosamente");
            result.put("totalTasks", tasks.size());
            result.put("timestamp", java.time.Instant.now().toString());
            
            return Response.ok(result).build();
        } catch (Exception e) {
            ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Error en la prueba hexagonal: " + e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                "/api/tasks/test"
            );
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
    }
}