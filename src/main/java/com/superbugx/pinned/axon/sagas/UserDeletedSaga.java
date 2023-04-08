package com.superbugx.pinned.axon.sagas;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import com.superbugx.pinned.axon.commands.DeleteTransmitterCommand;
import com.superbugx.pinned.axon.events.TransmitterDeletedEvent;
import com.superbugx.pinned.axon.events.UserDeletedEvent;
import com.superbugx.pinned.database.repository.UserRepository;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Saga
public class UserDeletedSaga {
	// Attributes
	@Inject
	private transient CommandGateway commandGateway;
	@Autowired
	private transient UserRepository userRepo;

	// Saga Transaction Values
	private String userId;

	@StartSaga
	@SagaEventHandler(associationProperty = "userId")
	public void on(UserDeletedEvent event) {
		// Remember User ID
		userId = event.getUserId();
		// Saga Associations
		SagaLifecycle.associateWith("transmitterId", event.getUserId());
		// Send Commands
		commandGateway.send(new DeleteTransmitterCommand(event.getUserId()));
	}

	@EndSaga
	@SagaEventHandler(associationProperty = "transmitterId")
	public void handle(TransmitterDeletedEvent event) {
		// Success
		// Update Query View
		userRepo.deleteById(userId);
		log.debug("User Deleted From Read Model: " + userId);
		SagaLifecycle.end();
	}
}
