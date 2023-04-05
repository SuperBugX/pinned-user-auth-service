package com.superbugx.pinned.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.superbugx.pinned.models.RefreshToken;
import com.superbugx.pinned.models.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JWTService {
	// Attributes
	// Issuer
	private String issuer;

	// Token Durations
	// In Minutes
	private long accessTokenExpiration;
	// In Days
	private long refreshTokenExpiration;

	// Encryption Algorithms
	private Algorithm accessTokenAlgorithm;
	private Algorithm refreshTokenAlgorithm;
	// Verifiers
	private JWTVerifier accessTokenVerifier;
	private JWTVerifier refreshTokenVerifier;

	// Token
	private DecodedJWT decodedToken;
	private boolean isAccessToken;

	public JWTService(@Value("${jwt.issuer}") String issuer, @Value("${jwt.access.key}") String accessTokenSecret,
			@Value("${jwt.refresh.key}") String refreshTokenSecret,
			@Value("${jwt.refresh.duration}") long refreshTokenExpirationDays,
			@Value("${jwt.access.duration}") long accessTokenExpirationMinutes) {
		// Default Values
		decodedToken = null;
		isAccessToken = false;

		// Set Attribute Values
		this.issuer = issuer;
		// Token Duration
		accessTokenExpiration = accessTokenExpirationMinutes * 60 * 1000;
		refreshTokenExpiration = refreshTokenExpirationDays * 24 * 60 * 60 * 1000;
		// Token Algorithms
		accessTokenAlgorithm = Algorithm.HMAC512(accessTokenSecret);
		refreshTokenAlgorithm = Algorithm.HMAC512(refreshTokenSecret);
		// Token Verifiers
		accessTokenVerifier = JWT.require(accessTokenAlgorithm).withIssuer(issuer).build();
		refreshTokenVerifier = JWT.require(refreshTokenAlgorithm).withIssuer(issuer).build();
	}

	// Getters
	public long getAccessTokenExpiration() {
		return accessTokenExpiration;
	}

	public long getRefreshTokenExpiration() {
		return refreshTokenExpiration;
	}

	// Generators
	// Generate a new token based on a User Model
	public String generateAccessToken(User user) {
		log.debug("Access Token Generated For : " + user.getId());

		// Convert Spring GrantedAuthority List to String Array
		List<String> stringAuthorities = new ArrayList<String>(user.getAuthorities().size());
		user.getAuthorities().forEach(authority -> {
			stringAuthorities.add(authority.getAuthority().toString());
		});

		Date currentDate = new Date();
		return JWT.create().withIssuer(issuer).withSubject(user.getId()).withIssuedAt(currentDate)
				.withClaim("email", user.getEmail()).withClaim("username", user.getUsername())
				.withArrayClaim("authorities",
						(String[]) stringAuthorities.toArray(new String[stringAuthorities.size()]))
				.withExpiresAt(new Date(currentDate.getTime() + accessTokenExpiration)).sign(accessTokenAlgorithm);
	}

	// Generate a new refresh token based on a User Model and refresh token entity
	public String generateRefreshToken(User user, RefreshToken refreshToken) {
		log.debug("Refresh Token Generated For : " + user.getId());

		return JWT.create().withIssuer(issuer).withSubject(user.getId()).withClaim("tokenId", refreshToken.getId())
				.withIssuedAt(new Date()).withExpiresAt(new Date((new Date()).getTime() + refreshTokenExpiration))
				.sign(refreshTokenAlgorithm);
	}

	// Decoder
	public boolean setToken(String newToken) {
		// Verify & Set Token
		try {
			// Is Access Token
			decodedToken = accessTokenVerifier.verify(newToken);
			isAccessToken = true;
			return true;
		} catch (JWTVerificationException e) {
		}

		try {
			// Is Refresh Token
			decodedToken = refreshTokenVerifier.verify(newToken);
			isAccessToken = false;
			return true;
		} catch (JWTVerificationException e) {
		}

		// Failure, Invalid Token
		decodedToken = null;
		isAccessToken = false;
		return false;
	}

	// Validators
	public boolean isTokenValid() {
		return decodedToken != null;
	}

	public boolean isAccessToken() throws IllegalStateException {
		if (isTokenValid())
			return isAccessToken;
		throw new IllegalStateException("Invalid Token");
	}

	public boolean isRefreshToken() throws IllegalStateException {
		if (isTokenValid())
			return !isAccessToken;
		throw new IllegalStateException("Invalid Token");
	}

	// Expiration Checker
	public boolean tokenIsExpired() throws IllegalStateException {
		if (isTokenValid())
			return decodedToken.getExpiresAt().before(new Date(System.currentTimeMillis()));
		throw new IllegalStateException("Invalid Token");
	}

	// Get Roles
	public Collection<? extends GrantedAuthority> getRoles() throws IllegalStateException {
		if (isTokenValid())
			return decodedToken.getClaim("authorities").asList(SimpleGrantedAuthority.class);
		throw new IllegalStateException("Invalid Token");
	}

	// Get IDs
	public Optional<String> getUserId() throws IllegalStateException {
		if (isTokenValid())
			return Optional.ofNullable(decodedToken.getSubject());
		throw new IllegalStateException("Invalid Token");
	}

	public Optional<String> getTokenId() throws IllegalStateException {
		if (isTokenValid()) {
			if (isRefreshToken())
				return Optional.ofNullable(decodedToken.getClaim("tokenId").asString());
			return null;
		} else {
			throw new IllegalStateException("Invalid Token");
		}
	}

	// Claim Getters
	public Optional<String> getUsername() throws IllegalStateException {
		if (isTokenValid())
			return Optional.ofNullable(decodedToken.getClaim("username").asString());
		throw new IllegalStateException("Invalid Token");
	}

	public Optional<String> getEmail() throws IllegalStateException {
		if (isTokenValid())
			return Optional.ofNullable(decodedToken.getClaim("email").asString());
		throw new IllegalStateException("Invalid Token");
	}

	public Optional<String> getCustomClaim(String claim) throws IllegalStateException {
		if (isTokenValid())
			return Optional.ofNullable(decodedToken.getClaim(claim).asString());
		throw new IllegalStateException("Invalid Token");
	}
}
