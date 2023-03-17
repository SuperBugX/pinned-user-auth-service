package com.superbugx.pinned.database.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.superbugx.pinned.database.models.UserDAO;

@Repository
public interface UserRepository extends MongoRepository<UserDAO, String>{
	
	public UserDAO findByEmail(String email);
	public UserDAO findByUsername(String username);
}	
