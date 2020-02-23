package com.swisscom.cloud.mongodb.dockerbroker.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

public interface LastOperationRepository extends ReactiveMongoRepository<LastOperationEntity, UUID> {
}
