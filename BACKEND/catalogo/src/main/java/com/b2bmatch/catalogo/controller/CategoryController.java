package com.b2bmatch.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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

	private static final String SELECT_COLUMNS = "id, name, description, created_at";

	private final JdbcTemplate jdbc;

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> findAll() {
		List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + SELECT_COLUMNS + " FROM catalogo.category ORDER BY name");
		return ResponseEntity.ok(rows);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
		try {
			Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.category WHERE id = ?", id);
			return ResponseEntity.ok(row);
		} catch (EmptyResultDataAccessException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
		String name = (String) body.get("name");
		if (name == null || name.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		String description = (String) body.get("description");
		Map<String, Object> created = jdbc.queryForMap(
				"INSERT INTO catalogo.category(name, description) VALUES (?, ?) RETURNING " + SELECT_COLUMNS,
				name, description);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
		String name = (String) body.get("name");
		String description = (String) body.get("description");
		int updated = jdbc.update("UPDATE catalogo.category SET name = ?, description = ? WHERE id = ?", name, description, id);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.category WHERE id = ?", id);
		return ResponseEntity.ok(row);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		int deleted = jdbc.update("DELETE FROM catalogo.category WHERE id = ?", id);
		if (deleted == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}
}
