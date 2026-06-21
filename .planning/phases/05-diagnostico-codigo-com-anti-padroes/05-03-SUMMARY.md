---
phase: 05-diagnostico-codigo-com-anti-padroes
plan: "03"
subsystem: database
tags: [mybatis, flyway, postgresql, anti-patterns, mapper, migrations]

# Dependency graph
requires:
  - phase: 05-02
    provides: "Modelos anêmicos Matricula, Aluno, Turma, ItemMatricula no pacote model"
provides:
  - "Quatro interfaces @Mapper MyBatis no pacote br.com.escola.matricula.repository"
  - "MatriculaRepository.java com countDisciplinas expondo DIAG-06"
  - "ItemMatriculaRepository.java com countByMatriculaId expondo DIAG-06"
  - "Quatro XMLs de mapeamento em src/main/resources/mapper/"
  - "Três migrations Flyway V1-V3 copiadas em erp-matricula-camadas/src/main/resources/db/migration/"
affects:
  - "05-04: MatriculaServiceImpl usa MatriculaRepository.countDisciplinas para DIAG-06"

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "@Mapper como repositório direto (sem separação domain interface vs. infrastructure)"
    - "resultMap com mapeamento explícito coluna->campo (map-underscore-to-camel-case=false)"
    - "jdbcType=OTHER em parâmetros UUID para PostgreSQL nativo"
    - "Migrations Flyway copiadas para banco separado erp_matricula_camadas"

key-files:
  created:
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/AlunoRepository.java
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/TurmaRepository.java
    - erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/ItemMatriculaRepository.java
    - erp-matricula-camadas/src/main/resources/mapper/MatriculaMapper.xml
    - erp-matricula-camadas/src/main/resources/mapper/AlunoMapper.xml
    - erp-matricula-camadas/src/main/resources/mapper/TurmaMapper.xml
    - erp-matricula-camadas/src/main/resources/mapper/ItemMatriculaMapper.xml
    - erp-matricula-camadas/src/main/resources/db/migration/V1__schema.sql
    - erp-matricula-camadas/src/main/resources/db/migration/V2__seeds.sql
    - erp-matricula-camadas/src/main/resources/db/migration/V3__adicionar_adicionada_em.sql
  modified: []

key-decisions:
  - "Usar jdbcType=OTHER em todos os parâmetros UUID para compatibilidade com PostgreSQL nativo"
  - "Mapeamento coluna 'nome' da tabela turmas para campo 'codigo' em Turma.java demonstra desalinhamento schema/modelo"
  - "type-aliases-package aponta para o pacote repository — XMLs usam FQN do model para evitar ambiguidade"

patterns-established:
  - "ANTI-PADRAO DIAG-06: countDisciplinas em MatriculaRepository expõe regra de negócio como SQL"
  - "ANTI-PADRAO DIAG-06: countByMatriculaId em ItemMatriculaRepository idem"
  - "@Mapper IS o repositório — sem MatriculaRepositorio (domínio) + MatriculaMapper (infra) separados"
  - "resultMap direto para entidade de domínio — sem MatriculaRow intermediária (contraste com DDD)"

requirements-completed:
  - DIAG-06
  - DID-01

# Metrics
duration: 25min
completed: "2026-06-21"
---

# Phase 05 Plan 03: Repositório e Migrations — Summary

**Quatro interfaces @Mapper MyBatis com countDisciplinas/countByMatriculaId expondo DIAG-06, quatro XMLs de mapeamento, e três migrations Flyway copiadas para banco separado erp_matricula_camadas**

## Performance

- **Duration:** 25 min
- **Started:** 2026-06-21T22:30:00Z
- **Completed:** 2026-06-21T22:55:33Z
- **Tasks:** 2
- **Files created:** 11

## Accomplishments

- Quatro interfaces @Mapper no pacote `br.com.escola.matricula.repository` — sem separação domain/infrastructure (contraste pedagógico com o módulo DDD)
- `MatriculaRepository.countDisciplinas` e `ItemMatriculaRepository.countByMatriculaId` com comentários ANTI-PADRAO DIAG-06 explicitando onde a regra "máximo 6 disciplinas" vive (no banco, não no domínio)
- Quatro XMLs MyBatis com resultMap explícito (map-underscore-to-camel-case=false), jdbcType=OTHER para UUID, e comentários pedagógicos sobre o anti-padrão
- Três migrations Flyway copiadas identicamente do módulo DDD para banco separado `erp_matricula_camadas` — demonstra que o schema é o mesmo; o que muda é o código Java
- Módulo `erp-matricula-camadas` compila com Java 21, exit code 0

## Task Commits

1. **Task 1: Criar as quatro interfaces @Mapper e copiar as migrations Flyway** - `cd63828` (feat)
2. **Task 2: Criar os XMLs de mapeamento MyBatis** - `4fa1815` (feat)

## Files Created

- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java` — @Mapper com countDisciplinas (DIAG-06)
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/AlunoRepository.java` — @Mapper com findById e findAll
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/TurmaRepository.java` — @Mapper com findById
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/ItemMatriculaRepository.java` — @Mapper com countByMatriculaId (DIAG-06)
- `erp-matricula-camadas/src/main/resources/mapper/MatriculaMapper.xml` — resultMap, 5 queries, countDisciplinas com comentário DIAG-06
- `erp-matricula-camadas/src/main/resources/mapper/AlunoMapper.xml` — resultMap, findById, findAll
- `erp-matricula-camadas/src/main/resources/mapper/TurmaMapper.xml` — TurmaResultMap, findById (coluna nome mapeada para campo codigo)
- `erp-matricula-camadas/src/main/resources/mapper/ItemMatriculaMapper.xml` — resultMap, findByMatriculaId, insert, countByMatriculaId
- `erp-matricula-camadas/src/main/resources/db/migration/V1__schema.sql` — schema idêntico ao módulo DDD
- `erp-matricula-camadas/src/main/resources/db/migration/V2__seeds.sql` — seeds idênticos ao módulo DDD
- `erp-matricula-camadas/src/main/resources/db/migration/V3__adicionar_adicionada_em.sql` — migration idêntica ao módulo DDD

## Decisions Made

- `type-aliases-package` em `application.yml` aponta para `br.com.escola.matricula.repository`, mas os XMLs usam FQN (`br.com.escola.matricula.model.Matricula`) para evitar ambiguidade — modelo de domínio não está no pacote de aliases
- A coluna `nome` da tabela `turmas` é mapeada para o campo `codigo` em `Turma.java` via resultMap explícito — reflete inconsistência intencional entre schema e modelo anêmico
- `jdbcType=OTHER` em todos os parâmetros UUID nos XMLs — necessário para PostgreSQL nativo (sem isso: "ERROR: operator does not exist: uuid = character varying")

## Deviations from Plan

None — plano executado exatamente como especificado.

## Issues Encountered

- `mvn` não estava no PATH; localizado em `/home/marcelino/.m2/wrapper/dists/apache-maven-3.9.14/db91789b/bin/mvn` e executado com `JAVA_HOME=/home/marcelino/.jdks/ms-21.0.10` (Java 21 necessário para compilar — o sistema tem Java 17 como padrão)

## Threat Surface Scan

T-05-02 (SQL injection via MyBatis): todos os parâmetros de usuário nos XMLs usam `#{param}` (prepared statements). Nenhum uso de `${param}` (interpolação direta). Ameaça mitigada conforme o threat model do plano.

## Known Stubs

Nenhum — este plano cria interfaces e XMLs de mapeamento sem dados estáticos ou placeholders.

## Next Phase Readiness

- `MatriculaRepository`, `AlunoRepository`, `TurmaRepository`, `ItemMatriculaRepository` disponíveis para o Plano 04 (`MatriculaServiceImpl`)
- `countDisciplinas` e `countByMatriculaId` prontos para demonstrar DIAG-06 no Service (Plano 04)
- Migrations V1-V3 prontas para aplicação no banco `erp_matricula_camadas`

---
*Phase: 05-diagnostico-codigo-com-anti-padroes*
*Completed: 2026-06-21*

## Self-Check: PASSED

Files verified:
- erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java: FOUND
- erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/ItemMatriculaRepository.java: FOUND
- erp-matricula-camadas/src/main/resources/mapper/MatriculaMapper.xml: FOUND
- erp-matricula-camadas/src/main/resources/mapper/ItemMatriculaMapper.xml: FOUND
- erp-matricula-camadas/src/main/resources/db/migration/V1__schema.sql: FOUND
- erp-matricula-camadas/src/main/resources/db/migration/V2__seeds.sql: FOUND
- erp-matricula-camadas/src/main/resources/db/migration/V3__adicionar_adicionada_em.sql: FOUND

Commits verified:
- cd63828: feat(05-03): criar interfaces @Mapper e copiar migrations Flyway — FOUND
- 4fa1815: feat(05-03): criar XMLs de mapeamento MyBatis com countDisciplinas (DIAG-06) — FOUND
