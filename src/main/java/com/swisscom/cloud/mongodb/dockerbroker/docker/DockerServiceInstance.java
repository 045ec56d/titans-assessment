package com.swisscom.cloud.mongodb.dockerbroker.docker;

import org.springframework.lang.NonNull;

public class DockerServiceInstance {

    @NonNull
    private DockerMeta dockerMeta;

    public DockerServiceInstance(DockerMeta dm) {
        dockerMeta = dm;
    }

    public DockerMeta getDockerMeta() {
        return dockerMeta;
    }

}
