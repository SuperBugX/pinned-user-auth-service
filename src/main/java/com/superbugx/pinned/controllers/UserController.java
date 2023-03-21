package com.superbugx.pinned.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.superbugx.pinned.dto.requests.RegisterUserDTO;
import com.superbugx.pinned.dto.responses.GenericResponse;
import com.superbugx.pinned.enums.Role;
import com.superbugx.pinned.exceptions.EmailNotUniqueException;
import com.superbugx.pinned.exceptions.UsernameNotUniqueException;
import com.superbugx.pinned.interfaces.services.IRefreshTokenService;
import com.superbugx.pinned.interfaces.services.IUserService;
import com.superbugx.pinned.models.User;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {
	// Services
	@Autowired
	private IUserService userService;
	@Autowired
	private IRefreshTokenService refreshTokenService;

	// POST
	/**
	 * Create a new user account. Only the Email, UserName, and password fields are
	 * considered.
	 * 
	 * @param newUserDTO - JSON input which is a 1:1 representation of the
	 *                   RegisterUserDTO
	 * @return - new user ID
	 * @throws EmailNotUniqueException
	 * @throws UsernameNotUniqueException
	 * @throws UsernameNotUniqueException
	 */
	@PostMapping(value = "/register", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<GenericResponse<String>> register(@RequestBody RegisterUserDTO newUserDTO) throws Exception {
		log.debug("User Register Request Received");

		// Convert DTO to Model
		User newUser = new User();
		newUser.setEmail(newUserDTO.getEmail());
		newUser.setPassword(newUserDTO.getPassword());
		newUser.setUsername(newUserDTO.getUsername());

		// Attempt to register the new user
		String newId = userService.register(newUser);

		// Create Generic HATEOAS Response with the new users ID
		GenericResponse<String> response = new GenericResponse<String>(newId);

		// Add HATEOAS links
		Link deleteLink = linkTo(methodOn(UserController.class).delete(newId)).withRel("delete");
		response.add(deleteLink);

		// Return response
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
	}

	// DELETE
	/**
	 * Delete a user based on ID. Will only succeed if the requester is an ADMIN or
	 * the user requesting the deletion matches the id provided. On success, all
	 * related and active refresh tokens are deleted.
	 * 
	 * @param id - ID of user to be deleted
	 * @return - Boolean on Success or Failure
	 * @throws Exception
	 */
	@DeleteMapping(value = "/delete/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<GenericResponse<Boolean>> delete(@PathVariable String id) throws Exception {
		log.debug("User Delete Request Received");
		// Get currently authenticated user details
		User user = getUserDetails();

		// Authorisation Check
		if (user.getAuthorities().contains(Role.ADMIN.toString()) || user.getId().equals(id)) {
			// Delete user
			boolean result = userService.deleteById(id);

			// Delete Refresh Token
			if (result) {
				refreshTokenService.deleteByUserId(id);
			}

			// Create Generic HATEOAS Response
			GenericResponse<Boolean> response = new GenericResponse<Boolean>(result);

			// Return response
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
		} else {
			// UnAuthorised
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
	}

	// Retrieves the current users authentication/authorisation details
	private User getUserDetails() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
}
