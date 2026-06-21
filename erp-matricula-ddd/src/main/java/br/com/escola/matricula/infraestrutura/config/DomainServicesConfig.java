package br.com.escola.matricula.infraestrutura.config;

import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import br.com.escola.matricula.dominio.servico.VerificadorElegibilidadeMatricula;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração que instancia os Domain Services como beans Spring.
 *
 * <p><strong>Por que não {@code @Service} direto em {@code VerificadorElegibilidadeMatricula}?</strong></p>
 *
 * <p>O Domain Service {@link VerificadorElegibilidadeMatricula} é Java puro — sem nenhuma
 * anotação Spring. Adicionar {@code @Service} em um arquivo do pacote {@code dominio.servico/}
 * importaria {@code org.springframework.stereotype.Service} no domínio, violando o critério de
 * sucesso central da fase:</p>
 *
 * <pre>
 * grep -r "import org.springframework" src/main/java/.../dominio/
 * # → deve retornar VAZIO
 * </pre>
 *
 * <p><strong>Padrão: framework na infra, domínio puro.</strong> A responsabilidade de tornar
 * o Domain Service um bean Spring pertence à camada de infraestrutura. Este {@code @Bean}
 * faz exatamente isso: instancia o serviço (injetando o {@code MatriculaRepositorio}) e
 * o registra no contexto Spring — sem contaminar o domínio com anotações de framework.</p>
 *
 * <p>O {@code MatricularAlunoUseCase} recebe o {@link VerificadorElegibilidadeMatricula}
 * por injeção de construtor como qualquer outro bean — Spring não distingue beans criados
 * via {@code @Bean} de beans criados via {@code @Service}.</p>
 *
 * <p>Referências: DOM-08, Pitfall 8 do RESEARCH.md</p>
 */
@Configuration
public class DomainServicesConfig {

    /**
     * Cria o {@link VerificadorElegibilidadeMatricula} como bean Spring.
     *
     * <p>O {@code MatriculaRepositorio} é injetado aqui pelo Spring (a implementação concreta
     * é {@code MatriculaRepositorioMyBatis}, que tem {@code @Repository}).
     * O Domain Service não sabe que é um bean — apenas recebe o repositório pelo construtor.</p>
     *
     * @param repositorio implementação de {@code MatriculaRepositorio} (injetada pelo Spring)
     * @return instância do Domain Service pronta para injeção nos UseCases
     */
    @Bean
    public VerificadorElegibilidadeMatricula verificadorElegibilidadeMatricula(
            MatriculaRepositorio repositorio) {
        return new VerificadorElegibilidadeMatricula(repositorio);
    }
}
