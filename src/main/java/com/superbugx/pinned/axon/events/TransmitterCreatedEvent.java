package com.superbugx.pinned.axon.events;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TransmitterCreatedEvent {
	// Attributes
	private final String transmitterId;
	private final List<String> trackers;
}
