package br.com.escola.matricula.dominio.modelo;

import br.com.escola.matricula.dominio.vo.NomeDisciplina;

/**
 * Entidade interna do Aggregate {@code Matricula} que representa uma disciplina incluída.
 *
 * <p><strong>Por que é interna ao Aggregate?</strong></p>
 *
 * <p>{@code ItemMatricula} não tem significado fora do contexto do Aggregate {@code Matricula}.
 * Uma disciplina incluída existe como parte de uma matrícula, não como objeto independente.
 * Criar um {@code ItemMatricula} sem {@code Matricula} não faz sentido no domínio — por isso
 * é catalogada aqui, não em {@code entidades.md} com {@code Aluno} e {@code Turma}.</p>
 *
 * <p><strong>Por que é um {@code record} apesar de ser "entidade interna"?</strong></p>
 *
 * <p>Entidades internas sem ciclo de vida próprio podem ser {@code record}. Um item de
 * matrícula, uma vez criado, não muda — só é adicionado ou removido. Se o aluno quiser
 * substituir uma disciplina, ele remove a atual e adiciona uma nova. Não há "modificar
 * uma disciplina já matriculada". Isso é diferente de {@code Aluno}, cujo status pode
 * mudar enquanto o aluno permanece o mesmo.</p>
 *
 * <p>Nunca referencie {@code ItemMatricula} diretamente de fora do Aggregate.
 * O acesso é sempre via {@code Matricula.getDisciplinas()}.</p>
 */
public record ItemMatricula(NomeDisciplina disciplina) {
}
