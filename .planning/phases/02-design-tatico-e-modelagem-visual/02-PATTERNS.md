# Phase 2: Design Tatico e Modelagem Visual - Pattern Map

**Mapped:** 2026-06-20
**Files analyzed:** 8 (7 new + 1 updated)
**Analogs found:** 8 / 8

---

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `docs/02-design-tatico/value-objects.md` | doc-pattern | transform | `docs/01-design-estrategico/linguagem-ubiqua.md` | role-match |
| `docs/02-design-tatico/entidades.md` | doc-pattern | transform | `docs/01-design-estrategico/linguagem-ubiqua.md` | role-match |
| `docs/02-design-tatico/agregados.md` | doc-pattern | transform | `docs/01-design-estrategico/bounded-contexts.md` | role-match |
| `docs/02-design-tatico/domain-services.md` | doc-pattern | transform | `docs/01-design-estrategico/bounded-contexts.md` | role-match |
| `docs/02-design-tatico/domain-events.md` | doc-pattern | event-driven | `docs/01-design-estrategico/context-map.md` | exact |
| `docs/02-design-tatico/repositorios.md` | doc-pattern | transform | `docs/adrs/ADR-001-mybatis-vs-jpa.md` | exact |
| `docs/02-design-tatico/modelagem.md` | doc-diagram | transform | `docs/01-design-estrategico/context-map.md` | exact |
| `README.md` | config | request-response | `README.md` (self — existing structure) | exact |

---

## Pattern Assignments

### `docs/02-design-tatico/value-objects.md` (doc-pattern, transform)

**Analog:** `docs/01-design-estrategico/linguagem-ubiqua.md`

**Opening pattern — bottom-up, problem first** (lines 1-5):

The analog opens with the purpose of the concept in plain prose before introducing any terminology. Copy this approach: start with a concrete problem in the Matrícula domain ("O que aconteceria se CPF fosse uma String?"), then introduce the pattern name only after the problem is established. Never start with "Em DDD, um Value Object é...".

**Table pattern** (lines 9-38):

```markdown
| Termo | Definição | BC Dono | Não usar |
|-------|-----------|---------|----------|
```

For `value-objects.md`, adapt this into the VO catalog table (from RESEARCH.md TAT-02):

```markdown
| VO | Tipo Java 21 | Validação no Construtor Compacto | Complexidade |
|----|-------------|----------------------------------|-------------|
| `Cpf` | `record Cpf(String valor)` | Remove máscara, verifica 11 dígitos, algoritmo dígito verificador | ALTA |
| `PeriodoLetivo` | `record PeriodoLetivo(int ano, int semestre)` | `ano >= 2000`, `semestre 1..2` | BAIXA |
| `MatriculaId` | `record MatriculaId(UUID valor)` | `Objects.requireNonNull(valor)` | MÍNIMA |
| `AlunoId` | `record AlunoId(UUID valor)` | `Objects.requireNonNull(valor)` | MÍNIMA |
| `TurmaId` | `record TurmaId(UUID valor)` | `Objects.requireNonNull(valor)` | MÍNIMA |
| `NomeDisciplina` | `record NomeDisciplina(String valor)` | não nulo, não branco, máx 100 chars | BAIXA |
```

**Section structure pattern** — each pattern document follows this shape extracted from Phase 1 docs:

1. Prose introduction — problem-first, no headers for first 2-3 paragraphs
2. `##` section with table showing catalog or comparison
3. `##` section with subsections per item (one `###` per VO/Entidade/etc.)
4. Code snippets inside each `###` — with `// Java 21: ...` comment explaining DDD fit
5. ERRADO/CERTO block inside "Erros Comuns" `##` at the end

**ERRADO/CERTO block pattern** (from RESEARCH.md):

```java
// ERRADO — [descrição do problema, em código típico Spring Boot que o dev reconhece]
@Service
public class MatriculaService {
    // código familiar ao dev target — @Service, @Autowired, String primitivo
}

// CERTO — [descrição da solução DDD]
public class MatricularAlunoUseCase {
    // código DDD: sem @Service, tipos de domínio, sem String primitivo
}
```

**Referência cruzada pattern** (from `linguagem-ubiqua.md` lines 20-38):

```markdown
> **Lição:** [insight concreto]. Ver [link para documento relacionado].
```

Use this blockquote pattern to link to ADRs and Phase 1 docs at the end of sections where the connection is pedagogically important.

---

### `docs/02-design-tatico/entidades.md` (doc-pattern, transform)

**Analog:** `docs/01-design-estrategico/linguagem-ubiqua.md`

**Entidades a cobrir:** `Aluno` e `Turma` com seções `###` próprias. `ItemMatricula` NÃO entra aqui — vai em `agregados.md`. Deixar nota explícita: "ItemMatricula é documentada em agregados.md por não ter significado fora do Aggregate Matricula."

**Comparação central (Conceitos Ambíguos pattern)** (lines 20-38 of `linguagem-ubiqua.md`):

```markdown
## Conceitos Ambíguos

| Contexto | O que importa ... | Dados que o BC mantém |
```

Adaptar para entidades: usar tabela de contraste Entidade vs Value Object — "por que `Aluno` não é um record?" A tabela deve mostrar a diferença de identidade vs valor como o analog mostra a diferença de contexto.

**Java 21 feature comment pattern** (from RESEARCH.md TAT-01):

```java
// Java 21: identidade tipada como record (Value Object)
// AlunoId não é UUID cru — o compilador distingue AlunoId de TurmaId
public record AlunoId(UUID valor) { ... }

// Entidade: identidade estável, estado mutável
// NÃO é um record porque status pode mudar — record é imutável
public class Aluno { ... }
```

O comentário deve sempre ter duas linhas: a primeira explica o fit Java 21, a segunda explica o fit DDD.

---

### `docs/02-design-tatico/agregados.md` (doc-pattern, transform)

**Analog:** `docs/01-design-estrategico/bounded-contexts.md`

**Structure pattern** (lines 34-102 of `bounded-contexts.md`):

O analog usa cabeçalhos `###` por BC com subseções: Responsabilidade → Limites → Linguagem própria → Dados próprios → Regras-chave. Adaptar para Aggregate:

```markdown
### Aggregate Root: Matricula

**Responsabilidade:** [o que o Aggregate protege]

**Limite de consistência:** [o que está dentro vs referência por ID]

**Estrutura interna:**
[diagrama de árvore ASCII mostrando composição]

**Invariantes:** [narrativa + tabela]
```

**Tabela de regras-chave pattern** (from `bounded-contexts.md` lines 56-62):

```markdown
**Regras-chave protegidas pelo Aggregate:**
- Máximo de N disciplinas por matrícula (invariante de capacidade)
- Sem disciplina duplicada na mesma matrícula
- Sem adição de disciplina após cancelamento
```

Para `agregados.md`, expandir em tabela com coluna de exceção lançada (conforme RESEARCH.md TAT-03):

```markdown
| Invariante | Regra | Exceção Lançada |
|-----------|-------|-----------------|
| Limite de disciplinas | Máximo 6 disciplinas por matrícula | `LimiteDisciplinasExcedidoException` |
| Sem duplicidade | Mesma disciplina não pode aparecer duas vezes | `DisciplinaJaMatriculadaException` |
| Estado terminal | Matrícula cancelada não recebe disciplinas | `MatriculaCanceladaException` |
```

**ADR cross-reference pattern** (from `bounded-contexts.md` line 54):

```markdown
**Dados próprios:** Tabelas `matricula`, `matricula_disciplina` e referência a `turma`. Não compartilha tabelas com outros Bounded Contexts...
```

Adaptar: ao mostrar `AlunoId` e `TurmaId` na estrutura do Aggregate, adicionar referência inline:

```markdown
- `AlunoId` (VO — referência por ID, não objeto `Aluno` — ver [ADR-003](../adrs/ADR-003-referencia-por-id.md))
```

**Argumento de concorrência pattern** — bloco blockquote `>` como o da linguagem-ubíqua:

```markdown
> **Por que a invariante pertence ao Aggregate, não ao Service:** Se a verificação do limite de 6 disciplinas estivesse no Service, duas threads concorrentes poderiam cada uma consultar a matrícula, encontrar 5 disciplinas, e adicionar mais uma — resultando em 7 disciplinas no banco. O Aggregate protege a invariante porque a verificação e a modificação acontecem dentro da mesma transação, no mesmo objeto carregado em memória.
```

---

### `docs/02-design-tatico/domain-services.md` (doc-pattern, transform)

**Analog:** `docs/01-design-estrategico/bounded-contexts.md`

**Table pattern para comparação** (lines 20-28 of `bounded-contexts.md`):

```markdown
| Subdomínio | Tipo | Justificativa |
|------------|------|---------------|
```

Adaptar para a tabela de distinção Domain Service vs Application Service (do RESEARCH.md TAT-04):

```markdown
| Aspecto | Domain Service | Application Service (UseCase) |
|---------|---------------|-------------------------------|
| Onde vive | `dominio/` — sem import de framework | `aplicacao/` — pode usar Spring |
| O que faz | Regra de negócio pura que cruza entidades | Orquestra: busca, chama domínio, salva, publica eventos |
```

**Justificativa pattern** — narrativa explicando o "por que não pertence a uma entidade" antes de mostrar o código. Seguir o estilo de `bounded-contexts.md` que apresenta a responsabilidade antes de listar o que o contexto faz: "Responsabilidade: [...]". Para domain-services.md: "Por que não é um método de Aluno? Por que não é um método de Matricula?"

---

### `docs/02-design-tatico/domain-events.md` (doc-pattern, event-driven)

**Analog:** `docs/01-design-estrategico/context-map.md` — match exato em estrutura e propósito.

**Table pattern** (lines 71-77 of `context-map.md`):

```markdown
| Evento | Publicado por | Consumido por | Propósito |
|--------|--------------|---------------|-----------|
| `AlunoMatriculado` | BC Matrícula | BC Financeiro, BC Acadêmico | ... |
```

Usar exatamente esta tabela como ponto de entrada, expandida com coluna "Gatilho" e "Campos" (conforme RESEARCH.md TAT-05):

```markdown
| Evento | Publicado Por | Consumido Por | Gatilho | Campos |
|--------|--------------|---------------|---------|--------|
| `AlunoMatriculado` | BC Matrícula | BC Financeiro, BC Acadêmico | `Matricula.criar()` | `matriculaId`, `alunoId`, `turmaId`, `periodoLetivo`, `ocorridoEm` |
```

**Cross-reference pattern** (from `context-map.md` lines 39-66):

```markdown
> Referência explícita ao `context-map.md` da Fase 1: "O Context Map documenta que BC Matrícula é Upstream (Supplier). O `domain-events.md` detalha o contrato interno de cada evento publicado."
```

**Mecanismo de coleta pattern** — explicar que o Aggregate coleta eventos em lista interna sem depender de Spring, com snippet Java. Seguir o estilo de `context-map.md` que explica o mecanismo antes dos exemplos (linhas 37-65 — Customer/Supplier explicado antes de citar eventos concretos).

---

### `docs/02-design-tatico/repositorios.md` (doc-pattern, transform)

**Analog:** `docs/adrs/ADR-001-mybatis-vs-jpa.md` — match exato: ambos tratam da separação domínio/persistência.

**ERRADO/CERTO pattern** (lines 13-39 of `ADR-001-mybatis-vs-jpa.md`):

```java
// COM JPA — anotações de persistência no modelo de domínio (PROBLEMA)
@Entity
@Table(name = "matriculas")
public class Matricula { ... }

// COM MYBATIS — modelo de domínio limpo (DECISÃO TOMADA)
public class Matricula { ... // Zero imports de framework }
```

Para `repositorios.md`, adaptar diretamente:

```java
// ERRADO — Spring Data JPA: o domínio herda de JpaRepository (dependência de framework)
public interface MatriculaRepository extends JpaRepository<Matricula, UUID> {
    // JpaRepository está em jakarta.persistence — o domínio importa framework
}

// CERTO — Interface pura no domínio, sem herança de framework
public interface MatriculaRepositorio {
    Optional<Matricula> buscarPorId(MatriculaId id);
    // zero imports de framework
}
```

**Verificação em bash pattern** (lines 88-91 of `ADR-001-mybatis-vs-jpa.md`):

```markdown
O domínio (`dominio/`) não importa nenhuma classe do MyBatis. A verificação é simples:

```bash
grep -r "import org.apache.ibatis" src/main/java/br/com/erp/dominio/
# Resultado esperado: nenhuma ocorrência
```
```

Incluir este bloco em `repositorios.md` como verificação prática da separação.

**Consequências positivas/negativas pattern** (lines 98-108 of `ADR-001-mybatis-vs-jpa.md`):

```markdown
### Positivas
- Domínio sem imports de framework — verificável com um `grep`

### Negativas (Trade-offs)
- Mais código de mapeamento escrito manualmente
```

Adaptar em `repositorios.md` como "O que essa escolha implica" ao final do documento.

**ADR cross-reference** obrigatório no início de `repositorios.md`:

```markdown
> Esta decisão é documentada em [ADR-001: MyBatis vs JPA](../adrs/ADR-001-mybatis-vs-jpa.md). O repositório como interface de domínio é a consequência direta dessa escolha arquitetural.
```

---

### `docs/02-design-tatico/modelagem.md` (doc-diagram, transform)

**Analog:** `docs/01-design-estrategico/context-map.md` — único documento de Phase 1 com blocos Mermaid.

**Diagrama intro pattern** (lines 1-7 of `context-map.md`):

```markdown
# Context Map — Matrícula Escolar

O Context Map mostra como os Bounded Contexts se relacionam — quem depende de quem, qual lado define o contrato e quais eventos cruzam as fronteiras. É a visão estratégica de integração do sistema: não descreve como cada contexto funciona internamente, mas como eles conversam e quem tem autoridade sobre cada parte do contrato de comunicação.
```

Para `modelagem.md`, cada diagrama deve seguir esta estrutura:

```markdown
## MOD-01: Diagrama de Classes

[2-3 linhas de contextualização: o que o diagrama mostra, o que observar, qual ADR é referenciado]
O diagrama abaixo mostra as classes do domínio de Matrícula. Observe que `Matricula` referencia `AlunoId` e `TurmaId` — não os objetos `Aluno` e `Turma` — implementando [ADR-003](../adrs/ADR-003-referencia-por-id.md).

```mermaid
[bloco do diagrama]
```

> **O que observar:** [nota após o diagrama, mesmo estilo blockquote `>` dos outros docs]
```

**Mermaid block pattern** (lines 9-29 of `context-map.md`):

```markdown
```mermaid
graph LR
    subgraph MAT["Matrícula — Core Domain [Implementado]"]
```

Usar exatamente este formato de bloco com backtick triplo + `mermaid`. Importante: usar `~` em vez de `<>` para genéricos em `classDiagram` (pitfall do RESEARCH.md):

```
+List~ItemMatricula~ disciplinas   // correto — não usar List<ItemMatricula>
```

**Nota de rodapé pattern** (from `context-map.md` line 31):

```markdown
> **Secretaria:** aparece como contexto separado mas não recebe eventos de Matrícula no v1.
```

Usar este padrão de nota após cada diagrama para explicar omissões intencionais.

---

### `README.md` (config, request-response)

**Analog:** `README.md` itself (existing structure — self-referential)

**Existing section pattern** (lines 19-31 of `README.md`):

```markdown
### Fase 1: Design Estratégico

- [Problema de Negócio](docs/01-design-estrategico/problema-negocio.md)
- [Linguagem Ubíqua](docs/01-design-estrategico/linguagem-ubiqua.md)
- [Bounded Contexts](docs/01-design-estrategico/bounded-contexts.md)
- [Context Map](docs/01-design-estrategico/context-map.md)
```

**New section to add — copy this pattern exactly:**

```markdown
### Fase 2: Design Tático

- [Value Objects](docs/02-design-tatico/value-objects.md)
- [Entidades](docs/02-design-tatico/entidades.md)
- [Agregados](docs/02-design-tatico/agregados.md)
- [Domain Services](docs/02-design-tatico/domain-services.md)
- [Domain Events](docs/02-design-tatico/domain-events.md)
- [Repositórios](docs/02-design-tatico/repositorios.md)
- [Modelagem Visual (Diagramas)](docs/02-design-tatico/modelagem.md)
```

**Insertion point:** After line 31 (end of `### Decisões Arquiteturais (ADRs)` block, line 30) and before `## Stack técnico` (line 33). The new section goes inside `## Documentação por fase` which already exists.

**"Por onde começar" update:** Add `value-objects.md` or `modelagem.md` as step 5 in the sequential reading list (lines 10-15), pushing ADRs to step 6 or keeping them at step 5 with the tactical docs after.

---

## Shared Patterns

### Bottom-up narrative opening (D-04)
**Source:** `docs/01-design-estrategico/problema-negocio.md` lines 1-9 and `linguagem-ubiqua.md` lines 1-5
**Apply to:** All 6 pattern documents (`value-objects.md`, `entidades.md`, `agregados.md`, `domain-services.md`, `domain-events.md`, `repositorios.md`)

Every document opens with a concrete problem before naming the pattern. The analog pattern from `linguagem-ubiqua.md`:

```markdown
Em DDD, Linguagem Ubíqua é a linguagem compartilhada entre especialistas de negócio e desenvolvedores. Não é um dicionário separado da documentação técnica — é a linguagem que aparece nas conversas, nos documentos de requisitos e, principalmente, no código.
```

This opens with the concept defined in terms of its practical role, not its theoretical definition. Each new document should open similarly — defining the concept in terms of the concrete problem it solves in the Matrícula domain.

### Blockquote callout for key insight
**Source:** `docs/01-design-estrategico/linguagem-ubiqua.md` lines 37-38
**Apply to:** All 6 pattern documents — place at end of each major section

```markdown
> **Lição:** "Aluno" no BC Matrícula e "Aluno" no BC Acadêmico são modelos diferentes... Compartilhar uma única classe `Aluno` entre os três contextos criaria acoplamento...
```

Use `> **Lição:**` for pedagogical insights, `> **Por que:**` for justifications, and plain `>` + italic for cross-references to ADRs.

### Cross-reference to ADRs
**Source:** `docs/01-design-estrategico/bounded-contexts.md` lines 44, 54, 94
**Apply to:** `agregados.md`, `repositorios.md`, `value-objects.md` (IDs tipados), `modelagem.md`

Inline cross-reference pattern:

```markdown
ver [ADR-003](../adrs/ADR-003-referencia-por-id.md)
```

Always use relative paths. Always embed in the prose, not as a standalone line.

### Java 21 feature comment block
**Source:** RESEARCH.md TAT-01 through TAT-06 (all Java snippets)
**Apply to:** All 6 pattern documents — every code snippet with a Java 21 feature

```java
// Java 21: [feature name] — [what the feature provides technically]
// DDD fit: [why DDD benefits from this specific feature]
```

Example:
```java
// Java 21: construtor compacto de record — validação sem boilerplate
// DDD fit: imutabilidade garantida pelo compilador = Value Object perfeito (sem setter possível)
public record Cpf(String valor) {
    public Cpf { ... }
}
```

### ERRADO/CERTO anti-pattern block
**Source:** `docs/adrs/ADR-001-mybatis-vs-jpa.md` lines 13-39
**Apply to:** All 6 pattern documents — section "Erros Comuns" at end of each document

```java
// ERRADO — [descrição breve do problema, mostrando código Spring típico]
@Service
public class [NomeService] {
    @Autowired
    private [Repositorio] repo;
    // lógica que deveria estar no domínio
}

// CERTO — [descrição breve da solução DDD]
public class [NomeUseCase] {
    // sem @Service, sem @Autowired, sem framework
    // domínio faz a decisão de negócio
}
```

The ERRADO block must look like real Spring Boot code the target developer writes today — never caricature. The CERTO block uses Portuguese identifiers, no framework annotations at domain level.

### Mermaid diagram intro + note
**Source:** `docs/01-design-estrategico/context-map.md` lines 1-31
**Apply to:** `docs/02-design-tatico/modelagem.md` — each of the 4 diagram sections

```markdown
## [MOD-0X]: [Título do Diagrama]

[2-3 linhas de contextualização em português. O que o diagrama mostra. O que o leitor deve observar. Referência a ADR se aplicável.]

```mermaid
[bloco do diagrama]
```

> **[Nota]:** [explicação de omissões intencionais ou decisões de granularidade]
```

### Document file naming
**Source:** `docs/01-design-estrategico/` filenames observed
**Apply to:** All new files in `docs/02-design-tatico/`

Pattern: lowercase, kebab-case, Portuguese descriptive name, no abbreviations. Examples from Phase 1: `problema-negocio.md`, `linguagem-ubiqua.md`, `bounded-contexts.md`, `context-map.md`. Phase 2 files already follow this: `value-objects.md`, `domain-events.md`, etc.

---

## No Analog Found

All files have close analogs in the existing codebase. No file requires falling back to RESEARCH.md patterns exclusively.

| File | Reason analog is partial |
|------|--------------------------|
| `docs/02-design-tatico/modelagem.md` (MOD-02, MOD-03, MOD-04) | `context-map.md` has only 1 Mermaid diagram (`graph LR`); the new file adds `classDiagram`, `flowchart TD`, and `sequenceDiagram` — these diagram types have no existing analog in the codebase. Use RESEARCH.md §Diagramas Mermaid for verified syntax of these three types. |

---

## Metadata

**Analog search scope:** `docs/01-design-estrategico/`, `docs/adrs/`, `README.md`
**Files scanned:** 7 (4 Phase 1 docs + 4 ADRs + README — ADR-002 and ADR-004 skipped as lower relevance)
**Key insight:** This phase is 100% documentation. The "code" being patterned is Markdown structure and Java snippets embedded in prose. The primary patterns to copy are: (1) document opening style, (2) table structure, (3) blockquote callout format, (4) Mermaid block format, (5) cross-reference link format, (6) ERRADO/CERTO code block structure from ADR-001.
**Pattern extraction date:** 2026-06-20
