package br.com.escola.matricula.infraestrutura.eventos;

import br.com.escola.matricula.dominio.evento.AlunoMatriculado;
import br.com.escola.matricula.dominio.evento.DisciplinaAdicionada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener stub do Bounded Context Acadêmico.
 *
 * <p>Mesmo padrão do {@link FinanceiroEventListener}: usa {@code @TransactionalEventListener}
 * para garantir execução após commit — o evento representa um fato persistido.</p>
 *
 * <p><strong>Implementação stub:</strong> registra no log para demonstração.
 * A implementação real (registrar vínculo aluno-turma, atualizar histórico acadêmico)
 * é responsabilidade da v2 do projeto (BC-02 — Bounded Context Acadêmico).</p>
 *
 * <p>Referências: APL-05, D-10, RESEARCH.md Seção 8</p>
 */
@Component
public class AcademicoEventListener {

    private static final Logger log = LoggerFactory.getLogger(AcademicoEventListener.class);

    /**
     * Processa o evento de aluno matriculado para o BC Acadêmico.
     *
     * <p>Executado após commit. Implementação real: registrar vínculo aluno-turma
     * no sistema acadêmico, gerar lista de chamada.</p>
     *
     * @param evento evento publicado por {@code MatricularAlunoUseCase} após persistência
     */
    @TransactionalEventListener
    public void aoMatricular(AlunoMatriculado evento) {
        log.info("[BC Academico] Registrando vínculo aluno-turma para matrícula {}",
                 evento.matriculaId());
        // Stub — implementação real: Fase v2, BC-02 (Bounded Context Acadêmico)
    }

    /**
     * Processa o evento de disciplina adicionada para o BC Acadêmico.
     *
     * <p>Executado após commit. Implementação real: atualizar histórico acadêmico do aluno,
     * registrar inscrição na disciplina.</p>
     *
     * @param evento evento publicado por {@code AdicionarDisciplinaUseCase} após persistência
     */
    @TransactionalEventListener
    public void aoDisciplinaAdicionada(DisciplinaAdicionada evento) {
        log.info("[BC Academico] Atualizando histórico: disciplina {} adicionada à matrícula {}",
                 evento.disciplina().valor(), evento.matriculaId());
        // Stub — implementação real: Fase v2, BC-02
    }
}
