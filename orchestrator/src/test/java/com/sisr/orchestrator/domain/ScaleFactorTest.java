package com.sisr.orchestrator.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Teste do fator de escala (regra do contrato — Seção 6.1 do CLAUDE.md):
 * scale só pode ser 2, 3 ou 4; o padrão é 4.
 *
 * Escrito ANTES do código (TDD). Neste momento, ScaleFactor ainda não tem
 * implementação real -> estes testes devem FALHAR (estado RED).
 */
class ScaleFactorTest {

    @Test
    void aceitaOsFatoresValidos_2_3_4() {
        assertThat(ScaleFactor.of(2).value()).isEqualTo(2);
        assertThat(ScaleFactor.of(3).value()).isEqualTo(3);
        assertThat(ScaleFactor.of(4).value()).isEqualTo(4);
    }

    @Test
    void rejeitaFatorForaDoPermitido() {
        assertThatThrownBy(() -> ScaleFactor.of(5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void usaDoisComoFatorPadrao() {
        assertThat(ScaleFactor.defaultFactor().value()).isEqualTo(2);
    }
}
