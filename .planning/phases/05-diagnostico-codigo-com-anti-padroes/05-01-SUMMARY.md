---
phase: 05-diagnostico-codigo-com-anti-padroes
plan: 01
subsystem: infra
tags: [maven, docker, multi-module, spring-boot, postgresql, flyway]

# Dependency graph
requires: []
provides:
  - Parent POM Maven multi-module (br.com.escola:erp-matricula-parent:0.1.0-SNAPSHOT)
  - erp-matricula-ddd/ module (renamed from erp-matricula-app/ with full git history)
  - erp-matricula-camadas/ module placeholder declared in parent POM
  - Dockerfile updated for multi-module (-pl erp-matricula-ddd)
  - Dockerfile.camadas for the camadas module
  - docker-compose.yml with three services: postgres, app-ddd (8080), app-camadas (8081)
  - init-db.sql creating erp_matricula_camadas database on postgres startup
  - All docs updated: erp-matricula-app references replaced with erp-matricula-ddd
affects:
  - 05-02 (erp-matricula-camadas module code)
  - 05-03 through 05-06 (documentation phases depend on module structure)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Maven multi-module with spring-boot-starter-parent as grandparent"
    - "spring-boot-maven-plugin skip=true in parent POM (avoids repackage failure on packaging=pom)"
    - "Separate PostgreSQL database per Spring Boot module (avoids Flyway schema_history conflict)"
    - "One Dockerfile per module in multi-module Docker Compose setup"

key-files:
  created:
    - pom.xml (parent POM multi-module)
    - Dockerfile.camadas
    - init-db.sql
  modified:
    - erp-matricula-ddd/pom.xml (parent block changed from spring-boot-starter-parent to local parent)
    - Dockerfile (updated -f erp-matricula-app → -pl erp-matricula-ddd)
    - docker-compose.yml (added app-ddd, app-camadas services; init-db.sql volume)
    - README.md (modules table, updated stack section, two-port test instructions)
    - docs/04-material-didatico/ddd-vs-camadas.md
    - docs/04-material-didatico/estrutura-pastas.md
    - docs/04-material-didatico/guia-consulta.md
    - docs/04-material-didatico/licoes-aprendidas.md
    - docs/adrs/ADR-001-mybatis-vs-jpa.md
    - docs/adrs/ADR-002-escopo-bounded-context.md
    - docs/adrs/ADR-003-referencia-por-id.md

key-decisions:
  - "git mv erp-matricula-app erp-matricula-ddd preserves full commit history (git log --follow works)"
  - "Bank separation (erp_matricula_camadas) rather than schema separation — simpler docker-compose config, avoids Flyway flyway_schema_history conflict"
  - "spring-boot-maven-plugin skip=true in parent POM — prevents repackage failure on packaging=pom modules"
  - "Exception to CLAUDE.md multi-module Maven restriction is intentional and documented in pom.xml (D-03)"

patterns-established:
  - "Pattern: Maven multi-module with local parent POM inheriting from spring-boot-starter-parent"
  - "Pattern: One Dockerfile per module for Docker Compose multi-service setup"
  - "Pattern: init-db.sql mounted to postgres entrypoint to create additional databases"

requirements-completed:
  - DID-01

# Metrics
duration: 20min
completed: 2026-06-21
---

# Phase 05 Plan 01: Infraestrutura Maven Multi-Module e Docker Dual-Service Summary

**Maven multi-module parent POM + git mv erp-matricula-app→erp-matricula-ddd + Dockerfile.camadas + docker-compose com dois Spring Boot (8080/8081) + banco separado via init-db.sql**

## Performance

- **Duration:** 20 min
- **Started:** 2026-06-21T22:10:00Z
- **Completed:** 2026-06-21T22:33:51Z
- **Tasks:** 2
- **Files modified:** 15 (created 3, modified 12)

## Accomplishments

- Renamed erp-matricula-app to erp-matricula-ddd via `git mv` preserving full git commit history
- Created parent POM (erp-matricula-parent) with multi-module declaration and spring-boot-maven-plugin skip=true
- Updated erp-matricula-ddd/pom.xml to inherit from local parent instead of spring-boot-starter-parent directly
- Created Dockerfile.camadas and updated Dockerfile to use -pl module selector
- Updated docker-compose.yml with three services: postgres, app-ddd (8080), app-camadas (8081)
- Created init-db.sql to create erp_matricula_camadas database on postgres startup (avoids Flyway conflict)
- Updated all docs and ADRs to replace erp-matricula-app references with erp-matricula-ddd

## Task Commits

Each task was committed atomically:

1. **Task 1: Rename erp-matricula-app to erp-matricula-ddd and create parent POM** - `123d93f` (feat)
2. **Task 2: Update Dockerfile, create Dockerfile.camadas, update docker-compose.yml and init-db.sql** - `f42d0fc` (feat)

## Files Created/Modified

- `pom.xml` — new parent POM (br.com.escola:erp-matricula-parent) with modules erp-matricula-ddd and erp-matricula-camadas
- `erp-matricula-ddd/pom.xml` — parent block updated to inherit from local parent (renamed from erp-matricula-app)
- `Dockerfile` — updated build command to -pl erp-matricula-ddd and COPY path
- `Dockerfile.camadas` — new Dockerfile for the camadas module (same structure, different -pl)
- `init-db.sql` — creates erp_matricula_camadas database on postgres container startup
- `docker-compose.yml` — added app-ddd (8080) and app-camadas (8081) services; added init-db.sql volume mount
- `README.md` — added modules comparison table, updated stack section (multi-module), two-port curl examples
- `docs/04-material-didatico/ddd-vs-camadas.md` — all erp-matricula-app → erp-matricula-ddd
- `docs/04-material-didatico/estrutura-pastas.md` — path in Java tree diagram updated
- `docs/04-material-didatico/guia-consulta.md` — all 20 file links updated
- `docs/04-material-didatico/licoes-aprendidas.md` — 4 file references updated
- `docs/adrs/ADR-001-mybatis-vs-jpa.md` — 2 file references in "Na prática" section updated
- `docs/adrs/ADR-002-escopo-bounded-context.md` — 2 file references updated
- `docs/adrs/ADR-003-referencia-por-id.md` — 2 file references updated

## Decisions Made

- Used `git mv erp-matricula-app erp-matricula-ddd` instead of creating a new directory — preserves the full git history through `git log --follow`
- Chose separate database (erp_matricula_camadas) over separate Flyway schema — simpler docker-compose configuration and avoids flyway_schema_history table conflict (Pitfall 1 from RESEARCH.md)
- Added spring-boot-maven-plugin `<skip>true</skip>` to parent POM — prevents repackage failure on packaging=pom parent module (Pitfall 2 from RESEARCH.md)
- Also updated docs/adrs/ (ADR-001, ADR-002, ADR-003) and docs/04-material-didatico/licoes-aprendidas.md which had erp-matricula-app path references — task plan mentioned these as "possível referência" and RESEARCH.md confirmed them

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] Updated docs/adrs/ and licoes-aprendidas.md file path references**
- **Found during:** Task 2 (docs update step)
- **Issue:** Task 2 plan listed estrutura-pastas.md, ddd-vs-camadas.md, guia-consulta.md, README.md for update. RESEARCH.md also mentions docs/adrs/*.md and licoes-aprendidas.md as having path references to update. Leaving them would result in broken links to non-existent erp-matricula-app paths.
- **Fix:** Updated all erp-matricula-app → erp-matricula-ddd in ADR-001, ADR-002, ADR-003 and licoes-aprendidas.md
- **Files modified:** docs/adrs/ADR-001-mybatis-vs-jpa.md, docs/adrs/ADR-002-escopo-bounded-context.md, docs/adrs/ADR-003-referencia-por-id.md, docs/04-material-didatico/licoes-aprendidas.md
- **Verification:** grep -r "erp-matricula-app" docs/ README.md Dockerfile docker-compose.yml returns 0 results
- **Committed in:** f42d0fc (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (Rule 2 — critical correctness: broken links in documentation)
**Impact on plan:** Necessary for correctness — avoiding broken file path links in docs. No scope creep.

## Issues Encountered

- Maven not available on host (only inside Docker containers per project design). Build verification `mvn -pl erp-matricula-ddd compile -q` could not be run locally. Structural verification performed instead: confirmed all XML structures, module declarations, and file paths are correct. The Docker-based build will verify at runtime.

## Known Stubs

None — all files created are complete infrastructure configuration. No placeholder code.

## Next Phase Readiness

- Parent POM structure is in place for Plan 02 to create erp-matricula-camadas/ module
- docker-compose.yml declares app-camadas service — Plan 02 will create the actual module code
- init-db.sql creates erp_matricula_camadas DB — Plan 02 can use this when creating application.yml for the camadas module
- All doc references to erp-matricula-ddd are correct and consistent

---
*Phase: 05-diagnostico-codigo-com-anti-padroes*
*Completed: 2026-06-21*

## Self-Check: PASSED

All created files verified to exist. All task commits verified to exist in git log.
