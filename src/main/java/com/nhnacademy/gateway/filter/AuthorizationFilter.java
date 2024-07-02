package com.nhnacademy.gateway.filter;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.TokenDetails;
import com.nhnacademy.gateway.jwt.JWTUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Front 서버로부터 넘어온 JWT 를 검증한다.
 * 토큰 내의 멤버 아이디 정보를 헤더에 추가해주는 필터.
 *
 * @author 오연수
 */
@Component
@Slf4j
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {
	private final String TOKEN_DETAILS = "token_details";

	private final RedisTemplate<String, Objects> redisTemplate;
	private JWTUtil jwtUtil;
	private ObjectMapper objectMapper;

	@Autowired
	public AuthorizationFilter(JWTUtil jwtUtil, @Qualifier("redisTemplate") RedisTemplate redisTemplate, ObjectMapper objectMapper) {
		super(Config.class);
		this.jwtUtil = jwtUtil;
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	@RequiredArgsConstructor
	public static class Config {
		private final JWTUtil jwtUtils;
	}

	@Override
	public GatewayFilter apply(AuthorizationFilter.Config config) {
		return ((exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			// /bookstore/login 은 검증 제외
			if (request.getURI().getPath().startsWith("/bookstore/login")) {
				log.warn("bookstore login api 요청");
				return chain.filter(exchange);
			}
			if(request.getURI().getPath().startsWith("/bookstore/members/oauth")&& request.getMethod()== HttpMethod.POST){
				log.warn("bookstore login/oauth api 요청");
				return chain.filter(exchange);
			}
			if(request.getURI().getPath().startsWith("/bookstore/members")&& request.getMethod()== HttpMethod.POST){
				return chain.filter(exchange);
			}
			if (!request.getHeaders().containsKey("Authorization")) {
				log.error("Authorization header not present");
				return onError(exchange, "Authorization 헤더가 존재하지 않는다", HttpStatus.UNAUTHORIZED);
			}

			String authorization = request.getHeaders().get(HttpHeaders.AUTHORIZATION).getFirst();
			String token = authorization.split(" ")[1];

			if (jwtUtil.getCategory(token).equals("refresh")) {
				log.warn("REFRESH token 으로 요청 들어왔습니다.");
				URI redirectUri = URI.create("http://localhost:8080/auth/reissue");
				ServerHttpRequest modifiedRequest = exchange
					.getRequest()
					.mutate()
					.uri(redirectUri)
					.build();

				ServerWebExchange modifiedExchange = exchange
					.mutate()
					.request(modifiedRequest)
					.build();

				return chain.filter(modifiedExchange);
			}

			if (!isJwtValid(token)) {
				log.error("JWT is not valid");
				return onError(exchange, "JWT가 유효하지 않다", HttpStatus.UNAUTHORIZED);
			}

			// 넘어가는 요청에 대해 Member-Id Header 추가
			String uuid = jwtUtil.getUuid(token);
			String data = (String)redisTemplate.opsForHash().get(TOKEN_DETAILS, uuid);
			TokenDetails tokenDetails = null;
			try {
				tokenDetails = objectMapper.readValue(data, TokenDetails.class);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
			if (tokenDetails == null) {
				System.out.println("TokenDetails not found in Redis for UUID: " + uuid);
			} else {
				System.out.println("TokenDetails retrieved: " + tokenDetails.toString());
			}
			HttpHeaders headers = new HttpHeaders();
			headers.addAll(request.getHeaders());

			headers.remove(HttpHeaders.AUTHORIZATION);
			headers.add("Member-Id", String.valueOf(tokenDetails.getMemberId()));

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

	private Mono<Void> handleRedirect(ServerWebExchange exchange) {
		// TODO main 페이지 구현 시 리다이렉트 url 변경
		String redirectUrl = "http://login";

		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.FOUND);
		response.getHeaders().setLocation(URI.create(redirectUrl));

		return response.setComplete();
	}


	private boolean isJwtValid(String token) {
		String uuid = jwtUtil.getUuid(token);

		if (Objects.isNull(uuid) || uuid.isEmpty()) {
			return false;
		}

		if (jwtUtil.isExpired(token)) {
			return false;
		}

		return true;
	}

}
