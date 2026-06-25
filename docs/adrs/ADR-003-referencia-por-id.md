# ADR-003: Referência Entre Aggregates por ID, Não por Objeto

**Status:** Aceito (revisado em 2026-06-25)
**Data:** 2026-06-20
**Revisão:** 2026-06-25 — IDs tipados substituídos por UUID direto (ver seção Revisão)
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
    private UUID alunoId;          // apenas o identificador
    private UUID turmaId;          // apenas o identificador
    private PeriodoLetivo periodo; // Value Object: faz parte do Aggregate
}
```

Com referência por objeto, qualquer operação em `Matrícula` carrega o `Aluno` completo — mesmo quando o BC Matrícula só precisa saber se o aluno está ativo. Isso cria acoplamento entre Aggregates de BCs distintos e viola o princípio de que cada BC tem seu próprio modelo, independente dos demais.

No schema SQL, essa decisão se reflete assim: a tabela `matricula` tem uma coluna `aluno_id UUID`, mas **sem** `FOREIGN KEY REFERENCES alunos(id)`. A ausência de FK entre tabelas de BCs diferentes é intencional — os BCs podem estar em bancos distintos no futuro, e FK relacional entre bancos distintos é impossível. A consistência entre os contextos é garantida por Domain Events, não por FK relacional.

## Alternativas Consideradas

### Opção A: Referência por Objeto Aluno Completo

**Prós:**
- Acesso conveniente a todos os dados do aluno diretamente do Aggregate

**Contras:**
- Acopla o BC Matrícula ao modelo de domínio do BC Aluno
- Carrega dados desnecessários

### Opção B: FOREIGN KEY Relacional Entre Tabelas de BCs Distintos

**Prós:**
- Integridade referencial garantida pelo banco de dados

**Contras:**
- Cria acoplamento de schema entre Bounded Contexts
- Impossível quando BCs estão em bancos distintos

### Opção C: Referência por ID Apenas para BCs Externos (Decisão Tomada)

**Prós:**
- `ItemMatricula` faz parte do Aggregate `Matricula` — carregá-lo por objeto é correto
- Referências externas (`alunoId`, `turmaId`) são apenas UUIDs — sem carregar objetos de outros BCs

**Contras:**
- O Application Service precisa carregar `Aluno` e `Matricula` separadamente quando ambos são necessários

## Decisão

Aggregates de Bounded Contexts diferentes são referenciados apenas por **UUID**. Entidades internas ao mesmo Aggregate (como `ItemMatricula` dentro de `Matricula`) são referenciadas por objeto.

No schema SQL: `aluno_id UUID NOT NULL` na tabela `matricula`, sem `FOREIGN KEY REFERENCES alunos(id)`.

## Revisão de 2026-06-25: Remoção dos IDs Tipados

A versão original desta ADR adotava `AlunoId`, `MatriculaId` e `TurmaId` como `record`s Java com o argumento de segurança em tempo de compilação:

```java
// Motivação original — detectar inversão de parâmetros em compilação
public void matricular(AlunoId alunoId, TurmaId turmaId) { ... }
```

**Por que foram removidos:**

O custo de manutenção dos wrappers supera o benefício na prática deste projeto:

1. **Value Objects têm comportamento — IDs não.** O princípio central do projeto é que VOs existem para encapsular regras e comportamento (`Cpf` valida dígitos, `PeriodoLetivo` calcula semestre, `NomeDisciplina` normaliza e limita 100 chars). `AlunoId(UUID valor)` não tem comportamento — é apenas um `UUID` embrulhado sem lógica.

2. **O custo é real:** cada novo ID requer um record, um TypeHandler MyBatis, um import em cada arquivo que o usa, e `.valor()` em toda chamada. São 3 arquivos a menos, ~800 linhas removidas, e todos os métodos ficaram mais diretos.

3. **A proteção de inversão de parâmetros raramente acontece:** construtores com múltiplos UUIDs do mesmo tipo são o cenário de risco. No Aggregate `Matricula`, o construtor privado de criação não é chamado diretamente pelo código de aplicação — o factory method `criar()` é chamado com `aluno.getId()` e `turma.getId()`, onde os tipos já estão associados aos seus objetos.

**O que permanece inalterado:**

- A regra central desta ADR: referência por ID entre Aggregates de BCs distintos — `UUID`, não objeto completo
- A ausência de FK relacional entre tabelas de BCs diferentes
- `ItemMatricula` continua sendo referenciado por objeto (é parte do mesmo Aggregate)

**VOs mantidos — por ter comportamento real:**

| Value Object | Por que fica |
|---|---|
| `Cpf` | Validação algoritmo módulo 11 |
| `PeriodoLetivo` | Cálculo de semestre, descricao(), comparação |
| `NomeDisciplina` | Strip, validação de branco e máx. 100 chars |

## Consequências

### Positivas

- Aggregates menores e focados — `Matricula` carrega apenas os dados necessários para suas invariantes
- Sem carga desnecessária de dados: para verificar se um aluno pode se matricular, busca-se apenas `UUID` + status ativo
- Possibilidade de BCs em bancos distintos no futuro: sem FK relacional entre tabelas de contextos diferentes

### Negativas (Trade-offs)

- O Application Service precisa carregar `Aluno` e `Matricula` separadamente quando ambos são necessários
- Sem FK relacional entre tabelas de BCs distintos, a consistência depende da lógica da aplicação e dos Domain Events
- Sem IDs tipados, uma inversão de parâmetros `(UUID alunoId, UUID turmaId)` não é detectada pelo compilador — requer atenção nos pontos de construção do Aggregate

## Referências

- Consistency Boundary — James Hickey: https://www.jamesmichaelhickey.com/consistency-boundary/
- Implementing Domain-Driven Design — Vaughn Vernon (Aggregates e referências por ID)
- contexto-matricula.md §15 (Relacionamentos Entre Aggregates)

## Na prática

**[Matricula.java](../../erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java)** — os campos são `UUID alunoId` e `UUID turmaId` (não `Aluno aluno` e `Turma turma`). O Aggregate Matrícula não carrega o objeto `Aluno` completo — carrega apenas o UUID. Isso significa que uma mudança no modelo de `Aluno` não invalida automaticamente o Aggregate Matrícula.

**[MatriculaController.java](../../erp-matricula-ddd/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java)** — o Controller constrói um `Aluno` placeholder com dados mínimos porque o BC Matrícula não tem repositório de `Aluno`. O `VerificadorElegibilidadeMatricula` precisa apenas de `aluno.estaAtivo()`.

Cada Bounded Context gerencia seu próprio ciclo de vida: o BC Matrícula não tem repositório de `Aluno` porque o modelo de `Aluno` relevante para matrícula é diferente do modelo relevante para o BC Financeiro e para o BC Acadêmico.
