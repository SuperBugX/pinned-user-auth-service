package com.superbugx.pinned.axon.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import com.superbugx.pinned.models.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CreateUserCommand {
	// Attributes
	@TargetAggregateIdentifier
	private final String userId;
	private final String password;
	private final String email;
	private final String username;
	private final List<String> authorities;

	public CreateUserCommand(User user) {
		this.userId = user.getId();
		this.password = user.getPassword();
		this.email = user.getEmail();
		this.username = user.getUsername();
		this.authorities = user.getAuthorities().stream().map(authority -> {
			return authority.getAuthority().toString();
		}).collect(Collectors.toList());
	}
}
