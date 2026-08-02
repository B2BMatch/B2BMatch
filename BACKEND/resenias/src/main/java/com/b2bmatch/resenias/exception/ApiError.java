package com.b2bmatch.resenias.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter // Genera automáticamente los métodos getter
@AllArgsConstructor // Genera un constructor con todos los atributos
// Representa el formato que tendrán todos los errores de la API
public class ApiError {

    // Fecha y hora en que ocurrió el error
    private LocalDateTime timestamp;
    // Código HTTP (400, 401, 404, etc.)
    private int status;
    // Nombre del error HTTP
    private String error;
    // Mensaje descriptivo del error
    private String message;
    // Ruta donde ocurrió el error
    private String path;
}