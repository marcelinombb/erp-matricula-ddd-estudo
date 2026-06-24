package br.com.escola.matricula.dominio.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários do Value Object {@link Cpf}.
 *
 * <p>Demonstra o princípio "objeto que existe = objeto válido": qualquer
 * tentativa de criar um {@code Cpf} inválido lança exceção antes que
 * o objeto exista. CPFs válidos são armazenados normalizados (sem máscara),
 * independentemente do formato recebido.</p>
 *
 * <p>Zero imports Spring — o domínio é testável sem nenhum framework de contêiner.</p>
 */
@DisplayName("Cpf")
class CpfTest {

    // CPF válido verificado contra o algoritmo módulo 11 implementado em Cpf.java
    private static final String CPF_VALIDO_COM_MASCARA = "529.982.247-25";
    private static final String CPF_VALIDO_NORMALIZADO = "52998224725";

    @Test
    @DisplayName("deve normalizar CPF removendo máscara ao criar")
    void deveNormalizarCpfRemovendoMascara() {
        // given / when
        var cpf = new Cpf(CPF_VALIDO_COM_MASCARA);

        // then
        assertThat(cpf.valor()).isEqualTo(CPF_VALIDO_NORMALIZADO);
    }

    @Test
    @DisplayName("deve retornar CPF formatado com máscara de exibição")
    void deveRetornarCpfFormatado() {
        // given / when
        var cpf = new Cpf(CPF_VALIDO_NORMALIZADO);

        // then
        assertThat(cpf.formatado()).isEqualTo(CPF_VALIDO_COM_MASCARA);
    }

    @Test
    @DisplayName("deve lançar exceção para CPF com dígito verificador inválido")
    void deveLancarExcecaoParaCpfComDigitoVerificadorInvalido() {
        // CPF "111.111.111-11" tem todos os dígitos iguais — rejeitado pelo algoritmo
        assertThatThrownBy(() -> new Cpf("111.111.111-11"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("dígito verificador inválido");
    }

    @Test
    @DisplayName("dois Cpf com mesmo valor devem ser iguais após normalização")
    void doisCpfComMesmoValorDevemSerIguais() {
        // given — um com máscara, outro sem; ambos normalizam para o mesmo valor
        var cpfComMascara = new Cpf(CPF_VALIDO_COM_MASCARA);
        var cpfNormalizado = new Cpf(CPF_VALIDO_NORMALIZADO);

        // then — record Java 21: equals/hashCode por valor do componente normalizado
        assertThat(cpfComMascara).isEqualTo(cpfNormalizado);
    }
}
