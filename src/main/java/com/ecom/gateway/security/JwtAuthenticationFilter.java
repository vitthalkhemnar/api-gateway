package com.ecom.gateway.security;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	private static final String SECRET_STRING = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

	private static final List<String> PUBLIC_PATHS = List.of(
			"/**/auth/**",
			"/auth/**"
	);

	private static final List<String> PUBLIC_GET_PATHS = List.of(
			"/**/product/**",
			"/**/variant/**",
			"/product/**",
			"/variant/**"
	);

	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	public int getOrder() {
		return -1;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		HttpMethod method = request.getMethod();
		String path = request.getURI().getPath();

		// 1. Allow preflight OPTIONS requests for CORS
		if (HttpMethod.OPTIONS.equals(method)) {
			return chain.filter(exchange);
		}

		// 2. Allow public auth paths (login, register)
		boolean isPublicAuth = PUBLIC_PATHS.stream()
				.anyMatch(pattern -> pathMatcher.match(pattern, path));
		if (isPublicAuth) {
			return chain.filter(exchange);
		}

		// 3. Allow public GET endpoints (viewing products, variants)
		if (HttpMethod.GET.equals(method)) {
			boolean isPublicGet = PUBLIC_GET_PATHS.stream()
					.anyMatch(pattern -> pathMatcher.match(pattern, path));
			if (isPublicGet) {
				return chain.filter(exchange);
			}
		}

		// 4. Validate Authorization Header for all other requests
		String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return onError(exchange, "Missing or Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
		}

		String token = authHeader.substring(7).trim();

		try {
			Claims claims = Jwts.parser()
					.verifyWith(Keys.hmacShaKeyFor(SECRET_STRING.getBytes()))
					.build()
					.parseSignedClaims(token)
					.getPayload();

			String username = claims.getSubject();
			@SuppressWarnings("unchecked")
			List<String> roles = claims.get("role", List.class);

			// Forward original Authorization header and extracted claims downstream
			ServerHttpRequest.Builder requestBuilder = request.mutate();
			if (username != null) {
				requestBuilder.header("X-Auth-Username", username);
			}
			if (roles != null && !roles.isEmpty()) {
				requestBuilder.header("X-Auth-Roles", String.join(",", roles));
			}

			return chain.filter(exchange.mutate().request(requestBuilder.build()).build());

		} catch (Exception e) {
			log.error("JWT validation error for [{} {}]: {}", method, path, e.getMessage());
			return onError(exchange, "Invalid or Expired JWT Token", HttpStatus.UNAUTHORIZED);
		}
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		return response.setComplete();
	}
}
