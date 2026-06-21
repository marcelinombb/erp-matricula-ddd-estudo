package br.com.escola.matricula.controller;

import br.com.escola.matricula.service.MatriculaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Fronteira HTTP do módulo erp-matricula-camadas.
 *
 * <p>ANTI-PADRAO: Regras na Interface (DIAG-05)
 * Este Controller contém validações de regra de negócio — não apenas validação de
 * formato/estrutura do HTTP. Um chamador que acesse MatriculaService diretamente
 * (batch job, teste de integração, outro Service) não passa por essas validações.</p>
 *
 * <p>Contraste: erp-matricula-ddd/.../interfaces/MatriculaController.java tem
 * zero lógica de negócio — delega tudo para os UseCases. A Javadoc do controller
 * DDD diz explicitamente "Zero lógica de negócio — validações de regra de negócio
 * pertencem ao Aggregate".</p>
 */
@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    // --- Records de request/response ---

    /**
     * Request para POST /matriculas.
     * Usa String para periodoInicio/periodoFim — sem tipo de domínio (contraste com módulo DDD
     * que usa PeriodoLetivo, um Value Object validado).
     */
    public record MatricularRequest(
            @NotNull(message = "O ID do aluno é obrigatório")
            UUID alunoId,

            @NotNull(message = "O ID da turma é obrigatório")
            UUID turmaId,

            @NotBlank(message = "O período de início é obrigatório")
            String periodoInicio,

            @NotBlank(message = "O período de fim é obrigatório")
            String periodoFim
    ) {}

    /**
     * Request para POST /matriculas/{id}/disciplinas.
     */
    public record AdicionarDisciplinaRequest(
            @NotBlank(message = "O nome da disciplina é obrigatório")
            String nomeDisciplina
    ) {}

    /**
     * Response de matrícula — espelha colunas da tabela (DIAG-06: Acoplamento ao Banco).
     * Contraste: módulo DDD retorna MatriculaDto construído a partir do Aggregate,
     * não diretamente das colunas.
     */
    public record MatriculaResponse(
            UUID id,
            UUID alunoId,
            UUID turmaId,
            String periodoInicio,
            String periodoFim,
            String status
    ) {}

    // --- Construtor ---

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    // --- Endpoints ---

    /**
     * POST /matriculas — matricula um aluno em uma turma.
     *
     * <p>ANTI-PADRAO: Regras na Interface (DIAG-05) — A validação abaixo verifica se o
     * período de início não é muito antigo. Esta regra de negócio existe APENAS aqui no
     * Controller. Consequências:
     * (1) Um batch job que chama MatriculaService.matricular() diretamente não passa por
     *     esta validação.
     * (2) Um teste de integração que chama o service sem HTTP bypassa a regra.
     * (3) A regra depende do protocolo HTTP para existir — sem Controller, ela desaparece.
     * </p>
     */
    @PostMapping
    public ResponseEntity<?> matricular(@RequestBody @Valid MatricularRequest request) {

        // ANTI-PADRAO: Regras na Interface (DIAG-05)
        // Validação 1: período de início não pode estar em branco (duplica @NotBlank acima —
        // demonstra redundância que surge quando regras estão espalhadas em múltiplas camadas).
        if (request.periodoInicio() == null || request.periodoInicio().isBlank()) {
            return ResponseEntity.badRequest().body("Período de início é obrigatório");
        }

        // ANTI-PADRAO: Regras na Interface (DIAG-05)
        // Validação 2: regras de negócio temporais deste tipo deveriam estar no Service ou
        // no modelo, não no Controller. Aqui: períodos que começam com "199" (década de 1990)
        // são bloqueados por uma heurística hardcoded — regra frágil e invisível para chamadores
        // que não passem pelo HTTP. Se um batch precisar corrigir matrículas históricas de 1999,
        // descobrirá que MatriculaService.matricular() aceita normalmente — o bloqueio desaparece.
        if (request.periodoInicio().startsWith("199")) {
            return ResponseEntity.badRequest()
                    .body("Período muito antigo: matrículas anteriores a 2000 não são aceitas. " +
                          "NOTA: Esta regra existe APENAS no Controller — MatriculaService.matricular() " +
                          "não faz esta validação.");
        }

        UUID id = matriculaService.matricular(
                request.alunoId(),
                request.turmaId(),
                request.periodoInicio(),
                request.periodoFim()
        );

        return ResponseEntity.status(201)
                .body(new MatriculaResponse(
                        id,
                        request.alunoId(),
                        request.turmaId(),
                        request.periodoInicio(),
                        request.periodoFim(),
                        "ATIVA"
                ));
    }

    /**
     * POST /matriculas/{id}/disciplinas — adiciona uma disciplina a uma matrícula existente.
     *
     * <p>ANTI-PADRAO: Regras na Interface (DIAG-05) — Validação do nome da disciplina está
     * APENAS aqui. MatriculaService.adicionarDisciplina() não valida o nome — se chamado
     * diretamente, aceita qualquer string, incluindo strings de 1 caractere ou nomes absurdos.
     * </p>
     */
    @PostMapping("/{id}/disciplinas")
    public ResponseEntity<?> adicionarDisciplina(
            @PathVariable UUID id,
            @RequestBody @Valid AdicionarDisciplinaRequest request) {

        // ANTI-PADRAO: Regras na Interface (DIAG-05)
        // Regra de negócio: nome da disciplina precisa ter ao menos 3 caracteres.
        // Esta regra vive aqui — o Service não a conhece. Um POST direto ao Service
        // com "AB" como nome de disciplina passaria sem erro.
        if (request.nomeDisciplina().length() < 3) {
            return ResponseEntity.badRequest()
                    .body("Nome da disciplina muito curto (mínimo 3 caracteres). " +
                          "NOTA: Esta regra existe APENAS no Controller — " +
                          "MatriculaService.adicionarDisciplina() não valida o comprimento do nome.");
        }

        matriculaService.adicionarDisciplina(id, request.nomeDisciplina());
        return ResponseEntity.ok().build();
    }

    /**
     * POST /matriculas/{id}/cancelamento — cancela uma matrícula existente.
     *
     * <p>Sem comentário DIAG-05 aqui: cancelamento não tem regra na interface.
     * Este contraste é intencional — em sistemas reais, alguns endpoints têm a falha
     * (validações no Controller) e outros não, tornando o comportamento inconsistente
     * e difícil de auditar.</p>
     */
    @PostMapping("/{id}/cancelamento")
    public ResponseEntity<?> cancelar(@PathVariable UUID id) {
        matriculaService.cancelar(id);
        return ResponseEntity.ok().build();
    }

    // --- Tratamento de exceções ---

    /**
     * Tratamento genérico de RuntimeException — no módulo DDD, exceções são tipadas
     * (MatriculaNaoEncontradaException, AlunoInativoException) e mapeadas para status
     * HTTP específicos pelo ExcecaoHandler. Aqui, toda RuntimeException vira HTTP 400
     * com a mensagem de erro completa — conveniente para desenvolvimento, inadequado
     * para produção (vaza detalhes internos para o cliente).
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> tratarRuntimeException(RuntimeException e) {
        return ResponseEntity.status(400)
                .body(Map.of("erro", e.getMessage() != null ? e.getMessage() : "Erro interno"));
    }
}
