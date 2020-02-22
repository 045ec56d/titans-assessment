package com.swisscom.cloud.mongodb.dockerbroker.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document(collection = "services")
public class DockerMongoServiceEntity {

    @Id
    @NotNull
    String serviceId;
    @NotNull
    String containerId;

    public DockerMongoServiceEntity(@NotNull String serviceId, @NotNull String containerId) {
        this.serviceId = serviceId;
        this.containerId = containerId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
