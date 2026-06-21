package br.com.escola.matricula.dominio.servico;

import br.com.escola.matricula.dominio.excecao.AlunoInativoException;
import br.com.escola.matricula.dominio.excecao.MatriculaDuplicadaException;
import br.com.escola.matricula.dominio.excecao.PeriodoFechadoException;
import br.com.escola.matricula.dominio.modelo.Aluno;
import br.com.escola.matricula.dominio.modelo.Turma;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.util.Objects;

/**
 * Domain Service — verifica se um aluno pode ser matriculado em uma turma.
 *
 * <p><strong>Por que este serviço existe?</strong></p>
 *
 * <p>A verificação de elegibilidade envolve três entidades:
 * <ul>
 *   <li>{@code Aluno} — precisa estar ativo</li>
 *   <li>{@code Turma} — período letivo precisa estar aberto</li>
 *   <li>{@code MatriculaRepositorio} — não pode existir matrícula ativa para o aluno no período</li>
 * </ul>
 * Nenhuma entidade sozinha tem acesso a todas as três informações. A lógica não pertence
 * a nenhum Aggregate específico — por isso vive em um Domain Service.</p>
 *
 * <p><strong>Sem {@code @Service}, sem {@code @Component}:</strong> esta é uma classe
 * Java pura no pacote {@code dominio/}. O Spring a instancia via {@code @Bean} em
 * {@code infraestrutura/config/DominioConfig} — sem que o domínio saiba que o Spring
 * existe. Ver RESEARCH.md Pitfall 8 e D-08 em 03-CONTEXT.md.</p>
 *
 * <p>Exemplo de configuração Spring (na infraestrutura, não aqui):
 * <pre>{@code
 * // infraestrutura/config/DominioConfig.java
 * @Configuration
 * public class DominioConfig {
 *     @Bean
 *     public VerificadorElegibilidadeMatricula verificador(MatriculaRepositorio repo) {
 *         return new VerificadorElegibilidadeMatricula(repo);
 *     }
 * }
 * }</pre>
 * </p>
 *
 * <p><strong>Domain Service vs Application Service:</strong> este serviço vive em
 * {@code dominio/} porque encapsula uma regra de negócio com nome reconhecível pela
 * Secretaria ("verificar elegibilidade para matrícula"). Não contém orquestração —
 * não busca dados, não salva, não publica eventos. Apenas verifica e lança exceção
 * se alguma regra for violada. Ver docs/02-design-tatico/domain-services.md.</p>
 */
public class VerificadorElegibilidadeMatricula {

    /**
     * Repositório de matrículas — interface de domínio, sem import de framework.
     * A implementação concreta (MyBatis) é injetada pelo Spring na infraestrutura.
     */
    private final MatriculaRepositorio repositorio;

    /**
     * Constrói o verificador com o repositório por injeção de construtor.
     *
     * <p>Injeção por construtor (não {@code @Autowired}) — o domínio não deve ter
     * anotações de framework. O Spring injeta via construtor automaticamente
     * quando configurado com {@code @Bean}.</p>
     *
     * @param repositorio repositório de matrículas (nunca nulo)
     */
    public VerificadorElegibilidadeMatricula(MatriculaRepositorio repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio,
            "Repositório de matrículas não pode ser nulo");
    }

    /**
     * Verifica se o aluno está elegível para matrícula na turma especificada.
     *
     * <p><strong>Verificações realizadas em ordem:</strong>
     * <ol>
     *   <li>Aluno está ativo? Se não → {@link AlunoInativoException}</li>
     *   <li>Período da turma está aberto? Se não → {@link PeriodoFechadoException}</li>
     *   <li>Aluno já tem matrícula ativa neste período? Se sim → {@link MatriculaDuplicadaException}</li>
     * </ol>
     * </p>
     *
     * <p>Se todas as verificações passarem, o método retorna normalmente e o UseCase
     * pode prosseguir com a criação da matrícula.</p>
     *
     * @param aluno  aluno a ser matriculado
     * @param turma  turma na qual o aluno será matriculado
     * @param periodo período letivo da matrícula
     * @throws AlunoInativoException     se o aluno não está ativo
     * @throws PeriodoFechadoException   se o período letivo da turma não está aberto
     * @throws MatriculaDuplicadaException se o aluno já tem matrícula ativa no período
     */
    public void verificar(Aluno aluno, Turma turma, PeriodoLetivo periodo) {
        if (!aluno.estaAtivo()) {
            throw new AlunoInativoException(aluno.getId());
        }

        if (!turma.periodoEstaAberto()) {
            throw new PeriodoFechadoException(periodo);
        }

        if (repositorio.existeMatriculaAtiva(aluno.getId(), periodo)) {
            throw new MatriculaDuplicadaException(aluno.getId(), periodo);
        }
    }
}
