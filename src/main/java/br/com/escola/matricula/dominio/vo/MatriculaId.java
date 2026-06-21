package br.com.escola.matricula.dominio.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa o identificador único de uma Matrícula.
 *
 * <p><strong>Por que IDs tipados existem (ADR-003)?</strong></p>
 *
 * <p>Com UUID cru, o compilador não distingue {@code matriculaId} de {@code alunoId} —
 * ambos são {@code UUID}. Uma inversão de parâmetros passa em compilação e só gera
 * erro em runtime. Com {@code MatriculaId}, {@code AlunoId} e {@code TurmaId} como
 * tipos distintos, uma inversão de parâmetros é um <em>erro de compilação</em>.</p>
 *
 * <pre>{@code
 * // Sem IDs tipados — compilador aceita, bug silencioso:
 * new Matricula(turmaId, alunoId); // UUID, UUID — ordem errada passa em compilação
 *
 * // Com IDs tipados — compilador rejeita:
 * new Matricula(turmaId, alunoId); // erro: AlunoId esperado, TurmaId fornecido
 * }</pre>
 *
 * <p>Ver ADR-003 (docs/adrs/ADR-003-referencia-por-id.md) para a justificativa completa.</p>
 */
public record MatriculaId(UUID valor) {

    /**
     * Construtor compacto — valida que o valor não é nulo.
     *
     * @throws NullPointerException se valor for nulo
     */
    public MatriculaId {
        Objects.requireNonNull(valor, "MatriculaId não pode ser nulo");
    }
}
