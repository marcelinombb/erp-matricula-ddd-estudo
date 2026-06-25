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
import br.com.escola.matricula.dominio.vo.Cpf;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
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
 */
@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    public record MatricularAlunoRequest(
            @NotNull(message = "O ID do aluno é obrigatório")
            String alunoId,

            @NotNull(message = "O ID da turma é obrigatório")
            String turmaId,

            @NotNull(message = "A data de início do período é obrigatória")
            LocalDate periodoInicio
    ) {}

    public record AdicionarDisciplinaRequest(
            @NotBlank(message = "O nome da disciplina é obrigatório e não pode estar em branco")
            String nome
    ) {}

    private final MatricularAlunoUseCase matricularUseCase;
    private final AdicionarDisciplinaUseCase adicionarUseCase;
    private final CancelarMatriculaUseCase cancelarUseCase;

    public MatriculaController(MatricularAlunoUseCase matricularUseCase,
                               AdicionarDisciplinaUseCase adicionarUseCase,
                               CancelarMatriculaUseCase cancelarUseCase) {
        this.matricularUseCase = matricularUseCase;
        this.adicionarUseCase = adicionarUseCase;
        this.cancelarUseCase = cancelarUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaDto matricular(@RequestBody @Valid MatricularAlunoRequest request) {
        var periodo = new PeriodoLetivo(
                request.periodoInicio().getYear(),
                request.periodoInicio().getMonthValue() <= 6 ? 1 : 2
        );

        var aluno = new Aluno(
                UUID.fromString(request.alunoId()),
                new Cpf("52998224725"),
                "N/A",
                true
        );

        var turma = new Turma(
                UUID.fromString(request.turmaId()),
                "N/A",
                periodo,
                1
        );

        var command = new MatricularAlunoCommand(aluno, turma, periodo);
        UUID novaId = matricularUseCase.executar(command);

        return new MatriculaDto(
                novaId.toString(),
                request.alunoId(),
                "ATIVA",
                0,
                List.of()
        );
    }

    @PostMapping("/{id}/disciplinas")
    public MatriculaDto adicionarDisciplina(@PathVariable UUID id,
                                            @RequestBody @Valid AdicionarDisciplinaRequest request) {
        var command = new AdicionarDisciplinaCommand(id, new NomeDisciplina(request.nome()));
        adicionarUseCase.executar(command);

        return new MatriculaDto(
                id.toString(),
                "",
                "ATIVA",
                1,
                List.of(new MatriculaDto.ItemDto(request.nome(), null))
        );
    }

    @PostMapping("/{id}/cancelamento")
    public MatriculaDto cancelar(@PathVariable UUID id) {
        cancelarUseCase.executar(new CancelarMatriculaCommand(id));

        return new MatriculaDto(
                id.toString(),
                "",
                "CANCELADA",
                0,
                List.of()
        );
    }
}
