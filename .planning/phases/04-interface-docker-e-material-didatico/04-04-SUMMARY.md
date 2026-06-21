---
phase: 04-interface-docker-e-material-didatico
plan: "04"
subsystem: material-didatico
tags:
  - documentacao
  - ddd
  - pedagogico
  - adr
dependency_graph:
  requires:
    - "04-01 (infraestrutura web ativa — aplicacao compilando)"
    - "04-02 (MatriculaController e ExcecaoHandler implementados — referenciados no guia)"
    - "04-03 (Docker Compose — README.md links resolvidos)"
  provides:
    - "docs/04-material-didatico/ com 4 arquivos Markdown"
    - "ADR-001..004 enriquecidos com secao Na pratica e links para codigo real"
  affects:
    - "docs/04-material-didatico/ddd-vs-camadas.md (novo)"
    - "docs/04-material-didatico/guia-consulta.md (novo)"
    - "docs/04-material-didatico/licoes-aprendidas.md (novo)"
    - "docs/04-material-didatico/estrutura-pastas.md (novo)"
    - "docs/adrs/ADR-001-mybatis-vs-jpa.md (enriquecido)"
    - "docs/adrs/ADR-002-escopo-bounded-context.md (enriquecido)"
    - "docs/adrs/ADR-003-referencia-por-id.md (enriquecido)"
    - "docs/adrs/ADR-004-codigo-em-portugues.md (enriquecido)"
tech_stack:
  added: []
  patterns:
    - "Documentacao comparativa lado a lado (tradicional vs DDD) com referencias a codigo real"
    - "Tabela de consulta rapida conceito DDD → arquivo Java concreto"
    - "Secao Na pratica em ADRs ligando decisao ao codigo implementado"
key_files:
  created:
    - "docs/04-material-didatico/ddd-vs-camadas.md"
    - "docs/04-material-didatico/guia-consulta.md"
    - "docs/04-material-didatico/licoes-aprendidas.md"
    - "docs/04-material-didatico/estrutura-pastas.md"
  modified:
    - "docs/adrs/ADR-001-mybatis-vs-jpa.md"
    - "docs/adrs/ADR-002-escopo-bounded-context.md"
    - "docs/adrs/ADR-003-referencia-por-id.md"
    - "docs/adrs/ADR-004-codigo-em-portugues.md"
decisions:
  - "ddd-vs-camadas.md usa comparacoes com codigo hipotetico no lado tradicional e referencias a arquivos reais no lado DDD — nenhum exemplo inventado para o lado DDD"
  - "guia-consulta.md usa caminhos relativos navegaveis no GitHub/IDE a partir de docs/"
  - "ADRs enriquecidos com secao ao final (apos Referencias) para nao alterar a estrutura original"
metrics:
  duration: "6m"
  completed: "2026-06-21"
  tasks_completed: 2
  files_modified: 8
---

# Phase 4 Plan 04: Material Didatico — Summary

**One-liner:** Quatro documentos Markdown em docs/04-material-didatico/ (DID-01 a DID-08) e secao "Na pratica" em cada ADR 001-004, ligando cada decisao arquitetural ao codigo Java real implementado nas Fases 3 e 4.

## What Was Built

Este plano entrega o Success Criteria 3 e 4 da Fase 4: o material didatico que transforma o codigo em licao compreensivel.

1. **ddd-vs-camadas.md** (DID-01) — Cinco secoes comparativas: `@Service` → UseCase + Domain Service; `@Entity` → Aggregate Root sem anotacoes; `extends JpaRepository` → Interface de Dominio + Implementacao Separada; `String status` → Sealed Interface com Pattern Matching; `RuntimeException` generica → Excecoes Tipadas com Dados Estruturados. Cada secao cita o arquivo Java real do projeto no lado DDD — nenhum exemplo hipotetico.

2. **guia-consulta.md** (DID-06) — Tabela de 20 linhas mapeando conceito DDD → arquivo Java concreto com caminho navegavel. Cobre todos os padroes taticos (Aggregate Root, Value Object, Domain Event, Interface de Repositorio, Domain Service, Excecao tipada), todos os Application Services (UseCase, Command, DTO), toda a infraestrutura (RepositorioMyBatis, TypeHandler, RowMapper, Row) e toda a camada interfaces (Controller, ExcecaoHandler).

3. **licoes-aprendidas.md** (DID-07) — Analise honesta estruturada em tres secoes: o que DDD resolveu bem (invariantes, excecoes semanticas, separacao dominio/persistencia, Domain Events); o que custou mais do que esperado (construcao de objetos no Controller, verbosidade do MyBatis, boilerplate sem Lombok, dificuldade de acertar Aggregate boundary); o que faríamos diferente em producao (optimistic locking PROD-01, Commands com IDs primitivos, TDD, CQRS simples).

4. **estrutura-pastas.md** (DID-08) — Mapa pedagogico da estrutura de diretorios com bloco de codigo anotado. Para cada pacote: "Por que esta aqui" e "O que voce NUNCA vera aqui" — o constraint negativo e pedagogicamente mais valioso que o positivo. Inclui estrutura de resources/ (Flyway migrations, XMLs MyBatis) e docs/.

5. **ADR-001..004 enriquecidos** — Secao "## Na pratica" adicionada ao final de cada ADR com links para os arquivos Java que demonstram a decisao:
   - ADR-001: MatriculaRow.java + MatriculaRowMapper.java com citacao do Javadoc "UNICO arquivo que conhece tanto MatriculaRow quanto Matricula"
   - ADR-002: FinanceiroEventListener.java + AcademicoEventListener.java com explicacao do @TransactionalEventListener AFTER_COMMIT
   - ADR-003: Matricula.java campos AlunoId/TurmaId + MatriculaController.java placeholder demonstrando por que nao ha repositorio de Aluno no BC Matricula
   - ADR-004: Lista completa de nomes em portugues implementados (MatricularAlunoUseCase, VerificadorElegibilidadeMatricula, adicionarDisciplina(), cancelar(), coletarEventos(), estaAtivo())

## Tasks Completed

| Task | Description | Commit |
|------|-------------|--------|
| 1 | Criar ddd-vs-camadas.md e guia-consulta.md | `9b7fa6e` |
| 2 | Criar licoes-aprendidas.md e estrutura-pastas.md; enriquecer ADRs 001-004 | `d03495a` |

## Deviations from Plan

None — plan executed exactly as written.

## Known Stubs

None — este plano cria apenas documentacao. Nenhum stub de codigo.

## Threat Flags

Nenhuma superficie nova alem do mapeado no `<threat_model>` do plano.

- T-04-12 (links relativos quebrados): verificado — todos os arquivos referenciados nos documentos existem no repositorio. Checagem executada via `ls` dos arquivos Java referenciados no guia-consulta.md.

## Self-Check: PASSED

- [x] docs/04-material-didatico/ contem exatamente 4 arquivos
- [x] ddd-vs-camadas.md contem 5 secoes de comparacao com referencias a arquivos reais
- [x] guia-consulta.md contem tabela com 20 linhas (> 18 requeridas)
- [x] licoes-aprendidas.md contem secoes de beneficios E trade-offs honestos
- [x] estrutura-pastas.md documenta todos os pacotes com restricoes negativas ("nunca")
- [x] ADRs 001-004 contem "## Na pratica" com links para arquivos Java implementados
- [x] Commits 9b7fa6e e d03495a existem no git log
