package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import br.com.escola.matricula.dominio.servico.VerificadorElegibilidadeMatricula;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application Service — orquestra sem decidir.
 *
 * <p><strong>O que este UseCase faz:</strong>
 * <ol>
 *   <li>Valida elegibilidade (Domain Service — decide se pode matricular)</li>
 *   <li>Cria o Aggregate (domínio decide — estado inicial, evento gerado)</li>
 *   <li>Persiste (Repositório — infraestrutura salva no PostgreSQL)</li>
 *   <li>Publica eventos (D-10 — publicação APÓS persistência)</li>
 * </ol>
 * </p>
 *
 * <p><strong>O que este UseCase NÃO faz:</strong> não contém lógica de negócio.
 * Toda regra fica no Aggregate ({@code Matricula}) ou no Domain Service
 * ({@code VerificadorElegibilidadeMatricula}). O UseCase apenas orquestra a sequência.</p>
 *
 * <p><strong>Diferença DDD vs arquitetura em camadas:</strong></p>
 * <pre>{@code
 * // Arquitetura tradicional — Service decide:
 * if (aluno.ativo && periodo.aberto && !duplicado) {
 *     matricula = new Matricula();
 *     matriculaRepo.save(matricula);
 * }
 *
 * // DDD — UseCase orquestra, domínio decide:
 * verificador.verificar(aluno, turma, periodo);   // Domain Service decide e lança se não pode
 * matricula = Matricula.criar(alunoId, turmaId, periodo); // Aggregate decide o estado inicial
 * repositorio.salvar(matricula);                  // Repositório persiste
 * matricula.coletarEventos().forEach(pub::publishEvent); // UseCase publica
 * }</pre>
 *
 * <p><strong>@Transactional + @TransactionalEventListener:</strong> o {@code @Transactional}
 * neste UseCase garante que todo o bloco (verificar + criar + salvar) é atômico.
 * Listeners com {@code @TransactionalEventListener} recebem o evento apenas após o commit
 * — sem que o listener processe dados que ainda podem sofrer rollback.</p>
 *
 * <p><strong>Injeção por construtor (sem @Autowired):</strong> Spring 4.3+ injeta
 * automaticamente quando há um único construtor. Sem {@code @Autowired} explícito —
 * mais limpo e testável (permite testar com implementações stub).</p>
 */
@Service
@Transactional
public class MatricularAlunoUseCase {

    private final MatriculaRepositorio repositorio;
    private final VerificadorElegibilidadeMatricula verificador;
    private final ApplicationEventPublisher publicador;

    /**
     * Construtor com injeção de dependências.
     *
     * <p>Spring injeta automaticamente via construtor único — sem {@code @Autowired}.
     * Cada dependência é final — sem possibilidade de substituição após construção.</p>
     *
     * @param repositorio repositório para persistir a matrícula
     * @param verificador Domain Service que verifica elegibilidade antes de criar
     * @param publicador  publicador de eventos Spring — publica após persistência (D-10)
     */
    public MatricularAlunoUseCase(
            MatriculaRepositorio repositorio,
            VerificadorElegibilidadeMatricula verificador,
            ApplicationEventPublisher publicador) {
        this.repositorio = repositorio;
        this.verificador = verificador;
        this.publicador = publicador;
    }

    /**
     * Executa o fluxo de matricular um aluno em uma turma.
     *
     * <p><strong>Sequência obrigatória (D-10):</strong></p>
     *
     * @param command dados necessários para a matrícula (Aluno, Turma, PeriodoLetivo)
     * @return identificador da matrícula criada
     * @throws br.com.escola.matricula.dominio.excecao.AlunoInativoException       se o aluno não está ativo
     * @throws br.com.escola.matricula.dominio.excecao.PeriodoFechadoException     se o período não está aberto
     * @throws br.com.escola.matricula.dominio.excecao.MatriculaDuplicadaException se já existe matrícula ativa no período
     */
    public MatriculaId executar(MatricularAlunoCommand command) {
        // 1. Validar elegibilidade (Domain Service — lança exceção se não elegível)
        verificador.verificar(command.aluno(), command.turma(), command.periodo());

        // 2. Criar Aggregate (domínio decide — estado inicial ATIVA, evento AlunoMatriculado coletado)
        Matricula matricula = Matricula.criar(
                command.aluno().getId(),
                command.turma().getId(),
                command.periodo());

        // 3. Persistir ANTES de publicar eventos (D-10: ordem obrigatória)
        repositorio.salvar(matricula);

        // 4. Publicar eventos APÓS persistência
        // @Transactional + @TransactionalEventListener garante que listeners só executam após commit
        // Sem esta ordem, um rollback posterior processaria eventos de dados não persistidos
        matricula.coletarEventos().forEach(publicador::publishEvent);

        return matricula.getId();
    }
}
