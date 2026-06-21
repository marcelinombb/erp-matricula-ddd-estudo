---
phase: 02-design-tatico-e-modelagem-visual
plan: "02"
subsystem: documentation
tags: [ddd, aggregates, sealed-interface, java-21, invariants, pedagogical]
dependency_graph:
  requires:
    - docs/02-design-tatico/value-objects.md
    - docs/02-design-tatico/entidades.md
    - docs/adrs/ADR-003-referencia-por-id.md
  provides:
    - docs/02-design-tatico/agregados.md
  affects:
    - docs/02-design-tatico/domain-events.md (referenciado via link de coleta de eventos)
    - docs/02-design-tatico/domain-services.md (VerificadorElegibilidade referencia Aggregate)
tech_stack:
  added: []
  patterns:
    - bottom-up narrative opening (problema de concorrência antes de nomear o padrão Aggregate)
    - Java 21 sealed interface com records internos para ciclo de vida do Aggregate
    - Java 21 pattern matching exaustivo sem default no switch
    - Aggregate Root com lista interna de eventos (sem dependência de Spring)
    - Tabela de invariantes com exceções tipadas
    - Argumento de concorrência em blockquote (D-06)
    - ERRADO/CERTO anti-pattern blocks em Erros Comuns
    - Cross-references inline para ADRs via links relativos Markdown
key_files:
  created:
    - docs/02-design-tatico/agregados.md
  modified: []
decisions:
  - "ItemMatricula documentada como entidade interna com record (justificativa: imutável após criação, sem ciclo de vida próprio) — exceção ao padrão class para Entidade explicada explicitamente"
  - "LIMITE_DISCIPLINAS = 6 como constante nomeada com comentário de configurabilidade (A3 do RESEARCH.md)"
  - "Argumento de concorrência posicionado após a tabela de invariantes — impacto máximo após apresentar o que é protegido"
metrics:
  duration: ~20min
  completed: 2026-06-20
  tasks_completed: 1
  tasks_total: 1
  files_created: 1
  files_modified: 0
---

# Phase 02 Plan 02: Aggregate Root Matricula — Summary

Aggregate Root Matricula documentado com estrutura interna em ASCII, três invariantes (narrativa + tabela + argumento de concorrência), StatusMatricula como sealed interface Java 21 com pattern matching exaustivo, método adicionarDisciplina() completo, ItemMatricula como entidade interna, e seção Erros Comuns com dois pares ERRADO/CERTO.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Criar docs/02-design-tatico/agregados.md | dcae75a | docs/02-design-tatico/agregados.md |

## What Was Built

### `docs/02-design-tatico/agregados.md`

Documentação do Aggregate Root `Matricula` — o padrão tático mais complexo da fase:

- **Abertura bottom-up (D-04):** problema concreto em três paragráfos — "onde fica a regra do limite de 6 disciplinas?", dois Services precisando replicar a regra, e o argumento de concorrência (duas threads, 5+1+1=7 disciplinas) — antes de nomear o padrão "Aggregate"
- **Estrutura em diagrama ASCII:** `Aggregate Root: Matricula` com todos os componentes (`MatriculaId`, `AlunoId`, `TurmaId`, `PeriodoLetivo`, `StatusMatricula`, `List<ItemMatricula>`, `List<DomainEvent>`); parágrafo explicando referências por ID com cross-reference ADR-003; link para `value-objects.md`
- **`### ItemMatricula — Entidade Interna`:** documenta ausência de ID próprio, justifica uso de `record` (imutável após criação, sem ciclo de vida independente), distingue de entidades com estado mutável (`Aluno`)
- **`## Invariantes do Aggregate Matricula`:** narrativa por invariante (limite, duplicidade, estado terminal) + tabela com coluna `Exceção Lançada` (`LimiteDisciplinasExcedidoException`, `DisciplinaJaMatriculadaException`, `MatriculaCanceladaException`) + blockquote do argumento de concorrência (D-06 obrigatório)
- **`## Ciclo de Vida — StatusMatricula`:** por que não `enum` (sem dados por estado), `sealed interface` com records `Ativa()`, `Cancelada(LocalDateTime canceladaEm)`, `Concluida(LocalDateTime concluidaEm)`, switch de pattern matching sem `default`, blockquote "Por que sealed interface e não enum?"
- **`## O Aggregate em Ação — adicionarDisciplina()`:** snippet completo com `LIMITE_DISCIPLINAS = 6` como constante nomeada, três verificações inline comentadas (estado, duplicidade, limite), `this.eventos.add(new DisciplinaAdicionada(...))` com nota de coleta sem Spring, `coletarEventos()`, link para `domain-events.md`
- **`## Erros Comuns` (D-05, D-09):** dois pares ERRADO/CERTO:
  1. Invariante no Service — ERRADO: `@Service MatriculaService` com acesso direto `getDisciplinas().add()` / CERTO: `AdicionarDisciplinaUseCase` delegando para `matricula.adicionarDisciplina(disciplina)`
  2. `enum` para status — ERRADO: `enum Status { ATIVA, CANCELADA }` com campos opcionais na entidade / CERTO: `sealed interface StatusMatricula` com records + switch exaustivo

## Requirements Coverage

- **TAT-03:** Aggregate `Matricula` documentado com Aggregate Root, entidades internas (`ItemMatricula`), invariantes (narrativa + tabela + argumento de concorrência), `StatusMatricula` sealed interface, `adicionarDisciplina()` completo

## Deviations from Plan

None — plan executed exactly as written.

## Known Stubs

Os seguintes placeholders são intencionais (D-07 — snippets prospectivos):

| Arquivo | Linha | Stub | Razão |
|---------|-------|------|-------|
| `docs/02-design-tatico/agregados.md` | ~95 | `cpfComDigitoVerificadorValido()` ausente (referenciado de value-objects.md) | Implementação na Fase 3 |
| `docs/02-design-tatico/agregados.md` | ~133 | `DisciplinaAdicionada` event record definido implicitamente | Record completo documentado em domain-events.md (Fase 3) |

Estes stubs não impedem o objetivo do plano (documentação pedagógica do Aggregate). Os snippets são especificações prospectivas para a Fase 3.

## Threat Flags

Nenhuma nova superfície de segurança introduzida — esta fase entrega apenas arquivos Markdown estáticos sem código executável, endpoints ou acesso a dados.

## Self-Check: PASSED

- [x] `docs/02-design-tatico/agregados.md` existe: FOUND
- [x] Commit dcae75a existe: FOUND
- [x] `## Erros Comuns` presente (1 ocorrência): PASS
- [x] `threads concorrentes` presente (argumento D-06): PASS
- [x] `LimiteDisciplinasExcedidoException` presente: PASS
- [x] `DisciplinaJaMatriculadaException` presente: PASS
- [x] `MatriculaCanceladaException` presente: PASS
- [x] `sealed interface StatusMatricula` presente: PASS
- [x] `ADR-003` cross-reference presente: PASS
- [x] `Aggregate Root: Matricula` diagrama ASCII presente: PASS
- [x] Arquivo NÃO começa com "Em DDD, um Aggregate é": PASS
- [x] `adicionarDisciplina()` com 3 invariantes presente: PASS
- [x] `ItemMatricula` sem ID próprio documentado: PASS
- [x] Blocos `// ERRADO` e `// CERTO` presentes: PASS
- [x] Identificadores Java em português: PASS
