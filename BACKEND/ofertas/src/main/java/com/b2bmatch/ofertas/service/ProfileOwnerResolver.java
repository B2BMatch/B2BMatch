package com.b2bmatch.ofertas.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProfileOwnerResolver {

    private final JdbcTemplate jdbcTemplate;

    public ProfileOwnerResolver(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasActiveProfessionalProfile(Long userId) {
        return Boolean.TRUE.equals(jdbcTemplate.query(
                "SELECT EXISTS (SELECT 1 FROM perfiles.professional_profile WHERE user_id = ? AND status <> 'DELETED')",
                rs -> rs.next() && rs.getBoolean(1),
                userId));
    }

    public boolean hasActiveCompanyProfile(Long userId) {
        return Boolean.TRUE.equals(jdbcTemplate.query(
                "SELECT EXISTS (SELECT 1 FROM perfiles.company_profile WHERE user_id = ? AND status <> 'DELETED')",
                rs -> rs.next() && rs.getBoolean(1),
                userId));
    }

    public Long serviceOwnerUserId(Long serviceId) {
        return jdbcTemplate.query(
                "SELECT pp.user_id FROM catalogo.professional_service ps "
                        + "JOIN perfiles.professional_profile pp ON pp.id = ps.professional_id "
                        + "WHERE ps.id = ?",
                rs -> rs.next() ? rs.getLong(1) : null,
                serviceId);
    }

    public boolean isActiveService(Long serviceId) {
        return Boolean.TRUE.equals(jdbcTemplate.query(
                "SELECT EXISTS (SELECT 1 FROM catalogo.professional_service WHERE id = ? AND status = 'ACTIVE')",
                rs -> rs.next() && rs.getBoolean(1),
                serviceId));
    }

    public boolean hasActiveCategory(Long categoryId) {
        return Boolean.TRUE.equals(jdbcTemplate.query(
                "SELECT EXISTS (SELECT 1 FROM catalogo.category WHERE id = ? AND status <> 'DELETED')",
                rs -> rs.next() && rs.getBoolean(1),
                categoryId));
    }
}