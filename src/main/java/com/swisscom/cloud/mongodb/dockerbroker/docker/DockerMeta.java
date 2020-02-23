package com.swisscom.cloud.mongodb.dockerbroker.docker;

import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class DockerMeta {

    @NonNull
    private String containerId;

    @NonNull
    private String imageId;

    @NonNull
    private Set<String> networkIds;

    @NonNull
    private Optional<String> volumeId;

    private DockerMeta(String containerId, String imageId, Set<String> networkIds, Optional<String> volumeId) {
        this.containerId = containerId;
        this.imageId = imageId;
        this.networkIds = networkIds;
        this.volumeId = volumeId;
    }

    public static DockerMetaBuilder builder() {
        return new DockerMetaBuilder();
    }

    @NonNull
    public String getContainerId() {
        return containerId;
    }

    @NonNull
    public String getImageId() {
        return imageId;
    }

    @NonNull
    public Set<String> getNetworkId() {
        return networkIds;
    }

    @NonNull
    public Optional<String> getVolumeId() {
        return volumeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DockerMeta that = (DockerMeta) o;
        return containerId.equals(that.containerId) &&
                imageId.equals(that.imageId) &&
                networkIds.equals(that.networkIds) &&
                volumeId.equals(that.volumeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerId, imageId, networkIds, volumeId);
    }

    public static final class DockerMetaBuilder {
        private String containerId;

        private String imageId;

        private Set<String> networkIds = new HashSet<>();

        private Optional<String> volumeId;

        public DockerMetaBuilder withContainerId(String id) {
            this.containerId = id;
            return this;
        }

        public DockerMetaBuilder withImageId(String id) {
            this.imageId = id;
            return this;
        }

        public DockerMetaBuilder withNetwoirkIds(Set<String> ids) {
            this.networkIds = ids;
            return this;
        }

        public DockerMeta build() {
            return new DockerMeta(containerId, imageId, networkIds, volumeId);
        }
    }
}
