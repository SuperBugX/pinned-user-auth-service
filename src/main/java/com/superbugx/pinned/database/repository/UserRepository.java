package com.superbugx.pinned.database.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.superbugx.pinned.models.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	public Optional<User> findByEmail(String email);
	public Optional<User> findByUsername(String username);
	@Query("{ $or: [ { 'username': ?0 }, { 'email': ?0 } ] }")
	public Optional<User> findByEmailOrUsername(String name);
}
