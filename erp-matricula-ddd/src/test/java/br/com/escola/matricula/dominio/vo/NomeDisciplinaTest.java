package br.com.escola.matricula.dominio.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários do Value Object {@link NomeDisciplina}.
 *
 * <p>Demonstra que VOs Java 21 (record com validação no construtor compacto) são
 * testáveis sem Spring ou banco — apenas {@code new + assertThat}.</p>
 *
 * <p>Sem nenhum import de framework Spring — evidência de que o domínio
 * é completamente independente do framework (D-12).</p>
 */
@DisplayName("NomeDisciplina")
class NomeDisciplinaTest {

    @Test
    @DisplayName("deve normalizar espaços ao criar NomeDisciplina")
    void deveNormalizarEspacosAoCriar() {
        // given / when
        var nome = new NomeDisciplina("  Matemática  ");

        // then
        assertThat(nome.valor()).isEqualTo("Matemática");
    }

    @Test
    @DisplayName("deve lançar exceção para nome em branco")
    void deveLancarExcecaoParaNomeEmBranco() {
        // given / when / then
        assertThatThrownBy(() -> new NomeDisciplina("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("em branco");
    }

    @Test
    @DisplayName("deve lançar exceção para nome com mais de 100 caracteres")
    void deveLancarExcecaoParaNomeAcimaDe100Chars() {
        // given
        var nomeInvalido = "A".repeat(101);

        // when / then
        assertThatThrownBy(() -> new NomeDisciplina(nomeInvalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("100 caracteres");
    }

    @Test
    @DisplayName("deve aceitar nome com exatamente 100 caracteres")
    void deveAceitarNomeComExatamente100Chars() {
        // given
        var nome100Chars = "A".repeat(100);

        // when
        var vo = new NomeDisciplina(nome100Chars);

        // then
        assertThat(vo.valor()).hasSize(100);
    }

    @Test
    @DisplayName("dois NomeDisciplina com mesmo valor devem ser iguais")
    void doisComMesmoValorDevemSerIguais() {
        // given / when
        var a = new NomeDisciplina("Matemática");
        var b = new NomeDisciplina("Matemática");

        // then
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
