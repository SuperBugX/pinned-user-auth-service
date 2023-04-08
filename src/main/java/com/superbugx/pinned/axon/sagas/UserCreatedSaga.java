package com.superbugx.pinned.axon.sagas;

import java.util.Collection;
import java.util.stream.Collectors;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.superbugx.pinned.axon.commands.CreateTransmitterCommand;
import com.superbugx.pinned.axon.events.TransmitterCreatedEvent;
import com.superbugx.pinned.axon.events.UserCreatedEvent;
import com.superbugx.pinned.database.repository.UserRepository;
import com.superbugx.pinned.models.User;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Saga
public class UserCreatedSaga {
	// Attributes
	@Inject
	private transient CommandGateway commandGateway;
	@Autowired
	private transient UserRepository userRepo;

	// Saga Transaction Values
	private String transmitterId;
	private User user;

	@StartSaga
	@SagaEventHandler(associationProperty = "userId")
	public void handle(UserCreatedEvent event) {
		// Perform List Conversion
		Collection<? extends GrantedAuthority> authorities;
		authorities = event.getAuthorities().stream().map(authority -> {
			return new SimpleGrantedAuthority(authority);
		}).collect(Collectors.toList());

		// Create User
		user = User.builder().id(event.getUserId()).email(event.getEmail()).password(event.getPassword())
				.username(event.getUsername()).authorities(authorities).build();

		// Create new ID for transmitter
		transmitterId = event.getUserId();
		// Saga Associations
		SagaLifecycle.associateWith("transmitterId", transmitterId);
		// Send Commands
		commandGateway.send(new CreateTransmitterCommand(transmitterId));
	}

	@EndSaga
	@SagaEventHandler(associationProperty = "transmitterId")
	public void handle(TransmitterCreatedEvent event) {
		// Success
		// Update Query View
		userRepo.save(user);
		log.debug("User Saved to Read Model: " + user.getId());
		SagaLifecycle.end();
	}
}
