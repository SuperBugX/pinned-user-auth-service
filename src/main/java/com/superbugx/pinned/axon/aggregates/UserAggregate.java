package com.superbugx.pinned.axon.aggregates;

import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

import java.util.List;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import com.superbugx.pinned.axon.commands.CreateUserCommand;
import com.superbugx.pinned.axon.commands.DeleteUserCommand;
import com.superbugx.pinned.axon.events.UserCreatedEvent;
import com.superbugx.pinned.axon.events.UserDeletedEvent;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aggregate
@Data
@NoArgsConstructor
public class UserAggregate {
	// 1:1 Representation of User Model
	// Used for event sourcing and SAGA Management
	// Attributes
	@AggregateIdentifier
	private String id;
	private String password;
	private String email;
	private String username;
	private List<String> authorities;

	// Commands
	@CommandHandler
	public UserAggregate(CreateUserCommand command) {
		log.debug("Create User Command: " + command.getUserId());
		AggregateLifecycle.apply(new UserCreatedEvent(command));
	}

	@CommandHandler
	public void handle(DeleteUserCommand command) {
		log.debug("Delete User Command: " + command.getUserId());
		AggregateLifecycle.apply(new UserDeletedEvent(command.getUserId()));
	}

	// Events
	@EventSourcingHandler
	public void on(UserCreatedEvent event) {
		log.info("Create User Event: " + event.getUserId());
		this.id = event.getUserId();
		this.email = event.getEmail();
		this.password = event.getPassword();
		this.username = event.getUsername();
		this.authorities = event.getAuthorities();
	}

	@EventSourcingHandler
	public void on(UserDeletedEvent event) {
		log.debug("Delete User Event: " + event.getUserId());
		this.id = event.getUserId();
		markDeleted();
	}
}
