package com.b2bmatch.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;

@RestController
@RequestMapping("/api/catalogo/skills")
@RequiredArgsConstructor
public class SkillController {

	private static final String SELECT_COLUMNS = "id, name, status, created_at, updated_at";

	private final JdbcTemplate jdbc;

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> findAll() {
		List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + SELECT_COLUMNS + " FROM catalogo.skill WHERE status <> 'DELETED' ORDER BY name");
		return ResponseEntity.ok(rows);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
		String name = requireText(body.get("name"));
		if (name == null) {
			return ResponseEntity.badRequest().build();
		}
		try {
			Map<String, Object> created = jdbc.queryForMap(
					"INSERT INTO catalogo.skill(name) VALUES (?) RETURNING " + SELECT_COLUMNS, name);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (DuplicateKeyException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
		String name = requireText(body.get("name"));
		if (name == null) {
			return ResponseEntity.badRequest().build();
		}
		try {
			int updated = jdbc.update("UPDATE catalogo.skill SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status <> 'DELETED'", name, id);
			if (updated == 0) {
				return ResponseEntity.notFound().build();
			}
			Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.skill WHERE id = ?", id);
			return ResponseEntity.ok(row);
		} catch (DuplicateKeyException ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		int updated = jdbc.update("UPDATE catalogo.skill SET status = 'DELETED', updated_at = CURRENT_TIMESTAMP WHERE id = ? AND status <> 'DELETED'", id);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	private String requireText(Object value) {
		if (!(value instanceof String str) || str.isBlank()) {
			return null;
		}
		return str.trim();
	}
}
