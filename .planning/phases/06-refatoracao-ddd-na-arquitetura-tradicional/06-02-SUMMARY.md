---
phase: "06-refatoracao-ddd-na-arquitetura-tradicional"
plan: 02
subsystem: "docs/00-ddd-sem-mudar-arquitetura"
tags: ["ddd", "linguagem-ubiqua", "entidades", "documentacao", "pedagogico"]
dependency_graph:
  requires: []
  provides: ["DDD-01-doc", "DDD-02-doc"]
  affects: ["docs/00-ddd-sem-mudar-arquitetura/00-introducao.md"]
tech_stack:
  added: []
  patterns: ["ANTES/DEPOIS doc pattern", "snippet-focused docs"]
key_files:
  created:
    - docs/00-ddd-sem-mudar-arquitetura/07-linguagem-ubiqua.md
    - docs/00-ddd-sem-mudar-arquitetura/08-entidades.md
  modified: []
decisions:
  - "Linguagem Ubíqua documentada com contraste MatriculaServiceImpl (ANTES) vs MatricularAlunoUseCase + MatriculaRepositorio (DEPOIS)"
  - "Entidades documentadas usando Aluno.java como exemplo DDD — identidade AlunoId, estaAtivo(), desativar(), equals por ID"
  - "Contraste Entidade vs. Value Object explicitado com PeriodoLetivo como exemplo de VO"
metrics:
  duration: "1m"
  completed_date: "2026-06-22"
  tasks_completed: 2
  files_created: 2
  files_modified: 0
---

# Phase 06 Plan 02: Linguagem Ubíqua e Entidades Summary

**One-liner:** Docs DDD-01 e DDD-02 com snippets ANTES/DEPOIS reais — MatricularAlunoUseCase vs MatriculaServiceImpl e Aluno.java com identidade final, estaAtivo(), equals por AlunoId.

---

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Criar 07-linguagem-ubiqua.md (DDD-01) | 4fad219 | docs/00-ddd-sem-mudar-arquitetura/07-linguagem-ubiqua.md |
| 2 | Criar 08-entidades.md (DDD-02) | c561774 | docs/00-ddd-sem-mudar-arquitetura/08-entidades.md |

---

## What Was Built

### Task 1 — 07-linguagem-ubiqua.md (DDD-01)

Documento de 89 linhas seguindo o padrão ANTES/DEPOIS estabelecido em `01-service-anemico.md`.

**Snippet ANTES** (`MatriculaServiceImpl.java`): assinatura `matricular(UUID alunoId, UUID turmaId, String periodoInicio, String periodoFim)` com tipos primitivos e `String status` — terminologia técnica, não de domínio.

**Snippet DEPOIS** (`MatricularAlunoUseCase`, `MatriculaRepositorio`, `StatusMatricula`, `Aluno`): `executar(MatricularAlunoCommand command)`, `buscarPorId(MatriculaId id)`, `buscarPorAluno(AlunoId alunoId)`, `sealed interface StatusMatricula`, `estaAtivo()`.

Ganhos documentados: especialista do domínio lê `MatricularAlunoUseCase.executar()` sem tradução mental; compilador rejeita `StatusMatricula` inválido.

### Task 2 — 08-entidades.md (DDD-02)

Documento de 97 linhas seguindo o padrão de `02-entidade-anemica.md` (analog exato de estrutura).

**Snippet ANTES** (`Matricula.java` camadas): `private String status`, `setStatus(String status)` sem validação de transição, sem `equals/hashCode` explícito.

**Snippet DEPOIS** (`Aluno.java` DDD): `private final AlunoId id` — campo final; `estaAtivo()` com semântica de negócio; `desativar()` comportamental; `equals/hashCode` baseados exclusivamente em `AlunoId`.

**Seção "O contraste de identidade"**: explicitação do critério Entidade vs. Value Object usando `PeriodoLetivo(2026, 1)` como contra-exemplo — mesmo valor = mesmo período (VO); mesmo nome ≠ mesma pessoa (Entidade).

---

## Verification Results

```
wc -l 07-linguagem-ubiqua.md  → 89  (entre 50 e 150 — PASSOU)
wc -l 08-entidades.md         → 97  (entre 50 e 150 — PASSOU)
grep ANTES  → ambos retornam  (PASSOU)
grep DEPOIS → ambos retornam  (PASSOU)
grep MatricularAlunoUseCase   → 3 ocorrências  (PASSOU)
grep MatriculaServiceImpl     → 4 ocorrências  (PASSOU)
grep StatusMatricula          → 4 ocorrências  (PASSOU)
grep estaAtivo                → 1 ocorrência   (PASSOU)
grep setStatus                → 1 ocorrência   (PASSOU)
grep AlunoId                  → 4 ocorrências  (PASSOU)
grep PeriodoLetivo (contraste)→ 3 ocorrências  (PASSOU)
```

---

## Deviations from Plan

None - plan executed exactly as written.

---

## Known Stubs

None — documentação referencia código real existente nos módulos `erp-matricula-camadas` e `erp-matricula-ddd`.

---

## Threat Flags

Nenhuma superfície de segurança introduzida — apenas documentação Markdown.

---

## Self-Check: PASSED

- [x] `docs/00-ddd-sem-mudar-arquitetura/07-linguagem-ubiqua.md` existe
- [x] `docs/00-ddd-sem-mudar-arquitetura/08-entidades.md` existe
- [x] Commit 4fad219 existe
- [x] Commit c561774 existe
