package com.swisscom.cloud.mongodb.dockerbroker.service

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import com.swisscom.cloud.mongodb.dockerbroker.BaseSpecification
import com.swisscom.cloud.mongodb.dockerbroker.DockerMongoServiceInstanceService
import com.swisscom.cloud.mongodb.dockerbroker.persistence.DockerMongoServiceRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.instance.DeleteServiceInstanceRequest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
class DockerMongoDBServiceSpecification extends BaseSpecification {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerMongoDBServiceSpecification.class)

    @Autowired
    DockerMongoServiceInstanceService dockerMongoService;

    @Autowired
    DockerMongoServiceRepository repository;

    DockerClientConfig config;

    DockerClient dockerClient;

    void setup() {
        config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        dockerClient = DockerClientBuilder.getInstance(config).build();
    }

    void 'should deprovision the requested service only'() {
        given:
        String id = UUID.randomUUID()
        CreateServiceInstanceRequest createReq = CreateServiceInstanceRequest
                .builder()
                .serviceInstanceId(id)
                .build()
        dockerMongoService.createServiceInstance(createReq)
        String containerId = repository.findById(id).get().getContainerId()

        when:
        List<String> ids = new ArrayList<String>()
        ids.add(containerId)
        DeleteServiceInstanceRequest deleteReq = DeleteServiceInstanceRequest
                .builder()
                .serviceInstanceId(id)
                .build()


        then:
        StepVerifier.create(dockerMongoService.deleteServiceInstance(deleteReq))
                .consumeNextWith { response ->
                    assertThat(dockerClient.listContainersCmd().withIdFilter(ids).exec().size() == 0)
                }

    }
}
