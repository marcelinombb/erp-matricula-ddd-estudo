package br.com.escola.matricula.service;

import br.com.escola.matricula.model.Aluno;
import br.com.escola.matricula.model.ItemMatricula;
import br.com.escola.matricula.model.Matricula;
import br.com.escola.matricula.model.Turma;
import br.com.escola.matricula.repository.AlunoRepository;
import br.com.escola.matricula.repository.ItemMatriculaRepository;
import br.com.escola.matricula.repository.MatriculaRepository;
import br.com.escola.matricula.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MatriculaServiceImpl — God Service (DIAG-03)")
class MatriculaServiceImplTest {

    // DIAG-03: 4 repositórios mockados para isolar MatriculaServiceImpl
    // Contraste: VerificadorElegibilidadeMatriculaTest (Phase 8) usa 0 mocks
    @Mock
    MatriculaRepository matriculaRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    TurmaRepository turmaRepository;
    @Mock
    ItemMatriculaRepository itemMatriculaRepository;

    @InjectMocks
    MatriculaServiceImpl service;

    private Aluno alunoAtivo;
    private Turma turma;
    private Matricula matriculaAtiva;

    @BeforeEach
    void configurarHappyPath() {
        UUID alunoId = UUID.randomUUID();

        // DIAG-02: Entidade anêmica — construção via setters (sem construtor com args)
        alunoAtivo = new Aluno();
        alunoAtivo.setId(alunoId);
        alunoAtivo.setNome("João da Silva");
        alunoAtivo.setAtivo(true);

        turma = new Turma();
        turma.setId(UUID.randomUUID());
        turma.setCodigo("TURMA-001");

        // alunoId coordenado: adicionarDisciplina busca aluno por matricula.getAlunoId()
        matriculaAtiva = new Matricula();
        matriculaAtiva.setId(UUID.randomUUID());
        matriculaAtiva.setAlunoId(alunoId);
        matriculaAtiva.setStatus("ATIVA");

        when(alunoRepository.findById(any(UUID.class))).thenReturn(Optional.of(alunoAtivo));
        when(turmaRepository.findById(any(UUID.class))).thenReturn(Optional.of(turma));
        when(matriculaRepository.findById(any(UUID.class))).thenReturn(Optional.of(matriculaAtiva));
        // DIAG-06: regra de limite no repositório, não no modelo
        when(matriculaRepository.countDisciplinas(any(UUID.class))).thenReturn(3);
    }

    @Test
    @DisplayName("deve matricular aluno com sucesso")
    void deveMatricularAlunoComSucesso() {
        // given — happy path configurado no @BeforeEach

        // when
        UUID id = service.matricular(alunoAtivo.getId(), turma.getId(), "2026-02-01", "2026-06-30");

        // then
        assertThat(id).isNotNull();
        verify(matriculaRepository).insert(any(Matricula.class));
    }

    @Test
    @DisplayName("deve lançar exceção quando aluno não encontrado")
    void deveLancarExcecaoQuandoAlunoNaoEncontrado() {
        // given
        when(alunoRepository.findById(any())).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> service.matricular(UUID.randomUUID(), turma.getId(), "2026-02-01", "2026-06-30"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Aluno não encontrado");
    }

    @Test
    @DisplayName("deve lançar exceção quando aluno está inativo")
    void deveLancarExcecaoQuandoAlunoInativo() {
        // given
        alunoAtivo.setAtivo(false);
        when(alunoRepository.findById(any(UUID.class))).thenReturn(Optional.of(alunoAtivo));

        // when/then
        assertThatThrownBy(() -> service.matricular(alunoAtivo.getId(), turma.getId(), "2026-02-01", "2026-06-30"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aluno inativo não pode ser matriculado");
        // Contraste com módulo DDD: lançaria AlunoInativoException (tipo específico)
    }

    @Test
    @DisplayName("deve adicionar disciplina com sucesso")
    void deveAdicionarDisciplinaComSucesso() {
        // given — happy path configurado no @BeforeEach

        // when
        service.adicionarDisciplina(matriculaAtiva.getId(), "Matemática");

        // then
        verify(itemMatriculaRepository).insert(any(ItemMatricula.class));
    }

    @Test
    @DisplayName("deve lançar exceção quando matrícula não está ativa ao adicionar disciplina")
    void deveLancarExcecaoQuandoMatriculaNaoEstaAtiva() {
        // given
        matriculaAtiva.setStatus("CANCELADA");
        when(matriculaRepository.findById(any(UUID.class))).thenReturn(Optional.of(matriculaAtiva));

        // when/then
        assertThatThrownBy(() -> service.adicionarDisciplina(matriculaAtiva.getId(), "Matemática"))
                .hasMessageContaining("Matrícula não está ativa");
    }

    @Test
    @DisplayName("deve lançar exceção quando limite de 6 disciplinas atingido")
    void deveLancarExcecaoQuandoLimiteDisciplinasAtingido() {
        // given
        // DIAG-06: limite verificado via banco — countDisciplinas() no repositório
        when(matriculaRepository.countDisciplinas(any(UUID.class))).thenReturn(6);

        // when/then
        assertThatThrownBy(() -> service.adicionarDisciplina(matriculaAtiva.getId(), "Matemática"))
                .hasMessageContaining("Limite de 6 disciplinas");
    }

    @Test
    @DisplayName("deve cancelar matrícula com sucesso")
    void deveCancelarMatriculaComSucesso() {
        // given — happy path configurado no @BeforeEach

        // when
        service.cancelar(matriculaAtiva.getId());

        // then
        verify(matriculaRepository).updateStatus(eq(matriculaAtiva.getId()), eq("CANCELADA"));
    }
}
