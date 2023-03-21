package com.superbugx.pinned.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.superbugx.pinned.dto.requests.LoginDTO;
import com.superbugx.pinned.dto.responses.TokenDTO;
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
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private IRefreshTokenService refreshTokenService;
	@Autowired
	private JWTService jwtService;

	@PostMapping(value = "/login", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
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
		return ResponseEntity.ok(new TokenDTO(accessToken, refreshTokenString));
	}

	@PostMapping(value = "logout", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> logout(@RequestBody TokenDTO dto) {
		log.debug("Logout Request Received");

		// Validate Token
		String refreshToken = dto.getRefreshToken();

		if (!jwtService.validateRefreshToken(refreshToken)) {
			throw new BadCredentialsException("Invalid Token");
		}

		// Get currently authenticated user details
		User user = getUserDetails();

		// Authorisation Check
		String accessTokenId = jwtService.getUserIdFromAccessToken(refreshToken);

		if (user.getAuthorities().contains(Role.ADMIN.toString()) || user.getId().equals(accessTokenId)) {
			// Delete Refresh Token
			refreshTokenService.deleteById(jwtService.getTokenIdFromRefreshToken(refreshToken));
			return ResponseEntity.ok().build();
		} else {
			// UnAuthorised
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
	}

	@PostMapping(value="logout-all/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<?> logoutAll(@PathVariable String id) {
		log.debug("Logout All Request Received");

		// Get currently authenticated user details
		User user = getUserDetails();

		// Authorisation Check
		if (user.getAuthorities().contains(Role.ADMIN.toString()) || user.getId().equals(id)) {
			// Delete All Refresh Tokens BY This Owner ID
			refreshTokenService.deleteByUserId(id);

			// Return response
			return ResponseEntity.ok().build();
		} else {
			// UnAuthorised
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
	}

	@PostMapping("refresh")
	public ResponseEntity<?> refreshToken(@RequestBody TokenDTO dto) {
		log.debug("Token Refresh Request Received");

		// Validate Tokens
		String accessToken = dto.getAccessToken();
		String refreshToken = dto.getRefreshToken();
		if (!jwtService.validateRefreshToken(refreshToken) || !jwtService.validateAccessToken(accessToken)) {
			throw new BadCredentialsException("Invalid Token");
		}

		// Check Refresh Token Expiration
		if (jwtService.tokenIsExpired(refreshToken)) {
			throw new BadCredentialsException("Refresh Token Expired");
		}
		
		//Check Tokens User Id Match
		if(!jwtService.getUserIdFromRefreshToken(refreshToken).equals(jwtService.getUserIdFromAccessToken(accessToken))) {
			throw new BadCredentialsException("Invalid Token Pair");
		}
		
		//Check If Refresh Token is Valid (Unused)
		String refreshTokenId = jwtService.getTokenIdFromRefreshToken(refreshToken);
		if(!refreshTokenService.exists(refreshTokenId)) {
			throw new BadCredentialsException("Refresh Token Expired");
		}

		// Delete Old Refresh Token
		refreshTokenService.deleteById(refreshTokenId);

		// Create & Save Refresh Token Entity
		User user = new User();
		user.setId(jwtService.getUserIdFromAccessToken(accessToken));
		user.setEmail(jwtService.getEmailFromAccessToken(accessToken));
		user.setUsername(jwtService.getUsernameFromAccessToken(accessToken));
		user.setAuthorities(jwtService.getRolesFromAccessToken(accessToken));

		RefreshToken newRefreshToken = new RefreshToken();
		newRefreshToken.setOwner(user);
		refreshTokenService.save(newRefreshToken);

		// Generate New Access & Refresh Token Pair
		String newAccessToken = jwtService.generateAccessToken(user);
		String newRefreshTokenString = jwtService.generateRefreshToken(user, newRefreshToken);

		// Return New Tokens
		return ResponseEntity.ok(new TokenDTO(newAccessToken, newRefreshTokenString));
	}

	// Retrieves the current users authentication/authorisation details
	private User getUserDetails() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
}
