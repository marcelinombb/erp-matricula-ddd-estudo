package br.com.escola.matricula.dominio.evento;

import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.time.LocalDateTime;
import java.util.UUID;

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
 * <p><strong>Sem interface base (Decisão D-11):</strong> Domain Events são
 * {@code record}s independentes sem interface comum. {@code ApplicationEventPublisher.publishEvent(Object)}
 * aceita qualquer tipo — sem necessidade de abstração prematura.</p>
 */
public record AlunoMatriculado(
    UUID matriculaId,
    UUID alunoId,
    UUID turmaId,
    PeriodoLetivo periodoLetivo,
    LocalDateTime ocorridoEm
) {
}
