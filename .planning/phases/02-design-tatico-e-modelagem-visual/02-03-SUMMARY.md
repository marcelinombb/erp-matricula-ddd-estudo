---
phase: 02-design-tatico-e-modelagem-visual
plan: "03"
subsystem: docs/02-design-tatico
tags:
  - domain-services
  - domain-events
  - repositorios
  - ddd-tatico
  - pedagogia
dependency_graph:
  requires:
    - "02-01"
    - "02-02"
  provides:
    - docs/02-design-tatico/domain-services.md
    - docs/02-design-tatico/domain-events.md
    - docs/02-design-tatico/repositorios.md
  affects:
    - docs/02-design-tatico/modelagem.md
    - README.md
tech_stack:
  added: []
  patterns:
    - Domain Service como classe Java pura no pacote dominio/ sem anotações de framework
    - Domain Event como Java 21 record imutável com IDs e VOs (nunca objetos mutáveis)
    - Coleta interna de eventos no Aggregate (List<Object> sem Spring) + publicação pelo UseCase
    - Repositório como interface de domínio pura (sem extends JpaRepository)
    - Inversão de dependência entre domínio e infraestrutura via interface de repositório
key_files:
  created:
    - docs/02-design-tatico/domain-services.md
    - docs/02-design-tatico/domain-events.md
    - docs/02-design-tatico/repositorios.md
  modified: []
decisions:
  - "Domain Service VerificadorElegibilidadeMatricula vive em dominio/ como classe Java pura sem anotações Spring"
  - "Domain Events são Java 21 records com apenas IDs e VOs — nunca objetos mutáveis"
  - "Aggregate coleta eventos internamente; UseCase publica após salvar (sem @Autowired ApplicationEventPublisher no domínio)"
  - "MatriculaRepositorio é interface pura no dominio/ sem herdar de JpaRepository — inversão de dependência"
metrics:
  duration: "~8 minutos"
  completed_date: "2026-06-20"
  tasks_completed: 3
  files_created: 3
  files_modified: 0
---

# Phase 02 Plan 03: Domain Services, Domain Events e Repositórios — Summary

Domain services com justificativa de por que a lógica cruza entidades, eventos como records Java 21 com mecanismo de coleta sem Spring, e repositório como interface pura de domínio com cross-reference ao ADR-001.

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Criar domain-services.md | d0585db | docs/02-design-tatico/domain-services.md |
| 2 | Criar domain-events.md | efb849e | docs/02-design-tatico/domain-events.md |
| 3 | Criar repositorios.md | 67ec158 | docs/02-design-tatico/repositorios.md |

## What Was Built

### domain-services.md (TAT-04)

Documenta `VerificadorElegibilidadeMatricula` como o único Domain Service do BC Matrícula. Abertura bottom-up com as três verificações de elegibilidade. Justificativa explícita para cada candidato eliminado (Aluno, Matricula, UseCase). Snippet completo do Domain Service em Java puro sem anotações. Tabela de distinção Domain Service vs Application Service com 6 aspectos. Snippet do UseCase consumindo o Domain Service. Seção Erros Comuns com dois pares ERRADO/CERTO (lógica no UseCase e @Service no domínio).

### domain-events.md (TAT-05)

Documenta os três eventos de domínio do BC Matrícula. Abertura bottom-up com o problema de comunicação entre BCs sem acoplamento. Blockquote de cross-reference obrigatório para context-map.md (Upstream/Downstream). Tabela expandida dos 3 eventos com colunas Gatilho e Campos. Snippet Java 21 de `AlunoMatriculado` como record com comentários `// Java 21:` e `// DDD fit:`. Assinaturas de `DisciplinaAdicionada` e `MatriculaCancelada`. Seção de mecanismo de coleta interna (`coletarEventos()`) sem dependência do Spring. Seção Erros Comuns com `@TransactionalEventListener` e contraste objeto mutável vs IDs imutáveis.

### repositorios.md (TAT-06)

Documenta `MatriculaRepositorio` como interface pura de domínio. Cross-reference obrigatório para ADR-001 antes da abertura. Abertura bottom-up com o problema de anotações JPA no domínio. Snippet da interface com zero imports de framework (4 métodos). Referência à implementação `MatriculaRepositorioMyBatis` na infraestrutura (Fase 3). Verificação bash `grep -r "import org.apache.ibatis"` como prova da separação. Seção de consequências positivas e negativas (trade-offs honestos). Seção Erros Comuns com dois pares ERRADO/CERTO (`extends JpaRepository` e `@Entity` no domínio).

## Deviations from Plan

None — plan executed exactly as written.

## Requirements Coverage

| Requirement | Status | Evidence |
|-------------|--------|----------|
| TAT-04 | Covered | domain-services.md com VerificadorElegibilidadeMatricula, tabela distinção, snippet, Erros Comuns |
| TAT-05 | Covered | domain-events.md com catálogo 3 eventos, mecanismo coleta, cross-reference context-map.md |
| TAT-06 | Covered | repositorios.md com interface pura, ADR-001 cross-reference, grep bash, trade-offs |

## Known Stubs

None — todos os três documentos entregam conteúdo completo conforme definido no plano. Os snippets Java são prospectivos (Fase 3), mas essa é a intenção documentada do plano.

## Threat Flags

None — os três arquivos são documentação Markdown estática sem código executável, endpoints ou dados sensíveis.

## Self-Check: PASSED

- [x] docs/02-design-tatico/domain-services.md existe
- [x] docs/02-design-tatico/domain-events.md existe
- [x] docs/02-design-tatico/repositorios.md existe
- [x] Commits d0585db, efb849e, 67ec158 existem no log
- [x] Todos os três arquivos contêm exatamente uma seção `## Erros Comuns`
- [x] domain-services.md: contém VerificadorElegibilidadeMatricula, tabela Domain Service vs Application Service
- [x] domain-events.md: contém os 3 eventos, blockquote context-map.md, @TransactionalEventListener
- [x] repositorios.md: contém blockquote ADR-001 no início, grep bash, JpaRepository no ERRADO
