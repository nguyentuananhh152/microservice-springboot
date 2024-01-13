package com.tuananh.security.config;

import com.tuananh.security.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

	private final TokenRepository tokenRepository;

	@Override
	public void logout(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) {
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.equals("")) {
			return;
		}
		jwt = authHeader.substring(7);
		var storedToken = tokenRepository.findByToken(jwt)
								  .orElse(null);
		if (storedToken != null) {
			storedToken.setExpired(true);
			storedToken.setRevoked(true);
			tokenRepository.save(storedToken);
			SecurityContextHolder.clearContext();
		}
	}

	public boolean logoutCustom(HttpServletRequest request,
								HttpServletResponse response,
								Authentication authentication
	) {
		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		if (authHeader == null || !authHeader.startsWith("Bearer ") || authHeader.equals("")) {
			System.out.println("Logout service - logout - Token is null/empty/unauth");
			return false;
		}
		jwt = authHeader.substring(7);
		var storedToken = tokenRepository.findByToken(jwt).orElse(null);
		if (storedToken == null) {
			System.out.println("Logout service - logout - Token not found");
			return false;
		}
		if (storedToken.isExpired() || storedToken.isRevoked()) {
			System.out.println("Logout service - logout - Token is expired or Token is revoked");
			return false;
		}

		storedToken.setExpired(true);
		storedToken.setRevoked(true);
		tokenRepository.save(storedToken);
		SecurityContextHolder.clearContext();
		return true;
	}
}
