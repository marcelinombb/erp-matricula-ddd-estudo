---
phase: 08-testes-unit-rios-do-dom-nio-ddd
plan: "02"
subsystem: dominio/vo
tags: [testes, value-objects, cpf, aluno-id, junit5, assertj]
dependency_graph:
  requires: []
  provides:
    - CpfTest (4 testes unitários do VO Cpf)
    - AlunoIdTest (3 testes unitários do VO AlunoId)
  affects:
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/
tech_stack:
  added: []
  patterns:
    - "Given-When-Then com comentários explícitos (D-09)"
    - "AssertJ para asserções fluentes — assertThat, assertThatThrownBy, assertThatNullPointerException (D-11)"
    - "Zero imports org.springframework.* nos testes de domínio (D-12)"
    - "Constantes de classe privadas para CPF válido (CPF_VALIDO_COM_MASCARA, CPF_VALIDO_NORMALIZADO)"
key_files:
  created:
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/CpfTest.java
    - erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/AlunoIdTest.java
  modified: []
decisions:
  - "CPF_VALIDO_COM_MASCARA = 529.982.247-25 — CPF verificado matematicamente contra algoritmo módulo 11"
  - "Mensagem de exceção .hasMessageContaining('dígito verificador inválido') — extraída do Cpf.java linha 53"
  - "assertThatNullPointerException para AlunoId(null) — Objects.requireNonNull lança NPE conforme AlunoId.java linha 29"
metrics:
  duration: "1m39s"
  completed: "2026-06-24"
  tasks_completed: 2
  tasks_total: 2
  files_created: 2
  files_modified: 0
requirements_satisfied:
  - TDDD-02
---

# Phase 8 Plan 2: Testes Unitários dos VOs Cpf e AlunoId — Summary

**One-liner:** Testes unitários de CpfTest (4 métodos) e AlunoIdTest (3 métodos) cobrindo normalização de máscara, formatação, rejeição de dígito verificador inválido, rejeição de null e igualdade por valor (record Java 21).

## Tasks Executadas

| Task | Nome | Commit | Arquivos |
|------|------|--------|---------|
| 1 | Criar CpfTest.java — 4 testes | c83e49c | `dominio/vo/CpfTest.java` (criado) |
| 2 | Criar AlunoIdTest.java — 3 testes | c0a4f14 | `dominio/vo/AlunoIdTest.java` (criado) |

## Verificação Final

```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

- `CpfTest`: 4 testes — normalização, formatado(), dígito verificador inválido, igualdade
- `AlunoIdTest`: 3 testes — criação válida, NullPointerException para null, igualdade
- Zero imports `org.springframework.*` em todos os arquivos de teste do VO

## Decisões Tomadas

### CPF de teste: 529.982.247-25 (Pitfall 3 considerado)

O CPF `529.982.247-25` foi escolhido como constante de teste por ser matematicamente válido contra o algoritmo módulo 11 implementado em `Cpf.java`. CPFs com todos dígitos iguais (ex: `111.111.111-11`) são rejeitados pelo algoritmo — o teste `deveLancarExcecaoParaCpfComDigitoVerificadorInvalido` usa exatamente este caso.

### Mensagem de exceção para dígito verificador inválido

Extraída diretamente de `Cpf.java` linha 53: `"CPF com dígito verificador inválido: " + valor`. O matcher `.hasMessageContaining("dígito verificador inválido")` foi escolhido por ser resiliente a mudanças no restante da mensagem.

### NullPointerException para AlunoId(null)

`AlunoId.java` usa `Objects.requireNonNull(valor, "AlunoId não pode ser nulo")` — lança `NullPointerException`. O teste usa `assertThatNullPointerException().isThrownBy(...).withMessageContaining("nulo")` conforme padrão do 08-PATTERNS.md.

## Deviations from Plan

None — plano executado exatamente como escrito.

## Threat Surface Scan

Nenhum novo endpoint de rede, path de auth, acesso a arquivo ou mudança de schema introduzida. Arquivos de teste são somente código de teste (sem superfície de ataque em produção). Conforme T-08-02 no threat model do plano: `accept`.

## Known Stubs

Nenhum — ambos os arquivos de teste instanciam as classes de domínio diretamente e verificam comportamento real.

## Self-Check

- [x] `CpfTest.java` existe em `dominio/vo/`
- [x] `AlunoIdTest.java` existe em `dominio/vo/`
- [x] Commit c83e49c existe (CpfTest)
- [x] Commit c0a4f14 existe (AlunoIdTest)
- [x] 7 testes passando, 0 falhas, BUILD SUCCESS
- [x] Zero imports Spring

## Self-Check: PASSED
