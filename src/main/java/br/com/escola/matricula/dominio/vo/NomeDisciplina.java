package br.com.escola.matricula.dominio.vo;

import java.util.Objects;

/**
 * Value Object que representa o nome de uma disciplina escolar.
 *
 * <p>O nome de uma disciplina tem restrições claras: não pode ser nulo,
 * não pode ser em branco, e tem um comprimento máximo de 100 caracteres
 * (limite da coluna {@code disciplina VARCHAR(100)} no banco de dados).
 * Com este VO, essas restrições são verificadas uma vez, no construtor —
 * qualquer {@code NomeDisciplina} que existe no sistema já é válido.</p>
 *
 * <p>Espaços nas bordas são normalizados automaticamente ({@code strip()}).
 * Dois {@code NomeDisciplina} com o mesmo valor são iguais — o Java 21
 * {@code record} gera {@code equals}/{@code hashCode} por valor automaticamente.</p>
 */
public record NomeDisciplina(String valor) {

    /**
     * Construtor compacto com validações e normalização.
     *
     * @throws NullPointerException se valor for nulo
     * @throws IllegalArgumentException se valor for em branco ou exceder 100 caracteres
     */
    public NomeDisciplina {
        Objects.requireNonNull(valor, "Nome da disciplina não pode ser nulo");
        valor = valor.strip(); // normaliza espaços nas bordas antes de validar comprimento
        if (valor.isBlank()) {
            throw new IllegalArgumentException("Nome da disciplina não pode ser em branco");
        }
        if (valor.length() > 100) {
            throw new IllegalArgumentException(
                "Nome da disciplina excede 100 caracteres: " + valor.length()
            );
        }
    }
}
