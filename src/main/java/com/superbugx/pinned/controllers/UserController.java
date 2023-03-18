package com.superbugx.pinned.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.superbugx.pinned.dto.requests.RegisterUserDTO;
import com.superbugx.pinned.dto.responses.GenericResponse;
import com.superbugx.pinned.interfaces.services.UserService;
import com.superbugx.pinned.models.User;

@RestController
@RequestMapping("/account")
public class UserController {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	// Services
	@Autowired
	UserService userService;

	// POST
	/**
	 * Create a new user account based on JSON input. Only the Email, UserName, and
	 * password fields are considered.
	 * 
	 * @param newUserDTO - JSON input which is a 1:1 representation of the RegisterUserDTO
	 * @return - new user ID
	 * @throws EmailNotUniqueException
	 * @throws UsernameNotUniqueException
	 * @throws UsernameNotUniqueException
	 */
	@PostMapping(value = "/register", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<GenericResponse<String>> register(@RequestBody RegisterUserDTO newUserDTO) throws Exception {
		logger.info("User Register Request Received");
		
		//Convert DTO to Model
		User newUser = new User();
		newUser.setEmail(newUserDTO.getEmail());
		newUser.setPassword(newUser.getPassword());
		newUser.setUsername(newUser.getUsername());

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
	 * 
	 * @param id - ID of user to be deleted
	 * @return - Boolean on Success or Failure
	 * @throws Exception
	 */
	@DeleteMapping(value = "/delete/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<GenericResponse<Boolean>> delete(@PathVariable String id) throws Exception {
		
		logger.info("User Delete Request Received");
		
		// Delete user
		boolean result = userService.delete(id);

		// Create Generic HATEOAS Response
		GenericResponse<Boolean> response = new GenericResponse<Boolean>(result);

		// Return response
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(response);
	}
}
