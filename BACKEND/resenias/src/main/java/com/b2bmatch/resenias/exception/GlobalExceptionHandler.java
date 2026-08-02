package com.b2bmatch.resenias.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

/* 5. Exceptions
Cuando escribes el servicio empiezan a aparecer casos como:
Review no encontrada.
Rating inválido.
Cliente inexistente.
Profesional inexistente.
Ahí tiene sentido crear excepciones personalizadas, por ejemplo:

exception
├── ReviewNotFoundException.java
├── BadRequestException.java
└── GlobalExceptionHandler.java

El GlobalExceptionHandler suele llevar:
@RestControllerAdvice
para transformar las excepciones en respuestas HTTP apropiadas. */

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja respuestas con código HTTP específico (p. ej. 403 por falta de permisos)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                ex.getStatusCode().value(),
                ex.getStatusCode().value() == 403 ? HttpStatus.FORBIDDEN.getReasonPhrase() : "Error",
                ex.getReason(),
                request.getRequestURI()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // Maneja recursos que no existen
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        // Construye la respuesta personalizada del error
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(), // Código HTTP (404)
                HttpStatus.NOT_FOUND.getReasonPhrase(), // Nombre del estado HTTP
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Captura cualquier excepción no controlada
    // Evita que la aplicación devuelva errores sin controlar
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex, HttpServletRequest request) {
        // Construye la respuesta personalizada del error
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        // Devuelve una respuesta HTTP 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}