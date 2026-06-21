package br.com.escola.matricula.dominio.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa o identificador único de um Aluno.
 *
 * <p><strong>Por que IDs tipados existem (ADR-003)?</strong></p>
 *
 * <p>Com UUID cru, o compilador não distingue {@code alunoId} de {@code turmaId} —
 * ambos são {@code UUID}. Uma inversão de parâmetros passa em compilação e só gera
 * erro em runtime. Com {@code AlunoId} e {@code TurmaId} como tipos distintos,
 * uma inversão de parâmetros é um <em>erro de compilação</em>.</p>
 *
 * <p>O Aggregate {@code Matricula} referencia o Aluno pelo seu {@code AlunoId} —
 * nunca carrega o objeto {@code Aluno} completo. Isso evita acoplamento entre
 * Aggregates de Bounded Contexts distintos.
 * Ver ADR-003 (docs/adrs/ADR-003-referencia-por-id.md).</p>
 */
public record AlunoId(UUID valor) {

    /**
     * Construtor compacto — valida que o valor não é nulo.
     *
     * @throws NullPointerException se valor for nulo
     */
    public AlunoId {
        Objects.requireNonNull(valor, "AlunoId não pode ser nulo");
    }
}
