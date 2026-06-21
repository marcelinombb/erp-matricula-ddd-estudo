package br.com.escola.matricula.infraestrutura.config;

import br.com.escola.matricula.aplicacao.AdicionarDisciplinaCommand;
import br.com.escola.matricula.aplicacao.AdicionarDisciplinaUseCase;
import br.com.escola.matricula.aplicacao.CancelarMatriculaCommand;
import br.com.escola.matricula.aplicacao.CancelarMatriculaUseCase;
import br.com.escola.matricula.aplicacao.MatricularAlunoCommand;
import br.com.escola.matricula.aplicacao.MatricularAlunoUseCase;
import br.com.escola.matricula.dominio.modelo.Aluno;
import br.com.escola.matricula.dominio.modelo.Turma;
import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.Cpf;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import br.com.escola.matricula.dominio.vo.TurmaId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * CommandLineRunner de demonstração — executa os 3 fluxos de negócio no startup.
 *
 * <p>Usa dados dos seeds ({@code V2__seeds.sql}) para executar os fluxos com dados reais
 * persistidos. Permite verificar o funcionamento completo sem Controllers HTTP.</p>
 *
 * <p><strong>Quando executar:</strong> {@code mvn spring-boot:run} com PostgreSQL disponível
 * em {@code localhost:5432/erp_matricula}. O Flyway aplica V1 e V2 automaticamente;
 * este runner executa os 3 fluxos logo após.</p>
 *
 * <p><strong>Fluxos executados:</strong>
 * <ol>
 *   <li><em>Matricular aluno:</em> Maria Silva (a0...001) matrícula na Turma 2026-1 (b0...001)</li>
 *   <li><em>Adicionar disciplina:</em> Matrícula pré-existente (c0...001) recebe "Física Quântica"</li>
 *   <li><em>Cancelar matrícula:</em> A matrícula criada no Fluxo 1 é cancelada</li>
 * </ol>
 * </p>
 *
 * <p><strong>Fase 4</strong> adicionará Controllers REST que substituirão este runner
 * como ponto de entrada dos fluxos.</p>
 *
 * <p>Referências: RESEARCH.md Seção 5, seeds V2__seeds.sql</p>
 */
@Component
public class DemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private final MatricularAlunoUseCase matricularUseCase;
    private final AdicionarDisciplinaUseCase adicionarUseCase;
    private final CancelarMatriculaUseCase cancelarUseCase;

    public DemoRunner(MatricularAlunoUseCase matricularUseCase,
                      AdicionarDisciplinaUseCase adicionarUseCase,
                      CancelarMatriculaUseCase cancelarUseCase) {
        this.matricularUseCase = matricularUseCase;
        this.adicionarUseCase  = adicionarUseCase;
        this.cancelarUseCase   = cancelarUseCase;
    }

    /**
     * Executa os 3 fluxos de negócio com os dados dos seeds.
     *
     * <p>Os UUIDs correspondem aos inseridos em V2__seeds.sql. Os objetos de domínio
     * ({@code Aluno}, {@code Turma}) são construídos em memória — na Fase 4, seriam
     * carregados de seus respectivos Aggregates via repositório próprio.</p>
     *
     * <p><strong>Proteção contra re-execução:</strong> o try/catch garante que a segunda
     * execução da aplicação (quando os dados dos seeds e o índice único
     * {@code uq_matricula_aluno_periodo_ativa} já existem) não derrube o startup.
     * Na Fase 4, o ponto de entrada principal são os Controllers REST — este runner
     * é mantido apenas como referência pedagógica do fluxo completo.</p>
     */
    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("=== DemoRunner iniciando execução dos 3 fluxos de negócio ===");

            // -----------------------------------------------------------------------
            // FLUXO 1 — Matricular Aluno
            // Maria Silva (a0...001) ainda não tem matrícula no período 2026/1.
            // VerificadorElegibilidadeMatricula verifica: ativa? período aberto? sem duplicata?
            // -----------------------------------------------------------------------
            log.info("--- FLUXO 1: Matricular aluno ---");

            // Aluno Maria Silva — UUID e CPF correspondem ao seed em V2__seeds.sql
            var maria = new Aluno(
                new AlunoId(UUID.fromString("a0000000-0000-0000-0000-000000000001")),
                new Cpf("52998224725"),   // CPF válido para o seed de Maria Silva
                "Maria Silva",
                true
            );

            // Turma 2026-1 — UUID corresponde ao seed em V2__seeds.sql
            var turma2026 = new Turma(
                new TurmaId(UUID.fromString("b0000000-0000-0000-0000-000000000001")),
                "Turma 2026-1",
                new PeriodoLetivo(2026, 1),
                30
            );

            var periodo2026s1 = new PeriodoLetivo(2026, 1);
            var command1 = new MatricularAlunoCommand(maria, turma2026, periodo2026s1);
            MatriculaId novaId = matricularUseCase.executar(command1);
            log.info("[DemoRunner] FLUXO 1: Matrícula criada com ID {}", novaId.valor());

            // -----------------------------------------------------------------------
            // FLUXO 2 — Adicionar Disciplina
            // Matrícula pré-existente do João Santos (c0...001) recebe "Física Quântica".
            // Aggregate.adicionarDisciplina() verifica: estado ativo? limite? duplicata?
            // -----------------------------------------------------------------------
            log.info("--- FLUXO 2: Adicionar disciplina ---");

            var matriculaExistente = new MatriculaId(
                UUID.fromString("c0000000-0000-0000-0000-000000000001")
            );
            var command2 = new AdicionarDisciplinaCommand(
                matriculaExistente,
                new NomeDisciplina("Física Quântica")
            );
            adicionarUseCase.executar(command2);
            log.info("[DemoRunner] FLUXO 2: Disciplina adicionada à matrícula {}",
                     matriculaExistente.valor());

            // -----------------------------------------------------------------------
            // FLUXO 3 — Cancelar Matrícula
            // A matrícula recém-criada no Fluxo 1 é cancelada.
            // Aggregate.cancelar() verifica: não está cancelada? Transiciona para Cancelada.
            // -----------------------------------------------------------------------
            log.info("--- FLUXO 3: Cancelar matrícula ---");

            var command3 = new CancelarMatriculaCommand(novaId);
            cancelarUseCase.executar(command3);
            log.info("[DemoRunner] FLUXO 3: Matrícula {} cancelada", novaId.valor());

            log.info("=== DemoRunner concluído — todos os 3 fluxos executados com sucesso ===");
        } catch (Exception e) {
            log.warn("DemoRunner: fluxos já executados ou erro esperado na re-execução — {}", e.getMessage());
        }
    }
}
