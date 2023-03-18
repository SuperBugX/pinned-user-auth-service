package com.superbugx.pinned.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class StringToGrantedAuthority implements Converter<String, GrantedAuthority> {
	@Override
	public GrantedAuthority convert(String source) {
		// TODO Auto-generated method stub
		return new SimpleGrantedAuthority(source);
	}
}
