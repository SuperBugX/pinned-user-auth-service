package com.superbugx.pinned.database.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "users")
public class UserDAO {
	//Attributes
	@Id
	private String id;
	private String password;
	private String email;
	private String username;
	private String[] roles;
}
