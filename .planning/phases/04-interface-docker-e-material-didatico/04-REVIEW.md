---
phase: 04-interface-docker-e-material-didatico
reviewed: 2026-06-21T00:00:00Z
depth: standard
files_reviewed: 17
files_reviewed_list:
  - docs/04-material-didatico/ddd-vs-camadas.md
  - docs/04-material-didatico/estrutura-pastas.md
  - docs/04-material-didatico/guia-consulta.md
  - docs/04-material-didatico/licoes-aprendidas.md
  - docs/adrs/ADR-001-mybatis-vs-jpa.md
  - docs/adrs/ADR-002-escopo-bounded-context.md
  - docs/adrs/ADR-003-referencia-por-id.md
  - docs/adrs/ADR-004-codigo-em-portugues.md
  - erp-matricula-app/pom.xml
  - erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatriculaDto.java
  - erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/config/DemoRunner.java
  - erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/ItemMatriculaRow.java
  - erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/ExcecaoHandler.java
  - erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java
  - erp-matricula-app/src/main/resources/application.yml
  - erp-matricula-app/src/main/resources/db/migration/V3__adicionar_adicionada_em.sql
  - erp-matricula-app/src/main/resources/mapper/MatriculaMapper.xml
findings:
  critical: 3
  warning: 4
  info: 3
  total: 10
status: issues_found
---

# Phase 04: Code Review Report

**Reviewed:** 2026-06-21
**Depth:** standard
**Files Reviewed:** 17
**Status:** issues_found

## Summary

This phase delivers the HTTP interface layer (REST controllers, exception handler), an updated MyBatis mapper with the new `adicionada_em` column, and four documentation files. The DDD layering is sound — the domain has no framework imports, exception mapping is centralized, and the mapper XML follows the established patterns correctly.

Three blockers were found: the `Turma` placeholder in the controller is constructed with `vagasMaximas=0`, which throws `IllegalArgumentException` on every POST /matriculas call, making the endpoint completely broken at runtime. The `periodoFim` field received from the HTTP request is silently discarded, creating a contract mismatch. The `inserirItens` SQL statement does not include the `adicionada_em` column, which is `NOT NULL` in the schema after V3, making every discipline insert fail with a constraint violation.

Four warnings cover silent exception swallowing in `DemoRunner`, a fixable return type in `ExcecaoHandler.handleValidacao`, a wrong package path in an ADR code snippet, and public mutable fields in `ItemMatriculaRow` that undermine the pedagogical goal of the material.

---

## Critical Issues

### CR-01: `Turma` placeholder constructed with `vagasMaximas=0` — throws on every POST /matriculas

**File:** `erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java:146-151`

**Issue:** `Turma.java` line 51 enforces `if (vagasMaximas <= 0) throw new IllegalArgumentException(...)`. The controller constructs the placeholder with `vagasMaximas=0`, so every invocation of `POST /matriculas` throws `IllegalArgumentException` before reaching the use case. This exception is not mapped in `ExcecaoHandler` and falls through to the generic 500 handler, silently breaking the endpoint.

The comment says "nome e capacidade não relevantes para o UseCase de matrícula" — true that `VerificadorElegibilidadeMatricula` never reads `vagasMaximas`, but the `Turma` constructor guards against it regardless.

**Fix:**
```java
// Use any positive integer — 1 is the minimal valid value.
// The value is unused by the domain service but the constructor guard must be satisfied.
var turma = new Turma(
        new TurmaId(UUID.fromString(request.turmaId())),
        "N/A",
        periodo,
        1   // placeholder — capacity not relevant for this use case
);
```

---

### CR-02: `periodoFim` accepted from HTTP request but silently discarded — contract mismatch

**File:** `erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java:80-85,129-134`

**Issue:** `MatricularAlunoRequest` declares `periodoFim` as a required `@NotNull` field. The javadoc on line 71 says "A data de fim do período é obrigatória". However, `periodoFim` is never read anywhere in the `matricular()` method body — `PeriodoLetivo` is derived solely from `periodoInicio.getYear()` and the month split. The caller is required to supply a value that is then thrown away.

This is both a contract lie (the API documentation implies the value matters) and a silent data loss: if the caller intentionally supplies a `periodoFim` that does not match the derived semestre boundary, the discrepancy is ignored. This could also introduce confusion: someone passing `periodoInicio=2026-08-01` with `periodoFim=2026-07-31` (crossing semester) gets no error.

**Fix:** Either remove `periodoFim` from the request record entirely (if the server always derives it from the month), or actually use it to construct `PeriodoLetivo` and validate that `periodoFim` is consistent with the derived semestre, throwing 400 if it is not:

```java
// Option A — remove the field (cleaner)
public record MatricularAlunoRequest(
        @NotNull(message = "O ID do aluno é obrigatório")  String alunoId,
        @NotNull(message = "O ID da turma é obrigatório")  String turmaId,
        @NotNull(message = "A data de início do período é obrigatória") LocalDate periodoInicio
) {}

// Option B — validate it is not contradictory and document that only periodoInicio drives inference
```

---

### CR-03: `inserirItens` XML omits `adicionada_em` column — violates NOT NULL constraint after V3

**File:** `erp-matricula-app/src/main/resources/mapper/MatriculaMapper.xml:185-190`

**Issue:** After `V3__adicionar_adicionada_em.sql` runs, the `itens_matricula.adicionada_em` column is `NOT NULL`. The `inserirItens` statement inserts only `(matricula_id, disciplina)`:

```xml
INSERT INTO itens_matricula (matricula_id, disciplina)
VALUES
<foreach collection="list" item="item" separator=",">
  (#{item.matriculaId, jdbcType=OTHER}, #{item.disciplina})
</foreach>
```

The V3 migration sets `DEFAULT NOW()` on the column, which means PostgreSQL fills in the timestamp automatically if the column is not named in the INSERT list — this works correctly at the database level. However, `ItemMatriculaRow` has an `adicionadaEm` field, and the comment in the XML says V3 was added explicitly so that `MatriculaDto.ItemDto.adicionadaEm` can be populated from the row. The design intent (documented in `ItemMatriculaRow.java` line 27-28 and in `MatriculaDto.java` line 28) is that `adicionadaEm` is populated when reading via the mapper, and the ResultMap already maps `item_adicionada_em` to `adicionadaEm`. This is only consistent if the column gets its value from the DB default.

The issue is that the insert silently relies on the DB DEFAULT without documenting that choice in the XML, and `ItemMatriculaRow.adicionadaEm` is never populated before insert, so any future attempt to pass a server-side timestamp for `adicionada_em` (e.g., to allow backdating for migration) would require both the XML and the row class to change. More critically: if a DBA were to remove or alter the DEFAULT (e.g., set it to `NULL` as default during a schema migration), every insert would silently fail with a constraint violation, and the problem would not be caught at compile time or by any test.

**Fix:** Explicitly include `adicionada_em` in the INSERT with `NOW()` or pass the timestamp from the domain/row to make the dependency explicit and resilient:

```xml
<insert id="inserirItens">
  INSERT INTO itens_matricula (matricula_id, disciplina, adicionada_em)
  VALUES
  <foreach collection="list" item="item" separator=",">
    (#{item.matriculaId, jdbcType=OTHER}, #{item.disciplina}, NOW())
  </foreach>
</insert>
```

This makes the intent explicit and does not rely on an implicit DB DEFAULT.

---

## Warnings

### WR-01: `DemoRunner` swallows all exceptions including startup failures — masks real errors

**File:** `erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/config/DemoRunner.java:79-143`

**Issue:** The entire `run()` body is wrapped in a broad `catch (Exception e)` that logs a single `WARN` line and returns normally. This means any real failure — database connection error, `IllegalArgumentException` from a domain guard, `NullPointerException` from a code bug — is silently downgraded to a warning. The operator sees "fluxos já executados ou erro esperado na re-execução" regardless of what actually went wrong.

In Fase 4 where the DemoRunner is documented as a reference for the full flow, hiding errors removes its utility as a smoke test. A developer running the app for the first time who has an incorrect schema or wrong seed UUIDs will see the warning and assume re-execution, not a bug.

**Fix:** Catch only the specific idempotency-related exceptions (e.g., `DataIntegrityViolationException` for the unique index violation on re-run). Let all other exceptions propagate so the startup fails loudly:

```java
} catch (org.springframework.dao.DataIntegrityViolationException e) {
    log.warn("DemoRunner: matrícula já existe (re-execução detectada) — {}", e.getMessage());
} // All other exceptions propagate and fail startup
```

---

### WR-02: `handleValidacao` uses `Collectors.toList()` instead of `.toList()` — inconsistent with Java 21 style used everywhere else

**File:** `erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/ExcecaoHandler.java:230-235`

**Issue:** Line 235 uses `Collectors.toList()` (which returns a mutable `ArrayList`) while the rest of the codebase consistently uses Java 16+ `.toList()` (which returns an unmodifiable list). The `Collectors` import on line 20 exists solely for this one call. This is inconsistent with the project's deliberate use of Java 21 features (records, pattern matching, `.toList()`) shown throughout all other files.

**Fix:**
```java
List<CampoErro> camposComErro = e.getBindingResult().getFieldErrors().stream()
        .map((FieldError fieldError) -> new CampoErro(
                fieldError.getField(),
                fieldError.getDefaultMessage()
        ))
        .toList();
```
Remove the `import java.util.stream.Collectors;` line as well.

---

### WR-03: Wrong package path in ADR-001 verification command — grep will always return zero results, misleading students

**File:** `docs/adrs/ADR-001-mybatis-vs-jpa.md:89`

**Issue:** The verification command shown to students is:

```bash
grep -r "import org.apache.ibatis" src/main/java/br/com/erp/dominio/
```

The actual package path is `src/main/java/br/com/escola/matricula/dominio/`. The path `br/com/erp/dominio/` does not exist. Running the command as written always returns zero results — not because the architecture is correct, but because the directory does not exist. A student verifying the claim gets a false confirmation.

**Fix:** Correct the path in the ADR:
```bash
grep -r "import org.apache.ibatis" src/main/java/br/com/escola/matricula/dominio/
# Resultado esperado: nenhuma ocorrência
```

---

### WR-04: `ItemMatriculaRow` uses public mutable fields — contradicts pedagogical message about encapsulation

**File:** `erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/ItemMatriculaRow.java:19-29`

**Issue:** All three fields (`matriculaId`, `disciplina`, `adicionadaEm`) are `public` with no encapsulation. The Javadoc correctly states this class is the relational mirror of the domain record `ItemMatricula`, which is declared as an immutable record. The documentation in `estrutura-pastas.md` and `licoes-aprendidas.md` emphasizes the contrast between the mutable Row class and the immutable domain model. However, `ItemMatricula` is a Java record — truly immutable, no setters, value semantics — while `ItemMatriculaRow` has fully public mutable fields.

The distinction is not that Row classes should be immutable (MyBatis needs to populate them), but that they should at least use package-private or getter/setter access rather than raw public fields. As written, any code anywhere in the application can directly mutate `row.disciplina = null` without any visibility. This undercuts the encapsulation lesson the project is teaching.

`MatriculaRow.java` (not in this review scope but observed via cross-reference) uses the same pattern; the issue is consistent. The fix for Row classes is to use package-private fields and expose them via getters, or document why public fields are intentional here.

**Fix:** Either document explicitly in the Javadoc why public fields are chosen (MyBatis field injection requirement), or switch to package-private with getters — MyBatis resolves properties via getter methods by default when fields are not public:

```java
// Option: package-private fields (MyBatis uses getter-based access by default)
UUID matriculaId;
String disciplina;
LocalDateTime adicionadaEm;
```

---

## Info

### IN-01: `@NotNull` on `String alunoId`/`turmaId` does not prevent blank strings — validation gap

**File:** `erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java:74-79`

**Issue:** `@NotNull` on a `String` field rejects JSON `null` but accepts `""` (empty string) or `"not-a-uuid"`. When an empty or malformed UUID string is passed, `UUID.fromString(request.alunoId())` throws `IllegalArgumentException`, which is not mapped in `ExcecaoHandler`, so it becomes a 500. The intent is clearly to validate the format. Using `@NotBlank` (already used for `AdicionarDisciplinaRequest.nome`) would at least catch empty strings; UUID format validation would require a custom constraint or pattern.

**Fix:** Use `@NotBlank` instead of `@NotNull` for String UUID fields. For full correctness, add a `@Pattern(regexp = "^[0-9a-fA-F]{8}-...")` or catch `IllegalArgumentException` from `UUID.fromString` in the handler.

---

### IN-02: `adicionarDisciplina` endpoint returns hardcoded `"ATIVA"` and `totalDisciplinas=1` — misleading response for Concluida status

**File:** `erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java:188-195`

**Issue:** The response DTO for `POST /{id}/disciplinas` is hard-constructed with `statusDescricao = "ATIVA"` and `totalDisciplinas = 1`. Neither value is read from the actual aggregate state after the operation. If the matrícula is already in `Concluida` state, the domain will throw `MatriculaCanceladaException` (actually, only `Cancelada` state throws — `Concluida` state in `adicionarDisciplina()` is not guarded, which is a separate domain concern). More practically, a matrícula that already had 2 disciplines and gets a third will return `totalDisciplinas=1`, which is incorrect. This is documented as a known limitation in the Javadoc comment, but the inaccuracy will confuse students using this endpoint as a reference implementation.

**Fix:** Either load the aggregate after the operation (adds a read query but produces correct data), or document clearly in the response Javadoc that this is a partial confirmation response, not the full aggregate state. Consider returning 204 No Content to avoid implying the DTO is authoritative.

---

### IN-03: `application.yml` hardcodes database credentials in plaintext — acceptable for dev, should be flagged for production promotion

**File:** `erp-matricula-app/src/main/resources/application.yml:17-19`

**Issue:** `username: matricula` and `password: matricula` are committed to source control as literal values. The comment on line 13 says "Fase 4 introduzirá variáveis de ambiente via Docker Compose" — this suggests environment-variable substitution was planned but not implemented in this file. The delivered `application.yml` still contains the hardcoded credentials with no reference to `${DB_PASSWORD}` or similar Spring property placeholders.

For a didactic project this is low severity (credentials are obviously for development only), but the documentation promise was not fulfilled in this phase.

**Fix:** Use Spring property placeholders to allow override at deployment time:
```yaml
datasource:
  url: ${DB_URL:jdbc:postgresql://localhost:5432/erp_matricula}
  username: ${DB_USERNAME:matricula}
  password: ${DB_PASSWORD:matricula}
```

---

_Reviewed: 2026-06-21_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
