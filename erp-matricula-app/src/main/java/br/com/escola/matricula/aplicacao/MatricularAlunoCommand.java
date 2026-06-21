package br.com.escola.matricula.aplicacao;

import br.com.escola.matricula.dominio.modelo.Aluno;
import br.com.escola.matricula.dominio.modelo.Turma;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

/**
 * Command para {@code MatricularAlunoUseCase}.
 *
 * <p><strong>O que é um Command?</strong> Um objeto imutável que carrega todos os dados
 * necessários para executar uma operação. É a fronteira entre o caller (Controller na Fase 4)
 * e o UseCase — substitui longas listas de parâmetros por um objeto nomeado e tipado.</p>
 *
 * <p><strong>Command vs DTO:</strong> Commands carregam <em>intenção de escrita</em>
 * (criar, modificar, cancelar). DTOs carregam <em>dados de leitura</em> (resultado de busca).
 * Esta distinção é parte do padrão CQRS — Commands e Queries separados.</p>
 *
 * <p><strong>Objetos de domínio no Command:</strong> diferente de um DTO HTTP que carregaria
 * primitivos (String, UUID), este Command já recebe objetos de domínio validados.
 * Na Fase 4, o Controller ficará responsável por:
 * <ol>
 *   <li>Receber o request HTTP (primitivos)</li>
 *   <li>Validar e construir os objetos de domínio ({@code Aluno}, {@code Turma})</li>
 *   <li>Criar este Command</li>
 *   <li>Chamar {@code MatricularAlunoUseCase.executar(command)}</li>
 * </ol>
 * O UseCase nunca valida primitivos — recebe domínio já construído.</p>
 *
 * <p><strong>Java 21 record:</strong> imutabilidade garantida pela linguagem — todos os campos
 * são {@code final} e não há setters. Geração automática de {@code equals}, {@code hashCode}
 * e {@code toString} baseados nos campos.</p>
 *
 * @param aluno   aluno a ser matriculado (já carregado e validado)
 * @param turma   turma na qual o aluno será matriculado (já carregada)
 * @param periodo período letivo da matrícula
 */
public record MatricularAlunoCommand(
        Aluno aluno,
        Turma turma,
        PeriodoLetivo periodo) {
}
