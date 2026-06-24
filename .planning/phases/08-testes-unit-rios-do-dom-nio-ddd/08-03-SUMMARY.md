---
phase: 08-testes-unit-rios-do-dom-nio-ddd
plan: "03"
subsystem: dominio/modelo
tags:
  - tdd
  - aggregate
  - domain-events
  - invariantes
  - matricula
dependency_graph:
  requires:
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/StatusMatricula.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/evento/AlunoMatriculado.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/evento/DisciplinaAdicionada.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/evento/MatriculaCancelada.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/excecao/MatriculaCanceladaException.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/excecao/LimiteDisciplinasExcedidoException.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/excecao/DisciplinaJaMatriculadaException.java
  provides:
    - MatriculaTest: testes unitários do Aggregate Matricula — 4 invariantes + 4 domain events
  affects: []
tech_stack:
  added: []
  patterns:
    - JUnit 5 @Test + @DisplayName com nomes em português
    - AssertJ assertThat/assertThatThrownBy — sem assertEquals
    - cast para tipo concreto de Domain Event: (AlunoMatriculado) eventos.get(0)
    - Given-When-Then com comentários explícitos
    - construtor de reconstituição para simular estado pré-existente
key_files:
  created:
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/modelo/MatriculaTest.java
  modified: []
decisions:
  - "D-06 aplicada: cast para tipo concreto + verificação de campos do record em cada teste de evento"
  - "D-07 aplicada: coletarEventos() chamado entre adições de disciplina no teste de Guard 2 para não acumular eventos"
  - "D-09 aplicada: Given-When-Then com comentários explícitos em todos os 8 testes"
  - "D-10 aplicada: nomes de métodos de teste em português"
  - "D-12 aplicada: zero imports org.springframework.* verificado por grep"
  - "Arquivo criado completo (Tasks 1 e 2 em um único commit): testes de invariantes e eventos são inseparáveis no mesmo arquivo de teste do Aggregate"
  - "DisciplinaAdicionada.disciplina() — campo é 'disciplina' (não 'nomeDisciplina' como o plano sugeria), corrigido ao ler o código de produção antes de escrever"
metrics:
  duration: "~15 minutos"
  completed_date: "2026-06-24"
  tasks_completed: 2
  files_created: 1
  files_modified: 0
requirements_satisfied:
  - TDDD-01
  - TDDD-04
---

# Phase 08 Plan 03: MatriculaTest — Invariantes e Domain Events do Aggregate Summary

**One-liner:** Testes unitários do Aggregate Matricula com 4 invariantes (cancelada, limite 6, duplicidade, caminho feliz) e 4 Domain Events (AlunoMatriculado, DisciplinaAdicionada, MatriculaCancelada, contrato de limpeza) — zero Spring, zero banco.

## What Was Built

Arquivo `MatriculaTest.java` criado em `src/test/java/br/com/escola/matricula/dominio/modelo/` com:

- **Método auxiliar `criarMatriculaAtiva()`**: instancia `Matricula.criar()` e descarta o `AlunoMatriculado` via `coletarEventos()`, retornando matrícula limpa para testes que testam comportamentos subsequentes.

- **4 testes de invariantes (TDDD-01)**:
  - `deveAdicionarDisciplinaComSucesso()` — caminho feliz, verifica que disciplina aparece em `getDisciplinas()`
  - `deveLancarExcecaoAoAdicionarDisciplinaEmMatriculaCancelada()` — Guard 1, usa construtor de reconstituição com `StatusMatricula.Cancelada`
  - `deveLancarExcecaoAoAdicionarASetimaDisciplina()` — Guard 2, adiciona 6 com sucesso, testa exceção na 7ª
  - `deveLancarExcecaoAoAdicionarDisciplinaDuplicada()` — Guard 3, mesma `NomeDisciplina` duas vezes

- **4 testes de Domain Events (TDDD-04)**:
  - `deveEmitirEventoAlunoMatriculadoAoCriar()` — verifica `alunoId`, `turmaId`, `periodoLetivo`, `ocorridoEm`
  - `deveEmitirEventoDisciplinaAdicionadaAoAdicionar()` — verifica `matriculaId`, `disciplina`, `ocorridoEm`
  - `deveEmitirEventoMatriculaCanceladaAoCancelar()` — verifica `matriculaId`, `ocorridoEm`, e status do Aggregate
  - `coletarEventosDeveRetornarListaVaziaNaSegundaChamada()` — demonstra contrato de limpeza: segunda chamada retorna lista vazia

## Commits

| Task | Descrição | Commit | Arquivos |
|------|-----------|--------|---------|
| Task 1 + Task 2 | MatriculaTest.java — 4 invariantes + 4 domain events | `8d9dcf2` | MatriculaTest.java (criado) |

> Nota: o arquivo foi criado completo em um único commit cobrindo ambas as tasks. A separação entre Task 1 (invariantes) e Task 2 (eventos) é lógica, não fisica — os testes de eventos são inseparáveis do arquivo de teste do Aggregate.

## Verification Results

```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Verificações adicionais:
- `grep "org.springframework" MatriculaTest.java` → nenhum resultado (D-12 satisfeita)
- `grep -c "@Test" MatriculaTest.java` → 8 (correto)

## Deviations from Plan

### Ajustes Automáticos (Rule 1/2)

**1. [Rule 1 - Correção de campo] Campo `disciplina` em vez de `nomeDisciplina` em DisciplinaAdicionada**
- **Encontrado durante:** Leitura de `DisciplinaAdicionada.java` antes de escrever o código
- **Issue:** O plano (Task 2, seção `<action>`) menciona `evento.nomeDisciplina()` para o teste do evento `DisciplinaAdicionada`. O campo real no record é `disciplina()`, não `nomeDisciplina()`.
- **Fix:** Usado `evento.disciplina()` nos testes — alinhado com o código de produção.
- **Arquivos modificados:** `MatriculaTest.java` (prevenção antes de escrever)

**2. [Rule 2 - Completude] Criação de diretório de teste**
- **Encontrado durante:** Task 1
- **Issue:** O diretório `src/test/java/br/com/escola/matricula/dominio/modelo/` não existia.
- **Fix:** Criado via `mkdir -p` antes de escrever o arquivo de teste.
- **Arquivos modificados:** diretório criado (sem arquivo de código alterado)

## Known Stubs

Nenhum — os testes verificam comportamento real do Aggregate de produção, sem stubs de dados ou valores hardcoded que fluam para UI.

## Threat Flags

Nenhum — arquivos de teste não introduzem superfície de ataque em produção (T-08-03: accept).

## Self-Check: PASSED

- [x] `MatriculaTest.java` existe em `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/modelo/`
- [x] Commit `8d9dcf2` existe no log git
- [x] BUILD SUCCESS com 8 testes, 0 falhas
- [x] Zero imports `org.springframework.*`
- [x] 8 métodos `@Test` no arquivo
