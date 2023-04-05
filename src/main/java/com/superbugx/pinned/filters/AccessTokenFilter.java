package com.superbugx.pinned.filters;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.superbugx.pinned.models.User;
import com.superbugx.pinned.services.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessTokenFilter extends OncePerRequestFilter {
	// Attributes
	@Autowired
	private JWTService jwtService;
	@Value("${jwt.access.name}")
	private String accessTokenName;

	@Override
	// Attempt Token Authentication if a token is provided
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			// Extract token from header
			Optional<String> accessToken = getCookie(request, accessTokenName);

			// Null Check & Validate Token
			if (accessToken.isPresent() && jwtService.setToken(accessToken.get())) {
				log.debug("Token is received and valid");

				if (!jwtService.tokenIsExpired()) {
					log.debug("Token has not expired");

					// Create Principal
					User user = User.builder()
							.email(jwtService.getEmail().get())
							.username(jwtService.getUsername().get())
							.authorities(jwtService.getRoles())
							.id(jwtService.getUserId().get())
							.build();

					// Create and set Security Context (Principal)
					UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(user, null,
							user.getAuthorities());
					upat.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(upat);
				} else {
					log.error("Token has expired");
				}
			}
		} catch (Exception e) {
			log.error("Cannot set authentication", e);
		}

		// Resume Execution
		filterChain.doFilter(request, response);
	}

	private Optional<String> getCookie(HttpServletRequest request, String cookieName) {
		// Get cookies from the request
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}

		// Find the cookie with the cookie name for the JWT token
		for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = cookies[i];
			log.debug(cookie.getName());
			if (!cookie.getName().equals(cookieName)) {
				continue;
			}
			// If we find the JWT cookie, return its value
			return Optional.of(cookie.getValue());
		}

		// Return empty if no cookie is found
		return Optional.empty();
	}
}