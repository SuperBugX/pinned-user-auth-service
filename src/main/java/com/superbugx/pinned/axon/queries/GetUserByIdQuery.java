package com.superbugx.pinned.axon.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class GetUserByIdQuery {
	//Attributes
	private final String id;
}
