package com.superbugx.pinned.interfaces.services;

import java.util.NoSuchElementException;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.superbugx.pinned.exceptions.BadUserException;
import com.superbugx.pinned.exceptions.EmailNotUniqueException;
import com.superbugx.pinned.exceptions.UsernameNotUniqueException;
import com.superbugx.pinned.models.User;

public interface IUserService extends UserDetailsService  {
	/**
	 * Create a new User account according to business logic and store into database
	 * 
	 * @param newUser - User object with Email, Password, and UserName values not null
	 * @return new user ID
	 * @throws EmailNotUniqueException
	 * @throws UsernameNotUniqueException
	 * @throws BadPasswordException
	 * @throws BadEmailException
	 * @throws BadUserException
	 * @throws BadUsernameException
	 */
	public String register(User newUser) throws EmailNotUniqueException, UsernameNotUniqueException, BadUserException;
	
	/**
	 * Delete a user account from database
	 * 
	 * @param id - ID of user to be deleted
	 * @return boolean for success
	 */
	public boolean deleteById(String id);
	
	/**
	 * Get User from storage based on ID
	 * @param id - ID of user to be retrieved
	 * @return User
	 * @throws NoSuchElementException 
	 */
	public User getById(String id) throws NoSuchElementException;
}
