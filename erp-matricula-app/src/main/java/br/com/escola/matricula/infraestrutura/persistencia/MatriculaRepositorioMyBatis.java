package br.com.escola.matricula.infraestrutura.persistencia;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementação de {@link MatriculaRepositorio} usando MyBatis.
 *
 * <p><strong>Dependency Inversion em ação:</strong> a interface {@code MatriculaRepositorio}
 * está no pacote {@code dominio.repositorio} — o domínio define o contrato. Esta classe está
 * em {@code infraestrutura.persistencia} — a infraestrutura implementa. O domínio não sabe que
 * MyBatis existe; o compilador garantiria a substituição por qualquer outro mecanismo de
 * persistência sem alterar uma linha do domínio.</p>
 *
 * <p><strong>Estratégia replace-all para {@code salvar()} (D-12):</strong>
 * A coleção de {@code ItemMatricula} é tratada como um todo atômico. A cada chamada de
 * {@code salvar()}, todos os itens são deletados e reinseridos. Pedagogicamente explícito:
 * o Aggregate é a unidade de consistência — salvamos o Aggregate completo, não partes dele.
 * Alternativa (UPDATE individual de cada item) seria mais eficiente mas menos clara.</p>
 *
 * <p>Referências: INF-07, D-12, ADR-001, RESEARCH.md Seção 9</p>
 */
@Repository
public class MatriculaRepositorioMyBatis implements MatriculaRepositorio {

    private final MatriculaMapper mapper;
    private final MatriculaRowMapper rowMapper;

    /**
     * Injeção por construtor — Spring 4.3+ injeta automaticamente com construtor único.
     * Sem {@code @Autowired} necessário.
     */
    public MatriculaRepositorioMyBatis(MatriculaMapper mapper, MatriculaRowMapper rowMapper) {
        this.mapper    = mapper;
        this.rowMapper = rowMapper;
    }

    /**
     * Busca uma matrícula pelo ID, reconstruindo o Aggregate completo com seus itens.
     *
     * <p>O {@code MatriculaMapper.buscarPorId} executa um LEFT JOIN — retorna a matrícula
     * com sua lista de disciplinas em uma única query (sem N+1).</p>
     *
     * @param id identificador da matrícula
     * @return {@code Optional<Matricula>} preenchido se encontrada; vazio se não existir
     */
    @Override
    public Optional<Matricula> buscarPorId(MatriculaId id) {
        MatriculaRow row = mapper.buscarPorId(id.valor());
        return Optional.ofNullable(row).map(rowMapper::toDomain);
    }

    /**
     * Lista todas as matrículas de um aluno, incluindo itens de cada uma.
     *
     * @param alunoId identificador do aluno
     * @return lista de Aggregates; lista vazia se o aluno não tiver matrículas
     */
    @Override
    public List<Matricula> buscarPorAluno(AlunoId alunoId) {
        return mapper.buscarPorAluno(alunoId.valor()).stream()
            .map(rowMapper::toDomain)
            .toList();
    }

    /**
     * Verifica se o aluno já possui matrícula ativa no período informado.
     *
     * <p>Converte {@code PeriodoLetivo(ano, semestre)} para {@code LocalDate} antes de
     * consultar o banco. A mesma lógica de conversão de {@link MatriculaRowMapper#fromDomain}:
     * semestre 1 = fev-jul, semestre 2 = ago-dez.</p>
     *
     * @param alunoId identificador do aluno
     * @param periodo período letivo a verificar
     * @return {@code true} se existe matrícula ativa; {@code false} caso contrário
     */
    @Override
    public boolean existeMatriculaAtiva(AlunoId alunoId, PeriodoLetivo periodo) {
        // Conversão PeriodoLetivo → LocalDate (mesma lógica de MatriculaRowMapper.fromDomain)
        LocalDate inicio = LocalDate.of(periodo.ano(), periodo.semestre() == 1 ? 2 : 8, 1);
        LocalDate fim    = LocalDate.of(periodo.ano(), periodo.semestre() == 1 ? 7 : 12, 31);
        return mapper.existeMatriculaAtiva(alunoId.valor(), inicio, fim);
    }

    /**
     * Persiste o Aggregate {@code Matricula} com estratégia replace-all (D-12).
     *
     * <p><strong>Sequência de operações:</strong>
     * <ol>
     *   <li>Tenta UPDATE na tabela {@code matriculas} pelo ID.</li>
     *   <li>Se UPDATE retornar 0 (registro não existe), faz INSERT.</li>
     *   <li>DELETE todos os itens da matrícula em {@code itens_matricula}.</li>
     *   <li>INSERT todos os itens do estado atual do Aggregate.</li>
     * </ol>
     *
     * <p>Passo 3 + 4 é o "replace-all": a coleção inteira é substituída a cada save.
     * O Aggregate é a unidade de consistência — salvamos o Aggregate completo, nunca
     * apenas parte dele. Se o Aggregate tem 3 disciplinas, o banco terá 3 itens após o save,
     * independente do estado anterior.</p>
     *
     * @param matricula Aggregate com estado atual após operações de domínio
     */
    @Override
    public void salvar(Matricula matricula) {
        MatriculaRow row = rowMapper.fromDomain(matricula);

        // Estratégia replace-all — pedagogicamente explícita (D-12):
        // UPDATE primeiro; se retornar 0 (novo registro), INSERT.
        if (mapper.atualizarMatricula(row) == 0) {
            mapper.inserirMatricula(row);
        }

        // DELETE todos os itens atuais — a coleção é tratada como um todo atômico
        mapper.deletarItensPorMatriculaId(matricula.getId().valor());

        // INSERT todos os itens do estado atual do Aggregate
        List<ItemMatriculaRow> itens = rowMapper.itemsFromDomain(
            matricula.getId(), matricula.getDisciplinas());
        if (!itens.isEmpty()) {
            mapper.inserirItens(itens);
        }
    }
}
