package com.swisscom.cloud.mongodb.dockerbroker.integrations

import com.swisscom.cloud.mongodb.dockerbroker.BaseSpecification
import com.swisscom.cloud.mongodb.dockerbroker.persistence.LastOperationEntity
import com.swisscom.cloud.mongodb.dockerbroker.persistence.LastOperationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class MongoDBSpecification extends BaseSpecification {

    @Autowired
    private LastOperationRepository repository

    static UUID lastOperationId = UUID.randomUUID()

    void 'should store last operation'() {
        def lastOperation = new LastOperationEntity(id: lastOperationId)

        when:
        repository.save(lastOperation)

        then:
        noExceptionThrown()
        def result = repository.
                findAll().
                find {si -> si.getId() == lastOperationId}
        result != null
    }

    void 'should delete last operation'() {
        def lastOperation = repository.
                findAll().
                find {si -> si.id == lastOperationId}

        when:
        repository.delete(lastOperation)

        then:
        noExceptionThrown()
        repository.
                findAll().
                find {si -> si.getId() == lastOperationId} == null
    }

    void 'should return ServiceInstance projection'() {
        given:
        repository.save(new LastOperationEntity(id: lastOperationId))

        when:
        LastOperationEntity serviceInstance = repository.findById(lastOperationId)
                .orElseThrow({ new RuntimeException("Last Operation not found") })

        then:
        noExceptionThrown()
        serviceInstance.getId() == lastOperationId
    }
}
