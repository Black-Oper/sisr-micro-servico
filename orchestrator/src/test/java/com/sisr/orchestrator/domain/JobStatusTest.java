package com.sisr.orchestrator.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Testes da máquina de estados do job (Seção 6.2 do CLAUDE.md):
 *
 *   PENDING ──► PROCESSING ──► DONE
 *                          └─► FAILED
 *
 * DONE e FAILED são terminais (não transicionam mais).
 * Escritos ANTES da implementação real (TDD: RED).
 */
class JobStatusTest {

    @Test
    void pendingSoPodeIrParaProcessing() {
        assertThat(JobStatus.PENDING.canTransitionTo(JobStatus.PROCESSING)).isTrue();
        assertThat(JobStatus.PENDING.canTransitionTo(JobStatus.DONE)).isFalse();
        assertThat(JobStatus.PENDING.canTransitionTo(JobStatus.FAILED)).isFalse();
    }

    @Test
    void processingPodeConcluirOuFalhar() {
        assertThat(JobStatus.PROCESSING.canTransitionTo(JobStatus.DONE)).isTrue();
        assertThat(JobStatus.PROCESSING.canTransitionTo(JobStatus.FAILED)).isTrue();
    }

    @Test
    void processingNaoPodeVoltarParaPending() {
        assertThat(JobStatus.PROCESSING.canTransitionTo(JobStatus.PENDING)).isFalse();
    }

    @Test
    void estadosTerminaisNaoTransicionam() {
        for (JobStatus destino : JobStatus.values()) {
            assertThat(JobStatus.DONE.canTransitionTo(destino)).isFalse();
            assertThat(JobStatus.FAILED.canTransitionTo(destino)).isFalse();
        }
    }

    @Test
    void identificaEstadosTerminais() {
        assertThat(JobStatus.DONE.isTerminal()).isTrue();
        assertThat(JobStatus.FAILED.isTerminal()).isTrue();
        assertThat(JobStatus.PENDING.isTerminal()).isFalse();
        assertThat(JobStatus.PROCESSING.isTerminal()).isFalse();
    }
}
