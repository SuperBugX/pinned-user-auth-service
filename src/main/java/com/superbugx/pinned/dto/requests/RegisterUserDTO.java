package com.superbugx.pinned.dto.requests;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDTO {
	//Attributes
	@NonNull
	private String username;
	@NonNull
	private String email;
	@NonNull
	private String password;
}
