package br.com.escola.matricula.service;

import br.com.escola.matricula.model.Matricula;

import java.util.List;
import java.util.UUID;

/**
 * Interface de serviço — no módulo DDD os UseCases não têm interfaces separadas
 * (MatricularAlunoUseCase é diretamente @Service). No módulo camadas, a interface
 * MatriculaService é convencional no padrão Controller-&gt;Service-&gt;Repository.
 *
 * <p>Esta separação interface/implementação é comum em arquiteturas em camadas para
 * facilitar testes via mock. No módulo DDD, a interface seria no domínio (porta)
 * e a implementação na aplicação — aqui ambas estão no pacote service.</p>
 */
public interface MatriculaService {

    /**
     * Matricula um aluno em uma turma para o período indicado.
     *
     * @param alunoId       ID do aluno a matricular
     * @param turmaId       ID da turma na qual o aluno será matriculado
     * @param periodoInicio início do período letivo (ex: "2024-02-01")
     * @param periodoFim    fim do período letivo (ex: "2024-06-30")
     * @return UUID da matrícula criada
     */
    UUID matricular(UUID alunoId, UUID turmaId, String periodoInicio, String periodoFim);

    /**
     * Adiciona uma disciplina a uma matrícula existente.
     *
     * @param matriculaId   ID da matrícula
     * @param nomeDisciplina nome da disciplina a adicionar
     */
    void adicionarDisciplina(UUID matriculaId, String nomeDisciplina);

    /**
     * Cancela uma matrícula ativa.
     *
     * @param matriculaId ID da matrícula a cancelar
     */
    void cancelar(UUID matriculaId);

    /**
     * Retorna todas as matrículas de um aluno.
     *
     * @param alunoId ID do aluno
     * @return lista de matrículas do aluno
     */
    List<Matricula> buscarPorAluno(UUID alunoId);

}
