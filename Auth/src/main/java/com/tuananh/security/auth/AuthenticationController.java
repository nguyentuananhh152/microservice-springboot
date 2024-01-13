package com.tuananh.security.auth;

import com.tuananh.security.config.LoginResponse;
import com.tuananh.security.config.LogoutService;
import com.tuananh.security.user.Role;
import com.tuananh.security.user.User;
import com.tuananh.security.user.UserService;
import com.tuananh.security.user.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthenticationController {

	private final AuthenticationService service;
	private final LogoutService logoutService;
	private final UserService userService;
	private final String KEY_REGISTER_ADMIN = "admin";
	private final String KEY_REGISTER_MANAGER = "manager";

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(
			@RequestBody RegisterRequest request
	) {
		System.out.println("Reghister");
		if (userService.findByUsername(request.getEmail()).getId() != null) {
			return ResponseEntity.status(422).body(null);
		} else {
			return ResponseEntity.ok(service.register(request, Role.USER));
		}
	}

	@PostMapping("/admin/register")
	public ResponseEntity<AuthenticationResponse> adminRegister(
			@RequestBody RegisterRequest request,
			@PathParam("key") String key
	) {
		if (key.equals(KEY_REGISTER_ADMIN)) {
			if (userService.findByUsername(request.getEmail()).getId() == null) {
				return ResponseEntity.ok(service.register(request, Role.ADMIN));
			}
		}
		return ResponseEntity.status(422).body(null);
	}

	@PostMapping("/manager/register")
	public ResponseEntity<AuthenticationResponse> managerRegister(
			@RequestBody RegisterRequest request,
			@PathParam("key") String key
	) {
		if (key.equals(KEY_REGISTER_MANAGER)) {
			if (userService.findByUsername(request.getEmail()).getId() == null) {
				return ResponseEntity.ok(service.register(request, Role.MANAGER));
			}
		}
		return ResponseEntity.status(422).body(null);
	}

	@PostMapping("/login")	// Refresh access token
	public ResponseEntity<LoginResponse> authenticate(
			@RequestBody AuthenticationRequest request
	) {
		return ResponseEntity.ok(service.authenticate(request));
	}

	@GetMapping("/check-token")
	public ResponseEntity<UserDTO> generateToken(
			HttpServletRequest request
	) {
		System.out.println("Check token");
		UserDTO u = service.claimUserDto(request);
		if (u != null) {
			return ResponseEntity.ok(u);
		}
		return ResponseEntity.status(401).body(null);
	}


	@PostMapping("/refresh-token")
	public void refreshToken(
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException {
		service.refreshToken(request, response);
	}

	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request,
										 HttpServletResponse response,
										 Authentication authentication) {
		try {
			boolean logout = logoutService.logoutCustom(request, response, authentication);
			if (logout) {
//				logoutService.logout(request, response, authentication);
				return ResponseEntity.ok().body("Logout success");
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT");
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error when logout");
		}
	}


}
