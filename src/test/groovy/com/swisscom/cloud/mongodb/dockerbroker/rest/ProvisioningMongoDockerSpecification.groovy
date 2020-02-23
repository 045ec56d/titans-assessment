package com.swisscom.cloud.mongodb.dockerbroker.rest

import com.swisscom.cloud.mongodb.dockerbroker.BaseSpecification
import groovy.json.JsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.servicebroker.model.instance.*
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Shared

import static java.time.Duration.ofMillis

@ActiveProfiles("test")
class ProvisioningMongoDockerSpecification extends BaseSpecification {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisioningMongoDockerSpecification.class)

    static UUID serviceInstanceId
    static UUID serviceBindingId

    @Shared
    WebTestClient webTestClient

    def setupSpec() {
        serviceInstanceId = UUID.randomUUID()
        serviceBindingId = UUID.randomUUID()
    }

    void setup() {
        webTestClient = WebTestClient.bindToServer().
                responseTimeout(ofMillis(36000)).
                baseUrl(getBaseUrl()).
                defaultHeaders({
                    header ->
                        header.setContentType(MediaType.APPLICATION_JSON)
                        header.setAccept([MediaType.APPLICATION_JSON])
                }).
                build()
    }

    void 'should return 500 if trying to delete a non-existent service'() {

        when:
        def exchange = webTestClient.delete()
                .uri(String.format("/v2/service_instances/%s?service_id=%s&plan_id=%s", serviceInstanceId, this.SIMPLE_DB_PLAN["service_id"], this.SIMPLE_DB_PLAN["plan_id"]))
                .exchange()

        then:
        exchange.expectStatus().is5xxServerError() //actually this could be something more meaningful.
    }

    void 'should successfully start provisioning a mongodb service'() {
        given:
        def provisionRequest = CreateServiceInstanceRequest
                .builder()
                .serviceInstanceId(serviceInstanceId.toString())
                .serviceDefinitionId(this.SIMPLE_DB_PLAN["service_id"])
                .planId(this.SIMPLE_DB_PLAN["plan_id"])
                .asyncAccepted(true)
                .parameters("mongodb_port", "27017")
                .build()

        LOGGER.info("sending request:\n {}", new JsonBuilder(provisionRequest).toPrettyString())

        when:
        def exchange = webTestClient.put()
                .uri(String.format("/v2/service_instances/%s", serviceInstanceId))
                .bodyValue(provisionRequest)
                .exchange()

        then:
        exchange.expectStatus().is2xxSuccessful()
        exchange.expectBody(CreateServiceInstanceResponse)
    }

    void 'should wait for lastOperation to finish'() {
        given:
        GetLastServiceOperationResponse lastServiceOperationResponse

        when:
        for (int i = 0; i <= 100; i++) {
                def exchange = webTestClient.get()
                        .uri(String.format("/v2/service_instances/%s/last_operation", serviceInstanceId))
                        .exchange()

                exchange.expectStatus().is2xxSuccessful()
                lastServiceOperationResponse = exchange.expectBody(GetLastServiceOperationResponse).returnResult().responseBody

                if (lastServiceOperationResponse.state != OperationState.IN_PROGRESS) {
                    break
                } else {
                    LOGGER.info("last operation was [{}]", lastServiceOperationResponse.state)
                }

            Thread.sleep(250)
        }

        then:
        noExceptionThrown()
        lastServiceOperationResponse.state != OperationState.IN_PROGRESS
    }

    void 'should successfully start deprovisioning a mongodb service'() {

        when:
        def exchange = webTestClient.delete()
                .uri(String.format("/v2/service_instances/%s?service_id=%s&plan_id=%s", serviceInstanceId, this.SIMPLE_DB_PLAN["service_id"], this.SIMPLE_DB_PLAN["plan_id"]))
                .exchange()

        then:
        exchange.expectStatus().is2xxSuccessful()
        exchange.expectBody(DeleteServiceInstanceResponse)
    }
}
