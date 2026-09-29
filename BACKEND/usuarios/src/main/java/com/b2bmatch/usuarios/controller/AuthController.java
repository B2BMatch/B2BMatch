package com.b2bmatch.usuarios.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2bmatch.usuarios.dto.LoginRequestDto;
import com.b2bmatch.usuarios.dto.LoginResponseDto;
import com.b2bmatch.usuarios.service.AppUserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request,
            HttpServletRequest http) {
        return ResponseEntity.ok(appUserService.login(request, origenDe(http)));
    }

    /**
     * La IP que se usa para el limite de intentos. Sin esto el servicio, que solo
     * ve al gateway, trataria a todos los clientes como a uno solo y un atacante
     * torpe bloquearia el servicio entero en lugar de a una cuenta.
     *
     * Se toma la primera entrada de X-Forwarded-For, que es la del cliente
     * original. Eso supone que quien llega al gateway no puede falsear la
     * cabecera: si pudiera, se saltaria el limite por IP inventandose una nueva en
     * cada peticion. En produccion el gateway es la unica entrada, y por lo que
     * se ve en este repositorio no hay nada delante. Queda anotado en el
     * itinerario porque es una dependencia de despliegue, no de codigo.
     */
    private String origenDe(HttpServletRequest http) {
        String reenviada = http.getHeader("X-Forwarded-For");
        if (reenviada != null && !reenviada.isBlank()) {
            String primera = reenviada.split(",")[0].trim();
            if (!primera.isEmpty()) {
                return primera;
            }
        }
        String directa = http.getRemoteAddr();
        return directa != null ? directa : "desconocida";
    }
}
