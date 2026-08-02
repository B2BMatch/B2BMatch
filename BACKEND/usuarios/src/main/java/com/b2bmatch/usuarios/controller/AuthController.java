package com.b2bmatch.usuarios.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2bmatch.usuarios.dto.AppUserLoginRequestDto;
import com.b2bmatch.usuarios.dto.AuthResponseDto;
import com.b2bmatch.usuarios.service.AppUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AppUserLoginRequestDto request) {
        AuthResponseDto user = appUserService.login(request);
        return ResponseEntity.ok(user);
    }
}
