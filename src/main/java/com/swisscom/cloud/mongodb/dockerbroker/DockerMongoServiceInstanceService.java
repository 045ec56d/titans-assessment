package com.swisscom.cloud.mongodb.dockerbroker;

import org.springframework.cloud.servicebroker.model.instance.*;
import org.springframework.cloud.servicebroker.service.ServiceInstanceService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DockerMongoServiceInstanceService implements ServiceInstanceService {
    @Override
    public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {
        // TODO: implement handling of create/provision

        return Mono.just(CreateServiceInstanceResponse.builder().build());
    }

    @Override
    public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
        // TODO: implement handling of delete/deprovision

        return Mono.just(DeleteServiceInstanceResponse.builder().build());
    }

    @Override
    public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
        // TODO: implement status check of provision/deprovision

        return Mono.just(GetLastServiceOperationResponse.builder().operationState(OperationState.SUCCEEDED).build());
    }
}
