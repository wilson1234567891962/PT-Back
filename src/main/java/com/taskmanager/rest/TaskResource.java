package com.taskmanager.rest;

import com.taskmanager.dao.TaskDAO;
import com.taskmanager.model.Task;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("/tasks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaskResource {
    
    private TaskDAO taskDAO = new TaskDAO();
    
    @GET
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
    public Response getTaskById(@PathParam("id") Long id) {
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
    public Response createTask(Task task) {
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
    public Response updateTask(@PathParam("id") Long id, Task task) {
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
    public Response deleteTask(@PathParam("id") Long id) {
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