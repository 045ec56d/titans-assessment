package com.swisscom.cloud.mongodb.dockerbroker.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

public class DockerController {
    protected DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
        .withDockerHost("unix:///var/run/docker.sock")
        .build();

    protected DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

    public final DockerClient getClient() { return dockerClient; }
}
