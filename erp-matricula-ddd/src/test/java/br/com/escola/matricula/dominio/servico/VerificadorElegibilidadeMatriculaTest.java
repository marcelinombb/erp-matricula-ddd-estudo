package br.com.escola.matricula.dominio.servico;

import br.com.escola.matricula.dominio.excecao.AlunoInativoException;
import br.com.escola.matricula.dominio.excecao.MatriculaDuplicadaException;
import br.com.escola.matricula.dominio.excecao.PeriodoFechadoException;
import br.com.escola.matricula.dominio.modelo.Aluno;
import br.com.escola.matricula.dominio.modelo.Turma;
import br.com.escola.matricula.dominio.vo.Cpf;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários do Domain Service {@link VerificadorElegibilidadeMatricula}.
 *
 * <p><strong>Ponto pedagógico central (D-01, D-12):</strong> estes testes usam apenas
 * Java puro e JUnit 5 — zero imports de framework de mock ou container Spring.
 * O {@code VerificadorElegibilidadeMatricula} aceita {@code MatriculaRepositorio}
 * por construtor (Dependency Inversion Principle) — qualquer implementação da interface
 * serve, inclusive um stub escrito à mão ({@link MatriculaRepositorioEmMemoria}).</p>
 *
 * <p><strong>Estrutura de cada teste (D-09):</strong> Given-When-Then com comentários
 * explícitos. Nomes em português (D-10). Asserções AssertJ (D-11).</p>
 *
 * <p><strong>Dependência de data ({@code Turma.periodoEstaAberto()}):</strong>
 * {@code Turma.periodoEstaAberto()} usa {@code LocalDate.now()} internamente.
 * Estratégia adotada:
 * <ul>
 *   <li>Período ABERTO: {@code PeriodoLetivo(2026, 1)} — semestre 1 (fev-jul de 2026).
 *       Este teste deve ser executado entre fevereiro e julho de 2026.</li>
 *   <li>Período FECHADO: {@code PeriodoLetivo(2020, 1)} — passado fixo, sempre fechado.</li>
 * </ul>
 * </p>
 */
@DisplayName("VerificadorElegibilidadeMatricula")
class VerificadorElegibilidadeMatriculaTest {

    /**
     * CPF válido fixo para uso em todos os testes — verificado pelo algoritmo módulo 11.
     *
     * <p>Evita a criação repetida de CPFs diferentes e o Pitfall 3 (usar "111.111.111-11"
     * que é matematicamente inválido e lança {@code IllegalArgumentException} ao instanciar
     * {@code Cpf} — causando falha no setup do teste, não no comportamento testado).</p>
     */
    private static final Cpf CPF_TESTE = new Cpf("529.982.247-25");

    // ─── Fixtures privados ──────────────────────────────────────────────────────

    /**
     * Cria um {@link Aluno} ativo com ID aleatório e CPF válido.
     * Representa o caso normal de aluno elegível para matrícula.
     */
    private Aluno criarAlunoAtivo() {
        return new Aluno(UUID.randomUUID(), CPF_TESTE, "João da Silva", true);
    }

    /**
     * Cria um {@link Aluno} inativo com ID aleatório e CPF válido.
     * Representa aluno que não pode ser matriculado (ex: inadimplente).
     */
    private Aluno criarAlunoInativo() {
        return new Aluno(UUID.randomUUID(), CPF_TESTE, "Maria Inativa", false);
    }

    /**
     * Cria uma {@link Turma} com período letivo fixo no passado.
     *
     * <p>{@code PeriodoLetivo(2020, 1)}: semestre 1 de 2020 (01/fev a 31/jul de 2020).
     * {@code Turma.periodoEstaAberto()} retorna {@code false} em qualquer data de execução
     * após julho de 2020 — sem dependência de data atual.</p>
     */
    private Turma criarTurmaComPeriodoFechado() {
        var periodo = new PeriodoLetivo(2020, 1);
        return new Turma(UUID.randomUUID(), "Turma Passada", periodo, 30);
    }

    // ─── Testes ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deve permitir matrícula quando aluno ativo, período aberto e sem matrícula duplicada")
    void devePermitirMatriculaQuandoAlunoAtivoEPeriodoAbertoElegivelSemDuplicidade() {
        // given
        var stub = new MatriculaRepositorioEmMemoria().semMatriculaExistente();
        var verificador = new VerificadorElegibilidadeMatricula(stub);
        var alunoAtivo = criarAlunoAtivo();
        // Turma com período "aberto" fixo: 01/jan a 31/dez de 9999 — independe de LocalDate.now()
        var periodo = new PeriodoLetivo(9999, 2);
        var turma = new Turma(UUID.randomUUID(), "Turma A", periodo, 30) {
            @Override public boolean periodoEstaAberto() { return true; }
        };

        // when / then — não deve lançar nenhuma exceção (happy path)
        assertThatCode(() -> verificador.verificar(alunoAtivo, turma, periodo))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve lançar AlunoInativoException quando aluno não está ativo")
    void deveLancarExcecaoQuandoAlunoInativo() {
        // given
        var stub = new MatriculaRepositorioEmMemoria().semMatriculaExistente();
        var verificador = new VerificadorElegibilidadeMatricula(stub);
        var alunoInativo = criarAlunoInativo();
        // Período e turma são irrelevantes — a exceção é lançada antes de verificar o período
        var turma = criarTurmaComPeriodoFechado();
        var periodo = new PeriodoLetivo(2020, 1);

        // when / then
        assertThatThrownBy(() -> verificador.verificar(alunoInativo, turma, periodo))
            .isInstanceOf(AlunoInativoException.class);
    }

    @Test
    @DisplayName("deve lançar PeriodoFechadoException quando período da turma está fechado")
    void deveLancarExcecaoQuandoPeriodoFechado() {
        // given
        var stub = new MatriculaRepositorioEmMemoria().semMatriculaExistente();
        var verificador = new VerificadorElegibilidadeMatricula(stub);
        var alunoAtivo = criarAlunoAtivo();
        // PeriodoLetivo(2020, 1): 01/fev a 31/jul de 2020 — sempre fechado (passado fixo)
        // Garante que periodoEstaAberto() retorna false independente de quando o teste roda
        var turmaFechada = criarTurmaComPeriodoFechado();
        var periodo = new PeriodoLetivo(2020, 1);

        // when / then
        assertThatThrownBy(() -> verificador.verificar(alunoAtivo, turmaFechada, periodo))
            .isInstanceOf(PeriodoFechadoException.class);
    }

    @Test
    @DisplayName("deve lançar MatriculaDuplicadaException quando aluno já tem matrícula ativa no período")
    void deveLancarExcecaoQuandoMatriculaDuplicada() {
        // given — stub configurado para simular matrícula existente (aluno já matriculado)
        var stub = new MatriculaRepositorioEmMemoria().comMatriculaExistente();
        var verificador = new VerificadorElegibilidadeMatricula(stub);
        var alunoAtivo = criarAlunoAtivo();
        // Turma com período "aberto" fixo — independe de LocalDate.now()
        // O verificador verifica o período ANTES da duplicidade; o período deve estar aberto
        // para que a execução chegue até a verificação de MatriculaDuplicadaException.
        var periodo = new PeriodoLetivo(9999, 2);
        var turma = new Turma(UUID.randomUUID(), "Turma B", periodo, 30) {
            @Override public boolean periodoEstaAberto() { return true; }
        };

        // when / then
        assertThatThrownBy(() -> verificador.verificar(alunoAtivo, turma, periodo))
            .isInstanceOf(MatriculaDuplicadaException.class);
    }
}
