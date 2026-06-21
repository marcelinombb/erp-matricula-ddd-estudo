package br.com.escola.matricula.dominio.evento;

import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.time.LocalDateTime;

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
 *   <li>{@code FinanceiroEventListener} — encerra o contrato de cobrança do período,
 *       processa estornos ou multas conforme as regras financeiras</li>
 *   <li>{@code AcademicoEventListener} — remove o vínculo do aluno com a turma,
 *       atualiza o registro acadêmico</li>
 * </ul>
 * </p>
 *
 * <p><strong>Por que é um Domain Event e não um método de callback?</strong></p>
 *
 * <p>Cancelar uma matrícula tem consequências em dois Bounded Contexts distintos.
 * Chamar Financeiro e Acadêmico diretamente no UseCase de Matrícula criaria
 * acoplamento transversal — o BC Matrícula saberia dos internos de dois outros BCs.
 * Com Domain Events, Matrícula apenas anuncia o fato; os outros BCs decidem como
 * reagir independentemente.</p>
 *
 * <p><strong>Sem interface base (Decisão D-11):</strong> Domain Events são
 * {@code record}s independentes sem interface comum. Simples e pedagógico.</p>
 */
public record MatriculaCancelada(
    MatriculaId matriculaId,
    AlunoId alunoId,
    PeriodoLetivo periodoLetivo,
    LocalDateTime ocorridoEm
) {
}
