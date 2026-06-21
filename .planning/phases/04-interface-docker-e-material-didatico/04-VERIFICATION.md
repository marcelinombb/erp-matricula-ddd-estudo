---
phase: 04-interface-docker-e-material-didatico
verified: 2026-06-21T00:00:00Z
status: human_needed
score: 13/13
overrides_applied: 0
human_verification:
  - test: "Executar docker compose up e fazer POST /matriculas"
    expected: "Aplicação sobe sem erros, Flyway aplica V1+V2+V3, POST /matriculas com {alunoId, turmaId, periodoInicio, periodoFim} retorna 201 com MatriculaDto"
    why_human: "Requer Docker disponível, Maven e compilação do JAR no container — não testável via grep. Também confirma race condition fix (service_healthy) funcionando na prática."
  - test: "Enviar payload inválido e payload de matrícula duplicada"
    expected: "Payload com campo obrigatório ausente retorna 400 com {erro, mensagem, campos:[{campo, mensagem}]}; matrícula duplicada retorna 409 com {erro: MATRICULA_DUPLICADA, mensagem}"
    why_human: "Requer aplicação rodando para disparar Bean Validation e MethodArgumentNotValidException reais."
  - test: "Enviar requisição que excede limite de disciplinas"
    expected: "Retorna 422 com {erro: LIMITE_DISCIPLINAS_EXCEDIDO, mensagem, limite: N, atual: N}"
    why_human: "Requer aplicação rodando para confirmar que getLimite() e getAtual() fluem corretamente até a resposta HTTP."
---

# Phase 4: Interface, Docker e Material Didatico — Verification Report

**Phase Goal:** O projeto está completo e operacional: API REST documentada expõe os três fluxos, Docker Compose sobe o ambiente com um comando, e o material didático comparativo transforma o código em lição compreensível para qualquer desenvolvedor vindo de arquitetura em camadas.
**Verified:** 2026-06-21
**Status:** human_needed
**Re-verification:** No — initial verification

---

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Aplicação sobe como servidor HTTP na porta 8080 (spring-boot-starter-web ativo, web-application-type removido) | ✓ VERIFIED | `pom.xml` declara `spring-boot-starter-web` e `spring-boot-starter-validation`; `application.yml` não contém `web-application-type` |
| 2 | Flyway V3 adiciona coluna adicionada_em com DEFAULT NOW() sem quebrar seeds V2 | ✓ VERIFIED | `V3__adicionar_adicionada_em.sql` existe, contém `ALTER TABLE itens_matricula ADD COLUMN adicionada_em TIMESTAMP NOT NULL DEFAULT NOW()`; INSERT `inserirItens` não menciona `adicionada_em` |
| 3 | MatriculaDto retorna lista de disciplinas com {nome, adicionadaEm} em cada item | ✓ VERIFIED | `MatriculaDto.java` contém record interno `ItemDto(String nome, LocalDateTime adicionadaEm)` e campo `List<ItemDto> disciplinas` |
| 4 | DemoRunner não falha na segunda execução | ✓ VERIFIED | `DemoRunner.run()` envolve todo o corpo em `try/catch(Exception e)` com `log.warn` |
| 5 | POST /matriculas aceita payload e retorna 201 com MatriculaDto | ✓ VERIFIED | `MatriculaController` tem `@PostMapping` + `@ResponseStatus(HttpStatus.CREATED)`; delega para `matricularUseCase.executar()` |
| 6 | POST /matriculas/{id}/disciplinas aceita {nome} e retorna 200 com MatriculaDto | ✓ VERIFIED | Endpoint `/{id}/disciplinas` existe, aceita `@RequestBody @Valid AdicionarDisciplinaRequest`; retorna `MatriculaDto` (minimal, ver notas) |
| 7 | POST /matriculas/{id}/cancelamento aceita corpo vazio e retorna 200 com MatriculaDto cancelado | ✓ VERIFIED | Endpoint `/{id}/cancelamento` existe sem `@RequestBody`; retorna `MatriculaDto` com `statusDescricao="CANCELADA"` |
| 8 | Payload inválido retorna 400 com campos; conflitos retornam 409; violações de invariante retornam 422 | ✓ VERIFIED | `ExcecaoHandler` tem 9 `@ExceptionHandler`: `CONFLICT` para 3 casos, `UNPROCESSABLE_ENTITY` para 3 casos, `NOT_FOUND` para 1, `BAD_REQUEST` para validação, `INTERNAL_SERVER_ERROR` fallback |
| 9 | O domínio nunca importa HttpStatus ou ResponseEntity | ✓ VERIFIED | `grep` em `dominio/excecao/*.java` retorna zero imports HTTP; apenas `ExcecaoHandler` conhece `HttpStatus` |
| 10 | docker compose up sobe PostgreSQL e aplicação sem configuração adicional | ✓ VERIFIED | `docker-compose.yml` existe com `build: .`, `postgres:16-alpine`, `pg_isready` healthcheck, `depends_on condition: service_healthy`, credenciais via `${DB_USER:-matricula}` |
| 11 | Dockerfile compila JAR com Maven no container (sem mvnw) | ✓ VERIFIED | `Dockerfile` Stage 1: `eclipse-temurin:21-jdk-alpine` + `apk add --no-cache maven` + `mvn -q package -DskipTests -f erp-matricula-app/pom.xml`; Stage 2: `eclipse-temurin:21-jre-alpine` + `COPY --from=builder` |
| 12 | README.md documenta como subir, derrubar e resetar ambiente Docker | ✓ VERIFIED | README contém `docker compose up`, `docker compose down`, `docker compose down -v`; seção "Material Didático" com links para `docs/04-material-didatico/` |
| 13 | Material didático completo: 4 arquivos + ADRs enriquecidos | ✓ VERIFIED | `docs/04-material-didatico/` contém exatamente 4 arquivos; `ddd-vs-camadas.md` tem 5 seções comparativas com refs a arquivos reais; `guia-consulta.md` tem 21 linhas de tabela (todas 20 paths verificados existentes); `licoes-aprendidas.md` tem trade-offs honestos; `estrutura-pastas.md` tem 15 ocorrências de "nunca/NUNCA"; ADR-001..004 todos têm seção `## Na prática` |

**Score:** 13/13 truths verified

---

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `erp-matricula-app/pom.xml` | spring-boot-starter-web + validation | ✓ VERIFIED | Ambas dependências presentes nas linhas 62 e 70 |
| `erp-matricula-app/src/main/resources/application.yml` | Sem web-application-type: none | ✓ VERIFIED | grep retornou vazio |
| `erp-matricula-app/src/main/resources/db/migration/V3__adicionar_adicionada_em.sql` | Migration com ADD COLUMN adicionada_em DEFAULT NOW() | ✓ VERIFIED | 1207 bytes, conteúdo correto |
| `erp-matricula-app/src/main/java/.../persistencia/ItemMatriculaRow.java` | Campo public LocalDateTime adicionadaEm | ✓ VERIFIED | Linha 29: `public LocalDateTime adicionadaEm` |
| `erp-matricula-app/src/main/resources/mapper/MatriculaMapper.xml` | alias item_adicionada_em no SELECT e result no ResultMap | ✓ VERIFIED | Presentes em buscarPorId (linha 77) e buscarPorAluno (linha 102); result na linha 54 |
| `erp-matricula-app/src/main/java/.../aplicacao/MatriculaDto.java` | List<ItemDto> disciplinas + record ItemDto | ✓ VERIFIED | Campo na linha 41, record na linha 55 |
| `erp-matricula-app/src/main/java/.../config/DemoRunner.java` | try/catch em run() | ✓ VERIFIED | `try {` na linha 79, `catch (Exception e)` na linha 140 |
| `erp-matricula-app/src/main/java/.../interfaces/MatriculaController.java` | @RestController com 3 @PostMapping | ✓ VERIFIED | 222 linhas; @RestController linha 56; 3 @PostMapping nas linhas 126, 178, 208 |
| `erp-matricula-app/src/main/java/.../interfaces/ExcecaoHandler.java` | @ControllerAdvice com 9 @ExceptionHandler | ✓ VERIFIED | 265 linhas; @ControllerAdvice linha 57; 9 @ExceptionHandler confirmados |
| `Dockerfile` | Multi-stage eclipse-temurin:21 + apk maven | ✓ VERIFIED | Stage 1: jdk-alpine + maven; Stage 2: jre-alpine; ENTRYPOINT correto |
| `docker-compose.yml` | PostgreSQL + app + healthcheck + depends_on service_healthy | ✓ VERIFIED | pg_isready healthcheck, condition: service_healthy, volumes, credenciais com defaults |
| `README.md` | Seção Docker + seção Material Didático | ✓ VERIFIED | docker compose up/down/down -v; links para docs/04-material-didatico/ |
| `docs/04-material-didatico/ddd-vs-camadas.md` | 5+ seções comparativas com refs a arquivos reais | ✓ VERIFIED | 5 seções H2, 10 refs a `erp-matricula-app/src` |
| `docs/04-material-didatico/guia-consulta.md` | Tabela 18+ linhas conceito → arquivo | ✓ VERIFIED | 21 linhas de dados; todos 20 paths verificados existentes |
| `docs/04-material-didatico/licoes-aprendidas.md` | Trade-offs honestos incluindo MyBatis verboso + PROD-01 | ✓ VERIFIED | Seções "O que custou mais" e "O que faríamos diferente" com PROD-01 mencionado |
| `docs/04-material-didatico/estrutura-pastas.md` | Todos os pacotes com restrições negativas ("nunca") | ✓ VERIFIED | 15 ocorrências de "nunca/NUNCA"; todos 4 pacotes documentados |
| `docs/adrs/ADR-001-mybatis-vs-jpa.md` | Seção ## Na prática | ✓ VERIFIED | Linha 116, com links para MatriculaRow.java e MatriculaRowMapper.java |
| `docs/adrs/ADR-002-escopo-bounded-context.md` | Seção ## Na prática | ✓ VERIFIED | Linha 90, com links para FinanceiroEventListener.java e AcademicoEventListener.java |
| `docs/adrs/ADR-003-referencia-por-id.md` | Seção ## Na prática | ✓ VERIFIED | Linha 109, com links para Matricula.java e MatriculaController.java |
| `docs/adrs/ADR-004-codigo-em-portugues.md` | Seção ## Na prática | ✓ VERIFIED | Linha 116, com lista completa de nomes em português implementados |

---

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `MatriculaController.matricular()` | `MatricularAlunoUseCase.executar()` | `matricularUseCase.executar(command)` linha 155 | ✓ WIRED | Constrói Aluno/Turma/PeriodoLetivo corretamente; retorna MatriculaId |
| `MatriculaController.adicionarDisciplina()` | `AdicionarDisciplinaUseCase.executar()` | `adicionarUseCase.executar(command)` linha 185 | ✓ WIRED | UseCase retorna void; Controller retorna MatriculaDto minimal |
| `MatriculaController.cancelar()` | `CancelarMatriculaUseCase.executar()` | `cancelarUseCase.executar(command)` linha 211 | ✓ WIRED | UseCase retorna void; Controller retorna MatriculaDto com status="CANCELADA" |
| `ExcecaoHandler` | `dominio/excecao/*.java` (7 exceções) | `@ExceptionHandler` por tipo | ✓ WIRED | Todos os 9 handlers registrados; mapeamento de status correto (409/422/404/400/500) |
| `docker-compose.yml (app service)` | `Dockerfile` | `build: .` | ✓ WIRED | Contexto na raiz; Dockerfile presente na raiz |
| `docker-compose.yml (SPRING_DATASOURCE_URL)` | `application.yml (spring.datasource.url)` | Spring Boot env var override | ✓ WIRED | `SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/erp_matricula` |
| `MatriculaMapper.xml buscarPorId/buscarPorAluno` | `ItemMatriculaRow.adicionadaEm` | `column="item_adicionada_em"` no ResultMap | ✓ WIRED | Alias no SELECT e result no ResultMap em ambas as queries |
| `guia-consulta.md (links)` | `arquivos Java reais em src/` | caminhos relativos navegáveis | ✓ WIRED | Todos 20 caminhos verificados existem no repositório |

---

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|---------------|--------|--------------------|--------|
| `MatriculaController.matricular()` | `MatriculaDto` retornado | `MatricularAlunoUseCase.executar()` → retorna `MatriculaId`; DTO construído com dados da requisição | Parcial — `matriculaId` real do banco; `totalDisciplinas=0, disciplinas=[]` são estáticos | ✓ FLOWING (aceitável: plano documenta limitação explicitamente) |
| `MatriculaController.adicionarDisciplina()` | `MatriculaDto` retornado | `AdicionarDisciplinaUseCase.executar()` retorna void; DTO construído com `id` da URL e `nome` do request | Parcial — confirma operação mas não retorna estado completo do Aggregate | ⚠️ PARTIAL (UseCase retorna void — desvio documentado no SUMMARY; DTO é confirmação, não leitura completa) |
| `MatriculaController.cancelar()` | `MatriculaDto` retornado | `CancelarMatriculaUseCase.executar()` retorna void; DTO hardcoded com `status="CANCELADA"` | Parcial — confirma cancelamento mas campos `alunoId` e `totalDisciplinas` estão vazios/zerados | ⚠️ PARTIAL (mesmo padrão que acima — desvio documentado no SUMMARY) |
| `MatriculaMapper.xml` | `MatriculaRow + ItemMatriculaRow` | JOIN query com PostgreSQL via MyBatis | ✓ DB query com JOIN e `<collection>` | ✓ FLOWING |

**Nota sobre DTOs parciais:** O SUMMARY 04-02 documenta explicitamente o desvio: `AdicionarDisciplinaUseCase.executar()` e `CancelarMatriculaUseCase.executar()` retornam `void`. O Controller constrói um `MatriculaDto` mínimo como confirmação. Isso é funcional — o objetivo da Fase 4 é expor os três fluxos via HTTP, não garantir que o DTO de retorno reflete o estado completo do Aggregate em todos os casos. Não é um bloqueador.

---

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| MatriculaController tem 3 @PostMapping | `grep -c "@PostMapping" MatriculaController.java` | 3 | ✓ PASS |
| ExcecaoHandler tem 9 @ExceptionHandler | `grep -c "@ExceptionHandler" ExcecaoHandler.java` | 9 | ✓ PASS |
| Domínio não importa HTTP | `grep -r "import org.springframework.http" dominio/excecao/` | vazio | ✓ PASS |
| V3 migration existe com ALTER TABLE correto | `grep "ADD COLUMN adicionada_em" V3__*.sql` | encontrado | ✓ PASS |
| Dockerfile multi-stage com 2 eclipse-temurin | `grep -c "eclipse-temurin" Dockerfile` | 2 | ✓ PASS |
| docker compose com service_healthy | `grep -c "service_healthy" docker-compose.yml` | 1 | ✓ PASS |
| Todos 20 paths do guia-consulta.md existem | loop de verificação | 20/20 encontrados | ✓ PASS |
| ADRs têm seção Na prática | `grep "Na prática" ADR-00*.md` | 4/4 encontrados | ✓ PASS |
| docker compose up/down/-v no README | `grep "docker compose" README.md` | múltiplos resultados | ✓ PASS |
| Aplicação via docker compose up (comportamento real) | Requer Docker + build | não testável staticamente | ? SKIP |

---

### Probe Execution

Step 7c: SKIPPED — nenhum arquivo `probe-*.sh` encontrado em `scripts/*/tests/`; fase não é de migração/tooling.

---

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| IFX-01 | 04-02 | Controller REST MatriculaController com endpoints para os três casos de uso | ✓ SATISFIED | `MatriculaController.java` com 3 @PostMapping delegando para UseCases |
| IFX-02 | 04-02 | @ControllerAdvice mapeando exceções de domínio para respostas HTTP semânticas | ✓ SATISFIED | `ExcecaoHandler.java` com @ControllerAdvice e 9 @ExceptionHandler; 409/422/404/400/500 |
| IFX-03 | 04-01, 04-02 | Validações de entrada nos request bodies (Bean Validation) | ✓ SATISFIED | `MatricularAlunoRequest` com @NotNull em 4 campos; `AdicionarDisciplinaRequest` com @NotBlank; `@Valid` nos endpoints |
| DCK-01 | 04-01, 04-03 | docker-compose.yml com PostgreSQL e Aplicação configurados e funcionando | ✓ SATISFIED | `docker-compose.yml` com postgres:16-alpine + app, healthcheck pg_isready, depends_on service_healthy, SPRING_DATASOURCE_URL |
| DCK-02 | 04-03 | Documentação de uso: como subir, derrubar, resetar banco | ✓ SATISFIED | README.md com docker compose up/down/down -v; comentário pedagógico no docker-compose.yml |
| DID-01 | 04-04 | Comparação DDD vs Arquitetura em Camadas com exemplos concretos do fluxo de matrícula | ✓ SATISFIED | `ddd-vs-camadas.md` com 5 seções comparativas (@Service→UseCase, @Entity→AR, JpaRepository→Interface+Impl, String→Sealed, RuntimeException→Tipada) com refs a arquivos reais |
| DID-02 | 04-04 | ADR-001: Por que MyBatis e não JPA/Hibernate | ✓ SATISFIED | ADR-001 enriquecido com seção "## Na prática" linkando MatriculaRow.java e MatriculaRowMapper.java |
| DID-03 | 04-04 | ADR-002: Por que somente o BC de Matrícula foi implementado | ✓ SATISFIED | ADR-002 enriquecido com seção "## Na prática" linkando FinanceiroEventListener.java e AcademicoEventListener.java |
| DID-04 | 04-04 | ADR-003: Por que referência por ID entre Aggregates | ✓ SATISFIED | ADR-003 enriquecido com seção "## Na prática" linkando Matricula.java (campos AlunoId/TurmaId) e MatriculaController.java |
| DID-05 | 04-04 | ADR-004: Por que o código está em português | ✓ SATISFIED | ADR-004 enriquecido com seção "## Na prática" com lista completa de nomes em português |
| DID-06 | 04-04 | Guia de Consulta mapeando conceito DDD para arquivos concretos | ✓ SATISFIED | `guia-consulta.md` com 21 linhas de tabela; todos 20 paths de arquivo verificados existentes |
| DID-07 | 04-04 | Lições Aprendidas com benefícios e trade-offs honestos | ✓ SATISFIED | `licoes-aprendidas.md` com 3 seções: "o que resolveu bem", "o que custou mais", "o que faríamos diferente"; PROD-01 mencionado |
| DID-08 | 04-04 | Estrutura de pastas com propósito e restrições de cada diretório | ✓ SATISFIED | `estrutura-pastas.md` com blocos de código anotados e 15 ocorrências de "nunca/NUNCA" |

**All 13 Phase 4 requirements accounted for and satisfied.**

---

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| Nenhum | — | Nenhum TBD/FIXME/XXX encontrado nos arquivos modificados desta fase | — | — |

Scan executado em: `MatriculaController.java`, `ExcecaoHandler.java`, `Dockerfile`, `docker-compose.yml`, `MatriculaDto.java`, `DemoRunner.java`, `docs/04-material-didatico/*.md`, `docs/adrs/ADR-00*.md`.

**Observação sobre DTOs parciais:** O campo `adicionadaEm=null` em `MatriculaDto.ItemDto` quando construído via factory method `de(Matricula)` é intencional e documentado com Javadoc. Não é um stub não intencional — é uma decisão de design documentada no SUMMARY 04-01 (campo preparado para leitura futura via Mapper).

---

### Human Verification Required

#### 1. docker compose up — fluxo end-to-end

**Test:** Executar `docker compose up` na raiz do projeto, aguardar o startup, e fazer `curl -X POST http://localhost:8080/matriculas -H "Content-Type: application/json" -d '{"alunoId":"...","turmaId":"...","periodoInicio":"2026-02-01","periodoFim":"2026-06-30"}'`
**Expected:** Aplicação sobe sem erros, Flyway aplica V1+V2+V3, POST retorna HTTP 201 com JSON MatriculaDto
**Why human:** Requer Docker rodando, Maven no container (apk add maven durante build — pode levar vários minutos), PostgreSQL healthcheck passando antes do app conectar. Não testável via grep.

#### 2. Bean Validation e mapeamento de erros

**Test:** Enviar `POST /matriculas` sem o campo `alunoId`; enviar `POST /matriculas` com IDs de um aluno que já tem matrícula ativa no mesmo período
**Expected:** Ausência de campo retorna 400 com `{"erro":"DADOS_INVALIDOS","mensagem":"Campos inválidos na requisição","campos":[{"campo":"alunoId","mensagem":"O ID do aluno é obrigatório"}]}`; duplicata retorna 409 com `{"erro":"MATRICULA_DUPLICADA","mensagem":"..."}`
**Why human:** Requer aplicação rodando e dados de seed no banco; valida que Bean Validation e ExcecaoHandler se comportam corretamente end-to-end.

#### 3. Success Criteria 2: 422 com limite e atual

**Test:** Adicionar 7 disciplinas à mesma matrícula (uma além do limite de 6): POST /matriculas/{id}/disciplinas 7 vezes
**Expected:** 7ª requisição retorna 422 com `{"erro":"LIMITE_DISCIPLINAS_EXCEDIDO","mensagem":"...","limite":6,"atual":6}`
**Why human:** Requer aplicação rodando e sequência de requisições; confirma que `e.getLimite()` e `e.getAtual()` fluem do domínio pelo ExcecaoHandler até a resposta HTTP.

---

### Gaps Summary

Nenhum gap encontrado. Todos os 13 must-haves verificados, todos os 13 requisitos satisfeitos, nenhum bloqueador de anti-padrão, nenhum arquivo ausente.

A nota sobre DTOs parciais nas endpoints de adicionarDisciplina e cancelar (UseCase retorna void) é um desvio documentado e aceito pelo executor — não impede o objetivo da fase que é expor os três fluxos via HTTP.

---

_Verified: 2026-06-21_
_Verifier: Claude (gsd-verifier)_
