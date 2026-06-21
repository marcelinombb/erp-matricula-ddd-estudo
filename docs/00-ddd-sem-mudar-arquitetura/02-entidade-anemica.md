# Anti-padrão: Entidade Anêmica

## O que é

Uma classe com campos, getters e setters sem comportamento. O objeto existe para carregar dados de um lugar para outro — um container passivo. Toda lógica de negócio que envolve esse objeto vive em outro lugar, tipicamente no Service.

---

## Manifestação no módulo camadas

`Matricula.java` no módulo camadas é uma classe com campos privados e accessors públicos. Não há `adicionarDisciplina()`, não há `cancelar()`, não há verificação de estado antes de uma mutação:

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java

// ANTI-PADRAO: Entidade Anêmica (DIAG-02)
// Esta classe não tem comportamento. Ela é um container de dados —
// um mapeamento próximo à tabela matriculas.
// Quem protege a invariante "máximo 6 disciplinas"? O Service.
// Quem protege "matrícula cancelada não aceita novas disciplinas"? O Service.
// A entidade não pode se defender — ela é passiva.
public class Matricula {
    private UUID id;
    private UUID alunoId;
    private UUID turmaId;
    private String periodoInicio;
    private String periodoFim;
    private String status;  // String livre: "ATIVA", "CANCELADA", "CONCLUIDA"

    public String getStatus() { return status; }

    // Sem validação de transição — qualquer chamador pode definir qualquer status
    public void setStatus(String status) { this.status = status; }

    // ... demais getters e setters
}
```

Arquivo Java completo: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java`

---

## O que falta

Uma entidade com comportamento teria métodos que protegem suas invariantes:

- `adicionarDisciplina(String disciplina)` — verifica o limite de 6 antes de adicionar
- `cancelar()` — transiciona o status para CANCELADA, rejeita se já cancelada
- `estaAtiva()` — semântica de negócio, não apenas getter de campo

Sem esses métodos, qualquer código que precise dessas operações implementa a lógica localmente. O limite de 6 disciplinas aparece no `MatriculaServiceImpl` como uma chamada a `COUNT(*)` no banco. O cancelamento é um `setStatus("CANCELADA")` direto.

---

## Contraste DDD

No módulo DDD, `Matricula.java` é o Aggregate Root — protege suas invariantes internamente:

```java
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java

// Entidade rica com comportamento encapsulado.
// A invariante "máximo 6 disciplinas" vive aqui, não no Service.
public void adicionarDisciplina(NomeDisciplina nomeDisciplina) {
    if (this.status instanceof StatusMatricula.Cancelada) {
        throw new MatriculaJaCanceladaException(this.id);
    }
    if (this.itens.size() >= 6) {
        throw new LimiteDisciplinasAtingidoException(this.id);
    }
    var item = new ItemMatricula(nomeDisciplina);
    this.itens.add(item);
    this.eventos.add(new DisciplinaAdicionadaEvent(this.id, nomeDisciplina));
}
```

Arquivo Java: `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java`

Um desenvolvedor que lê `Matricula.java` no módulo DDD encontra todas as regras que envolvem uma matrícula. A classe conta sua própria história.

---

## Por que `status` como String

No módulo camadas, `status = "ATIVA"` é uma string livre. Nada no compilador impede `setStatus("ACTIVA")` (erro de digitação) ou `setStatus(null)`. A validade do status depende de boa vontade de quem escreve o Service.

No módulo DDD, `StatusMatricula` é uma `sealed interface` com implementações `Ativa`, `Cancelada` e `Concluida`. O compilador garante que apenas estados válidos existem. Um `switch` sobre `StatusMatricula` sem tratar todos os casos falha na compilação — não em tempo de execução com um `NullPointerException`.

Essa diferença aparece em `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/StatusMatricula.java`.
