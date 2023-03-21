package com.superbugx.pinned.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import com.superbugx.pinned.converters.GrantedAuthorityToString;
import com.superbugx.pinned.converters.StringToGrantedAuthority;

@Configuration
public class MongoDBConfig {

	@Bean
	/*
	 * This Bean configures MongoDB to convert specific object to other objects via 
	 * developer defined converter classes. In this case, to prevent tight coupling
	 * MongoDB will now convert SpringSecurity GrantedAuthority objects to Strings
	 * and vice versa. As a result even if SpringBoot is not used, the data remains
	 * platform agnostic in data structure.
	 */
	public MongoCustomConversions mongoCustomConversions() {
		return new MongoCustomConversions(
				Arrays.asList(new GrantedAuthorityToString(), new StringToGrantedAuthority()));
	}
}
