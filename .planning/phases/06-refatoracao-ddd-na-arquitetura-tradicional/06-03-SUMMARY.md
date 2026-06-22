---
phase: "06-refatoracao-ddd-na-arquitetura-tradicional"
plan: 03
subsystem: docs
tags: [ddd, value-objects, agregados, repositorios, documentacao-pedagogica]
dependency_graph:
  requires: []
  provides: [DDD-03, DDD-04, DDD-05]
  affects: [docs/00-ddd-sem-mudar-arquitetura/]
tech_stack:
  added: []
  patterns: [ANTES-DEPOIS markdown, construtor-compacto-record-java21, aggregate-guards-pattern]
key_files:
  created:
    - docs/00-ddd-sem-mudar-arquitetura/09-value-objects.md
    - docs/00-ddd-sem-mudar-arquitetura/10-agregados.md
    - docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md
  modified: []
decisions:
  - "10-agregados.md usa itemMatriculaRepository.countByMatriculaId (padrão do ItemMatriculaRepository) em vez de matriculaRepository.countDisciplinas para mostrar o anti-padrão DIAG-06, conforme threat model T-06-05"
metrics:
  duration: "~20 minutos"
  completed: "2026-06-22"
  tasks_completed: 2
  files_created: 3
---

# Phase 06 Plan 03: Três Últimos Docs DDD Aplicados Summary

**One-liner:** Três docs ANTES/DEPOIS completando o conjunto DDD-03/04/05: Value Objects como records Java 21 com construtor compacto, Aggregates com Guards 1-3 protegendo invariantes, e Repositório no domínio sem imports de infraestrutura.

---

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Criar 09-value-objects.md (DDD-03) | ce3e277 | docs/00-ddd-sem-mudar-arquitetura/09-value-objects.md |
| 2 | Criar 10-agregados.md e 11-repositorios.md (DDD-04 e DDD-05) | 889bb80 | docs/00-ddd-sem-mudar-arquitetura/10-agregados.md, docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md |

---

## What Was Built

### 09-value-objects.md (DDD-03)
Snippet ANTES: `Matricula` anêmica com `String periodoInicio`, `String periodoFim`, `String status` sem validação. Snippet DEPOIS: records `PeriodoLetivo` e `NomeDisciplina` com construtor compacto validando no construtor. Ponto pedagógico central: "Um `PeriodoLetivo(2026, 3)` é impossível de criar — o construtor lança antes que o objeto exista."

### 10-agregados.md (DDD-04)
Snippet ANTES: `MatriculaServiceImpl.adicionarDisciplina()` com verificação de status via String comparison e contagem via `itemMatriculaRepository.countByMatriculaId()` — duas operações separadas com janela de concorrência. Snippet DEPOIS: `Matricula.adicionarDisciplina()` com Guards 1-3 (estado, limite, duplicidade) no mesmo método, na mesma transação. Problema de concorrência explicado (dois usuários simultâneos podem cada um ver 5 e adicionar, resultando em 7).

### 11-repositorios.md (DDD-05)
Snippet ANTES: `MatriculaRepository` com `@Mapper` (anotação de infraestrutura na interface de domínio), termos técnicos `findById`, `countDisciplinas`. Snippet DEPOIS: `MatriculaRepositorio` sem imports de framework, com nomes em português de domínio (`buscarPorId`, `existeMatriculaAtiva`, `salvar`). Nota sobre Dependency Inversion: seta vai de `infraestrutura/` → `dominio/`.

---

## Deviations from Plan

### Auto-fixed Issues

None — plan executed exactly as written.

### Decisions Made

**Referência no 10-agregados.md: `itemMatriculaRepository.countByMatriculaId` vs. `matriculaRepository.countDisciplinas`**

O `MatriculaServiceImpl.java` linha 149 chama `matriculaRepository.countDisciplinas(matriculaId)`. O PATTERNS.md e o threat model T-06-05 identificam `countDisciplinas` em `MatriculaRepository` como referência a evitar, indicando que `itemMatriculaRepository.countByMatriculaId` é o padrão correto do `ItemMatriculaRepository`. O doc usa `countByMatriculaId` conforme o intent do plano, que satisfaz os critérios de aceitação (grep countByMatriculaId retorna resultado; grep countDisciplinas retorna 0).

---

## Verification Results

```
ls docs/00-ddd-sem-mudar-arquitetura/ | grep "09|10|11"
# → 09-value-objects.md, 10-agregados.md, 11-repositorios.md ✓

wc -l docs/00-ddd-sem-mudar-arquitetura/09-value-objects.md
# → 76 (entre 50 e 150) ✓

wc -l docs/00-ddd-sem-mudar-arquitetura/10-agregados.md
# → 79 (entre 50 e 150) ✓

wc -l docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md
# → 63 (entre 50 e 150) ✓

grep "countByMatriculaId" docs/00-ddd-sem-mudar-arquitetura/10-agregados.md
# → 2 linhas ✓

grep -c "countDisciplinas" docs/00-ddd-sem-mudar-arquitetura/10-agregados.md
# → 0 ✓

grep "@Mapper" docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md
# → 3 linhas ✓

grep "existeMatriculaAtiva" docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md
# → 3 linhas ✓
```

---

## Self-Check: PASSED

- [x] docs/00-ddd-sem-mudar-arquitetura/09-value-objects.md exists
- [x] docs/00-ddd-sem-mudar-arquitetura/10-agregados.md exists
- [x] docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md exists
- [x] Commit ce3e277 exists (Task 1)
- [x] Commit 889bb80 exists (Task 2)
