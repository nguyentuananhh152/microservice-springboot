package com.tuananh.security.user;

import com.tuananh.security.user.model.ChangePasswordRequest;
import com.tuananh.security.user.model.Role;
import com.tuananh.security.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
	private final JdbcTemplate jdbcTemplate;
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        repository.save(user);
    }

	public User findByUsername(String username) {
		String sql = "SELECT * FROM _user u WHERE u.email LIKE '" + username + "'";
		User user = jdbcTemplate.query(sql, new ResultSetExtractor<User>() {
			@Override
			public User extractData(ResultSet rs) throws SQLException, DataAccessException {
				User u = new User();
				while (rs.next()) {
					u.setId(rs.getInt("id"));
					u.setFirstname(rs.getString("firstname"));
					u.setLastname(rs.getString("lastname"));
					u.setEmail(rs.getString("email"));
					u.setPassword(rs.getString("password"));
					u.setRole(Role.valueOf(rs.getString("role")));
				}
				System.out.println("UserService: " + u);
				return u;
			}
		});
		return user;
	}
}
