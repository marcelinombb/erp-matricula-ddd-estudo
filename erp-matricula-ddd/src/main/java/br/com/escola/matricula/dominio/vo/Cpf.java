package br.com.escola.matricula.dominio.vo;

import java.util.Objects;

/**
 * Value Object que representa um CPF brasileiro.
 *
 * <p><strong>Por que CPF é um Value Object e não uma String?</strong></p>
 *
 * <p>CPF tem invariantes de negócio: 11 dígitos numéricos com algoritmo de
 * dígito verificador específico. Se essas regras vivem fora do tipo, qualquer
 * parte do sistema pode criar um CPF inválido — e o compilador não vai reclamar.
 * Com um {@code record}, a criação de um {@code Cpf} inválido é impossível em
 * tempo de execução: o construtor lança exceção antes que o objeto exista.</p>
 *
 * <p>Dois {@code Cpf} com o mesmo valor são sempre iguais — o Java 21 {@code record}
 * gera {@code equals}/{@code hashCode} automaticamente por todos os campos.
 * Isso é o comportamento correto para um Value Object: identidade por valor.</p>
 *
 * <p>Diferença para Entidade: {@code Cpf} não tem identidade própria — é comparado
 * por valor. {@code Aluno} tem identidade (UUID) — é comparado pelo ID.
 * Ver {@link br.com.escola.matricula.dominio.modelo.Aluno}.</p>
 */
public record Cpf(String valor) {

    /**
     * Construtor compacto do record — executado a cada {@code new Cpf(...)}.
     *
     * <p>Responsabilidades:
     * <ol>
     *   <li>Valida não-nulo</li>
     *   <li>Normaliza para apenas dígitos (remove máscara "xxx.xxx.xxx-xx")</li>
     *   <li>Valida comprimento de 11 dígitos</li>
     *   <li>Valida o algoritmo de dígito verificador (módulo 11)</li>
     *   <li>Armazena normalizado (apenas dígitos, sem máscara)</li>
     * </ol>
     * </p>
     */
    public Cpf {
        Objects.requireNonNull(valor, "CPF não pode ser nulo");

        // Normalização: remove qualquer caractere que não seja dígito
        valor = valor.replaceAll("[^0-9]", "");

        if (valor.length() != 11) {
            throw new IllegalArgumentException(
                "CPF deve ter 11 dígitos numéricos. Recebido (após normalização): " + valor.length() + " dígitos"
            );
        }

        if (!cpfComDigitoVerificadorValido(valor)) {
            throw new IllegalArgumentException(
                "CPF com dígito verificador inválido: " + valor
            );
        }
    }

    /**
     * Valida o dígito verificador do CPF usando o algoritmo módulo 11.
     *
     * <p>Algoritmo: calcula dois dígitos verificadores a partir dos primeiros
     * 9 dígitos. Rejeita CPFs com todos os dígitos iguais (ex: "11111111111"),
     * que são matematicamente válidos pelo algoritmo mas não são CPFs reais.</p>
     *
     * @param digitos string com exatamente 11 dígitos numéricos
     * @return {@code true} se o dígito verificador é válido
     */
    private static boolean cpfComDigitoVerificadorValido(String digitos) {
        // CPFs com todos os dígitos iguais são inválidos (11111111111, 22222222222, etc.)
        if (digitos.chars().distinct().count() == 1) {
            return false;
        }

        // Primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;

        if (primeiroDigito != Character.getNumericValue(digitos.charAt(9))) {
            return false;
        }

        // Segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(digitos.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;

        return segundoDigito == Character.getNumericValue(digitos.charAt(10));
    }

    /**
     * Retorna o CPF formatado com máscara de exibição "xxx.xxx.xxx-xx".
     *
     * <p>O armazenamento interno é sempre sem máscara (apenas 11 dígitos).
     * Use este método apenas para exibição na interface.</p>
     *
     * @return CPF formatado como "xxx.xxx.xxx-xx"
     */
    public String formatado() {
        return String.format("%s.%s.%s-%s",
            valor.substring(0, 3),
            valor.substring(3, 6),
            valor.substring(6, 9),
            valor.substring(9, 11)
        );
    }
}
