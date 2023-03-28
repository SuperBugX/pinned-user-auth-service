package com.superbugx.pinned.services;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.superbugx.pinned.database.repository.UserRepository;
import com.superbugx.pinned.enums.Role;
import com.superbugx.pinned.exceptions.BadUserException;
import com.superbugx.pinned.exceptions.EmailNotUniqueException;
import com.superbugx.pinned.exceptions.UsernameNotUniqueException;
import com.superbugx.pinned.interfaces.services.IUserService;
import com.superbugx.pinned.models.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class UserService implements IUserService {

	@Autowired
	private UserRepository userRepo;

	@Override
	public String register(User newUser) throws EmailNotUniqueException, UsernameNotUniqueException, BadUserException {
		log.debug("User Service: Attempt Register");
		// Allow repository to decide ID
		newUser.setId(null);

		// Check Null Values
		if (newUser.getEmail() == null || newUser.getPassword() == null || newUser.getUsername() == null) {
			throw new BadUserException("User is incomplete");
		}
		
		//Trim Inputs
		newUser.setEmail(newUser.getEmail().trim());
		newUser.setPassword(newUser.getPassword().trim());
		newUser.setUsername(newUser.getUsername().trim());

		// Check if Username is correct
		if (newUser.getUsername().length() < 4) {
			throw new BadUserException("Username is too small");
		}

		// Check if Email is correct
		String emailRegex = "^[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
		Pattern pattern = Pattern.compile(emailRegex);
		Matcher matcher = pattern.matcher(newUser.getEmail());

		if (!matcher.matches()) {
			throw new BadUserException("Email is incorrect");
		}

		// Check if Password is correct
		// Minimum eight characters, at least one uppercase letter, one lowercase letter
		// and one number
		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
		pattern = Pattern.compile(passwordRegex);
		matcher = pattern.matcher(newUser.getPassword());

		if (!matcher.matches()) {
			throw new BadUserException("Password is incorrect");
		}

		// Check if Email is Unique
		if (userRepo.findByEmail(newUser.getEmail()).isPresent()) {
			throw new EmailNotUniqueException("Email already in use");
		}

		// Check if Username is unique
		if (userRepo.findByUsername(newUser.getUsername()).isPresent()) {
			throw new UsernameNotUniqueException("Username already in use");
		}

		// Create a new password hash
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashedPassword = passwordEncoder.encode(newUser.getPassword());
		newUser.setPassword(hashedPassword);

		// Default Roles
		List<GrantedAuthority> roles = new LinkedList<GrantedAuthority>();
		roles.add(new SimpleGrantedAuthority(Role.USER.toString()));
		newUser.setAuthorities(roles);

		// Save new user entry
		return (userRepo.save(newUser)).getId();
	}
	
	@Override
	public User getById(String id) {
		log.debug("User Service: Get By ID");
		return userRepo.findById(id).orElseThrow();
	}

	@Override
	public boolean deleteById(String id) {
		log.debug("User Service: Attempt Delete");
		// Delete User
		userRepo.deleteById(id);
		return true;
	}

	@Override
	//Need by Spring Security for authentication purposes
	//Finds a user by username (or email) and returns it from storage.
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.debug("Load User By Name Attempt");
		//Get user by ID
		Optional<User> userOptional = userRepo.findByEmailOrUsername(username);

		// Check If Exists
		if (userOptional.isEmpty()) {
			throw new UsernameNotFoundException("No User Found");
		}

		// return user details
		return userOptional.get();
	}
}
