---
phase: 01-design-estrategico
plan: "01"
subsystem: documentation
tags: [ddd, markdown, linguagem-ubiqua, bounded-context, dominio]

requires: []

provides:
  - "README.md na raiz como mapa de navegação do projeto (D-02)"
  - "docs/01-design-estrategico/problema-negocio.md — descrição do problema de negócio (ESTR-01)"
  - "docs/01-design-estrategico/linguagem-ubiqua.md — glossário com 6 termos e seção Conceitos Ambíguos (ESTR-02)"

affects:
  - 01-02 (bounded-contexts.md referencia linguagem-ubiqua.md §Conceitos Ambíguos)
  - 01-03 (ADRs podem referenciar problema-negocio.md como contexto de justificativa)
  - todas as fases subsequentes (termos do glossário aparecem no código Java e nos documentos táticos)

tech-stack:
  added: []
  patterns:
    - "Documentação em docs/01-design-estrategico/ separada por fase (D-01)"
    - "README.md como mapa de navegação sem conteúdo DDD inline (D-02)"
    - "Nomes de arquivo em português descritivo sem prefixo numérico (D-03)"
    - "Glossário com colunas Termo | Definição | BC Dono | Não usar (D-05, D-06)"
    - "Seção Conceitos Ambíguos mostrando mesmo termo com modelos distintos por BC (D-07)"

key-files:
  created:
    - README.md
    - docs/01-design-estrategico/problema-negocio.md
    - docs/01-design-estrategico/linguagem-ubiqua.md
  modified: []

key-decisions:
  - "README.md segue padrão mapa-de-navegação (D-02): ponto de entrada com links, sem definições DDD inline"
  - "Glossário inclui coluna 'Não usar' com anti-exemplos em inglês para reforçar ativamente a Linguagem Ubíqua (D-06)"
  - "Seção Conceitos Ambíguos demonstra que Aluno e Matrícula têm modelos distintos em cada Bounded Context — insight central do DDD (D-07)"
  - "Conteúdo de domínio derivado integralmente de contexto-matricula.md como fonte primária"

patterns-established:
  - "Documentação progressiva: problema → linguagem → bounded contexts → context map"
  - "Anti-exemplos obrigatórios na coluna Não usar do glossário"
  - "Seção Conceitos Ambíguos após glossário para evidenciar Bounded Contexts"

requirements-completed: [ESTR-01, ESTR-02]

duration: 3min
completed: "2026-06-20"
---

# Phase 01 Plan 01: Documentos de Fundação Summary

**README como mapa de navegação + problema-negocio.md (ESTR-01) + glossário linguagem-ubiqua.md com Conceitos Ambíguos (ESTR-02) em Markdown puro sem código Java**

## Performance

- **Duration:** 3 min
- **Started:** 2026-06-20T15:47:29Z
- **Completed:** 2026-06-20T15:50:34Z
- **Tasks:** 3
- **Files modified:** 3 (todos criados, nenhum modificado)

## Accomplishments

- README.md criado como mapa de navegação com "Por onde começar" sequencial, links para 4 documentos estratégicos e 4 ADRs, stack técnico e placeholder de execução
- problema-negocio.md com descrição do domínio em linguagem de negócio, tabela de 4 usuários, 3 fluxos principais em prosa narrativa e argumentação pedagógica sobre DDD
- linguagem-ubiqua.md com glossário de 6 termos (colunas Termo | Definição | BC Dono | Não usar), seção Conceitos Ambíguos com sub-tabelas para Aluno e Matrícula por contexto, e blockquote de lição sobre modelos independentes por BC

## Task Commits

Cada task foi commitada atomicamente:

1. **Task 1: Criar README.md como mapa de navegação** - `6114e24` (docs)
2. **Task 2: Criar problema-negocio.md (ESTR-01)** - `a6dfb28` (docs)
3. **Task 3: Criar linguagem-ubiqua.md com glossário e Conceitos Ambíguos (ESTR-02)** - `f15a833` (docs)

## Files Created/Modified

- `README.md` — mapa de navegação do projeto: overview, Por onde começar, links para todos os docs e ADRs, stack técnico, placeholder de execução
- `docs/01-design-estrategico/problema-negocio.md` — problema de negócio em linguagem de negócio com usuários, fluxos e justificativa pedagógica DDD (ESTR-01)
- `docs/01-design-estrategico/linguagem-ubiqua.md` — glossário compartilhado com 6 termos, coluna Não usar e seção Conceitos Ambíguos (ESTR-02)

## Decisions Made

- README segue estritamente o padrão mapa-de-navegação (D-02): nenhuma seção define Aggregate Root, Value Object ou outros conceitos DDD — esses ficam nos documentos específicos.
- A seção "Por que DDD para este domínio" em problema-negocio.md inclui link para `linguagem-ubiqua.md#conceitos-ambíguos` para evitar duplicação de conteúdo.
- O glossário usa `PeriodoLetivo` (camelCase sem espaço) como o termo canônico, alinhando com o nome da classe Java que será criada nas fases seguintes.
- Conteúdo derivado integralmente de `contexto-matricula.md` como fonte primária, sem reinvenção.

## Deviations from Plan

Nenhuma — plano executado exatamente como escrito.

## Issues Encountered

Nenhum.

## Known Stubs

Nenhum — os 3 arquivos entregam conteúdo completo conforme especificação. Os links no README para `bounded-contexts.md`, `context-map.md` e `docs/adrs/` apontam para arquivos que serão criados nos planos 02 e 03 desta fase — isso é intencional e documentado no próprio README.

## Threat Flags

Nenhum — fase cria apenas arquivos Markdown estáticos sem endpoints, dados sensíveis ou código executável.

## Next Phase Readiness

- problema-negocio.md e linguagem-ubiqua.md estão prontos para referência em bounded-contexts.md (Plano 02)
- Os 6 termos do glossário são a base para as definições de Aggregate, Value Object e Entity no Design Tático (Fase 2)
- README contém links para bounded-contexts.md e context-map.md — esses arquivos serão criados no Plano 02
- Nenhum bloqueador identificado

---
*Phase: 01-design-estrategico*
*Completed: 2026-06-20*
