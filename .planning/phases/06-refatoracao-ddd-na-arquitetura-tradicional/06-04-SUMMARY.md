---
phase: "06-refatoracao-ddd-na-arquitetura-tradicional"
plan: 04
subsystem: "docs/00-ddd-sem-mudar-arquitetura"
tags: [documentation, ddd, reading-guide, exercise, pedagogical]
dependency_graph:
  requires: []
  provides:
    - guia-leitura-comparativo.md
    - exercicio-classificacao.md
  affects:
    - docs/00-ddd-sem-mudar-arquitetura/
tech_stack:
  added: []
  patterns:
    - "Trail instrucional com tom imperativo (Abra/Observe/Compare)"
    - "Gabarito interativo com HTML <details> para auto-avaliação"
key_files:
  created:
    - docs/00-ddd-sem-mudar-arquitetura/guia-leitura-comparativo.md
    - docs/00-ddd-sem-mudar-arquitetura/exercicio-classificacao.md
  modified: []
decisions:
  - "Snippets referenciados por método/bloco, não por número de linha, para resistir a refatorações futuras"
  - "Tom imperativo do guia move o olho do estudante — não explica em abstrato"
  - "Critério de decisão visível antes das 10 regras (não apenas nos gabaritos) para orientar o raciocínio"
metrics:
  duration_minutes: 2
  completed_date: "2026-06-22"
  tasks_completed: 2
  files_created: 2
  files_modified: 0
---

# Phase 06 Plan 04: Guia de Leitura e Exercício de Classificação — Summary

Trail instrucional de 5 passos cobrindo a operação "matricular" nos dois módulos, mais exercício interativo de classificação Domínio vs. Aplicação com 10 regras e gabarito em HTML details.

---

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Criar guia-leitura-comparativo.md | 4ded3f2 | docs/00-ddd-sem-mudar-arquitetura/guia-leitura-comparativo.md |
| 2 | Criar exercicio-classificacao.md | a293ad0 | docs/00-ddd-sem-mudar-arquitetura/exercicio-classificacao.md |

---

## What Was Built

### guia-leitura-comparativo.md (191 linhas)

Trail instrucional de 5 passos cobrindo a transformação completa da operação "matricular":

- **Passo 1:** `MatriculaServiceImpl.matricular()` — regras de negócio no Service (aluno ativo, período, criação com setters e `setStatus("ATIVA")`)
- **Passo 2:** `VerificadorElegibilidadeMatricula.verificar()` — Domain Service concentra elegibilidade; `estaAtivo()` vs. `isAtivo()` explica Linguagem Ubíqua
- **Passo 3:** `Matricula.criar()` + construtor privado — estado inicial `Ativa` garantido; evento `AlunoMatriculado` coletado automaticamente
- **Passo 4:** `MatricularAlunoUseCase.executar()` — 4 linhas de orquestração, zero decisões de negócio; contraste explícito com ~35 linhas de `MatriculaServiceImpl.matricular()`
- **Passo 5:** Exercício livre com `AdicionarDisciplinaUseCase` e `CancelarMatriculaUseCase` — o padrão se repete

Tom imperativo com 14 ocorrências de Abra/Observe/Compare. Snippets focados (máx 15 linhas cada) referenciando métodos reais verificados no código.

### exercicio-classificacao.md (198 linhas)

10 regras do domínio de matrícula com gabarito em `<details>` HTML:

- **Casos claros (1-6):** limite de disciplinas, estado cancelada, aluno inativo, período aberto, transação (`@Transactional`), e-mail de confirmação
- **Caso médio (7):** período letivo da turma — parece "validação de entrada" mas é Domínio
- **Casos ambíguos (8-10):**
  - Regra 8 (CPF): parece regra técnica, mas CPF inválido é dado inconsistente — Domínio
  - Regra 9 (Financeiro): parece regra de negócio, mas é coordenação de Bounded Contexts — Aplicação
  - Regra 10 (duplicata): parece UNIQUE constraint de banco, mas é decisão acadêmica — Domínio

Critério de decisão visível antes das regras. Cada gabarito inclui justificativa com o critério explícito, não apenas a resposta.

---

## Decisions Made

1. **Snippets por método, não por linha:** Seguindo RESEARCH.md Pitfall 1, todas as referências ao código usam método + descrição do bloco. Linhas são frágeis; nomes de método são rastreáveis.

2. **Tom imperativo no guia:** D-04 especifica guia instrucional — o guia move o olho do estudante, não explica em abstrato. Cada passo começa com um verbo de ação.

3. **Critério de decisão antes das regras no exercício:** D-12 (casos claros e ambíguos) + Pitfall 4 (gabarito com justificativa) levaram a expor o critério de classificação no início do arquivo, não apenas nos gabaritos.

4. **Referência ao método correto no módulo camadas:** O trail DIAG-06 da Phase 5 estava quebrado (apontava para `countDisciplinas` que é dead code). Este guia referencia `itemMatriculaRepository.countByMatriculaId()` que é o método real usado em `MatriculaServiceImpl.adicionarDisciplina()`.

---

## Deviations from Plan

None — plan executed exactly as written.

---

## Known Stubs

None. Ambos os arquivos são documentação completa, sem placeholders ou TODOs pendentes.

---

## Threat Flags

Nenhum novo surface de segurança introduzido. Apenas arquivos Markdown de documentação pedagógica.

---

## Self-Check: PASSED

- `docs/00-ddd-sem-mudar-arquitetura/guia-leitura-comparativo.md`: FOUND
- `docs/00-ddd-sem-mudar-arquitetura/exercicio-classificacao.md`: FOUND
- Commit 4ded3f2: FOUND (guia)
- Commit a293ad0: FOUND (exercício)
- grep -c "<details>" exercicio-classificacao.md = 10: PASSED
- grep -c "</details>" exercicio-classificacao.md = 10: PASSED (paridade HTML)
- grep -c "Abra|Observe|Compare" guia-leitura-comparativo.md = 14 (>= 5): PASSED
