package com.swisscom.cloud.mongodb.dockerbroker

import org.junit.ClassRule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers
import spock.lang.Specification

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
abstract class BaseSpecification extends Specification {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseSpecification.class)

    public static boolean inited = false

    protected final static Map<String, String> SIMPLE_DB_PLAN = [service_id: "f8e0bb08-bd8d-4899-9058-28ef2a3e2701",
                                                                       plan_id   : "ed1bda66-bc7b-495c-a8c2-3740217085be"]
    protected final static Map<String, String> DB_WITH_ADMIN_PLAN = [service_id: "f8e0bb08-bd8d-4899-9058-28ef2a3e2701",
                                                                         plan_id   : "c1736c2a-44fb-48e5-b3c5-49f0c1c53baa"]

    @Autowired
    Environment springEnv

    protected WebTestClient webTestClient

    @LocalServerPort
    protected int port

    protected String getBaseUrl() {
        "http://localhost:" + port.toString()
    }

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(new File("docker/docker-compose.yml"))
                    .withExposedService("mongo",
                            27017,
                            Wait.forLogMessage(".*waiting for connections on port 27017.*", 2))
                    .withLogConsumer("mongo", new Slf4jLogConsumer(LOGGER))
                    .withExposedService("rabbitmq", 5672)
                    .withLogConsumer("rabbitmq", new Slf4jLogConsumer(LOGGER))

    def setupSpec() {
        if (!inited) {
            environment.start()
            LOGGER.info("Docker compose services started")
            inited = true
        }
    }
}
