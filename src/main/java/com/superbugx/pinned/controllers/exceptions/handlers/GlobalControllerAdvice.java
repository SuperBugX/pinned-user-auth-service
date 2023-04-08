package com.superbugx.pinned.controllers.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.superbugx.pinned.exceptions.BadUserException;
import com.superbugx.pinned.exceptions.EmailNotUniqueException;
import com.superbugx.pinned.exceptions.QueryException;
import com.superbugx.pinned.exceptions.UsernameNotUniqueException;

/*
 * Class is responsible for catching and transforming exceptions 
 * thrown by any controller (globally) into a predictable response. This class
 * is generic in error handling.
 */

@ControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {
	@ExceptionHandler(QueryException.class)
	public ResponseEntity<Object> axonQueryError(Exception ex, WebRequest request) {
		return new ResponseEntity<>("Unable to query", HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(UsernameNotUniqueException.class)
	public ResponseEntity<Object> handleUsernamenotUnique(Exception ex, WebRequest request) {
		return new ResponseEntity<>("Username is not unique", HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(EmailNotUniqueException.class)
	public ResponseEntity<Object> handleEmailNotUnique(Exception ex, WebRequest request) {
		return new ResponseEntity<>("Email is already in use", HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(BadUserException.class)
	public ResponseEntity<Object> handleBadUser(Exception ex, WebRequest request) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Object> handleCrendentials(Exception ex, WebRequest request) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleInternalError(Exception ex, WebRequest request) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
