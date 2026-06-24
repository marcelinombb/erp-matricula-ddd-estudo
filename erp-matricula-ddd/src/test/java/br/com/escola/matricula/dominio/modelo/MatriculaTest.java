package br.com.escola.matricula.dominio.modelo;

import br.com.escola.matricula.dominio.evento.AlunoMatriculado;
import br.com.escola.matricula.dominio.evento.DisciplinaAdicionada;
import br.com.escola.matricula.dominio.evento.MatriculaCancelada;
import br.com.escola.matricula.dominio.excecao.DisciplinaJaMatriculadaException;
import br.com.escola.matricula.dominio.excecao.LimiteDisciplinasExcedidoException;
import br.com.escola.matricula.dominio.excecao.MatriculaCanceladaException;
import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import br.com.escola.matricula.dominio.vo.TurmaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários do Aggregate Matricula.
 *
 * <p>Cada teste é uma especificação de negócio legível:
 * o leitor vê <em>o que o domínio garante</em>, não como o banco funciona.
 * Zero imports de Spring — o Aggregate é testável sem contexto de aplicação.</p>
 *
 * <p>Invariantes cobertas (TDDD-01):
 * <ul>
 *   <li>Guard 1 — matrícula cancelada não recebe novas disciplinas</li>
 *   <li>Guard 2 — limite máximo de 6 disciplinas por matrícula</li>
 *   <li>Guard 3 — a mesma disciplina não pode aparecer duas vezes</li>
 * </ul>
 * </p>
 *
 * <p>Domain Events cobertos (TDDD-04):
 * <ul>
 *   <li>AlunoMatriculado — emitido ao criar()</li>
 *   <li>DisciplinaAdicionada — emitido ao adicionarDisciplina()</li>
 *   <li>MatriculaCancelada — emitido ao cancelar()</li>
 *   <li>Contrato de coletarEventos() — limpa lista interna após retornar</li>
 * </ul>
 * </p>
 */
@DisplayName("Matricula — Aggregate Root")
class MatriculaTest {

    // =========================================================================
    // Método auxiliar — evita repetição nos testes de invariantes e eventos
    // =========================================================================

    /**
     * Cria uma Matricula ativa e descarta o evento AlunoMatriculado gerado pelo criar().
     *
     * <p>Usar em testes que precisam de uma matrícula limpa para testar
     * comportamentos posteriores (adicionarDisciplina, cancelar). Testes
     * que verificam o evento AlunoMatriculado devem chamar Matricula.criar()
     * diretamente e manter os eventos.</p>
     *
     * @return Matricula no estado Ativa, sem eventos pendentes
     */
    private Matricula criarMatriculaAtiva() {
        Matricula matricula = Matricula.criar(
            new AlunoId(UUID.randomUUID()),
            new TurmaId(UUID.randomUUID()),
            new PeriodoLetivo(2026, 1)
        );
        // Descarta AlunoMatriculado gerado pelo criar() — semântica: coletarEventos() limpa a lista
        matricula.coletarEventos();
        return matricula;
    }

    // =========================================================================
    // Task 1 — Invariantes do Aggregate (Guards 1, 2, 3) e caminho feliz
    // =========================================================================

    @Test
    @DisplayName("deve adicionar disciplina com sucesso quando matrícula está ativa")
    void deveAdicionarDisciplinaComSucesso() {
        // given
        Matricula matricula = criarMatriculaAtiva();
        var disciplina = new NomeDisciplina("Matemática");

        // when
        matricula.adicionarDisciplina(disciplina);

        // then
        assertThat(matricula.getDisciplinas()).hasSize(1);
        assertThat(matricula.getDisciplinas().get(0).disciplina()).isEqualTo(disciplina);
    }

    @Test
    @DisplayName("deve lançar exceção ao adicionar disciplina em matrícula cancelada (Guard 1)")
    void deveLancarExcecaoAoAdicionarDisciplinaEmMatriculaCancelada() {
        // given — usa construtor de reconstituição para simular matrícula já cancelada
        // (não usa criar() seguido de cancelar() — testa o Guard 1 diretamente)
        var matriculaCancelada = new Matricula(
            new MatriculaId(UUID.randomUUID()),
            new AlunoId(UUID.randomUUID()),
            new TurmaId(UUID.randomUUID()),
            new PeriodoLetivo(2026, 1),
            new StatusMatricula.Cancelada(LocalDateTime.now().minusDays(1)),
            List.of()
        );

        // when / then
        assertThatThrownBy(() -> matriculaCancelada.adicionarDisciplina(new NomeDisciplina("Matemática")))
            .isInstanceOf(MatriculaCanceladaException.class);
    }

    @Test
    @DisplayName("deve lançar exceção ao tentar adicionar a sétima disciplina (Guard 2 — limite é 6)")
    void deveLancarExcecaoAoAdicionarASetimaDisciplina() {
        // given — criar matrícula ativa e adicionar exatamente 6 disciplinas com sucesso
        Matricula matricula = criarMatriculaAtiva();
        for (int i = 1; i <= 6; i++) {
            matricula.adicionarDisciplina(new NomeDisciplina("Disciplina " + i));
            // descarta eventos intermediários para não acumular na lista
            matricula.coletarEventos();
        }

        // when / then — a sétima disciplina deve lançar LimiteDisciplinasExcedidoException
        assertThatThrownBy(() -> matricula.adicionarDisciplina(new NomeDisciplina("Disciplina 7")))
            .isInstanceOf(LimiteDisciplinasExcedidoException.class);
    }

    @Test
    @DisplayName("deve lançar exceção ao adicionar disciplina duplicada (Guard 3)")
    void deveLancarExcecaoAoAdicionarDisciplinaDuplicada() {
        // given
        Matricula matricula = criarMatriculaAtiva();
        var matematica = new NomeDisciplina("Matemática");
        matricula.adicionarDisciplina(matematica);
        matricula.coletarEventos(); // descarta DisciplinaAdicionada

        // when / then — mesma disciplina pela segunda vez deve lançar DisciplinaJaMatriculadaException
        assertThatThrownBy(() -> matricula.adicionarDisciplina(matematica))
            .isInstanceOf(DisciplinaJaMatriculadaException.class);
    }

    @Test
    @DisplayName("deve lançar exceção ao cancelar matrícula já cancelada")
    void deveLancarExcecaoAoCancelarMatriculaJaCancelada() {
        // given — matrícula ativa cancelada uma vez
        Matricula matricula = criarMatriculaAtiva();
        matricula.cancelar();
        matricula.coletarEventos(); // descarta MatriculaCancelada

        // when / then — segundo cancelamento deve lançar MatriculaCanceladaException
        assertThatThrownBy(() -> matricula.cancelar())
            .isInstanceOf(MatriculaCanceladaException.class);
    }

    // =========================================================================
    // Task 2 — Domain Events (AlunoMatriculado, DisciplinaAdicionada, MatriculaCancelada)
    //          e contrato de coletarEventos() como limpeza de lista
    // =========================================================================

    @Test
    @DisplayName("deve emitir evento AlunoMatriculado ao criar matrícula")
    void deveEmitirEventoAlunoMatriculadoAoCriar() {
        // given — valores conhecidos para verificar os campos do evento
        var alunoId = new AlunoId(UUID.randomUUID());
        var turmaId = new TurmaId(UUID.randomUUID());
        var periodo = new PeriodoLetivo(2026, 1);

        // when
        var matricula = Matricula.criar(alunoId, turmaId, periodo);
        var eventos = matricula.coletarEventos();

        // then — exatamente 1 evento do tipo AlunoMatriculado com os dados corretos
        assertThat(eventos).hasSize(1);
        var evento = (AlunoMatriculado) eventos.get(0);
        assertThat(evento.alunoId()).isEqualTo(alunoId);
        assertThat(evento.turmaId()).isEqualTo(turmaId);
        assertThat(evento.periodoLetivo()).isEqualTo(periodo);
        assertThat(evento.ocorridoEm()).isNotNull();
    }

    @Test
    @DisplayName("deve emitir evento DisciplinaAdicionada ao adicionar disciplina")
    void deveEmitirEventoDisciplinaAdicionadaAoAdicionar() {
        // given — criarMatriculaAtiva() já descartou AlunoMatriculado
        Matricula matricula = criarMatriculaAtiva();
        var disciplina = new NomeDisciplina("Matemática");

        // when
        matricula.adicionarDisciplina(disciplina);
        var eventos = matricula.coletarEventos();

        // then — exatamente 1 evento do tipo DisciplinaAdicionada com os dados corretos
        assertThat(eventos).hasSize(1);
        var evento = (DisciplinaAdicionada) eventos.get(0);
        assertThat(evento.matriculaId()).isEqualTo(matricula.getId());
        assertThat(evento.disciplina()).isEqualTo(disciplina);
        assertThat(evento.ocorridoEm()).isNotNull();
    }

    @Test
    @DisplayName("deve emitir evento MatriculaCancelada ao cancelar matrícula")
    void deveEmitirEventoMatriculaCanceladaAoCancelar() {
        // given — criarMatriculaAtiva() já descartou AlunoMatriculado
        Matricula matricula = criarMatriculaAtiva();

        // when
        matricula.cancelar();
        var eventos = matricula.coletarEventos();

        // then — exatamente 1 evento do tipo MatriculaCancelada com os dados corretos
        assertThat(eventos).hasSize(1);
        var evento = (MatriculaCancelada) eventos.get(0);
        assertThat(evento.matriculaId()).isEqualTo(matricula.getId());
        assertThat(evento.ocorridoEm()).isNotNull();

        // then — status da matrícula deve ser Cancelada com canceladaEm preenchido
        assertThat(matricula.getStatus()).isInstanceOf(StatusMatricula.Cancelada.class);
        var statusCancelada = (StatusMatricula.Cancelada) matricula.getStatus();
        assertThat(statusCancelada.canceladaEm())
            .isNotNull()
            .isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("coletarEventos deve retornar lista vazia na segunda chamada (contrato de limpeza)")
    void coletarEventosDeveRetornarListaVaziaNaSegundaChamada() {
        // given — Matricula.criar() gera AlunoMatriculado na lista interna
        var matricula = Matricula.criar(
            new AlunoId(UUID.randomUUID()),
            new TurmaId(UUID.randomUUID()),
            new PeriodoLetivo(2026, 1)
        );

        // when — primeira chamada coleta o evento; segunda chamada deve retornar vazio
        var primeira = matricula.coletarEventos();
        var segunda = matricula.coletarEventos();

        // then — primeira tem 1 evento (AlunoMatriculado), segunda está vazia
        assertThat(primeira).hasSize(1);
        assertThat(segunda).isEmpty();
    }
}
