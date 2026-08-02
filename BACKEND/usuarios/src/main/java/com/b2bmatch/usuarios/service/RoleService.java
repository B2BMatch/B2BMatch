package com.b2bmatch.usuarios.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import com.b2bmatch.usuarios.model.Role;
import com.b2bmatch.usuarios.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Role create(Role role) {
        String normalizedName = role.getName().trim().toUpperCase();
        role.setName(normalizedName);

        if (roleRepository.findByName(normalizedName).isPresent()) {
            throw new IllegalArgumentException("El rol ya existe");
        }
        role.setCreatedAt(LocalDateTime.now());
        return roleRepository.save(role);
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}