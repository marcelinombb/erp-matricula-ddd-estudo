---
phase: 01-design-estrategico
plan: "02"
subsystem: documentation
tags: [ddd, bounded-contexts, context-map, mermaid, strategic-design]

# Dependency graph
requires:
  - phase: 01-design-estrategico/plan-01
    provides: problema-negocio.md e linguagem-ubiqua.md que este plano referencia
provides:
  - Classificação de 6 subdomínios (Core/Supporting/Generic) com justificativas pedagógicas
  - Definição dos 4 Bounded Contexts com responsabilidades, limites e linguagem própria
  - Context Map em Mermaid com padrões DDD rotulados (Customer-Supplier, OHS, PL, ACL)
  - Distinção explícita entre Subdomínio (problema) e Bounded Context (solução)
  - Narrativa explicando por que Secretaria está isolada no v1
affects:
  - 02-design-tatico (usa os BCs como referência para modelagem tática)
  - 03-implementacao (usa Context Map para definir contratos de eventos)
  - todos os planos subsequentes (referência canônica de subdomínios e contextos)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Diagrama Mermaid graph LR com subgraphs para Context Map (4 contextos, setas de evento com rótulos DDD)"
    - "Documento bounded-contexts.md com seção explícita Subdomínio vs BC (prevenção do Pitfall 4)"
    - "Context Map estruturado: diagrama + tabela de eventos + narrativa por padrão DDD"

key-files:
  created:
    - docs/01-design-estrategico/bounded-contexts.md
    - docs/01-design-estrategico/context-map.md
  modified: []

key-decisions:
  - "Diagrama usa setas -- 'texto' --> (não |'texto'|) para compatibilidade Mermaid com rótulos multi-linha contendo caracteres especiais"
  - "Secretaria aparece como nó isolado no diagrama sem setas — ausência intencional documentada em nota e em seção própria"
  - "ACL documentado apenas na narrativa, não no diagrama — evita excesso visual (per RESEARCH.md recomendação)"

patterns-established:
  - "Context Map: diagrama Mermaid graph LR + tabela de eventos + narrativa por padrão (não apenas diagrama)"
  - "bounded-contexts.md: seção Subdomínio vs BC antes da classificação (Pitfall 4 prevenido)"
  - "Cada BC tem: Responsabilidade, Limites, Linguagem própria, Dados próprios, Status no v1"

requirements-completed:
  - ESTR-03
  - ESTR-04
  - ESTR-05

# Metrics
duration: 10min
completed: 2026-06-20
---

# Phase 01 Plan 02: Bounded Contexts e Context Map Summary

**Dois documentos estratégicos DDD: classificação de 6 subdomínios com justificativas pedagógicas e Context Map Mermaid com 4 contextos, 3 eventos e padrões Customer-Supplier/OHS/PL/ACL rotulados**

## Performance

- **Duration:** ~10 min
- **Started:** 2026-06-20T15:40:00Z
- **Completed:** 2026-06-20T15:50:29Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments

- `bounded-contexts.md` cobre ESTR-03 (6 subdomínios classificados com justificativas em prosa) e ESTR-04 (4 BCs com responsabilidades, limites, linguagem própria, dados próprios e status no v1)
- `context-map.md` cobre ESTR-05: diagrama Mermaid `graph LR` com 4 contextos e 3 eventos, padrões DDD rotulados no diagrama e narrativa pedagógica de cada padrão (Customer/Supplier, OHS, PL, ACL)
- Pitfall 4 prevenido: `bounded-contexts.md` declara explicitamente a distinção entre Subdomínio (problema) e Bounded Context (solução), com nota de que o mapeamento 1:1 deste projeto é uma escolha, não uma regra do DDD
- Pitfall 3 prevenido: Secretaria aparece como contexto isolado no diagrama sem setas de evento, com nota explicativa e seção própria no `context-map.md`

## Task Commits

Cada task foi commitada atomicamente:

1. **Task 1: Criar bounded-contexts.md com subdomínios e contextos (ESTR-03 + ESTR-04)** - `4c066ee` (docs)
2. **Task 2: Criar context-map.md com diagrama Mermaid e narrativa (ESTR-05)** - `982c67f` (docs)

## Files Created/Modified

- `docs/01-design-estrategico/bounded-contexts.md` — Classificação de subdomínios (Core/Supporting/Generic) com justificativas, definição de 4 BCs com responsabilidades/limites/linguagem própria/dados próprios, distinção explícita Subdomínio vs BC
- `docs/01-design-estrategico/context-map.md` — Diagrama Mermaid graph LR com 4 contextos e 3 eventos, narrativa pedagógica dos padrões DDD (Customer/Supplier, OHS, PL, ACL), tabela de eventos, nota sobre Secretaria isolada

## Decisions Made

- **Sintaxe Mermaid para rótulos de seta:** usado `-- "texto" -->` em vez de `-->|"texto"|` para compatibilidade mais ampla com rótulos multi-linha e caracteres especiais (`\n` e `·`). Ambas as sintaxes são válidas em Mermaid; a escolha seguiu o Exemplo 2 do RESEARCH.md.
- **ACL no diagrama:** confirmada a recomendação do RESEARCH.md — ACL documentado na narrativa, não no diagrama (`[Customer/Supplier · OHS/PL]` nos rótulos). Adicionar `[ACL]` nos rótulos tornaria as setas excessivamente longas sem ganho pedagógico.
- **Secretaria no diagrama:** aparece como nó sem setas de evento (`S["BC Secretaria"]` no subgraph SEC). A ausência é documentada em nota imediatamente após o bloco Mermaid e em seção própria `## Secretaria no v1`.

## Deviations from Plan

None — plano executado exatamente como escrito. Toda a sintaxe Mermaid e estrutura de conteúdo estava especificada no PLAN.md e no RESEARCH.md; não houve necessidade de adaptações.

## Issues Encountered

None — fase de documentação pura sem dependências externas, instalações ou código executável. Todos os arquivos criados conforme especificado.

## User Setup Required

None — documentação estática, sem serviços externos, sem variáveis de ambiente.

## Next Phase Readiness

- `bounded-contexts.md` e `context-map.md` estão prontos como referência para Plan 03 (ADRs) e para todas as fases subsequentes
- Plan 01-03 (ADRs) pode referenciar o Context Map para contextualizar as decisões arquiteturais (especialmente ADR-002 sobre escopo do BC Matrícula)
- Fase 2 (Design Tático) tem referência canônica dos BCs para modelagem de Aggregates, Entities e Value Objects dentro do BC Matrícula

---
*Phase: 01-design-estrategico*
*Completed: 2026-06-20*
