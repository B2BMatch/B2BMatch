package com.b2bmatch.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter implements WebFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/users/register"
    );

    private static final Pattern USER_DELETE_PATTERN = Pattern.compile("^/api/users/(\\d+)$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Falta el token de autenticación");
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            String role = claims.get("role", String.class);

            if (isRoleCreation(request.getMethod(), path) && !"ADMIN".equals(role)) {
                return forbidden(exchange, "Se requiere rol ADMIN para esta acción");
            }

            Long targetUserId = extractUserDeleteId(request.getMethod(), path);
            if (targetUserId != null) {
                Long requesterId = claims.get("userId", Long.class);
                boolean isOwner = targetUserId.equals(requesterId);
                boolean isAdmin = "ADMIN".equals(role);
                if (!isOwner && !isAdmin) {
                    return forbidden(exchange, "Solo puedes eliminar tu propia cuenta, o ser ADMIN");
                }
            }

            return chain.filter(exchange);
        } catch (JwtException e) {
            return unauthorized(exchange, "Token inválido o expirado");
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isRoleCreation(HttpMethod method, String path) {
        return HttpMethod.POST.equals(method) && path.equals("/api/roles");
    }

    private Long extractUserDeleteId(HttpMethod method, String path) {
        if (!HttpMethod.DELETE.equals(method)) {
            return null;
        }
        Matcher matcher = USER_DELETE_PATTERN.matcher(path);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return respondWithError(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized", message);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        return respondWithError(exchange, HttpStatus.FORBIDDEN, "Forbidden", message);
    }

    private Mono<Void> respondWithError(ServerWebExchange exchange, HttpStatus status, String error, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        String body = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                status.value(), error, message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}