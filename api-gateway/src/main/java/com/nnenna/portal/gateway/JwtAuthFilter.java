package com.nnenna.portal.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Validates the JWT on every request except login/register, strips any client-supplied
 * identity headers and forwards the verified identity to downstream services.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    static final String USER_ID = "X-User-Id";
    static final String USER_ROLE = "X-User-Role";
    private static final Set<String> PUBLIC_PATHS = Set.of("/auth/login", "/auth/register");

    private final SecretKey key;

    public JwtAuthFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (PUBLIC_PATHS.contains(path)) {
            ServerHttpRequest clean = exchange.getRequest().mutate()
                    .headers(h -> { h.remove(USER_ID); h.remove(USER_ROLE); })
                    .build();
            return chain.filter(exchange.mutate().request(clean).build());
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(auth.substring(7)).getPayload();
            String role = claims.get("role", String.class);
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .headers(h -> {
                        h.remove(USER_ID);
                        h.remove(USER_ROLE);
                        h.add(USER_ID, claims.getSubject());
                        h.add(USER_ROLE, role == null ? "CUSTOMER" : role);
                    })
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        } catch (JwtException | IllegalArgumentException e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
