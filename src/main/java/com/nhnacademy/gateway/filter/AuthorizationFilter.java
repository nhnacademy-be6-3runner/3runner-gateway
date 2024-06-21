package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {
    @Autowired
    private JWTUtil jwtUtil;

    public AuthorizationFilter() {
        super(Config.class);
    }

    @RequiredArgsConstructor
    public static class Config {
        private final JWTUtil jwtUtils;
    }

    @Override
    public GatewayFilter apply(AuthorizationFilter.Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (!request.getHeaders().containsKey("Authorization")) {
                return onError(exchange, "Authorization 헤더가 존재하지 않는다", HttpStatus.UNAUTHORIZED);
            }

            String authorization = request.getHeaders().get(HttpHeaders.AUTHORIZATION).getFirst();
            String token = authorization.split(" ")[1];

            if (!isJwtValid(token)) {
                return onError(exchange, "JWT가 유효하지 않다", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
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
