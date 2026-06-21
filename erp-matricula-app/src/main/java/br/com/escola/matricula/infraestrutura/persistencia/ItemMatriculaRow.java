package br.com.escola.matricula.infraestrutura.persistencia;

import java.util.UUID;

/**
 * Modelo relacional da tabela {@code itens_matricula}.
 *
 * <p>Representa uma linha da tabela de itens, usada como modelo de leitura/escrita
 * pelo MyBatis. Sem lógica de negócio — compare com
 * {@link br.com.escola.matricula.dominio.modelo.ItemMatricula}, que é um record imutável
 * com {@code NomeDisciplina} validado.</p>
 *
 * <p>{@code matriculaId} é necessário para o INSERT em lote ({@code inserirItens} no
 * {@code MatriculaMapper.xml}): cada item precisa do UUID da matrícula pai para a FK.</p>
 */
public class ItemMatriculaRow {

    /** UUID da matrícula pai — necessário para INSERT em lote (coluna {@code itens_matricula.matricula_id}) */
    public UUID matriculaId;

    /** Nome da disciplina — coluna {@code itens_matricula.disciplina} */
    public String disciplina;
}
