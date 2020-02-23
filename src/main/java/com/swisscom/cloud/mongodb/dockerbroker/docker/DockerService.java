package com.swisscom.cloud.mongodb.dockerbroker.docker;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The intention of this component is to create a reactive service on top of the docker-java client.
 *
 */
@Component
public class DockerService extends DockerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerService.class);

    private static final Set<String> PREDEFINED_NETWORK_NAMES = new HashSet<>(5);

    static {
        PREDEFINED_NETWORK_NAMES.add("bridge");
        PREDEFINED_NETWORK_NAMES.add("host");
        PREDEFINED_NETWORK_NAMES.add("overlay");
        PREDEFINED_NETWORK_NAMES.add("macvlan");
        PREDEFINED_NETWORK_NAMES.add("none");
    }

    public final Mono<DockerMeta> createInstanceFromImage(String imageName) {
        LOGGER.info("creating docker container from image [{}]", imageName);

        String containerId = dockerClient.createContainerCmd(imageName).exec().getId();
        dockerClient.startContainerCmd(containerId).exec();

        List<String> ids = new ArrayList<>();
        ids.add(containerId);
        List<Container> containers = dockerClient.listContainersCmd().withIdFilter(ids).exec();

        if ( containers.isEmpty() ) return Mono.error(new RuntimeException("container not started"));
        else {
            Map<String, ContainerNetwork> networks = containers.get(0).getNetworkSettings().getNetworks();
            Set<String> networkNames = networks.keySet()
                .stream()
                .filter(name -> !PREDEFINED_NETWORK_NAMES.contains(name))
                .collect(Collectors.toSet());
            Set<String> networkIds = networkNames
                .stream()
                .map(name -> networks.get(name).getNetworkID())
                .collect(Collectors.toSet());
            return Mono.just(createDockerMeta(containerId, networkIds));
        }
    }

    public final Mono<Void> deleteInstance(DockerMeta meta) {

        deleteContainer(meta.getContainerId());
        meta.getNetworkIds().forEach(id -> deleteNetwork(id));
        meta.getVolumeId().ifPresent(id -> deleteVolume(id));

        return Mono.empty();

    }

    public final Mono<Void> deleteContainer(String containerId) {
        LOGGER.info("deleting container with id [{}]", containerId);

        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();

        return Mono.empty();
    }

    public final Mono<Void> deleteNetwork(String id) {
        dockerClient.removeNetworkCmd(id).exec();

        return Mono.empty();
    }

    public final Mono<Void> deleteVolume(String id) {
        dockerClient.removeVolumeCmd(id).exec();

        return Mono.empty();
    }

    private DockerMeta createDockerMeta(String containerId, Set<String> networkIds) {
        return DockerMeta.builder().withContainerId(containerId).withNetwoirkIds(networkIds).build();
    }

}
