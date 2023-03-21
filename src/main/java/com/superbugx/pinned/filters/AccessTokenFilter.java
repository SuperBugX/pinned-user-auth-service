package com.superbugx.pinned.filters;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.superbugx.pinned.models.User;
import com.superbugx.pinned.services.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessTokenFilter extends OncePerRequestFilter {
	@Autowired
	private JWTService jwtService;

	@Override
	// Attempt Token Authentication if a token is provided
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			// Extract token from header
			Optional<String> accessToken = parseAccessToken(request);

			// Null Check & Validate Token
			if (accessToken.isPresent() && jwtService.validateAccessToken(accessToken.get())) {
				log.debug("Token is received and valid");
				String token = accessToken.get();

				if (!jwtService.tokenIsExpired(token)) {
					log.debug("Token has not expired");

					// Create Principal
					User user = 
							User.builder()
							.email(jwtService.getEmailFromAccessToken(token))
							.username(jwtService.getUsernameFromAccessToken(token))
							.authorities(jwtService.getRolesFromAccessToken(token))
							.id(jwtService.getUserIdFromAccessToken(token))
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

	// Extract token from header if it exists
	private Optional<String> parseAccessToken(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");

		// Extract
		if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer "))
			return Optional.of(authHeader.replace("Bearer ", ""));
		
		return Optional.empty();
	}
}