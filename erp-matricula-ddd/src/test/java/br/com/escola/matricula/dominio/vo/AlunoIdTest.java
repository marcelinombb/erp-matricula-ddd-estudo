package br.com.escola.matricula.dominio.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Testes unitários do Value Object {@link AlunoId}.
 *
 * <p>Demonstra o padrão de ID tipado (ADR-003): o compilador distingue
 * {@code AlunoId} de {@code TurmaId}, impossibilitando inversão de parâmetros
 * sem erro de compilação. O construtor rejeita {@code null} com
 * {@link NullPointerException} — "objeto que existe = objeto válido".</p>
 *
 * <p>Zero imports Spring — o domínio é testável sem nenhum framework de contêiner.</p>
 */
@DisplayName("AlunoId")
class AlunoIdTest {

    @Test
    @DisplayName("deve criar AlunoId válido com UUID fornecido")
    void deveCriarAlunoIdValido() {
        // given
        var uuid = UUID.randomUUID();

        // when
        var id = new AlunoId(uuid);

        // then
        assertThat(id.valor()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("deve lançar NullPointerException para valor nulo")
    void deveLancarExcecaoParaValorNulo() {
        // AlunoId usa Objects.requireNonNull — lança NullPointerException
        assertThatNullPointerException()
                .isThrownBy(() -> new AlunoId(null))
                .withMessageContaining("nulo");
    }

    @Test
    @DisplayName("dois AlunoId com mesmo UUID devem ser iguais")
    void doisComMesmoUuidDevemSerIguais() {
        // given
        var uuid = UUID.randomUUID();

        // then — record Java 21: equals/hashCode por valor do componente UUID
        assertThat(new AlunoId(uuid)).isEqualTo(new AlunoId(uuid));
    }
}
