package com.b2bmatch.usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.b2bmatch.usuarios.dto.AppUserRegisterRequestDto;
import com.b2bmatch.usuarios.dto.AppUserResponseDto;
import com.b2bmatch.usuarios.dto.AuthResponseDto;
import com.b2bmatch.usuarios.service.AppUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    @GetMapping
    public List<AppUserResponseDto> getAll(Authentication authentication) {
        requireAdmin(authentication);
        return appUserService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUserResponseDto> getById(@PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication) && !id.equals(currentUserId(authentication))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver la información de otro usuario");
        }
        return ResponseEntity.ok(appUserService.findById(id));
    }

    @GetMapping("/role/{roleName}")
    public List<AppUserResponseDto> getByRole(@PathVariable String roleName, Authentication authentication) {
        requireAdmin(authentication);
        return appUserService.findByRole(roleName);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody AppUserRegisterRequestDto request) {
        AuthResponseDto saved = appUserService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        requireAdmin(authentication);
        appUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<AppUserResponseDto> reactivate(@PathVariable Long id, Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(appUserService.reactivate(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AppUserResponseDto> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body, Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(appUserService.updateStatus(id, body.get("status")));
    }

    private void requireAdmin(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede realizar esta acción");
        }
    }
}
