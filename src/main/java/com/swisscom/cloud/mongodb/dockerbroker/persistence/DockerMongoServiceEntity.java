package com.swisscom.cloud.mongodb.dockerbroker.persistence;

import com.swisscom.cloud.mongodb.dockerbroker.docker.DockerMeta;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document(collection = "services")
public class DockerMongoServiceEntity {

    @Id
    @NotNull
    String serviceId;
    @NotNull
    DockerMeta meta;

    public DockerMongoServiceEntity(@NotNull String serviceId, @NotNull DockerMeta meta) {
        this.serviceId = serviceId;
        this.meta = meta;
    }

    public String getServiceId() {
        return serviceId;
    }

    public DockerMeta getMeta() {
        return meta;
    }

}
