---
phase: 01-design-estrategico
plan: "03"
subsystem: documentation
tags: [ddd, adr, mybatis, jpa, bounded-context, ubiquitous-language]

# Dependency graph
requires:
  - phase: 01-01
    provides: problema-negocio.md e linguagem-ubiqua.md como contexto para os ADRs
  - phase: 01-02
    provides: bounded-contexts.md e context-map.md como contexto para ADR-002 (escopo BC) e ADR-003 (referência por ID)
provides:
  - "ADR-001: decisão MyBatis vs JPA com código antes/depois (@Entity vs classe limpa)"
  - "ADR-002: decisão de escopo único BC Matrícula com stubs @TransactionalEventListener"
  - "ADR-003: decisão de referência por ID entre Aggregates com blocos de código comparativos"
  - "ADR-004: decisão de código em português com argumento Linguagem Ubíqua e trade-offs"
affects:
  - fase 2 (Design Tático)
  - fase 3 (Implementação)
  - qualquer fase que implemente código Java (convenção de nomes em português)
  - qualquer fase que implemente persistência (confirmação de MyBatis + ausência de FOREIGN KEY entre BCs)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Template ADR Nygard: Status → Contexto → Alternativas Consideradas → Decisão → Consequências (Positivas / Negativas)"
    - "Blocos de código antes/depois em ADRs para demonstrar impacto de decisões arquiteturais"

key-files:
  created:
    - docs/adrs/ADR-001-mybatis-vs-jpa.md
    - docs/adrs/ADR-002-escopo-bounded-context.md
    - docs/adrs/ADR-003-referencia-por-id.md
    - docs/adrs/ADR-004-codigo-em-portugues.md
  modified: []

key-decisions:
  - "MyBatis (não JPA): mapeamento explícito, domínio sem imports de framework, verificável via grep"
  - "Apenas BC Matrícula implementado: stubs @TransactionalEventListener demonstram contrato sem complexidade de integração real"
  - "Referência por ID tipado entre Aggregates: AlunoId/TurmaId como records Java, sem FOREIGN KEY entre tabelas de BCs distintos"
  - "Código em português: Linguagem Ubíqua vive no código, trade-off aceito de ferramentas menos eficientes"

patterns-established:
  - "Template ADR Nygard: todos os ADRs do projeto seguirão Status/Contexto/Alternativas/Decisão/Consequências"
  - "Trade-offs honestos obrigatórios: seção Negativas em toda decisão arquitetural"
  - "Código antes/depois em ADRs técnicos: quando a decisão envolve código, mostrar o problema e a solução explicitamente"

requirements-completed:
  - ESTR-06

# Metrics
duration: 10min
completed: 2026-06-20
---

# Phase 01 Plan 03: ADRs de Design Estratégico Summary

**4 ADRs no template Nygard com blocos de código Java comparativos, trade-offs honestos e alternativas consideradas para MyBatis, escopo de BC, referência por ID e código em português**

## Performance

- **Duration:** ~10 min
- **Started:** 2026-06-20T16:00:09Z
- **Completed:** 2026-06-20T16:10:36Z
- **Tasks:** 2
- **Files modified:** 4 criados

## Accomplishments

- ADR-001 criado com bloco de código @Entity (JPA, problema) vs classe Java limpa (MyBatis, solução) — ponto pedagógico central do projeto
- ADR-002 criado justificando implementação somente do BC Matrícula com stubs @TransactionalEventListener para Financeiro e Acadêmico
- ADR-003 criado com blocos de código comparando referência por objeto (Aluno, anti-padrão) vs referência por ID tipado (AlunoId, padrão correto), com justificativa da ausência de FOREIGN KEY entre tabelas de BCs distintos
- ADR-004 criado com contraste buscarPorId (PT) vs findById (EN), argumento Linguagem Ubíqua e trade-offs honestos de ferramentas

## Task Commits

Cada task foi commitada atomicamente:

1. **Task 1: Criar ADR-001 (MyBatis vs JPA) e ADR-002 (Escopo do BC)** - `7b6287e` (docs)
2. **Task 2: Criar ADR-003 (Referência por ID) e ADR-004 (Código em Português)** - `68e309f` (docs)

## Files Created/Modified

- `docs/adrs/ADR-001-mybatis-vs-jpa.md` — Decisão MyBatis vs JPA com blocos @Entity antes/depois, 3 alternativas (JPA, Spring Data JDBC, JDBC puro), trade-offs de XML manual
- `docs/adrs/ADR-002-escopo-bounded-context.md` — Decisão de escopo único BC com stubs @TransactionalEventListener, 3 alternativas de integração, trade-offs de consistência eventual
- `docs/adrs/ADR-003-referencia-por-id.md` — Decisão de referência por ID tipado com exemplos AlunoId vs Aluno, justificativa de ausência de FK entre BCs, trade-offs de orquestração
- `docs/adrs/ADR-004-codigo-em-portugues.md` — Decisão de código em português com contraste buscarPorId/findById, argumento Linguagem Ubíqua, trade-offs de ferramentas e mercado

## Decisions Made

- Template ADR Nygard adotado para todos os 4 ADRs com seções Positivas/Negativas obrigatórias dentro de Consequências
- Blocos de código Java inline em ADRs técnicos (ADR-001 e ADR-003) para demonstrar o problema visualmente antes de apresentar a solução
- Cada ADR inclui 3+ alternativas consideradas com prós e contras explícitos — nenhuma alternativa descartada sem justificativa

## Deviations from Plan

None — plano executado exatamente como especificado. Todos os blocos de código obrigatórios, seções do template Nygard e trade-offs honestos foram incluídos conforme o RESEARCH.md.

## Issues Encountered

- `contexto-matricula.md` não está no worktree (apenas no repositório principal) — conteúdo necessário já estava disponível nos arquivos de pesquisa (`01-RESEARCH.md`, `01-CONTEXT.md`) e nos documentos Wave 1 criados pelos agentes paralelos (`bounded-contexts.md`, `problema-negocio.md`). Nenhum impacto na entrega.

## User Setup Required

None — este plano cria exclusivamente arquivos Markdown estáticos. Sem instalações, sem configuração de serviços externos.

## Next Phase Readiness

- ESTR-06 satisfeito: 4 ADRs com alternativas consideradas, vantagens, desvantagens e motivo da escolha final
- DID-02..05 considerados entregues antecipadamente (previstos na Fase 4, entregues na Fase 1 conforme D-11)
- Todos os documentos de Design Estratégico completos: Wave 1 entregou problema-negocio.md, linguagem-ubiqua.md, bounded-contexts.md, context-map.md; este plano entrega os 4 ADRs
- Fase 2 (Design Tático) pode começar: a fundação documental estratégica está completa

---
*Phase: 01-design-estrategico*
*Completed: 2026-06-20*
