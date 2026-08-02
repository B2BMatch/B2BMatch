package com.b2bmatch.catalogo.controller;

import java.util.List;
import java.util.Map;

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
@RequestMapping("/api/catalogo/professional-services")
@RequiredArgsConstructor
public class ProfessionalServiceController {

	private static final String SELECT_COLUMNS = "id, professional_id, category_id, title, description, price, status, created_at";

	private final JdbcTemplate jdbc;

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> findAll() {
		List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + SELECT_COLUMNS + " FROM catalogo.professional_service ORDER BY created_at DESC");
		return ResponseEntity.ok(rows);
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
		Object professionalRaw = body.get("professional_id");
		Object categoryRaw = body.get("category_id");
		String title = (String) body.get("title");
		if (professionalRaw == null || categoryRaw == null || title == null || title.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		Long professionalId = ((Number) professionalRaw).longValue();
		Long categoryId = ((Number) categoryRaw).longValue();
		String description = (String) body.get("description");
		Number price = (Number) body.get("price");
		Map<String, Object> created = jdbc.queryForMap(
				"INSERT INTO catalogo.professional_service(professional_id, category_id, title, description, price) VALUES (?, ?, ?, ?, ?) RETURNING " + SELECT_COLUMNS,
				professionalId, categoryId, title, description, price);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}
}
