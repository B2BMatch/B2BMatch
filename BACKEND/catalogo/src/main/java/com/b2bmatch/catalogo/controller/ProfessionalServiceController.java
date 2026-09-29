package com.b2bmatch.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b2bmatch.catalogo.config.JwtUtil;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/catalogo/professional-services")
@RequiredArgsConstructor
public class ProfessionalServiceController {

	private static final String SELECT_COLUMNS = "id, professional_id, category_id, title, description, price, status, created_at";

	// `status = 'ACTIVE'` ya no excluye los borrados: el borrado vive en
	// deleted_at, asi que el filtro tiene que mirar las dos cosas.
	private static final String ACTIVE_FILTER = " status = 'ACTIVE' AND deleted_at IS NULL ";

	private final JdbcTemplate jdbc;
	private final JwtUtil jwtUtil;

	@GetMapping
	public ResponseEntity<List<Map<String, Object>>> findAll() {
		List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + SELECT_COLUMNS + " FROM catalogo.professional_service WHERE" + ACTIVE_FILTER + "ORDER BY created_at DESC");
		return ResponseEntity.ok(rows);
	}

	@GetMapping("/mine")
	public ResponseEntity<List<Map<String, Object>>> findMine(@RequestHeader("Authorization") String authHeader) {
		Claims claims = jwtUtil.parseToken(authHeader.substring(7));
		String role = claims.get("role", String.class);
		Long userId = claims.get("userId", Long.class);

		if (!"PROFESSIONAL".equals(role) && !"ADMIN".equals(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		Long professionalId = jdbc.query(
				"SELECT id FROM perfiles.professional_profile WHERE user_id = ? AND deleted_at IS NULL",
				rs -> rs.next() ? rs.getLong(1) : null, userId);
		if (professionalId == null) {
			return ResponseEntity.ok(List.of());
		}

		List<Map<String, Object>> rows = jdbc.queryForList(
				"SELECT " + SELECT_COLUMNS + " FROM catalogo.professional_service WHERE professional_id = ? AND deleted_at IS NULL ORDER BY created_at DESC",
				professionalId);
		return ResponseEntity.ok(rows);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
		try {
			Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.professional_service WHERE id = ? AND deleted_at IS NULL", id);
			return ResponseEntity.ok(row);
		} catch (EmptyResultDataAccessException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping
	public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body,
			@RequestHeader("Authorization") String authHeader) {
		Claims claims = jwtUtil.parseToken(authHeader.substring(7));
		String role = claims.get("role", String.class);
		Long userId = claims.get("userId", Long.class);

		if (!"PROFESSIONAL".equals(role) && !"ADMIN".equals(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		Object professionalRaw = body.get("professional_id");
		Object categoryRaw = body.get("category_id");
		String title = (String) body.get("title");
		String description = (String) body.get("description");
		Object priceRaw = body.get("price");
		if (professionalRaw == null || categoryRaw == null || title == null || title.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		Long professionalId = ((Number) professionalRaw).longValue();
		Long categoryId = ((Number) categoryRaw).longValue();
		Number price = (Number) priceRaw;

		if (!"ADMIN".equals(role)) {
			Long profUserId = jdbc.query(
					"SELECT user_id FROM perfiles.professional_profile WHERE id = ? AND deleted_at IS NULL",
					rs -> rs.next() ? rs.getLong(1) : null, professionalId);
			if (profUserId == null) {
				return ResponseEntity.badRequest().build();
			}
			if (!profUserId.equals(userId)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		}

		Map<String, Object> created = jdbc.queryForMap(
				"INSERT INTO catalogo.professional_service(professional_id, category_id, title, description, price) VALUES (?, ?, ?, ?, ?) RETURNING " + SELECT_COLUMNS,
				professionalId, categoryId, title, description, price);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body,
			@RequestHeader("Authorization") String authHeader) {
		Claims claims = jwtUtil.parseToken(authHeader.substring(7));
		String role = claims.get("role", String.class);
		Long userId = claims.get("userId", Long.class);

		if (!"ADMIN".equals(role)) {
			Long ownerId = queryOwnerUserId(id);
			if (ownerId == null) {
				return ResponseEntity.notFound().build();
			}
			if (!ownerId.equals(userId)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		}

		Object categoryRaw = body.get("category_id");
		String title = (String) body.get("title");
		String description = (String) body.get("description");
		Object priceRaw = body.get("price");
		if (categoryRaw == null || title == null || title.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		Long categoryId = ((Number) categoryRaw).longValue();
		Number price = (Number) priceRaw;
		int updated = jdbc.update("UPDATE catalogo.professional_service SET category_id = ?, title = ?, description = ?, price = ? WHERE id = ?",
				categoryId, title, description, price, id);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.professional_service WHERE id = ?", id);
		return ResponseEntity.ok(row);
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body,
			@RequestHeader("Authorization") String authHeader) {
		Claims claims = jwtUtil.parseToken(authHeader.substring(7));
		String role = claims.get("role", String.class);
		Long userId = claims.get("userId", Long.class);

		if (!"ADMIN".equals(role)) {
			Long ownerId = queryOwnerUserId(id);
			if (ownerId == null) {
				return ResponseEntity.notFound().build();
			}
			if (!ownerId.equals(userId)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		}

		String newStatus = body.get("status") instanceof String s ? s.toUpperCase() : null;
		if (!List.of("ACTIVE", "INACTIVE", "SUSPENDED").contains(newStatus)) {
			return ResponseEntity.badRequest().build();
		}
		if ("SUSPENDED".equals(newStatus) && !"ADMIN".equals(role)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		int updated = jdbc.update("UPDATE catalogo.professional_service SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND deleted_at IS NULL",
				newStatus, id);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		Map<String, Object> row = jdbc.queryForMap("SELECT " + SELECT_COLUMNS + " FROM catalogo.professional_service WHERE id = ?", id);
		return ResponseEntity.ok(row);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id,
			@RequestHeader("Authorization") String authHeader) {
		Claims claims = jwtUtil.parseToken(authHeader.substring(7));
		String role = claims.get("role", String.class);
		Long userId = claims.get("userId", Long.class);

		if (!"ADMIN".equals(role)) {
			Long ownerId = queryOwnerUserId(id);
			if (ownerId == null) {
				return ResponseEntity.notFound().build();
			}
			if (!ownerId.equals(userId)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		}

		int updated = jdbc.update("UPDATE catalogo.professional_service SET deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND deleted_at IS NULL", id);
		if (updated == 0) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	private Long queryOwnerUserId(Long serviceId) {
		return jdbc.query(
				"SELECT pp.user_id FROM catalogo.professional_service s "
						+ "JOIN perfiles.professional_profile pp ON pp.id = s.professional_id WHERE s.id = ?",
				rs -> rs.next() ? rs.getLong(1) : null, serviceId);
	}
}