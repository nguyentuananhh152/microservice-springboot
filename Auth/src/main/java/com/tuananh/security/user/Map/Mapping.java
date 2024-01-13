package com.tuananh.security.user.Map;

import com.tuananh.security.user.User;
import com.tuananh.security.user.dto.UserDTO;

public class Mapping {
	public static User mapToUser(UserDTO userDTO) {
		return null;
	}

	public static UserDTO mapToUserDto(User user) {
		UserDTO userDTO = new UserDTO();
		userDTO.setId(user.getId());
		userDTO.setFirstname(user.getFirstname());
		userDTO.setLastname(user.getLastname());
		userDTO.setEmail(user.getEmail());
		userDTO.setRole(user.getRole());
		return userDTO;
	}


}
