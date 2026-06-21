package br.com.escola.matricula.dominio.excecao;

import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

/**
 * Exceção lançada quando se tenta matricular em um período letivo fechado.
 *
 * <p>A verificação de período aberto é feita pelo {@code VerificadorElegibilidadeMatricula}
 * via {@code Turma.periodoEstaAberto()} antes de criar a matrícula.</p>
 *
 * <p>Lançada por: {@code VerificadorElegibilidadeMatricula.verificar()} quando
 * {@code !turma.periodoEstaAberto()}.</p>
 */
public class PeriodoFechadoException extends RuntimeException {

    /** Período letivo que está fechado. */
    private final PeriodoLetivo periodo;

    /**
     * Constrói a exceção com o período letivo fechado.
     *
     * @param periodo período letivo que não está aberto para matrícula
     */
    public PeriodoFechadoException(PeriodoLetivo periodo) {
        super("Período " + periodo.ano() + "/" + periodo.semestre()
            + " não está aberto para matrícula");
        this.periodo = periodo;
    }

    /** Retorna o período letivo fechado. */
    public PeriodoLetivo getPeriodo() {
        return periodo;
    }
}
