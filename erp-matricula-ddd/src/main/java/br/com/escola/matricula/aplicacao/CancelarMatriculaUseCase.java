package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.excecao.MatriculaNaoEncontradaException;
import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UseCase: Cancelar Matrícula.
 *
 * <p>Busca, delega cancelamento ao Aggregate, persiste, publica {@code MatriculaCancelada}.
 * Listeners stub ({@code FinanceiroEventListener}, {@code AcademicoEventListener})
 * recebem o evento e processam após commit — via {@code @TransactionalEventListener}.</p>
 *
 * <p><strong>Sequência (D-10):</strong>
 * <ol>
 *   <li>Buscar o Aggregate existente — lança {@link MatriculaNaoEncontradaException} se não existe</li>
 *   <li>Aggregate decide o cancelamento — lança {@code MatriculaCanceladaException} se já cancelada</li>
 *   <li>Persistir o Aggregate com status atualizado</li>
 *   <li>Publicar evento {@code MatriculaCancelada}</li>
 * </ol>
 * </p>
 *
 * <p><strong>Por que cancelamento é responsabilidade do Aggregate?</strong>
 * O cancelamento é uma transição de estado com regra de negócio: uma matrícula já cancelada
 * não pode ser cancelada novamente. Esta regra pertence ao Aggregate — sem verificação
 * externa ao Aggregate. O UseCase apenas delega e orquestra.</p>
 *
 * <p><strong>Context Map — integração por eventos:</strong> {@code MatriculaCancelada}
 * é o mecanismo de integração com os Bounded Contexts Financeiro e Acadêmico.
 * Os listeners stub desta Fase 3 registram o evento no console; a Fase 4 os expandirá
 * com chamadas reais aos sistemas externos.</p>
 */
@Service
@Transactional
public class CancelarMatriculaUseCase {

    private final MatriculaRepositorio repositorio;
    private final ApplicationEventPublisher publicador;

    /**
     * Construtor com injeção de dependências.
     *
     * @param repositorio repositório para buscar e persistir a matrícula
     * @param publicador  publicador de eventos Spring — publica após persistência (D-10)
     */
    public CancelarMatriculaUseCase(
            MatriculaRepositorio repositorio,
            ApplicationEventPublisher publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    /**
     * Executa o fluxo de cancelamento de uma matrícula existente.
     *
     * @param command dados necessários: identificador da matrícula a ser cancelada
     * @throws MatriculaNaoEncontradaException                                     se a matrícula não existe
     * @throws br.com.escola.matricula.dominio.excecao.MatriculaCanceladaException se a matrícula já está cancelada
     */
    public void executar(CancelarMatriculaCommand command) {
        // 1. Buscar Aggregate existente
        Matricula matricula = repositorio.buscarPorId(command.matriculaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(command.matriculaId()));

        // 2. Aggregate decide — lança MatriculaCanceladaException se já cancelada
        matricula.cancelar();

        // 3. Persistir (status atualizado para CANCELADA, cancelada_em preenchida)
        repositorio.salvar(matricula);

        // 4. Publicar evento MatriculaCancelada — FinanceiroEventListener e AcademicoEventListener recebem
        // @Transactional + @TransactionalEventListener garante execução após commit
        matricula.coletarEventos().forEach(publicador::publishEvent);
    }
}
