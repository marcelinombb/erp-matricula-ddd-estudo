# Phase 4: Interface, Docker e Material Didático — Research

**Researched:** 2026-06-20
**Domain:** Spring MVC REST, Docker Compose multi-stage, documentação técnica comparativa
**Confidence:** HIGH

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Camada de Interface REST**
- D-01: Pacote `interfaces/` no mesmo nível de `dominio/`, `aplicacao/`, `infraestrutura/`. Arquivos: `MatriculaController.java` e `ExcecaoHandler.java`.
- D-02: Endpoints REST: `POST /matriculas`, `POST /matriculas/{id}/disciplinas`, `POST /matriculas/{id}/cancelamento`.
- D-03: Payload `POST /matriculas`: `{alunoId, turmaId, periodoInicio, periodoFim}` — mapeia para `MatricularAlunoCommand`.
- D-04: `MatriculaDto` response inclui `disciplinas` como lista de objetos `[{nome, adicionadaEm}]`. Requer Flyway V3 com `adicionada_em TIMESTAMP DEFAULT NOW()` em `itens_matricula`.
- D-05: Adicionar `spring-boot-starter-web` e `spring-boot-starter-validation` ao `pom.xml`. Remover `spring.main.web-application-type: none` do `application.yml`.

**Tratamento de Erros HTTP (@ControllerAdvice)**
- D-06: Formato de erro: `{erro: "SNAKE_CASE_UPPER", mensagem: "texto legível"}`. Classe: `ExcecaoHandler`.
- D-07: Mapeamento completo de exceções: `DisciplinaJaMatriculadaException` → 409, `MatriculaCanceladaException` → 409, `LimiteDisciplinasExcedidoException` → 422 (+ `limite`, `atual`), `AlunoInativoException` → 422, `MatriculaNaoEncontradaException` → 404, `MethodArgumentNotValidException` → 400 (+ `campos: [{campo, mensagem}]`).
- D-08: `POST /matriculas/{id}/cancelamento` aceita corpo vazio.

**Docker Compose**
- D-09: Dockerfile multi-stage: stage 1 `eclipse-temurin:21-jdk-alpine` compila com Maven; stage 2 `eclipse-temurin:21-jre-alpine` copia o JAR.
- D-10: PostgreSQL `postgres:16-alpine` com healthcheck `pg_isready`. `depends_on: condition: service_healthy`.
- D-11: Credenciais com defaults: `${DB_USER:-matricula}`, `${DB_PASSWORD:-matricula}`. App via `SPRING_DATASOURCE_*` env vars.

**Material Didático**
- D-12: ADRs 001-004 enriquecidos com seção `## Na prática` linkando arquivos da Fase 3.
- D-13: `docs/04-material-didatico/` com 4 arquivos: `ddd-vs-camadas.md`, `guia-consulta.md`, `licoes-aprendidas.md`, `estrutura-pastas.md`.
- D-14: `README.md` atualizado com Docker e links para material didático.

### Claude's Discretion

- Porta exposta no `docker-compose.yml`: 8080 (padrão Spring Boot).
- Estratégia `application.yml` para datasource: `SPRING_DATASOURCE_URL` env var sobrescreve automaticamente via Spring Boot — sem profiles separados.
- Conteúdo exato da tabela do Guia de Consulta (DID-06): cobrir todos os padrões táticos + padrões de infraestrutura (TypeHandler, RowMapper, listener stub).

### Deferred Ideas (OUT OF SCOPE)

- Testes automatizados (v2: TEST-01..03)
- Optimistic locking completo (v2: PROD-01)
- Contextos Financeiro e Acadêmico implementados (v2: BC-01, BC-02)
- Documentação da API (OpenAPI/Swagger)
</user_constraints>

---

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| IFX-01 | Controller REST `MatriculaController` com endpoints para os três casos de uso | Spring MVC `@RestController` + `@PostMapping`; padrão de delegação para UseCases verificado no código existente |
| IFX-02 | `@ControllerAdvice` mapeando exceções de domínio para respostas HTTP semânticas | Spring MVC `@ControllerAdvice` + `@ExceptionHandler`; mapeamento de exceções tipadas verificado no código existente |
| IFX-03 | Validações de entrada nos request bodies (Bean Validation) | `spring-boot-starter-validation` com `@Valid`, `@NotNull`, `@NotBlank`; integração com `MethodArgumentNotValidException` |
| DCK-01 | `docker-compose.yml` com PostgreSQL e Aplicação configurados e funcionando | Docker Compose v2 disponível (v5.1.4); Dockerfile multi-stage com `eclipse-temurin:21` |
| DCK-02 | Documentação de uso: como subir, como derrubar, como resetar banco | Seção em README.md com comandos `docker compose up/down` |
| DID-01 | Seção "DDD para quem vem da Arquitetura em Camadas" — comparação lado a lado | Código da Fase 3 disponível como fonte de exemplos concretos |
| DID-02 | ADR-001 enriquecido com seção "Na prática" | ADR-001 lido; caminhos dos arquivos de infraestrutura verificados |
| DID-03 | ADR-002 enriquecido com seção "Na prática" | ADR-002 lido; listeners stub verificados |
| DID-04 | ADR-003 enriquecido com seção "Na prática" | ADR-003 lido; `AlunoId.java` verificado |
| DID-05 | ADR-004 enriquecido com seção "Na prática" | ADR-004 lido; convenção de nomes verificada no código |
| DID-06 | Guia de Consulta: conceito DDD → arquivo concreto | Todos os arquivos de domínio inventariados |
| DID-07 | Seção "Lições Aprendidas" com comparação arquitetura tradicional vs DDD | Padrões concretos extraídos do código implementado |
| DID-08 | Estrutura de pastas documentada com propósito pedagógico | Estrutura de pacotes verificada no codebase |
</phase_requirements>

---

## Summary

A Fase 4 entrega as três camadas finais do projeto: interface REST, containerização e material didático comparativo. Toda a infraestrutura necessária (UseCases, Aggregate, exceções tipadas, infraestrutura MyBatis) já está implementada e funcional desde a Fase 3 — a camada de interface apenas delega para ela. Isso simplifica significativamente o trabalho: o Controller não contém lógica, apenas traduz HTTP para Commands e Commands para HTTP.

O maior ponto de atenção técnico é a adaptação entre o `MatricularAlunoCommand` e o Controller: o command recebe objetos de domínio (`Aluno`, `Turma`) já construídos, mas o Controller recebe primitivos (`alunoId`, `turmaId` como UUIDs, `periodoInicio`/`periodoFim` como strings de data). O Controller precisa construir objetos de domínio in-memory a partir dos IDs — sem carregar do repositório (os seeds já têm os dados no banco). Isso é um ponto pedagógico intencional: o controller é responsável pela fronteira HTTP↔domínio.

Para o Docker Compose, o maior cuidado é o Dockerfile multi-stage funcionar sem Maven instalado no host (build acontece dentro do container), e a estratégia de healthcheck para garantir que o banco está pronto antes da aplicação tentar se conectar.

O material didático é a parte mais extensa da fase em volume, mas a mais simples em risco técnico — são documentos Markdown com referências a código já implementado. A chave é usar exemplos concretos do próprio projeto, não snippets hipotéticos.

**Recomendação primária:** Planejar em 3 waves: (1) pom.xml + application.yml + Flyway V3 + controller + exception handler, (2) Dockerfile + docker-compose.yml, (3) material didático (ADRs + 4 documentos + README).

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Receber requisição HTTP | Interface (`MatriculaController`) | — | Ponto de entrada REST; traduz HTTP → Command |
| Validação de entrada | Interface (`@Valid` + Bean Validation) | — | Validação de formato é responsabilidade da interface, não do domínio |
| Tradução HTTP → Command | Interface (`MatriculaController`) | — | Constrói objetos de domínio a partir de primitivos HTTP |
| Execução do caso de uso | Aplicação (UseCases) | — | Orquestração sem lógica de negócio; já implementado na Fase 3 |
| Regras de negócio | Domínio (`Matricula`, `VerificadorElegibilidade`) | — | Invariantes encapsuladas no Aggregate; já implementado |
| Mapeamento exceção → HTTP | Interface (`ExcecaoHandler`) | — | Única fronteira onde exceções de domínio se tornam respostas HTTP |
| Persistência | Infraestrutura (MyBatis) | — | Já implementado; sem mudanças exceto Flyway V3 |
| Containerização | Docker (Dockerfile + compose) | — | Build multi-stage isolado; sem dependência do host |
| Documentação comparativa | Markdown em `docs/04-material-didatico/` | — | Artefatos estáticos; sem lógica de runtime |

---

## Standard Stack

### Core

| Biblioteca | Versão | Propósito | Por que padrão |
|-----------|--------|-----------|----------------|
| `spring-boot-starter-web` | 3.5.3 (gerenciada pelo BOM) | Ativa Spring MVC, Tomcat embutido, suporte REST | Já no parent POM; só falta declarar a dependência |
| `spring-boot-starter-validation` | 3.5.3 (gerenciada pelo BOM) | Bean Validation (Jakarta Validation 3.x) + integração com Spring MVC | Padrão para validação de request bodies no Spring Boot |
| `eclipse-temurin:21-jdk-alpine` | 21-jdk-alpine | Stage 1 do Dockerfile: compilação Maven | Imagem oficial Eclipse Temurin; alpine para menor tamanho |
| `eclipse-temurin:21-jre-alpine` | 21-jre-alpine | Stage 2 do Dockerfile: runtime | JRE (não JDK) para imagem final menor |
| `postgres:16-alpine` | 16-alpine | Banco PostgreSQL no compose | Versão alinhada com o schema existente; alpine para menor tamanho |

[VERIFIED: pom.xml do projeto — spring-boot-starter-parent 3.5.3 já declarado]
[ASSUMED: eclipse-temurin como imagem base padrão para Java 21; Adoptium é o projeto oficial pós-AdoptOpenJDK]

### Supporting

| Biblioteca | Versão | Propósito | Quando usar |
|-----------|--------|-----------|-------------|
| `spring-boot-starter` | Já presente | Core Spring sem web | Base existente; continua |
| `mybatis-spring-boot-starter` | 3.0.5 | Persistência | Já presente; sem mudanças |
| Flyway | Gerenciado pelo BOM | Migrations | V3 adiciona `adicionada_em` em `itens_matricula` |

### Alternatives Considered

| Em vez de | Poderia usar | Trade-off |
|-----------|-------------|-----------|
| `eclipse-temurin:21-jdk-alpine` | `amazoncorretto:21-alpine` | Temurin é a distribuição de referência do OpenJDK; Corretto é equivalente mas específico da Amazon |
| `postgres:16-alpine` | `postgres:17-alpine` | Schema foi desenvolvido e testado com PostgreSQL 16; manter compatibilidade |
| `@ControllerAdvice` | `@RestControllerAdvice` | `@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`; ambos funcionam; `@ControllerAdvice` mais explícito |

**Instalação (dependências a adicionar no pom.xml):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## Package Legitimacy Audit

Esta fase não instala pacotes novos de terceiros. As dependências adicionadas (`spring-boot-starter-web`, `spring-boot-starter-validation`) são módulos oficiais do Spring Boot gerenciados pelo BOM declarado no pom.xml (`spring-boot-starter-parent 3.5.3`).

| Pacote | Registry | Origem | slopcheck | Disposition |
|--------|----------|--------|-----------|-------------|
| `spring-boot-starter-web` | Maven Central | Módulo oficial Spring Boot 3.5.3 | N/A (Maven BOM) | Aprovado |
| `spring-boot-starter-validation` | Maven Central | Módulo oficial Spring Boot 3.5.3 | N/A (Maven BOM) | Aprovado |
| `eclipse-temurin:21-jdk-alpine` | Docker Hub | Imagem oficial Eclipse Adoptium | N/A (Docker image) | Aprovado |
| `postgres:16-alpine` | Docker Hub | Imagem oficial PostgreSQL | N/A (Docker image) | Aprovado |

**Pacotes removidos por slopcheck:** nenhum
**Pacotes suspeitos:** nenhum

---

## Architecture Patterns

### System Architecture Diagram

```
HTTP Request
     |
     v
[MatriculaController]  ← interfaces/
     |  @RestController
     |  @Valid request body → MethodArgumentNotValidException
     |  constrói Command com objetos de domínio
     |
     v
[UseCase]  ← aplicacao/
     |  MatricularAlunoUseCase / AdicionarDisciplinaUseCase / CancelarMatriculaUseCase
     |  já implementados na Fase 3
     |
     v
[Domínio]  ← dominio/
     |  Aggregate lança exceção tipada (DisciplinaJaMatriculada, LimitesExcedido, etc.)
     |
     v                     exceção propagada
[MatriculaRepositorioMyBatis]     |
     |  persiste aggregate        v
     |               [ExcecaoHandler]  ← interfaces/
     |               @ControllerAdvice
     |               mapeia exceção → ResponseEntity<ErroResponse>
     v
[ApplicationEventPublisher]
     |
     v
[FinanceiroEventListener / AcademicoEventListener]  ← infraestrutura/eventos/
```

### Estrutura de Pacotes Recomendada

```
src/main/java/br/com/escola/matricula/
├── dominio/                     # já existe — sem mudanças
│   ├── excecao/
│   ├── modelo/
│   ├── repositorio/
│   ├── servico/
│   └── vo/
├── aplicacao/                   # já existe — MatriculaDto recebe campo disciplinas
│   ├── MatriculaDto.java        # atualizar: adicionar List<ItemDto> disciplinas
│   └── ...
├── infraestrutura/              # já existe — Flyway V3
│   └── ...
└── interfaces/                  # NOVO nesta fase
    ├── MatriculaController.java
    └── ExcecaoHandler.java

src/main/resources/
└── db/migration/
    └── V3__adicionar_adicionada_em.sql  # NOVO
```

### Pattern 1: Controller como Fronteira de Tradução

**O que é:** O Controller não tem lógica de negócio. Sua única responsabilidade é traduzir HTTP (primitivos) para o Command (objetos de domínio) e chamar o UseCase.

**Quando usar:** Sempre que um endpoint delega para um UseCase existente.

**Exemplo:**
```java
// Source: padrão DDD standard — Controller delega para UseCase
@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatricularAlunoUseCase matricularUseCase;
    // ...

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse matricular(@RequestBody @Valid MatricularAlunoRequest request) {
        // Tradução: primitivos HTTP → objetos de domínio
        var aluno = new Aluno(
            new AlunoId(UUID.fromString(request.alunoId())),
            new Cpf("00000000000"),   // CPF não é passado no endpoint — usar placeholder
            "N/A",
            true
        );
        var turma = new Turma(
            new TurmaId(UUID.fromString(request.turmaId())),
            "N/A",
            new PeriodoLetivo(request.periodoInicio().getYear(),
                              request.periodoInicio().getMonthValue() <= 6 ? 1 : 2),
            0
        );
        var command = new MatricularAlunoCommand(aluno, turma, ...);
        MatriculaId id = matricularUseCase.executar(command);
        return new MatriculaResponse(id.valor().toString());
    }
}
```

**Observação crítica:** `MatricularAlunoCommand` recebe objetos `Aluno` e `Turma` completos, mas o payload `POST /matriculas` só envia `alunoId` e `turmaId`. O Controller constrói objetos de domínio com dados mínimos (apenas o ID) para satisfazer o Command. Isso é intencional nesta fase: os campos de `Aluno` e `Turma` além do ID não são usados pelo UseCase em si — o `VerificadorElegibilidadeMatricula` usa `aluno.estaAtivo()` e `turma.getPeriodo()`. Veja a seção "Ponto Crítico: Construção de Aluno e Turma no Controller".

### Pattern 2: @ControllerAdvice como Fronteira HTTP

**O que é:** O `ExcecaoHandler` é o único ponto onde exceções de domínio se tornam respostas HTTP. O domínio nunca importa `jakarta.ws.*` ou `org.springframework.http.*`.

**Quando usar:** Sempre que uma exceção de domínio precisa ser convertida para status HTTP semântico.

```java
// Source: padrão Spring MVC @ControllerAdvice
@ControllerAdvice
public class ExcecaoHandler {

    @ExceptionHandler(MatriculaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> aoNaoEncontrar(MatriculaNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErroResponse("MATRICULA_NAO_ENCONTRADA", e.getMessage()));
    }

    @ExceptionHandler(LimiteDisciplinasExcedidoException.class)
    public ResponseEntity<ErroLimiteResponse> aoExcederLimite(LimiteDisciplinasExcedidoException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErroLimiteResponse(
                "LIMITE_DISCIPLINAS_EXCEDIDO",
                e.getMessage(),
                e.getLimite(),
                e.getAtual()));
    }
    // ...
}
```

### Pattern 3: Dockerfile Multi-Stage

**O que é:** Stage 1 compila o JAR dentro do container (sem Maven no host); stage 2 cria a imagem final com apenas o JRE e o JAR.

```dockerfile
# Source: padrão Docker multi-stage para Spring Boot
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY . .
RUN ./mvnw -q package -DskipTests -pl erp-matricula-app

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/erp-matricula-app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Observação:** O projeto usa Maven Wrapper (`mvnw`) ou requer instalação do Maven no stage 1. Verificar se `mvnw` existe no repositório.

### Anti-Patterns a Evitar

- **Lógica de negócio no Controller:** o Controller nunca valida regras de negócio — isso é do domínio. Validação de formato (`@NotNull`, `@Size`) é da interface; validação de negócio (`limite de disciplinas`) é do Aggregate.
- **Retornar o Aggregate diretamente:** o Controller retorna `MatriculaResponse` (DTO HTTP), nunca o objeto `Matricula` diretamente.
- **Exceções de domínio com imports HTTP:** as classes em `dominio/excecao/` nunca importam `HttpStatus` ou `ResponseEntity` — isso quebra a independência do domínio.
- **Hardcode de credenciais no Dockerfile/compose:** usar variáveis de ambiente com defaults (D-11).

---

## Don't Hand-Roll

| Problema | Não construir | Usar em vez disso | Por que |
|----------|--------------|-------------------|---------|
| Tratamento global de exceções | switch/if em cada controller | `@ControllerAdvice` + `@ExceptionHandler` | Spring MVC captura e despacha automaticamente; hierarquia de herança resolvida |
| Validação de request body | `if (campo == null)` nos controllers | `@Valid` + Bean Validation (`@NotNull`, `@NotBlank`) | Validação automática antes do método ser chamado; erros agregados em `MethodArgumentNotValidException` |
| Espera do banco no Docker | `sleep 10` ou scripts `wait-for-it.sh` | `healthcheck: pg_isready` + `depends_on: condition: service_healthy` | Docker Compose nativo; sem dependência de scripts externos |
| Configuração de datasource por ambiente | Perfis Spring separados (application-dev.yml, application-docker.yml) | `SPRING_DATASOURCE_URL` env var sobrescreve `application.yml` | Spring Boot suporta sobrescrita via env var por convenção; mais simples |

**Insight central:** Spring MVC já resolve todos os problemas de fronteira HTTP → domínio. Não há nada a construir na camada de interface além do mapeamento direto.

---

## Ponto Crítico: Construção de Aluno e Turma no Controller

Este é o aspecto mais delicado da implementação da interface.

**O problema:** `MatricularAlunoCommand` foi desenhado para receber `Aluno` e `Turma` completos (objetos de domínio). O Controller recebe apenas `alunoId` e `turmaId` como UUIDs no payload HTTP. O `VerificadorElegibilidadeMatricula` usa `aluno.estaAtivo()` — para a demonstração funcionar, o aluno precisar estar marcado como ativo.

**Opções analisadas:**

1. **Construir Aluno/Turma placeholder com dados mínimos** (recomendado para esta fase): o Controller cria `new Aluno(new AlunoId(UUID.fromString(request.alunoId())), cpfPlaceholder, "N/A", true)`. O campo `ativo = true` é assumido — o controller não consulta o banco de alunos. Isso é aceitável porque: (a) a fase não tem repositório de Aluno; (b) o verificador usa `aluno.estaAtivo()`, então passar `true` sempre habilita a matrícula (o seed tem aluno ativo); (c) demonstra que o BC Matrícula não carrega o Aggregate completo do Aluno.

2. **Carregar Aluno do banco via query direta ao Mapper**: mais correto semanticamente, mas adiciona um `MatriculaMapper` de leitura de alunos que está fora do escopo desta fase.

**Decisão: opção 1** — placeholder com dados mínimos. O Javadoc do Controller deve explicar que, em produção, os dados viriam do BC correspondente via chamada de API ou evento.

O `PeriodoLetivo` é construído a partir de `periodoInicio` e `periodoFim` (datas recebidas no payload), inferindo semestre pelo mês (mesma lógica do `MatriculaRowMapper`).

---

## Ponto Crítico: MatriculaDto — campo `disciplinas`

O `MatriculaDto` atual tem 4 campos: `matriculaId`, `alunoId`, `statusDescricao`, `totalDisciplinas`. A decisão D-04 adiciona `List<ItemDto> disciplinas` com `{nome, adicionadaEm}`.

**Impacto no código existente:**
1. Flyway V3: `ALTER TABLE itens_matricula ADD COLUMN adicionada_em TIMESTAMP NOT NULL DEFAULT NOW()` — `DEFAULT NOW()` preserva os seeds existentes (V2).
2. `ItemMatriculaRow.java`: adicionar campo `adicionadaEm` do tipo `LocalDateTime`.
3. `MatriculaMapper.xml`: adicionar `adicionada_em` no ResultMap de `ItemMatriculaRow`.
4. `MatriculaRowMapper.java`: mapear `item.adicionadaEm` para o record `ItemDto`.
5. `MatriculaDto.java`: adicionar `List<ItemDto> disciplinas` ao record.

**Ordem de operações:** Flyway V3 primeiro (infraestrutura), depois atualizações no `ItemMatriculaRow`/`MatriculaMapper.xml`/`MatriculaRowMapper`, por fim `MatriculaDto`.

---

## Common Pitfalls

### Pitfall 1: `web-application-type: none` Esquecido no application.yml

**O que dá errado:** Aplicação sobe sem servidor HTTP — endpoints REST não são registrados. Requisições ao port 8080 são recusadas.
**Por que acontece:** A Fase 3 intencionalmente adicionou `spring.main.web-application-type: none` como ponto pedagógico. A Fase 4 precisa remover isso.
**Como evitar:** Remover a linha do `application.yml` (D-05). Verificar: `curl http://localhost:8080/actuator/health` (ou qualquer endpoint) deve responder.
**Sinais de alerta:** Log de startup sem linha "Tomcat started on port(s): 8080".

### Pitfall 2: @ControllerAdvice Não Captura Exceção

**O que dá errado:** Exceção propagada retorna stack trace em HTML (Whitelabel Error Page) em vez do JSON de erro estruturado.
**Por que acontece:** Spring MVC só aplica `@ExceptionHandler` declarado para a classe exata da exceção, a menos que a exceção seja subclasse. `RuntimeException` genérico no handler captura qualquer não mapeada.
**Como evitar:** Mapear todas as exceções de domínio explicitamente. Adicionar handler genérico de fallback para `Exception` que retorne 500 com mensagem genérica.
**Sinais de alerta:** Resposta com `Content-Type: text/html` em vez de `application/json`.

### Pitfall 3: depends_on Não Garante Disponibilidade do Banco

**O que dá errado:** `depends_on` padrão no Docker Compose apenas espera o container *iniciar* — não espera o PostgreSQL estar pronto para conexões. A aplicação Spring Boot inicia, Flyway tenta conectar, e falha com "Connection refused".
**Por que acontece:** PostgreSQL leva alguns segundos para inicializar após o container subir.
**Como evitar:** Usar `healthcheck` com `pg_isready` no service `postgres` + `depends_on: condition: service_healthy` no service da aplicação (D-10). Isso é nativo do Docker Compose v2 — sem scripts externos.
**Sinais de alerta:** Logs da aplicação com "Connection refused" ou "Unable to acquire JDBC Connection" logo após `docker compose up`.

### Pitfall 4: Maven Wrapper Ausente no Dockerfile

**O que dá errado:** `RUN ./mvnw package` falha com "Permission denied" ou "mvnw: not found".
**Por que acontece:** O `mvnw` não está no repositório, ou não tem permissão de execução.
**Como evitar:** Verificar se `mvnw` e `.mvn/wrapper/` existem no repositório. Alternativa: instalar Maven explicitamente no stage de build (`RUN apk add --no-cache maven`). Para este projeto single-module, `RUN mvn -q package -DskipTests -f erp-matricula-app/pom.xml` funciona se Maven estiver instalado no container.
**Sinais de alerta:** Falha no `docker compose build` antes de chegar ao stage 2.

### Pitfall 5: UUID Inválido no Path Parameter

**O que dá errado:** `UUID.fromString(id)` lança `IllegalArgumentException` para IDs mal formatados, resultando em 500 em vez de 400.
**Por que acontece:** Spring MVC não converte automaticamente strings para UUID — o Controller precisa chamar `UUID.fromString()` manualmente (sem `@PathVariable UUID id`).
**Como evitar:** Usar `@PathVariable UUID id` diretamente — Spring MVC converte e lança `MethodArgumentTypeMismatchException` que pode ser mapeada no `ExcecaoHandler` como 400.
**Sinais de alerta:** Stack trace com `IllegalArgumentException: Invalid UUID string`.

### Pitfall 6: Resposta de Sucesso com Status Errado

**O que dá errado:** `POST /matriculas` retorna 200 em vez de 201 Created.
**Por que acontece:** `@PostMapping` retorna 200 por padrão.
**Como evitar:** Anotar o método de criação com `@ResponseStatus(HttpStatus.CREATED)` ou retornar `ResponseEntity.created(uri).body(response)`.
**Sinais de alerta:** Requisição POST retorna 200 — clientes REST que verificam status esperariam 201.

---

## Code Examples

### Request Body para POST /matriculas

```java
// Sem Lombok (convenção do projeto) — usar record Java 21
public record MatricularAlunoRequest(
    @NotNull(message = "alunoId é obrigatório")
    String alunoId,      // UUID como String — convertido no Controller

    @NotNull(message = "turmaId é obrigatório")
    String turmaId,      // UUID como String — convertido no Controller

    @NotNull(message = "periodoInicio é obrigatório")
    LocalDate periodoInicio,   // Jackson converte "2026-02-01" automaticamente

    @NotNull(message = "periodoFim é obrigatório")
    LocalDate periodoFim
) {}
```

### Request Body para POST /matriculas/{id}/disciplinas

```java
public record AdicionarDisciplinaRequest(
    @NotBlank(message = "nome é obrigatório")
    String nome
) {}
```

### Formato de Resposta de Erro

```json
// Erro genérico (409, 422, 404)
{
  "erro": "DISCIPLINA_JA_MATRICULADA",
  "mensagem": "Disciplina 'Matemática Básica' já está matriculada em c000...001"
}

// Erro com campos extras (422 — LimiteDisciplinasExcedido)
{
  "erro": "LIMITE_DISCIPLINAS_EXCEDIDO",
  "mensagem": "Limite de 6 disciplinas excedido. Atual: 6.",
  "limite": 6,
  "atual": 6
}

// Erro de validação (400 — MethodArgumentNotValid)
{
  "erro": "DADOS_INVALIDOS",
  "mensagem": "Campos inválidos na requisição",
  "campos": [
    {"campo": "alunoId", "mensagem": "alunoId é obrigatório"}
  ]
}
```

### docker-compose.yml com Healthcheck

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: erp_matricula
      POSTGRES_USER: ${DB_USER:-matricula}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-matricula}
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-matricula} -d erp_matricula"]
      interval: 5s
      timeout: 5s
      retries: 5

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/erp_matricula
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-matricula}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-matricula}
    depends_on:
      postgres:
        condition: service_healthy
```

---

## Inventário de Código para Material Didático (DID-01 a DID-08)

Esta seção mapeia qual código existente serve de fonte para cada documento didático.

### Para `ddd-vs-camadas.md` (DID-01)

Comparações concretas disponíveis no código:

| Artefato Tradicional | Equivalente DDD | Arquivo DDD |
|---------------------|-----------------|-------------|
| `@Service MatriculaService` com `if/else` | `Matricula.adicionarDisciplina()` | `dominio/modelo/Matricula.java` L183-203 |
| `@Service MatriculaService` com `if/else` | `VerificadorElegibilidadeMatricula.verificar()` | `dominio/servico/VerificadorElegibilidadeMatricula.java` |
| `@Repository extends JpaRepository<Matricula, UUID>` | Interface `MatriculaRepositorio` no domínio | `dominio/repositorio/MatriculaRepositorio.java` |
| `@Entity @Table @Column` na classe Matricula | `Matricula` sem anotações | `dominio/modelo/Matricula.java` L44 |
| `service.save(entity)` | `repositorio.salvar(aggregate)` + coleta de eventos | `aplicacao/MatricularAlunoUseCase.java` L89-108 |
| `status == "ATIVA"` (String magic) | `switch (status) { case Ativa a -> ...}` | `aplicacao/MatriculaDto.java` L44-49 |

### Para `guia-consulta.md` (DID-06)

Inventário completo de padrões DDD no código:

| Conceito DDD | Arquivo | O que observar |
|-------------|---------|----------------|
| Aggregate Root | `dominio/modelo/Matricula.java` | Métodos `adicionarDisciplina()` e `cancelar()` encapsulando invariantes |
| Value Object (tipado) | `dominio/vo/AlunoId.java` | Record com validação no construtor compacto |
| Value Object (com lógica) | `dominio/vo/Cpf.java` | Validação de formato embutida |
| Value Object (período) | `dominio/vo/PeriodoLetivo.java` | Abstração de (ano, semestre) sem acoplamento a datas |
| Entidade | `dominio/modelo/Aluno.java` | `equals`/`hashCode` por identidade (AlunoId) |
| Estado como Sealed Interface | `dominio/modelo/StatusMatricula.java` | Pattern matching exaustivo sem `default` |
| Domain Event | `dominio/evento/AlunoMatriculado.java` | Record imutável com dados do fato |
| Interface de Repositório | `dominio/repositorio/MatriculaRepositorio.java` | Sem imports de framework |
| Domain Service | `dominio/servico/VerificadorElegibilidadeMatricula.java` | Lógica que não pertence a um único Aggregate |
| Exceção tipada com dados | `dominio/excecao/LimiteDisciplinasExcedidoException.java` | Campos `limite` e `atual` estruturados |
| Application Service / UseCase | `aplicacao/MatricularAlunoUseCase.java` | Orquestra sem decidir |
| Command | `aplicacao/MatricularAlunoCommand.java` | Intenção de escrita em objeto imutável |
| DTO de leitura | `aplicacao/MatriculaDto.java` | Factory method `de(Matricula)` |
| Impl do Repositório | `infraestrutura/persistencia/MatriculaRepositorioMyBatis.java` | Dependency Inversion: domínio define, infra implementa |
| TypeHandler | `infraestrutura/persistencia/typehandler/CpfTypeHandler.java` | Conversão VO ↔ JDBC |
| RowMapper (separação domínio/persistência) | `infraestrutura/persistencia/MatriculaRowMapper.java` | ÚNICO arquivo que conhece ambos os modelos |
| Modelo relacional (Row) | `infraestrutura/persistencia/MatriculaRow.java` | Espelho plano do banco, sem comportamento |
| Listener de Domínio Event (stub) | `infraestrutura/eventos/FinanceiroEventListener.java` | `@TransactionalEventListener` após commit |
| Controller REST | `interfaces/MatriculaController.java` | Traduz HTTP → Command; zero lógica de negócio |
| Handler de Exceções | `interfaces/ExcecaoHandler.java` | Única fronteira domínio → HTTP |

### Para `estrutura-pastas.md` (DID-08)

Estrutura completa com propósito pedagógico de cada pacote:

```
erp-matricula-app/src/main/java/br/com/escola/matricula/
├── dominio/          → O coração do sistema. Zero imports de framework.
│   ├── excecao/      → Exceções tipadas com dados estruturados (não RuntimeException genérica)
│   ├── evento/       → Domain Events: records imutáveis (AlunoMatriculado, etc.)
│   ├── modelo/       → Entities e Aggregate Root (Matricula, Aluno, Turma, ItemMatricula)
│   ├── repositorio/  → Interfaces de repositório: domínio define, infra implementa
│   ├── servico/      → Domain Services: lógica que cruza múltiplos Aggregates
│   └── vo/           → Value Objects: tipos que substituem primitivos (AlunoId, Cpf, etc.)
├── aplicacao/        → UseCases: orquestram sem decidir. Podem importar Spring (transações).
│   └── (commands, DTOs, usecases)
├── infraestrutura/   → Tudo que depende de tecnologia externa.
│   ├── config/       → Beans Spring, DemoRunner
│   ├── eventos/      → Listeners de eventos (stubs)
│   └── persistencia/ → MyBatis: Mappers, TypeHandlers, RowMappers
└── interfaces/       → Fronteira HTTP. Traduz entre protocolo e linguagem do domínio.
    ├── MatriculaController.java
    └── ExcecaoHandler.java
```

---

## Estado do Arte — o que mudou desde a Fase 3

| Aspecto | Estado na Fase 3 | Estado na Fase 4 |
|---------|-----------------|-----------------|
| Servidor HTTP | Desativado (`web-application-type: none`) | Ativado (remover configuração) |
| Ponto de entrada | `DemoRunner` via CLI | `MatriculaController` via HTTP |
| Validação de entrada | Construída manualmente no DemoRunner | Bean Validation (`@Valid`) |
| Tratamento de erros | Stack trace propagado | `@ControllerAdvice` mapeado |
| Ambiente | Local apenas (Maven + PostgreSQL instalados) | Docker Compose (`docker compose up`) |
| `itens_matricula` | Sem `adicionada_em` | Com `adicionada_em TIMESTAMP` (Flyway V3) |
| `MatriculaDto` | Sem lista de disciplinas | Com `List<ItemDto> disciplinas` |
| Material didático | ADRs básicos | ADRs enriquecidos + 4 novos documentos |

**Pontos pedagógicos a destacar no `ExcecaoHandler` (Success Criteria 2):** O Javadoc deve afirmar explicitamente que este é o único arquivo do sistema onde exceções de `dominio.excecao.*` se transformam em respostas HTTP. O domínio não importa nada de `jakarta.ws.*` ou `org.springframework.http.*` — essa responsabilidade pertence exclusivamente à camada de interface.

---

## Ambiente Disponível

| Dependência | Requerida por | Disponível | Versão | Fallback |
|------------|--------------|-----------|--------|----------|
| Docker | DCK-01 | Sim | 29.6.0 | — |
| Docker Compose | DCK-01 | Sim | v5.1.4 | — |
| Java | Compilação local | Não (apenas Java 17 disponível) | 17.0.18 (Temurin) | Build via Docker (stage 1) |
| Maven | Compilação local | Não | — | Build via Docker (stage 1) |
| PostgreSQL local | Desenvolvimento | Não verificado | — | Docker Compose provê |

**Observação crítica:** Java 17 está instalado no host, mas o projeto requer Java 21. Maven não está instalado. Isso significa que `mvn spring-boot:run` local não funciona sem instalar Java 21 e Maven. **O Dockerfile multi-stage é a solução padrão** — compila e executa tudo dentro do container. O planner deve:
1. Incluir Maven Wrapper (`mvnw`) no Dockerfile ou instalar Maven no stage de build.
2. Documentar que `docker compose up` é o único comando necessário (sem pré-requisitos de Java/Maven no host além do Docker).

**Dependências sem fallback:** nenhuma (Docker está disponível).

---

## Assumptions Log

| # | Afirmação | Seção | Risco se Errado |
|---|-----------|-------|-----------------|
| A1 | `eclipse-temurin:21-jdk-alpine` é a imagem Docker padrão para Java 21 no estilo deste projeto | Standard Stack | Imagem alternativa seria usada; comportamento idêntico |
| A2 | `mvnw` (Maven Wrapper) não existe no repositório (projeto foi criado manualmente sem `mvn wrapper:wrapper`) | Pitfall 4 / Docker | O Dockerfile precisaria de ajuste: instalar Maven via apk ou usar mvnw |
| A3 | `DemoRunner` deve ser desativado ou adaptado após adicionar a camada web (para evitar conflito de seeds) | Ponto Crítico | DemoRunner executaria os 3 fluxos a cada startup, potencialmente falhando na segunda execução pelo índice único `uq_matricula_aluno_periodo_ativa` |

**Sobre A3 (DemoRunner com a camada web):** O `DemoRunner` sempre tenta criar matrícula para Maria Silva. Na segunda execução (ou ao usar `docker compose up` com volume persistido), o Flyway não reaplica V2 (idempotente), mas a matrícula criada pelo DemoRunner do run anterior já existe — o índice único `uq_matricula_aluno_periodo_ativa` lança erro. O planner deve decidir: (a) adicionar `@ConditionalOnProperty` para desativar o DemoRunner quando o servidor web está ativo, ou (b) envolver o DemoRunner em try/catch com log de aviso.

---

## Questões Abertas

1. **DemoRunner com a camada web ativa**
   - O que sabemos: DemoRunner executa a cada startup e pode falhar na segunda execução pelo índice único de matrícula.
   - O que não está claro: a decisão não foi especificada no CONTEXT.md.
   - Recomendação: Desativar o DemoRunner via propriedade (`demo.runner.enabled=false` no application.yml padrão; true apenas para testes explícitos), ou envolver em try/catch com log. O planner deve escolher.

2. **Construção de Aluno e Turma no Controller**
   - O que sabemos: `MatricularAlunoCommand` recebe `Aluno` e `Turma` completos; Controller tem apenas IDs.
   - O que não está claro: o CPF do Aluno é obrigatório no construtor de `Aluno` (via `Cpf` record com validação). O Controller não recebe CPF.
   - Recomendação: Verificar o construtor de `Aluno` — se `Cpf` tem validação que rejeita placeholder, o Controller precisará de ajuste. Alternativa: o command aceitar apenas IDs em vez de objetos completos, mas isso mudaria código da Fase 3 (fora do escopo indicado pelo CONTEXT.md).

3. **Maven Wrapper no repositório**
   - O que sabemos: Maven não está instalado no host; `mvnw` não foi verificado no repositório.
   - O que não está claro: se `mvnw` existe ou precisa ser criado/gerado.
   - Recomendação: O Dockerfile deve usar `mvn package` com Maven instalado via `apk add --no-cache maven` no stage 1 — mais robusto que depender do wrapper.

---

## Project Constraints (from CLAUDE.md)

| Diretiva | Impacto na Fase 4 |
|----------|------------------|
| Stack obrigatória: Java 21, Spring Boot 3.x, MyBatis, PostgreSQL, Docker | `spring-boot-starter-web` e `spring-boot-starter-validation` são módulos do Spring Boot 3.x — conformes |
| Sem Lombok | Classes de request sem Lombok: usar records Java 21 com `@NotNull` nas anotações. Records já são imutáveis. |
| Sem MapStruct | Mapeamento HTTP ↔ domínio feito manualmente no Controller — é o ponto pedagógico |
| Diagramas em Mermaid | Material didático não usa diagramas novos além dos já existentes (Fase 1/2); referências a diagramas existentes |
| Documentação em Markdown, em português | Todos os arquivos de `docs/04-material-didatico/` em Markdown, em português |
| Código em português | `MatriculaController`, `ExcecaoHandler` em português; campos de request em português (`alunoId` em camelCase, não `studentId`) |
| Single-module Maven | Dockerfile usa `-f erp-matricula-app/pom.xml` ou `mvn -pl erp-matricula-app package` |

---

## Sources

### Primary (HIGH confidence)
- Codebase da Fase 3 — todos os arquivos verificados diretamente (aplicacao/, dominio/, infraestrutura/)
- `pom.xml` do projeto — versões e dependências verificadas
- `application.yml` — configuração atual verificada
- `V1__schema.sql` e `V2__seeds.sql` — schema e seeds verificados
- `04-CONTEXT.md` — decisões do usuário verificadas

### Secondary (MEDIUM confidence)
- Docker 29.6.0 e Docker Compose v5.1.4 — verificados via `docker --version` e `docker compose version`
- Java 17 (não 21) instalado no host — verificado via `java -version`; Maven ausente — verificado

### Tertiary (ASSUMED)
- `eclipse-temurin:21-jdk-alpine` como imagem base padrão para Java 21 [ASSUMED]
- Maven Wrapper (`mvnw`) ausente no repositório [ASSUMED — não verificado diretamente]

---

## Metadata

**Confidence breakdown:**
- Camada de Interface: HIGH — código existente completamente lido; padrões Spring MVC bem estabelecidos
- Docker Compose: HIGH — ambiente verificado; Docker e Compose disponíveis
- Material Didático: HIGH — código-fonte completo disponível como base; estrutura de arquivos verificada
- Ponto crítico DemoRunner: MEDIUM — comportamento inferido da lógica do índice único; solução recomendada mas não confirmada

**Research date:** 2026-06-20
**Valid until:** 2026-07-20 (stack estável; Spring Boot 3.5.x sem mudanças esperadas neste prazo)
