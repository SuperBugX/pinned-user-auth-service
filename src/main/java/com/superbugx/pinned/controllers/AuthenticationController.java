package com.superbugx.pinned.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.superbugx.pinned.dto.requests.LoginDTO;
import com.superbugx.pinned.enums.Role;
import com.superbugx.pinned.interfaces.services.IRefreshTokenService;
import com.superbugx.pinned.models.RefreshToken;
import com.superbugx.pinned.models.User;
import com.superbugx.pinned.services.JWTService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/authentication")
@Slf4j
public class AuthenticationController {
	// Attributes
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private IRefreshTokenService refreshTokenService;
	@Autowired
	private JWTService jwtService;

	// JWT Variables
	@Value("${jwt.access.name}")
	private String accessTokenName;
	@Value("${jwt.refresh.name}")
	private String refreshTokenName;

	@PostMapping(value = "/login", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
		log.debug("Login Request Received");

		// Use PreConfigured Authentication Manager to Authenticate Credentials
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		User user = (User) authentication.getPrincipal();

		// Create and Save Refresh Token Entity
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setOwner(user);
		refreshTokenService.save(refreshToken);

		// Generate Actual Tokens
		String accessToken = jwtService.generateAccessToken(user);
		String refreshTokenString = jwtService.generateRefreshToken(user, refreshToken);

		// Return Token
		return ResponseEntity.ok().headers(createServerSideCookies(accessToken, refreshTokenString)).build();
	}

	@PostMapping(value = "logout", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> logout(@CookieValue(name = "refresh-token") String refreshToken) {
		log.debug("Logout Request Received");

		// Validate Token
		if (!jwtService.setToken(refreshToken)) {
			throw new BadCredentialsException("Invalid Token");
		}

		// Get currently authenticated user details
		User user = getUserDetails();

		// Authorisation Check
		String refreshTokenUserId = jwtService.getUserId().get();
		if (user.getAuthorities().contains(Role.ADMIN.toString()) || user.getId().equals(refreshTokenUserId)) {
			// Delete Refresh Token
			refreshTokenService.deleteById(jwtService.getTokenId().get());

			return ResponseEntity.ok().headers(removeServerSideCookies()).build();
		} else {
			// UnAuthorised
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	@PostMapping(value = "logout-all/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> logoutAll(@PathVariable String id) {
		log.debug("Logout All Request Received");

		// Get currently authenticated user details
		User user = getUserDetails();

		// Authorisation Check
		if (user.getAuthorities().contains(Role.ADMIN.toString()) || user.getId().equals(id)) {
			// Delete All Refresh Tokens BY This Owner ID
			refreshTokenService.deleteByUserId(id);

			// Return response
			return ResponseEntity.ok().headers(removeServerSideCookies()).build();
		} else {
			// UnAuthorised
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
	}

	@PostMapping("refresh")
	public ResponseEntity<?> refreshToken(@CookieValue(name = "access-token") String accessToken,
			@CookieValue(name = "refresh-token") String refreshToken) {
		log.debug("Token Refresh Request Received");

		// Validate Tokens
		if (!jwtService.setToken(refreshToken) || !jwtService.setToken(accessToken)) {
			throw new BadCredentialsException("Invalid Token");
		}

		// Set Refresh Token
		jwtService.setToken(refreshToken);

		// Check Refresh Token Expiration
		if (jwtService.tokenIsExpired()) {
			throw new BadCredentialsException("Refresh Token Expired");
		}

		// Extract Refresh Token Details
		String refreshTokenUser = jwtService.getUserId().get();
		String refreshTokenId = jwtService.getTokenId().get();
		// Extract Access Token Details
		jwtService.setToken(accessToken);
		String accessTokenUser = jwtService.getUserId().get();

		// Check Tokens User Id Match
		if (!refreshTokenUser.equals(accessTokenUser)) {
			throw new BadCredentialsException("Invalid Token Pair");
		}

		// Check If Refresh Token is Valid (Unused)
		if (!refreshTokenService.exists(refreshTokenId)) {
			throw new BadCredentialsException("Refresh Token Expired");
		}

		// Delete Old Refresh Token
		refreshTokenService.deleteById(refreshTokenId);

		// Create & Save Refresh Token Entity
		User user = new User();
		user.setId(jwtService.getUserId().get());
		user.setEmail(jwtService.getEmail().get());
		user.setUsername(jwtService.getUsername().get());
		user.setAuthorities(jwtService.getRoles());

		RefreshToken newRefreshToken = new RefreshToken();
		newRefreshToken.setOwner(user);
		refreshTokenService.save(newRefreshToken);

		// Generate New Access & Refresh Token Pair
		String newAccessToken = jwtService.generateAccessToken(user);
		String newRefreshTokenString = jwtService.generateRefreshToken(user, newRefreshToken);

		// Return New Tokens
		return ResponseEntity.ok().headers(createServerSideCookies(newAccessToken, newRefreshTokenString)).build();
	}

	// Retrieves the current users authentication/authorisation details
	private User getUserDetails() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	// Create a new HttpHeader object which includes all server side cookies
	private HttpHeaders createServerSideCookies(String accessToken, String refreshToken) {
		// Add Server-Side Cookies
		HttpHeaders httpHeaders = new HttpHeaders();
		// Access Cookie
		httpHeaders.add("Set-Cookie", accessTokenName + "=" + accessToken + "; Max-Age="
				+ jwtService.getAccessTokenExpiration()/1000 + "; Path=/; HttpOnly; SameSite=Lax");
		// Refresh Cookie
		httpHeaders.add("Set-Cookie",
				refreshTokenName + "=" + refreshToken + "; Max-Age=" + jwtService.getRefreshTokenExpiration()/1000
						+ "; Path=/api/authentication/refresh; HttpOnly; SameSite=Lax");
		return httpHeaders;
	}

	// Create a new HttpHeader object which removes all server side cookies
	private HttpHeaders removeServerSideCookies() {
		// Delete Server-Side Cookies
		HttpHeaders httpHeaders = new HttpHeaders();
		// Access Cookie
		httpHeaders.add("Set-Cookie", accessTokenName + "=" + null + "; Path=/; HttpOnly;");
		// Refresh Cookie
		httpHeaders.add("Set-Cookie", refreshTokenName + "=" + null + "; Path=/api/authentication/refresh; HttpOnly;");
		return httpHeaders;
	}
}
