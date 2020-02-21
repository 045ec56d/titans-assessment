package com.swisscom.cloud.mongodb.dockerbroker.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface LastOperationRepository extends MongoRepository<LastOperationEntity, UUID> {
}
