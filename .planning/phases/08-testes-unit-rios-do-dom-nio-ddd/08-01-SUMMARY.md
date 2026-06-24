---
phase: 08-testes-unit-rios-do-dom-nio-ddd
plan: "01"
subsystem: dominio/vo
tags: [testes-unitarios, value-objects, junit5, assertj, ddd]
dependency_graph:
  requires: []
  provides:
    - NomeDisciplinaTest (5 testes)
    - PeriodoLetivoTest (4 testes)
  affects:
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/
tech_stack:
  added: []
  patterns:
    - JUnit 5 + AssertJ sem Spring (D-12)
    - Given-When-Then com comentários explícitos (D-09)
    - Nomes de teste em português (D-10)
    - Um arquivo de teste por VO (D-03, D-04)
key_files:
  created:
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/NomeDisciplinaTest.java
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/PeriodoLetivoTest.java
  modified: []
decisions:
  - Zero imports org.springframework.* nos testes do domínio (D-12)
  - Testes instanciam VOs diretamente com new — sem factory, sem Spring context
  - AssertJ para asserções fluentes (assertThatThrownBy, assertThatCode, assertThat)
  - Nomes de método em português descrevem comportamento de negócio
metrics:
  duration: "2 minutos"
  completed: "2026-06-24"
  tasks_completed: 2
  tasks_total: 2
  files_created: 2
  files_modified: 0
---

# Phase 08 Plan 01: Testes Unitários dos Value Objects NomeDisciplina e PeriodoLetivo — Summary

**One-liner:** Dois arquivos de teste JUnit 5 sem Spring testam NomeDisciplina (5 casos) e PeriodoLetivo (4 casos) instanciando records Java 21 diretamente via new + AssertJ.

## O que foi construído

Dois arquivos de teste unitário para os Value Objects do módulo `erp-matricula-ddd`, demonstrando que o domínio DDD é testável sem nenhuma dependência de framework:

- **NomeDisciplinaTest.java** — 5 testes cobrindo: normalização de espaços (`strip()`), rejeição de valor em branco, rejeição acima de 100 caracteres, aceitação de exatamente 100 caracteres, e igualdade por valor (equals/hashCode do record Java 21).
- **PeriodoLetivoTest.java** — 4 testes cobrindo: criação válida com verificação de `descricao()`, rejeição de ano anterior a 2000, rejeição de semestre 0, rejeição de semestre 3.

## Resultado dos Testes

```
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

- NomeDisciplinaTest: 5/5 passando
- PeriodoLetivoTest: 4/4 passando
- Zero imports `org.springframework.*` em ambos os arquivos

## Commits

| Task | Nome | Commit | Arquivos |
|------|------|--------|----------|
| 1 | NomeDisciplinaTest — 5 testes | 12d9e5e | NomeDisciplinaTest.java (criado) |
| 2 | PeriodoLetivoTest — 4 testes | 3ad1fee | PeriodoLetivoTest.java (criado) |

## Decisões de Implementação

- **Mensagens de exceção verificadas:** A mensagem do `NomeDisciplina` contém "em branco" e "100 caracteres" — matchers `.hasMessageContaining()` alinhados com o código de produção.
- **Mensagens de PeriodoLetivo:** Exceção de ano contém "2000"; exceção de semestre contém "Semestre inválido" — ambas verificadas antes de escrever os matchers.
- **Semestre 0 e 3 como testes separados:** O plano define 4 testes distintos (D-03, D-04) — semestre 0 e semestre 3 ficaram em métodos separados para máxima clareza pedagógica.

## Deviations from Plan

None — plano executado exatamente conforme especificado.

## Known Stubs

None — nenhum stub ou placeholder introduzido.

## Threat Flags

None — arquivos de teste não introduzem superfície de ataque em produção.

## Self-Check

- [x] NomeDisciplinaTest.java existe em erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/
- [x] PeriodoLetivoTest.java existe em erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/
- [x] Commit 12d9e5e (Task 1) existe no histórico
- [x] Commit 3ad1fee (Task 2) existe no histórico
- [x] mvn test -Dtest="NomeDisciplinaTest,PeriodoLetivoTest" retorna BUILD SUCCESS, 9 testes, 0 falhas
- [x] Zero imports org.springframework.* nos dois arquivos

## Self-Check: PASSED
