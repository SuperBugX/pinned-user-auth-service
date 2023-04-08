package com.superbugx.pinned.config;

import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;

@Configuration
public class AxonConfig {
	@Bean
	@Primary
	public Serializer defaultSerializer() {
		// Set the secure types on the xStream instance
		XStream xStream = new XStream();
		xStream.addPermission(AnyTypePermission.ANY);
		return XStreamSerializer.builder().xStream(xStream).build();
	}
}
