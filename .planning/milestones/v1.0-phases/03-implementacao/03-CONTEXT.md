# Phase 3: Implementacao - Context

**Gathered:** 2026-06-20
**Status:** Ready for planning

<domain>
## Phase Boundary

Esta fase entrega o código Java 21 funcional das camadas de domínio, aplicação e infraestrutura. O projeto compila, o banco PostgreSQL sobe via Flyway, e os três fluxos de negócio (matricular aluno, adicionar disciplina, cancelar matrícula) são executáveis com dados reais persistidos.

**Deliverables:** `src/` completo com estrutura DDD, `pom.xml`, `application.yml`, migrations Flyway, seeds, MyBatis mappers e ResultMaps.

**Não inclui:** Controllers REST, endpoints HTTP, Docker Compose completo, material didático comparativo (esses são Fase 4).

</domain>

<decisions>
## Implementation Decisions

### Estrutura de Pacotes

- **D-01:** Pacote raiz: `br.com.escola.matricula` — padrão Java de produção, consistente com o contexto do projeto (equipe de escola).
- **D-02:** Sub-pacotes do domínio por tipo:
  - `dominio.modelo/` — Aggregate Root `Matricula`, Entidades `Aluno`, `Turma`, `ItemMatricula`
  - `dominio.vo/` — Value Objects: `Cpf`, `PeriodoLetivo`, `MatriculaId`, `AlunoId`, `TurmaId`, `NomeDisciplina`
  - `dominio.evento/` — Domain Events: `AlunoMatriculado`, `DisciplinaAdicionada`, `MatriculaCancelada`
  - `dominio.repositorio/` — Interface `MatriculaRepositorio` (sem import de framework)
  - `dominio.servico/` — Domain Service `VerificadorElegibilidadeMatricula`
  - `dominio.excecao/` — Exceções tipadas: `LimiteDisciplinasExcedidoException`, `DisciplinaJaMatriculadaException`, `MatriculaCanceladaException`, `AlunoInativoException`, `PeriodoFechadoException`
- **D-03:** Camada de aplicação flat: `aplicacao/` sem sub-pacotes. Use cases, Commands e DTOs ficam todos em `aplicacao/`. São poucos artefatos — sub-pacotes adicionariam navegação sem ganho.
- **D-04:** Camada de infraestrutura com sub-pacotes por tecnologia:
  - `infraestrutura.persistencia/` — `MatriculaRepositorioMyBatis`, `MatriculaMapper` (interface MyBatis), `MatriculaRowMapper`, `MatriculaRow`, `ItemMatriculaRow`, TypeHandlers
  - `infraestrutura.eventos/` — `FinanceiroEventListener`, `AcademicoEventListener` (stubs)
  - `infraestrutura.config/` — `MyBatisConfig`, configurações de TypeHandlers
- **D-05:** `ErpMatriculaApplication.java` na raiz do pacote `br.com.escola.matricula`.

### Bootstrap do Projeto

- **D-06:** `groupId: br.com.escola`, `artifactId: erp-matricula`, `version: 0.1.0-SNAPSHOT`. Java 21, Spring Boot 3.5.3.
- **D-07:** Starters incluídos na Fase 3 (mínimo viável — sem web):
  - `spring-boot-starter` (core, não inclui Tomcat)
  - `mybatis-spring-boot-starter:3.0.5`
  - `postgresql` (driver JDBC, gerenciado pelo Spring Boot BOM)
  - `flyway-core` (gerenciado pelo Spring Boot BOM)
  - `spring-boot-starter-test` (scope test)
  - **Não incluir** `spring-boot-starter-web` — Controllers são Fase 4. Mostra pedagogicamente que DDD não depende de HTTP.
- **D-08:** `application.yml` único (sem profiles Spring na Fase 3):
  - Datasource: `localhost:5432/erp_matricula`, usuário/senha simples para desenvolvimento
  - Flyway habilitado com `locations: classpath:db/migration`
  - MyBatis: `mapper-locations: classpath:mapper/**/*.xml`, `type-aliases-package: br.com.escola.matricula.infraestrutura.persistencia`
  - Log level DEBUG para `br.com.escola.matricula` para facilitar leitura pedagógica

### Coleta e Publicação de Eventos de Domínio

- **D-09:** O Aggregate `Matricula` mantém `private final List<Object> eventos = new ArrayList<>()`. Método `coletarEventos()` retorna `List.copyOf(eventos)` e limpa a lista interna. Zero dependência de Spring — o Aggregate é Java puro. Domain Events são adicionados dentro dos métodos de negócio (`matricular()`, `adicionarDisciplina()`, `cancelar()`).
- **D-10:** O UseCase recebe `ApplicationEventPublisher` por injeção de construtor. Sequência obrigatória: (1) validar elegibilidade → (2) operar no Aggregate → (3) `repositorio.salvar(matricula)` → (4) `matricula.coletarEventos().forEach(publisher::publishEvent)`. Publicação ocorre APÓS a persistência — garantido pela ordem explícita no código, reforçado por `@Transactional`.
- **D-11:** Domain Events são records Java 21 independentes, sem interface base nem tipo comum. Lista interna do Aggregate é `List<Object>`. Simples e pedagógico: mostra que `ApplicationEventPublisher.publishEvent(Object)` aceita qualquer tipo. Evita a abstração prematura de uma interface `DomainEvent` sem comportamento.

### Persistência do Aggregate (MyBatis)

- **D-12:** Estratégia replace-all para salvar `Matricula` com sua coleção de `ItemMatricula`:
  1. Se novo: `INSERT` em `matriculas`
  2. Se existente: `UPDATE` em `matriculas`
  3. `DELETE` todos os registros em `itens_matricula` para o `matricula_id`
  4. `INSERT` todos os `ItemMatricula` atuais
  Pedagogicamente explícita: o desenvolvedor vê que o Aggregate é a unidade de consistência e que a coleção é tratada como um todo atômico.
- **D-13:** Separação explícita domínio ↔ persistência via `MatriculaRow` e `MatriculaRowMapper`:
  - `MatriculaRow` — classe simples com campos da tabela (`UUID id`, `UUID alunoId`, `String status`, `LocalDate periodoInicio`, `LocalDate periodoFim`) sem nenhum método de negócio
  - `ItemMatriculaRow` — análogo para `itens_matricula`
  - `MatriculaRowMapper` — converte explicitamente `MatriculaRow + List<ItemMatriculaRow>` → `Matricula` e `Matricula` → `MatriculaRow`. Alinhado com INF-06. O aluno lê este arquivo e vê exatamente onde domínio e persistência se separam.
- **D-14:** Value Objects com múltiplos campos mapeiam para colunas separadas no PostgreSQL:
  - `PeriodoLetivo` → `periodo_inicio DATE` + `periodo_fim DATE`
  - `Cpf` → `cpf VARCHAR(11)` (uma coluna)
  - `MatriculaId`, `AlunoId`, `TurmaId` → `UUID` nativo PostgreSQL
  - TypeHandlers MyBatis: `CpfTypeHandler` (String ↔ Cpf), `PeriodoLetivoTypeHandler` precisará de abordagem diferente (dois campos) — o planejador decide: TypeHandler para cada campo ou mapeamento inline no ResultMap.

### Claude's Discretion

- Número exato de seeds: suficientes para cobrir os 3 fluxos (matricular, adicionar, cancelar). Pelo menos 1 Aluno ativo, 1 Turma com período aberto, 1 Matrícula existente para os fluxos de adição e cancelamento.
- Nome do banco PostgreSQL: `erp_matricula` (sugerido; planejador pode ajustar).
- Abordagem exata do `PeriodoLetivoTypeHandler` para dois campos: TypeHandler separado por campo vs. mapeamento inline no XML ResultMap — julgamento do planejador.
- Configuração de transaction manager: padrão Spring Boot (DataSourceTransactionManager), sem customização.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requisitos da Fase 3

- `.planning/REQUIREMENTS.md` §DOM-01..10, §APL-01..05, §INF-01..07 — Requisitos completos com critérios de aceite. Leitura obrigatória.
- `.planning/ROADMAP.md` §Phase 3 — Goal, Success Criteria e dependências da fase.

### Especificação de Design (saídas da Fase 2 — são a spec para o código desta fase)

- `docs/02-design-tatico/value-objects.md` — Quais VOs existem, suas validações e o snippet Java 21 prospectivo para cada um. DOM-01.
- `docs/02-design-tatico/entidades.md` — Entidades `Aluno`, `Turma`, `ItemMatricula` com equals/hashCode por identidade. DOM-02, DOM-03.
- `docs/02-design-tatico/agregados.md` — Invariantes do Aggregate `Matricula`, `StatusMatricula` como sealed interface, ciclo de vida. DOM-04, DOM-05.
- `docs/02-design-tatico/domain-events.md` — Domain Events definidos, quem publica, quem consome. DOM-06.
- `docs/02-design-tatico/domain-services.md` — `VerificadorElegibilidadeMatricula`: o que verifica (aluno ativo, período aberto, sem duplicidade). DOM-08.
- `docs/02-design-tatico/repositorios.md` — Interface `MatriculaRepositorio` no domínio, sem imports de framework. DOM-09.
- `docs/02-design-tatico/modelagem.md` — Sequence diagram completo "Realizar Matrícula" (HTTP → UseCase → Aggregate → Repositório → Evento). Referência para a ordem das operações nos UseCases.

### Decisões Arquiteturais (ADRs)

- `docs/adrs/ADR-001-mybatis-vs-jpa.md` — Por que MyBatis; implica `MatriculaRow` separado, TypeHandlers e mapeamento explícito. INF-04..07.
- `docs/adrs/ADR-003-referencia-por-id.md` — `Matricula` guarda `AlunoId` (não `Aluno`); `Turma` referenciada por `TurmaId` também. DOM-02, DOM-03.
- `docs/adrs/ADR-004-codigo-em-portugues.md` — Todos os nomes de classes, métodos, campos e variáveis em português. Obrigatório em todo o código.

### Linguagem Ubíqua

- `docs/01-design-estrategico/linguagem-ubiqua.md` — Glossário de termos; todos os nomes de classes e métodos DEVEM seguir a Linguagem Ubíqua aqui definida.

### Stack Tecnológica Verificada

- `.planning/research/STACK.md` — Versões verificadas: Spring Boot 3.5.3, mybatis-spring-boot-starter 3.0.5, Java 21. Usar essas versões.
- `.planning/research/PITFALLS.md` — Pitfalls conhecidos: N+1 com MyBatis, TypeHandlers para VOs, ordem de persistência do Aggregate.
- `.planning/PROJECT.md` §Key Decisions — Decisões pré-existentes que NÃO devem ser re-discutidas.

### Contexto de Domínio

- `contexto-matricula.md` — Regras de negócio completas do domínio; fonte de verdade para invariantes do Aggregate e fluxos.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `docs/02-design-tatico/*.md` — Snippets Java 21 prospectivos nos documentos táticos funcionam como especificação técnica do código a ser escrito. O planejador deve tratar esses snippets como contrato de interface.
- `docs/adrs/` — 4 ADRs existentes; referenciar nos comentários de código onde relevante (ex: `MatriculaRow.java` pode referenciar ADR-001 em Javadoc).

### Established Patterns

- **Nomes em português**: estabelecido na Fase 1 e reforçado em todos os snippets da Fase 2. Nunca `StudentEntity`, sempre `Aluno`.
- **Sem Lombok**: Java 21 records eliminam necessidade de Lombok para VOs; entidades com construtores explícitos.
- **Sem JPA/Spring Data**: nenhuma anotação `@Entity`, `@Id`, `@Column` no pacote `dominio/`.
- **Diagramas Mermaid** no `modelagem.md` mostram a estrutura de classes esperada — referência para criar os arquivos corretos.

### Integration Points

- Esta fase produz o `src/` que a Fase 4 estende com Controllers REST e Docker Compose.
- `application.yml` desta fase será expandido na Fase 4 com configurações web.
- Flyway migrations desta fase persistem para a Fase 4 — o schema criado aqui é o schema final.
- Os listeners stub (`FinanceiroEventListener`, `AcademicoEventListener`) criados em APL-05 são referenciados no material didático da Fase 4.

</code_context>

<specifics>
## Specific Ideas

- **Sequence de publicação de eventos no UseCase** (D-10): a ordem `salvar() → coletarEventos() → publishEvent()` deve aparecer explicitamente e em sequência nos três UseCases. O `@Transactional` garante que listeners com `@TransactionalEventListener` recebem o evento apenas após o commit — isso deve ser comentado no código.
- **Teste de sucesso dos criteria via grep** (Success Criteria 1 do ROADMAP): `grep -r "import org.springframework" src/main/java/*/dominio/` deve retornar vazio. O planejador deve verificar que nenhum arquivo em `dominio/` importa Spring.
- **MatriculaRow sem comportamento**: `MatriculaRow` deve ser uma classe com campos públicos ou getters simples, sem lógica de negócio — o contraste com `Matricula.java` (que tem comportamento) é o ponto pedagógico de INF-06.
- **`LimiteDisciplinasExcedidoException` com campos estruturados** (DOM-10): a exceção deve conter `int limite` e `int atual` como campos, não apenas mensagem string. Isso permite que controllers na Fase 4 retornem respostas HTTP 422 com dados estruturados.

</specifics>

<deferred>
## Deferred Ideas

- **Docker Compose** — Configuração do ambiente containerizado vai para a Fase 4 (DCK-01, DCK-02).
- **Controllers REST** — Exposição HTTP da API vai para a Fase 4 (IFX-01, IFX-02, IFX-03).
- **Testes automatizados** — Fora do escopo do projeto no v1 (declarado em PROJECT.md §Out of Scope).
- **Optimistic locking completo** — Apenas nota explicativa no código desta fase, sem implementação completa (decisão de STATE.md acumulado).

</deferred>

---

*Phase: 3-Implementacao*
*Context gathered: 2026-06-20*
