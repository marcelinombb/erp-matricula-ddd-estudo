---
phase: 04-interface-docker-e-material-didatico
fixed_at: 2026-06-21T00:00:00Z
review_path: .planning/phases/04-interface-docker-e-material-didatico/04-REVIEW.md
iteration: 1
findings_in_scope: 7
fixed: 7
skipped: 0
status: all_fixed
---

# Phase 04: Code Review Fix Report

**Fixed at:** 2026-06-21
**Source review:** .planning/phases/04-interface-docker-e-material-didatico/04-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 7 (3 critical, 4 warnings)
- Fixed: 7
- Skipped: 0

## Fixed Issues

### CR-01: `Turma` placeholder constructed with `vagasMaximas=0` — throws on every POST /matriculas

**Files modified:** `erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java`
**Commit:** 3273404
**Applied fix:** Changed `vagasMaximas` from `0` to `1` in the `Turma` placeholder constructor in `matricular()`. Added comment explaining that value must be positive to satisfy the constructor guard but is not read by the use case.

---

### CR-02: `periodoFim` accepted from HTTP request but silently discarded — contract mismatch

**Files modified:** `erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java`
**Commit:** b5c42da
**Applied fix:** Applied Option A — removed `periodoFim` field entirely from `MatricularAlunoRequest` record. Updated the Javadoc to explain that the semestre is derived automatically from `periodoInicio` month (1-6 → semester 1, 7-12 → semester 2). Also updated the `@param` in the `matricular()` method Javadoc to remove the reference to `periodoFim`.

---

### CR-03: `inserirItens` XML omits `adicionada_em` column — violates NOT NULL constraint after V3

**Files modified:** `erp-matricula-app/src/main/resources/mapper/MatriculaMapper.xml`
**Commit:** 021c80c
**Applied fix:** Added `adicionada_em` to the INSERT column list with `NOW()` as the value. Added a comment in the XML explaining that this makes the dependency on the NOT NULL column explicit and avoids silent reliance on the DB DEFAULT.

---

### WR-01: `DemoRunner` swallows all exceptions including startup failures — masks real errors

**Files modified:** `erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/config/DemoRunner.java`
**Commit:** 4a2bb5e
**Applied fix:** Changed the broad `catch (Exception e)` to `catch (org.springframework.dao.DataIntegrityViolationException e)`. Only the expected unique-index violation on re-execution is caught; all other exceptions propagate and fail startup loudly. Added a comment explaining why this specific exception is caught.

---

### WR-02: `handleValidacao` uses `Collectors.toList()` instead of `.toList()` — inconsistent with Java 21 style

**Files modified:** `erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/ExcecaoHandler.java`
**Commit:** 851d906
**Applied fix:** Replaced `.collect(Collectors.toList())` with `.toList()` in `handleValidacao()`. Removed the now-unused `import java.util.stream.Collectors;` line.

---

### WR-03: Wrong package path in ADR-001 verification command — grep will always return zero results

**Files modified:** `docs/adrs/ADR-001-mybatis-vs-jpa.md`
**Commit:** 705f99c
**Applied fix:** Corrected the grep path from `src/main/java/br/com/erp/dominio/` to `src/main/java/br/com/escola/matricula/dominio/` so the command actually targets the real package where verification is meaningful.

---

### WR-04: `ItemMatriculaRow` uses public mutable fields — contradicts pedagogical message about encapsulation

**Files modified:** `erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/ItemMatriculaRow.java`
**Commit:** b376886
**Applied fix:** Added Javadoc paragraph explaining why public fields are used (MyBatis direct field injection via reflection), that the pattern is consistent with the sibling `MatriculaRow` class, and that package-private fields with getters would be preferred in production. This documents the intentional design choice rather than changing the field visibility — switching `ItemMatriculaRow` to package-private while `MatriculaRow` remained public would create inconsistency between the two sibling Row classes.

---

_Fixed: 2026-06-21_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
