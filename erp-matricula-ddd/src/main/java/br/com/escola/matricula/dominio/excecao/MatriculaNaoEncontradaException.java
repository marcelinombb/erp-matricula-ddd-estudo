package br.com.escola.matricula.dominio.excecao;

import java.util.UUID;

/**
 * Exceção lançada quando uma matrícula não é encontrada pelo seu identificador.
 *
 * <p>Usada pelos UseCases quando {@code repositorio.buscarPorId(id)} retorna
 * {@code Optional.empty()}.</p>
 */
public class MatriculaNaoEncontradaException extends RuntimeException {

    private final UUID matriculaId;

    public MatriculaNaoEncontradaException(UUID matriculaId) {
        super("Matrícula " + matriculaId + " não encontrada");
        this.matriculaId = matriculaId;
    }

    public UUID getMatriculaId() {
        return matriculaId;
    }
}
