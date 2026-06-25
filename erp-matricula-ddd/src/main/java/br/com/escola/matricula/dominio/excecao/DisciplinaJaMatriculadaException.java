package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.NomeDisciplina;

import java.util.UUID;

/**
 * Exceção lançada quando se tenta adicionar uma disciplina que já está na matrícula.
 *
 * <p>Lançada por: {@code Matricula.adicionarDisciplina()} quando a disciplina
 * já existe na lista interna.</p>
 */
public class DisciplinaJaMatriculadaException extends RuntimeException {

    private final NomeDisciplina disciplina;
    private final UUID matriculaId;

    public DisciplinaJaMatriculadaException(NomeDisciplina disciplina, UUID matriculaId) {
        super("Disciplina '" + disciplina.valor() + "' já está matriculada em " + matriculaId);
        this.disciplina = disciplina;
        this.matriculaId = matriculaId;
    }

    public NomeDisciplina getDisciplina() {
        return disciplina;
    }

    public UUID getMatriculaId() {
        return matriculaId;
    }
}
