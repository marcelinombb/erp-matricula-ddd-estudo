package br.com.escola.matricula.dominio.repositorio;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.util.List;
import java.util.Optional;

/**
 * Interface de Repositório declarada no DOMÍNIO — não na infraestrutura.
 *
 * <p><strong>Regra de Dependência (Dependency Inversion Principle):</strong>
 * o domínio define o contrato; a infraestrutura implementa. A seta de dependência
 * vai de {@code infraestrutura/} para {@code dominio/} — nunca o contrário.
 * O domínio não importa nada da infraestrutura.</p>
 *
 * <p>Observe o que não está aqui: sem {@code extends JpaRepository}, sem
 * {@code import org.springframework.data}, sem {@code import org.apache.ibatis}.
 * Esta interface é definida em termos do próprio domínio: recebe e retorna
 * {@code MatriculaId}, {@code AlunoId}, {@code PeriodoLetivo}, {@code Matricula} —
 * todos objetos do domínio. Ver docs/02-design-tatico/repositorios.md.</p>
 *
 * <p><strong>Implementação:</strong> {@code MatriculaRepositorioMyBatis} em
 * {@code infraestrutura.persistencia/} — a única parte do sistema que sabe como
 * converter entre o modelo de domínio e o banco de dados (MyBatis + PostgreSQL).</p>
 *
 * <p><strong>Nomes em português (ADR-004):</strong> {@code buscarPorId},
 * {@code buscarPorAluno}, {@code existeMatriculaAtiva}, {@code salvar} —
 * mantendo a Linguagem Ubíqua também nas interfaces.
 * Ver docs/adrs/ADR-004-codigo-em-portugues.md.</p>
 *
 * <p><strong>Testabilidade:</strong> com esta interface, os UseCases podem ser
 * testados com uma implementação em memória — sem banco real, sem Spring.
 * Basta implementar esta interface com um {@code Map} em memória.</p>
 */
public interface MatriculaRepositorio {

    // REFD-05 (DDD-05): Compare com MatriculaRepository (camadas) — findById(UUID id) com @Mapper.
    // Aqui: recebe MatriculaId (Value Object), não UUID cru. O compilador impede passar um
    // TurmaId por engano — UUID cru não carrega semântica de domínio.
    /**
     * Busca uma matrícula pelo seu identificador único.
     *
     * @param id identificador da matrícula
     * @return {@code Optional} com a matrícula, ou vazio se não encontrada
     */
    Optional<Matricula> buscarPorId(MatriculaId id);

    // REFD-05 (DDD-05): Compare com MatriculaRepository (camadas) — findByAlunoId(UUID alunoId).
    // Aqui: parâmetro AlunoId (Value Object com semântica) em vez de UUID cru. O tipo do
    // parâmetro documenta a intenção; ao lado de buscarPorId(MatriculaId), a diferença é legível.
    /**
     * Busca todas as matrículas de um aluno (em qualquer estado).
     *
     * @param alunoId identificador do aluno
     * @return lista de matrículas do aluno (pode ser vazia)
     */
    List<Matricula> buscarPorAluno(AlunoId alunoId);

    // REFD-05 (DDD-05): No módulo camadas não existe equivalente com semântica de negócio.
    // MatriculaRepository.countDisciplinas(UUID) expõe uma contagem SQL como método — não uma pergunta
    // de negócio. Aqui, o nome é uma frase de negócio: "existe matrícula ativa para aluno no período?".
    /**
     * Verifica se existe uma matrícula ativa para o aluno no período especificado.
     *
     * <p>Usado pelo {@code VerificadorElegibilidadeMatricula} para detectar
     * tentativas de matrícula duplicada.</p>
     *
     * @param alunoId identificador do aluno
     * @param periodo período letivo a verificar
     * @return {@code true} se existe matrícula com status {@code Ativa} para o aluno no período
     */
    boolean existeMatriculaAtiva(AlunoId alunoId, PeriodoLetivo periodo);

    // REFD-05 (DDD-05): No módulo camadas, o Service chamava insert(Matricula) e itemMatriculaRepository.insert(item)
    // separadamente — o chamador decidia como persistir as partes. Aqui, salvar() persiste o
    // Aggregate inteiro (Matrícula + todos os ItemMatricula) como unidade atômica.
    /**
     * Persiste a matrícula (INSERT se nova, UPDATE se existente).
     *
     * <p>O Aggregate é a unidade de persistência — {@code salvar()} deve persistir
     * o estado completo: {@code Matricula} + todos os {@code ItemMatricula}.
     * A estratégia de persistência (replace-all para a coleção de itens) é
     * responsabilidade da implementação ({@code MatriculaRepositorioMyBatis}).
     * Ver D-12 em 03-CONTEXT.md.</p>
     *
     * <p>Chamado pelo UseCase ANTES de {@code coletarEventos()} — per D-10.</p>
     *
     * @param matricula matrícula a ser persistida (nunca nula)
     */
    void salvar(Matricula matricula);
}
