package com.sisr.orchestrator.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitJobPublisher implements JobPublisher {

    public static final String EXCHANGE = "sisr.exchange";
    public static final String ROUTING_KEY = "job.created";

    private final RabbitTemplate rabbitTemplate;

    public RabbitJobPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(JobMessage message) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
    }
}
