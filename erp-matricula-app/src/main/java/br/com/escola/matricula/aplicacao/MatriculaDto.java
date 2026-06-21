package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.modelo.StatusMatricula;

/**
 * DTO de leitura — retornado pelos UseCases após operações de escrita ou em consultas.
 *
 * <p><strong>DTO vs Aggregate:</strong> o DTO expõe apenas os dados necessários para o caller
 * (Controller, teste, DemoRunner). O Aggregate {@code Matricula} encapsula comportamento e
 * invariantes — não deve ser exposto diretamente fora da camada de aplicação.</p>
 *
 * <p><strong>Fase 4:</strong> Controllers converterão este DTO para a resposta HTTP (JSON),
 * adicionando os campos de links (HATEOAS) se necessário. O UseCase nunca retorna
 * o Aggregate diretamente ao caller HTTP.</p>
 *
 * <p><strong>statusDescricao:</strong> converte a {@code sealed interface StatusMatricula}
 * para uma String legível via pattern matching exaustivo. Demonstra como o consumidor
 * da camada de aplicação não precisa conhecer a estrutura interna do status.</p>
 *
 * @param matriculaId      ID da matrícula como String (UUID sem hifens — formato para APIs)
 * @param alunoId          ID do aluno como String
 * @param statusDescricao  descrição textual do status (ex: "ATIVA", "CANCELADA", "CONCLUIDA")
 * @param totalDisciplinas número de disciplinas atualmente incluídas na matrícula
 */
public record MatriculaDto(
        String matriculaId,
        String alunoId,
        String statusDescricao,
        int totalDisciplinas) {

    /**
     * Factory method — converte um Aggregate {@code Matricula} em {@code MatriculaDto}.
     *
     * <p>Centraliza a lógica de conversão em um único lugar — sem duplicação nos UseCases.</p>
     *
     * <p>O pattern matching exaustivo no {@code switch} garante que novos estados de
     * {@code StatusMatricula} sejam tratados explicitamente — o compilador aponta
     * todos os switches que precisam ser atualizados se um novo estado for adicionado.</p>
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

        return new MatriculaDto(
                matricula.getId().valor().toString(),
                matricula.getAlunoId().valor().toString(),
                statusDescricao,
                matricula.getDisciplinas().size()
        );
    }
}
