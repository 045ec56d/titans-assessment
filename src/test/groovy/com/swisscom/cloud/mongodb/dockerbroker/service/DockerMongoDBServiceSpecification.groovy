package com.swisscom.cloud.mongodb.dockerbroker.service

import com.github.dockerjava.api.DockerClient
import com.swisscom.cloud.mongodb.dockerbroker.BaseSpecification
import com.swisscom.cloud.mongodb.dockerbroker.DockerMongoServiceInstanceService
import com.swisscom.cloud.mongodb.dockerbroker.docker.DockerController
import com.swisscom.cloud.mongodb.dockerbroker.persistence.DockerMongoServiceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationRequest
import org.springframework.cloud.servicebroker.model.instance.OperationState
import org.springframework.test.context.ActiveProfiles

import static org.assertj.core.api.Assertions.assertThat

@ActiveProfiles("test")
class DockerMongoDBServiceSpecification extends BaseSpecification {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerMongoDBServiceSpecification.class)

    @Autowired
    DockerMongoServiceInstanceService dockerMongoService;

    @Autowired
    DockerMongoServiceRepository repository;

    @Autowired
    DockerController dockerController;

    void setup() { }

    void 'should deprovision the requested service only'() {
        given:
        String id = UUID.randomUUID()
        String containerId = createServiceContainer(id)

        when:
        DeleteServiceInstanceRequest deleteReq = DeleteServiceInstanceRequest.builder().serviceInstanceId(id).build()
        DeleteServiceInstanceResponse resp = dockerMongoService.deleteServiceInstance(deleteReq).block()

        waitUntilSucceeded(id)


        then:
        List<String> ids = new ArrayList<String>()
        ids.add(containerId)
        int containersFound = dockerController.getClient().listContainersCmd().withIdFilter(ids).exec().size()
        LOGGER.info("found containers: {}", containersFound)
        assertThat(containersFound == 0).isTrue()

    }


    void 'should be able to provision multiple instances'() {
        given:

        String id1 = UUID.randomUUID()
        String containerId1 = createServiceContainer(id1)

        when:
        String id2 = UUID.randomUUID()
        String containerId2 = createServiceContainer(id2)

        then:
        assertThat(containerId1 != containerId2).isTrue()

        cleanup:
        removeContainer(containerId1)
        removeContainer(containerId2)
    }

    void 'should return the existing instance when trying to provision the same service twice'() {
        given:
        String id1 = UUID.randomUUID()
        String containerId1 = createServiceContainer(id1)

        when:
        String containerId2 = createServiceContainer(id1)

        then:
        assertThat(containerId1 == containerId2).isTrue()

        cleanup:
        removeContainer(containerId1)
    }

    def removeContainer(String id) {
        DockerClient client = dockerController.getClient()
        client.stopContainerCmd(id).exec()
        client.removeContainerCmd(id).exec()
        // remove networks
        // remove volumes
    }


    CreateServiceInstanceRequest createServiceInstanceRequest(String id) {
        CreateServiceInstanceRequest
                .builder()
                .serviceInstanceId(id)
                .build()
    }

    String createServiceContainer(String serviceInstanceId) {
        CreateServiceInstanceRequest createReq = createServiceInstanceRequest(serviceInstanceId)
        CreateServiceInstanceResponse resp = dockerMongoService.createServiceInstance(createReq).block()

        waitUntilSucceeded(serviceInstanceId)

        return repository.findById(serviceInstanceId).block().getMeta().getContainerId() //no composability needed here
    }

    def waitUntilSucceeded(String serviceId) {
        GetLastServiceOperationRequest lastOpReq = GetLastServiceOperationRequest.builder().serviceInstanceId(serviceId).build()

        int counter = 0
        while (dockerMongoService.getLastOperation(lastOpReq).block().state != OperationState.SUCCEEDED || counter > 100) { //needed to get the actual container ID
            Thread.sleep(250)
            LOGGER.debug("service instance [{}] not ready yet", serviceId)
            counter++
        }

    }

}
