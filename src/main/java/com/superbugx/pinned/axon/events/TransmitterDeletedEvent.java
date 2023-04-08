package com.superbugx.pinned.axon.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TransmitterDeletedEvent {
	// Attributes
	private final String transmitterId;
}
