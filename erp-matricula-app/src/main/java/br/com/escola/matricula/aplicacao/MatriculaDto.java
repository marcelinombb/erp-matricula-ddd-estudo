package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.modelo.StatusMatricula;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de leitura — retornado pelos UseCases após operações de escrita ou em consultas.
 *
 * <p><strong>DTO vs Aggregate:</strong> o DTO expõe apenas os dados necessários para o caller
 * (Controller, teste, DemoRunner). O Aggregate {@code Matricula} encapsula comportamento e
 * invariantes — não deve ser exposto diretamente fora da camada de aplicação.</p>
 *
 * <p><strong>Fase 4:</strong> Controllers converterão este DTO para a resposta HTTP (JSON),
 * serializando automaticamente os campos via Jackson. O UseCase nunca retorna
 * o Aggregate diretamente ao caller HTTP.</p>
 *
 * <p><strong>statusDescricao:</strong> converte a {@code sealed interface StatusMatricula}
 * para uma String legível via pattern matching exaustivo. Demonstra como o consumidor
 * da camada de aplicação não precisa conhecer a estrutura interna do status.</p>
 *
 * <p><strong>disciplinas:</strong> lista de {@link ItemDto} com nome e timestamp de adição.
 * Quando construído via {@link #de(Matricula)}, {@code adicionadaEm} é {@code null} porque
 * o modelo de domínio {@code ItemMatricula} não carrega o timestamp — esse dado pertence
 * à camada de persistência. O timestamp é populado apenas quando o DTO é construído
 * diretamente a partir de {@code ItemMatriculaRow} (leitura via repositório).</p>
 *
 * @param matriculaId      ID da matrícula como String (UUID com hifens — formato padrão UUID)
 * @param alunoId          ID do aluno como String
 * @param statusDescricao  descrição textual do status (ex: "ATIVA", "CANCELADA", "CONCLUIDA")
 * @param totalDisciplinas número de disciplinas atualmente incluídas na matrícula
 * @param disciplinas      lista de disciplinas com nome e timestamp de adição
 */
public record MatriculaDto(
        String matriculaId,
        String alunoId,
        String statusDescricao,
        int totalDisciplinas,
        List<ItemDto> disciplinas) {

    /**
     * DTO interno que representa uma disciplina na matrícula.
     *
     * <p>Expõe o nome da disciplina e o timestamp de quando foi adicionada.
     * {@code adicionadaEm} é {@code null} quando o DTO é construído via
     * {@link MatriculaDto#de(Matricula)} — o domínio não carrega esse campo.
     * É populado quando construído a partir de {@code MatriculaRowMapper}
     * com acesso direto ao {@code ItemMatriculaRow.adicionadaEm}.</p>
     *
     * @param nome         nome da disciplina (valor de {@code NomeDisciplina})
     * @param adicionadaEm timestamp de adição — {@code null} quando construído via factory method
     */
    public record ItemDto(String nome, LocalDateTime adicionadaEm) {}

    /**
     * Factory method — converte um Aggregate {@code Matricula} em {@code MatriculaDto}.
     *
     * <p>Centraliza a lógica de conversão em um único lugar — sem duplicação nos UseCases.</p>
     *
     * <p>O pattern matching exaustivo no {@code switch} garante que novos estados de
     * {@code StatusMatricula} sejam tratados explicitamente — o compilador aponta
     * todos os switches que precisam ser atualizados se um novo estado for adicionado.</p>
     *
     * <p><strong>Nota sobre adicionadaEm:</strong> os {@link ItemDto} criados aqui têm
     * {@code adicionadaEm = null} porque o modelo de domínio {@code ItemMatricula} não
     * carrega o timestamp. O campo existe no DTO preparado para quando a camada de leitura
     * for adicionada (query direta via Mapper retornando {@code ItemMatriculaRow}).</p>
     *
     * @param matricula Aggregate a ser convertido (nunca nulo)
     * @return DTO com dados extraídos do Aggregate
     */
    public static MatriculaDto de(Matricula matricula) {
        String statusDescricao = switch (matricula.getStatus()) {
            case StatusMatricula.Ativa a       -> "ATIVA";
            case StatusMatricula.Cancelada c   -> "CANCELADA";
            case StatusMatricula.Concluida cc  -> "CONCLUIDA";
        };

        var disciplinas = matricula.getDisciplinas().stream()
                .map(item -> new ItemDto(item.disciplina().valor(), null))
                .toList();

        return new MatriculaDto(
                matricula.getId().valor().toString(),
                matricula.getAlunoId().valor().toString(),
                statusDescricao,
                matricula.getDisciplinas().size(),
                disciplinas
        );
    }
}
