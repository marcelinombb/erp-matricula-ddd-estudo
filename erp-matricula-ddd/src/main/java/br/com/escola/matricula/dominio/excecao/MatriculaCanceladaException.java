package br.com.escola.matricula.dominio.excecao;

import java.util.UUID;

/**
 * Exceção lançada quando se tenta operar em uma matrícula que já foi cancelada.
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

    private final UUID matriculaId;

    public MatriculaCanceladaException(UUID matriculaId) {
        super("Matrícula " + matriculaId + " está cancelada e não aceita operações");
        this.matriculaId = matriculaId;
    }

    public UUID getMatriculaId() {
        return matriculaId;
    }
}
