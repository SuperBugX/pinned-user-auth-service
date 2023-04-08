package com.superbugx.pinned.axon.projections;

import java.util.Optional;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.superbugx.pinned.axon.queries.GetUserByEmailOrUsernameQuery;
import com.superbugx.pinned.axon.queries.GetUserByEmailQuery;
import com.superbugx.pinned.axon.queries.GetUserByIdQuery;
import com.superbugx.pinned.axon.queries.GetUserByUsernameQuery;
import com.superbugx.pinned.database.repository.UserRepository;
import com.superbugx.pinned.models.User;

@Component
public class UserProjection {
	// Attributes
	@Autowired
	private UserRepository userRepo;

	@QueryHandler
	public Optional<User> handle(GetUserByIdQuery query) {
		return userRepo.findById(query.getId());
	}

	@QueryHandler
	public Optional<User> handle(GetUserByEmailOrUsernameQuery query) {
		return userRepo.findByEmailOrUsername(query.getName());
	}

	@QueryHandler
	public Optional<User> handle(GetUserByEmailQuery query) {
		return userRepo.findByEmail(query.getEmail());
	}

	@QueryHandler
	public Optional<User> handle(GetUserByUsernameQuery query) {
		return userRepo.findByUsername(query.getUsername());
	}
}
