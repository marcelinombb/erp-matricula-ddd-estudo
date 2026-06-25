package br.com.escola.matricula.dominio.excecao;

import java.util.UUID;

/**
 * Exceção lançada quando uma matrícula tenta exceder o limite máximo de disciplinas.
 *
 * <p>Lançada por: {@code Matricula.adicionarDisciplina()} quando
 * {@code disciplinas.size() >= LIMITE_DISCIPLINAS}.</p>
 */
public class LimiteDisciplinasExcedidoException extends RuntimeException {

    private final int limite;
    private final int atual;
    private final UUID matriculaId;

    public LimiteDisciplinasExcedidoException(int limite, int atual, UUID matriculaId) {
        super("Limite de " + limite + " disciplinas excedido. Atual: " + atual
            + ". Matrícula: " + matriculaId);
        this.limite = limite;
        this.atual = atual;
        this.matriculaId = matriculaId;
    }

    public int getLimite() {
        return limite;
    }

    public int getAtual() {
        return atual;
    }

    public UUID getMatriculaId() {
        return matriculaId;
    }
}
