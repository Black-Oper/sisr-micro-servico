package com.sisr.orchestrator.domain;

import java.util.Set;

public final class ScaleFactor {

    public static final int DEFAULT_VALUE = 2;

    private static final Set<Integer> ALLOWED = Set.of(2, 3, 4);

    private final int value;

    private ScaleFactor(int value) {
        this.value = value;
    }

    /**
     * Cria um ScaleFactor validado.
     *
     * @throws IllegalArgumentException se o valor não for 2, 3 ou 4.
     */
    public static ScaleFactor of(int value) {
        if (!ALLOWED.contains(value)) {
            throw new IllegalArgumentException(
                    "scale inválido: " + value + " (permitidos: 2, 3, 4)");
        }
        return new ScaleFactor(value);
    }

    /** Retorna o fator padrão (4). */
    public static ScaleFactor defaultFactor() {
        return new ScaleFactor(DEFAULT_VALUE);
    }

    public int value() {
        return value;
    }
}
