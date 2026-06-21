package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.MatriculaId;

/**
 * Exceção lançada quando se tenta operar em uma matrícula que já foi cancelada.
 *
 * <p>Cancelamento é um estado terminal: após o cancelamento, a matrícula existe
 * apenas como registro histórico. Qualquer tentativa de modificá-la é um erro de
 * fluxo — provavelmente um bug na interface ou uma chamada fora de ordem.</p>
 *
 * <p>Lançada por:
 * <ul>
 *   <li>{@code Matricula.adicionarDisciplina()} — tentativa de adicionar disciplina
 *       a matrícula cancelada</li>
 *   <li>{@code Matricula.cancelar()} — tentativa de cancelar uma matrícula já cancelada</li>
 * </ul>
 * </p>
 */
public class MatriculaCanceladaException extends RuntimeException {

    /** Identificador da matrícula que está cancelada. */
    private final MatriculaId matriculaId;

    /**
     * Constrói a exceção com o identificador da matrícula cancelada.
     *
     * @param matriculaId identificador da matrícula que está cancelada
     */
    public MatriculaCanceladaException(MatriculaId matriculaId) {
        super("Matrícula " + matriculaId.valor() + " está cancelada e não aceita operações");
        this.matriculaId = matriculaId;
    }

    /** Retorna o identificador da matrícula cancelada. */
    public MatriculaId getMatriculaId() {
        return matriculaId;
    }
}
