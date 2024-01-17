package com.api.gateway.config;

import com.api.gateway.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class AuthFilter implements GatewayFilter{
	private final WebClient webClient;
	private final List<String> PUBLIC_PATHS = Arrays.asList(
			"/api/v1/auth/register",
			"/api/v1/auth/admin/register",
			"/api/v1/auth/manager/register",
			"/api/v1/auth/login",
			"/api/v1/auth/refresh-token",
			"/api/v1/auth/logout"
	);


	public AuthFilter(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.baseUrl("http://127.0.0.1:8081").build();
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		// check path public
		String path = request.getPath().toString();
		if (PUBLIC_PATHS.contains(path)) {
			System.out.println("Public Path: " + path);
			return chain.filter(exchange);
		}
		//
		String authToken = request.getHeaders().getFirst("Authorization");
		return webClient.post()
					   .uri("/api/v1/auth/check-token")
					   .header("Authorization", authToken)
					   .retrieve()
					   .onStatus(status -> status.is4xxClientError(), response -> {return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token validation failed aaa"));})
					   .bodyToMono(UserDTO.class)	// get response and convert to UserDTO
					   .then(chain.filter(exchange));
	};
}
