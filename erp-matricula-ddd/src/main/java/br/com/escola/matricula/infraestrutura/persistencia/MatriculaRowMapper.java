package br.com.escola.matricula.infraestrutura.persistencia;

import br.com.escola.matricula.dominio.modelo.ItemMatricula;
import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.modelo.StatusMatricula;
import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import br.com.escola.matricula.dominio.vo.TurmaId;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * ÚNICO arquivo que conhece tanto {@link MatriculaRow} quanto {@link Matricula}.
 *
 * <p><strong>Este arquivo é o ponto pedagógico central do projeto.</strong></p>
 *
 * <p>Compare os dois tipos que este arquivo manipula:
 * <ul>
 *   <li>{@link Matricula}: tem comportamento — {@code adicionarDisciplina()}, {@code cancelar()},
 *       {@code coletarEventos()}. Decide, protege invariantes, emite eventos. É DDD.</li>
 *   <li>{@link MatriculaRow}: tem dados — campos públicos que espelham as colunas da tabela.
 *       Sem nenhum método de negócio. É o modelo relacional.</li>
 * </ul>
 * A separação entre estes dois mundos — domínio rico em comportamento vs. modelo relacional
 * plano — é o argumento central do ADR-001 (Por que MyBatis em vez de JPA).
 * Com JPA, o modelo de domínio ficaria contaminado com {@code @Entity}, {@code @Id},
 * {@code @Column}. Com MyBatis e este mapper, a separação é explícita e navegável.</p>
 *
 * <p><strong>Conversão de {@code PeriodoLetivo}:</strong> o VO do domínio usa
 * {@code (int ano, int semestre)} enquanto o banco armazena {@code DATE} (inicio, fim).
 * A conversão é feita inline neste mapper — Opção C do RESEARCH.md:
 * <ul>
 *   <li>Banco → domínio: inferir semestre a partir do mês de início (≤6 = semestre 1, >6 = semestre 2)</li>
 *   <li>Domínio → banco: semestre 1 = fev-jul, semestre 2 = ago-dez</li>
 * </ul>
 * Esta lógica de conversão é uma regra de negócio — ela pertence explicitamente aqui,
 * não escondida em um TypeHandler. O aluno vê a regra e a entende.</p>
 *
 * <p>Referências: ADR-001, INF-06, RESEARCH.md Seções 2, 6 e 9</p>
 */
@Component
public class MatriculaRowMapper {

    /**
     * Converte um {@link MatriculaRow} (modelo relacional) em {@link Matricula} (modelo de domínio).
     *
     * <p>Este é o caminho de leitura: banco → domínio. MyBatis popula o {@code MatriculaRow};
     * este método o transforma em um Aggregate rico com invariantes.</p>
     *
     * <p><strong>Regra de conversão de PeriodoLetivo:</strong>
     * Inferimos o semestre a partir do mês de início:
     * <ul>
     *   <li>Mês ≤ 6 (janeiro-junho) → semestre 1</li>
     *   <li>Mês > 6 (julho-dezembro) → semestre 2</li>
     * </ul>
     * </p>
     *
     * @param row row preenchida pelo MyBatis com dados do banco
     * @return Aggregate {@code Matricula} pronto para receber operações de domínio
     */
    public Matricula toDomain(MatriculaRow row) {
        // Conversão PeriodoLetivo: DATE → (ano, semestre)
        // Regra de negócio: mês ≤ 6 = semestre 1, mês > 6 = semestre 2
        var periodo = new PeriodoLetivo(
            row.periodoInicio.getYear(),
            row.periodoInicio.getMonthValue() <= 6 ? 1 : 2
        );

        var status = reconstruirStatus(row);

        var disciplinas = row.itens.stream()
            .map(item -> new ItemMatricula(new NomeDisciplina(item.disciplina)))
            .toList();

        return new Matricula(
            new MatriculaId(row.id),
            new AlunoId(row.alunoId),
            new TurmaId(row.turmaId),
            periodo,
            status,
            disciplinas
        );
    }

    /**
     * Converte um {@link Matricula} (modelo de domínio) em {@link MatriculaRow} (modelo relacional).
     *
     * <p>Este é o caminho de escrita: domínio → banco. O UseCase chama este método antes
     * de persistir. Apenas os dados escalares da matrícula são copiados aqui — os itens
     * são tratados separadamente via {@link #itemsFromDomain}.</p>
     *
     * <p><strong>Regra de conversão de PeriodoLetivo:</strong>
     * <ul>
     *   <li>Semestre 1: início = 1° de fevereiro, fim = 31 de julho</li>
     *   <li>Semestre 2: início = 1° de agosto, fim = 31 de dezembro</li>
     * </ul>
     * </p>
     *
     * @param matricula Aggregate com estado atual após operações de domínio
     * @return row com campos prontos para INSERT/UPDATE no banco
     */
    public MatriculaRow fromDomain(Matricula matricula) {
        var row = new MatriculaRow();

        row.id = matricula.getId().valor();
        row.alunoId = matricula.getAlunoId().valor();
        row.turmaId = matricula.getTurmaId().valor();

        // Conversão PeriodoLetivo: (ano, semestre) → LocalDate
        // Semestre 1: fevereiro (2) até julho (7); Semestre 2: agosto (8) até dezembro (12)
        int ano = matricula.getPeriodoLetivo().ano();
        int semestre = matricula.getPeriodoLetivo().semestre();
        row.periodoInicio = LocalDate.of(ano, semestre == 1 ? 2 : 8, 1);
        row.periodoFim    = LocalDate.of(ano, semestre == 1 ? 7 : 12, 31);

        // Conversão StatusMatricula: sealed interface → String + timestamps opcionais
        row.status = switch (matricula.getStatus()) {
            case StatusMatricula.Ativa a     -> "ATIVA";
            case StatusMatricula.Cancelada c -> "CANCELADA";
            case StatusMatricula.Concluida c -> "CONCLUIDA";
        };

        // cancelada_em e concluida_em: null exceto quando no estado correspondente
        row.canceladaEm = (matricula.getStatus() instanceof StatusMatricula.Cancelada c)
            ? c.canceladaEm() : null;
        row.concluidaEm = (matricula.getStatus() instanceof StatusMatricula.Concluida c)
            ? c.concluidaEm() : null;

        return row;
    }

    /**
     * Converte a lista de {@link ItemMatricula} do domínio em {@link ItemMatriculaRow} para
     * persistência em lote.
     *
     * <p>Cada {@code ItemMatriculaRow} recebe o {@code matriculaId} explicitamente — necessário
     * para o INSERT em lote no {@code MatriculaMapper.xml} (coluna FK {@code matricula_id}).</p>
     *
     * @param matriculaId identificador da matrícula pai (para a FK em itens_matricula)
     * @param items       lista de itens do Aggregate ({@code Matricula.getDisciplinas()})
     * @return lista de rows prontas para INSERT via {@code MatriculaMapper.inserirItens()}
     */
    public List<ItemMatriculaRow> itemsFromDomain(MatriculaId matriculaId, List<ItemMatricula> items) {
        return items.stream().map(item -> {
            var row = new ItemMatriculaRow();
            row.matriculaId = matriculaId.valor();
            row.disciplina  = item.disciplina().valor();
            return row;
        }).toList();
    }

    /**
     * Reconstrói o {@link StatusMatricula} a partir dos campos planos do {@link MatriculaRow}.
     *
     * <p>Switch exaustivo sobre String — com {@code default} lançando exceção para estados
     * desconhecidos (defesa contra dados corrompidos no banco). Contraste com o switch
     * no domínio (sealed interface), que é exaustivo em tempo de compilação.</p>
     *
     * @param row row com campos {@code status}, {@code canceladaEm}, {@code concluidaEm}
     * @return instância da sealed interface correspondente ao status
     * @throws IllegalStateException se o valor de status não for reconhecido
     */
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
