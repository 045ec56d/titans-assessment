package com.swisscom.cloud.mongodb.dockerbroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DockerBrokerApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(DockerBrokerApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(DockerBrokerApplication.class, args);
	}

}
