package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.excecao.MatriculaNaoEncontradaException;
import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UseCase: Adicionar Disciplina.
 *
 * <p>O Aggregate decide se pode adicionar (invariantes). O UseCase só orquestra.
 * Exceções de invariante propagam naturalmente ao caller (Controller na Fase 4).</p>
 *
 * <p><strong>Sequência (D-10):</strong>
 * <ol>
 *   <li>Buscar o Aggregate existente — lança {@link MatriculaNaoEncontradaException} se não existe</li>
 *   <li>Aggregate decide se pode adicionar — lança se invariante violada</li>
 *   <li>Persistir o Aggregate atualizado</li>
 *   <li>Publicar eventos coletados pelo Aggregate</li>
 * </ol>
 * </p>
 *
 * <p><strong>Por que não precisa de {@code VerificadorElegibilidadeMatricula}?</strong>
 * A elegibilidade foi verificada no momento da matrícula. Adicionar disciplina é uma
 * operação sobre uma matrícula existente — as invariantes relevantes estão no próprio
 * Aggregate ({@code adicionarDisciplina}: estado, limite, duplicidade).</p>
 *
 * <p><strong>Exceções que podem ser lançadas pelo Aggregate:</strong>
 * <ul>
 *   <li>{@code MatriculaCanceladaException} — matrícula está cancelada</li>
 *   <li>{@code LimiteDisciplinasExcedidoException} — limite de 6 disciplinas atingido</li>
 *   <li>{@code DisciplinaJaMatriculadaException} — disciplina já está na matrícula</li>
 * </ul>
 * Todas propagam ao caller sem tratamento aqui — são regras de negócio, não erros de sistema.</p>
 *
 * <p><strong>Injeção por construtor:</strong> sem {@code @Autowired} explícito. Apenas
 * {@code MatriculaRepositorio} e {@code ApplicationEventPublisher} — sem verificador.</p>
 */
@Service
@Transactional
public class AdicionarDisciplinaUseCase {

    private final MatriculaRepositorio repositorio;
    private final ApplicationEventPublisher publicador;

    /**
     * Construtor com injeção de dependências.
     *
     * @param repositorio repositório para buscar e persistir a matrícula
     * @param publicador  publicador de eventos Spring — publica após persistência (D-10)
     */
    public AdicionarDisciplinaUseCase(
            MatriculaRepositorio repositorio,
            ApplicationEventPublisher publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    /**
     * Executa o fluxo de adicionar uma disciplina a uma matrícula existente.
     *
     * @param command dados necessários: identificador da matrícula e nome da disciplina
     * @throws MatriculaNaoEncontradaException                                      se a matrícula não existe
     * @throws br.com.escola.matricula.dominio.excecao.MatriculaCanceladaException  se a matrícula está cancelada
     * @throws br.com.escola.matricula.dominio.excecao.LimiteDisciplinasExcedidoException se o limite foi atingido
     * @throws br.com.escola.matricula.dominio.excecao.DisciplinaJaMatriculadaException   se a disciplina já está incluída
     */
    public void executar(AdicionarDisciplinaCommand command) {
        // 1. Buscar Aggregate existente (repositório retorna Optional)
        Matricula matricula = repositorio.buscarPorId(command.matriculaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(command.matriculaId()));

        // 2. Aggregate decide (lança LimiteDisciplinasExcedidoException, DisciplinaJaMatriculadaException,
        //    MatriculaCanceladaException se invariante violada)
        matricula.adicionarDisciplina(command.disciplina());

        // 3. Persistir (replace-all via MatriculaRepositorioMyBatis — Aggregate inteiro é regravado)
        repositorio.salvar(matricula);

        // 4. Publicar eventos (DisciplinaAdicionada coletado dentro do Aggregate)
        // @Transactional + @TransactionalEventListener garante execução após commit
        matricula.coletarEventos().forEach(publicador::publishEvent);
    }
}
