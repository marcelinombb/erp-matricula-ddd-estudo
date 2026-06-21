---
phase: 05-diagnostico-codigo-com-anti-padroes
plan: "06"
subsystem: docs
tags: [ddd, anti-patterns, documentation, markdown, portuguese]

requires:
  - phase: 05-01
    provides: Maven multi-module structure with erp-matricula-camadas and erp-matricula-ddd

provides:
  - "7 Markdown files in docs/00-ddd-sem-mudar-arquitetura/ documenting the 6 anti-patterns with code snippets, consequences, and DDD contrasts"
  - "Pedagogical bridge between the layered-architecture module (erp-matricula-camadas) and the DDD module (erp-matricula-ddd)"

affects:
  - 05-diagnostico-codigo-com-anti-padroes
  - 06-refatoracao-ddd-sem-mudar-arquitetura

tech-stack:
  added: []
  patterns:
    - "Anti-pattern documentation: H1 title, H2 sections (definition, manifestation with file path, consequence, DDD contrast), code snippets with // ANTI-PADRAO: inline comments"
    - "Cross-reference pattern: paths from docs/ to erp-matricula-camadas/src/ and erp-matricula-ddd/src/"

key-files:
  created:
    - docs/00-ddd-sem-mudar-arquitetura/00-introducao.md
    - docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md
    - docs/00-ddd-sem-mudar-arquitetura/02-entidade-anemica.md
    - docs/00-ddd-sem-mudar-arquitetura/03-service-deus.md
    - docs/00-ddd-sem-mudar-arquitetura/04-duplicacao-regras.md
    - docs/00-ddd-sem-mudar-arquitetura/05-regras-na-interface.md
    - docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md
  modified: []

key-decisions:
  - "00-introducao.md uses navigation table linking each anti-pattern to both its doc file and its primary Java file — dev reads doc, goes directly to source"
  - "countDisciplinas used in 06-acoplamento-banco.md as the canonical example of SQL-as-business-rule, matching the acceptance criterion exactly"
  - "Each file ends with a DDD contrast section pointing to the specific file in erp-matricula-ddd/ that solves the same problem"

patterns-established:
  - "Anti-pattern doc pattern: O que é / Manifestação (with path) / Consequência / Contraste DDD / Relação com outros anti-padrões"
  - "No emojis, Portuguese throughout, code snippets use // ANTI-PADRAO: comment format consistent with inline code comments"

requirements-completed:
  - DIAG-01
  - DIAG-02
  - DIAG-03
  - DIAG-04
  - DIAG-05
  - DIAG-06
  - DID-01

duration: 15min
completed: 2026-06-21
---

# Phase 05 Plan 06: Documentação dos Anti-Padrões Summary

**7 arquivos Markdown em docs/00-ddd-sem-mudar-arquitetura/ cobrindo os 6 anti-padrões com snippets de código, consequências narradas e contraste direto para o equivalente DDD em erp-matricula-ddd/**

## Performance

- **Duration:** ~15 min
- **Started:** 2026-06-21T22:26:00Z
- **Completed:** 2026-06-21T22:41:43Z
- **Tasks:** 1
- **Files modified:** 7 (all created)

## Accomplishments

- `00-introducao.md` contextualiza o módulo como o "antes" didático, com tabela de navegação e comparativo DDD vs. camadas (porta 8080 vs. 8081)
- Seis arquivos de anti-padrões (01-06) cobrem cada diagnóstico com: definição, snippet Java com caminho de arquivo real, consequência narrada (não prescritiva), contraste DDD com arquivo exato
- Todos os arquivos apontam para `erp-matricula-camadas/src/.../` e `erp-matricula-ddd/src/.../` com paths corretos — o desenvolvedor pode ir direto ao código sem adivinhar localização

## Task Commits

Each task was committed atomically:

1. **Task 1: Criar 00-introducao.md e os seis arquivos de anti-padrões** - `2760945` (docs)

**Plan metadata:** (committed together with SUMMARY.md)

## Files Created/Modified

- `docs/00-ddd-sem-mudar-arquitetura/00-introducao.md` - Contexto pedagógico: o que é o módulo, por que não é "código errado", tabela de navegação por anti-padrão, comparativo lado a lado, próximo passo
- `docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md` - DIAG-01: toda regra no Service, snippet MatriculaServiceImpl.matricular(), contraste VerificadorElegibilidadeMatricula
- `docs/00-ddd-sem-mudar-arquitetura/02-entidade-anemica.md` - DIAG-02: classe com setStatus sem validação, contraste Matricula.adicionarDisciplina() e StatusMatricula sealed interface
- `docs/00-ddd-sem-mudar-arquitetura/03-service-deus.md` - DIAG-03: 200+ linhas de Service, causalidade com DIAG-01/02, contraste UseCases separados
- `docs/00-ddd-sem-mudar-arquitetura/04-duplicacao-regras.md` - DIAG-04: if(!aluno.isAtivo()) em MatriculaServiceImpl e DisciplinaServiceImpl, cenário de divergência 6 meses depois
- `docs/00-ddd-sem-mudar-arquitetura/05-regras-na-interface.md` - DIAG-05: validação de período só no MatriculaController, três cenários de bypass (batch, teste, chamada interna)
- `docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md` - DIAG-06: countDisciplinas como regra SQL, problema de descoberta em 3 arquivos, nota sobre MyBatis + DDD sendo compatíveis

## Decisions Made

- `00-introducao.md` usa tabela de navegação com três colunas (anti-padrão, doc, Java file) para que o desenvolvedor vá diretamente do índice para o código-fonte sem overhead
- `06-acoplamento-banco.md` encerra com nota explícita de que "o anti-padrão não é usar SQL — é usar SQL como repositório de regras de negócio", seguindo D-09 do CONTEXT.md (sem julgamento de valor)
- Snippets de código usam `// ANTI-PADRAO:` inline consistente com o estilo estabelecido nos arquivos Java do módulo camadas

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## Known Stubs

None. Os arquivos de documentação fazem referência a arquivos Java em `erp-matricula-camadas/` que foram criados pelos planos 02-05. Esses arquivos existem e os paths são corretos.

## Threat Flags

None. Plano cria apenas arquivos Markdown estáticos sem trust boundaries.

## Self-Check: PASSED

Files verified:
- `docs/00-ddd-sem-mudar-arquitetura/00-introducao.md` — FOUND
- `docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md` — FOUND
- `docs/00-ddd-sem-mudar-arquitetura/02-entidade-anemica.md` — FOUND
- `docs/00-ddd-sem-mudar-arquitetura/03-service-deus.md` — FOUND
- `docs/00-ddd-sem-mudar-arquitetura/04-duplicacao-regras.md` — FOUND
- `docs/00-ddd-sem-mudar-arquitetura/05-regras-na-interface.md` — FOUND
- `docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md` — FOUND

Task commit 2760945 — FOUND (git log verified)

All acceptance criteria: PASSED (verified in execution)
All plan verification checks: PASSED (verified in execution)

## Next Phase Readiness

- Documentação de diagnóstico completa — 7 arquivos cobrem os 6 anti-padrões com referências cruzadas ao código
- Pronto para Fase 6 (refatoração DDD): cada arquivo de anti-padrão aponta para o equivalente DDD, criando o comparativo lado a lado necessário para o módulo pedagógico
- `erp-matricula-ddd` continua como referência de contraste para todos os DDD contrast sections

---
*Phase: 05-diagnostico-codigo-com-anti-padroes*
*Completed: 2026-06-21*
