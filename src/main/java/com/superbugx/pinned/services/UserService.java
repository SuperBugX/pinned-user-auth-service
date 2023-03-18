package com.superbugx.pinned.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.superbugx.pinned.database.models.UserDAO;
import com.superbugx.pinned.database.repository.UserRepository;
import com.superbugx.pinned.exceptions.BadUserException;
import com.superbugx.pinned.exceptions.EmailNotUniqueException;
import com.superbugx.pinned.exceptions.UsernameNotUniqueException;
import com.superbugx.pinned.interfaces.services.IUserService;
import com.superbugx.pinned.models.User;

@Service
public class UserService implements IUserService {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	UserRepository userRepo;

	@Override
	public String register(User newUser) throws EmailNotUniqueException, UsernameNotUniqueException, BadUserException {
		logger.info("User Service: Attempt Register");
		// Create a new User DAO from input
		UserDAO userDao = new UserDAO();
		BeanUtils.copyProperties(newUser, userDao);
		// Allow repository to decide ID
		userDao.setId(null);

		// Check Null Values
		if (userDao.getEmail() == null || userDao.getPassword() == null || userDao.getUsername() == null) {
			throw new BadUserException("User is incomplete");
		}
		
		//Check if Username is correct
		if(userDao.getUsername().length() < 4) {
			throw new BadUserException("Username is too small");
		}

		// Check if Email is correct
		String emailRegex = "^[a-zA-Z0-9_!#$%&amp;'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
		Pattern pattern = Pattern.compile(emailRegex);
		Matcher matcher = pattern.matcher(userDao.getEmail());
				
		if (!matcher.matches()) {
			throw new BadUserException("Email is incorrect");
		}
		
		//Check if Password is correct
		//Minimum eight characters, at least one uppercase letter, one lowercase letter and one number
		String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
		pattern = Pattern.compile(passwordRegex);
		matcher = pattern.matcher(userDao.getPassword());
		
		if (!matcher.matches()) {
			throw new BadUserException("Password is incorrect");
		}
		
		// Check if Email is Unique
		if (userRepo.findByEmail(newUser.getEmail()) != null) {
			throw new EmailNotUniqueException("Email already in use");
		}

		// Check if Username is unique
		if (userRepo.findByUsername(newUser.getUsername()) != null) {
			throw new UsernameNotUniqueException("Username already in use");
		}

		// Create a new password hash
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashedPassword = passwordEncoder.encode(userDao.getPassword());
		userDao.setPassword(hashedPassword);

		// Set default roles
		userDao.setRoles(new String[] { "user" });

		// Save new user entry
		return (userRepo.save(userDao)).getId();
	}

	@Override
	public boolean delete(String id) {
		logger.info("User Service: Attempt Delete");
		// Delete User
		userRepo.deleteById(id);
		return true;
	}
}
