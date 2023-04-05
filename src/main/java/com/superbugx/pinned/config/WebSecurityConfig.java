package com.superbugx.pinned.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.superbugx.pinned.filters.AccessTokenEntryPoint;
import com.superbugx.pinned.filters.AccessTokenFilter;
import com.superbugx.pinned.interfaces.services.IUserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	//Attributes
	@Autowired
	private IUserService userDetailsService;
	
	@Value("${client.origin}")
	private String clientOrigin;

	@Bean
	// Dependency Inject the password encoder used
	public PasswordEncoder userPasswordEncoder() {
		return new BCryptPasswordEncoder(11);
	}

	@Autowired
	// Authentication Failure Handler
	private AccessTokenEntryPoint accessTokenEntryPoint;

	@Bean
	// Authentication Filter
	public AccessTokenFilter accessTokenFilter() {
		return new AccessTokenFilter();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
		return authConfiguration.getAuthenticationManager();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(userPasswordEncoder());
		return authProvider;
	}

	@Bean
	// Configure URL paths security
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// For now until we enter production, Disable CORS and CSRF
		// Disable Spring Boot Security default login page
		http.cors().and().csrf().disable();
		http.formLogin().disable();
		//Add Routes Config
		http.authorizeHttpRequests().requestMatchers("/api/user/register/**", "/api/authentication/login/**", "/api/authentication/refresh/**").permitAll();
		http.authorizeHttpRequests().anyRequest().authenticated();
		// Add exception handler for authentication failures
		http.exceptionHandling().authenticationEntryPoint(accessTokenEntryPoint);
		// Indicate to Spring Boot that every request is stateless
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		// Add Web Filter to authenticate requests if a token is provided
		http.addFilterBefore(accessTokenFilter(), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
	

    @Bean
    //CORs Config
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(clientOrigin));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
