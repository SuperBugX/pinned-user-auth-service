package com.superbugx.pinned.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;

public class GrantedAuthorityToString implements Converter<GrantedAuthority, String> {
	@Override
	public String convert(GrantedAuthority source) {
		// TODO Auto-generated method stub
		return source.getAuthority().toString();
	}
}
