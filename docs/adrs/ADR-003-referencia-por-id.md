# ADR-003: Referência Entre Aggregates por ID, Não por Objeto

**Status:** Aceito
**Data:** 2026-06-20
**Contexto da fase:** Fase 1 — Design Estratégico

## Contexto

Em DDD, a regra é que Aggregates de Bounded Contexts diferentes não se referenciam por objeto Java — apenas por ID. Esta decisão parece técnica à primeira vista, mas tem consequências de design profundas. Sem essa regra, é tentador carregar o objeto completo "para facilitar" o acesso a dados:

```java
// EVITAR: referência por objeto entre Aggregates
public class Matricula {
    private Aluno aluno;           // carrega o Aggregate Aluno inteiro
    private Turma turma;           // carrega o Aggregate Turma inteiro
    private PeriodoLetivo periodo;
}
```

```java
// PREFERIR: referência por ID entre Aggregates
public class Matricula {
    private AlunoId alunoId;       // apenas o identificador
    private TurmaId turmaId;       // apenas o identificador
    private PeriodoLetivo periodo; // Value Object: faz parte do Aggregate
}
```

Com referência por objeto, qualquer operação em `Matrícula` carrega o `Aluno` completo — com todos os dados de endereço, histórico, contatos e responsáveis financeiros que o BC Financeiro ou Acadêmico precisam — mesmo quando o BC Matrícula só precisa saber se o aluno está ativo. Isso cria acoplamento entre Aggregates de BCs distintos e viola o princípio de que cada BC tem seu próprio modelo, independente dos demais.

No schema SQL, essa decisão se reflete da seguinte forma: a tabela `matricula` tem uma coluna `aluno_id UUID`, mas **sem** `FOREIGN KEY REFERENCES alunos(id)`. A ausência de FK entre tabelas de BCs diferentes é intencional — os BCs podem estar em bancos distintos no futuro, e FK relacional entre bancos distintos é impossível. A consistência entre os contextos é garantida por Domain Events, não por FK relacional.

## Alternativas Consideradas

### Opção A: Referência por Objeto Aluno Completo

**Prós:**
- Acesso conveniente a todos os dados do aluno diretamente do Aggregate
- Sem necessidade de carregar `Aluno` separadamente no Application Service

**Contras:**
- Acopla o BC Matrícula ao modelo de domínio do BC Aluno — qualquer mudança no modelo de `Aluno` pode quebrar `Matricula`
- Viola o princípio de que cada BC tem seu próprio modelo de Aluno (veja `bounded-contexts.md` — o Aluno do BC Matrícula é diferente do Aluno do BC Financeiro)
- Carrega dados desnecessários: para verificar se um aluno pode se matricular, não precisamos de endereço, responsáveis ou histórico de pagamentos

### Opção B: FOREIGN KEY Relacional Entre Tabelas de BCs Distintos

**Prós:**
- Integridade referencial garantida pelo banco de dados
- Conveniente para relatórios que precisam cruzar dados de múltiplos contextos

**Contras:**
- Cria acoplamento de schema entre Bounded Contexts: se o BC Aluno renomear ou reestruturar sua tabela, o banco de dados de Matrícula quebra
- Impossível quando BCs estão em bancos distintos (cenário comum em produção com microserviços)
- Esconde o acoplamento — o desenvolvedor que adiciona uma FK não percebe que está violando a fronteira entre BCs

### Opção C: Referência por ID Apenas para BCs Externos, Objetos Para Entidades do Mesmo BC (Decisão Tomada)

**Prós:**
- Equilíbrio entre conveniência (entidades internas são objetos) e isolamento (referências externas são IDs)
- `ItemMatricula` faz parte do Aggregate `Matricula` — carregá-lo por objeto é correto
- `AlunoId` e `TurmaId` são referências externas — IDs tipados evitam confusão em tempo de compilação

**Contras:**
- O desenvolvedor precisa aprender quando usar ID e quando usar objeto — a regra "mesmo BC = objeto, BC diferente = ID" precisa ser explícita
- Requer disciplina da equipe para não misturar os dois padrões

## Decisão

Aggregates de Bounded Contexts diferentes são referenciados apenas por **ID tipado** (`AlunoId`, `TurmaId`). Esses IDs são `record`s Java (Value Objects), não `UUID`s crus — evitam confusão entre IDs de tipos diferentes em tempo de compilação:

```java
// Sem IDs tipados — erro silencioso possível
public void matricular(UUID alunoId, UUID turmaId) {
    // Alguém pode passar os parâmetros na ordem errada
    // e o compilador não detecta
}

// Com IDs tipados — erro detectado em compilação
public void matricular(AlunoId alunoId, TurmaId turmaId) {
    // Impossível passar TurmaId onde AlunoId é esperado
}
```

Entidades internas ao mesmo Aggregate (como `ItemMatricula` dentro de `Matricula`) são referenciadas por objeto — é correto e necessário que o Aggregate carregue seus próprios filhos.

No schema SQL: `aluno_id UUID NOT NULL` na tabela `matricula`, sem `FOREIGN KEY REFERENCES alunos(id)`. A ausência de FK é comentada no script de migração para deixar a intenção explícita.

## Consequências

### Positivas

- Aggregates menores e focados — `Matricula` carrega apenas os dados necessários para suas invariantes, não o modelo completo de `Aluno` e `Turma`
- Sem carga desnecessária de dados: para verificar se um aluno pode se matricular, busca-se apenas `AlunoId` + status ativo, não o objeto completo com 20 campos
- Possibilidade de BCs em bancos distintos no futuro: sem FK relacional entre tabelas de contextos diferentes, a migração para bancos separados não exige quebrar o schema
- Detecção em tempo de compilação de confusão entre IDs: `AlunoId` não é `TurmaId` — o compilador Java rejeita a troca

### Negativas (Trade-offs)

- O Application Service precisa carregar `Aluno` e `Matricula` separadamente quando ambos são necessários — mais código de orquestração. O desenvolvedor acostumado a navegar `matricula.getAluno().getNome()` precisará fazer duas queries explícitas
- Sem FK relacional entre tabelas de BCs distintos, a consistência depende da lógica da aplicação e dos Domain Events — se um evento for perdido, dados inconsistentes podem existir sem que o banco de dados detecte

## Referências

- Consistency Boundary — James Hickey: https://www.jamesmichaelhickey.com/consistency-boundary/
- Implementing Domain-Driven Design — Vaughn Vernon (Aggregates e referências por ID)
- contexto-matricula.md §15 (Relacionamentos Entre Aggregates)
