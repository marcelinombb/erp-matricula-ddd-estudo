package br.com.escola.matricula.infraestrutura.config;

import br.com.escola.matricula.aplicacao.AdicionarDisciplinaCommand;
import br.com.escola.matricula.aplicacao.AdicionarDisciplinaUseCase;
import br.com.escola.matricula.aplicacao.CancelarMatriculaCommand;
import br.com.escola.matricula.aplicacao.CancelarMatriculaUseCase;
import br.com.escola.matricula.aplicacao.MatricularAlunoCommand;
import br.com.escola.matricula.aplicacao.MatricularAlunoUseCase;
import br.com.escola.matricula.dominio.modelo.Aluno;
import br.com.escola.matricula.dominio.modelo.Turma;
import br.com.escola.matricula.dominio.vo.Cpf;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * CommandLineRunner de demonstração — executa os 3 fluxos de negócio no startup.
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

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("=== DemoRunner iniciando execução dos 3 fluxos de negócio ===");

            // FLUXO 1 — Matricular Aluno
            log.info("--- FLUXO 1: Matricular aluno ---");

            var maria = new Aluno(
                UUID.fromString("a0000000-0000-0000-0000-000000000001"),
                new Cpf("52998224725"),
                "Maria Silva",
                true
            );

            var turma2026 = new Turma(
                UUID.fromString("b0000000-0000-0000-0000-000000000001"),
                "Turma 2026-1",
                new PeriodoLetivo(2026, 1),
                30
            );

            var periodo2026s1 = new PeriodoLetivo(2026, 1);
            var command1 = new MatricularAlunoCommand(maria, turma2026, periodo2026s1);
            UUID novaId = matricularUseCase.executar(command1);
            log.info("[DemoRunner] FLUXO 1: Matrícula criada com ID {}", novaId);

            // FLUXO 2 — Adicionar Disciplina
            log.info("--- FLUXO 2: Adicionar disciplina ---");

            var matriculaExistente = UUID.fromString("c0000000-0000-0000-0000-000000000001");
            var command2 = new AdicionarDisciplinaCommand(
                matriculaExistente,
                new NomeDisciplina("Física Quântica")
            );
            adicionarUseCase.executar(command2);
            log.info("[DemoRunner] FLUXO 2: Disciplina adicionada à matrícula {}", matriculaExistente);

            // FLUXO 3 — Cancelar Matrícula
            log.info("--- FLUXO 3: Cancelar matrícula ---");

            var command3 = new CancelarMatriculaCommand(novaId);
            cancelarUseCase.executar(command3);
            log.info("[DemoRunner] FLUXO 3: Matrícula {} cancelada", novaId);

            log.info("=== DemoRunner concluído — todos os 3 fluxos executados com sucesso ===");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.warn("DemoRunner: matrícula já existe (re-execução detectada) — {}", e.getMessage());
        }
    }
}
