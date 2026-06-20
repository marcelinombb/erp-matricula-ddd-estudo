---
phase: 02-design-tatico-e-modelagem-visual
plan: "01"
subsystem: documentation
tags: [ddd, value-objects, entidades, java-21, pedagogical]
dependency_graph:
  requires:
    - docs/01-design-estrategico/linguagem-ubiqua.md
    - docs/adrs/ADR-003-referencia-por-id.md
    - docs/adrs/ADR-004-codigo-em-portugues.md
  provides:
    - docs/02-design-tatico/value-objects.md
    - docs/02-design-tatico/entidades.md
  affects:
    - docs/02-design-tatico/agregados.md (referenciado de entidades.md via nota sobre ItemMatricula)
tech_stack:
  added: []
  patterns:
    - bottom-up narrative opening (problema antes do padrão)
    - Java 21 record como Value Object com construtor compacto
    - Java 21 class como Entidade com equals/hashCode por ID
    - ERRADO/CERTO anti-pattern blocks em Erros Comuns
    - Blockquote Lição para insight pedagógico central
    - Cross-reference inline para ADRs via link relativo Markdown
key_files:
  created:
    - docs/02-design-tatico/value-objects.md
    - docs/02-design-tatico/entidades.md
  modified: []
decisions:
  - "IDs tipados (AlunoId, TurmaId, MatriculaId) agrupados em subseção IDs Tipados em value-objects.md — mesmo padrão, profundidade proporcional"
  - "ItemMatricula não documentada em entidades.md — apenas mencionada com link para agregados.md (decisão pedagógica: sem Matricula, ItemMatricula não tem significado)"
  - "Algoritmo dígito verificador do CPF deixado como placeholder TODO Fase 3 — conforme D-07 (snippets prospectivos)"
metrics:
  duration: ~25min
  completed: 2026-06-20
  tasks_completed: 2
  tasks_total: 2
  files_created: 2
  files_modified: 0
---

# Phase 02 Plan 01: Value Objects e Entidades — Summary

Value Objects e Entidades documentados com abordagem bottom-up, snippets Java 21 com comentários de fit DDD, catálogo dos 6 VOs, tabela de contraste Entidade vs VO, e seções Erros Comuns com pares ERRADO/CERTO.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Criar docs/02-design-tatico/value-objects.md | 2be5a63 | docs/02-design-tatico/value-objects.md |
| 2 | Criar docs/02-design-tatico/entidades.md | b4daedb | docs/02-design-tatico/entidades.md |

## What Was Built

### `docs/02-design-tatico/value-objects.md`

Documentação completa dos 6 Value Objects do domínio de Matrícula:

- **Abertura bottom-up (D-04):** problema do CPF como String (validação espalhada, parâmetros invertidos) antes de nomear o padrão Value Object
- **Catálogo de 6 VOs** com tabela: `Cpf`, `PeriodoLetivo`, `MatriculaId`, `AlunoId`, `TurmaId`, `NomeDisciplina` — com tipo Java 21, validação e complexidade
- **Blockquote Lição** após o catálogo explicando `equals`/`hashCode` por valor nos `record`s Java 21
- **Snippets Java 21** para cada VO com comentários `// Java 21:` e `// DDD fit:`
  - `Cpf`: construtor compacto com normalização de máscara e placeholder de algoritmo (TODO Fase 3)
  - `PeriodoLetivo`: validação completa de `ano >= 2000` e `semestre 1..2`
  - `NomeDisciplina`: validação de nulo, branco e comprimento máximo
  - `### IDs Tipados`: agrupamento de `MatriculaId`, `AlunoId`, `TurmaId` com cross-reference ADR-003
- **Erros Comuns (D-05, D-09):** 2 pares ERRADO/CERTO:
  1. String primitiva vaza validação — ERRADO: `@Service MatriculaService(String cpfAluno)` / CERTO: `executar(Cpf cpf, PeriodoLetivo periodo)`
  2. Comparação errada — ERRADO: `cpf1.valor().equals(cpf2.valor())` / CERTO: `cpf1.equals(cpf2)`

### `docs/02-design-tatico/entidades.md`

Documentação das Entidades `Aluno` e `Turma` com contraste central contra Value Objects:

- **Abertura bottom-up (D-04):** parte do que foi estabelecido em `value-objects.md` sobre `Cpf` — "o que acontece com `Aluno`?" — para introduzir o contraste de identidade persistente
- **Tabela de contraste** Entidade vs Value Object: 6 características (identidade, imutabilidade, `equals`/`hashCode`, tipo Java 21, ciclo de vida, persistência)
- **`### Aluno`:** snippet `record AlunoId` + `class Aluno` com `final AlunoId id`, `boolean ativo`, `equals`/`hashCode` por ID. Blockquote "Por que não é um record?" explicando imutabilidade de `record` vs estado mutável de Entidade
- **`### Turma`:** snippet `record TurmaId` + `class Turma` com nota sobre vagas disponíveis no v2 e cross-reference ADR-003
- **Nota sobre `ItemMatricula`:** parágrafo explícito com link para `agregados.md` — entidade interna ao Aggregate, sem significado independente
- **Erros Comuns (D-05, D-09):** 2 pares ERRADO/CERTO:
  1. `equals` por conteúdo — ERRADO: comparação por `nome` e `cpf` / CERTO: comparação apenas por `id`
  2. String/UUID cru como ID — ERRADO: `UUID alunoId` + `UUID turmaId` / CERTO: `AlunoId` + `TurmaId` com cross-reference ADR-003

## Requirements Coverage

- **TAT-01:** Entidades documentadas com identidade (`AlunoId`, `TurmaId`), ciclo de vida (status ativo/inativo; vagas no v2) e responsabilidades (elegibilidade para matrícula)
- **TAT-02:** Todos os 6 VOs de DOM-01 documentados com catálogo, validações, snippets Java 21 e anti-patterns

## Deviations from Plan

None — plan executed exactly as written.

Nota: Os `TODO Fase 3` nos snippets de `Cpf.cpfComDigitoVerificadorValido()` e `PeriodoLetivo.estaAberto()` são **intencionais** — o plano estabelece D-07 (snippets prospectivos) explicitamente. Não são stubs que bloqueiam o objetivo do plano; são especificações técnicas para a Fase 3.

## Known Stubs

Os seguintes placeholders são intencionais (D-07 — snippets prospectivos) e rastreados para a Fase 3:

| Arquivo | Linha | Stub | Razão |
|---------|-------|------|-------|
| `docs/02-design-tatico/value-objects.md` | ~60 | `cpfComDigitoVerificadorValido()` retorna `true` | Algoritmo módulo 11 será implementado na Fase 3 |
| `docs/02-design-tatico/value-objects.md` | ~120 | `PeriodoLetivo.estaAberto()` retorna `true` | Lógica de data de encerramento depende de configuração institucional — Fase 3 |

Estes stubs não impedem o objetivo do plano (documentação pedagógica de VOs e Entidades).

## Threat Flags

Nenhuma nova superfície de segurança introduzida — esta fase entrega apenas arquivos Markdown estáticos sem código executável, endpoints ou acesso a dados.

## Self-Check: PASSED

- [x] `docs/02-design-tatico/value-objects.md` existe: FOUND
- [x] `docs/02-design-tatico/entidades.md` existe: FOUND
- [x] Commit 2be5a63 existe: FOUND
- [x] Commit b4daedb existe: FOUND
- [x] Ambos os arquivos passam na verificação automatizada do plano
