---
status: partial
phase: 04-interface-docker-e-material-didatico
source: [04-VERIFICATION.md]
started: 2026-06-21
updated: 2026-06-21
---

## Current Test

[awaiting human testing]

## Tests

### 1. docker compose up end-to-end
expected: HTTP 201 em POST /matriculas, Flyway aplica V1+V2+V3, dados persistem no PostgreSQL
result: [pending]

### 2. Bean Validation e mapeamento de erros
expected: Campo ausente retorna 400 com {"erro":"DADOS_INVALIDOS","campos":[...]}. Duplicata retorna 409 com {"erro":"MATRICULA_DUPLICADA"}
result: [pending]

### 3. 422 com campos limite e atual
expected: 7ª disciplina retorna 422 com {"erro":"LIMITE_DISCIPLINAS_EXCEDIDO","limite":6,"atual":6}
result: [pending]

## Summary

total: 3
passed: 0
issues: 0
pending: 3
skipped: 0
blocked: 0

## Gaps
