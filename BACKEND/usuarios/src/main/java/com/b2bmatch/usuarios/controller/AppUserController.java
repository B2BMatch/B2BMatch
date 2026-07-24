package com.b2bmatch.usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.b2bmatch.usuarios.dto.AppUserRegisterRequestDto;
import com.b2bmatch.usuarios.dto.AppUserResponseDto;
import com.b2bmatch.usuarios.service.AppUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping
    public List<AppUserResponseDto> getAll() {
        return appUserService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.findById(id));
    }

    @GetMapping("/role/{roleName}")
    public List<AppUserResponseDto> getByRole(@PathVariable String roleName) {
        return appUserService.findByRole(roleName);
    }

    @PostMapping("/register")
    public ResponseEntity<AppUserResponseDto> register(@Valid @RequestBody AppUserRegisterRequestDto request) {
        AppUserResponseDto saved = appUserService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<AppUserResponseDto> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.reactivate(id));
    }
}