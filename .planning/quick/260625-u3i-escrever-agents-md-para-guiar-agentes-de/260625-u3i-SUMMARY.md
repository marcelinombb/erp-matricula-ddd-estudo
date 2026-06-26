---
phase: quick
plan: 260625-u3i
subsystem: documentation
tags: [agents, operational-guide, ddd, mybatis, ai-tooling]
dependency_graph:
  requires: []
  provides: [AGENTS.md]
  affects: [onboarding, ai-agent-productivity]
tech_stack:
  added: []
  patterns: [agents-md-pattern, operational-guide]
key_files:
  created:
    - AGENTS.md
  modified: []
decisions:
  - "AGENTS.md usa referências a arquivos existentes para evitar duplicação de conteúdo pedagógico do README.md"
  - "Pitfall 5 (replace-all) adicionado além dos 4 descritos no plano — informação crítica do MatriculaRepositorioMyBatis"
metrics:
  duration: "~2 min"
  completed: "2026-06-26"
requirements: [DOCS-AGENTS-01]
---

# Phase quick Plan 260625-u3i: AGENTS.md para Guia de Agentes de IA — Summary

AGENTS.md criado no raiz do repositório com guia operacional completo para agentes de IA desenvolverem no módulo DDD: estrutura de pacotes, checklists de artefatos, padrões de use case de escrita e leitura, pitfalls críticos MyBatis com exemplos reais, e tabela de referências rápidas.

## What Was Built

`AGENTS.md` (265 linhas) no raiz do repositório `erp-matricula`, cobrindo:

- **Seção 1** — Visão geral dos dois módulos com link para README.md
- **Seção 2** — Tabela de pacotes com regra de dependência e comando grep de verificação
- **Seção 3** — Checklist de 9 artefatos para use case de escrita + padrão dos 4 passos obrigatórios do `executar()` + proibições explícitas do UseCase
- **Seção 4** — Padrão de query sem CQRS com snippet `BuscarMatriculaUseCase` + fluxo de 4 passos para nova query
- **Seção 5** — 5 pitfalls MyBatis críticos com exemplos ERRADO/CERTO
- **Seção 6** — Padrão de Value Object com Java 21 record
- **Seção 7** — Convenções de schema PostgreSQL resumidas com referência ao V1__schema.sql
- **Seção 8** — Tabela de nomenclatura com 14 tipos de artefato
- **Seção 9** — 16 referências rápidas para copiar padrões

## Commits

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Criar AGENTS.md com guia completo DDD + MyBatis | 2980f63 | AGENTS.md |

## Deviations from Plan

### Auto-additions

**1. [Rule 2 - Missing Critical Content] Pitfall 5: replace-all strategy**
- **Found during:** Task 1 — análise de `MatriculaRepositorioMyBatis.java`
- **Issue:** O plano listava 4 pitfalls, mas a estratégia replace-all (delete + reinsert) é um padrão não óbvio que causa bugs se um agente tentar fazer diff de itens
- **Fix:** Adicionada Seção 5 Pitfall 5 com a sequência completa de operações
- **Files modified:** AGENTS.md

## Self-Check: PASSED

- AGENTS.md existe: FOUND
- Commit 2980f63 existe: FOUND
- jdbcType=OTHER aparece 3 vezes: PASS
- notNullColumn aparece 3 vezes: PASS
- BuscarMatriculaUseCase aparece 4 vezes: PASS
- 265 linhas (min: 120): PASS

## Threat Flags

Nenhuma nova superfície de segurança introduzida — arquivo de documentação estático sem endpoints ou acesso a dados.
