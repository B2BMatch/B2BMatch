package com.b2bmatch.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
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

@RestController
@RequestMapping("/api/catalogo/categories")
@RequiredArgsConstructor
public class CategoryController {

	private static final String SELECT_COLUMNS = "id, name, description, status, created_at, updated_at";

	private final JdbcTemplate jdbc;

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> findAll() {
		List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + SELECT_COLUMNS + " FROM catalogo.category WHERE deleted_at IS NULL ORDER BY name");
		return ResponseEntity.ok(rows);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
		try {
			Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.category WHERE id = ? AND deleted_at IS NULL", id);
			return ResponseEntity.ok(row);
		} catch (EmptyResultDataAccessException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
		String name = requireText(body.get("name"));
		if (name == null) {
			return ResponseEntity.badRequest().build();
		}
		String description = optionalText(body.get("description"));
		Map<String, Object> created = jdbc.queryForMap(
				"INSERT INTO catalogo.category(name, description) VALUES (?, ?) RETURNING " + SELECT_COLUMNS,
				name, description);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
		String name = requireText(body.get("name"));
		if (name == null) {
			return ResponseEntity.badRequest().build();
		}
		String description = optionalText(body.get("description"));
		int updated = jdbc.update("UPDATE catalogo.category SET name = ?, description = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND deleted_at IS NULL", name, description, id);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.category WHERE id = ?", id);
		return ResponseEntity.ok(row);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		int updated = jdbc.update("UPDATE catalogo.category SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND deleted_at IS NULL", id);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/restore")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Map<String, Object>> restore(@PathVariable Long id) {
		// Restore solo despeja la marca de borrado. `delete` ya no toca `status`,
		// asi que el estado de negocio sigue intacto y esta operacion no debe
		// escribirlo: si lo hiciera, restaurar una categoria INACTIVE o SUSPENDED
		// la promoveria a ACTIVE. `previous_status` quedo vestigial (V5 ya
		// normalizo las filas heredadas) y se drena aqui.
		int updated = jdbc.update("UPDATE catalogo.category SET deleted_at = NULL, previous_status = NULL, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND deleted_at IS NOT NULL", id);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.category WHERE id = ?", id);
		return ResponseEntity.ok(row);
	}

	private String requireText(Object value) {
		if (!(value instanceof String str) || str.isBlank()) {
			return null;
		}
		return str.trim();
	}

	private String optionalText(Object value) {
		if (!(value instanceof String str)) {
			return null;
		}
		return str.trim();
	}
}
