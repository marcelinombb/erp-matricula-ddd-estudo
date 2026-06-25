package br.com.escola.matricula.dominio.evento;

import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event publicado quando uma matrícula é cancelada.
 *
 * <p><strong>Quem publica:</strong> {@code CancelarMatriculaUseCase}, após chamar
 * {@code repositorio.salvar(matricula)} com o status atualizado para {@code Cancelada}.
 * O Aggregate {@code Matricula} coleta o evento internamente em {@code cancelar()} —
 * o UseCase publica via {@code ApplicationEventPublisher} após a persistência.</p>
 *
 * <p><strong>Quem consome:</strong>
 * <ul>
 *   <li>{@code FinanceiroEventListener} — encerra o contrato de cobrança do período</li>
 *   <li>{@code AcademicoEventListener} — remove o vínculo do aluno com a turma</li>
 * </ul>
 * </p>
 *
 * <p><strong>Sem interface base (Decisão D-11):</strong> Domain Events são
 * {@code record}s independentes sem interface comum. Simples e pedagógico.</p>
 */
public record MatriculaCancelada(
    UUID matriculaId,
    UUID alunoId,
    PeriodoLetivo periodoLetivo,
    LocalDateTime ocorridoEm
) {
}
