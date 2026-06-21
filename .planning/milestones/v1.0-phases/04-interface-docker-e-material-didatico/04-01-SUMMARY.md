---
phase: 04-interface-docker-e-material-didatico
plan: "01"
subsystem: infraestrutura-web
tags:
  - spring-boot-web
  - flyway
  - mybatis
  - dto
  - demo-runner
dependency_graph:
  requires:
    - "03-implementacao (MatriculaMapper.xml, ItemMatriculaRow, MatriculaRowMapper)"
  provides:
    - "Servidor HTTP ativo na porta 8080 (Fase 4 Controllers dependem disto)"
    - "V3__adicionar_adicionada_em.sql (campo adicionada_em em itens_matricula)"
    - "MatriculaDto.ItemDto com adicionadaEm preparado para leitura com timestamp"
  affects:
    - "erp-matricula-app/pom.xml (dependências web ativadas)"
    - "erp-matricula-app/src/main/resources/application.yml (web-application-type removido)"
tech_stack:
  added:
    - "spring-boot-starter-web (Tomcat embutido, porta 8080, Jackson JSON)"
    - "spring-boot-starter-validation (Bean Validation JSR-380 para Controllers)"
  patterns:
    - "Flyway DEFAULT NOW() para retrocompatibilidade com seeds existentes"
    - "DTO com campo nullable para timestamp de camada de persistência (adicionadaEm=null via factory method)"
    - "DemoRunner protegido com try/catch para startup idempotente"
key_files:
  created:
    - "erp-matricula-app/src/main/resources/db/migration/V3__adicionar_adicionada_em.sql"
  modified:
    - "erp-matricula-app/pom.xml"
    - "erp-matricula-app/src/main/resources/application.yml"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/ItemMatriculaRow.java"
    - "erp-matricula-app/src/main/resources/mapper/MatriculaMapper.xml"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatriculaDto.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/config/DemoRunner.java"
decisions:
  - "adicionadaEm=null no MatriculaDto.de(Matricula): o modelo de domínio ItemMatricula não carrega timestamp — dado de persistência fica em ItemMatriculaRow; será populado quando Controllers adicionarem leitura via Mapper"
  - "DEFAULT NOW() em V3: retrocompatível com seeds V2 sem alterar INSERT inserirItens no Mapper"
  - "try/catch em DemoRunner.run(): protege startup idempotente sem @ConditionalOnProperty — abordagem mais simples e pedagógica para Fase 4"
metrics:
  duration: "4m"
  completed: "2026-06-21"
  tasks_completed: 3
  files_modified: 6
---

# Phase 4 Plan 01: Ativar Camada Web e Preparar Infraestrutura de Dados — Summary

**One-liner:** Ativação de spring-boot-starter-web + Flyway V3 adicionando adicionada_em + MatriculaDto.ItemDto com timestamp preparado para Controllers REST.

## What Was Built

Este plano ativa a fundação necessária para todos os planos subsequentes da Fase 4:

1. **Dependências web ativadas** — `spring-boot-starter-web` e `spring-boot-starter-validation` adicionados ao `pom.xml`. `web-application-type: none` removido do `application.yml`. A aplicação agora sobe como servidor HTTP na porta 8080 (default Spring Boot).

2. **Flyway V3** — `V3__adicionar_adicionada_em.sql` adiciona coluna `adicionada_em TIMESTAMP NOT NULL DEFAULT NOW()` na tabela `itens_matricula`. O `DEFAULT NOW()` garante retrocompatibilidade total com os seeds V2 sem alterar o `INSERT inserirItens` no Mapper.

3. **Modelo relacional atualizado** — `ItemMatriculaRow.java` recebe campo `public LocalDateTime adicionadaEm`. `MatriculaMapper.xml` recebe alias `i.adicionada_em AS item_adicionada_em` nos SELECTs `buscarPorId` e `buscarPorAluno`, e `<result property="adicionadaEm" column="item_adicionada_em">` no ResultMap da collection.

4. **MatriculaDto com ItemDto** — Record interno `ItemDto(String nome, LocalDateTime adicionadaEm)` adicionado. Campo `List<ItemDto> disciplinas` adicionado ao record principal. O factory method `de(Matricula)` mapeia as disciplinas do Aggregate com `adicionadaEm=null` (domínio não carrega timestamp — design intencional documentado).

5. **DemoRunner idempotente** — Corpo do método `run()` envolvido em `try/catch(Exception e)` com `log.warn`. Segunda execução da aplicação não derruba o startup quando o índice único `uq_matricula_aluno_periodo_ativa` já existe.

## Tasks Completed

| Task | Description | Commit |
|------|-------------|--------|
| 1 | Ativar dependências web no pom.xml e remover web-application-type | `02e1d1e` |
| 2 | Flyway V3 e atualização do modelo relacional (ItemMatriculaRow + MatriculaMapper.xml) | `5030aa0` |
| 3 | Atualizar MatriculaDto com ItemDto, MatriculaRowMapper e proteger DemoRunner | `6da0652` |

## Deviations from Plan

None — plan executed exactly as written.

## Known Stubs

| Stub | File | Line | Reason |
|------|------|------|--------|
| `adicionadaEm = null` em `ItemDto` via factory method `de(Matricula)` | `MatriculaDto.java` | ~72 | Intencional e documentado: o modelo de domínio `ItemMatricula` não carrega timestamp de persistência. Campo preparado para quando Controllers adicionarem leitura direta via `MatriculaMapper` (Plano 04-02 ou posterior). Não impede o objetivo do plano — Controllers REST não dependem de `adicionadaEm` para os 3 fluxos POST deste plano. |

## Self-Check: PASSED
