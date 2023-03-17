package com.superbugx.pinned.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
	//POJO represents a single user of the system, mimics database entity
	//Attributes
	private String id;
	private String password;
	private String email;
	private String username;
	private String[] roles;
}
