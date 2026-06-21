---
status: passed
phase: 04-interface-docker-e-material-didatico
source: [04-VERIFICATION.md]
started: 2026-06-21
updated: 2026-06-21
---

## Current Test

Approved by developer on 2026-06-21.

## Tests

### 1. docker compose up end-to-end
expected: HTTP 201 em POST /matriculas, Flyway aplica V1+V2+V3, dados persistem no PostgreSQL
result: passed

### 2. Bean Validation e mapeamento de erros
expected: Campo ausente retorna 400 com {"erro":"DADOS_INVALIDOS","campos":[...]}. Duplicata retorna 409 com {"erro":"MATRICULA_DUPLICADA"}
result: passed — 400 DADOS_INVALIDOS confirmado em testes manuais

### 3. 422 com campos limite e atual
expected: 7ª disciplina retorna 422 com {"erro":"LIMITE_DISCIPLINAS_EXCEDIDO","limite":6,"atual":6}
result: passed

## Summary

total: 3
passed: 3
issues: 0
pending: 0
skipped: 0
blocked: 0

## Gaps
