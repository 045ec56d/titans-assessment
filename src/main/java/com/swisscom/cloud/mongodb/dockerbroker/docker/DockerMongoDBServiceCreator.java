package com.swisscom.cloud.mongodb.dockerbroker.docker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DockerMongoDBServiceCreator {

    private final String imageName = "mongo";

    @Autowired
    DockerService dockerService;

    public Mono<DockerServiceInstance> createInstance() {
        return dockerService.createInstanceFromImage(imageName);
    }

}
