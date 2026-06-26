# Entidades — Matrícula Escolar

Vimos que `Cpf` é imutável — dois CPFs com o mesmo valor são o mesmo CPF. Não importa se foram criados em momentos diferentes ou por partes diferentes do sistema: `new Cpf("12345678909").equals(new Cpf("12345678909"))` é sempre verdadeiro. O `record` Java 21 garante isso automaticamente.

Mas o que acontece com `Aluno`?

Um aluno pode mudar de nome após casamento. Pode mudar de endereço. Pode ter seu status alterado de ativo para inativo por inadimplência, e depois reativado. Em cada uma dessas mudanças, o aluno continua sendo o mesmo aluno — com o mesmo histórico, as mesmas matrículas passadas, as mesmas disciplinas cursadas. A identidade do aluno persiste mesmo quando seus dados mudam. Não faz sentido dizer "dois alunos com o mesmo nome são o mesmo aluno" — dois alunos podem ter nomes idênticos e serem pessoas completamente diferentes.

Essa distinção é central: quando um objeto tem **identidade própria que persiste no tempo**, independente de seus atributos, ele é uma **Entidade**. O que distingue uma Entidade de um Value Object não é o número de campos nem a complexidade — é a semântica. `Cpf` é comparado pelo valor; `Aluno` é comparado pelo ID. `Cpf` não muda; `Aluno` pode mudar de estado. `Cpf` é um `record`; `Aluno` é uma `class`.

## Entidade vs Value Object — Contraste

| Característica | Value Object (ex: `Cpf`) | Entidade (ex: `Aluno`) |
|---------------|--------------------------|------------------------|
| Identidade | Sem identidade — comparado por valor | Tem identidade — `UUID id` persiste |
| Imutabilidade | Imutável — `record` Java 21 | Pode mudar estado (status ativo/inativo) |
| `equals`/`hashCode` | Por todos os campos (gerado pelo `record`) | Apenas pelo `id` — implementado manualmente |
| Tipo Java 21 | `record` — imutabilidade garantida pelo compilador | `class` — permite estado mutável |
| Ciclo de vida | Criado e descartado; substituído por novo valor | Criado, modificado ao longo do tempo, arquivado |
| Persistência | Pode ser coluna inline na tabela da Entidade | Tem linha própria na tabela, identificada por `id` |

---

## Entidades do Domínio de Matrícula

### Aluno

**Identidade:** `UUID id` — imutável; o aluno ao qual ele pertence pode mudar de status, mas o ID nunca muda.

**Ciclo de vida:** Criado quando cadastrado na secretaria. Pode ser inativado (por inadimplência ou solicitação). Pode ser reativado. Nunca é excluído — o histórico de matrículas depende da existência do registro.

**Responsabilidades:** Carregar o status que determina elegibilidade para matrícula. O método `estaAtivo()` é a interface que o `VerificadorElegibilidadeMatricula` usa — sem precisar conhecer a lógica interna de ativação/inativação.

```java
// Java 21: class comum com estado mutável (não record)
// DDD fit: Entidade = identidade estável + estado mutável ao longo do tempo
public class Aluno {

    private final UUID id;  // identidade — final, nunca muda
    private boolean ativo;  // estado — pode mudar, por isso não é record

    // NÃO é um record porque status pode mudar — record é imutável
    // Um record com campo boolean gera um objeto novo a cada mudança de estado,
    // perdendo a identidade que distingue Entidades de Value Objects.

    public Aluno(UUID id, boolean ativo) {
        this.id = Objects.requireNonNull(id, "Aluno deve ter um id");
        this.ativo = ativo;
    }

    public UUID getId() { return id; }

    public boolean estaAtivo() { return ativo; }

    // equals/hashCode baseados APENAS no id — nunca em atributos como nome ou CPF
    // Dois alunos com o mesmo nome são pessoas diferentes; dois Aluno com o mesmo id são o mesmo.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aluno outro)) return false;  // pattern matching Java 16+, finalizou no 21
        return id.equals(outro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
```

> **Por que não é um record?** `Aluno` pode mudar de status (ativo → inativo). Um `record` Java 21 é imutável por design — campos `final`, sem setters. Entidades têm ciclo de vida com estado mutável; para isso usamos `class`. O `record` é perfeito para Value Objects (sem identidade, comparados por valor); a `class` é necessária quando o objeto tem identidade que persiste enquanto seus atributos mudam.

---

### Turma

**Identidade:** `UUID id` — mesmo padrão de `Aluno`.

**Ciclo de vida:** Criada pela secretaria para um período letivo específico, com capacidade máxima definida. Em v1, a verificação de vagas disponíveis não está implementada no escopo do Aggregate — a turma existe como referência para a matrícula.

**Responsabilidades:** Fornecer o UUID que a `Matricula` usa como referência. Carregar a capacidade máxima e o período letivo ao qual pertence.

> `Turma` é referenciada por UUID no Aggregate `Matricula` — sem carregar o objeto completo. Ver [ADR-003](../adrs/ADR-003-referencia-por-id.md).

```java
// Java 21: class para Entidade com identidade estável
// DDD fit: Turma tem ID próprio e dados que descrevem a oferta do período letivo
public class Turma {

    private final UUID id;
    private final int capacidadeMaxima;
    private final PeriodoLetivo periodo;

    public Turma(UUID id, int capacidadeMaxima, PeriodoLetivo periodo) {
        this.id = Objects.requireNonNull(id, "Turma deve ter um id");
        if (capacidadeMaxima <= 0) {
            throw new IllegalArgumentException("Capacidade máxima deve ser positiva: " + capacidadeMaxima);
        }
        this.capacidadeMaxima = capacidadeMaxima;
        this.periodo = Objects.requireNonNull(periodo, "Turma deve ter um período letivo");
    }

    public UUID getId() { return id; }

    public int getCapacidadeMaxima() { return capacidadeMaxima; }

    public PeriodoLetivo getPeriodo() { return periodo; }

    // verificação de vagas disponíveis: v2 — ver STATE.md open decisions
    // No v1, o foco está nas invariantes de Matricula; Turma existe como referência.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Turma outra)) return false;
        return id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
```

---

## Nota sobre `ItemMatricula`

Existe uma terceira entidade, `ItemMatricula`, que representa uma disciplina incluída na matrícula. Ela não é documentada aqui porque não tem significado fora do contexto do Aggregate `Matricula` — é uma entidade interna. Criar um `ItemMatricula` sem `Matricula` não faz sentido no domínio: uma disciplina incluída existe como parte de uma matrícula, não como objeto independente. Sua documentação está em [agregados.md](./agregados.md).

---

## Erros Comuns

### Erro 1: `equals` por conteúdo em vez de por identidade

O desenvolvedor familiarizado com Value Objects pode implementar `equals` de `Aluno` comparando nome, CPF, endereço — como se fosse um registro a ser desduplicado. O problema: dois alunos podem ter o mesmo nome, o mesmo CPF até (em dados corrompidos ou homônimos), e ainda serem pessoas diferentes no sistema.

```java
// ERRADO — equals por atributos (como se Aluno fosse um Value Object)
@Override
public boolean equals(Object o) {
    if (!(o instanceof Aluno outro)) return false;
    return this.nome.equals(outro.nome)
        && this.cpf.equals(outro.cpf);
    // Dois alunos podem ter o mesmo nome — o que os distingue é o ID.
    // Essa implementação trataria como duplicatas alunos distintos com dados similares.
}
```

```java
// CERTO — equals apenas pelo id (o que define a identidade de uma Entidade)
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Aluno outro)) return false;
    return id.equals(outro.id);
    // Dois alunos são iguais se e somente se têm o mesmo UUID id.
    // Não importa se o nome mudou, se o CPF foi corrigido — o ID define quem é quem.
}
```

### Erro 2: Confundir identidade com igualdade estrutural

Com UUID como identificador, a responsabilidade de implementar `equals`/`hashCode` corretamente cai sobre o desenvolvedor. O erro comum é comparar por atributos (como Value Object) em vez de por ID (como Entidade).

```java
// ERRADO — equals por atributos em uma Entidade
public class Aluno {
    private UUID id;
    private String nome;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Aluno outro)) return false;
        return this.nome.equals(outro.nome); // compara pelo nome — dois alunos com o mesmo nome seriam "iguais"
    }
}
```

```java
// CERTO — equals pelo UUID id
public class Aluno {
    private final UUID id;
    private String nome;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aluno outro)) return false;
        return id.equals(outro.id); // identidade pelo UUID — imutável
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
// Ver [ADR-003](../adrs/ADR-003-referencia-por-id.md) para o princípio de referência por ID.
```
