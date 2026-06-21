---
phase: 05-diagnostico-codigo-com-anti-padroes
reviewed: 2026-06-21T00:00:00Z
depth: standard
files_reviewed: 33
files_reviewed_list:
  - docs/00-ddd-sem-mudar-arquitetura/00-introducao.md
  - docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md
  - docs/00-ddd-sem-mudar-arquitetura/02-entidade-anemica.md
  - docs/00-ddd-sem-mudar-arquitetura/03-service-deus.md
  - docs/00-ddd-sem-mudar-arquitetura/04-duplicacao-regras.md
  - docs/00-ddd-sem-mudar-arquitetura/05-regras-na-interface.md
  - docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md
  - docs/04-material-didatico/ddd-vs-camadas.md
  - docs/04-material-didatico/estrutura-pastas.md
  - docs/04-material-didatico/guia-consulta.md
  - erp-matricula-camadas/pom.xml
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/MatriculaController.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/ErpMatriculaCamadasApplication.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Aluno.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/ItemMatricula.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Turma.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/AlunoRepository.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/ItemMatriculaRepository.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/TurmaRepository.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java
  - erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaService.java
  - erp-matricula-camadas/src/main/resources/application.yml
  - erp-matricula-camadas/src/main/resources/db/migration/V1__schema.sql
  - erp-matricula-camadas/src/main/resources/db/migration/V2__seeds.sql
  - erp-matricula-camadas/src/main/resources/db/migration/V3__adicionar_adicionada_em.sql
  - erp-matricula-camadas/src/main/resources/mapper/AlunoMapper.xml
  - erp-matricula-camadas/src/main/resources/mapper/ItemMatriculaMapper.xml
  - erp-matricula-camadas/src/main/resources/mapper/MatriculaMapper.xml
  - erp-matricula-camadas/src/main/resources/mapper/TurmaMapper.xml
  - erp-matricula-ddd/pom.xml
findings:
  critical: 3
  warning: 5
  info: 4
  total: 12
status: issues_found
---

# Phase 05: Code Review Report

**Reviewed:** 2026-06-21
**Depth:** standard
**Files Reviewed:** 33
**Status:** issues_found

## Summary

Reviewed the full `erp-matricula-camadas` module (Java sources, XML mappers, SQL migrations, application config) and the documentation set (`docs/00-ddd-sem-mudar-arquitetura/` and `docs/04-material-didatico/`). The intentional anti-patterns (anemic entities, god service, DB-coupling, rules-in-controller, rule duplication) are working as designed and are not flagged here.

Three actual bugs were found — two of which will cause incorrect runtime behavior, and one that causes a silent data integrity inconsistency. Five warnings cover dead code and schema type inconsistencies that degrade correctness or maintainability. Four info items cover documentation accuracy issues where code examples shown to students diverge from the real implementation.

---

## Critical Issues

### CR-01: `turma` fetched but never used — useless query on every `matricular()` call

**File:** `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java:80-82`

**Issue:** `turmaRepository.findById(turmaId)` is called and its result assigned to `turma`, but the `turma` variable is never read after that. The only purpose of this fetch is to throw an exception if the turma is not found — but even that purpose is pedagogically incoherent, because the code immediately below the fetch validates `periodoInicio` from the method parameter (not from `turma`). The turma's own period data is completely ignored.

In practice, every call to `matricular()` issues a `SELECT` against `turmas` for no functional reason. Additionally, the declared intent in the comment ("No módulo DDD: turma.periodoEstaAberto()") implies turma data should be used, which suggests the period check was meant to be `turma.getPeriodoInicio()` and `turma.getPeriodoFim()` — but it checks the raw String parameter instead. This is a logic error: the validation reads from caller-supplied data rather than DB-persisted data, making it trivially bypassable via direct Service calls.

**Fix:** Either (a) remove the `turma` fetch entirely if the intent is to demonstrate that the period check is missing, or (b) use `turma.getPeriodoInicio()` / `turma.getPeriodoFim()` in the validation below it. Option (a) matches the declared anti-pattern documentation; option (b) makes the code functionally complete. Given the pedagogical context, option (a) is cleaner — and a comment noting the absent validation is fine. The unused import of `Turma` should then also be removed.

```java
// Remove lines 80-82:
// Turma turma = turmaRepository.findById(turmaId)
//     .orElseThrow(() -> new RuntimeException("Turma não encontrada: " + turmaId));

// If you want to demonstrate a missing period check, add a comment:
// ANTI-PADRAO (DIAG-01): Turma não é validada. periodoEstaAberto() deveria estar em Turma.
```

---

### CR-02: `MatriculaRepository.countDisciplinas()` is dead code — doc points students to wrong method

**File:** `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java:44` and `erp-matricula-camadas/src/main/resources/mapper/MatriculaMapper.xml:103-105`

**Issue:** The documentation `06-acoplamento-banco.md` explicitly tells students to look at `matriculaRepository.countDisciplinas()` as the canonical example of the "DB-coupling" anti-pattern, showing the following code:

```java
// (from 06-acoplamento-banco.md line 51)
int quantidadeAtual = matriculaRepository.countDisciplinas(matriculaId);
```

However, neither `MatriculaServiceImpl` nor `DisciplinaServiceImpl` ever calls `matriculaRepository.countDisciplinas()`. Both services call `itemMatriculaRepository.countByMatriculaId()` instead. The method `MatriculaRepository.countDisciplinas()` exists in the interface and has a SQL mapping in `MatriculaMapper.xml`, but is never invoked from any application code.

This is a direct factual error in the documentation: a student who follows the docs and greps for `countDisciplinas` in service code will find zero hits, undermining the pedagogical point. Additionally, two parallel count methods doing the same thing in two different repositories (`MatriculaRepository.countDisciplinas` and `ItemMatriculaRepository.countByMatriculaId`) creates confusion about which one is canonical.

**Fix:** Either (a) update `MatriculaServiceImpl.adicionarDisciplina()` (line 149) to call `matriculaRepository.countDisciplinas()` instead of `itemMatriculaRepository.countByMatriculaId()`, and update `DisciplinaServiceImpl.adicionarDisciplinaExtra()` (line 89) similarly; then remove the duplicate `countByMatriculaId` from `ItemMatriculaRepository`; or (b) do the reverse: remove `MatriculaRepository.countDisciplinas()` and its XML mapping, and update the documentation to reference `itemMatriculaRepository.countByMatriculaId()`. Option (a) is preferable because it matches the doc narrative exactly.

---

### CR-03: `item.setId(UUID.randomUUID())` is silently discarded — in-memory object has wrong ID after insert

**File:** `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java:156` and `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java:95`

**Issue:** Both `MatriculaServiceImpl.adicionarDisciplina()` and `DisciplinaServiceImpl.adicionarDisciplinaExtra()` call `item.setId(UUID.randomUUID())` before inserting. However, the `ItemMatriculaMapper.xml` INSERT statement (lines 41-47) omits the `id` column entirely:

```xml
INSERT INTO itens_matricula (matricula_id, disciplina, adicionada_em)
VALUES (#{matriculaId, jdbcType=OTHER}, #{disciplina}, NOW())
```

The schema defines `id UUID PRIMARY KEY DEFAULT gen_random_uuid()`, so PostgreSQL generates a different UUID for the row. The `item` object in memory holds a UUID that does not match the actual DB row. If any code downstream calls `item.getId()` after the insert, it returns a UUID that refers to no existing row.

Currently no code reads `item.getId()` after insert, so this doesn't cause an observable crash in the current codebase. However, it is a latent data integrity bug: a developer adding a feature that uses the returned ID (e.g., responding with item location) will silently get incorrect data.

**Fix:** Either include the `id` column in the INSERT (making Java the authoritative source), or remove `item.setId(UUID.randomUUID())` and let the DB generate the ID:

```xml
<!-- Option A: include id in INSERT -->
INSERT INTO itens_matricula (id, matricula_id, disciplina, adicionada_em)
VALUES (#{id, jdbcType=OTHER}, #{matriculaId, jdbcType=OTHER}, #{disciplina}, NOW())
```

```java
// Option B: remove the setId call (DB generates it; item.getId() returns null after insert)
// item.setId(UUID.randomUUID());  // remove this line
```

Option A is preferable because it makes the Java code the source of truth for IDs, which is consistent with how `MatriculaServiceImpl.matricular()` handles `matricula.setId(UUID.randomUUID())` + explicit `#{id}` in the INSERT.

---

## Warnings

### WR-01: `DisciplinaServiceImpl` is registered as `@Service` but never injected anywhere — unreachable code

**File:** `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java:34`

**Issue:** `DisciplinaServiceImpl` bears `@Service` and will be instantiated by Spring on startup. Its `adicionarDisciplinaExtra()` method is never called by any controller or other service in the module. The only reference to `DisciplinaServiceImpl` by name is in comments within `MatriculaServiceImpl`. The class is dead code from a runtime perspective.

This matters beyond aesthetics: the class instantiates three repository dependencies and registers them with the Spring context. A student running the application and examining bean graphs will see this service active, which could lead to the false conclusion that there is a second endpoint for adding discipline extras. The documentation in `04-duplicacao-regras.md` implies the duplication is "live" (it occurs "when new features are developed"), but as implemented the duplicated code path is unreachable.

**Fix:** Either (a) expose `adicionarDisciplinaExtra` via a controller endpoint (making the duplication live and demonstrable), or (b) remove `@Service` and annotate the class with a comment explaining it's a code-only illustration of the pattern:

```java
// @Service  -- removido: esta classe demonstra duplicação de regras mas não é exposta via HTTP.
// Para uma demonstração ativa, seria necessário um endpoint adicional no Controller.
public class DisciplinaServiceImpl {
```

---

### WR-02: `V3__adicionar_adicionada_em.sql` uses `TIMESTAMP` instead of `TIMESTAMPTZ` — type inconsistency across schema

**File:** `erp-matricula-camadas/src/main/resources/db/migration/V3__adicionar_adicionada_em.sql:22`

**Issue:** V3 adds column `adicionada_em` as `TIMESTAMP NOT NULL DEFAULT NOW()`. Every other timestamp column in V1 uses `TIMESTAMPTZ` (`criado_em`, `criada_em`, `cancelada_em`, `concluida_em`, `incluida_em`). `TIMESTAMP` stores no timezone information; `TIMESTAMPTZ` stores UTC and converts on retrieval. Using plain `TIMESTAMP` for `adicionada_em` while all peers are `TIMESTAMPTZ` creates inconsistent behavior when the application server or database server runs in a non-UTC timezone: `adicionada_em` will be stored as local time, while all other timestamps are stored as UTC.

**Fix:**

```sql
ALTER TABLE itens_matricula
    ADD COLUMN adicionada_em TIMESTAMPTZ NOT NULL DEFAULT NOW();
```

---

### WR-03: `type-aliases-package` points to `repository` package instead of `model` package — misleading config

**File:** `erp-matricula-camadas/src/main/resources/application.yml:37`

**Issue:** `mybatis.type-aliases-package` is set to `br.com.escola.matricula.repository`. MyBatis type aliases are registered for the classes in that package so they can be referenced by short name in XML mappers instead of fully-qualified class names. However, the repository package contains only `@Mapper` interfaces — not the model classes. All XML result maps and `parameterType` attributes use fully-qualified names like `br.com.escola.matricula.model.Matricula`, so the alias registration has no effect on anything.

The model classes (`Aluno`, `Matricula`, `ItemMatricula`, `Turma`) are in `br.com.escola.matricula.model`. If a developer attempts to use short names in a new mapper entry (e.g., `resultType="Matricula"`), it will fail with a `TypeException` because the alias was registered for the repository package, not the model package. This makes `type-aliases-package` a trap.

**Fix:**

```yaml
mybatis:
  type-aliases-package: br.com.escola.matricula.model
```

---

### WR-04: `verificarElegibilidade()` private method in `MatriculaServiceImpl` is never called — dead private method

**File:** `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java:215-225`

**Issue:** `private void verificarElegibilidade(UUID alunoId)` is declared and documented (including a `@throws` Javadoc) but never invoked. Neither `matricular()` nor `adicionarDisciplina()` delegates to it; both inline their own aluno-active checks. The method is unreachable from any call path. Its existence as dead code undermines the pedagogical point it is meant to illustrate: the comment says the method was "created with good intent but developers ignore it," but the evidence for that claim is itself the method being unused — which is correct — yet the method also produces no observable behavior, so students running the application cannot see the "ignored method" effect in action.

**Fix:** The method should be marked in a way that makes the dead-code status unmistakably intentional to students. A compile-time comment or `@SuppressWarnings` with an explanation is better than silent dead code:

```java
// ANTI-PADRAO: Dead Code (consequência de DIAG-01 + DIAG-04)
// Este método foi criado para centralizar a verificação de elegibilidade,
// mas matricular() e adicionarDisciplina() não o usam — cada um verifica inline.
// Resultado: método com boa intenção que ninguém chama. Acontece frequentemente
// em codebases sem modelo de domínio rico.
@SuppressWarnings("unused")
private void verificarElegibilidade(UUID alunoId) {
```

---

### WR-05: `MatriculaController` `@ExceptionHandler(RuntimeException.class)` leaks internal error messages to HTTP clients

**File:** `erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/MatriculaController.java:193-196`

**Issue:** Every `RuntimeException` thrown anywhere during request processing is caught here and its `getMessage()` is returned verbatim in the HTTP response body. Exception messages in this codebase include internal identifiers (`"Aluno não encontrado: " + alunoId`, `"Turma não encontrada: " + turmaId`). These UUIDs are internal system identifiers and their exposure in error responses is an information disclosure that helps enumerate valid/invalid resource IDs.

The Javadoc comment on the method itself says "conveniente para desenvolvimento, inadequado para produção" — the code is self-aware of the problem but the leak is still present. For a module that will be run by students against a real PostgreSQL database, this is a functional security gap rather than merely a style issue.

**Fix:** Map exception messages to safe responses. At minimum, distinguish between "business validation failures" (safe to return) and "infrastructure failures" (should return a generic message):

```java
@ExceptionHandler(RuntimeException.class)
public ResponseEntity<Map<String, String>> tratarRuntimeException(RuntimeException e) {
    // NOTA: Em produção, não retornar e.getMessage() diretamente — vaza detalhes internos.
    // Para este módulo didático, usar apenas em desenvolvimento (ver application.yml dev profile).
    String mensagem = e.getMessage() != null ? e.getMessage() : "Erro interno";
    return ResponseEntity.status(400).body(Map.of("erro", mensagem));
}
```

The real fix for production would be to use typed exceptions mapped to safe messages in a `@ControllerAdvice`.

---

## Info

### IN-01: `01-service-anemico.md` code example references method `existsByAlunoIdAndPeriodo` that does not exist

**File:** `docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md:36-38`

**Issue:** The code snippet in `01-service-anemico.md` shows:

```java
boolean jaExiste = matriculaRepository.existsByAlunoIdAndPeriodo(alunoId, periodoInicio);
if (jaExiste) {
    throw new RuntimeException("Aluno já matriculado neste período");
}
```

No such method exists in `MatriculaRepository.java` or `MatriculaMapper.xml`. The actual service (`MatriculaServiceImpl.matricular()`) performs no duplicate-matricula check at all. A student who reads this documentation and then looks at the real code will be confused and may think the method was accidentally deleted.

**Fix:** Remove the `existsByAlunoIdAndPeriodo` fragment from the example, or add the method to `MatriculaRepository` and its SQL mapping to make the code match the documentation.

---

### IN-02: `05-regras-na-interface.md` code example uses `LocalDate.isBefore()` but real code uses `String.startsWith("199")`

**File:** `docs/00-ddd-sem-mudar-arquitetura/05-regras-na-interface.md:29-31`

**Issue:** The documentation shows:

```java
if (request.periodoInicio().isBefore(LocalDate.now().minusMonths(6))) {
    return ResponseEntity.badRequest().body("Período muito antigo para matrícula");
}
```

The actual implementation uses:

```java
if (request.periodoInicio().startsWith("199")) {
    return ResponseEntity.badRequest()
        .body("Período muito antigo: matrículas anteriores a 2000 não são aceitas. ...");
}
```

The doc implies `periodoInicio` is typed as `LocalDate`, but it is a `String` in the actual `MatricularRequest` record. These two snippets demonstrate different things: the doc shows a relative-date check (semantically richer), while the real code shows a hardcoded string prefix check (simpler but more fragile). The real implementation may actually be a better example of the anti-pattern (magic string check vs. proper date comparison), but as-written it contradicts the documentation without explanation.

**Fix:** Update the code example in `05-regras-na-interface.md` to match the actual implementation, or align the implementation to match the doc.

---

### IN-03: `03-service-deus.md` lists `limparMatriculasAntigas()` as a method in `MatriculaServiceImpl` but it does not exist

**File:** `docs/00-ddd-sem-mudar-arquitetura/03-service-deus.md:24`

**Issue:** The documentation lists the following as a method in `MatriculaServiceImpl`:

```
//   limparMatriculasAntigas()— 20 linhas: operação batch de limpeza
```

This method does not exist in `MatriculaServiceImpl.java`. The comment also says "200+ linhas" for the class, but the actual implementation is ~228 lines including blank lines and Javadoc comments — plausibly ~200 lines of actual code. The absent `limparMatriculasAntigas()` means the "Service Deus grows without bound" narrative is partially supported by code that doesn't exist.

**Fix:** Either add a stub `limparMatriculasAntigas()` to `MatriculaServiceImpl` to make the documentation accurate, or remove it from the list in `03-service-deus.md`.

---

### IN-04: `06-acoplamento-banco.md` references `MatriculaRepository.countDisciplinas()` but actual service uses `ItemMatriculaRepository.countByMatriculaId()`

**File:** `docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md:51-52`

**Issue:** This is the documentation side of CR-02. The documentation says:

```java
int quantidadeAtual = matriculaRepository.countDisciplinas(matriculaId);
```

But both services call `itemMatriculaRepository.countByMatriculaId(matriculaId)`. The canonical `countDisciplinas` in `MatriculaRepository` is dead code. When a student follows the documentation trail — "read Matricula.java (no rule), read MatriculaServiceImpl.adicionarDisciplina() (finds countDisciplinas), trace to mapper XML" — the second step fails because the service calls a different method in a different repository.

**Fix:** Align code and documentation as described in CR-02.

---

_Reviewed: 2026-06-21_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
