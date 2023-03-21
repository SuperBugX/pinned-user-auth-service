package com.superbugx.pinned.dto.requests;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
public class LoginDTO {
	// Attributes
	@NonNull
	private String username;
	@NonNull
	private String password;
}
