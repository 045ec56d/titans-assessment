package com.swisscom.cloud.mongodb.dockerbroker;

import com.swisscom.cloud.mongodb.dockerbroker.docker.DockerMongoDBServiceCreator;
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

    @Autowired
    DockerMongoDBServiceCreator mongoDBServiceCreator;

    @Override
    public Mono<CreateServiceInstanceResponse> createServiceInstance(CreateServiceInstanceRequest request) {

        return Mono.just(request.getServiceInstanceId())
            .flatMap (
                serviceId -> mongoDBServiceCreator.createInstance()
                    .flatMap(instance -> {
                        String containerId = instance.getDockerMeta().getContainerId();
                        LOGGER.info("mongo db service instance created with ID: [{}], containerId: [{}]", serviceId, containerId);
                        DockerMongoServiceEntity entity = new DockerMongoServiceEntity(serviceId, containerId);
                        return Mono.just(repository.existsById(serviceId)).flatMap(exists -> {
                            if (exists) return Mono.just(serviceAlreadyExistedResponse);
                            else return Mono.just(repository.insert(entity)).flatMap(e -> Mono.just(serviceCreatedResponse));
                        });
                    })
            );

    }

    private CreateServiceInstanceResponse serviceAlreadyExistedResponse = CreateServiceInstanceResponse
        .builder()
        .async(true)
        .instanceExisted(true)
        .build();

    private CreateServiceInstanceResponse serviceCreatedResponse = CreateServiceInstanceResponse
        .builder()
        .async(true)
        .instanceExisted(false)
        .build();



    @Override
    public Mono<DeleteServiceInstanceResponse> deleteServiceInstance(DeleteServiceInstanceRequest request) {
        // TODO: implement handling of delete/deprovision
//
//        String serviceInstanceId = request.getServiceInstanceId();
//
//        if (repository.existsById(request.getServiceInstanceId())) {
//            String containerId = repository.findById(serviceInstanceId).get().getContainerId();
//            try {
//                dockerClient.stopContainerCmd(containerId).exec();
//                return Mono.just(DeleteServiceInstanceResponse.builder().build());
//            } catch (NotFoundException ex) {
//                return Mono.error(new RuntimeException("service instance not found."));
//            } catch (NotModifiedException ex) {
//                return Mono.error(new RuntimeException("There was an error while stopping the service"));
//            }
//        }
//
//        return Mono.error(new RuntimeException("service instance not found."));
//

        return Mono.just(DeleteServiceInstanceResponse.builder().build());
    }

    @Override
    public Mono<GetLastServiceOperationResponse> getLastOperation(GetLastServiceOperationRequest request) {
        // TODO: implement status check of provision/deprovision

        return Mono.just(GetLastServiceOperationResponse.builder().operationState(OperationState.SUCCEEDED).build());
    }
}
