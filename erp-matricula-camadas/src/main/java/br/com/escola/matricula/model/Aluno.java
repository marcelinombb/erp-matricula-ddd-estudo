package br.com.escola.matricula.model;

import java.util.UUID;

// ANTI-PADRAO: Entidade Anêmica (DIAG-02)
// Aluno como data class sem comportamento.
// Contraste: erp-matricula-ddd/.../dominio/modelo/Aluno.java tem estaAtivo() e desativar()
// com semântica de negócio.
// cpf é String simples — no módulo DDD é Cpf (Value Object com validação de formato).
public class Aluno {

    private UUID id;
    private String cpf;   // String simples — no módulo DDD é Cpf (Value Object validado)
    private String nome;
    private boolean ativo;

    // Getters e setters — sem validação, sem comportamento de domínio
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // isAtivo() — getter booleano simples.
    // Contraste: erp-matricula-ddd usa estaAtivo() como método com semântica de negócio.
    // A diferença de nomes (is vs. esta) indica a diferença de intenção:
    // isAtivo() é um accessor; estaAtivo() é uma pergunta de domínio.
    public boolean isAtivo() {
        return ativo;
    }

    // setAtivo() — setter público sem semântica de domínio.
    // No módulo DDD, a desativação ocorre pelo método desativar() que registra
    // evento de domínio e protege a invariante. Aqui, qualquer código pode
    // chamar setAtivo(false) sem garantias de consistência.
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

}
