package br.com.escola.matricula.model;

import java.time.LocalDateTime;
import java.util.UUID;

// ANTI-PADRAO: Entidade Anêmica (DIAG-02) + Acoplamento ao Banco (DIAG-06)
// disciplina é String simples — no módulo DDD é NomeDisciplina (Value Object com
// validação de comprimento mínimo e sem espaços em branco).
// adicionadaEm espelha a coluna adicionada_em da tabela itens_matricula (DIAG-06).
//
// Contraste: erp-matricula-ddd/.../dominio/modelo/ItemMatricula.java é um record
// imutável com NomeDisciplina como Value Object — impossível criar um item com
// nome de disciplina inválido.
public class ItemMatricula {

    private UUID id;
    private UUID matriculaId;       // igual à coluna matricula_id — ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
    private String disciplina;      // String simples — no módulo DDD é NomeDisciplina (VO validado)
    private LocalDateTime adicionadaEm; // espelha coluna adicionada_em da tabela itens_matricula

    // Getters e setters — mutável, sem validação
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMatriculaId() {
        return matriculaId;
    }

    public void setMatriculaId(UUID matriculaId) {
        this.matriculaId = matriculaId;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public LocalDateTime getAdicionadaEm() {
        return adicionadaEm;
    }

    public void setAdicionadaEm(LocalDateTime adicionadaEm) {
        this.adicionadaEm = adicionadaEm;
    }

}
