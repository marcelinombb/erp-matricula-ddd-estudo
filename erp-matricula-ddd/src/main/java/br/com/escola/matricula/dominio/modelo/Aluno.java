package br.com.escola.matricula.dominio.modelo;

import br.com.escola.matricula.dominio.vo.Cpf;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa um Aluno do sistema escolar.
 *
 * <p><strong>Diferença entre Entidade e Value Object:</strong></p>
 *
 * <p>{@code Aluno} é uma <em>Entidade</em>, não um Value Object. A distinção é semântica:
 * um aluno tem identidade própria que persiste no tempo, independente de seus atributos.
 * Um aluno pode mudar de nome após casamento, mudar de endereço, ter seu status alterado
 * de ativo para inativo — e continua sendo o mesmo aluno, com o mesmo histórico e as
 * mesmas matrículas passadas. O identificador (UUID) não muda.</p>
 *
 * <p>Compare com {@code Cpf}: dois CPFs com o mesmo valor são indistinguíveis —
 * são o mesmo CPF. Dois Alunos com o mesmo nome podem ser pessoas completamente diferentes.</p>
 *
 * <p><strong>Por que não é um {@code record}?</strong></p>
 *
 * <p>{@code Aluno} pode mudar de status (ativo → inativo). Um {@code record} Java 21
 * é imutável por design — campos {@code final}, sem setters. Entidades têm ciclo de vida
 * com estado mutável; para isso usamos {@code class}. O {@code record} é perfeito para
 * Value Objects (sem identidade, comparados por valor); a {@code class} é necessária
 * quando o objeto tem identidade que persiste enquanto seus atributos mudam.</p>
 *
 * <p><strong>equals/hashCode por identidade:</strong></p>
 *
 * <p>Dois {@code Aluno} são iguais se e somente se têm o mesmo {@code id} (UUID).
 * Não importa se o nome mudou, se o CPF foi corrigido — o ID define quem é quem.</p>
 */
public class Aluno {

    /** Identidade permanente do aluno — nunca muda durante o ciclo de vida. */
    private final UUID id;

    /** CPF do aluno — imutável (Value Object). */
    private final Cpf cpf;

    /** Nome completo do aluno. */
    private final String nome;

    /**
     * Status de atividade. Pode mudar: ativo → inativo (por inadimplência)
     * ou inativo → ativo (por regularização). Por isso é {@code boolean} e não {@code final}.
     */
    private boolean ativo;

    /**
     * Constrói um Aluno com todos os campos obrigatórios.
     *
     * @param id   identificador único do aluno (nunca nulo)
     * @param cpf  CPF do aluno já validado (nunca nulo)
     * @param nome nome completo do aluno (nunca nulo)
     * @param ativo {@code true} se o aluno está ativo no sistema
     */
    public Aluno(UUID id, Cpf cpf, String nome, boolean ativo) {
        this.id = Objects.requireNonNull(id, "Aluno deve ter um id");
        this.cpf = Objects.requireNonNull(cpf, "Aluno deve ter um CPF");
        this.nome = Objects.requireNonNull(nome, "Aluno deve ter um nome");
        this.ativo = ativo;
    }

    /** Retorna o identificador único do aluno. */
    public UUID getId() {
        return id;
    }

    /** Retorna o CPF do aluno. */
    public Cpf getCpf() {
        return cpf;
    }

    /** Retorna o nome completo do aluno. */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna {@code true} se o aluno está ativo.
     * O {@code VerificadorElegibilidadeMatricula} usa este método para verificar
     * se o aluno pode ser matriculado.
     */
    public boolean estaAtivo() {
        return ativo;
    }

    /**
     * Desativa o aluno (ex: por inadimplência ou solicitação).
     * Um aluno desativado não pode ser matriculado.
     */
    public void desativar() {
        this.ativo = false;
    }

    /**
     * Dois Alunos são iguais se e somente se têm o mesmo {@code id} (UUID).
     * Atributos como nome e CPF não participam da comparação — é a identidade que define.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aluno outro)) return false; // pattern matching Java 16+ (finalizado no 21)
        return id.equals(outro.id);
    }

    /**
     * Hash baseado apenas no {@code id}, consistente com {@code equals}.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Aluno{id=" + id + ", nome='" + nome + "', ativo=" + ativo + "}";
    }
}
