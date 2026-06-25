package br.com.escola.matricula.dominio.excecao;

import java.util.UUID;

/**
 * Exceção lançada quando se tenta matricular um aluno inativo.
 *
 * <p>Lançada por: {@code VerificadorElegibilidadeMatricula.verificar()} quando
 * {@code !aluno.estaAtivo()}.</p>
 */
public class AlunoInativoException extends RuntimeException {

    private final UUID alunoId;

    public AlunoInativoException(UUID alunoId) {
        super("Aluno " + alunoId + " está inativo e não pode ser matriculado");
        this.alunoId = alunoId;
    }

    public UUID getAlunoId() {
        return alunoId;
    }
}
