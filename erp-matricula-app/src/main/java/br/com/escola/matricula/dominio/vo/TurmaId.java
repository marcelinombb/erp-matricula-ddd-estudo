package br.com.escola.matricula.dominio.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa o identificador único de uma Turma.
 *
 * <p><strong>Por que IDs tipados existem (ADR-003)?</strong></p>
 *
 * <p>Com UUID cru, o compilador não distingue {@code turmaId} de {@code alunoId} —
 * ambos são {@code UUID}. Uma inversão de parâmetros passa em compilação e só gera
 * erro em runtime. Com {@code TurmaId} e {@code AlunoId} como tipos distintos,
 * uma inversão de parâmetros é um <em>erro de compilação</em>.</p>
 *
 * <p>O Aggregate {@code Matricula} referencia a Turma pelo seu {@code TurmaId} —
 * nunca carrega o objeto {@code Turma} completo. Isso evita acoplamento entre
 * Aggregates de Bounded Contexts distintos.
 * Ver ADR-003 (docs/adrs/ADR-003-referencia-por-id.md).</p>
 */
public record TurmaId(UUID valor) {

    /**
     * Construtor compacto — valida que o valor não é nulo.
     *
     * @throws NullPointerException se valor for nulo
     */
    public TurmaId {
        Objects.requireNonNull(valor, "TurmaId não pode ser nulo");
    }
}
