---
phase: 08-testes-unit-rios-do-dom-nio-ddd
plan: "04"
subsystem: dominio/servico
tags:
  - testes-unitarios
  - domain-service
  - stub-in-memory
  - ddd
  - java21
dependency_graph:
  requires:
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Aluno.java
    - erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Turma.java
  provides:
    - MatriculaRepositorioEmMemoria (stub in-memory de MatriculaRepositorio — src/test/java)
    - VerificadorElegibilidadeMatriculaTest (4 testes do Domain Service — src/test/java)
  affects: []
tech_stack:
  added: []
  patterns:
    - Stub in-memory escrito à mão como substituto de Mockito (D-01)
    - Package-private visibility para stub de teste (D-02)
    - API fluente de configuração de stub: comMatriculaExistente()/semMatriculaExistente()
    - Injeção por construtor: new VerificadorElegibilidadeMatricula(stub)
    - PeriodoLetivo(2020,1) como passado fixo para testar período fechado (Pitfall 2)
key_files:
  created:
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/servico/MatriculaRepositorioEmMemoria.java
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatriculaTest.java
  modified: []
decisions:
  - "D-01: stub in-memory escrito à mão — sem Mockito — demonstra que o domínio DDD é testável com Java puro"
  - "D-02: stub com visibilidade package-private em src/test/java no mesmo pacote do teste"
  - "Pitfall 2 resolvido: PeriodoLetivo(2026,1) para período aberto (executado em fev-jul/2026); PeriodoLetivo(2020,1) para período fechado (passado fixo)"
metrics:
  duration: "~15 min"
  completed: "2026-06-24T11:38:36Z"
  tasks_completed: 2
  files_created: 2
---

# Phase 08 Plan 04: VerificadorElegibilidadeMatricula Testes Summary

Stub in-memory `MatriculaRepositorioEmMemoria` e 4 testes de `VerificadorElegibilidadeMatricula` usando injeção por construtor sem Mockito nem Spring.

## O Que Foi Construído

### Task 1 — MatriculaRepositorioEmMemoria (stub in-memory, D-01, D-02)

Commit: `3234b69`

Stub package-private que implementa `MatriculaRepositorio` com campo booleano configurável e API fluente:

- `comMatriculaExistente()` → `existeMatriculaAtiva()` retorna `true`
- `semMatriculaExistente()` → `existeMatriculaAtiva()` retorna `false`
- Demais métodos (`buscarPorId`, `buscarPorAluno`, `salvar`) lançam `UnsupportedOperationException` com mensagem clara
- Zero imports de `org.mockito.*` ou `org.springframework.*`

**Ponto pedagógico:** a classe demonstra que implementar uma interface de domínio em memória é trivial — e suficiente para testar o Domain Service. O desenvolvedor vê que Mockito é opcional, não obrigatório.

### Task 2 — VerificadorElegibilidadeMatriculaTest (4 testes, D-03, D-09 a D-12, TDDD-03)

Commit: `e2b868b`

Quatro testes unitários cobrindo cada regra de negócio do Domain Service:

| Teste | Cenário | Exceção Esperada |
|-------|---------|-----------------|
| `devePermitirMatriculaQuandoAlunoAtivoEPeriodoAbertoElegivelSemDuplicidade` | Happy path | Nenhuma |
| `deveLancarExcecaoQuandoAlunoInativo` | Aluno com `ativo=false` | `AlunoInativoException` |
| `deveLancarExcecaoQuandoPeriodoFechado` | Turma com PeriodoLetivo(2020,1) | `PeriodoFechadoException` |
| `deveLancarExcecaoQuandoMatriculaDuplicada` | stub.comMatriculaExistente() + período aberto | `MatriculaDuplicadaException` |

**Resultado:** `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`

## Verificação Final

```
# Testes passam
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

# Zero imports Spring ou Mockito em src/test/java/.../dominio/
grep -r "org.springframework" .../dominio/ → PASS (sem saída)
```

## Deviations from Plan

### Ajuste Automático

**1. [Rule 1 - Bug] Remoção de referência a org.mockito no Javadoc**
- **Found during:** Task 2 — verificação do acceptance criterion
- **Issue:** O Javadoc da classe `VerificadorElegibilidadeMatriculaTest` mencionava `{@code org.springframework.*}` e `{@code org.mockito.*}` para documentar o que a classe NÃO usa. Isso fazia `grep -c "org.springframework\|org.mockito"` retornar 1 em vez de 0, violando o critério de aceitação.
- **Fix:** Reformulado o Javadoc para descrever o invariante sem mencionar os pacotes diretamente: "zero imports de framework de mock ou container Spring".
- **Files modified:** `VerificadorElegibilidadeMatriculaTest.java`
- **Commit:** incluído no commit `e2b868b`

## Decisões Tomadas

### Estratégia para Pitfall 2 (data-dependência de `periodoEstaAberto()`)

O plano documentou o risco: `Turma.periodoEstaAberto()` usa `LocalDate.now()` internamente.

**Solução adotada:**

- **Período ABERTO** (testes 1 e 4): `PeriodoLetivo(2026, 1)` — semestre 1 (fev-jul de 2026). Data de execução: 24/jun/2026 (dentro do intervalo). Documentado com comentário nos testes.
- **Período FECHADO** (teste 3): `PeriodoLetivo(2020, 1)` — semestre 1 de 2020, sempre no passado. Sem dependência de data.

O PATTERNS.md sugeria `PeriodoLetivo(2026, 2)` para o teste de duplicidade mas isso estaria fechado em junho. A escolha de `PeriodoLetivo(2026, 1)` é a correta para o contexto de execução atual.

## Known Stubs

Nenhum. Todos os valores dos testes são derivados diretamente dos contratos do código de produção.

## Threat Flags

Nenhum. Esta fase cria apenas arquivos de teste em `src/test/java` — sem novos endpoints, sem acesso a dados externos, sem superfície de ataque adicional.

## Self-Check: PASSED

Arquivos criados:
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/servico/MatriculaRepositorioEmMemoria.java` — FOUND
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatriculaTest.java` — FOUND

Commits:
- `3234b69` — feat(08-04): criar MatriculaRepositorioEmMemoria — FOUND
- `e2b868b` — feat(08-04): criar VerificadorElegibilidadeMatriculaTest — FOUND

Testes: `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
