package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.AlunoId;

/**
 * Exceção lançada quando se tenta matricular um aluno inativo.
 *
 * <p>Um aluno inativo (por inadimplência ou solicitação) não pode ser matriculado.
 * A atividade do aluno é verificada pelo {@code VerificadorElegibilidadeMatricula}
 * antes de criar a matrícula.</p>
 *
 * <p>Lançada por: {@code VerificadorElegibilidadeMatricula.verificar()} quando
 * {@code !aluno.estaAtivo()}.</p>
 */
public class AlunoInativoException extends RuntimeException {

    /** Identificador do aluno inativo. */
    private final AlunoId alunoId;

    /**
     * Constrói a exceção com o identificador do aluno inativo.
     *
     * @param alunoId identificador do aluno que está inativo
     */
    public AlunoInativoException(AlunoId alunoId) {
        super("Aluno " + alunoId.valor() + " está inativo e não pode ser matriculado");
        this.alunoId = alunoId;
    }

    /** Retorna o identificador do aluno inativo. */
    public AlunoId getAlunoId() {
        return alunoId;
    }
}
