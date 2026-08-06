package com.b2bmatch.usuarios.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.b2bmatch.usuarios.exception.AuthException;
import com.b2bmatch.usuarios.model.Role;
import com.b2bmatch.usuarios.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Transactional
    public Role create(Role role) {
        if (roleRepository.findByName(role.getName()).isPresent()) {
            throw new AuthException(HttpStatus.CONFLICT, "Ya existe un rol con ese nombre: " + role.getName());
        }

        LocalDateTime now = LocalDateTime.now();
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        return roleRepository.save(role);
    }
}
