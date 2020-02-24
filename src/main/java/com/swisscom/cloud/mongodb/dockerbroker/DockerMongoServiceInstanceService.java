package com.swisscom.cloud.mongodb.dockerbroker;

import com.swisscom.cloud.mongodb.dockerbroker.docker.DockerService;
import com.swisscom.cloud.mongodb.dockerbroker.persistence.DockerMongoServiceEntity;
import com.swisscom.cloud.mongodb.dockerbroker.persistence.DockerMongoServiceRepository;
import com.swisscom.cloud.mongodb.dockerbroker.persistence.LastOperationEntity;
import com.swisscom.cloud.mongodb.dockerbroker.persistence.LastOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.model.instance.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class DockerMongoServiceInstanceService implements ServiceInstanceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerMongoServiceInstanceService.class);

    public static final String imageName = "mongo";

    @Autowired
    DockerMongoServiceRepository repository;

    @Autowired
    DockerService dockerService;

    @Autowired
    LastOperationRepository lastOperationRepository;

    @Override
    public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {

        String serviceId = request.getServiceInstanceId();
        Boolean isExists = repository.existsById(serviceId).block();

        if (isExists) {
            return Mono.just(CreateServiceInstanceResponse.builder().instanceExisted(true).build());
        } else {
            new Thread(() -> {
                LOGGER.debug("started hickity-hackity thread");
                dockerService.createInstanceFromImage(imageName)
                    .flatMap(meta -> repository.insert(new DockerMongoServiceEntity(serviceId, meta)))
                    .then(lastOperationRepository.save(new LastOperationEntity(UUID.fromString(serviceId), OperationState.SUCCEEDED))).block();
            }).start();

            return lastOperationRepository.save(new LastOperationEntity(UUID.fromString(serviceId), OperationState.IN_PROGRESS))
                .flatMap(o -> Mono.just(CreateServiceInstanceResponse.builder().operation(OperationState.IN_PROGRESS.toString()).build()));

        }

    }

    @Override
    public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {

        String serviceId = request.getServiceInstanceId();
        Boolean isExists = repository.existsById(serviceId).block();

        if (isExists) {
            new Thread(() -> {
                LOGGER.debug("started hickity-hackity thread");
                repository.findById(serviceId)
                    .flatMap(entity -> dockerService.deleteInstance(entity.getMeta()))
                    .then(lastOperationRepository.save(new LastOperationEntity(UUID.fromString(serviceId), OperationState.SUCCEEDED))).block();
            }).start();

            return lastOperationRepository.save(new LastOperationEntity(UUID.fromString(serviceId), OperationState.IN_PROGRESS))
                .flatMap(o -> Mono.just(DeleteServiceInstanceResponse.builder().operation(OperationState.IN_PROGRESS.toString()).build()));
        } else {
            return Mono.error(new RuntimeException("Service does not exist.")); //could be more specific
        }

    }

    @Override
    public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
        return Mono.just(request.getServiceInstanceId())
            .flatMap(serviceId -> Mono.just(GetLastServiceOperationResponse.builder())
                .flatMap(responseBuilder -> lastOperationRepository.existsById(UUID.fromString(serviceId))
                    .flatMap(exists -> {
                            if (exists) {
                                return lastOperationRepository.findById(UUID.fromString(serviceId))
                                    .flatMap(entity -> Mono.just(responseBuilder.operationState(entity.getState()).build()));
                            } else {
                                return Mono.error(new RuntimeException("Service does not exist."));
                            }
                        }
                    )
                )
            );
    }
}