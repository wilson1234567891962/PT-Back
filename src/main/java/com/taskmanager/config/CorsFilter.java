package com.taskmanager.config;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        
        // Configurar headers CORS
        responseContext.getHeaders().add(
            "Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add(
            "Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add(
            "Access-Control-Allow-Headers",
            "origin, content-type, accept, authorization, x-requested-with");
        responseContext.getHeaders().add(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        responseContext.getHeaders().add(
            "Access-Control-Max-Age", "1209600");
        
        // Manejar preflight requests
        if ("OPTIONS".equals(requestContext.getMethod())) {
            responseContext.setStatus(200);
        }
    }
}