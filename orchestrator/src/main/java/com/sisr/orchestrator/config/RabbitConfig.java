package com.sisr.orchestrator.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sisr.orchestrator.messaging.RabbitJobPublisher;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "sisr.jobs.queue";
    public static final String DLX = "sisr.dlx";
    public static final String DLQ = "sisr.jobs.dlq";
    public static final String DEAD_ROUTING_KEY = "job.dead";

    @Bean
    DirectExchange sisrExchange() {
        return new DirectExchange(RabbitJobPublisher.EXCHANGE, true, false);
    }

    @Bean
    Queue jobsQueue() {
        // Fila durável que encaminha rejeições (nack sem requeue) para a DLQ.
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DEAD_ROUTING_KEY)
                .build();
    }

    @Bean
    Binding jobsBinding(Queue jobsQueue, DirectExchange sisrExchange) {
        return BindingBuilder.bind(jobsQueue).to(sisrExchange).with(RabbitJobPublisher.ROUTING_KEY);
    }

    // ----- Dead-letter (para mensagens rejeitadas pelo worker) -----

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue deadLetterQueue() {
        return new Queue(DLQ, true);
    }

    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_ROUTING_KEY);
    }

    /** Faz o RabbitTemplate enviar/receber JSON em vez de serialização Java. */
    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
