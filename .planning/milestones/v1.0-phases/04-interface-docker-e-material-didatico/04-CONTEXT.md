# Phase 4: Interface, Docker e Material Didatico - Context

**Gathered:** 2026-06-20
**Status:** Ready for planning

<domain>
## Phase Boundary

Esta fase entrega o projeto completo e operacional: camada de interface REST (`MatriculaController` + `ExcecaoHandler`), Docker Compose autônomo com Dockerfile multi-stage, e o material didático comparativo — comparação DDD vs arquitetura em camadas, enriquecimento dos ADRs existentes com código real, Guia de Consulta conceito→arquivo, Lições Aprendidas e Estrutura de Pastas documentada.

**Deliverables:**
- `src/.../interfaces/` — Controller REST e handler de exceções
- `Dockerfile` multi-stage + `docker-compose.yml`
- `docs/04-material-didatico/` — 4 novos arquivos didáticos
- `docs/adrs/ADR-001..004.md` — enriquecidos com seção "Na prática"
- `README.md` — atualizado com seções de interface e material didático

**Não inclui:** testes automatizados, autenticação, outros Bounded Contexts (fora do escopo do v1 conforme PROJECT.md).

</domain>

<decisions>
## Implementation Decisions

### Camada de Interface REST

- **D-01:** Pacote `interfaces/` — espelha a terminologia DDD ("Interface Layer"), consistente com `dominio/`, `aplicacao/`, `infraestrutura/`. Arquivos: `MatriculaController.java` e `ExcecaoHandler.java`.
- **D-02:** Endpoints REST dos três fluxos:
  - `POST /matriculas` — criar matrícula
  - `POST /matriculas/{id}/disciplinas` — adicionar disciplina
  - `POST /matriculas/{id}/cancelamento` — cancelar matrícula (sub-recurso: trata ação de estado como recurso, consistente com `/disciplinas`)
- **D-03:** Payload `POST /matriculas`: `{alunoId, turmaId, periodoInicio, periodoFim}` — mapeia diretamente para `MatricularAlunoCommand`. Disciplinas adicionadas em chamadas separadas via `/disciplinas`.
- **D-04:** `MatriculaDto` response inclui `disciplinas` como lista de objetos: `[{nome, adicionadaEm}]`. Requer nova coluna `adicionada_em TIMESTAMP` em `itens_matricula` (Flyway V3 com `DEFAULT NOW()` para retrocompatibilidade com seeds existentes).
- **D-05:** Adicionar `spring-boot-starter-web` e `spring-boot-starter-validation` ao `pom.xml`. Remover `spring.main.web-application-type: none` do `application.yml`.

### Tratamento de Erros HTTP (@ControllerAdvice)

- **D-06:** Formato base de resposta de erro: `{erro: "SNAKE_CASE_UPPER", mensagem: "texto legível"}`. Campos extras estruturados quando a exceção os possui (ex: `limite`, `atual` de `LimiteDisciplinasExcedidoException`). Nome da classe: `ExcecaoHandler` (português, consistente com o padrão do projeto).
- **D-07:** Mapeamento completo de exceções de domínio:

  | Exceção | Status HTTP | Campos extras |
  |---------|------------|---------------|
  | `DisciplinaJaMatriculadaException` | 409 Conflict | — |
  | `MatriculaCanceladaException` | 409 Conflict | — |
  | `LimiteDisciplinasExcedidoException` | 422 Unprocessable | `limite`, `atual` |
  | `AlunoInativoException` | 422 Unprocessable | — |
  | `MatriculaNaoEncontradaException` | 404 Not Found | — |
  | `MethodArgumentNotValidException` | 400 Bad Request | `campos: [{campo, mensagem}]` |

- **D-08:** `POST /matriculas/{id}/cancelamento` aceita corpo vazio (cancelamento sem motivo no v1).

### Docker Compose

- **D-09:** Dockerfile multi-stage: Stage 1 `eclipse-temurin:21-jdk-alpine` compila com Maven (`mvn -q package -DskipTests`); Stage 2 `eclipse-temurin:21-jre-alpine` copia o JAR. Autônomo — `docker compose up` funciona sem JAR pré-compilado.
- **D-10:** PostgreSQL `postgres:16-alpine` com healthcheck `pg_isready`. A aplicação usa `depends_on: condition: service_healthy` — sem scripts externos de espera.
- **D-11:** Credenciais como variáveis de ambiente com defaults: `${DB_USER:-matricula}`, `${DB_PASSWORD:-matricula}`. Aplicação configurada via `SPRING_DATASOURCE_*` env vars no compose. Mostra a prática de não hardcodar credenciais mesmo em projeto didático.

### Material Didático

- **D-12:** ADRs existentes (`docs/adrs/ADR-001..004.md`) enriquecidos com seção `## Na prática` que linka os arquivos implementados na Fase 3. Sem criar documentos duplicados — os ADRs originais são expandidos.
- **D-13:** `docs/04-material-didatico/` com 4 arquivos:
  - `ddd-vs-camadas.md` — DID-01: tabela lado a lado comparando artefatos tradicionais vs DDD com exemplos concretos do fluxo de matrícula (pelo menos: `@Service` vs `UseCase`, `@Repository extends JpaRepository` vs interface de domínio + impl separada, `@Entity` vs Aggregate Root sem anotações, `@Service` com lógica de negócio vs lógica encapsulada no Aggregate)
  - `guia-consulta.md` — DID-06: tabela Markdown `| Conceito DDD | Arquivo | O que observar |` cobrindo todos os padrões do projeto
  - `licoes-aprendidas.md` — DID-07: o que seria feito em arquitetura tradicional vs DDD, benefícios e trade-offs honestos
  - `estrutura-pastas.md` — DID-08: propósito de cada diretório com explicação pedagógica de por que está ali
- **D-14:** `README.md` da raiz atualizado com novas seções linkando `docs/04-material-didatico/` e informações de como rodar com Docker.

### Claude's Discretion

- Porta exposta no `docker-compose.yml`: 8080 (padrão Spring Boot). Planejador decide se mapeia para porta diferente no host.
- Estratégia `application.yml` para datasource funcionar tanto local (`localhost:5432`) quanto Docker (`postgres:5432`): usar `SPRING_DATASOURCE_URL` env var no compose sobrescreve o `application.yml` automaticamente via Spring Boot — sem precisar de profiles separados.
- Conteúdo exato da tabela do Guia de Consulta (DID-06): cobrir todos os padrões táticos do projeto mais os padrões de infraestrutura (TypeHandler, RowMapper, listener stub).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requisitos da Fase 4

- `.planning/REQUIREMENTS.md` §IFX-01..03, §DCK-01..02, §DID-01..08 — Requisitos completos. Leitura obrigatória.
- `.planning/ROADMAP.md` §Phase 4 — Goal, Success Criteria e dependências da fase.

### Código Implementado na Fase 3 (base para estender)

- `erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/` — `MatricularAlunoUseCase`, `AdicionarDisciplinaUseCase`, `CancelarMatriculaUseCase`, Commands e `MatriculaDto`. O Controller delega para estes.
- `erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/excecao/` — Todas as exceções tipadas com campos estruturados. O `ExcecaoHandler` as captura.
- `erp-matricula-app/src/main/resources/application.yml` — Remover `web-application-type: none` (D-05). Adicionado na Fase 3 como ponto pedagógico que agora cede lugar à camada web.
- `erp-matricula-app/pom.xml` — Adicionar `spring-boot-starter-web` e `spring-boot-starter-validation`.
- `erp-matricula-app/src/main/resources/db/migration/` — Flyway existente; adicionar V3 com `adicionada_em` em `itens_matricula`.

### ADRs para Enriquecer (DID-02..05)

- `docs/adrs/ADR-001-mybatis-vs-jpa.md` — Enriquecer com links para `MatriculaRow.java` e `MatriculaRowMapper.java` na seção "Na prática".
- `docs/adrs/ADR-002-escopo-bounded-context.md` — Enriquecer com links para `FinanceiroEventListener.java` e `AcademicoEventListener.java`.
- `docs/adrs/ADR-003-referencia-por-id.md` — Enriquecer com link para `Matricula.java` mostrando o campo `AlunoId alunoId`.
- `docs/adrs/ADR-004-codigo-em-portugues.md` — Enriquecer com exemplos do código implementado.

### Documentação Existente (referências para o material didático)

- `docs/02-design-tatico/modelagem.md` — Sequence diagram "Realizar Matrícula" a ser referenciado em `guia-consulta.md`.
- `docs/01-design-estrategico/linguagem-ubiqua.md` — Nomes canônicos dos artefatos usados no Guia de Consulta.
- `contexto-matricula.md` — Regras de negócio para `ddd-vs-camadas.md` e `licoes-aprendidas.md`.
- `.planning/PROJECT.md` §Key Decisions — Decisões pré-existentes que NÃO devem ser re-discutidas.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `aplicacao/MatricularAlunoUseCase.java` — Já implementado. Controller chama `useCase.executar(command)` e recebe `MatriculaDto`.
- `aplicacao/MatriculaDto.java` — Já existe. Verificar campos atuais e adicionar `List<ItemDto> disciplinas` com `{nome, adicionadaEm}`.
- `dominio/excecao/LimiteDisciplinasExcedidoException.java` — Já tem `getLimite()` e `getAtual()`. ExcecaoHandler usa diretamente.
- `infraestrutura/eventos/FinanceiroEventListener.java`, `AcademicoEventListener.java` — Stubs já implementados; referenciados no material didático como exemplo de Domain Events cross-context.

### Established Patterns

- **Nomes em português**: `ExcecaoHandler` (não `GlobalExceptionHandler`), `MatriculaController`, classe de request como `MatriculaRequest` ou `MatricularAlunoRequest`.
- **Sem Lombok**: campos de request com `@NotNull` via Bean Validation, construtores ou records para DTOs de entrada.
- **Estrutura de pacotes DDD**: nova pasta `interfaces/` no nível de `dominio/`, `aplicacao/`, `infraestrutura/`.
- **Flyway migrations**: V1 (schema), V2 (seeds) já existem. V3 adiciona `adicionada_em`.

### Integration Points

- `pom.xml`: adicionar `spring-boot-starter-web` e `spring-boot-starter-validation`.
- `application.yml`: remover `web-application-type: none`; adicionar `server.port: 8080` (opcional, é o default).
- `docker-compose.yml` conecta via `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/erp_matricula` (sobrescreve `localhost:5432` do `application.yml`).
- Flyway V3: `ALTER TABLE itens_matricula ADD COLUMN adicionada_em TIMESTAMP NOT NULL DEFAULT NOW()` — `DEFAULT NOW()` garante retrocompatibilidade com seeds V2.

</code_context>

<specifics>
## Specific Ideas

- **Campo `erro` em português**: o JSON de erro usa `"erro"` (não `"error"`), consistente com o restante do projeto em português.
- **POST /matriculas/{id}/cancelamento com corpo vazio**: o endpoint aceita `{}` ou sem corpo — matrícula no v1 não persiste motivo de cancelamento.
- **Ponto pedagógico central do @ControllerAdvice** (Success Criteria 2): o arquivo deve deixar explícito no Javadoc que exceções de domínio (`dominio.excecao.*`) cruzam a fronteira para HTTP aqui — é a única saída do domínio para o protocolo HTTP. Demonstra que o domínio não conhece HTTP.
- **`ddd-vs-camadas.md`** deve usar exemplos do próprio projeto (o código já existe!) — não snippets hipotéticos. A comparação é "como você faria em Spring Service vs como está implementado aqui em `MatricularAlunoUseCase.java`".
- **Guia de Consulta** deve incluir coluna "Conceito DDD" com o nome canônico (ex: "Aggregate Root", "Value Object"), coluna "Arquivo" com caminho relativo e coluna "O que observar" com 1 linha de destaque pedagógico.

</specifics>

<deferred>
## Deferred Ideas

- **Testes automatizados** — fora do escopo do v1 (declarado em PROJECT.md §Out of Scope). v2 requirements TEST-01..03.
- **Optimistic locking completo** — apenas nota explicativa nas Lições Aprendidas, sem implementação (PROD-01 em v2 requirements).
- **Contextos Financeiro e Acadêmico implementados** — BC-01, BC-02 em v2. Os listeners stub da Fase 3 são o ponto de referência no material didático.
- **Documentação da API (OpenAPI/Swagger)** — não está nos requisitos do v1; pode ser adicionado em v2 como aprimoramento do material didático.

</deferred>

---

*Phase: 4-Interface-Docker-e-Material-Didatico*
*Context gathered: 2026-06-20*
