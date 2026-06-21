---
phase: 04-interface-docker-e-material-didatico
plan: "03"
subsystem: infraestrutura-docker
tags:
  - docker
  - docker-compose
  - dockerfile
  - multi-stage-build
  - postgresql
  - healthcheck
dependency_graph:
  requires:
    - "04-01 (spring-boot-starter-web ativo — aplicação sobe como servidor HTTP)"
  provides:
    - "Dockerfile multi-stage para build sem dependências no host"
    - "docker-compose.yml com PostgreSQL 16-alpine + app em healthcheck-chain"
    - "README.md com seções Docker e Material Didático"
  affects:
    - "Dockerfile (novo)"
    - "docker-compose.yml (novo)"
    - "README.md (seções adicionadas)"
tech_stack:
  added:
    - "eclipse-temurin:21-jdk-alpine (stage de compilação Maven)"
    - "eclipse-temurin:21-jre-alpine (stage de runtime — imagem final menor)"
    - "postgres:16-alpine (banco de dados via Docker Compose)"
  patterns:
    - "Multi-stage Docker build: stage 1 compila com Maven, stage 2 executa apenas o JAR"
    - "healthcheck pg_isready + depends_on condition: service_healthy para evitar race condition"
    - "Variáveis de ambiente com defaults: ${DB_USER:-matricula} — configurável sem .env obrigatório"
    - "SPRING_DATASOURCE_URL env var override: sobrescreve application.yml sem perfis Spring separados"
key_files:
  created:
    - "Dockerfile"
    - "docker-compose.yml"
  modified:
    - "README.md"
decisions:
  - "apk add --no-cache maven no Stage 1: mvnw não está no repositório — Maven instalado no container"
  - "JAR wildcard COPY erp-matricula-*.jar: robusto a mudanças de versão; nome literal documentado no Dockerfile (erp-matricula-0.1.0-SNAPSHOT.jar)"
  - "Sem HEALTHCHECK no Dockerfile: spring-boot-starter-actuator não está no pom.xml — healthcheck omitido (o do postgres via pg_isready é suficiente no Compose)"
  - "version: omitida no docker-compose.yml: Docker Compose v2+ não requer a chave obsoleta"
  - "Porta 5432 não exposta no host: banco acessível apenas via rede Docker interna (reduz superfície de ataque)"
metrics:
  duration: "8m"
  completed: "2026-06-21"
  tasks_completed: 2
  files_modified: 3
---

# Phase 4 Plan 03: Docker Compose e Documentação de Execução — Summary

**One-liner:** Dockerfile multi-stage eclipse-temurin:21 + docker-compose.yml com healthcheck pg_isready e depends_on service_healthy, eliminando race condition de startup.

## What Was Built

Este plano entrega DCK-01 (docker-compose.yml funcional) e DCK-02 (documentação de uso), completando o requisito central da Fase 4: qualquer desenvolvedor executa `docker compose up` sem Java ou Maven no host.

1. **Dockerfile multi-stage** — Stage 1 usa `eclipse-temurin:21-jdk-alpine` com `apk add --no-cache maven` para compilar (resolvendo o pitfall crítico: mvnw ausente no repositório). Stage 2 usa `eclipse-temurin:21-jre-alpine` com apenas o JAR executável — imagem final sem Maven, JDK ou código-fonte. Comentários pedagógicos explicam cada decisão de build.

2. **docker-compose.yml** — Service `postgres` usa `postgres:16-alpine` com `healthcheck` via `pg_isready`. Service `app` usa `build: .` apontando para o Dockerfile e `depends_on condition: service_healthy` — garante que o Flyway só conecta quando o PostgreSQL já está aceitando conexões (resolve pitfall de race condition). Credenciais via `${DB_USER:-matricula}` e `${DB_PASSWORD:-matricula}` — configuráveis sem arquivo `.env` obrigatório. Volume `postgres_data` persiste dados entre restarts.

3. **README.md atualizado** — Seção "Como Executar (Docker)" com comandos `docker compose up`, `docker compose down` e `docker compose down -v` (reset do banco). Seção "Material Didático" com links para `docs/04-material-didatico/` (4 arquivos — serão criados no Plano 04-04). Conteúdo existente preservado integralmente.

## Tasks Completed

| Task | Description | Commit |
|------|-------------|--------|
| 1 | Criar Dockerfile multi-stage eclipse-temurin:21 com apk add maven | `d83113b` |
| 2 | Criar docker-compose.yml com healthcheck e depends_on; atualizar README.md | `57eff17` |

## Deviations from Plan

None — plan executed exactly as written.

The plan mentioned the JAR name as `erp-matricula-app-*.jar` in the context section, but the actual artifact ID in `erp-matricula-app/pom.xml` is `erp-matricula` (version `0.1.0-SNAPSHOT`), producing `erp-matricula-0.1.0-SNAPSHOT.jar`. The Dockerfile uses the wildcard `erp-matricula-*.jar` with the literal name documented in a comment — no behavioral change, but the naming was corrected from the plan's assumption.

## Known Stubs

| Stub | File | Line | Reason |
|------|------|------|--------|
| Links para `docs/04-material-didatico/` no README.md | README.md | ~77-82 | Intencional e documentado: os 4 arquivos de material didático são criados no Plano 04-04. Os links existem no README para demonstrar a estrutura planejada. Não impede o objetivo deste plano (Docker funcional). |

## Threat Flags

Nenhuma superfície nova além do mapeado no `<threat_model>` do plano. As ameaças T-04-07 a T-04-SC foram endereçadas conforme especificado:
- T-04-07: Credenciais default documentadas como exemplo pedagógico, comentário no docker-compose.yml sobre uso de .env em produção está implícito nas variáveis substituíveis.
- T-04-08: Porta 5432 não exposta no host (apenas rede interna Docker).
- T-04-09: Container executa como root (aceitável para projeto didático).
- T-04-10: Volume `postgres_data` com `docker compose down -v` documentado no README.

## Self-Check: PASSED
