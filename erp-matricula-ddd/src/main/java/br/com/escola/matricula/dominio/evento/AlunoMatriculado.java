package br.com.escola.matricula.dominio.evento;

import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import br.com.escola.matricula.dominio.vo.TurmaId;

import java.time.LocalDateTime;

/**
 * Domain Event publicado quando um aluno é matriculado com sucesso.
 *
 * <p><strong>Quem publica:</strong> {@code MatricularAlunoUseCase}, após chamar
 * {@code repositorio.salvar(matricula)} e antes de retornar o resultado.
 * A sequência obrigatória é: salvar → coletarEventos → publishEvent.
 * Isso garante que o evento é publicado apenas se a persistência teve sucesso.</p>
 *
 * <p><strong>Quem consome:</strong>
 * <ul>
 *   <li>{@code FinanceiroEventListener} — cria o contrato de cobrança para o período</li>
 *   <li>{@code AcademicoEventListener} — registra o vínculo do aluno com a turma</li>
 * </ul>
 * Os listeners usam {@code @TransactionalEventListener}, garantindo que só executam
 * após o commit da transação que salvou a matrícula.</p>
 *
 * <p><strong>Por que é um Domain Event e não um método de callback?</strong></p>
 *
 * <p>Um callback direto criaria acoplamento: {@code MatricularAlunoUseCase} precisaria
 * conhecer as APIs internas de Financeiro e Acadêmico. Se um deles mudar, o UseCase
 * quebra. Um Domain Event inverte isso: o BC Matrícula publica um fato; os consumidores
 * decidem como reagir. O publicador não sabe que os consumidores existem.</p>
 *
 * <p><strong>Por que os campos são IDs e VOs, não objetos completos?</strong></p>
 *
 * <p>Um evento que carrega um objeto mutável ({@code Aluno}, {@code Matricula}) pode
 * ser modificado após a publicação, tornando-o um fato histórico alterável —
 * contradição. IDs e VOs imutáveis garantem que o evento é um fato imutável.</p>
 *
 * <p><strong>Sem interface base (Decisão D-11):</strong> Domain Events são
 * {@code record}s independentes sem interface comum. {@code ApplicationEventPublisher.publishEvent(Object)}
 * aceita qualquer tipo — sem necessidade de abstração prematura.</p>
 */
public record AlunoMatriculado(
    MatriculaId matriculaId,
    AlunoId alunoId,
    TurmaId turmaId,
    PeriodoLetivo periodoLetivo,
    LocalDateTime ocorridoEm
) {
}
