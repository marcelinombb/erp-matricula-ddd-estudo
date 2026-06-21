# Phase 1: Design Estrategico - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-20
**Phase:** 1-Design Estrategico
**Areas discussed:** Estrutura de arquivos, Profundidade do Glossário, Context Map — nível de detalhe, ESTR-06 vs ADRs formais

---

## Estrutura de Arquivos

### Pergunta 1: Localização dos documentos

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| docs/ separada por fase | docs/01-design-estrategico/ com arquivos por tópico. README.md como mapa. | ✓ |
| Raiz do projeto | GLOSSARIO.md, CONTEXT-MAP.md direto na raiz. Mistura com pom.xml depois. | |
| README.md único e longo | Tudo em seções do README.md principal. | |

**Escolha:** `docs/` separada por fase.

---

### Pergunta 2: Relação README ↔ docs/

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| README como mapa de navegação | Visão geral + links. Sem conteúdo duplicado. | ✓ |
| README com sumário executivo | ESTR-01 e ESTR-03 resumidos inline no README. | |
| README mínimo | Só nome, stack e como rodar. Todo DDD em docs/. | |

**Escolha:** README como mapa de navegação.

---

### Pergunta 3: Nomenclatura de arquivos

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Descritivo em português | problema-negocio.md, linguagem-ubiqua.md, etc. | ✓ |
| Numerado + descritivo | 01-problema-negocio.md, 02-linguagem-ubiqua.md | |
| Mapeado por ESTR-XX | estr-01-problema-negocio.md | |

**Escolha:** Descritivo em português.

---

## Profundidade do Glossário

### Pergunta 1: Estrutura de cada entrada

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Tabela: termo + definição + BC dono | Formato enxuto e escaneável. | ✓ |
| Tabela extendida + exemplos de uso | Adiciona exemplos em frases de negócio. | |
| Narrativa por conceito | Parágrafo por termo. Máxima riqueza, mínima escaneabilidade. | |

**Escolha:** Tabela com colunas: Termo | Definição | BC Dono | Não usar.

---

### Pergunta 2: Conceitos ambíguos entre contextos

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Seção separada "Conceitos Ambíguos" | Seção após a tabela mostrando Aluno/Matrícula em cada BC. | ✓ |
| Inline, na coluna "BC dono" | Ambiguidade mostrada na tabela. | |
| Não, manter simples | Ambiguidade fica no doc de Bounded Contexts. | |

**Escolha:** Seção separada "Conceitos Ambíguos" após a tabela principal.

---

### Pergunta 3: Anti-exemplos em inglês

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Coluna "Não usar" na tabela | StudentEntity, RegistrationDTO etc. ao lado de cada termo. | ✓ |
| Nota de rodapé nos termos ambíguos | Só para os mais confusos. | |
| Não incluir | Anti-exemplos ficam na Fase 4 (DID-01). | |

**Escolha:** Coluna "Não usar" na tabela principal.

---

## Context Map — Nível de Detalhe

### Pergunta 1: Padrões DDD no diagrama

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Padrões DDD rotulados | Customer-Supplier, OHS, Published Language, ACL. | ✓ |
| Simples: caixas + setas + eventos | Apenas fluxo de eventos, sem padrões. | |
| Dois diagramas: simples + anotado | Visão progressiva; mais trabalho. | |

**Escolha:** Diagrama com padrões DDD rotulados.

---

### Pergunta 2: Quais subdomínios incluir

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Todos os 4, Secretaria isolada | Secretaria aparece mas sem conexão de eventos. | ✓ |
| Apenas os que se comunicam | Matrícula, Financeiro, Acadêmico. Secretaria omitida. | |
| Todos com notação de escopo | Financeiro/Acadêmico marcados como "stub (v1)". | |

**Escolha:** Todos os 4, Secretaria como contexto isolado.

---

### Pergunta 3: Quais eventos no diagrama

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Apenas AlunoMatriculado | Evento principal cross-context. | |
| Todos os 3 eventos | AlunoMatriculado, DisciplinaAdicionada, MatriculaCancelada. | ✓ |
| Eventos agrupados | Uma seta "Eventos de Matrícula" com 3 nomes. | |

**Escolha:** Todos os 3 eventos que cruzam fronteiras.

---

## ESTR-06 vs ADRs Formais

### Pergunta 1: Como documentar decisões arquiteturais

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| ADRs formais já na Fase 1 | ADR-001 a ADR-004 completos agora. DID-02..05 entregues aqui. | ✓ |
| Documento leve agora, ADR formal na Fase 4 | Tabela resumida em ESTR-06; ADRs completos na Fase 4. | |
| Inline no doc de Bounded Contexts | Seção "Decisões" no final do bounded-contexts.md. | |

**Escolha:** Escrever os ADRs formais já na Fase 1.

---

### Pergunta 2: Template dos ADRs

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Template ADR clássico (Nygard) | Status / Contexto / Decisão / Consequências. | ✓ |
| Template didático customizado | Problema / Alternativas / Decisão / Por que não as outras. | |
| Formato livre | Cada ADR com estrutura própria. | |

**Escolha:** Template ADR clássico (Michael Nygard).

---

### Pergunta 3: Localização dos ADRs

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| docs/adrs/ compartilhada | Pasta de projeto, não de fase. Padrão de mercado. | ✓ |
| docs/01-design-estrategico/adrs/ | Junto com docs da Fase 1. | |
| docs/01-design-estrategico/ diretamente | Sem subpasta. | |

**Escolha:** `docs/adrs/` compartilhada por todas as fases.

---

## Claude's Discretion

- Profundidade de cada documento (número de frases por definição, nível de detalhamento das consequências nos ADRs).
- Sintaxe Mermaid exata (flowchart LR vs graph TD) — o que produzir melhor renderização no GitHub.

## Deferred Ideas

Nenhuma — a discussão se manteve dentro do escopo da Fase 1.
