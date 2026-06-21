---
phase: 05-diagnostico-codigo-com-anti-padroes
plan: "05"
subsystem: erp-matricula-camadas/controller
tags:
  - anti-patterns
  - regras-na-interface
  - diag-05
  - did-01
  - controller
  - didatico
dependency_graph:
  requires:
    - "05-04: MatriculaService interface (matricular, adicionarDisciplina, cancelar, buscarPorAluno)"
  provides:
    - "MatriculaController com três endpoints POST e DIAG-05 evidenciado"
    - "Pilha completa erp-matricula-camadas: controller + service + repository + model"
  affects:
    - "mvn -pl erp-matricula-camadas compile: agora compila a pilha completa"
tech_stack:
  added: []
  patterns:
    - "ResponseEntity<?> wildcard para retorno misto (sucesso/erro) sem ExcecaoHandler separado"
    - "@ExceptionHandler(RuntimeException.class) inline no Controller (contraste com ExcecaoHandler dedicado do módulo DDD)"
    - "Records como classes estáticas internas do Controller (MatricularRequest, AdicionarDisciplinaRequest, MatriculaResponse)"
key_files:
  created:
    - "erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/MatriculaController.java"
  modified: []
decisions:
  - "ResponseEntity<?> usado em vez de @ResponseStatus para permitir retornar body de erro nas validações DIAG-05"
  - "197 linhas (acima do alvo de 100-130) — necessário para incluir todos os blocos de comentário DIAG-05 pedagógicos e o ExceptionHandler"
  - "6 ocorrências de ANTI-PADRAO: Regras na Interface (DIAG-05) — supera o requisito mínimo de 2"
metrics:
  duration: "5 minutos"
  completed_date: "2026-06-21"
  tasks_completed: 1
  tasks_total: 1
  files_created: 1
  files_modified: 0
---

# Phase 05 Plan 05: Controller com Anti-padrão Regras na Interface (DIAG-05) Summary

Controller REST com três endpoints POST demonstrando DIAG-05 (Regras na Interface): validações de regra de negócio que existem apenas no Controller e são bypassadas por qualquer chamador direto do MatriculaService.

## What Was Built

**MatriculaController.java** — 197 linhas no pacote `br.com.escola.matricula.controller`:

1. **Três endpoints @PostMapping:**
   - `POST /matriculas` — matricular aluno; contém 3 blocos de comentário DIAG-05
   - `POST /matriculas/{id}/disciplinas` — adicionar disciplina; contém 2 blocos DIAG-05
   - `POST /matriculas/{id}/cancelamento` — cancelar matrícula; SEM comentário DIAG-05 (contraste intencional)

2. **Anti-padrão DIAG-05 evidenciado em dois endpoints:**
   - Endpoint matricular: (a) redundância `@NotBlank` + if-blank inline, (b) regra temporal hardcoded (`startsWith("199")`) explicando que MatriculaService.matricular() não faz essa verificação
   - Endpoint adicionarDisciplina: validação de comprimento mínimo 3 chars que não existe no Service

3. **Records de request/response como classes estáticas internas:**
   - `MatricularRequest` com `@NotNull UUID alunoId, turmaId` e `@NotBlank String periodoInicio, periodoFim`
   - `AdicionarDisciplinaRequest` com `@NotBlank String nomeDisciplina`
   - `MatriculaResponse` com 6 campos espelhando colunas da tabela (DIAG-06 implícito)

4. **ExceptionHandler inline:** `@ExceptionHandler(RuntimeException.class)` retornando HTTP 400 com Map genérico — contraste documentado com ExcecaoHandler tipado do módulo DDD

## Acceptance Criteria — Verificação

| Critério | Status |
|----------|--------|
| ANTI-PADRAO: Regras na Interface (DIAG-05) >= 2 ocorrências (wc: 6) | PASS |
| @PostMapping >= 3 ocorrências (wc: 3) | PASS |
| "/{id}/disciplinas" presente | PASS |
| "/{id}/cancelamento" presente | PASS |
| matriculaService.matricular( presente | PASS |
| matriculaService.adicionarDisciplina( presente | PASS |
| matriculaService.cancelar( presente | PASS |
| @RestController presente | PASS |
| mvn -pl erp-matricula-camadas compile -q exit 0 | PASS |
| controller/ + service/ + repository/ + model/ cada um com >= 1 .java | PASS |

## Tasks Completed

| Task | Commit | Files |
|------|--------|-------|
| Tarefa 1: MatriculaController com DIAG-05 | c4bf6a5 | MatriculaController.java |

## Deviations from Plan

None — plan executed exactly as written.

O arquivo ficou com 197 linhas (alvo era 100-130). O excedente é justificado pelos blocos de comentário pedagógicos DIAG-05 detalhados que o plano especificou (o alvo de 130 linhas era estimativa antes de escrever os comentários completos).

## Key Decisions

1. **ResponseEntity<?> wildcard**: O plano especificava `ResponseEntity` sem tipo; `ResponseEntity<?>` foi usado para permitir retornar tanto `MatriculaResponse` (sucesso) quanto `String` de erro nas validações DIAG-05 sem criar um tipo union explícito.

2. **ExceptionHandler inline vs. classe separada**: O módulo DDD tem `ExcecaoHandler.java` separado. O módulo camadas manteve o handler inline no Controller para demonstrar a informalidade da arquitetura em camadas — não há disciplina arquitetural forçando separação.

## Threat Flags

Nenhuma nova superfície de segurança além do documentado no threat model do plano:

- T-05-04: Validações de negócio apenas no Controller (DIAG-05 intencional) — aceito como ponto pedagógico
- T-05-05: RuntimeException retorna mensagem completa para o cliente — aceito como dev-only, documentado no handler
- T-05-02: @Valid nos parâmetros de request — Bean Validation ativo (mitigação implementada)

## Self-Check: PASSED

- [x] MatriculaController.java existe no worktree
- [x] Commit c4bf6a5 existe no log
- [x] mvn compile exit 0 confirmado
- [x] 6 ocorrências de DIAG-05 (requisito: >= 2)
- [x] 3 @PostMapping (requisito: 3 endpoints)
- [x] Pilha completa: controller/ + service/ + repository/ + model/ populados
