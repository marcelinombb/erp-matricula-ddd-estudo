package br.com.escola.matricula.aplicacao;

import java.util.UUID;

/**
 * Command para {@code CancelarMatriculaUseCase}.
 *
 * @param matriculaId UUID da matrícula a ser cancelada
 */
public record CancelarMatriculaCommand(UUID matriculaId) {
}
