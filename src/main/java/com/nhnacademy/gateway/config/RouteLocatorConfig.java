package com.nhnacademy.gateway.config;

import com.nhnacademy.gateway.filter.AuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//public class RouteLocatorConfig {
//
//    @Autowired
//    AuthorizationFilter authorizationFilter;
//
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthorizationFilter authorizationFilter) {
//        return builder.routes()
//                .route("bookstore", p -> p.path("/bookstore")
//                        .filters(f -> f.filter())
//                        .uri("lb://3RUNNER-BOOKSTORE"))
//                .route("coupon", p -> p.path("/coupon").and().uri("lb://3RUNNER-COUPON"))
//                .build();
//    }
//}
