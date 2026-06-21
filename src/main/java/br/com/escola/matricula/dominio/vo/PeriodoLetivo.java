package br.com.escola.matricula.dominio.vo;

/**
 * Value Object que representa um período letivo como ano e semestre.
 *
 * <p><strong>Decisão de design:</strong> O VO mantém {@code (ano, semestre)} — tipos inteiros —
 * em vez de {@code (LocalDate inicio, LocalDate fim)}. Essa decisão preserva a expressividade
 * do domínio: a Secretaria fala em "1º semestre de 2026", não em "01/02/2026 a 31/07/2026".</p>
 *
 * <p><strong>Mapeamento para banco de dados:</strong> A conversão de {@code PeriodoLetivo}
 * para as colunas {@code periodo_inicio DATE} e {@code periodo_fim DATE} do PostgreSQL é
 * responsabilidade do {@code MatriculaRowMapper} na camada de infraestrutura. O VO não
 * conhece o schema — isso é um detalhe de persistência. Ver {@code MatriculaRowMapper.toDomain()}.
 * (Decisão D-14 e Opção C do RESEARCH.md da Fase 3.)</p>
 *
 * <p>Dois {@code PeriodoLetivo(2026, 1)} são iguais — o {@code record} Java 21 compara
 * por valor automaticamente. Isso é o comportamento correto para Value Objects.</p>
 */
public record PeriodoLetivo(int ano, int semestre) {

    /**
     * Construtor compacto com validações de negócio.
     *
     * @throws IllegalArgumentException se ano menor que 2000 ou semestre fora de {1, 2}
     */
    public PeriodoLetivo {
        if (ano < 2000) {
            throw new IllegalArgumentException(
                "Ano do período letivo inválido: " + ano + ". Mínimo: 2000."
            );
        }
        if (semestre < 1 || semestre > 2) {
            throw new IllegalArgumentException(
                "Semestre inválido: " + semestre + ". Valores permitidos: 1 ou 2."
            );
        }
    }

    /**
     * Retorna a descrição canônica do período, usada em exibição e logs.
     *
     * @return string no formato "ano-semestre" (ex: "2026-1")
     */
    public String descricao() {
        return ano + "-" + semestre;
    }
}
