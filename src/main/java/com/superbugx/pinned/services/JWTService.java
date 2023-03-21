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
	// Issuer
	static String issuer = "pinned-auth-server";

	// Token Durations
	private long accessTokenExpirationMs;
	private long refreshTokenExpirationMs;

	// Encryption Algorithms
	private Algorithm accessTokenAlgorithm;
	private Algorithm refreshTokenAlgorithm;
	private JWTVerifier accessTokenVerifier;
	private JWTVerifier refreshTokenVerifier;

	public JWTService(@Value("${jwt.access.key}") String accessTokenSecret,
			@Value("${jwt.refresh.key}") String refreshTokenSecret,
			@Value("${jwt.refresh.duration}") int refreshTokenExpirationDays,
			@Value("${jwt.access.duration}") int accessTokenExpirationMinutes) {

		//Set Attribute Values
		//Token Duration
		accessTokenExpirationMs = (long) accessTokenExpirationMinutes * 60 * 1000;
		refreshTokenExpirationMs = (long) refreshTokenExpirationDays * 24 * 60 * 60 * 1000;
		//Token Algorithms
		accessTokenAlgorithm = Algorithm.HMAC512(accessTokenSecret);
		refreshTokenAlgorithm = Algorithm.HMAC512(refreshTokenSecret);
		//Token Verifiers
		accessTokenVerifier = JWT.require(accessTokenAlgorithm).withIssuer(issuer).build();
		refreshTokenVerifier = JWT.require(refreshTokenAlgorithm).withIssuer(issuer).build();
	}

	//Generators
	//Generate a new token based on a User Model
	public String generateAccessToken(User user) {
		//Convert Spring GrantedAuthority List to String Array
		List<String> stringAuthorities = new ArrayList<String>(user.getAuthorities().size());
		user.getAuthorities().forEach(authority -> {
			stringAuthorities.add(authority.getAuthority().toString());
		});
		
		Date currentDate = new Date();
		return JWT.create().withIssuer(issuer).withSubject(user.getId()).withIssuedAt(currentDate)
				.withClaim("email", user.getEmail())
				.withClaim("username", user.getUsername())
				.withArrayClaim("authorities",(String[]) stringAuthorities.toArray(new String[stringAuthorities.size()]))
				.withExpiresAt(new Date(currentDate.getTime() + accessTokenExpirationMs)).sign(accessTokenAlgorithm);
	}

	//Generate a new refresh token based on a User Model and refresh token entity
	public String generateRefreshToken(User user, RefreshToken refreshToken) {
		return JWT.create().withIssuer(issuer).withSubject(user.getId()).withClaim("tokenId", refreshToken.getId())
				.withIssuedAt(new Date()).withExpiresAt(new Date((new Date()).getTime() + refreshTokenExpirationMs))
				.sign(refreshTokenAlgorithm);
	}

	//Decoders
	private Optional<DecodedJWT> decodeAccessToken(String token) {
		try {
			return Optional.of(accessTokenVerifier.verify(token));
		} catch (JWTVerificationException e) {
			log.error("invalid access token", e);
		}
		return Optional.empty();
	}

	private Optional<DecodedJWT> decodeRefreshToken(String token) {
		try {
			return Optional.of(refreshTokenVerifier.verify(token));
		} catch (JWTVerificationException e) {
			log.error("invalid refresh token", e);
		}
		return Optional.empty();
	}

	//Validators
	public boolean validateAccessToken(String token) {
		return decodeAccessToken(token).isPresent();
	}

	public boolean validateRefreshToken(String token) {
		return decodeRefreshToken(token).isPresent();
	}
	
	//Expiration Checkers
	public boolean tokenIsExpired(String token) {
		return decodeRefreshToken(token).get().getExpiresAt().before(new Date(System.currentTimeMillis()));
	}
	
	//Get Roles
	public Collection<? extends GrantedAuthority> getRolesFromAccessToken(String token) {
		return decodeAccessToken(token).get().getClaim("authorities").asList(SimpleGrantedAuthority.class);
	}

	//Get IDs
	public String getUserIdFromAccessToken(String token) {
		return decodeAccessToken(token).get().getSubject();
	}
	
	public String getUserIdFromRefreshToken(String token) {
		return decodeRefreshToken(token).get().getSubject();
	}

	public String getTokenIdFromRefreshToken(String token) {
		return decodeRefreshToken(token).get().getClaim("tokenId").asString();
	}
	
	//Claim Getters
	public String getUsernameFromAccessToken(String token) {
		return decodeAccessToken(token).get().getClaim("username").asString();
	}
	
	public String getEmailFromAccessToken(String token) {
		return decodeAccessToken(token).get().getClaim("email").asString();
	}
}
