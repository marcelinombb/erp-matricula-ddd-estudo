# Phase 1: Design Estrategico - Context

**Gathered:** 2026-06-20
**Status:** Ready for planning

<domain>
## Phase Boundary

Esta fase entrega a fundação documental do projeto: o desenvolvedor consegue ler a documentação e entender qual problema de negócio DDD resolve, quais são os contextos do sistema, como eles se relacionam e quais decisões arquiteturais foram tomadas e por quê.

**Deliverables:** README.md (mapa de navegação) + docs/01-design-estrategico/ (problema-negocio.md, linguagem-ubiqua.md, bounded-contexts.md, context-map.md) + docs/adrs/ (ADR-001 a ADR-004).

**Não inclui:** código Java, diagramas táticos, persistência, testes.

</domain>

<decisions>
## Implementation Decisions

### Estrutura de Arquivos

- **D-01:** Documentação fica em `docs/` separada por fase: `docs/01-design-estrategico/` para os documentos desta fase.
- **D-02:** `README.md` na raiz funciona como **mapa de navegação** — visão geral + links para cada documento. Sem conteúdo duplicado, sem seções DDD inline.
- **D-03:** Nomes de arquivo em **português descritivo**: `problema-negocio.md`, `linguagem-ubiqua.md`, `bounded-contexts.md`, `context-map.md`. (Sem prefixo numérico, sem mapeamento ESTR-XX.)
- **D-04:** ADRs ficam em `docs/adrs/` — pasta compartilhada por todas as fases, já que ADRs são decisões de projeto, não de fase.

### Glossário de Linguagem Ubíqua (ESTR-02)

- **D-05:** Estrutura de cada entrada: **tabela** com colunas `Termo | Definição | BC Dono | Não usar`.
- **D-06:** Adicionar coluna **"Não usar"** com anti-exemplos em inglês (ex: `StudentEntity`, `RegistrationDTO`). Reforça ativamente o uso da Linguagem Ubíqua.
- **D-07:** Após a tabela principal, seção separada **"Conceitos Ambíguos"** mostrando como o mesmo conceito (Aluno, Matrícula) é visto diferentemente em cada Bounded Context.

### Context Map — Mermaid (ESTR-05)

- **D-08:** O diagrama mostra **padrões DDD de relação rotulados**: Customer-Supplier entre Matrícula (upstream) e Financeiro/Acadêmico (downstream); Open Host Service + Published Language para os eventos; Anti-Corruption Layer nos consumidores.
- **D-09:** **Todos os 4 subdomínios** aparecem no mapa — Matrícula, Financeiro, Acadêmico, Secretaria. Secretaria aparece como contexto isolado (sem conexão com eventos de Matrícula no v1).
- **D-10:** **Os 3 eventos que cruzam fronteiras** são mostrados: `AlunoMatriculado`, `DisciplinaAdicionada`, `MatriculaCancelada`.

### Decisões Arquiteturais (ESTR-06)

- **D-11:** ADRs **formais escritos já na Fase 1** (não adiados para a Fase 4). Os 4 ADRs planejados (DID-02..05) são entregues aqui: ADR-001 a ADR-004.
- **D-12:** Template ADR **clássico** (Michael Nygard): Status → Contexto → Decisão → Consequências (positivas e negativas). Ensina o formato enquanto documenta.
- **D-13:** Localização: `docs/adrs/ADR-001-mybatis-vs-jpa.md`, `ADR-002-escopo-bounded-context.md`, `ADR-003-referencia-por-id.md`, `ADR-004-codigo-em-portugues.md`.

### Claude's Discretion

- Profundidade de cada documento (ex: quantas frases por definição no glossário) — julgamento do agente durante a execução, priorizando clareza e concisão.
- Diagrama Mermaid: sintaxe exata (flowchart LR vs graph TD) — o que produzir renderização mais legível no GitHub.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requisitos da Fase

- `.planning/REQUIREMENTS.md` §ESTR-01..06 — Requisitos completos com critérios de aceite. Leitura obrigatória.
- `.planning/ROADMAP.md` §Phase 1 — Goal, Success Criteria e dependências da fase.

### Contexto de Domínio

- `contexto-matricula.md` — Documento de referência do domínio: subdomínios, Bounded Contexts, Linguagem Ubíqua, regras de negócio, fluxo completo. Fonte primária de conteúdo para todos os documentos desta fase.

### Decisões Já Tomadas (PROJECT.md)

- `.planning/PROJECT.md` §Key Decisions — Decisões pré-existentes: MyBatis vs JPA, código em português, escopo único BC Matrícula, referência por ID entre agregados. Não re-discutir.

### Pesquisa de Apoio

- `.planning/research/SUMMARY.md` — Achados de pesquisa sobre stack, padrões, pitfalls e recomendações. Suporte para conteúdo dos ADRs.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- Nenhum — projeto sem código ainda (sem `src/`). Esta é a primeira fase.

### Established Patterns

- Nenhum padrão de código estabelecido ainda. Os padrões emergirão nas Fases 3-4.

### Integration Points

- Esta fase produz documentação que serve de insumo para a Fase 2 (Design Tático) e todas as fases subsequentes.
- `docs/adrs/` criada aqui será referenciada pela Fase 4 (DID-02..05 considerados entregues aqui).

</code_context>

<specifics>
## Specific Ideas

- O conceito de **"Aluno muda conforme o contexto"** (Matrícula vs Financeiro vs Acadêmico) é um dos pontos pedagógicos mais importantes — deve aparecer com destaque tanto no Glossário (seção "Conceitos Ambíguos") quanto no documento de Bounded Contexts.
- ADR-001 (MyBatis vs JPA) deve incluir exemplo de código **antes/depois** mostrando anotações `@Entity`/`@Id` invadindo o domínio — o problema visual que motivou a decisão.
- O template ADR clássico (Status / Contexto / Decisão / Consequências) deve aparecer consistente nos 4 ADRs.

</specifics>

<deferred>
## Deferred Ideas

None — a discussão se manteve dentro do escopo da Fase 1.

</deferred>

---

*Phase: 1-Design Estrategico*
*Context gathered: 2026-06-20*
