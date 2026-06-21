package br.com.escola.matricula.interfaces;

import br.com.escola.matricula.aplicacao.AdicionarDisciplinaCommand;
import br.com.escola.matricula.aplicacao.AdicionarDisciplinaUseCase;
import br.com.escola.matricula.aplicacao.CancelarMatriculaCommand;
import br.com.escola.matricula.aplicacao.CancelarMatriculaUseCase;
import br.com.escola.matricula.aplicacao.MatriculaDto;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Fronteira HTTP do Bounded Context Matrícula.
 * Traduz primitivos HTTP para objetos de domínio e delega para os UseCases correspondentes.
 * Zero lógica de negócio — validações de regra de negócio pertencem ao Aggregate.
 * Exceções de domínio propagam naturalmente ao ExcecaoHandler.
 *
 * <p><strong>Estrutura de camadas (DDD):</strong>
 * <pre>
 * HTTP Request → MatriculaController (interfaces)
 *                  ↓ constrói Commands e VOs do domínio
 *              UseCase (aplicacao)
 *                  ↓ orquestra sem decidir
 *              Aggregate (dominio)
 *                  ↓ decide — lança exceções de domínio se invariante violada
 *              Repositório → PostgreSQL
 * </pre>
 * </p>
 *
 * <p><strong>Por que sem try/catch?</strong> Exceções de domínio como
 * {@code DisciplinaJaMatriculadaException} não devem ser tratadas aqui.
 * O {@link ExcecaoHandler} é o único ponto de tradução exceção→HTTP.
 * Isso garante que a lógica de mapeamento de erros não vaze para vários Controllers.</p>
 */
@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    /**
     * Record de request para o endpoint POST /matriculas.
     *
     * <p>Os IDs de aluno e turma chegam como Strings UUID — o domínio os
     * converte para Value Objects no Controller. Essa conversão explícita
     * é um ponto pedagógico: demonstra que tipos primitivos HTTP não entram
     * no domínio sem validação e tipagem adequada.</p>
     *
     * @param alunoId      UUID do aluno em formato String — obrigatório
     * @param turmaId      UUID da turma em formato String — obrigatório
     * @param periodoInicio data de início do período letivo — obrigatória
     * @param periodoFim    data de fim do período letivo — obrigatória
     */
    public record MatricularAlunoRequest(
            @NotNull(message = "O ID do aluno é obrigatório")
            String alunoId,

            @NotNull(message = "O ID da turma é obrigatório")
            String turmaId,

            @NotNull(message = "A data de início do período é obrigatória")
            LocalDate periodoInicio,

            @NotNull(message = "A data de fim do período é obrigatória")
            LocalDate periodoFim
    ) {}

    /**
     * Record de request para o endpoint POST /matriculas/{id}/disciplinas.
     *
     * @param nome nome da disciplina a ser adicionada — obrigatório e não vazio
     */
    public record AdicionarDisciplinaRequest(
            @NotBlank(message = "O nome da disciplina é obrigatório e não pode estar em branco")
            String nome
    ) {}

    private final MatricularAlunoUseCase matricularUseCase;
    private final AdicionarDisciplinaUseCase adicionarUseCase;
    private final CancelarMatriculaUseCase cancelarUseCase;

    /**
     * Construtor com injeção de dependências — sem {@code @Autowired} (Spring 4.3+).
     *
     * @param matricularUseCase UseCase de matrícula de aluno
     * @param adicionarUseCase  UseCase de adição de disciplina
     * @param cancelarUseCase   UseCase de cancelamento de matrícula
     */
    public MatriculaController(MatricularAlunoUseCase matricularUseCase,
                               AdicionarDisciplinaUseCase adicionarUseCase,
                               CancelarMatriculaUseCase cancelarUseCase) {
        this.matricularUseCase = matricularUseCase;
        this.adicionarUseCase = adicionarUseCase;
        this.cancelarUseCase = cancelarUseCase;
    }

    /**
     * POST /matriculas — matricula um aluno em uma turma.
     *
     * <p>Retorna 201 Created com o DTO da matrícula recém-criada.
     * Os campos {@code totalDisciplinas=0} e {@code disciplinas=[]} refletem
     * o estado inicial da matrícula — sem disciplinas adicionadas ainda.</p>
     *
     * @param request dados da matrícula (alunoId, turmaId, periodoInicio, periodoFim)
     * @return MatriculaDto com o ID da matrícula criada e status "ATIVA"
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaDto matricular(@RequestBody @Valid MatricularAlunoRequest request) {
        // 1. Construir PeriodoLetivo — semestre inferido a partir do mês de início
        //    Meses 1-6 → semestre 1; meses 7-12 → semestre 2
        var periodo = new PeriodoLetivo(
                request.periodoInicio().getYear(),
                request.periodoInicio().getMonthValue() <= 6 ? 1 : 2
        );

        // 2. Construir Aluno placeholder — CPF de Maria Silva do seed V2__seeds.sql
        //    Em produção este objeto viria do BC Aluno via API ou evento de integração
        var aluno = new Aluno(
                new AlunoId(UUID.fromString(request.alunoId())),
                new Cpf("52998224725"), // CPF placeholder — em produção viria do BC Aluno via API ou evento
                "N/A",
                true
        );

        // 3. Construir Turma placeholder — nome e capacidade não relevantes para o UseCase de matrícula
        //    vagasMaximas=1 satisfaz o guard do construtor (> 0); o valor não é lido pelo UseCase
        var turma = new Turma(
                new TurmaId(UUID.fromString(request.turmaId())),
                "N/A",
                periodo,
                1   // placeholder — capacidade não é relevante para este UseCase
        );

        // 4. Construir Command e executar o UseCase — retorna o ID da nova matrícula
        var command = new MatricularAlunoCommand(aluno, turma, periodo);
        MatriculaId novaId = matricularUseCase.executar(command);

        // 5. Retornar DTO com estado inicial da matrícula recém-criada
        return new MatriculaDto(
                novaId.valor().toString(),
                request.alunoId(),
                "ATIVA",
                0,
                List.of()
        );
    }

    /**
     * POST /matriculas/{id}/disciplinas — adiciona uma disciplina a uma matrícula existente.
     *
     * <p>Retorna 200 OK com o DTO atualizado da matrícula.
     * O DTO retornado reflete o estado mínimo disponível após a operação —
     * o UseCase não retorna o Aggregate atualizado, apenas executa a operação.</p>
     *
     * @param id      UUID da matrícula
     * @param request dados da disciplina a ser adicionada (nome)
     * @return MatriculaDto com confirmação da operação
     */
    @PostMapping("/{id}/disciplinas")
    public MatriculaDto adicionarDisciplina(@PathVariable UUID id,
                                            @RequestBody @Valid AdicionarDisciplinaRequest request) {
        var command = new AdicionarDisciplinaCommand(
                new MatriculaId(id),
                new NomeDisciplina(request.nome())
        );
        adicionarUseCase.executar(command);

        // UseCase retorna void — DTO construído com dados disponíveis no contexto da requisição
        // O campo disciplinas e totalDisciplinas refletem apenas o estado confirmado desta operação
        return new MatriculaDto(
                id.toString(),
                "",
                "ATIVA",
                1,
                List.of(new MatriculaDto.ItemDto(request.nome(), null))
        );
    }

    /**
     * POST /matriculas/{id}/cancelamento — cancela uma matrícula existente.
     *
     * <p>Aceita corpo vazio — {@code @RequestBody} omitido intencionalmente.
     * Spring MVC aceita POST sem corpo quando não há {@code @RequestBody} declarado.
     * Retorna 200 OK com o DTO da matrícula cancelada.</p>
     *
     * @param id UUID da matrícula a ser cancelada
     * @return MatriculaDto com status "CANCELADA"
     */
    @PostMapping("/{id}/cancelamento")
    public MatriculaDto cancelar(@PathVariable UUID id) {
        var command = new CancelarMatriculaCommand(new MatriculaId(id));
        cancelarUseCase.executar(command);

        // UseCase retorna void — DTO construído com o estado resultante conhecido após cancelamento
        return new MatriculaDto(
                id.toString(),
                "",
                "CANCELADA",
                0,
                List.of()
        );
    }
}
