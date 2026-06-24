package br.com.escola.matricula.dominio.modelo;

import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import br.com.escola.matricula.dominio.vo.TurmaId;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidade que representa uma Turma oferecida em um período letivo específico.
 *
 * <p>{@code Turma} é referenciada por {@code TurmaId} no Aggregate {@code Matricula} —
 * sem carregar o objeto completo. Isso evita acoplamento entre Aggregates de Bounded
 * Contexts distintos. Ver ADR-003 (docs/adrs/ADR-003-referencia-por-id.md).</p>
 *
 * <p><strong>equals/hashCode por identidade ({@code TurmaId}):</strong> duas turmas
 * com o mesmo ID são a mesma turma, independente de capacidade ou nome.</p>
 *
 * <p>Em v1, a verificação de vagas disponíveis não está implementada no escopo do
 * Aggregate — a turma existe como referência para a matrícula. Ver STATE.md para
 * a decisão de granularidade do Aggregate.</p>
 */
public class Turma {

    /** Identificador único da turma. */
    private final TurmaId id;

    /** Nome descritivo da turma (ex: "Matemática Básica — Turma A"). */
    private final String nome;

    /** Período letivo ao qual esta turma pertence. */
    private final PeriodoLetivo periodoLetivo;

    /** Número máximo de alunos que podem ser matriculados nesta turma. */
    private final int vagasMaximas;

    /**
     * Constrói uma Turma com todos os campos obrigatórios.
     *
     * @param id            identificador único da turma
     * @param nome          nome descritivo da turma
     * @param periodoLetivo período letivo da turma
     * @param vagasMaximas  capacidade máxima (deve ser positiva)
     * @throws NullPointerException     se id, nome ou periodoLetivo forem nulos
     * @throws IllegalArgumentException se vagasMaximas for menor ou igual a zero
     */
    public Turma(TurmaId id, String nome, PeriodoLetivo periodoLetivo, int vagasMaximas) {
        this.id = Objects.requireNonNull(id, "Turma deve ter um id");
        this.nome = Objects.requireNonNull(nome, "Turma deve ter um nome");
        this.periodoLetivo = Objects.requireNonNull(periodoLetivo, "Turma deve ter um período letivo");
        if (vagasMaximas <= 0) {
            throw new IllegalArgumentException(
                "Vagas máximas deve ser positivo: " + vagasMaximas
            );
        }
        this.vagasMaximas = vagasMaximas;
    }

    /** Retorna o identificador único da turma. */
    public TurmaId getId() {
        return id;
    }

    /** Retorna o nome descritivo da turma. */
    public String getNome() {
        return nome;
    }

    /** Retorna o período letivo desta turma. */
    public PeriodoLetivo getPeriodoLetivo() {
        return periodoLetivo;
    }

    /** Retorna a quantidade máxima de vagas desta turma. */
    public int getVagasMaximas() {
        return vagasMaximas;
    }

    /**
     * Verifica se o período letivo desta turma está aberto para matrículas na data informada.
     *
     * <p>Regra: Semestre 1 abrange 01/fev a 31/jul. Semestre 2 abrange 01/ago a 31/dez.</p>
     *
     * <p>Overload testável: aceita {@code hoje} como parâmetro para que testes unitários
     * possam controlar a data sem depender de {@code LocalDate.now()} — evita testes
     * frágeis que falham quando o semestre vira.</p>
     *
     * @param hoje data de referência para a verificação
     * @return {@code true} se {@code hoje} está dentro do intervalo do semestre
     */
    public boolean periodoEstaAberto(LocalDate hoje) {
        int ano = periodoLetivo.ano();
        int semestre = periodoLetivo.semestre();

        LocalDate inicio;
        LocalDate fim;

        if (semestre == 1) {
            // 1º semestre: 01/fev a 31/jul
            inicio = LocalDate.of(ano, 2, 1);
            fim = LocalDate.of(ano, 7, 31);
        } else {
            // 2º semestre: 01/ago a 31/dez
            inicio = LocalDate.of(ano, 8, 1);
            fim = LocalDate.of(ano, 12, 31);
        }

        return !hoje.isBefore(inicio) && !hoje.isAfter(fim);
    }

    /**
     * Verifica se o período letivo desta turma está aberto para matrículas hoje.
     *
     * <p>Delegada a {@link #periodoEstaAberto(LocalDate)} com {@code LocalDate.now()}.
     * Código de produção usa este método; testes usam o overload com data explícita.</p>
     *
     * @return {@code true} se hoje está dentro do período letivo desta turma
     */
    public boolean periodoEstaAberto() {
        return periodoEstaAberto(LocalDate.now());
    }

    /**
     * Duas Turmas são iguais se e somente se têm o mesmo {@code TurmaId}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Turma outra)) return false;
        return id.equals(outra.id);
    }

    /**
     * Hash baseado apenas no {@code TurmaId}, consistente com {@code equals}.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Turma{id=" + id + ", nome='" + nome + "', periodo=" + periodoLetivo.descricao() + "}";
    }
}
