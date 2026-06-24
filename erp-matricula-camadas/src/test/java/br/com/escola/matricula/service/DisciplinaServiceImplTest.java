package br.com.escola.matricula.service;

import br.com.escola.matricula.model.Aluno;
import br.com.escola.matricula.model.ItemMatricula;
import br.com.escola.matricula.model.Matricula;
import br.com.escola.matricula.repository.AlunoRepository;
import br.com.escola.matricula.repository.ItemMatriculaRepository;
import br.com.escola.matricula.repository.MatriculaRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DisciplinaServiceImpl — Duplicação de Regras (DIAG-04)")
class DisciplinaServiceImplTest {

    // DIAG-04: 3 dos 4 mocks do MatriculaServiceImplTest — mesmas regras, quase mesmo setup
    @Mock
    MatriculaRepository matriculaRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    ItemMatriculaRepository itemMatriculaRepository;

    @InjectMocks
    DisciplinaServiceImpl service; // construtor 3-args (DisciplinaServiceImpl.java:41-47)

    private Aluno alunoAtivo;
    private Matricula matriculaAtiva;

    @BeforeEach
    void configurarHappyPath() {
        UUID alunoId = UUID.randomUUID();

        alunoAtivo = new Aluno();
        alunoAtivo.setId(alunoId);
        alunoAtivo.setAtivo(true);

        // coordenar UUID: alunoId na matrícula = ID do aluno
        matriculaAtiva = new Matricula();
        matriculaAtiva.setId(UUID.randomUUID());
        matriculaAtiva.setAlunoId(alunoId);
        matriculaAtiva.setStatus("ATIVA");

        when(matriculaRepository.findById(any(UUID.class))).thenReturn(Optional.of(matriculaAtiva));
        when(alunoRepository.findById(any(UUID.class))).thenReturn(Optional.of(alunoAtivo));
        when(matriculaRepository.countDisciplinas(any(UUID.class))).thenReturn(3);
    }

    @Test
    @DisplayName("deve adicionar disciplina extra com sucesso")
    void deveAdicionarDisciplinaExtraComSucesso() {
        // given — happy path configurado no @BeforeEach

        // when
        service.adicionarDisciplinaExtra(matriculaAtiva.getId(), "Matemática");

        // then
        verify(itemMatriculaRepository).insert(any(ItemMatricula.class));
    }

    @Test
    @DisplayName("deve lançar exceção quando matrícula não encontrada")
    void deveLancarExcecaoQuandoMatriculaNaoEncontrada() {
        // given
        when(matriculaRepository.findById(any())).thenReturn(Optional.empty());

        // when/then
        assertThatThrownBy(() -> service.adicionarDisciplinaExtra(UUID.randomUUID(), "Matemática"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Matrícula não encontrada");
    }

    @Test
    @DisplayName("deve lançar exceção quando matrícula não está ativa")
    void deveLancarExcecaoQuandoMatriculaNaoEstaAtiva() {
        // given
        matriculaAtiva.setStatus("CANCELADA");
        when(matriculaRepository.findById(any(UUID.class))).thenReturn(Optional.of(matriculaAtiva));

        // when/then
        assertThatThrownBy(() -> service.adicionarDisciplinaExtra(matriculaAtiva.getId(), "Matemática"))
                .hasMessageContaining("Matrícula não está ativa");
    }

    @Test
    @DisplayName("deve lançar exceção quando aluno está inativo ao adicionar disciplina extra")
    void deveLancarExcecaoQuandoAlunoInativo() {
        // DIAG-04: mesma regra testada em MatriculaServiceImplTest.deveLancarExcecaoQuandoAlunoInativo()
        // Nota: mensagem DIFERENTE — "Aluno inativo" aqui vs "Aluno inativo não pode ser matriculado"
        // em MatriculaServiceImpl. Esta divergência é o bug silencioso que DIAG-04 prevê.

        // given
        alunoAtivo.setAtivo(false);
        when(alunoRepository.findById(any(UUID.class))).thenReturn(Optional.of(alunoAtivo));

        // when/then
        assertThatThrownBy(() -> service.adicionarDisciplinaExtra(matriculaAtiva.getId(), "Matemática"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aluno inativo");
    }

    @Test
    @DisplayName("deve retornar true quando matrícula está ativa (método verificador — DIAG-04)")
    void deveVerificarSeMatriculaEstaAtiva() {
        // DIAG-04: proliferação de métodos verificadores — verificarStatusMatricula()
        // existe aqui, mas MatriculaServiceImpl não o usa (faz a mesma verificação inline).

        // given — happy path configurado no @BeforeEach

        // when
        boolean resultado = service.verificarStatusMatricula(matriculaAtiva.getId());

        // then
        assertThat(resultado).isTrue();
    }
}
