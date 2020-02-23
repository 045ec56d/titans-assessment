package com.swisscom.cloud.mongodb.dockerbroker.persistence;

import org.springframework.cloud.servicebroker.model.instance.OperationState;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Document(collection = "lastOperations")
public class LastOperationEntity {
    @Id
    @NotNull
    private UUID id;

    @NotNull
    private OperationState state;

    public LastOperationEntity(@NotNull UUID id, @NotNull OperationState state) {
        this.id = id;
        this.state = state;
    }

    public OperationState getState() { return this.state; }
}
