package com.taskmanager.rest;

import com.taskmanager.dao.TaskDAO;
import com.taskmanager.model.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Tasks", description = "Operaciones CRUD para gestión de tareas")
public class TaskResource {
    
    private TaskDAO taskDAO = new TaskDAO();
    
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
                schema = @Schema(implementation = Task.class, type = "array"))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al obtener las tareas"
        )
    })
    public Response getAllTasks() {
        try {
            List<Task> tasks = taskDAO.getAllTasks();
            return Response.ok(tasks).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener las tareas: " + e.getMessage())
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
                schema = @Schema(implementation = Task.class))
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
            Task task = taskDAO.getTaskById(id);
            if (task == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tarea no encontrada con ID: " + id)
                        .build();
            }
            return Response.ok(task).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al obtener la tarea: " + e.getMessage())
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
                schema = @Schema(implementation = Task.class))
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
                schema = @Schema(implementation = Task.class))
            Task task) {
        try {
            // Validaciones básicas
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El título es requerido")
                        .build();
            }
            
            Task createdTask = taskDAO.createTask(task);
            return Response.status(Response.Status.CREATED)
                    .entity(createdTask)
                    .build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al crear la tarea: " + e.getMessage())
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
                schema = @Schema(implementation = Task.class))
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
                schema = @Schema(implementation = Task.class))
            Task task) {
        try {
            // Verificar si la tarea existe
            Task existingTask = taskDAO.getTaskById(id);
            if (existingTask == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tarea no encontrada con ID: " + id)
                        .build();
            }
            
            // Validaciones básicas
            if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("El título es requerido")
                        .build();
            }
            
            Task updatedTask = taskDAO.updateTask(id, task);
            return Response.ok(updatedTask).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al actualizar la tarea: " + e.getMessage())
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
            // Verificar si la tarea existe
            Task existingTask = taskDAO.getTaskById(id);
            if (existingTask == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Tarea no encontrada con ID: " + id)
                        .build();
            }
            
            boolean deleted = taskDAO.deleteTask(id);
            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error al eliminar la tarea")
                        .build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al eliminar la tarea: " + e.getMessage())
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
            taskDAO.testPLSQLPackage();
            return Response.ok("Servicio probado exitosamente").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la prueba: " + e.getMessage())
                    .build();
        }
    }
}