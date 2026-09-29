package com.b2bmatch.usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.b2bmatch.usuarios.dto.AppUserAdminRegisterRequestDto;
import com.b2bmatch.usuarios.dto.AppUserRegisterRequestDto;
import com.b2bmatch.usuarios.dto.AppUserResponseDto;
import com.b2bmatch.usuarios.dto.AppUserStatusRequestDto;
import com.b2bmatch.usuarios.service.AppUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUserResponseDto> getAll(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return appUserService.findAll(includeDeleted);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal")
    public ResponseEntity<AppUserResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.findById(id));
    }

    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUserResponseDto> getByRole(@PathVariable String roleName) {
        return appUserService.findByRole(roleName);
    }

    @PostMapping("/register")
    public ResponseEntity<AppUserResponseDto> register(@Valid @RequestBody AppUserRegisterRequestDto request) {
        AppUserResponseDto saved = appUserService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/admin-register")
    public ResponseEntity<AppUserResponseDto> registerAdmin(
            @Valid @RequestBody AppUserAdminRegisterRequestDto request,
            @RequestHeader(value = "X-Admin-Bootstrap-Key", required = false) String bootstrapKey) {
        AppUserResponseDto saved = appUserService.registerAdmin(request, bootstrapKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        appUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUserResponseDto> updateStatus(@PathVariable Long id,
            @Valid @RequestBody AppUserStatusRequestDto request) {
        return ResponseEntity.ok(appUserService.updateStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppUserResponseDto> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(appUserService.reactivate(id));
    }
}
