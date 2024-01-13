package com.api.gateway;

import com.api.gateway.dto.UserDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<Object> {

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
		this.webClient = webClientBuilder.baseUrl("http://auth").build();
	}

	@Override
	public GatewayFilter apply(Object config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();
			// check path public
			String path = request.getPath().toString();
			if (PUBLIC_PATHS.contains(path)) {
				System.out.println("Public Path: " + path);
				return chain.filter(exchange);
			}
			//
			String authToken = request.getHeaders().getFirst("Authorization");
			if (StringUtils.isEmpty(authToken)) {
				return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is missing"));
			}
			return webClient.get()
						   .uri("/api/v1/auth/check-token")
						   .header("Authorization", authToken)
						   .retrieve()
						   .onStatus(status -> status.is4xxClientError(), response -> {
							   System.out.println("Status code: 4xxxx");
							   return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token validation failed"));

						   })
						   .onStatus(status -> status.is5xxServerError(), response -> {
							   // Xử lý lỗi 5xx (Internal Server Error) nếu cần
							   System.out.println("Status code: 5xxxx");
							   return Mono.error(new ResponseStatusException(response.statusCode(), "Internal Server Error"));
						   })
						   .bodyToMono(UserDTO.class)	// get response and convert to UserDTO
						   .flatMap(userDTO -> {	// handle data (UserDTO)
							   // Xử lý thông tin người dùng nếu cần
							   System.out.println("User Info: " + userDTO.toString());
							   // Tiếp tục chuyển tiếp yêu cầu
							   return chain.filter(exchange);
						   })
						   .then(chain.filter(exchange));
		};
	}
}
