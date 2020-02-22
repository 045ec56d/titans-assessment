package com.swisscom.cloud.mongodb.dockerbroker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.swisscom.cloud.mongodb.dockerbroker.persistence.DockerMongoServiceEntity;
import com.swisscom.cloud.mongodb.dockerbroker.persistence.DockerMongoServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.instance.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DockerMongoServiceInstanceService implements ServiceInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerMongoServiceInstanceService.class);

    @Autowired
    DockerMongoServiceRepository repository;

    private DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost("unix:///var/run/docker.sock")
            .build();

    private DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

    @Override
    public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
        // TODO: implement handling of create/provision


        CreateContainerResponse container = dockerClient.createContainerCmd("mongo").exec();
        dockerClient.startContainerCmd(container.getId()).exec();

        boolean isExisted = repository.existsById(request.getServiceInstanceId());
        repository.insert(new DockerMongoServiceEntity(request.getServiceInstanceId(), container.getId()));

        CreateServiceInstanceResponse resp =
                CreateServiceInstanceResponse
                        .builder()
                        .async(true)
                        .instanceExisted(isExisted)
                        .build();

        return Mono.just(resp);
    }

    @Override
    public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
        // TODO: implement handling of delete/deprovision

        String serviceInstanceId = request.getServiceInstanceId();

        if (repository.existsById(request.getServiceInstanceId())) {
            String containerId = repository.findById(serviceInstanceId).get().getContainerId();
            try {
                dockerClient.stopContainerCmd(containerId).exec();
                return Mono.just(DeleteServiceInstanceResponse.builder().build());
            } catch (NotFoundException ex) {
                return Mono.error(new RuntimeException("service instance not found."));
            } catch (NotModifiedException ex) {
                return Mono.error(new RuntimeException("There was an error while stopping the service"));
            }
        }

        return Mono.error(new RuntimeException("service instance not found."));
    }

    @Override
    public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
        // TODO: implement status check of provision/deprovision

        return Mono.just(GetLastServiceOperationResponse.builder().operationState(OperationState.SUCCEEDED).build());
    }
}
