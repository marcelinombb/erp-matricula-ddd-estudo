package br.com.escola.matricula.infraestrutura.persistencia;

import br.com.escola.matricula.dominio.modelo.ItemMatricula;
import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.modelo.StatusMatricula;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ÚNICO arquivo que conhece tanto {@link MatriculaRow} quanto {@link Matricula}.
 *
 * <p>Separa explicitamente o modelo de domínio (com comportamento) do modelo relacional
 * (dados planos). A conversão de {@code PeriodoLetivo} (ano/semestre) para datas SQL
 * e vice-versa é feita aqui — regra de negócio visível e navegável.</p>
 */
@Component
public class MatriculaRowMapper {

    public Matricula toDomain(MatriculaRow row) {
        var periodo = new PeriodoLetivo(
            row.periodoInicio.getYear(),
            row.periodoInicio.getMonthValue() <= 6 ? 1 : 2
        );

        var status = reconstruirStatus(row);

        var disciplinas = row.itens.stream()
            .map(item -> new ItemMatricula(new NomeDisciplina(item.disciplina)))
            .toList();

        return new Matricula(
            row.id,
            row.alunoId,
            row.turmaId,
            periodo,
            status,
            disciplinas
        );
    }

    public MatriculaRow fromDomain(Matricula matricula) {
        var row = new MatriculaRow();

        row.id      = matricula.getId();
        row.alunoId = matricula.getAlunoId();
        row.turmaId = matricula.getTurmaId();

        int ano = matricula.getPeriodoLetivo().ano();
        int semestre = matricula.getPeriodoLetivo().semestre();
        row.periodoInicio = LocalDate.of(ano, semestre == 1 ? 2 : 8, 1);
        row.periodoFim    = LocalDate.of(ano, semestre == 1 ? 7 : 12, 31);

        row.status = switch (matricula.getStatus()) {
            case StatusMatricula.Ativa a     -> "ATIVA";
            case StatusMatricula.Cancelada c -> "CANCELADA";
            case StatusMatricula.Concluida c -> "CONCLUIDA";
        };

        row.canceladaEm = (matricula.getStatus() instanceof StatusMatricula.Cancelada c)
            ? c.canceladaEm() : null;
        row.concluidaEm = (matricula.getStatus() instanceof StatusMatricula.Concluida c)
            ? c.concluidaEm() : null;

        return row;
    }

    public List<ItemMatriculaRow> itemsFromDomain(UUID matriculaId, List<ItemMatricula> items) {
        return items.stream().map(item -> {
            var row = new ItemMatriculaRow();
            row.matriculaId = matriculaId;
            row.disciplina  = item.disciplina().valor();
            return row;
        }).toList();
    }

    private StatusMatricula reconstruirStatus(MatriculaRow row) {
        return switch (row.status) {
            case "ATIVA"     -> new StatusMatricula.Ativa();
            case "CANCELADA" -> new StatusMatricula.Cancelada(row.canceladaEm);
            case "CONCLUIDA" -> new StatusMatricula.Concluida(row.concluidaEm);
            default -> throw new IllegalStateException(
                "Status desconhecido no banco de dados: '" + row.status + "'. " +
                "Valores esperados: ATIVA, CANCELADA, CONCLUIDA."
            );
        };
    }
}
