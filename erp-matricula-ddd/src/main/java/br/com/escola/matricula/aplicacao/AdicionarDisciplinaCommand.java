package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.vo.NomeDisciplina;

import java.util.UUID;

/**
 * Command para {@code AdicionarDisciplinaUseCase}.
 *
 * @param matriculaId UUID da matrícula que receberá a disciplina
 * @param disciplina  nome da disciplina a ser adicionada
 */
public record AdicionarDisciplinaCommand(
        UUID matriculaId,
        NomeDisciplina disciplina) {
}
