package com.b2bmatch.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalogo/skills")
@RequiredArgsConstructor
public class SkillController {

	private static final String SELECT_COLUMNS = "id, name, created_at";

	private final JdbcTemplate jdbc;

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> findAll() {
		List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + SELECT_COLUMNS + " FROM catalogo.skill ORDER BY name");
		return ResponseEntity.ok(rows);
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
		String name = (String) body.get("name");
		if (name == null || name.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		try {
			Map<String, Object> created = jdbc.queryForMap(
					"INSERT INTO catalogo.skill(name) VALUES (?) RETURNING " + SELECT_COLUMNS, name);
			return ResponseEntity.status(HttpStatus.CREATED).body(created);
		} catch (Exception ex) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}
}
