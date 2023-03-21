package com.superbugx.pinned.enums;

public enum Role {
	//User Roles
	USER("USER"), ADMIN("ADMIN");

	public final String label;

	private Role(String label) {
		this.label = label;
	}
}
