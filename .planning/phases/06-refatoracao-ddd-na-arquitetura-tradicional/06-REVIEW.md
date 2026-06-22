---
phase: 06-refatoracao-ddd-na-arquitetura-tradicional
reviewed: 2026-06-22T00:00:00Z
depth: standard
files_reviewed: 11
files_reviewed_list:
  - docs/00-ddd-sem-mudar-arquitetura/00-introducao.md
  - docs/00-ddd-sem-mudar-arquitetura/07-linguagem-ubiqua.md
  - docs/00-ddd-sem-mudar-arquitetura/08-entidades.md
  - docs/00-ddd-sem-mudar-arquitetura/09-value-objects.md
  - docs/00-ddd-sem-mudar-arquitetura/10-agregados.md
  - docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md
  - docs/00-ddd-sem-mudar-arquitetura/exercicio-classificacao.md
  - docs/00-ddd-sem-mudar-arquitetura/guia-leitura-comparativo.md
  - erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java
  - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java
  - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java
findings:
  critical: 3
  warning: 4
  info: 3
  total: 10
status: issues_found
---

# Phase 6: Code Review Report

**Reviewed:** 2026-06-22
**Depth:** standard
**Files Reviewed:** 11
**Status:** issues_found

## Summary

This phase delivers the DDD teaching module: six documentation files in `docs/00-ddd-sem-mudar-arquitetura/` explaining the DDD refactoring of the traditional layered module, plus three core Java files — `MatricularAlunoUseCase`, `Matricula` (Aggregate Root), and `MatriculaRepositorio` (domain repository interface).

The documentation is pedagogically sound and the overall DDD architecture is correctly applied. However, three behavioral bugs were found in `Matricula.java` that undermine the invariants the code is designed to protect: (1) the `Concluida` terminal state is not guarded in either `adicionarDisciplina()` or `cancelar()`, (2) the `cancelar()` method is missing a guard against transitioning from `Concluida`, and (3) the creation constructor does not null-check its parameters while the reconstitution constructor does not either, but both call behaviors that will NPE immediately if null is passed. There are also documentation accuracy issues where code snippets shown to students will not compile as written.

---

## Critical Issues

### CR-01: `adicionarDisciplina()` does not guard against `Concluida` terminal state

**File:** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java:191`

**Issue:** `StatusMatricula.Concluida` is explicitly documented as a terminal state in `StatusMatricula.java` ("Estado terminal por conclusão do período letivo"). However, Guard 1 in `adicionarDisciplina()` only checks `instanceof StatusMatricula.Cancelada`. A `Concluida` matricula can still have new disciplines added to it — the guard falls through silently. The Javadoc on the class (line 32) states the invariant as "Estado terminal: matrícula cancelada não recebe novas disciplinas" but omits `Concluida`, which appears to be where the implementation caught up to the comment rather than the correct design intent.

This is a behavioral bug: the invariant that concluded enrollments are read-only is not enforced in code.

**Fix:**
```java
public void adicionarDisciplina(NomeDisciplina disciplina) {
    // Guard 1: qualquer estado terminal bloqueia novas disciplinas
    if (this.status instanceof StatusMatricula.Cancelada) {
        throw new MatriculaCanceladaException(this.id);
    }
    if (this.status instanceof StatusMatricula.Concluida) {
        throw new MatriculaConcluidaException(this.id); // nova exceção de domínio
    }
    // ... guards 2 e 3 inalterados
}
```

Alternatively, define a helper method on `StatusMatricula` (e.g., `boolean eTerminal()`) and guard on that to keep guards future-proof as the sealed hierarchy evolves.

---

### CR-02: `cancelar()` does not guard against cancelling a `Concluida` matricula

**File:** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java:228`

**Issue:** The `cancelar()` method checks only `instanceof StatusMatricula.Cancelada` before transitioning state. A matricula in the `Concluida` terminal state can be cancelled — transitioning it from one terminal state to another. This corrupts audit history (the `concluidaEm` timestamp is discarded) and violates the documented semantics that `Concluida` is a terminal state from which no transitions are permitted. In a real school system this would let users "cancel" a student's completed enrollment retroactively, destroying the completion record.

**Fix:**
```java
public void cancelar() {
    if (this.status instanceof StatusMatricula.Cancelada) {
        throw new MatriculaCanceladaException(this.id);
    }
    if (this.status instanceof StatusMatricula.Concluida) {
        // Concluida is a terminal state — cannot be cancelled
        throw new OperacaoInvalidaEmMatriculaConcluidaException(this.id);
    }
    LocalDateTime agora = LocalDateTime.now();
    this.status = new StatusMatricula.Cancelada(agora);
    this.eventos.add(new MatriculaCancelada(this.id, this.alunoId, this.periodoLetivo, agora));
}
```

---

### CR-03: `Matricula` creation constructor has no null guards; reconstitution constructor has no null guards either

**File:** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java:101-143`

**Issue:** The private creation constructor (lines 101–113) assigns `alunoId`, `turmaId`, and `periodoLetivo` without null checks. The public reconstitution constructor (lines 133–143) assigns `id`, `alunoId`, `turmaId`, `periodoLetivo`, `status`, and `disciplinas` without null checks. A null `disciplinas` passed to the reconstitution constructor would bypass the defensive copy and produce a list initialized from `new ArrayList<>(null)` — which throws `NullPointerException` at construction time but with a misleading stack trace. More critically, a null `status` will not fail at construction but will NPE at the first call to `adicionarDisciplina()` or `cancelar()` on the `instanceof` guard, again with a misleading call site. Compare with `Aluno.java` and `Turma.java` which both use `Objects.requireNonNull` for every field — this is a consistency gap and a real NPE risk for the infrastructure layer that calls the reconstitution constructor.

**Fix:**
```java
// Creation constructor
private Matricula(AlunoId alunoId, TurmaId turmaId, PeriodoLetivo periodoLetivo) {
    this.alunoId = Objects.requireNonNull(alunoId, "AlunoId não pode ser nulo");
    this.turmaId = Objects.requireNonNull(turmaId, "TurmaId não pode ser nulo");
    this.periodoLetivo = Objects.requireNonNull(periodoLetivo, "PeriodoLetivo não pode ser nulo");
    // ... rest unchanged
}

// Reconstitution constructor
public Matricula(MatriculaId id, AlunoId alunoId, TurmaId turmaId,
                 PeriodoLetivo periodoLetivo, StatusMatricula status,
                 List<ItemMatricula> disciplinas) {
    this.id = Objects.requireNonNull(id, "MatriculaId não pode ser nulo");
    this.alunoId = Objects.requireNonNull(alunoId, "AlunoId não pode ser nulo");
    this.turmaId = Objects.requireNonNull(turmaId, "TurmaId não pode ser nulo");
    this.periodoLetivo = Objects.requireNonNull(periodoLetivo, "PeriodoLetivo não pode ser nulo");
    this.status = Objects.requireNonNull(status, "StatusMatricula não pode ser nulo");
    Objects.requireNonNull(disciplinas, "Lista de disciplinas não pode ser nula");
    this.disciplinas = new ArrayList<>(disciplinas);
    this.eventos = new ArrayList<>();
}
```

---

## Warnings

### WR-01: `coletarEventos()` clears the event list before confirming publication — events can be silently lost

**File:** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java:257-260`

**Issue:** `coletarEventos()` copies the list with `List.copyOf` and then calls `this.eventos.clear()` unconditionally. In `MatricularAlunoUseCase.executar()`, the returned list is immediately iterated with `forEach(publicador::publishEvent)`. If `publishEvent` throws a runtime exception for the first event, subsequent events in the list are never published — and since `eventos` is already cleared, there is no recovery path. The UseCase wraps the whole operation in `@Transactional`, so a publish exception would cause a rollback (the persistence is undone), but the events already dispatched in the same `forEach` before the failure would have been published against a transaction that is about to roll back. This is the exact scenario the code's Javadoc on `@TransactionalEventListener` is meant to prevent, but it only works if all events are dispatched by `publishEvent` without mid-iteration failure.

For the didactic context this is a teaching gap as well: the code claims events are published "AFTER persistence" for safety, but a partial failure during event publishing creates a split-brain window.

**Fix:** Consider catching exceptions during publish and collecting failures, or trust `@TransactionalEventListener` fully by not clearing events until after the `forEach` completes:
```java
public List<Object> coletarEventos() {
    List<Object> copia = List.copyOf(this.eventos);
    // Clear only after copy is returned to caller; caller bears responsibility for publishing
    this.eventos.clear();
    return copia;
}
// In UseCase: publish, then if no exception, clear is already done. The existing code is
// structurally fine as long as publishEvent never throws before adding to Spring's
// event queue — document this assumption explicitly.
```

At minimum, add a comment documenting the assumption that `ApplicationEventPublisher.publishEvent` is non-throwing.

---

### WR-02: `VerificadorElegibilidadeMatricula` Javadoc references a non-existent class `DominioConfig`

**File:** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java:29,34,36`

**Issue:** The Javadoc example code block at lines 34–43 shows:
```java
// infraestrutura/config/DominioConfig.java
public class DominioConfig {
```
The actual configuration class is `DomainServicesConfig` in `infraestrutura/config/DomainServicesConfig.java`. A developer following the Javadoc example to locate the Spring configuration file will not find `DominioConfig` — it does not exist. This is especially problematic as the Javadoc is a teaching aid directing students to the infrastructure configuration.

**Fix:** Update the Javadoc class name to `DomainServicesConfig` to match the actual implementation:
```java
// infraestrutura/config/DomainServicesConfig.java
@Configuration
public class DomainServicesConfig {
    @Bean
    public VerificadorElegibilidadeMatricula verificadorElegibilidadeMatricula(MatriculaRepositorio repo) {
        return new VerificadorElegibilidadeMatricula(repo);
    }
}
```

---

### WR-03: `07-linguagem-ubiqua.md` code snippet for `sealed interface` will not compile as written

**File:** `docs/00-ddd-sem-mudar-arquitetura/07-linguagem-ubiqua.md:71`

**Issue:** The documentation shows:
```java
sealed interface StatusMatricula permits Ativa, Cancelada, Concluida { ... }
```
The actual code in `StatusMatricula.java` line 38–39 is:
```java
public sealed interface StatusMatricula
        permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida {
```
Because `Ativa`, `Cancelada`, and `Concluida` are nested types, the `permits` clause requires the enclosing class qualifier (`StatusMatricula.Ativa`, not `Ativa`). If a student copies the snippet from the documentation and tries to compile it independently or as a standalone test, it will fail with a compile error. For a didactic codebase where students are expected to reproduce patterns, inaccurate code examples are a correctness issue.

**Fix:** Update the snippet:
```java
sealed interface StatusMatricula
        permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida { ... }
```

---

### WR-04: `Aluno.nome` accepts blank strings — no enforcement of non-blank invariant

**File:** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Aluno.java:63`

**Issue:** (Cross-reference finding, surface during review of `08-entidades.md` which uses `Aluno` as the showcase entity.) The constructor uses `Objects.requireNonNull(nome, ...)` but does not call `.isBlank()` on the result. An `Aluno` with `nome = "   "` (whitespace only) is successfully constructed and would be persisted. By contrast, `NomeDisciplina` in the same codebase normalizes and rejects blank values, and the documentation in `09-value-objects.md` explicitly teaches this as the correct pattern. The inconsistency undermines the pedagogical point that "if the object exists, it is valid."

**Fix:**
```java
this.nome = Objects.requireNonNull(nome, "Aluno deve ter um nome");
if (this.nome.isBlank()) {
    throw new IllegalArgumentException("Nome do aluno não pode ser em branco");
}
```

---

## Info

### IN-01: `10-agregados.md` Javadoc invariant description omits `Concluida` terminal state

**File:** `docs/00-ddd-sem-mudar-arquitetura/10-agregados.md:48-69`

**Issue:** The `adicionarDisciplina()` snippet shown in the documentation correctly shows only the `Cancelada` guard (which matches the current code), but since CR-01 establishes that the `Concluida` guard is missing from the implementation, the documentation is consistent with the bug rather than with the intent. Once CR-01 is fixed, this documentation will also need updating to reflect the three-way guard logic.

**Fix:** After fixing CR-01, update the `adicionarDisciplina()` snippet in `10-agregados.md` to include the `Concluida` guard, and update the prose at line 37 from "verificação de estado no Service" comparison to reflect both terminal states.

---

### IN-02: `MatricularAlunoUseCase.executar()` has no null guard on `command` parameter

**File:** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java:89`

**Issue:** `executar(MatricularAlunoCommand command)` passes `command` directly to `verificador.verificar(command.aluno(), command.turma(), command.periodo())`. If `command` is null, the NPE is thrown at line 94 against `command.aluno()` with no clear error message. Since the project pattern (visible in `VerificadorElegibilidadeMatricula`, `Aluno`, `Turma`) uses explicit null guards with descriptive messages, a null check on `command` at the entry point would be consistent.

**Fix:**
```java
public MatriculaId executar(MatricularAlunoCommand command) {
    Objects.requireNonNull(command, "Command para matricular aluno não pode ser nulo");
    // ... rest unchanged
}
```

---

### IN-03: `guia-leitura-comparativo.md` Passo 3 code snippet has incorrect constructor call — missing `alunoId` assignment

**File:** `docs/00-ddd-sem-mudar-arquitetura/guia-leitura-comparativo.md:104`

**Issue:** The constructor snippet shown in the guide at Passo 3 includes:
```java
this.eventos.add(new AlunoMatriculado(this.id, this.alunoId, ...));
```
using `...` as a placeholder. The actual constructor in `Matricula.java` line 111–112 passes five arguments: `this.id, this.alunoId, this.turmaId, this.periodoLetivo, LocalDateTime.now()`. The `...` elision is reasonable for a guide, but `this.alunoId` at that point in the private constructor would reference the field that has just been assigned — the guide should note that `this.alunoId` is the field (just set on line 103) not an unbound reference. Minor but could confuse a student tracing field initialization order.

**Fix:** Expand the ellipsis in the guide snippet to show the full parameter list so students can trace the constructor body against the actual `AlunoMatriculado` record signature:
```java
this.eventos.add(new AlunoMatriculado(this.id, this.alunoId, this.turmaId,
    this.periodoLetivo, LocalDateTime.now()));
```

---

_Reviewed: 2026-06-22_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
