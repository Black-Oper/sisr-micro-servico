package com.sisr.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada do serviço orchestrator (Spring Boot).
 */
@SpringBootApplication
public class OrchestratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrchestratorApplication.class, args);
    }
}
