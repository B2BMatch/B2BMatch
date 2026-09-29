package com.b2bmatch.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2bmatch.catalogo.config.JwtUtil;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalogo/professional-skills")
@RequiredArgsConstructor
public class ProfessionalSkillController {

	private static final String SKILL_COLUMNS = "s.id, s.name, s.status, s.created_at, s.updated_at";

	private final JdbcTemplate jdbc;
	private final JwtUtil jwtUtil;

	@GetMapping("/{professionalId}")
	public ResponseEntity<List<Map<String, Object>>> findByProfessional(@PathVariable Long professionalId) {
		List<Map<String, Object>> rows = jdbc.queryForList(
				"SELECT " + SKILL_COLUMNS
						+ " FROM catalogo.professional_skill ps JOIN catalogo.skill s ON s.id = ps.skill_id "
						+ "WHERE ps.professional_id = ? AND s.deleted_at IS NULL "
						+ "AND EXISTS (SELECT 1 FROM perfiles.professional_profile pp "
						+ "WHERE pp.id = ps.professional_id AND pp.deleted_at IS NULL) ORDER BY s.name",
				professionalId);
		return ResponseEntity.ok(rows);
	}

	@PostMapping("/{professionalId}/skills/{skillId}")
	public ResponseEntity<Map<String, Object>> assign(@PathVariable Long professionalId, @PathVariable Long skillId,
			@RequestHeader("Authorization") String authHeader) {
		Claims claims = jwtUtil.parseToken(authHeader.substring(7));
		String role = claims.get("role", String.class);
		Long userId = claims.get("userId", Long.class);

		Long ownerUserId = professionalOwnerUserId(professionalId);
		if (ownerUserId == null) {
			return ResponseEntity.notFound().build();
		}
		if (!"ADMIN".equals(role) && !ownerUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		Boolean skillExists = jdbc.query(
				"SELECT EXISTS(SELECT 1 FROM catalogo.skill WHERE id = ? AND deleted_at IS NULL)",
				(org.springframework.jdbc.core.ResultSetExtractor<Boolean>) rs -> rs.next() && rs.getBoolean(1), skillId);
		if (!Boolean.TRUE.equals(skillExists)) {
			return ResponseEntity.notFound().build();
		}

		try {
			jdbc.update(
					"INSERT INTO catalogo.professional_skill(professional_id, skill_id) VALUES (?, ?)",
					professionalId, skillId);
		} catch (DuplicateKeyException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}

		try {
			Map<String, Object> row = jdbc.queryForMap(
					"SELECT " + SKILL_COLUMNS
							+ " FROM catalogo.professional_skill ps JOIN catalogo.skill s ON s.id = ps.skill_id "
							+ "WHERE ps.professional_id = ? AND ps.skill_id = ?",
					professionalId, skillId);
			return ResponseEntity.status(HttpStatus.CREATED).body(row);
		} catch (EmptyResultDataAccessException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{professionalId}/skills/{skillId}")
	public ResponseEntity<Void> unassign(@PathVariable Long professionalId, @PathVariable Long skillId,
			@RequestHeader("Authorization") String authHeader) {
		Claims claims = jwtUtil.parseToken(authHeader.substring(7));
		String role = claims.get("role", String.class);
		Long userId = claims.get("userId", Long.class);

		Long ownerUserId = professionalOwnerUserId(professionalId);
		if (ownerUserId == null) {
			return ResponseEntity.notFound().build();
		}
		if (!"ADMIN".equals(role) && !ownerUserId.equals(userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		int updated = jdbc.update(
				"DELETE FROM catalogo.professional_skill WHERE professional_id = ? AND skill_id = ?",
				professionalId, skillId);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	private Long professionalOwnerUserId(Long professionalId) {
		return jdbc.query(
				"SELECT user_id FROM perfiles.professional_profile WHERE id = ? AND deleted_at IS NULL",
				rs -> rs.next() ? rs.getLong(1) : null, professionalId);
	}
}