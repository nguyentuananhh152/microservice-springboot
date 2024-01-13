package com.tuananh.security.auth;

import com.tuananh.security.config.LoginResponse;
import com.tuananh.security.token.Token;
import com.tuananh.security.token.TokenRepository;
import com.tuananh.security.token.TokenType;
import com.tuananh.security.user.Map.Mapping;
import com.tuananh.security.user.Role;
import com.tuananh.security.user.User;
import com.tuananh.security.user.UserRepository;
import com.tuananh.security.config.JwtService;
import com.tuananh.security.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuananh.security.user.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@CrossOrigin
public class AuthenticationService {
	private final UserRepository repository;
	private final TokenRepository tokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final UserService userService;
	private final JdbcTemplate jdbcTemplate;

	public AuthenticationResponse register(RegisterRequest request, Role role) {
		var user = User.builder()
						   .firstname(request.getFirstname())
						   .lastname(request.getLastname())
						   .email(request.getEmail())
						   .password(passwordEncoder.encode(request.getPassword()))
						   .role(role)
						   .build();
		var savedUser = repository.save(user);
		System.out.println("Register: " + savedUser);
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		saveUserToken(savedUser, jwtToken);
		return AuthenticationResponse.builder()
					   .accessToken(jwtToken)
					   .refreshToken(refreshToken)
					   .build();
	}

	public LoginResponse authenticate(AuthenticationRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()
				)
		);
		User user = userService.findByUsername(request.getEmail());
		if (user.getId() == null) {
			return null;
		}
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		revokeAllUserTokens(user);
		saveUserToken(user, jwtToken);
		return new LoginResponse(Mapping.mapToUserDto(user), AuthenticationResponse.builder()
																	 .accessToken(jwtToken)
																	 .refreshToken(refreshToken)
																	 .build());
	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder()
							.user(user)
							.token(jwtToken)
							.tokenType(TokenType.BEARER)
							.expired(false)
							.revoked(false)
							.build();
		tokenRepository.save(token);
	}

	private void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}

	public void refreshToken(
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String refreshToken;
		final String userEmail;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		refreshToken = authHeader.substring(7);
		userEmail = jwtService.extractUsername(refreshToken);
		if (userEmail != null) {
			var user = this.repository.findByEmail(userEmail)
							   .orElseThrow();
			if (jwtService.isTokenValid(refreshToken, user)) {
				var accessToken = jwtService.generateToken(user);
				revokeAllUserTokens(user);
				saveUserToken(user, accessToken);
				var authResponse = AuthenticationResponse.builder()
										   .accessToken(accessToken)
										   .refreshToken(refreshToken)
										   .build();
				new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
			}
		}
	}

	public UserDTO claimUserDto(HttpServletRequest request) {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String token;
		final String userEmail;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return null;
		}
		token = authHeader.substring(7);
		Token t = tokenRepository.findByToken(token).orElse(null);
		if (t != null) {
			if (!t.isExpired() && !t.isRevoked()) {
				userEmail = jwtService.extractUsername(token);
				return Mapping.mapToUserDto(repository.findByEmail(userEmail).orElseThrow());
			} else {
				return null;
			}
		}
		return null;
	}

}
