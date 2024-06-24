package com.nhnacademy.gateway.filter;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.nhnacademy.gateway.jwt.JWTUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Front 서버로부터 넘어온 JWT를 검증한다.
 * 토큰 내의 멤버 아이디 정보를 헤더에 추가해주는 필터.
 *
 * @author 오연수
 */
@Component
@Slf4j
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {
	@Autowired
	private JWTUtil jwtUtil;

	/**
	 * Instantiates a new Authorization filter.
	 */
	public AuthorizationFilter() {
		super(Config.class);
	}

	/**
	 * The type Config.
	 */
	@RequiredArgsConstructor
	public static class Config {
		private final JWTUtil jwtUtils;
	}

	@Override
	public GatewayFilter apply(AuthorizationFilter.Config config) {
		return ((exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			if (request.getURI().getPath().startsWith("/bookstore/login")) {
				return chain.filter(exchange);
			}

			if (!request.getHeaders().containsKey("Authorization")) {
				log.error("Authorization header not present");
				return onError(exchange, "Authorization 헤더가 존재하지 않는다", HttpStatus.UNAUTHORIZED);
			}

			String authorization = request.getHeaders().get(HttpHeaders.AUTHORIZATION).getFirst();
			String token = authorization.split(" ")[1];

			if (!isJwtValid(token)) {
				log.error("JWT is not valid");
				return onError(exchange, "JWT가 유효하지 않다", HttpStatus.UNAUTHORIZED);
			}

			// 넘어가는 요청에 대해 Member-Id Header 추가
			HttpHeaders headers = new HttpHeaders();
			headers.addAll(request.getHeaders());

			headers.remove(HttpHeaders.AUTHORIZATION);
			headers.add("Member-Id", String.valueOf(jwtUtil.getMemberId(token)));

			ServerHttpRequest modifiedRequest = request.mutate()
				.headers(httpHeaders -> httpHeaders.addAll(headers))
				.build();
			return chain.filter(exchange.mutate().request(modifiedRequest).build());
		});
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);

		return response.setComplete();
	}

	private boolean isJwtValid(String token) {
		String username = jwtUtil.getUsername(token);
		String auth = jwtUtil.getAuth(token);

		if (Objects.isNull(username) || username.isEmpty()) {
			return false;
		}
		if (Objects.isNull(auth) || auth.isEmpty()) {
			return false;
		}
		if (jwtUtil.isExpired(token)) {
			return false;
		}

		return true;
	}

}
