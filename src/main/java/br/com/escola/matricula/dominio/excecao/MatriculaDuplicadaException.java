package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

/**
 * Exceção lançada quando se tenta criar uma segunda matrícula ativa para o mesmo aluno
 * no mesmo período letivo.
 *
 * <p>Um aluno pode ter apenas uma matrícula ativa por período letivo. Esta restrição
 * é verificada pelo {@code VerificadorElegibilidadeMatricula} via
 * {@code MatriculaRepositorio.existeMatriculaAtiva()} antes de criar a matrícula.</p>
 *
 * <p>Lançada por: {@code VerificadorElegibilidadeMatricula.verificar()} quando
 * {@code repositorio.existeMatriculaAtiva(aluno.getId(), periodo)} retorna {@code true}.</p>
 */
public class MatriculaDuplicadaException extends RuntimeException {

    /** Identificador do aluno com matrícula duplicada. */
    private final AlunoId alunoId;

    /** Período letivo onde já existe uma matrícula ativa. */
    private final PeriodoLetivo periodo;

    /**
     * Constrói a exceção com os dados do aluno e período.
     *
     * @param alunoId identificador do aluno
     * @param periodo período letivo onde já existe matrícula ativa
     */
    public MatriculaDuplicadaException(AlunoId alunoId, PeriodoLetivo periodo) {
        super("Aluno " + alunoId.valor() + " já possui matrícula ativa no período "
            + periodo.ano() + "/" + periodo.semestre());
        this.alunoId = alunoId;
        this.periodo = periodo;
    }

    /** Retorna o identificador do aluno. */
    public AlunoId getAlunoId() {
        return alunoId;
    }

    /** Retorna o período letivo com matrícula duplicada. */
    public PeriodoLetivo getPeriodo() {
        return periodo;
    }
}
