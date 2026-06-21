---
phase: 05-diagnostico-codigo-com-anti-padroes
plan: 02
subsystem: domain-model
tags: [java, spring-boot, mybatis, anti-patterns, anemic-entity, ddd-contrast]

# Dependency graph
requires:
  - 05-01 (parent POM multi-module e estrutura de diretórios raiz)
provides:
  - erp-matricula-camadas/pom.xml (módulo Maven herdando de erp-matricula-parent)
  - erp-matricula-camadas/.../ErpMatriculaCamadasApplication.java (main class com @MapperScan)
  - erp-matricula-camadas/src/main/resources/application.yml (banco erp_matricula_camadas)
  - erp-matricula-camadas/.../model/Matricula.java (entidade anêmica DIAG-02 + DIAG-06)
  - erp-matricula-camadas/.../model/Aluno.java (entidade anêmica DIAG-02)
  - erp-matricula-camadas/.../model/Turma.java (entidade anêmica DIAG-02)
  - erp-matricula-camadas/.../model/ItemMatricula.java (entidade anêmica DIAG-02 + DIAG-06)
affects:
  - 05-03 (service layer usa esses models)
  - 05-04 (repository layer usa esses models)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Entidade Anêmica (DIAG-02): classe Java com apenas campos + getters + setters, sem comportamento de domínio"
    - "Acoplamento ao Banco (DIAG-06): campos nomeados como colunas (alunoId, adicionadaEm), tipos primitivos em vez de Value Objects"
    - "@MapperScan no pacote repository (vs. infraestrutura.persistencia no módulo DDD)"

key-files:
  created:
    - erp-matricula-camadas/pom.xml
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/ErpMatriculaCamadasApplication.java
    - erp-matricula-camadas/src/main/resources/application.yml
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Aluno.java
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Turma.java
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/ItemMatricula.java
  modified: []

key-decisions:
  - "Comentários de contraste em cada model class referenciam o equivalente DDD — o desenvolvedor lê Matricula.java e vê exatamente o que a versão anêmica não faz comparada ao modelo rico"
  - "Campos de Matricula.java nomeados como alunoId/turmaId (1:1 com colunas DB) e status como String — demonstram DIAG-06 na própria estrutura de dados"
  - "isAtivo() em vez de estaAtivo() em Aluno.java — nome Bean-convention vs. nome de domínio evidencia a diferença intencional"

# Metrics
duration: 3min
completed: 2026-06-21
---

# Phase 05 Plan 02: Esqueleto do Módulo Camadas — Models Anêmicos Summary

**pom.xml + main class + application.yml + 4 model classes anêmicas (DIAG-02 + DIAG-06) para o módulo erp-matricula-camadas**

## Performance

- **Duration:** 3 min
- **Started:** 2026-06-21T22:37:36Z
- **Completed:** 2026-06-21T22:41:24Z
- **Tasks:** 2
- **Files modified:** 7 (todos criados, nenhum modificado)

## Accomplishments

- Criado erp-matricula-camadas/pom.xml herdando de erp-matricula-parent com mybatis-spring-boot-starter 3.0.5 explícito
- Criada estrutura de diretórios completa: model, controller, service, repository, resources/db/migration, resources/mapper, test
- Criada ErpMatriculaCamadasApplication.java com @MapperScan("br.com.escola.matricula.repository")
- Criado application.yml apontando para banco erp_matricula_camadas (evita conflito Flyway do módulo DDD)
- Criadas 4 model classes anêmicas com comentários pedagógicos de contraste:
  - Matricula.java: String status (vs. StatusMatricula sealed interface), UUID alunoId (vs. AlunoId VO), sem adicionarDisciplina/cancelar
  - Aluno.java: isAtivo() getter (vs. estaAtivo() de domínio), sem desativar()
  - Turma.java: String periodoInicio/periodoFim, sem periodoEstaAberto()
  - ItemMatricula.java: String disciplina (vs. NomeDisciplina VO), mutável

## Task Commits

Each task was committed atomically:

1. **Task 1: Criar pom.xml, ErpMatriculaCamadasApplication.java e application.yml** — `be3d8cf` (feat)
2. **Task 2: Criar quatro model classes anêmicas (DIAG-02 + DIAG-06)** — `56aa8f3` (feat)

## Files Created/Modified

- `erp-matricula-camadas/pom.xml` — parent=erp-matricula-parent, mybatis-spring-boot-starter 3.0.5 explícito, spring-boot-maven-plugin sem skip
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/ErpMatriculaCamadasApplication.java` — @MapperScan("br.com.escola.matricula.repository")
- `erp-matricula-camadas/src/main/resources/application.yml` — datasource erp_matricula_camadas, map-underscore-to-camel-case: false
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java` — entidade anêmica DIAG-02 + DIAG-06, sem comportamento
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Aluno.java` — entidade anêmica DIAG-02, isAtivo() getter
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Turma.java` — entidade anêmica DIAG-02, sem periodoEstaAberto()
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/ItemMatricula.java` — entidade anêmica DIAG-02 + DIAG-06, String disciplina mutável

## Decisions Made

- Comentários de anti-padrão seguem o formato obrigatório definido em 05-PATTERNS.md: `// ANTI-PADRAO: <Nome> (<ID>) / <Consequência> / Contraste: <caminho DDD>`
- Cada model class referencia o equivalente no módulo DDD pelo path completo — permite ao desenvolvedor navegar diretamente ao contraste
- isAtivo() em Aluno (Bean convention) vs. estaAtivo() no DDD — a diferença de nomes não é acidental; foi preservada para evidenciar a diferença de semântica
- Maven não disponível no host — verificação de compilação feita por análise estrutural (balanço de chaves, imports corretos, declarações de package). O Plan 01 SUMMARY documenta o mesmo approach. Build real ocorre no Docker.

## Deviations from Plan

None — plan executed exactly as written.

## Issues Encountered

- Maven not available on host (consistent with Plan 01 behavior). Structural verification performed: all 4 model classes have balanced braces and correct imports. The Docker-based build will verify at runtime.

## Known Stubs

None — all model classes are complete data carriers with proper getters and setters. No placeholder code; the intentional absence of behavior IS the pedagogical content.

## Threat Flags

None — pure model classes with no I/O, network, or persistence surface. Threat register T-05-02 (mutável setters) accepted per plan: módulo didático onde setters abertos são o ponto pedagógico.

---
*Phase: 05-diagnostico-codigo-com-anti-padroes*
*Completed: 2026-06-21*

## Self-Check: PASSED

All 7 created files verified to exist. Both task commits (be3d8cf, 56aa8f3) verified in git log.
