package com.superbugx.pinned.axon.events;

import java.util.List;

import com.superbugx.pinned.axon.commands.CreateUserCommand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserCreatedEvent {
	// Attributes
	private final String userId;
	private final String password;
	private final String email;
	private final String username;
	private final List<String> authorities;

	public UserCreatedEvent(CreateUserCommand command) {
		this.userId = command.getUserId();
		this.password = command.getPassword();
		this.email = command.getEmail();
		this.username = command.getUsername();
		this.authorities = command.getAuthorities();
	}
}
