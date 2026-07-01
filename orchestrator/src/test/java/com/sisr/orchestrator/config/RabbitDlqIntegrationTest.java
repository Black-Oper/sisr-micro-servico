package com.sisr.orchestrator.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.client.GetResponse;
import com.sisr.orchestrator.messaging.RabbitJobPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Teste de INTEGRAÇÃO da DLQ contra um RabbitMQ REAL (Testcontainers).
 * Declara a topologia de produção (via RabbitConfig), publica, rejeita a
 * mensagem sem requeue e verifica que ela acabou na dead-letter queue.
 */
@Testcontainers
class RabbitDlqIntegrationTest {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management");

    RabbitTemplate template;

    @BeforeEach
    void setup() {
        var cf = new CachingConnectionFactory(rabbit.getHost(), rabbit.getAmqpPort());
        cf.setUsername(rabbit.getAdminUsername());
        cf.setPassword(rabbit.getAdminPassword());

        var config = new RabbitConfig();
        var exchange = config.sisrExchange();
        var mainQueue = config.jobsQueue();
        var dlx = config.deadLetterExchange();
        var dlq = config.deadLetterQueue();

        var admin = new RabbitAdmin(cf);
        admin.declareExchange(exchange);
        admin.declareQueue(mainQueue);
        admin.declareExchange(dlx);
        admin.declareQueue(dlq);
        admin.declareBinding(config.jobsBinding(mainQueue, exchange));
        admin.declareBinding(config.deadLetterBinding(dlq, dlx));

        template = new RabbitTemplate(cf);
        template.setReceiveTimeout(5000);
    }

    @Test
    void mensagemRejeitadaVaiParaDlq() {
        template.convertAndSend(
                RabbitJobPublisher.EXCHANGE, RabbitJobPublisher.ROUTING_KEY, "payload-x");

        // consome da fila principal e rejeita sem requeue -> deve ir para a DLQ
        template.execute(channel -> {
            GetResponse got = channel.basicGet(RabbitConfig.QUEUE, false);
            assertThat(got).isNotNull();
            channel.basicNack(got.getEnvelope().getDeliveryTag(), false, false);
            return null;
        });

        Object morta = template.receiveAndConvert(RabbitConfig.DLQ);
        assertThat(morta).isNotNull();
    }
}
