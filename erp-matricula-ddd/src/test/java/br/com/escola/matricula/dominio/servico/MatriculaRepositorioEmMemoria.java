package br.com.escola.matricula.dominio.servico;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.util.List;
import java.util.Optional;

/**
 * Stub in-memory de {@link MatriculaRepositorio} para uso exclusivo em testes unitários.
 *
 * <p><strong>Por que stub escrito à mão em vez de Mockito? (D-01)</strong></p>
 *
 * <p>Mockito funcionaria — {@code Mockito.mock(MatriculaRepositorio.class)} é perfeitamente
 * válido. Mas esta classe demonstra um ponto pedagógico central: o domínio DDD é testável
 * sem nenhum framework de mock. A interface {@code MatriculaRepositorio} é uma porta
 * definida no domínio; qualquer classe Java que a implemente serve como substituto em testes.
 * O desenvolvedor não precisa aprender Mockito para testar lógica de domínio.</p>
 *
 * <p><strong>Visibilidade package-private (D-02):</strong> sem modificador {@code public}
 * — esta classe só é acessível ao {@code VerificadorElegibilidadeMatriculaTest} no mesmo
 * pacote ({@code br.com.escola.matricula.dominio.servico} em {@code src/test/java}).
 * Testes de outros pacotes não podem usar este stub — cada pacote define seu próprio
 * suporte de teste, evitando dependência cruzada entre testes.</p>
 *
 * <p><strong>API fluente de configuração:</strong> os métodos {@link #comMatriculaExistente()}
 * e {@link #semMatriculaExistente()} permitem configurar o comportamento do stub no
 * próprio {@code // given} do teste:
 * <pre>{@code
 * var stub = new MatriculaRepositorioEmMemoria().comMatriculaExistente();
 * var verificador = new VerificadorElegibilidadeMatricula(stub);
 * }</pre>
 * </p>
 *
 * <p><strong>Métodos não usados lançam {@code UnsupportedOperationException}:</strong>
 * apenas {@code existeMatriculaAtiva()} é usado pelo {@code VerificadorElegibilidadeMatricula}.
 * Os demais métodos da interface ({@code buscarPorId}, {@code buscarPorAluno}, {@code salvar})
 * lançam {@code UnsupportedOperationException} — se o stub for chamado em um contexto
 * inesperado, o teste falha com uma mensagem clara em vez de retornar silenciosamente
 * um valor padrão.</p>
 */
class MatriculaRepositorioEmMemoria implements MatriculaRepositorio {

    /**
     * Campo configurável que controla o retorno de {@link #existeMatriculaAtiva}.
     * Valor padrão {@code false}: sem matrícula existente (estado mais comum nos testes).
     */
    private boolean existeMatriculaAtiva = false;

    /**
     * Configura o stub para retornar {@code true} em {@link #existeMatriculaAtiva}.
     * Simula o cenário em que o aluno já tem matrícula ativa no período.
     *
     * @return {@code this} para encadeamento fluente no {@code // given} do teste
     */
    MatriculaRepositorioEmMemoria comMatriculaExistente() {
        this.existeMatriculaAtiva = true;
        return this;
    }

    /**
     * Configura o stub para retornar {@code false} em {@link #existeMatriculaAtiva}.
     * Simula o cenário em que o aluno ainda não tem matrícula ativa no período.
     *
     * @return {@code this} para encadeamento fluente no {@code // given} do teste
     */
    MatriculaRepositorioEmMemoria semMatriculaExistente() {
        this.existeMatriculaAtiva = false;
        return this;
    }

    /**
     * Retorna o valor configurado por {@link #comMatriculaExistente()} ou
     * {@link #semMatriculaExistente()}. Este é o único método realmente usado pelo
     * {@code VerificadorElegibilidadeMatricula}.
     *
     * @param alunoId identificador do aluno (ignorado no stub — o comportamento é controlado
     *                pelo campo {@code existeMatriculaAtiva})
     * @param periodo período letivo a verificar (ignorado no stub)
     * @return {@code true} se configurado com {@link #comMatriculaExistente()},
     *         {@code false} se configurado com {@link #semMatriculaExistente()}
     */
    @Override
    public boolean existeMatriculaAtiva(AlunoId alunoId, PeriodoLetivo periodo) {
        return this.existeMatriculaAtiva;
    }

    /**
     * Não implementado neste stub — o {@code VerificadorElegibilidadeMatricula}
     * não usa este método.
     *
     * @throws UnsupportedOperationException sempre — indica uso inesperado do stub
     */
    @Override
    public Optional<Matricula> buscarPorId(MatriculaId id) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }

    /**
     * Não implementado neste stub — o {@code VerificadorElegibilidadeMatricula}
     * não usa este método.
     *
     * @throws UnsupportedOperationException sempre — indica uso inesperado do stub
     */
    @Override
    public List<Matricula> buscarPorAluno(AlunoId alunoId) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }

    /**
     * Não implementado neste stub — o {@code VerificadorElegibilidadeMatricula}
     * não usa este método.
     *
     * @throws UnsupportedOperationException sempre — indica uso inesperado do stub
     */
    @Override
    public void salvar(Matricula matricula) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }
}
