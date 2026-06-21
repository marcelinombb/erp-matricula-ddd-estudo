package br.com.escola.matricula.infraestrutura.persistencia;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Mapper MyBatis para {@code Matricula}.
 *
 * <p>Métodos mapeados para queries em {@code src/main/resources/mapper/MatriculaMapper.xml}.
 * O namespace do XML aponta para este interface:
 * {@code "br.com.escola.matricula.infraestrutura.persistencia.MatriculaMapper"}.</p>
 *
 * <p><strong>Não usar diretamente</strong> no código de aplicação — usar
 * {@link MatriculaRepositorioMyBatis}, que implementa a interface de domínio
 * {@link br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio}.
 * O Mapper é um detalhe de infraestrutura; o Repositório é o contrato do domínio.</p>
 *
 * <p>Registrado automaticamente pelo {@code @MapperScan} em {@code ErpMatriculaApplication}.
 * Ver RESEARCH.md Pitfall 4: sem {@code @MapperScan} ou {@code @Mapper}, o Spring não
 * encontra a implementação do Mapper gerada pelo MyBatis.</p>
 */
@Mapper
public interface MatriculaMapper {

    /**
     * Busca um {@link MatriculaRow} completo pelo ID, incluindo os itens via JOIN.
     *
     * @param id UUID da matrícula ({@code jdbcType=OTHER} no XML — Pitfall 2)
     * @return row preenchida com itens, ou {@code null} se não encontrada
     */
    MatriculaRow buscarPorId(@Param("id") UUID id);

    /**
     * Lista todos os {@link MatriculaRow} de um aluno, incluindo itens via JOIN.
     *
     * @param alunoId UUID do aluno ({@code jdbcType=OTHER} no XML — Pitfall 2)
     * @return lista de rows com itens; lista vazia se aluno não tiver matrículas
     */
    List<MatriculaRow> buscarPorAluno(@Param("alunoId") UUID alunoId);

    /**
     * Verifica se existe matrícula ativa para o aluno no período informado.
     *
     * <p>Usado por {@link br.com.escola.matricula.dominio.servico.VerificadorElegibilidadeMatricula}
     * via {@link MatriculaRepositorioMyBatis} para prevenir matrículas duplicadas.</p>
     *
     * @param alunoId       UUID do aluno
     * @param periodoInicio data de início do período (convertida de {@code PeriodoLetivo} pelo repositório)
     * @param periodoFim    data de fim do período
     * @return {@code true} se existe matrícula ativa; {@code false} caso contrário
     */
    boolean existeMatriculaAtiva(@Param("alunoId") UUID alunoId,
                                  @Param("periodoInicio") LocalDate periodoInicio,
                                  @Param("periodoFim") LocalDate periodoFim);

    /**
     * Insere uma nova matrícula na tabela {@code matriculas}.
     *
     * <p>Chamado pelo {@link MatriculaRepositorioMyBatis#salvar} quando
     * {@link #atualizarMatricula} retornar 0 (registro ainda não existe).</p>
     *
     * @param row dados da matrícula a inserir
     * @return número de linhas afetadas (1 em caso de sucesso)
     */
    int inserirMatricula(MatriculaRow row);

    /**
     * Atualiza o status e timestamps de uma matrícula existente.
     *
     * <p>Chamado primeiro no {@link MatriculaRepositorioMyBatis#salvar} (estratégia replace-all).
     * Se retornar 0, o registro não existe e deve ser criado via {@link #inserirMatricula}.</p>
     *
     * @param row dados com status, cancelada_em e concluida_em atualizados
     * @return número de linhas afetadas (1 se existia, 0 se não existia)
     */
    int atualizarMatricula(MatriculaRow row);

    /**
     * Deleta todos os itens de uma matrícula — parte da estratégia replace-all (D-12).
     *
     * <p>Executado após {@link #atualizarMatricula} ou {@link #inserirMatricula},
     * antes de {@link #inserirItens}. Trata a coleção de itens como um todo atômico.</p>
     *
     * @param matriculaId UUID da matrícula cujos itens serão deletados
     * @return número de linhas deletadas
     */
    int deletarItensPorMatriculaId(@Param("matriculaId") UUID matriculaId);

    /**
     * Insere itens de matrícula em lote usando {@code <foreach>} no XML.
     *
     * <p>Chamado após {@link #deletarItensPorMatriculaId} com o estado atual
     * do Aggregate. Um único INSERT com múltiplos VALUES — mais eficiente que N INSERTs.</p>
     *
     * @param itens lista de itens a inserir; não chamar com lista vazia
     * @return número total de linhas inseridas
     */
    int inserirItens(@Param("list") List<ItemMatriculaRow> itens);
}
