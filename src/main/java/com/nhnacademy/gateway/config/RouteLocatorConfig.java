package com.nhnacademy.gateway.config;

//@Configuration
//public class RouteLocatorConfig {
//
//    @Autowired
//    AuthorizationHeaderFilter authorizationHeaderFilter;
//
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthorizationHeaderFilter authorizationHeaderFilter) {
//        return builder.routes()
//                .route("bookstore", p -> p.path("/bookstore")
//                        .filters(f -> f.filter(authorizationHeaderFilter))
//                        .uri("lb://3RUNNER-BOOKSTORE"))
//                .route("coupon", p -> p.path("/coupon").and().uri("lb://3RUNNER-COUPON"))
//                .build();
//    }
//}
