package br.com.escola.matricula.dominio.evento;

import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;

import java.time.LocalDateTime;

/**
 * Domain Event publicado quando uma disciplina é adicionada a uma matrícula existente.
 *
 * <p><strong>Quem publica:</strong> {@code AdicionarDisciplinaUseCase}, após chamar
 * {@code repositorio.salvar(matricula)}. O Aggregate {@code Matricula} coleta o evento
 * internamente em {@code adicionarDisciplina()} — o UseCase publica via
 * {@code ApplicationEventPublisher} após a persistência.</p>
 *
 * <p><strong>Quem consome:</strong>
 * <ul>
 *   <li>{@code AcademicoEventListener} — atualiza o registro acadêmico do aluno
 *       com a nova disciplina incluída</li>
 * </ul>
 * </p>
 *
 * <p><strong>Por que é um Domain Event e não um método de callback?</strong></p>
 *
 * <p>O BC Matrícula não deve conhecer como o BC Acadêmico registra disciplinas.
 * O evento desacopla: Matrícula publica o fato "disciplina foi adicionada";
 * Acadêmico decide como reagir. Se Acadêmico mudar sua lógica interna, Matrícula
 * não é afetado.</p>
 *
 * <p><strong>Sem interface base (Decisão D-11):</strong> Domain Events são
 * {@code record}s independentes sem interface comum. Simples e pedagógico.</p>
 */
public record DisciplinaAdicionada(
    MatriculaId matriculaId,
    AlunoId alunoId,
    NomeDisciplina disciplina,
    LocalDateTime ocorridoEm
) {
}
