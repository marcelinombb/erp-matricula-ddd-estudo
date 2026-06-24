package br.com.escola.matricula.controller;

import br.com.escola.matricula.repository.AlunoRepository;
import br.com.escola.matricula.repository.ItemMatriculaRepository;
import br.com.escola.matricula.repository.MatriculaRepository;
import br.com.escola.matricula.repository.TurmaRepository;
import br.com.escola.matricula.service.MatriculaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatriculaController.class)
@DisplayName("MatriculaController — Regras na Interface (DIAG-05)")
class MatriculaControllerTest {

    @Autowired
    MockMvc mockMvc;

    // DIAG-05: Para testar regras no Controller, precisamos de Spring context parcial (@WebMvcTest).
    // Contraste com MatriculaServiceImplTest: aquele usa @ExtendWith(MockitoExtension) — zero Spring.
    @MockBean
    MatriculaService matriculaService;

    // @MapperScan na ErpMatriculaCamadasApplication registra os 4 mappers MyBatis como beans Spring.
    // @WebMvcTest sobe apenas a web layer, mas o scanner MyBatis ainda tenta inicializar os mappers.
    // Estes @MockBean satisfazem a inicialização do contexto sem precisar de DataSource ou SqlSession.
    // O controller NÃO usa esses repositórios diretamente — eles são necessários apenas para o contexto.
    @MockBean
    MatriculaRepository matriculaRepository;
    @MockBean
    AlunoRepository alunoRepository;
    @MockBean
    TurmaRepository turmaRepository;
    @MockBean
    ItemMatriculaRepository itemMatriculaRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("deve bloquear período 199x e nunca chamar o service")
    void deveBloquearPeriodoAntigo_nuncaChamarService() throws Exception {
        // given
        String json = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoInicio": "1999-02-01",
                  "periodoFim": "1999-06-30"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        // when
        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // then
        verify(matriculaService, never()).matricular(any(), any(), any(), any());
        // DIAG-05: regra temporal existe APENAS no controller
        // Se um batch job chamar matriculaService.matricular() diretamente com "1999-02-01",
        // a matrícula será criada sem erro — a regra só existe aqui.
    }

    @Test
    @DisplayName("deve bloquear nome de disciplina com menos de 3 caracteres")
    void deveBloquearNomeDisciplinaCurto() throws Exception {
        // given
        String json = """
                {"nomeDisciplina": "AB"}
                """;

        // when
        mockMvc.perform(post("/matriculas/{id}/disciplinas", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        // then
        verify(matriculaService, never()).adicionarDisciplina(any(), any());
        // DIAG-05: regra de comprimento existe só no controller
        // MatriculaService.adicionarDisciplina() aceita "AB" sem erro se chamado diretamente
    }

    @Test
    @DisplayName("deve matricular com sucesso no happy path")
    void deveMatricularComSucesso() throws Exception {
        // given
        when(matriculaService.matricular(any(), any(), any(), any())).thenReturn(UUID.randomUUID());

        String json = """
                {
                  "alunoId": "%s",
                  "turmaId": "%s",
                  "periodoInicio": "2026-02-01",
                  "periodoFim": "2026-06-30"
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID());

        // when/then
        mockMvc.perform(post("/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(matriculaService).matricular(any(), any(), any(), any());
    }
}
