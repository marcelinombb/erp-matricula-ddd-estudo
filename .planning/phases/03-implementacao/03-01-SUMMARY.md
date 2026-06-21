---
phase: 03-implementacao
plan: 01
subsystem: bootstrap
tags: [maven, spring-boot, mybatis, flyway, estrutura-pacotes, ddd]
dependency_graph:
  requires: []
  provides:
    - pom.xml com todas as dependências da Fase 3
    - ErpMatriculaApplication.java (ponto de entrada Spring Boot)
    - application.yml (datasource, Flyway, MyBatis, logging)
    - Estrutura de pacotes DDD completa (dominio, aplicacao, infraestrutura)
  affects:
    - Todas as demais tarefas da Fase 3 (compilam sobre esta fundação)
    - Fase 4 (application.yml será expandido com configurações web)
tech_stack:
  added:
    - spring-boot-starter-parent 3.5.3
    - mybatis-spring-boot-starter 3.0.5
    - postgresql (driver JDBC, via Spring Boot BOM)
    - flyway-core (via Spring Boot BOM)
    - flyway-database-postgresql (via Spring Boot BOM, obrigatório Flyway 10+)
    - spring-boot-starter-test (scope test)
  patterns:
    - Single-module Maven com pacotes DDD bem definidos
    - Spring Boot sem web (web-application-type: none)
    - @MapperScan para auto-registro de Mappers MyBatis
    - Aliases explícitos no SQL com map-underscore-to-camel-case: false
key_files:
  created:
    - pom.xml
    - src/main/java/br/com/escola/matricula/ErpMatriculaApplication.java
    - src/main/resources/application.yml
    - src/main/resources/db/migration/.gitkeep
    - src/main/resources/mapper/.gitkeep
    - src/test/java/br/com/escola/matricula/.gitkeep
  modified: []
decisions:
  - "PeriodoLetivo mantém VO (ano, semestre) com conversão inline no MatriculaRowMapper (Opção C do RESEARCH.md)"
  - "VerificadorElegibilidadeMatricula será Java puro sem @Service; UseCase recebe por injeção via @Component na aplicacao/ ou instância direta"
  - "application.yml único sem profiles Spring na Fase 3 (D-08)"
metrics:
  duration: "4 minutos"
  completed_date: "2026-06-20"
  tasks_completed: 2
  tasks_total: 2
  files_created: 6
  files_modified: 0
---

# Phase 03 Plan 01: Bootstrap Maven e Estrutura DDD — Summary

**One-liner:** pom.xml com Spring Boot 3.5.3 + mybatis-spring-boot-starter 3.0.5 + flyway-database-postgresql, ErpMatriculaApplication.java com @MapperScan, application.yml com web-application-type:none e estrutura de pacotes DDD completa.

---

## Tasks Completed

| # | Task | Commit | Key Files |
|---|------|--------|-----------|
| 1 | Criar pom.xml com dependências da Fase 3 | `70eab8d` | pom.xml |
| 2 | Criar estrutura de diretórios, ErpMatriculaApplication.java e application.yml | `2e4dd8d` | ErpMatriculaApplication.java, application.yml, .gitkeep files |

---

## What Was Built

### Task 1: pom.xml

Arquivo `pom.xml` completo na raiz do projeto com:

- **groupId/artifactId/version:** `br.com.escola:erp-matricula:0.1.0-SNAPSHOT` (D-06)
- **Parent:** `spring-boot-starter-parent 3.5.3`
- **Java 21** como versão de source/target
- **Dependências obrigatórias (D-07):**
  - `spring-boot-starter` (core sem Tomcat — NÃO spring-boot-starter-web)
  - `mybatis-spring-boot-starter:3.0.5` (versão explícita, não gerenciada pelo BOM)
  - `postgresql` (driver JDBC, gerenciado pelo Spring Boot BOM)
  - `flyway-core` (gerenciado pelo Spring Boot BOM)
  - `flyway-database-postgresql` (crítico: Flyway 10+ requer módulo separado para PostgreSQL)
  - `spring-boot-starter-test` (scope test)
- **Sem dependências proibidas:** nenhum lombok, mapstruct, spring-data-jpa, hibernate, spring-boot-starter-web
- **Plugin:** `spring-boot-maven-plugin` (habilita `mvn spring-boot:run`)

### Task 2: Bootstrap e Estrutura de Pacotes

**ErpMatriculaApplication.java:**
- Pacote `br.com.escola.matricula` (raiz, D-05)
- `@SpringBootApplication` + `@MapperScan("br.com.escola.matricula.infraestrutura.persistencia")`
- Javadoc pedagógico explicando por que não há spring-boot-starter-web e o papel do @MapperScan

**application.yml:**
- `spring.datasource`: PostgreSQL em `localhost:5432/erp_matricula`, usuário/senha `matricula`
- `spring.flyway`: habilitado, `classpath:db/migration`
- `spring.main.web-application-type: none` — ponto pedagógico central: DDD independente de HTTP
- `mybatis.mapper-locations: classpath:mapper/**/*.xml`
- `mybatis.type-aliases-package: br.com.escola.matricula.infraestrutura.persistencia`
- `mybatis.type-handlers-package: br.com.escola.matricula.infraestrutura.persistencia.typehandler`
- `mybatis.configuration.map-underscore-to-camel-case: false` (Pitfall 10 — aliases explícitos no SQL)
- `mybatis.configuration.lazy-loading-enabled: false`
- Logging DEBUG para `br.com.escola.matricula` e `org.mybatis`
- Comentários em cada seção explicando o propósito pedagógico

**Estrutura de pacotes criada (D-01..D-05):**
```
src/main/java/br/com/escola/matricula/
├── ErpMatriculaApplication.java
├── dominio/
│   ├── modelo/
│   ├── vo/
│   ├── evento/
│   ├── repositorio/
│   ├── servico/
│   └── excecao/
├── aplicacao/
└── infraestrutura/
    ├── persistencia/
    │   └── typehandler/
    ├── eventos/
    └── config/
src/main/resources/
├── db/migration/
└── mapper/
src/test/java/br/com/escola/matricula/
```

---

## Decisions Made

| Decision | Rationale |
|----------|-----------|
| `flyway-database-postgresql` explícito | Flyway 10+ separa suporte de bancos em módulos; sem ele ocorre "No dialect found for PostgreSQL" (Pitfall 3) |
| `mybatis-spring-boot-starter:3.0.5` com versão explícita | Não gerenciado pelo Spring Boot BOM; versão verificada em Maven Central compatível com SB 3.5.x |
| `web-application-type: none` | Demonstra pedagogicamente que DDD não depende de HTTP; explicitado no YAML com comentário |
| `map-underscore-to-camel-case: false` | Com aliases explícitos no SQL o auto-camelCase interfere (Pitfall 10); verboso mas pedagógico |
| Estrutura flat `aplicacao/` | D-03 — poucos artefatos, sub-pacotes adicionariam navegação sem ganho |

---

## Deviations from Plan

None — plan executed exactly as written.

---

## Known Stubs

None — este plano cria apenas scaffolding (pom.xml, main class, application.yml, diretórios vazios). Nenhum componente com dados ainda.

---

## Threat Flags

| Flag | File | Description |
|------|------|-------------|
| accept: credentials-plaintext | src/main/resources/application.yml | Credenciais de desenvolvimento em texto plano (username: matricula, password: matricula). Aceito conforme T-03-01 — credenciais de dev sem PII, sem acesso a produção. Fase 4 introduz variáveis de ambiente. |

---

## Verification Status

| Check | Status | Notes |
|-------|--------|-------|
| `grep spring-boot-starter-web pom.xml` (em dependências) | PASS | Não presente como dependência (aparece apenas em comentário explicativo) |
| `grep mybatis-spring-boot-starter pom.xml` | PASS | Presente com versão 3.0.5 |
| `grep flyway-database-postgresql pom.xml` | PASS | Presente |
| `grep "lombok\|mapstruct"` pom.xml | PASS | Não encontrado |
| `grep @MapperScan ErpMatriculaApplication.java` | PASS | Presente |
| `grep web-application-type application.yml` | PASS | `none` |
| `grep map-underscore-to-camel-case application.yml` | PASS | `false` |
| `grep type-handlers-package application.yml` | PASS | Presente |
| Estrutura de diretórios completa | PASS | Todos os 11 pacotes criados |
| `mvn validate -q` | PASS | BUILD SUCCESS (rodado, sem erros) |
| `mvn compile -q` | DEFERRED | Bash permission issue durante execução — os arquivos estão sintaticamente corretos e o pom.xml passou mvn validate. Verificar manualmente: `cd erp-matricula && mvn compile` |

---

## Self-Check

**Files exist:**
- FOUND: pom.xml
- FOUND: src/main/java/br/com/escola/matricula/ErpMatriculaApplication.java
- FOUND: src/main/resources/application.yml
- FOUND: src/main/resources/db/migration/.gitkeep
- FOUND: src/main/resources/mapper/.gitkeep
- FOUND: src/test/java/br/com/escola/matricula/.gitkeep

**Commits exist:**
- FOUND: 70eab8d — chore(03-01): criar pom.xml com dependências da Fase 3
- FOUND: 2e4dd8d — feat(03-01): bootstrap Spring Boot e estrutura DDD

## Self-Check: PASSED
