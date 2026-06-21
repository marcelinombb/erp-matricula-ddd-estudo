package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.MatriculaId;

/**
 * Exceção lançada quando uma matrícula não é encontrada pelo seu identificador.
 *
 * <p>Usada pelos UseCases da camada de aplicação (Wave 2) quando
 * {@code repositorio.buscarPorId(id)} retorna {@code Optional.empty()}.</p>
 *
 * <p>Lançada tipicamente em padrões como:
 * <pre>{@code
 * Matricula matricula = repositorio.buscarPorId(id)
 *     .orElseThrow(() -> new MatriculaNaoEncontradaException(id));
 * }</pre>
 * </p>
 */
public class MatriculaNaoEncontradaException extends RuntimeException {

    /** Identificador da matrícula que não foi encontrada. */
    private final MatriculaId matriculaId;

    /**
     * Constrói a exceção com o identificador da matrícula não encontrada.
     *
     * @param matriculaId identificador da matrícula que não existe no repositório
     */
    public MatriculaNaoEncontradaException(MatriculaId matriculaId) {
        super("Matrícula " + matriculaId.valor() + " não encontrada");
        this.matriculaId = matriculaId;
    }

    /** Retorna o identificador da matrícula não encontrada. */
    public MatriculaId getMatriculaId() {
        return matriculaId;
    }
}
