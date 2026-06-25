package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.util.UUID;

/**
 * Exceção lançada quando se tenta criar uma segunda matrícula ativa para o mesmo aluno
 * no mesmo período letivo.
 *
 * <p>Lançada por: {@code VerificadorElegibilidadeMatricula.verificar()} quando
 * {@code repositorio.existeMatriculaAtiva(aluno.getId(), periodo)} retorna {@code true}.</p>
 */
public class MatriculaDuplicadaException extends RuntimeException {

    private final UUID alunoId;
    private final PeriodoLetivo periodo;

    public MatriculaDuplicadaException(UUID alunoId, PeriodoLetivo periodo) {
        super("Aluno " + alunoId + " já possui matrícula ativa no período "
            + periodo.ano() + "/" + periodo.semestre());
        this.alunoId = alunoId;
        this.periodo = periodo;
    }

    public UUID getAlunoId() {
        return alunoId;
    }

    public PeriodoLetivo getPeriodo() {
        return periodo;
    }
}
