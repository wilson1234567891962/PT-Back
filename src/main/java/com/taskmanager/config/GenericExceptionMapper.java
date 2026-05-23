package com.taskmanager.config;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        
        // Mapear diferentes tipos de excepciones a respuestas HTTP apropiadas
        if (exception instanceof SQLException) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error de base de datos: " + exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Parámetros inválidos: " + exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        
        if (exception instanceof javax.ws.rs.NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Recurso no encontrado: " + exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        
        // Excepción genérica
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Error interno del servidor: " + exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
    
    // Clase interna para respuestas de error estandarizadas
    public static class ErrorResponse {
        private String message;
        private long timestamp;
        
        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}