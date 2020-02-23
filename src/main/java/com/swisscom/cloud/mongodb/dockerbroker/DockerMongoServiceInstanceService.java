package com.swisscom.cloud.mongodb.dockerbroker;

import com.swisscom.cloud.mongodb.dockerbroker.docker.DockerService;
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

    public static final String imageName = "mongo";

    @Autowired
    DockerMongoServiceRepository repository;

    @Autowired
    DockerService dockerService;

    @Override
    public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {

        return Mono.just(request.getServiceInstanceId())
            .flatMap(serviceId -> Mono.just(CreateServiceInstanceResponse.builder())
                .flatMap(responseBuilder -> repository.existsById(serviceId)
                    .flatMap(exists -> {
                        if (exists)
                            return Mono.just(responseBuilder.instanceExisted(true).build());
                        else {
                            return dockerService.createInstanceFromImage(imageName)
                                .flatMap(meta -> {
                                    String containerId = meta.getContainerId();
                                    LOGGER.info("mongo db service instance created with ID: [{}], containerId: [{}]", serviceId, containerId);
                                    DockerMongoServiceEntity entity = new DockerMongoServiceEntity(serviceId, meta);
                                    return repository.insert(entity);
                                }).thenReturn(responseBuilder.build());
                        }
                    })
                )
            );
    }

    @Override
    public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {

        return Mono.just(request.getServiceInstanceId())
            .flatMap(serviceId -> Mono.just(DeleteServiceInstanceResponse.builder())
                .flatMap(responseBuilder -> repository.existsById(serviceId)
                    .flatMap(exists -> {
                            if (exists) {
                                return repository.findById(serviceId)
                                    .flatMap(entity -> dockerService.deleteInstance(entity.getMeta()))
                                    .thenReturn(responseBuilder.build());
                                //.onErrorMap(throwable -> awesomeErrorHandler);
                            } else {
                                return Mono.error(new RuntimeException("Service does not exist.")); //could be more specific
                            }
                        }
                    )
                )
            );

    }

    @Override
    public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
        // TODO: implement status check of provision/deprovision

        return Mono.just(GetLastServiceOperationResponse.builder().operationState(OperationState.SUCCEEDED).build());
    }
}
