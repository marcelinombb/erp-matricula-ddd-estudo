---
status: partial
phase: 05-diagnostico-codigo-com-anti-padroes
source: [05-VERIFICATION.md]
started: 2026-06-21T00:00:00Z
updated: 2026-06-21T00:00:00Z
---

## Current Test

[awaiting human testing]

## Tests

### 1. DIAG-06 doc/code trail — countDisciplinas discrepancy
expected: `06-acoplamento-banco.md` describes a reading trail (MatriculaServiceImpl → matriculaRepository.countDisciplinas). Student follows the trail and finds a working example of DB-coupling.
result: [pending]

### 2. DisciplinaServiceImpl reachability
expected: DIAG-04 (Duplicação de Regras) can be studied and understood by a developer reading the module. The question is whether HTTP reachability is required for the "before" module to be pedagogically complete.
result: [pending]

## Summary

total: 2
passed: 0
issues: 0
pending: 2
skipped: 0
blocked: 0

## Gaps
