package com.superbugx.pinned.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.Data;

@Document
@Data
public class RefreshToken {
	@Id
	private String id;
	@DocumentReference(lazy = true)
	private User owner;
}
