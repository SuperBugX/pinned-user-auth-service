package com.superbugx.pinned.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TokenDTO {
	//Class Represents a Token Response
	@NonNull
    private String accessToken;
	@NonNull
    private String refreshToken;
}
