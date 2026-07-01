package com.sisr.orchestrator.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste de INTEGRAÇÃO do RabbitJobPublisher contra um RabbitMQ REAL (Testcontainers).
 * Publica via JobPublisher e consome de volta da fila para conferir o payload.
 */
@Testcontainers
class RabbitJobPublisherIntegrationTest {

    private static final String QUEUE = "sisr.jobs.queue";

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    RabbitTemplate rabbitTemplate;
    JobPublisher publisher;

    @BeforeEach
    void setup() {
        var connectionFactory = new CachingConnectionFactory(rabbit.getHost(), rabbit.getAmqpPort());
        connectionFactory.setUsername(rabbit.getAdminUsername());
        connectionFactory.setPassword(rabbit.getAdminPassword());

        rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setReceiveTimeout(5000);

        // Declara exchange, fila e binding (mesmo contrato da Seção 6.3)
        var admin = new RabbitAdmin(connectionFactory);
        var exchange = new DirectExchange(RabbitJobPublisher.EXCHANGE, true, false);
        var queue = new Queue(QUEUE, true);
        admin.declareExchange(exchange);
        admin.declareQueue(queue);
        admin.declareBinding(
                BindingBuilder.bind(queue).to(exchange).with(RabbitJobPublisher.ROUTING_KEY));

        publisher = new RabbitJobPublisher(rabbitTemplate);
    }

    @Test
    void publicaMensagemDoJob() {
        var msg = new JobMessage(
                "job-1", "sisr-inputs", "job-1/input.png", 4, "2026-06-30T12:00:00Z");

        publisher.publish(msg);

        JobMessage recebida = (JobMessage) rabbitTemplate.receiveAndConvert(QUEUE);

        assertThat(recebida).isNotNull();
        assertThat(recebida.jobId()).isEqualTo("job-1");
        assertThat(recebida.inputBucket()).isEqualTo("sisr-inputs");
        assertThat(recebida.inputKey()).isEqualTo("job-1/input.png");
        assertThat(recebida.scale()).isEqualTo(4);
    }
}
