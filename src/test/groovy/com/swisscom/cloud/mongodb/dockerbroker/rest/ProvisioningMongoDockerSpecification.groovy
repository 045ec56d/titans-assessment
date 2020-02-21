package com.swisscom.cloud.mongodb.dockerbroker.rest

import com.swisscom.cloud.mongodb.dockerbroker.BaseSpecification
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceAppBindingResponse
import org.springframework.cloud.servicebroker.model.binding.CreateServiceInstanceBindingRequest
import org.springframework.cloud.servicebroker.model.binding.DeleteServiceInstanceBindingResponse
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceRequest
import org.springframework.cloud.servicebroker.model.instance.CreateServiceInstanceResponse
import org.springframework.cloud.servicebroker.model.instance.GetLastServiceOperationResponse
import org.springframework.cloud.servicebroker.model.instance.OperationState
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Shared

import static java.time.Duration.ofMillis

class ProvisioningMongoDockerSpecification extends BaseSpecification {
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
        exchange.expectBody(CreateServiceInstanceResponse)
    }
}
