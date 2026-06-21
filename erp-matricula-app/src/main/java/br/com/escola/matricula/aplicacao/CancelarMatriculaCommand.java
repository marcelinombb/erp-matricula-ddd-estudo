package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.vo.MatriculaId;

/**
 * Command para {@code CancelarMatriculaUseCase}.
 *
 * <p>Carrega o identificador da matrícula a ser cancelada.
 * É o Command mais simples do sistema — cancelar requer apenas a identidade.</p>
 *
 * <p>Na Fase 4, o Controller receberá o UUID da matrícula via path parameter
 * (ex: {@code DELETE /matriculas/{id}}), construirá o {@code MatriculaId}
 * e criará este Command.</p>
 *
 * @param matriculaId identificador da matrícula a ser cancelada
 */
public record CancelarMatriculaCommand(MatriculaId matriculaId) {
}
