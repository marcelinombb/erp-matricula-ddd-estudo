package br.com.escola.matricula.model;

import java.util.UUID;

// ANTI-PADRAO: Entidade Anêmica (DIAG-02)
// Sem periodoEstaAberto() — quem verifica se o período está aberto? O MatriculaServiceImpl.
// A lógica de verificação de período vive no Service, não na entidade que detém os dados.
// Consequência: quando a regra de abertura de período muda, é preciso varrer todos os
// Services para encontrar onde essa verificação é feita.
//
// Contraste: erp-matricula-ddd/.../dominio/modelo/Turma.java tem periodoEstaAberto()
// encapsulando a regra diretamente na entidade que possui as datas.
public class Turma {

    private UUID id;
    private String codigo;
    private String periodoInicio; // String simples — no DDD seria um PeriodoLetivo (Value Object)
    private String periodoFim;    // String simples — sem validação de período válido

    // Getters e setters — sem comportamento de domínio
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

}
