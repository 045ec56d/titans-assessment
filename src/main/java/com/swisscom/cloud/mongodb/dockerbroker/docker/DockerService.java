package com.swisscom.cloud.mongodb.dockerbroker.docker;

import com.github.dockerjava.api.model.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class DockerService extends DockerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerService.class);

    public final Mono<DockerServiceInstance> createInstanceFromImage(String imageName) {
        LOGGER.info("creating docker container from image [{}]", imageName);

        String containerId = dockerClient.createContainerCmd(imageName).exec().getId();
        dockerClient.startContainerCmd(containerId).exec();

        List<String> ids = new ArrayList<>();
        ids.add(containerId);
        List<Container> containers = dockerClient.listContainersCmd().withIdFilter(ids).exec();

        if ( containers.isEmpty() ) return Mono.error(new RuntimeException("container not started"));
        else {
            Set<String> networkIds = containers.get(0).getNetworkSettings().getNetworks().keySet();
            DockerMeta meta = createDockerMeta(containerId, networkIds);
            return Mono.just(new DockerServiceInstance(meta));
        }

    }

    public final Mono<Void> deleteContainer(String containerId) {
        LOGGER.info("deleting container with id [{}]", containerId);

        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();

        return Mono.empty();
    }

    private DockerMeta createDockerMeta(String containerId, Set<String> networkIds) {
        return DockerMeta.builder().withContainerId(containerId).withNetwoirkIds(networkIds).build();
    }


}
