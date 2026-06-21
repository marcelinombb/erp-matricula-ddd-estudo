package br.com.escola.matricula.infraestrutura.persistencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo relacional da tabela {@code matriculas}.
 *
 * <p><strong>Sem lógica de negócio</strong> — apenas campos que espelham as colunas da tabela.
 * Compare com {@link br.com.escola.matricula.dominio.modelo.Matricula}, que tem comportamento
 * ({@code adicionarDisciplina()}, {@code cancelar()}, {@code coletarEventos()}).
 * Esta separação explícita é o ponto pedagógico central do projeto: ver ADR-001, INF-06.</p>
 *
 * <p><strong>Por que não é um record?</strong> MyBatis precisa instanciar via construtor sem
 * argumentos e setar campos (ou via construtores específicos). {@code class} com campos públicos
 * é a abordagem mais simples e direta — sem getters, sem builder, sem XML adicional.
 * O aluno lê esta classe e imediatamente entende: "isso é só dados, sem comportamento".</p>
 *
 * <p><strong>Conversão realizada por:</strong> {@link MatriculaRowMapper} — o ÚNICO arquivo
 * que conhece tanto {@code MatriculaRow} quanto {@code Matricula}. Leia
 * {@code MatriculaRowMapper.java} para ver onde domínio e persistência se separam.</p>
 *
 * <p>Referências: ADR-001 (docs/adrs/ADR-001-mybatis-vs-jpa.md), INF-06 (.planning/REQUIREMENTS.md)</p>
 */
public class MatriculaRow {

    /** Identificador da matrícula — coluna {@code matriculas.id} */
    public UUID id;

    /** Referência por ID ao aluno — coluna {@code matriculas.aluno_id} (sem FK por ADR-003) */
    public UUID alunoId;

    /** Referência por ID à turma — coluna {@code matriculas.turma_id} (sem FK por ADR-003) */
    public UUID turmaId;

    /** Data de início do período letivo — coluna {@code matriculas.periodo_inicio} */
    public LocalDate periodoInicio;

    /** Data de fim do período letivo — coluna {@code matriculas.periodo_fim} */
    public LocalDate periodoFim;

    /**
     * Status como String — coluna {@code matriculas.status}.
     * Valores possíveis: "ATIVA", "CANCELADA", "CONCLUIDA".
     * Convertido de/para {@code StatusMatricula} (sealed interface) por {@link MatriculaRowMapper}.
     */
    public String status;

    /**
     * Data/hora do cancelamento — coluna {@code matriculas.cancelada_em}.
     * {@code null} quando status != "CANCELADA".
     * Mapeado de/para {@code StatusMatricula.Cancelada.canceladaEm()} por {@link MatriculaRowMapper}.
     */
    public LocalDateTime canceladaEm;

    /**
     * Data/hora da conclusão — coluna {@code matriculas.concluida_em}.
     * {@code null} quando status != "CONCLUIDA".
     * Mapeado de/para {@code StatusMatricula.Concluida.concluidaEm()} por {@link MatriculaRowMapper}.
     */
    public LocalDateTime concluidaEm;

    /**
     * Itens da matrícula — resultado do LEFT JOIN com {@code itens_matricula}.
     * Inicializado como lista vazia para evitar NPE no MyBatis quando não há itens
     * (MatriculaMapper.xml usa notNullColumn="item_disciplina" para evitar elemento fantasma).
     */
    public List<ItemMatriculaRow> itens = new ArrayList<>();
}
