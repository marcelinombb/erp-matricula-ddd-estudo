# Entidades — Aplicadas na Arquitetura Tradicional

## O que é

Entidade é um objeto com identidade própria que persiste no tempo. Dois objetos com os mesmos
atributos são objetos diferentes se têm IDs diferentes. Um aluno que muda de nome continua
sendo o mesmo aluno — o que define "quem ele é" é o seu identificador, não seus dados.

Identidade por ID é o que separa Entidade de Value Object. Um `PeriodoLetivo(2026, 1)` é
idêntico a qualquer outro `PeriodoLetivo(2026, 1)` — não tem identidade própria, é identificado
pelos seus valores. Um `Aluno` com o mesmo nome que outro `Aluno` é uma pessoa diferente.

Para teoria completa, ver `docs/02-design-tatico/entidades.md`.

---

## Manifestação no módulo camadas (ANTES)

Arquivo de referência:
`erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java`

```java
// ANTES: Entidade anêmica — apenas dados, sem comportamento, sem identidade explícita
// Arquivo: erp-matricula-camadas/src/main/java/.../model/Matricula.java
public class Matricula {
    private UUID id;
    private String status;  // String livre: "ATIVA", "CANCELADA", "CONCLUIDA"

    // Sem validação de transição — qualquer String é aceita
    public void setStatus(String status) { this.status = status; }

    // Sem adicionarDisciplina(), sem cancelar(), sem estaAtiva()
    // Sem equals/hashCode explícito — usa Object.equals() (por referência)
    // ... demais getters e setters
}
```

Arquivo Java completo:
`erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java`

Ver também: `docs/00-ddd-sem-mudar-arquitetura/02-entidade-anemica.md` para o ângulo do
comportamento ausente (DIAG-02).

---

## Aplicação no módulo DDD (DEPOIS)

Arquivo de referência:
`erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Aluno.java`

```java
// DEPOIS: Entidade com identidade explícita e comportamento encapsulado
// Arquivo: erp-matricula-ddd/src/main/java/.../dominio/modelo/Aluno.java
public class Aluno {

    private final UUID id;  // identidade imutável — nunca muda durante o ciclo de vida
    private boolean ativo;     // estado mutável — aluno pode ser desativado

    // Semântica de negócio — não getter booleano isAtivo()
    public boolean estaAtivo() { return this.ativo; }

    // Comportamento encapsulado — não setter setAtivo(false)
    public void desativar() { this.ativo = false; }

    // equals/hashCode baseados APENAS no UUID id
    // Dois alunos são o mesmo se têm o mesmo ID — não importa se o nome mudou
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Aluno outro)) return false;
        return id.equals(outro.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
```

Arquivo Java:
`erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Aluno.java`

---

## O contraste de identidade

Dois alunos com o mesmo nome são pessoas diferentes — `equals` usa `UUID id`, não `nome`.
Dois `PeriodoLetivo(2026, 1)` são o mesmo período — é um Value Object, `equals` usa `ano` e
`semestre`. Esta distinção define quando usar Entidade (tem identidade que persiste no tempo)
vs. Value Object (sem identidade própria, identificado pelos seus valores).

---

## O que foi ganho

`desativar()` encapsula a transição de estado — código externo não pode chamar `setAtivo(false)`
e criar um aluno em estado inconsistente. A identidade explícita por UUID garante que um
aluno transferido de turma (com dados atualizados) é reconhecido como o mesmo aluno em qualquer
coleção Java que use `equals/hashCode`.
