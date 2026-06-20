# Phase 2: Design Tatico e Modelagem Visual - Context

**Gathered:** 2026-06-20
**Status:** Ready for planning

<domain>
## Phase Boundary

Esta fase entrega a documentação tática do modelo de domínio e os diagramas visuais que o representam. O desenvolvedor consegue ler a documentação e entender cada padrão DDD tático — Entidade, Value Object, Agregado, Domain Service, Domain Event, Repositório — com justificativas pedagógicas e exemplos concretos do domínio de Matrícula. Complementarmente, 4 diagramas Mermaid mostram o modelo visualmente: classes, agregados, fluxos de negócio e o sequence diagram completo do caso de uso principal.

**Deliverables:** `docs/02-design-tatico/` com 6 arquivos de padrões + `modelagem.md` + atualização do `README.md` da raiz.

**Não inclui:** código Java compilável/executável (isso é Fase 3), persistência, testes, controllers REST.

</domain>

<decisions>
## Implementation Decisions

### Estrutura de Arquivos

- **D-01:** 6 arquivos por padrão DDD em `docs/02-design-tatico/`: `entidades.md`, `value-objects.md`, `agregados.md`, `domain-services.md`, `domain-events.md`, `repositorios.md` — cada arquivo cobre o padrão com todos os exemplos relevantes do domínio de Matrícula.
- **D-02:** Diagramas Mermaid em arquivo dedicado `docs/02-design-tatico/modelagem.md` — separado dos documentos de padrão, reúne os 4 diagramas (MOD-01..04) em um único ponto de referência visual.
- **D-03:** `README.md` da raiz atualizado com nova seção `## Design Tático` contendo links para os 7 arquivos da fase (6 padrões + modelagem.md). Sem criar README.md local na pasta — navegação centralizada.

### Template de Documentação por Padrão

- **D-04:** Abordagem **bottom-up**: cada documento começa com um problema concreto do domínio (ex: "CPF não muda, não tem ciclo de vida..."), depois nomeia e define o padrão DDD que o resolve. Mais pedagógico para quem nunca viu DDD — o problema motiva o padrão, não o contrário.
- **D-05:** Cada documento de padrão termina com seção **"Erros Comuns"** — anti-patterns que o aluno pode encontrar em código legado ou ser tentado a reproduzir.
- **D-06:** Invariantes do Agregado `Matricula` documentadas com **narrativa + tabela resumo**: narrativa explica por que cada invariante pertence ao Agregado e não ao Service (incluindo o argumento de concorrência); tabela resume as invariantes como referência rápida.

### Código Java nos Documentos

- **D-07:** Snippets **Java 21 prospectivos** — código que SERÁ implementado na Fase 3, escrito nos documentos táticos da Fase 2. Os documentos táticos funcionam como especificação técnica para a Fase 3.
- **D-08:** Features Java 21 **destacadas explicitamente** com comentário explicando o fit DDD: `record` → "imutabilidade garantida pelo compilador = Value Object perfeito"; `sealed interface` → "estados finitos e exaustividade verificada em compilação = StatusMatricula".
- **D-09:** Padrão **ERRADO/CERTO** na seção "Erros Comuns": snippet com arquitetura em camadas (// ERRADO) seguido de snippet DDD (// CERTO). Ex: lógica de invariante no Service vs encapsulada no Agregado.

### Diagramas Mermaid (modelagem.md)

- **D-10:** Sequence diagram de "Realizar Matrícula" (MOD-04) com **fluxo completo**: HTTP → Controller → UseCase → VerificadorElegibilidade → Agregado.matricular() → Repositório.salvar() → EventPublisher.publicar(AlunoMatriculado). Cobre exatamente o success criteria da fase.
- **D-11:** Flowcharts de negócio (MOD-03) com **happy path + caminhos de erro**: cada ponto de decisão ramifica para a exceção de domínio correspondente (`AlunoInativoException`, `PeriodoFechadoException`, `LimiteDisciplinasExcedidoException`, etc.). Une visualmente os fluxos com as invariantes de TAT-03.
- **D-12:** Cada diagrama no `modelagem.md` tem **2-3 linhas de contextualização** antes do bloco Mermaid, apontando o que observar e referenciando ADRs relevantes quando aplicável (ex: diagrama de classes referencia ADR-003 para a referência por ID entre Aggregates).

### Claude's Discretion

- Quantidade exata de Value Objects a cobrir por arquivo: cobrir todos os VOs do domínio mencionados em DOM-01 (Cpf, PeriodoLetivo, MatriculaId, AlunoId, TurmaId, NomeDisciplina) com profundidade proporcional à complexidade de validação de cada um.
- Granularidade do diagrama de classes (MOD-01): incluir atributos e métodos relevantes sem poluir o diagrama — julgamento do agente.
- Entidades a cobrir em `entidades.md`: Aluno, Turma, e ItemMatricula — incluir ou não ItemMatricula separado ou dentro de agregados.md é julgamento do agente baseado na clareza pedagógica.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requisitos da Fase

- `.planning/REQUIREMENTS.md` §TAT-01..06, §MOD-01..04 — Requisitos completos dos padrões táticos e diagramas. Leitura obrigatória.
- `.planning/ROADMAP.md` §Phase 2 — Goal, Success Criteria e dependências da fase.

### Decisões da Fase 1 (base para o conteúdo)

- `docs/01-design-estrategico/bounded-contexts.md` — Bounded Contexts definidos; o contexto Matrícula e suas responsabilidades são o escopo do modelo tático.
- `docs/01-design-estrategico/context-map.md` — Context Map com eventos cross-context (AlunoMatriculado, DisciplinaAdicionada, MatriculaCancelada). Insumo para domain-events.md e para o sequence diagram.
- `docs/01-design-estrategico/linguagem-ubiqua.md` — Glossário de termos; todos os nomes de Entidades, VOs e eventos DEVEM seguir a Linguagem Ubíqua aqui definida.
- `docs/adrs/ADR-001-mybatis-vs-jpa.md` — Decisão MyBatis vs JPA; relevante para repositorios.md (por que o Repositório é uma interface de domínio e não um JpaRepository).
- `docs/adrs/ADR-003-referencia-por-id.md` — Referência por ID entre Aggregates; aparece no diagrama de classes (MOD-01) e em agregados.md.
- `docs/adrs/ADR-004-codigo-em-portugues.md` — Todos os nomes de classes, métodos e campos nos snippets Java DEVEM ser em português.

### Contexto do Domínio

- `contexto-matricula.md` — Documento de referência do domínio: regras de negócio, fluxos, conceitos. Fonte primária do conteúdo para todos os documentos táticos.
- `.planning/PROJECT.md` §Key Decisions — Decisões pré-existentes que NÃO devem ser re-discutidas.

### Padrões de Tecnologia Relevantes

- `.planning/STACK.md` — Stack tecnológica verificada; ver seção Java 21 para records, sealed classes e pattern matching.
- `.planning/SUMMARY.md` — Achados de pesquisa da fase inicial; incluindo decisões sobre granularidade do Aggregate e optimistic locking.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `docs/01-design-estrategico/` — 4 documentos estratégicos prontos para referência cruzada. Bounded contexts, linguagem ubíqua e ADRs são insumos diretos para os documentos táticos.
- `docs/adrs/ADR-001..004.md` — 4 ADRs existentes; repositorios.md, value-objects.md e agregados.md devem referenciar os ADRs relevantes.

### Established Patterns

- Nomes de arquivo em português descritivo (estabelecido na Fase 1: `problema-negocio.md`, `linguagem-ubiqua.md`, etc.)
- Template ADR clássico Michael Nygard já estabelecido em `docs/adrs/` — não replicar para docs táticos (diferente propósito)
- Diagramas Mermaid com `flowchart LR` ou `TD` conforme legibilidade — sem ferramenta externa

### Integration Points

- Esta fase produz documentação que serve como **especificação de design** para a Fase 3 (Implementação). Os snippets Java prospectivos em D-07 são a interface entre Fase 2 e Fase 3.
- `docs/adrs/` criada na Fase 1 é referenciada de dentro dos documentos táticos — sem criar novos ADRs nesta fase (ADRs de MyBatis e referência por ID já existem).
- `README.md` da raiz atualizado nesta fase com nova seção `## Design Tático` (D-03).

</code_context>

<specifics>
## Specific Ideas

- **O argumento de concorrência para invariantes no Agregado** (D-06): explicar que se a lógica de limite de disciplinas estivesse no Service, duas threads concorrentes poderiam cada uma checar "5 disciplinas" e adicionar — resultando em 7 disciplinas. O Agregado com transação garante a invariante. Esse argumento concreto clarifica o "por que" que a documentação precisa ter.
- **ERRADO/CERTO para anti-patterns** (D-09): o snippet ERRADO deve sempre mostrar código estilo Spring Service típico que o desenvolvedor conhece (método void no Service, `if` fora do Agregado, `String` em vez de VO), para criar o reconhecimento — "ah, eu faço assim hoje".
- **Referência cruzada ADR-003 no diagrama de classes** (D-12): o texto introdutório antes do `classDiagram` Mermaid deve apontar explicitamente que `Matricula --> AlunoId` (não `Matricula --> Aluno`) implementa ADR-003.
- **StatusMatricula com sealed interface**: no `agregados.md`, o ciclo de vida do Agregado `Matricula` deve incluir snippet mostrando `sealed interface StatusMatricula permits Ativa, Cancelada, Concluida` e um `switch` com pattern matching — demonstra que o compilador garante que todos os estados são tratados.

</specifics>

<deferred>
## Deferred Ideas

None — a discussão se manteve dentro do escopo da Fase 2.

</deferred>

---

*Phase: 2-Design Tatico e Modelagem Visual*
*Context gathered: 2026-06-20*
