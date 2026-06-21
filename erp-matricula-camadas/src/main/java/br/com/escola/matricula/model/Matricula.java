package br.com.escola.matricula.model;

import java.util.UUID;

// ANTI-PADRAO: Entidade Anêmica (DIAG-02)
// Esta classe não tem comportamento. Ela é um container de dados —
// um mapeamento 1:1 com a tabela matriculas.
// Quem protege a invariante "máximo 6 disciplinas"? O MatriculaServiceImpl.
// Quem protege "matrícula cancelada não aceita disciplinas"? O MatriculaServiceImpl.
// A entidade não pode se defender — ela é passiva.
//
// Contraste: erp-matricula-ddd/.../dominio/modelo/Matricula.java tem
// adicionarDisciplina(), cancelar(), e protege suas invariantes internamente.
public class Matricula {

    private UUID id;
    private UUID alunoId;       // igual à coluna aluno_id — ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
    private UUID turmaId;       // igual à coluna turma_id — ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
    private String periodoInicio;
    private String periodoFim;
    private String status;      // String livre: "ATIVA", "CANCELADA" — sem tipo seguro; no DDD é StatusMatricula (sealed interface)

    // Getters e setters — sem lógica, sem validação de invariantes
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(UUID alunoId) {
        this.alunoId = alunoId;
    }

    public UUID getTurmaId() {
        return turmaId;
    }

    public void setTurmaId(UUID turmaId) {
        this.turmaId = turmaId;
    }

    public String getPeriodoInicio() {
        return periodoInicio;
    }

    public void setPeriodoInicio(String periodoInicio) {
        this.periodoInicio = periodoInicio;
    }

    public String getPeriodoFim() {
        return periodoFim;
    }

    public void setPeriodoFim(String periodoFim) {
        this.periodoFim = periodoFim;
    }

    public String getStatus() {
        return status;
    }

    // setStatus sem validação de transição — qualquer String é aceita.
    // No módulo DDD, o status é StatusMatricula (sealed interface) e a
    // transição é protegida pelo método cancelar() dentro da própria entidade.
    public void setStatus(String status) {
        this.status = status;
    }

}
