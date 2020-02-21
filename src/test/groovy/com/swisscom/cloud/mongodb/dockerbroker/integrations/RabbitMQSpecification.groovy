package com.swisscom.cloud.mongodb.dockerbroker.integrations

import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import com.swisscom.cloud.mongodb.dockerbroker.BaseSpecification
import spock.lang.Shared

class RabbitMQSpecification extends BaseSpecification {

    final static EXCHANGE_NAME = "RABBIT-MQ-SPECIFICATION"
    final static QUEUE_NAME = "RABBIT-MQ-SPECIFICATION"

    final static MESSAGE = "RABBIT-MQ-SPECIFICATION-DATA"

    @Shared
    ConnectionFactory factory

    @Shared
    Connection connection

    @Shared
    Channel channel

    def setupSpec() {
        factory = new ConnectionFactory()
        factory.setHost("localhost")
        connection = factory.newConnection()

        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT)
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "")
    }

    def cleanupSpec() {
        if (channel != null) {
            channel.close()
        }
        connection.close()
    }

    void 'should send message to rabbitmq'() {
        when:
        channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null, MESSAGE.getBytes())

        then:
        noExceptionThrown()
    }

    void 'should read message from rabbitmq'() {
        given:
        boolean isCanceled = false;
        boolean isReceived = false;
        Delivery receivedMessage = null;

        and:
        channel.basicConsume(
            QUEUE_NAME,
            { String consumerTag, Delivery message ->
                receivedMessage = message;
                isReceived = true;
            } as DeliverCallback,
            { cancelTag ->
                isCanceled = true;
            } as CancelCallback)

        when:
        channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null, MESSAGE.getBytes())
        // give RabbitMQ some time to forward the message
        Thread.sleep(150)

        then:
        noExceptionThrown()
        isCanceled == false
        isReceived == true

        and:
        new String(receivedMessage.getBody()).equals(MESSAGE)
    }
}
