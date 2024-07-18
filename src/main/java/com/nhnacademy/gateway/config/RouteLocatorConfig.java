package com.nhnacademy.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration
// public class RouteLocatorConfig {
//
//     @Bean
//     public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//         return builder.routes()
//                 .route("bookstore", p -> p.path("/**").and().uri("lb://3RUNNER-BOOKSTORE"))
// //                .route("coupon", p -> p.path("/coupon").and().uri("lb://3RUNNER-COUPON"))
// //                .route("auth", p -> p.path("/auth").and().uri("lb://3RUNNER-AUTH"))
//                 .build();
//     }
//     //
//     //
// }
