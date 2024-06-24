package com.nhnacademy.gateway.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import com.nhnacademy.gateway.jwt.JWTUtil;

import reactor.core.publisher.Mono;

/**
 * Authorization filter 테스트 입니다.
 *
 * @author 오연수
 */
@ExtendWith(MockitoExtension.class)
public class AuthorizationFilterTest {
	@Mock
	private JWTUtil jwtUtil;

	@Mock
	private GatewayFilterChain chain;

	@InjectMocks
	private AuthorizationFilter filter;

	@BeforeEach
	public void setup() {
		filter = new AuthorizationFilter(jwtUtil);
	}

	@DisplayName("Authorization Header가 존재하지 않는 경우 테스트")
	@Test
	public void testFilter_NoAuthorizationHeader() {
		// given
		ServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/test").build()
		);

		// when
		Mono<Void> result = filter.apply(new AuthorizationFilter.Config(jwtUtil)).filter(exchange, chain);

		// then
		ServerHttpResponse response = exchange.getResponse();
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@DisplayName("Jwt가 유효하지 않은 경우 테스트")
	@Test
	public void testFilter_InvalidJwt() {
		// given
		ServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/test")
				.header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
				.build()
		);

		when(jwtUtil.getUsername(anyString())).thenReturn(null);

		// when
		Mono<Void> result = filter.apply(new AuthorizationFilter.Config(jwtUtil)).filter(exchange, chain);

		// then
		ServerHttpResponse response = exchange.getResponse();
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

	@DisplayName("Jwt가 유효한 경우 테스트")
	@Test
	public void testFilter_ValidJwt() {
		// given
		String validToken = "valid_token";
		Long memberId = 12345L;

		ServerWebExchange exchange = MockServerWebExchange.from(
			MockServerHttpRequest.get("/api/test")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
				.build()
		);

		when(jwtUtil.getUsername(validToken)).thenReturn("username");
		when(jwtUtil.getAuth(validToken)).thenReturn(List.of("ROLE_USER"));
		when(jwtUtil.isExpired(validToken)).thenReturn(false);
		when(jwtUtil.getMemberId(validToken)).thenReturn(memberId);

		when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

		// when
		Mono<Void> result = filter.apply(new AuthorizationFilter.Config(jwtUtil)).filter(exchange, chain);

		// then
		ServerHttpRequest modifiedRequest = exchange.getRequest();
		assertEquals(String.valueOf(memberId), modifiedRequest.getHeaders().getFirst("Member-Id"));
		verify(chain, times(1)).filter(any(ServerWebExchange.class));
	}
}
