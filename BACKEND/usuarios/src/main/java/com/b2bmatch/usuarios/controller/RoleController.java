package com.b2bmatch.usuarios.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2bmatch.usuarios.model.Role;
import com.b2bmatch.usuarios.service.RoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public List<Role> getAll(){
        return roleService.findAll();
    }

    @PostMapping
    public ResponseEntity<Role> create(@Valid @RequestBody Role role){
        Role saved = roleService.create(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

}

