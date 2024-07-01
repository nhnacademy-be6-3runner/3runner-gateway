package com.nhnacademy.gateway.filter;

import java.net.URI;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.nhnacademy.gateway.jwt.JWTUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @Component
// @Slf4j
// public class LoginCheckFilter extends AbstractGatewayFilterFactory<LoginCheckFilter.Config> {
//
// 	@Override
// 	public GatewayFilter apply(LoginCheckFilter.Config config) {
// 		return (exchange, chain) -> {
// 			ServerHttpRequest request = exchange.getRequest();
//
// 			if (request.getURI().getPath().equals("/auth/login")) {
// 				URI redirectUri = URI.create("http://localhost:3001/login");
// 				if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
//
// 					ServerHttpRequest modifiedRequest = exchange
// 						.getRequest()
// 						.mutate()
// 						.uri(redirectUri)
// 						.build();
//
// 					ServerWebExchange modifiedExchange = exchange
// 						.mutate()
// 						.request(modifiedRequest)
// 						.build();
//
// 					return chain.filter(modifiedExchange);
// 				}
// 			}
// 			return chain.filter(exchange);
// 		};
// 	}
//
// 	@RequiredArgsConstructor
// 	public static class Config {
// 		private final JWTUtil jwtUtils;
// 	}
// }
