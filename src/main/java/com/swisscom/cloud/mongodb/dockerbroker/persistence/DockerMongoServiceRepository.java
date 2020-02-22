package com.swisscom.cloud.mongodb.dockerbroker.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DockerMongoServiceRepository extends MongoRepository<DockerMongoServiceEntity, String> {}
