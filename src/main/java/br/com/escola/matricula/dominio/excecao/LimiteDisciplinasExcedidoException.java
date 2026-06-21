package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.MatriculaId;

/**
 * Exceção lançada quando uma matrícula tenta exceder o limite máximo de disciplinas.
 *
 * <p>Esta exceção tem campos estruturados ({@code limite} e {@code atual}) em vez de
 * apenas uma mensagem String. Isso permite que os UseCases e Controllers da Fase 4
 * retornem respostas HTTP 422 com dados estruturados, sem precisar parsear a mensagem.</p>
 *
 * <p>Lançada por: {@code Matricula.adicionarDisciplina()} quando
 * {@code disciplinas.size() >= LIMITE_DISCIPLINAS}.</p>
 */
public class LimiteDisciplinasExcedidoException extends RuntimeException {

    /** Limite máximo configurado para a matrícula. */
    private final int limite;

    /** Número atual de disciplinas na matrícula no momento da tentativa. */
    private final int atual;

    /** Identificador da matrícula que excedeu o limite. */
    private final MatriculaId matriculaId;

    /**
     * Constrói a exceção com dados estruturados sobre o limite excedido.
     *
     * @param limite      número máximo de disciplinas permitidas
     * @param atual       número de disciplinas que a matrícula já possui
     * @param matriculaId identificador da matrícula com limite excedido
     */
    public LimiteDisciplinasExcedidoException(int limite, int atual, MatriculaId matriculaId) {
        super("Limite de " + limite + " disciplinas excedido. Atual: " + atual
            + ". Matrícula: " + matriculaId.valor());
        this.limite = limite;
        this.atual = atual;
        this.matriculaId = matriculaId;
    }

    /** Retorna o limite máximo de disciplinas configurado. */
    public int getLimite() {
        return limite;
    }

    /** Retorna o número atual de disciplinas na matrícula. */
    public int getAtual() {
        return atual;
    }

    /** Retorna o identificador da matrícula com limite excedido. */
    public MatriculaId getMatriculaId() {
        return matriculaId;
    }
}
