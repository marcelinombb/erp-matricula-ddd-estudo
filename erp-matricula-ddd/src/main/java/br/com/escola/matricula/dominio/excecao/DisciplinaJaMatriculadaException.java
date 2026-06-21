package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;

/**
 * Exceção lançada quando se tenta adicionar uma disciplina que já está na matrícula.
 *
 * <p>Invariante do Aggregate: a mesma disciplina não pode aparecer duas vezes
 * na mesma matrícula. Uma matrícula com "Matemática Básica" listada duas vezes
 * não faz sentido no domínio — a secretaria não saberia como calcular carga
 * horária nem gerar o histórico acadêmico corretamente.</p>
 *
 * <p>Lançada por: {@code Matricula.adicionarDisciplina()} quando a disciplina
 * já existe na lista interna.</p>
 */
public class DisciplinaJaMatriculadaException extends RuntimeException {

    /** Nome da disciplina que já está matriculada. */
    private final NomeDisciplina disciplina;

    /** Identificador da matrícula onde a duplicata foi detectada. */
    private final MatriculaId matriculaId;

    /**
     * Constrói a exceção com a disciplina duplicada e a matrícula.
     *
     * @param disciplina  nome da disciplina que já está na matrícula
     * @param matriculaId identificador da matrícula
     */
    public DisciplinaJaMatriculadaException(NomeDisciplina disciplina, MatriculaId matriculaId) {
        super("Disciplina '" + disciplina.valor() + "' já está matriculada em " + matriculaId.valor());
        this.disciplina = disciplina;
        this.matriculaId = matriculaId;
    }

    /** Retorna o nome da disciplina duplicada. */
    public NomeDisciplina getDisciplina() {
        return disciplina;
    }

    /** Retorna o identificador da matrícula. */
    public MatriculaId getMatriculaId() {
        return matriculaId;
    }
}
