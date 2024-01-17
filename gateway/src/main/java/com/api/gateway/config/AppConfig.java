package com.api.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, AuthFilter authFilter) {
		return builder.routes()
					   .route("auth", r -> r.path("/api/v1/auth/*")
												   .uri("http://localhost:8081"))
					   .route("test", r -> r.path("/test/**")
												   .filters(f -> f.filter(authFilter))
												   .uri("http://localhost:8086"))
					   .build();
	}
}
