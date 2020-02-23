package com.swisscom.cloud.mongodb.dockerbroker.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DockerMongoServiceRepository extends ReactiveMongoRepository<DockerMongoServiceEntity, String> {}
