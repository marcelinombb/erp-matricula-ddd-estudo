package br.com.escola.matricula.dominio.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários do Value Object {@link PeriodoLetivo}.
 *
 * <p>Demonstra que VOs Java 21 (record imutável com validações no construtor compacto)
 * são testáveis sem Spring ou banco — apenas {@code new + assertThat}.</p>
 *
 * <p>Zero imports de framework Spring — evidência de que o domínio
 * é completamente independente do framework (D-12).</p>
 */
@DisplayName("PeriodoLetivo")
class PeriodoLetivoTest {

    @Test
    @DisplayName("deve criar PeriodoLetivo válido com ano e semestre corretos")
    void deveCriarPeriodoLetivoValido() {
        // given / when
        var periodo = new PeriodoLetivo(2026, 1);

        // then
        assertThat(periodo.ano()).isEqualTo(2026);
        assertThat(periodo.semestre()).isEqualTo(1);
        assertThat(periodo.descricao()).isEqualTo("2026-1");
    }

    @Test
    @DisplayName("deve lançar exceção para ano anterior a 2000")
    void deveLancarExcecaoParaAnoAnteriorA2000() {
        // given / when / then
        assertThatThrownBy(() -> new PeriodoLetivo(1999, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("2000");
    }

    @Test
    @DisplayName("deve lançar exceção para semestre zero")
    void deveLancarExcecaoParaSemestreZero() {
        // given / when / then
        assertThatThrownBy(() -> new PeriodoLetivo(2026, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Semestre inválido");
    }

    @Test
    @DisplayName("deve lançar exceção para semestre três")
    void deveLancarExcecaoParaSemestreTres() {
        // given / when / then
        assertThatThrownBy(() -> new PeriodoLetivo(2026, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Semestre inválido");
    }
}
