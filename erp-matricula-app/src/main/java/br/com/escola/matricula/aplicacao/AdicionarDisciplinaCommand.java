package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;

/**
 * Command para {@code AdicionarDisciplinaUseCase}.
 *
 * <p>Carrega os dados necessários para adicionar uma disciplina a uma matrícula existente:
 * o identificador da matrícula e o nome da disciplina a ser adicionada.</p>
 *
 * <p>Na Fase 4, o Controller receberá o UUID da matrícula e o nome da disciplina
 * como strings no request HTTP, construirá os Value Objects ({@code MatriculaId},
 * {@code NomeDisciplina}) e criará este Command para o UseCase.</p>
 *
 * @param matriculaId identificador da matrícula que receberá a disciplina
 * @param disciplina  nome da disciplina a ser adicionada
 */
public record AdicionarDisciplinaCommand(
        MatriculaId matriculaId,
        NomeDisciplina disciplina) {
}
