package com.esl.config;

import com.esl.security.JWTAuthorizationFilter;
import com.esl.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Value(value = "${testing:false}")
	private boolean isTesting;

	@Autowired private JWTService jwtService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);

		http
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests((authz) -> authz
					.requestMatchers(HttpMethod.GET, "/member/**").authenticated()
					.requestMatchers(HttpMethod.POST, "/member/**").authenticated()
					.requestMatchers(HttpMethod.GET, "/admin/**").hasRole("admin")
					.requestMatchers(HttpMethod.POST, "/admin/**").hasRole("admin")
					.anyRequest().permitAll()
			)
			.httpBasic(withDefaults())
			// this disables session creation on Spring Security
			.sessionManagement((sessionManager) -> sessionManager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(withDefaults());
		http.apply(new JWTFilterConfigurer(jwtService, isTesting));
		return http.build();
	}

}
