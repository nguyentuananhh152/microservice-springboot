package com.tuananh.security.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tuananh.security.auth.AuthenticationResponse;
import com.tuananh.security.user.dto.UserDTO;

public class LoginResponse {
	@JsonProperty("user")
	private UserDTO userDTO;
	private AuthenticationResponse token;

	public UserDTO getUserDTO() {
		return userDTO;
	}

	public LoginResponse(UserDTO userDTO, AuthenticationResponse token) {
		this.userDTO = userDTO;
		this.token = token;
	}

	public void setUserDTO(UserDTO userDTO) {
		this.userDTO = userDTO;
	}

	public AuthenticationResponse getToken() {
		return token;
	}

	public void setToken(AuthenticationResponse token) {
		this.token = token;
	}
}
