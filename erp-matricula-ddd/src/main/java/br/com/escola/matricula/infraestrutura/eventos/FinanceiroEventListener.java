package br.com.escola.matricula.infraestrutura.eventos;

import br.com.escola.matricula.dominio.evento.AlunoMatriculado;
import br.com.escola.matricula.dominio.evento.MatriculaCancelada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener stub do Bounded Context Financeiro.
 *
 * <p><strong>Por que {@code @TransactionalEventListener} e não {@code @EventListener}?</strong></p>
 *
 * <p>Com {@code @EventListener}: o listener executa <em>dentro</em> da transação do UseCase.
 * Se a transação fizer rollback DEPOIS do listener ter executado (ex: erro no commit),
 * o BC Financeiro já processou um evento de uma operação que foi desfeita — dados inconsistentes.</p>
 *
 * <p>Com {@code @TransactionalEventListener} (default: {@code AFTER_COMMIT}): o listener só
 * executa se e quando a transação <em>commitou</em> com sucesso. O evento representa um
 * <strong>fato persistido</strong> — o BC Financeiro pode confiar que a matrícula existe no banco.</p>
 *
 * <p><strong>Implementação stub:</strong> este listener registra no log para demonstração.
 * A implementação real (criar contrato de cobrança, processar pagamento) é responsabilidade
 * da v2 do projeto (BC-01 — Bounded Context Financeiro), que consumiria estes eventos via
 * mensageria (Kafka, RabbitMQ) ou via polling de uma tabela de outbox.</p>
 *
 * <p>Referências: APL-05, D-10, RESEARCH.md Seção 8</p>
 */
@Component
public class FinanceiroEventListener {

    private static final Logger log = LoggerFactory.getLogger(FinanceiroEventListener.class);

    /**
     * Processa o evento de aluno matriculado para o BC Financeiro.
     *
     * <p>Executado após commit — a matrícula já existe no banco quando este método roda.
     * Implementação real: criar contrato de cobrança para o período letivo.</p>
     *
     * @param evento evento publicado por {@code MatricularAlunoUseCase} após persistência
     */
    @TransactionalEventListener
    public void aoMatricular(AlunoMatriculado evento) {
        log.info("[BC Financeiro] Criando contrato de cobrança para matrícula {}",
                 evento.matriculaId().valor());
        // Stub — implementação real: Fase v2, BC-01 (Bounded Context Financeiro)
    }

    /**
     * Processa o evento de matrícula cancelada para o BC Financeiro.
     *
     * <p>Executado após commit — o cancelamento já foi persistido.
     * Implementação real: processar estorno ou encerramento de cobrança.</p>
     *
     * @param evento evento publicado por {@code CancelarMatriculaUseCase} após persistência
     */
    @TransactionalEventListener
    public void aoCancelar(MatriculaCancelada evento) {
        log.info("[BC Financeiro] Processando cancelamento de cobrança para matrícula {}",
                 evento.matriculaId().valor());
        // Stub — implementação real: Fase v2, BC-01
    }
}
