package com.tuananh.security.config;

import com.tuananh.security.auditing.ApplicationAuditAware;
import com.tuananh.security.user.Role;
import com.tuananh.security.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

//  private final UserRepository repository;

  private final JdbcTemplate jdbcTemplate;


  // Load balanced
  @Bean
  @LoadBalanced
  public RestTemplate restTemplate() {
	  return new RestTemplate();
  }

  @Bean
  public UserDetailsService userDetailsService() {

    return username -> {
      String sql = "SELECT * FROM _user u WHERE u.email LIKE '" + username + "'";
		return jdbcTemplate.query(sql, new ResultSetExtractor<User>() {
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
          System.out.println("ApplicationConfig: " + u);
          return u;
        }
      });
    };
//	  return username -> repository.findByEmail(username)
//								 .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuditorAware<Integer> auditorAware() {
    return new ApplicationAuditAware();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
