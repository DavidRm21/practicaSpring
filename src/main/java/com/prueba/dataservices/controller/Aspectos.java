package com.prueba.dataservices.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class Aspectos {

    // Define un Pointcut para interceptar todos los métodos en controladores
    @Pointcut("execution(* com.prueba.dataservices.controller..*(..))")
    public void controllerMethods() {}

    // Antes de la ejecución del método
    @Before("controllerMethods()")
    public void before(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        log.info("Aspecto1");
        // Genera un ID de correlación si no está presente en la solicitud
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        // Log de información de la solicitud
        String path = request.getRequestURI();
        String method = request.getMethod();
        String headers = getHeadersInfo(request);

        System.out.println("Correlation ID: " + correlationId);
        System.out.println("Request Path: " + path);
        System.out.println("HTTP Method: " + method);
        System.out.println("Headers: " + headers);

        // Podrías guardar esta información en algún contexto
    }

    // Después de la ejecución del método, pero antes de retornar
    @AfterReturning(pointcut = "controllerMethods()", returning = "response")
    public void afterReturning(JoinPoint joinPoint, ResponseEntity<?> response) {
        HttpServletResponse servletResponse = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        if (servletResponse != null) {
            servletResponse.addHeader("X-Correlation-Id", getCorrelationId());
        }
        log.info("Aspecto2");

        // Log de información de la respuesta
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());
        log.info("Correlation {}", servletResponse.getHeader("X-Correlation-Id") );
    }

    // Métodos auxiliares
    private String getHeadersInfo(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        log.info("Aspecto3");
        StringBuilder headers = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue).append(", ");
        }
        return headers.toString();
    }

    private String getCorrelationId() {
        // Lógica para obtener el ID de correlación del contexto
        // Podrías usar algo como ThreadLocal para almacenar el ID de correlación
        log.info("Aspecto4");
        return UUID.randomUUID().toString(); // Ejemplo básico
    }

}


