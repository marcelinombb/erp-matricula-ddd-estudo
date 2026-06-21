# Phase 4: Interface, Docker e Material Didatico - Pattern Map

**Mapped:** 2026-06-20
**Files analyzed:** 14 (novos ou modificados)
**Analogs found:** 11 / 14

---

## File Classification

| Novo/Modificado | Role | Data Flow | Analog Mais Próximo | Qualidade |
|-----------------|------|-----------|---------------------|-----------|
| `interfaces/MatriculaController.java` | controller | request-response | `infraestrutura/config/DemoRunner.java` | role-partial (mesmo padrão de delegação para UseCases) |
| `interfaces/ExcecaoHandler.java` | middleware | request-response | `dominio/excecao/LimiteDisciplinasExcedidoException.java` | partial (exceções existem; handler é novo) |
| `erp-matricula-app/pom.xml` | config | — | si mesmo (modificação) | exact |
| `erp-matricula-app/src/main/resources/application.yml` | config | — | si mesmo (modificação) | exact |
| `db/migration/V3__adicionar_adicionada_em.sql` | migration | batch | `db/migration/V1__schema.sql` | exact |
| `infraestrutura/persistencia/ItemMatriculaRow.java` | model | — | `infraestrutura/persistencia/MatriculaRow.java` | exact |
| `infraestrutura/persistencia/MatriculaRowMapper.java` | utility | transform | si mesmo (modificação) | exact |
| `resources/mapper/MatriculaMapper.xml` | config | — | si mesmo (modificação) | exact |
| `aplicacao/MatriculaDto.java` | utility | transform | si mesmo (modificação) | exact |
| `Dockerfile` | config | — | — | sem analog |
| `docker-compose.yml` | config | — | — | sem analog |
| `docs/04-material-didatico/ddd-vs-camadas.md` | documentation | — | `docs/adrs/ADR-001-mybatis-vs-jpa.md` | role-match |
| `docs/04-material-didatico/guia-consulta.md` | documentation | — | `docs/02-design-tatico/modelagem.md` | role-match |
| `docs/04-material-didatico/licoes-aprendidas.md` | documentation | — | `docs/adrs/ADR-002-escopo-bounded-context.md` | role-match |
| `docs/04-material-didatico/estrutura-pastas.md` | documentation | — | `docs/adrs/ADR-003-referencia-por-id.md` | role-match |
| `docs/adrs/ADR-001..004.md` | documentation | — | si mesmo (enriquecimento) | exact |
| `README.md` | documentation | — | si mesmo (modificação) | exact |

---

## Pattern Assignments

### `interfaces/MatriculaController.java` (controller, request-response)

**Analog:** `infraestrutura/config/DemoRunner.java`

O `DemoRunner` é o análogo mais próximo existente: constrói objetos de domínio (`Aluno`, `Turma`, VOs) a partir de dados primitivos e delega para os três UseCases exatamente como o Controller deve fazer. A diferença é que o Controller recebe primitivos via HTTP em vez de hardcodar UUIDs.

**Padrão de imports** — baseado em `DemoRunner.java` (linhas 1-20) e na convenção do projeto:
```java
package br.com.escola.matricula.interfaces;

import br.com.escola.matricula.aplicacao.AdicionarDisciplinaCommand;
import br.com.escola.matricula.aplicacao.AdicionarDisciplinaUseCase;
import br.com.escola.matricula.aplicacao.CancelarMatriculaCommand;
import br.com.escola.matricula.aplicacao.CancelarMatriculaUseCase;
import br.com.escola.matricula.aplicacao.MatriculaDto;
import br.com.escola.matricula.aplicacao.MatricularAlunoCommand;
import br.com.escola.matricula.aplicacao.MatricularAlunoUseCase;
import br.com.escola.matricula.dominio.modelo.Aluno;
import br.com.escola.matricula.dominio.modelo.Turma;
import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.Cpf;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import br.com.escola.matricula.dominio.vo.TurmaId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;
```

**Padrão de injeção por construtor** — copiado de `MatricularAlunoUseCase.java` (linhas 69-76):
```java
// SEM @Autowired — Spring injeta automaticamente com construtor único
private final MatricularAlunoUseCase matricularUseCase;
private final AdicionarDisciplinaUseCase adicionarUseCase;
private final CancelarMatriculaUseCase cancelarUseCase;

public MatriculaController(
        MatricularAlunoUseCase matricularUseCase,
        AdicionarDisciplinaUseCase adicionarUseCase,
        CancelarMatriculaUseCase cancelarUseCase) {
    this.matricularUseCase = matricularUseCase;
    this.adicionarUseCase  = adicionarUseCase;
    this.cancelarUseCase   = cancelarUseCase;
}
```

**Padrão de construção de domínio a partir de primitivos** — copiado de `DemoRunner.java` (linhas 82-99):
```java
// DemoRunner.java linhas 82-99 — EXATAMENTE este padrão no Controller
var maria = new Aluno(
    new AlunoId(UUID.fromString("a0000000-0000-0000-0000-000000000001")),
    new Cpf("52998224725"),
    "Maria Silva",
    true
);
var turma2026 = new Turma(
    new TurmaId(UUID.fromString("b0000000-0000-0000-0000-000000000001")),
    "Turma 2026-1",
    new PeriodoLetivo(2026, 1),
    30
);
```

O Controller constrói Aluno/Turma da mesma forma, mas a partir dos UUIDs recebidos no request. CPF usa placeholder `new Cpf("52998224725")` — CPF válido do seed — pois o endpoint não recebe CPF. Ativo = `true` assumido (ver RESEARCH.md "Ponto Crítico").

**Padrão de inferência de PeriodoLetivo** — copiado de `MatriculaRowMapper.java` (linhas 68-71):
```java
// MatriculaRowMapper.java linhas 68-71 — mesma lógica no Controller
var periodo = new PeriodoLetivo(
    row.periodoInicio.getYear(),
    row.periodoInicio.getMonthValue() <= 6 ? 1 : 2
);
```

No Controller: `periodoInicio.getMonthValue() <= 6 ? 1 : 2` para inferir semestre do `LocalDate` recebido no request.

**Padrão de delegação ao UseCase** — copiado de `DemoRunner.java` (linhas 99-101):
```java
// DemoRunner.java linhas 99-101
var command = new MatricularAlunoCommand(maria, turma2026, periodo2026s1);
MatriculaId novaId = matricularUseCase.executar(command);
// Controller retorna DTO, não MatriculaId diretamente
```

**Request records (sem Lombok)** — padrão do projeto: usar Java 21 records com Bean Validation:
```java
// Padrão do projeto: sem Lombok, usar records Java 21
// Ver MatricularAlunoCommand.java linhas 37-41 para o padrão de record com campos tipados
public record MatricularAlunoRequest(
    @NotNull(message = "alunoId é obrigatório") String alunoId,
    @NotNull(message = "turmaId é obrigatório") String turmaId,
    @NotNull(message = "periodoInicio é obrigatório") LocalDate periodoInicio,
    @NotNull(message = "periodoFim é obrigatório") LocalDate periodoFim
) {}
```

---

### `interfaces/ExcecaoHandler.java` (middleware, request-response)

**Analog:** `dominio/excecao/LimiteDisciplinasExcedidoException.java` + todas as exceções tipadas em `dominio/excecao/`

O handler captura as exceções de domínio já implementadas. O padrão de campos estruturados de `LimiteDisciplinasExcedidoException.java` define como a resposta 422 com `limite`/`atual` é montada.

**Exceções disponíveis para mapear** (todas em `dominio/excecao/`):

| Exceção | Campos extras disponíveis | Status |
|---------|--------------------------|--------|
| `DisciplinaJaMatriculadaException` | `getDisciplina()`, `getMatriculaId()` | 409 |
| `MatriculaCanceladaException` | — | 409 |
| `LimiteDisciplinasExcedidoException` (linhas 33-39) | `getLimite()`, `getAtual()`, `getMatriculaId()` | 422 |
| `AlunoInativoException` (linhas 25-28) | `getAlunoId()` | 422 |
| `MatriculaNaoEncontradaException` (linhas 28-31) | `getMatriculaId()` | 404 |

**Padrão de mensagem de erro** — extraído de `LimiteDisciplinasExcedidoException.java` (linha 34):
```java
// LimiteDisciplinasExcedidoException.java linha 34
super("Limite de " + limite + " disciplinas excedido. Atual: " + atual
    + ". Matrícula: " + matriculaId.valor());
// e.getMessage() retorna exatamente esta String no handler
```

**Padrão de resposta de erro** — campo `"erro"` em português (D-06):
```java
// Formato: {erro: "SNAKE_CASE_UPPER", mensagem: "texto legível"}
// Usar record Java 21 para ErroResponse — consistente com MatricularAlunoCommand (record)
public record ErroResponse(String erro, String mensagem) {}
public record ErroLimiteResponse(String erro, String mensagem, int limite, int atual) {}
public record ErroCamposResponse(String erro, String mensagem, List<CampoErro> campos) {}
public record CampoErro(String campo, String mensagem) {}
```

**Padrão de Logger** — copiado de `DemoRunner.java` (linha 50) e `FinanceiroEventListener.java` (linha 33):
```java
// DemoRunner.java linha 50 — padrão de Logger no projeto
private static final Logger log = LoggerFactory.getLogger(ExcecaoHandler.class);
```

---

### `erp-matricula-app/pom.xml` (config — modificação)

**Analog:** si mesmo — `erp-matricula-app/pom.xml`

**Padrão de dependência existente** (linhas 50-55) — copiar o estilo de declaração:
```xml
<!-- pom.xml linhas 50-55 — padrão: sem versão para dependências gerenciadas pelo BOM -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
```

**Dependências a adicionar** — sem versão (gerenciadas pelo BOM `spring-boot-starter-parent 3.5.3`):
```xml
<!-- Adicionar APÓS spring-boot-starter (linha 55 do pom.xml atual) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**Remover o comentário pedagógico** da linha 8 do pom.xml ("NOTA: spring-boot-starter-web NÃO está incluído intencionalmente") — este ponto pedagógico da Fase 3 deve ser substituído pela adição da dependência com comentário explicando a Fase 4.

---

### `erp-matricula-app/src/main/resources/application.yml` (config — modificação)

**Analog:** si mesmo — `application.yml`

**Linha a remover** — `application.yml` linhas 39-40:
```yaml
  main:
    web-application-type: none
```

**Comentário a remover** — `application.yml` linhas 33-38 (bloco pedagógico sobre `web-application-type: none`). Substituir pelo comentário inverso: a Fase 4 ativa o servidor HTTP.

**Padrão de configuração** — `application.yml` linha 18 mostra o estilo de datasource que será sobrescrito pela variável de ambiente `SPRING_DATASOURCE_URL` no Docker Compose:
```yaml
# application.yml linha 18 — permanece para desenvolvimento local
datasource:
  url: jdbc:postgresql://localhost:5432/erp_matricula
  username: matricula
  password: matricula
```

---

### `db/migration/V3__adicionar_adicionada_em.sql` (migration, batch)

**Analog:** `db/migration/V1__schema.sql`

**Padrão de cabeçalho SQL** — copiado de `V1__schema.sql` (linhas 1-16):
```sql
-- =============================================================================
-- V3__adicionar_adicionada_em.sql — Adiciona coluna adicionada_em em itens_matricula
--
-- Fase 4: D-04 — MatriculaDto.disciplinas precisa de {nome, adicionadaEm}.
-- DEFAULT NOW() garante retrocompatibilidade com seeds V2 (itens existentes
-- recebem o timestamp de aplicação da migration, não null).
-- =============================================================================
```

**Padrão de ALTER TABLE** — sem análogo direto no projeto (V1 e V2 só fazem CREATE/INSERT), mas seguir o estilo de comentário do V1:
```sql
ALTER TABLE itens_matricula
    ADD COLUMN adicionada_em TIMESTAMP NOT NULL DEFAULT NOW();

COMMENT ON COLUMN itens_matricula.adicionada_em
    IS 'Timestamp de quando a disciplina foi adicionada à matrícula — Fase 4, D-04';
```

---

### `infraestrutura/persistencia/ItemMatriculaRow.java` (model — modificação)

**Analog:** `infraestrutura/persistencia/MatriculaRow.java` (exact match de padrão)

**Padrão de campo público** — copiado de `MatriculaRow.java` (linhas 28-72):
```java
// MatriculaRow.java linhas 57-64 — padrão de campo nullable com Javadoc
/**
 * Data/hora do cancelamento — coluna {@code matriculas.cancelada_em}.
 * {@code null} quando status != "CANCELADA".
 */
public LocalDateTime canceladaEm;
```

**Campo a adicionar em `ItemMatriculaRow.java`** — seguir o mesmo padrão:
```java
// Adicionar em ItemMatriculaRow.java após o campo 'disciplina'
/** Timestamp de quando a disciplina foi adicionada — coluna {@code itens_matricula.adicionada_em} */
public java.time.LocalDateTime adicionadaEm;
```

**Import a adicionar** em `ItemMatriculaRow.java` (atualmente só tem `import java.util.UUID;`):
```java
import java.time.LocalDateTime;
```

---

### `infraestrutura/persistencia/MatriculaRowMapper.java` (utility, transform — modificação)

**Analog:** si mesmo — `MatriculaRowMapper.java`

**Padrão de mapeamento item→domínio** — `MatriculaRowMapper.java` linha 76:
```java
// MatriculaRowMapper.java linhas 75-77 — a ser atualizado para incluir adicionadaEm
var disciplinas = row.itens.stream()
    .map(item -> new ItemMatricula(new NomeDisciplina(item.disciplina)))
    .toList();
```

**Padrão de mapeamento item→row** — `MatriculaRowMapper.java` linhas 147-153:
```java
// MatriculaRowMapper.java linhas 147-153 — itemsFromDomain não precisa de adicionadaEm
// (adicionada_em usa DEFAULT NOW() no banco — não é passada no INSERT)
public List<ItemMatriculaRow> itemsFromDomain(MatriculaId matriculaId, List<ItemMatricula> items) {
    return items.stream().map(item -> {
        var row = new ItemMatriculaRow();
        row.matriculaId = matriculaId.valor();
        row.disciplina  = item.disciplina().valor();
        return row;
    }).toList();
}
```

**Atualização necessária:** o `toDomain` (linha 76) deve passar `item.adicionadaEm` para o `ItemMatricula` — mas somente se o construtor de `ItemMatricula` for atualizado para aceitar `LocalDateTime`. Verificar `ItemMatricula.java` antes de modificar o mapper.

---

### `resources/mapper/MatriculaMapper.xml` (config — modificação)

**Analog:** si mesmo — `resources/mapper/MatriculaMapper.xml`

**Padrão de ResultMap com collection** — ver arquivo XML existente. O campo `adicionada_em` deve ser adicionado ao `<result>` dentro do `<collection>` de itens:
```xml
<!-- Adicionar dentro do <collection> de ItemMatriculaRow no ResultMap existente -->
<result property="adicionadaEm" column="item_adicionada_em"/>
```

O alias SQL da query de busca deve incluir `i.adicionada_em AS item_adicionada_em`.

---

### `aplicacao/MatriculaDto.java` (utility, transform — modificação)

**Analog:** si mesmo — `aplicacao/MatriculaDto.java`

**Padrão atual do record** — `MatriculaDto.java` linhas 26-30:
```java
// MatriculaDto.java linhas 26-30 — estilo de record com Javadoc por campo
public record MatriculaDto(
        String matriculaId,
        String alunoId,
        String statusDescricao,
        int totalDisciplinas) {
```

**Adição necessária:** novo campo `List<ItemDto> disciplinas` + record interno `ItemDto`:
```java
// Adicionar ao record MatriculaDto:
public record MatriculaDto(
        String matriculaId,
        String alunoId,
        String statusDescricao,
        int totalDisciplinas,
        List<ItemDto> disciplinas) {   // NOVO — D-04

    // Record aninhado — padrão Java 21, sem Lombok
    public record ItemDto(String nome, LocalDateTime adicionadaEm) {}
```

**Padrão do factory method** — `MatriculaDto.java` linhas 44-57. O `de(Matricula)` deve mapear `matricula.getDisciplinas()` para `List<ItemDto>`.

---

### `Dockerfile` (config — novo)

**Sem analog no projeto.** Usar padrão do RESEARCH.md.

**Padrão a seguir** (RESEARCH.md "Pattern 3: Dockerfile Multi-Stage"):
- Stage 1: `eclipse-temurin:21-jdk-alpine` com `apk add --no-cache maven` (Maven Wrapper ausente — Assumption A2 do RESEARCH.md)
- Stage 2: `eclipse-temurin:21-jre-alpine` copiando apenas o JAR
- JAR final em `erp-matricula-app/target/*.jar` (single-module Maven)
- `ENTRYPOINT ["java", "-jar", "app.jar"]`

---

### `docker-compose.yml` (config — novo)

**Sem analog no projeto.** Usar padrão do RESEARCH.md.

**Padrão a seguir** (RESEARCH.md "docker-compose.yml com Healthcheck"):
- `postgres:16-alpine` com `healthcheck: pg_isready`
- `depends_on: condition: service_healthy`
- Credenciais com defaults: `${DB_USER:-matricula}`, `${DB_PASSWORD:-matricula}`
- `SPRING_DATASOURCE_URL` sobrescreve `application.yml` via convenção Spring Boot
- Porta `8080:8080`

---

### `docs/04-material-didatico/ddd-vs-camadas.md` (documentation)

**Analog:** `docs/adrs/ADR-001-mybatis-vs-jpa.md` (mesmo padrão de comparação lado a lado com código)

**Padrão de estrutura do ADR-001** (linhas 1-39) — usar como template:
- Seção de contexto explicando o problema
- Bloco de código "COM JPA (PROBLEMA)" vs "COM MYBATIS (DECISÃO)"
- Análise de trade-offs

**Fontes de exemplos concretos** (RESEARCH.md "Para ddd-vs-camadas.md"):

| Artefato Tradicional | Arquivo DDD | Linhas |
|---------------------|-------------|--------|
| `@Service MatriculaService` com `if/else` | `Matricula.java` | 183-203 |
| `@Service` com lógica | `VerificadorElegibilidadeMatricula.java` | completo |
| `@Repository extends JpaRepository` | `dominio/repositorio/MatriculaRepositorio.java` | completo |
| `@Entity @Table @Column` | `dominio/modelo/Matricula.java` (ausência de anotações) | linha 44 |
| `service.save(entity)` | `aplicacao/MatricularAlunoUseCase.java` | 89-108 |
| `status == "ATIVA"` (String magic) | `aplicacao/MatriculaDto.java` | 44-49 |

---

### `docs/04-material-didatico/guia-consulta.md` (documentation)

**Analog:** `docs/02-design-tatico/modelagem.md` (tabelas Markdown referenciando código)

**Inventário completo para a tabela** (RESEARCH.md "Para guia-consulta.md"):

| Conceito DDD | Arquivo | O que observar |
|-------------|---------|----------------|
| Aggregate Root | `dominio/modelo/Matricula.java` | Métodos `adicionarDisciplina()` e `cancelar()` encapsulando invariantes |
| Value Object (tipado) | `dominio/vo/AlunoId.java` | Record com validação no construtor compacto |
| Value Object (com lógica) | `dominio/vo/Cpf.java` | Validação de dígito verificador embutida no construtor compacto |
| Value Object (período) | `dominio/vo/PeriodoLetivo.java` | Abstração de (ano, semestre) sem acoplamento a datas |
| Entidade | `dominio/modelo/Aluno.java` | `equals`/`hashCode` por identidade (AlunoId), não por atributos |
| Estado como Sealed Interface | `dominio/modelo/StatusMatricula.java` | Pattern matching exaustivo sem `default` |
| Domain Event | `dominio/evento/AlunoMatriculado.java` | Record imutável com dados do fato |
| Interface de Repositório | `dominio/repositorio/MatriculaRepositorio.java` | Sem imports de framework — só tipos do domínio |
| Domain Service | `dominio/servico/VerificadorElegibilidadeMatricula.java` | Lógica que cruza múltiplos Aggregates |
| Exceção tipada com dados | `dominio/excecao/LimiteDisciplinasExcedidoException.java` | Campos `limite` e `atual` estruturados |
| Application Service / UseCase | `aplicacao/MatricularAlunoUseCase.java` | Orquestra sem decidir — Javadoc explica cada passo |
| Command | `aplicacao/MatricularAlunoCommand.java` | Intenção de escrita em objeto imutável |
| DTO de leitura | `aplicacao/MatriculaDto.java` | Factory method `de(Matricula)` |
| Impl do Repositório | `infraestrutura/persistencia/MatriculaRepositorioMyBatis.java` | Dependency Inversion: domínio define, infra implementa |
| TypeHandler | `infraestrutura/persistencia/typehandler/CpfTypeHandler.java` | Conversão VO Cpf ↔ JDBC VARCHAR |
| RowMapper (fronteira domínio/persistência) | `infraestrutura/persistencia/MatriculaRowMapper.java` | ÚNICO arquivo que conhece ambos os modelos |
| Modelo relacional (Row) | `infraestrutura/persistencia/MatriculaRow.java` | Espelho plano do banco, sem comportamento |
| Listener de Domain Event (stub) | `infraestrutura/eventos/FinanceiroEventListener.java` | `@TransactionalEventListener` — só após commit |
| Controller REST | `interfaces/MatriculaController.java` | Traduz HTTP → Command; zero lógica de negócio |
| Handler de Exceções | `interfaces/ExcecaoHandler.java` | Única fronteira domínio → HTTP |

---

### `docs/04-material-didatico/licoes-aprendidas.md` (documentation)

**Analog:** `docs/adrs/ADR-002-escopo-bounded-context.md` (mesmo padrão: contexto → decisão → consequências positivas e negativas)

**Padrão de estrutura do ADR-002** (linhas 1-85) — "Consequências / Positivas" e "Negativas (Trade-offs)" deve ser o modelo para a seção de trade-offs honestos.

**Fontes de exemplos concretos:**
- `aplicacao/MatricularAlunoUseCase.java` linhas 27-40 (Javadoc: DDD vs arquitetura tradicional)
- `dominio/excecao/LimiteDisciplinasExcedidoException.java` (campos estruturados vs mensagem String)
- `infraestrutura/persistencia/MatriculaRowMapper.java` (mapeamento explícito vs JPA implícito)

---

### `docs/04-material-didatico/estrutura-pastas.md` (documentation)

**Analog:** `docs/adrs/ADR-001-mybatis-vs-jpa.md` (explica "por que" cada decisão, não só "o quê")

**Estrutura de pacotes real** (confirmada pelo `find` nas fontes):
```
erp-matricula-app/src/main/java/br/com/escola/matricula/
├── dominio/excecao/        — 7 exceções tipadas
├── dominio/evento/         — 3 Domain Events (records)
├── dominio/modelo/         — 5 classes (Matricula, Aluno, Turma, ItemMatricula, StatusMatricula)
├── dominio/repositorio/    — 1 interface (MatriculaRepositorio)
├── dominio/servico/        — 1 Domain Service (VerificadorElegibilidadeMatricula)
├── dominio/vo/             — 6 Value Objects (AlunoId, Cpf, MatriculaId, NomeDisciplina, PeriodoLetivo, TurmaId)
├── aplicacao/              — 7 arquivos (3 UseCases, 3 Commands, 1 DTO)
├── infraestrutura/config/  — 3 arquivos (DemoRunner, DomainServicesConfig, MyBatisConfig)
├── infraestrutura/eventos/ — 2 listeners stub
├── infraestrutura/persistencia/ — 5 arquivos (MatriculaRow, ItemMatriculaRow, MatriculaMapper, MatriculaRepositorioMyBatis, MatriculaRowMapper)
├── infraestrutura/persistencia/typehandler/ — 2 TypeHandlers (CpfTypeHandler, UUIDTypeHandler)
└── interfaces/             — 2 arquivos (MatriculaController, ExcecaoHandler) [Fase 4]
```

---

### `docs/adrs/ADR-001..004.md` (documentation — enriquecimento)

**Analog:** si mesmo — ADR existente.

**Padrão de enriquecimento:** adicionar seção `## Na prática` ao final de cada ADR (após `## Referências`), seguindo o padrão de blocos de código inline do ADR-001 (linhas 12-38).

**Links concretos por ADR:**
- ADR-001: `erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRow.java` e `MatriculaRowMapper.java`
- ADR-002: `infraestrutura/eventos/FinanceiroEventListener.java` e `AcademicoEventListener.java`
- ADR-003: `dominio/modelo/Matricula.java` campo `AlunoId alunoId` e `TurmaId turmaId` (sem FK)
- ADR-004: exemplos concretos de nomes em português do código implementado (`MatricularAlunoUseCase`, `VerificadorElegibilidadeMatricula`, `adicionarDisciplina()`, `cancelar()`)

---

### `README.md` (documentation — modificação)

**Analog:** si mesmo — `README.md` (linhas 1-57 já lidos)

**Padrão de seção existente** — `README.md` linhas 19-43 mostram o padrão de listas com links Markdown. Novas seções seguem o mesmo estilo.

**Seções a adicionar:**
1. "Como executar (Docker)" — `docker compose up` como único pré-requisito
2. "Material Didático" — links para `docs/04-material-didatico/*.md`
3. Atualizar "Como executar" (linha 55-56: "serão adicionadas na Fase 3") para a instrução real

---

## Shared Patterns

### Padrão de Injeção por Construtor (sem @Autowired)

**Fonte:** `MatricularAlunoUseCase.java` linhas 69-76, `AdicionarDisciplinaUseCase.java` linhas 54-58, `CancelarMatriculaUseCase.java` linhas 49-53, `DemoRunner.java` linhas 56-62
**Aplicar em:** `MatriculaController.java`, `ExcecaoHandler.java`
```java
// Padrão universal no projeto — Spring injeta automaticamente com construtor único
public MinhaClasse(DependenciaA a, DependenciaB b) {
    this.a = a;
    this.b = b;
}
```

### Padrão de Logger

**Fonte:** `DemoRunner.java` linha 50, `FinanceiroEventListener.java` linha 33
**Aplicar em:** `MatriculaController.java`, `ExcecaoHandler.java`
```java
private static final Logger log = LoggerFactory.getLogger(NomeDaClasse.class);
```

### Padrão de Records Java 21 (sem Lombok)

**Fonte:** `MatricularAlunoCommand.java` linhas 37-41, `MatriculaDto.java` linhas 26-30, `dominio/vo/Cpf.java` linhas 24-56
**Aplicar em:** Classes de request (`MatricularAlunoRequest`, `AdicionarDisciplinaRequest`), classes de resposta de erro (`ErroResponse`, `ErroLimiteResponse`), `ItemDto` no `MatriculaDto`
```java
// Imutabilidade garantida pela linguagem — sem setters, campos final automáticos
public record NomeRecord(
        TipoCampo campo1,
        TipoCampo campo2) {
}
```

### Padrão de Javadoc Pedagógico

**Fonte:** `MatricularAlunoUseCase.java` linhas 11-50, `MatriculaRowMapper.java` linhas 17-44, `Aluno.java` linhas 8-34
**Aplicar em:** `MatriculaController.java` (Javadoc: "única fronteira HTTP→domínio para tradução"), `ExcecaoHandler.java` (Javadoc obrigatório: D-06 — "única fronteira onde exceções de domínio se tornam respostas HTTP")

O padrão é: Javadoc explica **por que** a classe existe no contexto DDD, não apenas **o quê** ela faz.

### Padrão de Comentário inline de Sequência

**Fonte:** `MatricularAlunoUseCase.java` linhas 91-107 (comentários `// 1.`, `// 2.`, `// 3.`, `// 4.`)
**Aplicar em:** Método principal de cada endpoint do `MatriculaController` — descreve a sequência de tradução HTTP → Command → UseCase → DTO response

### Padrão de Tratamento de Erros (propagação natural)

**Fonte:** `AdicionarDisciplinaUseCase.java` linhas 70-85 e `CancelarMatriculaUseCase.java` linhas 63-77
**Aplicar em:** `MatriculaController.java` — os métodos dos endpoints NÃO têm try/catch. Exceções de domínio propagam naturalmente ao `ExcecaoHandler`. Apenas o handler tem a lógica de mapeamento HTTP.

---

## No Analog Found

| Arquivo | Role | Data Flow | Motivo |
|---------|------|-----------|--------|
| `Dockerfile` | config | — | Nenhum Dockerfile existe no projeto |
| `docker-compose.yml` | config | — | Nenhum arquivo de composição Docker existe no projeto |

Para estes arquivos, o planejador deve usar os padrões do RESEARCH.md (seção "Pattern 3: Dockerfile Multi-Stage" e "docker-compose.yml com Healthcheck").

---

## Metadata

**Escopo de busca de analogs:**
- `erp-matricula-app/src/main/java/br/com/escola/matricula/` (todos os pacotes)
- `erp-matricula-app/src/main/resources/db/migration/`
- `docs/adrs/`
- `README.md`

**Arquivos escaneados:** 43 arquivos Java + 4 ADRs + 2 SQL migrations + 1 XML + 1 YAML + 1 README

**Data de extração de padrões:** 2026-06-20

**Notas críticas para o planejador:**

1. **CPF placeholder no Controller:** `new Cpf("52998224725")` — CPF de Maria Silva do seed V2. É um placeholder intencional porque o endpoint não recebe CPF. O Javadoc deve explicar isso (ver RESEARCH.md "Ponto Crítico").

2. **DemoRunner:** deve ser desativado ou protegido com try/catch para evitar falha na segunda execução (índice único `uq_matricula_aluno_periodo_ativa`). O RESEARCH.md (Assumption A3) documenta o problema.

3. **Maven Wrapper:** provavelmente ausente (Assumption A2). O Dockerfile deve usar `apk add --no-cache maven` no stage de build em vez de `./mvnw`.

4. **Ordem de operações para MatriculaDto:** Flyway V3 → `ItemMatriculaRow.java` → `MatriculaMapper.xml` → `MatriculaRowMapper.java` → `MatriculaDto.java`. Essa sequência deve ser respeitada nos planos de execução.
