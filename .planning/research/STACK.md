# Stack Research — Java DDD Didático

**Projeto:** ERP Matrícula — Projeto Didático DDD
**Pesquisado em:** 2026-06-20
**Confiança geral:** ALTA (verificado em Maven Central + documentação oficial + Context7)

---

## Java 21 para DDD

### Records como Value Objects

Records são a implementação nativa de Value Objects no Java moderno. Um Value Object DDD tem três propriedades: imutabilidade, igualdade estrutural (por valor, não por identidade) e ausência de identidade própria. Records entregam as três automaticamente.

**Mapeamento direto:**

```java
// Value Object: CPF
public record Cpf(String valor) {
    public Cpf {
        if (valor == null || !valor.matches("\\d{11}"))
            throw new IllegalArgumentException("CPF inválido: " + valor);
    }
}

// Value Object: PeriodoLetivo
public record PeriodoLetivo(int ano, int semestre) {
    public PeriodoLetivo {
        if (semestre < 1 || semestre > 2)
            throw new IllegalArgumentException("Semestre deve ser 1 ou 2");
    }
}

// Value Object com comportamento
public record Carga(int horas) {
    public Carga {
        if (horas <= 0) throw new IllegalArgumentException("Carga deve ser positiva");
    }
    public boolean excede(Carga outra) { return this.horas > outra.horas; }
}
```

**Por que pedagogicamente valioso:** O aluno vê que o compilador garante imutabilidade (`final` implícito em todos os campos), que `equals()` e `hashCode()` funcionam por valor sem código manual, e que o bloco `compact constructor` é o lugar natural para invariantes. Isso contrasta diretamente com o mundo JPA onde Value Objects viram `@Embeddable` e perdem essas garantias.

**Validação (Context7):** Confirmado na documentação Spring Boot 3.3+: records são suportados como beans de configuração com `@ConfigurationProperties` e como componentes de domínio sem nenhuma anotação especial.

**Confiança:** ALTA.

---

### Sealed Classes para Estados de Domínio

Sealed classes permitem modelar o ciclo de vida de um agregado como um tipo fechado e exaustivo. O compilador garante que todos os estados sejam tratados em cada `switch`.

**Mapeamento ao domínio Matrícula:**

```java
// Status da Matrícula como tipo selado
public sealed interface StatusMatricula
    permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida {

    record Ativa(LocalDate dataInicio) implements StatusMatricula {}
    record Cancelada(LocalDate dataCancelamento, String motivo) implements StatusMatricula {}
    record Concluida(LocalDate dataConclusao) implements StatusMatricula {}
}
```

```java
// Uso no agregado: switch exaustivo obrigatório pelo compilador
public String descricaoStatus(StatusMatricula status) {
    return switch (status) {
        case StatusMatricula.Ativa a    -> "Ativa desde " + a.dataInicio();
        case StatusMatricula.Cancelada c -> "Cancelada em " + c.dataCancelamento();
        case StatusMatricula.Concluida c -> "Concluída em " + c.dataConclusao();
    };
}
```

**Por que pedagogicamente valioso:** Demonstra que o compilador pode enforçar regras de negócio (sem estado desconhecido possível), e que tipos soma (sum types) são melhores que enums para estados com dados. O aluno que vinha de `enum Status { ATIVA, CANCELADA }` entende imediatamente a limitação que sealed classes resolvem.

**Confiança:** ALTA.

---

### Pattern Matching para Regras de Negócio

Java 21 estabilizou pattern matching para `switch` (JEP 441) e record patterns (JEP 440). Para DDD, a aplicação mais direta é em Domain Services e Application Services que precisam inspecionar estados sem `instanceof` cascateado.

```java
// Domain Service: regra de negócio que depende do estado atual
public class MatriculaService {
    public void cancelar(Matricula matricula, String motivo) {
        switch (matricula.status()) {
            case StatusMatricula.Ativa a ->
                matricula.cancelar(motivo); // transição válida
            case StatusMatricula.Cancelada c ->
                throw new MatriculaJaCanceladaException(matricula.id());
            case StatusMatricula.Concluida c ->
                throw new MatriculaConcluidaException(matricula.id());
        }
    }
}
```

**Confiança:** ALTA — finalizado no Java 21 LTS (JEP 440, JEP 441).

---

### Virtual Threads (Project Loom) — Relevância para Didático

Java 21 estabilizou Virtual Threads. Para um projeto didático com MyBatis (I/O bloqueante por definição), Virtual Threads melhoram throughput sem mudar o código. Em Spring Boot 3.2+, habilitado com uma linha:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**Recomendação para o projeto:** Habilitar, mas não ser o foco do didático. Mencionar como "benefício gratuito do Java 21 com Spring Boot 3.2+".

**Confiança:** ALTA.

---

## Spring Boot 3.x para DDD

### Estrutura de Pacotes: A Regra de Dependência

A regra fundamental: **a camada de domínio não pode importar nenhuma classe das camadas de infraestrutura ou aplicação**. Spring Boot não enforça isso por padrão — é responsabilidade da estrutura de pacotes.

**Estrutura de pacotes recomendada (single-module):**

```
src/main/java/br/com/escola/matricula/
├── dominio/                          # Zero dependências Spring
│   ├── modelo/
│   │   ├── Matricula.java            # Aggregate Root
│   │   ├── ItemMatricula.java        # Entity dentro do agregado
│   │   ├── Aluno.java                # Entity (referência por AlunoId)
│   │   └── AlunoId.java              # Value Object (record)
│   ├── valorobj/
│   │   ├── Cpf.java                  # Value Object (record)
│   │   ├── PeriodoLetivo.java        # Value Object (record)
│   │   └── StatusMatricula.java      # sealed interface
│   ├── evento/
│   │   ├── AlunoMatriculado.java     # Domain Event (record)
│   │   ├── MatriculaCancelada.java   # Domain Event (record)
│   │   └── DisciplinaAdicionada.java # Domain Event (record)
│   ├── repositorio/
│   │   └── MatriculaRepositorio.java # Interface (porta de saída)
│   └── servico/
│       └── MatriculaDomainService.java
│
├── aplicacao/                        # Depende de dominio/. Spring @Service OK.
│   ├── usecase/
│   │   ├── MatricularAlunoUseCase.java
│   │   ├── AdicionarDisciplinaUseCase.java
│   │   └── CancelarMatriculaUseCase.java
│   └── dto/
│       ├── MatricularAlunoCommand.java
│       └── MatriculaResponse.java
│
├── infraestrutura/                   # Depende de dominio/ e aplicacao/
│   ├── persistencia/
│   │   ├── MatriculaRepositorioMyBatis.java  # Implementa MatriculaRepositorio
│   │   ├── MatriculaMapper.java              # Interface @Mapper
│   │   └── MatriculaResultMapper.java        # Objeto de leitura do banco
│   └── config/
│       └── MyBatisConfig.java
│
└── interfaces/                       # Controllers REST
    ├── MatriculaController.java
    └── GlobalExceptionHandler.java
```

**A fronteira crítica do didático:** A interface `MatriculaRepositorio` mora em `dominio/repositorio/`. A implementação `MatriculaRepositorioMyBatis` mora em `infraestrutura/persistencia/`. Isso demonstra a Inversão de Dependência: a infraestrutura depende do domínio, nunca o contrário.

**Por que pedagogicamente valioso:** O aluno que vem de arquitetura em camadas (onde `UserRepository extends JpaRepository<User, Long>` fica em qualquer lugar) vê concretamente que o repositório DDD é uma **interface de domínio** antes de ser uma implementação de infraestrutura.

---

### Configuração Spring Boot: Evitando Vazamento de Infraestrutura

**O problema central com JPA:** `@Entity`, `@Id`, `@Column`, `@ManyToOne` são anotações JPA que precisam ir nas classes de domínio. O modelo de domínio vira um mapa 1:1 do banco. MyBatis elimina esse problema completamente — as classes de domínio ficam POJO puro.

**Configuração application.yml mínima:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/matricula_db
    username: ${DB_USER:matricula}
    password: ${DB_PASS:matricula}
    driver-class-name: org.postgresql.Driver
  threads:
    virtual:
      enabled: true  # Project Loom — grátis no Java 21

mybatis:
  mapper-locations: classpath:mappers/**/*.xml
  type-aliases-package: br.com.escola.matricula.infraestrutura.persistencia
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 100
    default-statement-timeout: 30
    lazy-loading-enabled: false   # Explicitar: sem lazy loading surpresa
```

**Configuração @MapperScan:**

```java
@SpringBootApplication
@MapperScan("br.com.escola.matricula.infraestrutura.persistencia")
public class MatriculaApplication { ... }
```

**Anotar mappers com @Mapper** é equivalente, mas `@MapperScan` no ponto de entrada torna a configuração explícita e visível — importante para o didático.

**Confiança:** ALTA — verificado em Context7 + documentação mybatis-spring-boot-autoconfigure.

---

### Transaction Management

Use `@Transactional` nos Use Cases (camada de aplicação), nunca no repositório:

```java
@Service
@Transactional
public class MatricularAlunoUseCase {
    private final MatriculaRepositorio repositorio;
    // ...
}
```

**Por que:** A unidade transacional é o caso de uso, não o repositório. Um caso de uso pode orquestrar múltiplos repositórios na mesma transação. Isso é explícito no Spring e instrui o aluno corretamente.

---

## MyBatis para persistência de Aggregates

### Padrão Fundamental: Aggregate como Unidade de Leitura

O agregado `Matricula` possui `List<ItemMatricula>`. A reconstrução correta carrega o agregado inteiro em uma query — **não** em queries separadas por demanda.

**Padrão recomendado: JOIN + ResultMap com `<collection>`**

Este é o padrão correto para reconstruir um aggregate root com suas entidades filhas. Uma query SQL com JOIN, um ResultMap que usa `<collection>`, MyBatis desdobra as linhas duplicadas na coleção Java.

```xml
<!-- MatriculaMapper.xml -->
<mapper namespace="br.com.escola.matricula.infraestrutura.persistencia.MatriculaMapper">

  <resultMap id="MatriculaResultMap" type="MatriculaResultMapper">
    <id property="id" column="matricula_id"/>
    <result property="alunoId" column="aluno_id"/>
    <result property="periodoAno" column="periodo_ano"/>
    <result property="periodoSemestre" column="periodo_semestre"/>
    <result property="status" column="status"/>
    <result property="dataCriacao" column="data_criacao"/>
    <collection property="itens"
                ofType="ItemMatriculaResultMapper"
                notNullColumn="item_id">
      <id property="id" column="item_id"/>
      <result property="disciplinaId" column="disciplina_id"/>
      <result property="cargaHoras" column="carga_horas"/>
    </collection>
  </resultMap>

  <select id="buscarPorId" resultMap="MatriculaResultMap">
    SELECT
      m.id              AS matricula_id,
      m.aluno_id        AS aluno_id,
      m.periodo_ano     AS periodo_ano,
      m.periodo_semestre AS periodo_semestre,
      m.status          AS status,
      m.data_criacao    AS data_criacao,
      i.id              AS item_id,
      i.disciplina_id   AS disciplina_id,
      i.carga_horas     AS carga_horas
    FROM matriculas m
    LEFT JOIN itens_matricula i ON i.matricula_id = m.id
    WHERE m.id = #{id}
  </select>

</mapper>
```

**Por que usar objeto de leitura separado (`MatriculaResultMapper`) em vez do domínio diretamente:**

O `MatriculaResultMapper` é um POJO simples que espelha o resultado da query. O repositório converte esse objeto para o domínio:

```java
// MatriculaRepositorioMyBatis.java
@Override
public Optional<Matricula> buscarPorId(MatriculaId id) {
    return Optional.ofNullable(mapper.buscarPorId(id.valor()))
        .map(this::toDomain);
}

private Matricula toDomain(MatriculaResultMapper row) {
    var periodo = new PeriodoLetivo(row.periodoAno(), row.periodoSemestre());
    var status = StatusMatricula.parse(row.status()); // factory method
    var itens = row.itens().stream()
        .map(i -> new ItemMatricula(
            new ItemMatriculaId(i.id()),
            new DisciplinaId(i.disciplinaId()),
            new Carga(i.cargaHoras())))
        .toList();
    return new Matricula(new MatriculaId(row.id()), new AlunoId(row.alunoId()),
                         periodo, status, itens);
}
```

**Por que pedagogicamente valioso:** O aluno vê explicitamente a conversão entre modelo relacional e modelo de domínio. Não há magia de auto-mapeamento que esconde a separação. Esta é a pedagogia central do projeto: o espaço entre banco e domínio é visível e deliberado.

---

### Evitando N+1 com MyBatis

**O problema:** Usar `<collection select="...">` (nested select) executa 1 query para listar matrículas + N queries para buscar itens de cada matrícula.

```xml
<!-- ANTI-PADRÃO: nested select dispara N+1 -->
<resultMap id="MatriculaResultMap" type="MatriculaResultMapper">
  <collection property="itens"
              select="buscarItensPorMatriculaId"   <!-- dispara 1 query POR matricula -->
              column="matricula_id"/>
</resultMap>
```

**A solução correta: nested results com JOIN** (demonstrado acima). Uma query, uma viagem ao banco.

**Regra para o projeto didático:**

| Situação | Estratégia | Motivo |
|----------|-----------|--------|
| Buscar 1 Matrícula por ID | JOIN + `<collection>` nested results | Aggregate completo em 1 query |
| Listar Matrículas de 1 Aluno | JOIN + `<collection>` nested results | Mesma query, filtro diferente |
| Buscar só metadados (lista sem itens) | Query simples sem JOIN | Quando itens não são necessários |
| Persistir (INSERT/UPDATE) | Queries separadas na ordem certa | Aggregate root primeiro, filhos depois |

**Atenção: `notNullColumn` é obrigatório** no `<collection>` quando usa LEFT JOIN. Sem ele, MyBatis cria um item com todos os campos nulos quando a matrícula não tem itens, resultando em `List<ItemMatricula>` com um elemento fantasma.

**Confiança:** ALTA — verificado na documentação oficial MyBatis (Context7).

---

### TypeHandlers para Value Objects

Para mapear tipos de domínio customizados (CPF, Status selado), use TypeHandlers:

```java
// TypeHandler para StatusMatricula (sealed interface → String no banco)
@MappedTypes(StatusMatricula.class)
public class StatusMatriculaTypeHandler extends BaseTypeHandler<StatusMatricula> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    StatusMatricula status, JdbcType jdbcType) throws SQLException {
        ps.setString(i, toDb(status));
    }

    @Override
    public StatusMatricula getNullableResult(ResultSet rs, String col) throws SQLException {
        return fromDb(rs.getString(col));
    }
    // ... outros métodos obrigatórios da interface
}
```

**Registrar no application.yml:**

```yaml
mybatis:
  type-handlers-package: br.com.escola.matricula.infraestrutura.persistencia.typehandler
```

**Por que pedagogicamente valioso:** O aluno vê que a conversão entre domínio e banco é código explícito e testável — não anotações mágicas do ORM.

**Confiança:** ALTA — verificado em Context7 + documentação oficial MyBatis.

---

### Persistência de Aggregate: Ordem de Operações

Ao salvar `Matricula` (que contém `List<ItemMatricula>`):

```java
// MatriculaRepositorioMyBatis.java
@Override
public void salvar(Matricula matricula) {
    // 1. Aggregate root primeiro (FK constraint)
    mapper.upsertMatricula(toRow(matricula));
    // 2. Deletar itens existentes (estratégia replace-all)
    mapper.deletarItensPorMatriculaId(matricula.id().valor());
    // 3. Inserir itens atuais
    if (!matricula.itens().isEmpty()) {
        mapper.inserirItens(
            matricula.itens().stream()
                .map(item -> toItemRow(matricula.id(), item))
                .toList()
        );
    }
}
```

**Estratégia replace-all (delete + re-insert):** Mais simples para um didático. Evita rastrear quais itens foram adicionados, modificados ou removidos. O trade-off de performance (re-inserção desnecessária de itens não modificados) é aceitável para o volume de um projeto de treinamento. Documentar o trade-off explicitamente no código.

**Confiança:** MÉDIA — padrão estabelecido na comunidade, não documentado em fonte única oficial.

---

## PostgreSQL — Schema patterns

### Princípio: Uma tabela por Aggregate Root (mais tabelas de filhos)

O agregado `Matricula` com seus `ItemMatricula` mapeia para 2 tabelas:

```sql
-- Schema: matricula_escolar

CREATE TABLE alunos (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cpf         VARCHAR(11) NOT NULL UNIQUE,
    nome        VARCHAR(200) NOT NULL,
    email       VARCHAR(200) NOT NULL,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE matriculas (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id          UUID        NOT NULL,  -- Referência por ID, não FK para o agregado Aluno
    periodo_ano       SMALLINT    NOT NULL,
    periodo_semestre  SMALLINT    NOT NULL CHECK (periodo_semestre IN (1, 2)),
    status            VARCHAR(20) NOT NULL CHECK (status IN ('ATIVA', 'CANCELADA', 'CONCLUIDA')),
    data_criacao      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    data_atualizacao  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_matricula_aluno_periodo UNIQUE (aluno_id, periodo_ano, periodo_semestre)
);

CREATE TABLE itens_matricula (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    matricula_id    UUID        NOT NULL REFERENCES matriculas(id) ON DELETE CASCADE,
    disciplina_id   UUID        NOT NULL,   -- Referência por ID ao contexto Acadêmico
    carga_horas     SMALLINT    NOT NULL CHECK (carga_horas > 0),
    data_inclusao   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_item_matricula_disciplina UNIQUE (matricula_id, disciplina_id)
);

-- Índices para os padrões de acesso
CREATE INDEX idx_matriculas_aluno_id ON matriculas (aluno_id);
CREATE INDEX idx_itens_matricula_id  ON itens_matricula (matricula_id);
```

---

### Por que `aluno_id UUID` em vez de `FOREIGN KEY REFERENCES alunos(id)`

Este é um dos pontos pedagógicos mais importantes do esquema: `matriculas.aluno_id` referencia o agregado `Aluno` por ID, sem FK relacional entre as tabelas.

**Motivo DDD:** `Matricula` e `Aluno` são agregados de Bounded Contexts diferentes. A FK relacional cria um acoplamento no banco que contradiz a separação de contextos. Em produção real, `Aluno` poderia estar em outro banco. No projeto didático, estão no mesmo banco mas documentamos a decisão explicitamente (ADR).

**Documentar no schema:** Comentário SQL explicitando que é referência por ID entre agregados:

```sql
-- aluno_id: referência por ID ao Aggregate Aluno.
-- Deliberadamente sem FK — ver ADR-003: Referência entre Aggregates
COMMENT ON COLUMN matriculas.aluno_id IS 'ID do Aluno (referência entre agregados, sem FK relacional)';
```

---

### Value Objects como Colunas Inline

Value Objects DDD **não ganham tabelas próprias**. `PeriodoLetivo(ano, semestre)` vira dois campos em `matriculas`. `Cpf` vira uma coluna `cpf VARCHAR(11)`.

```sql
-- PeriodoLetivo mapeado como colunas inline
periodo_ano       SMALLINT NOT NULL,
periodo_semestre  SMALLINT NOT NULL CHECK (periodo_semestre IN (1, 2)),

-- A constraint de unicidade captura a invariante de negócio:
-- um aluno não pode ter duas matrículas no mesmo período
CONSTRAINT uq_matricula_aluno_periodo UNIQUE (aluno_id, periodo_ano, periodo_semestre)
```

**Por que pedagogicamente valioso:** O aluno vê que o banco normaliza dados, mas não desnormaliza o modelo de domínio. `Matricula` ainda tem `PeriodoLetivo` como Value Object — a conversão é feita explicitamente no TypeHandler/ResultMapper, não por mágica de ORM.

---

### Seeds para Demonstração

```sql
-- seeds/01_alunos.sql
INSERT INTO alunos (id, cpf, nome, email) VALUES
  ('a1b2c3d4-0000-0000-0000-000000000001', '12345678901', 'Maria Silva', 'maria@escola.br'),
  ('a1b2c3d4-0000-0000-0000-000000000002', '98765432100', 'João Santos', 'joao@escola.br');

-- seeds/02_matriculas.sql
INSERT INTO matriculas (id, aluno_id, periodo_ano, periodo_semestre, status) VALUES
  ('b2c3d4e5-0000-0000-0000-000000000001',
   'a1b2c3d4-0000-0000-0000-000000000001', 2026, 1, 'ATIVA');
```

**Confiança:** ALTA — padrões SQL padrão PostgreSQL; schema design baseado em princípios DDD verificados.

---

## Maven — Estrutura de módulos

### Recomendação: Single-Module com Pacotes bem Definidos

**Para um projeto didático: single-module é a escolha certa.**

Multi-module resolve um problema real em produção (impedir que infraestrutura seja importada em domínio — o compilador bloqueia no nível de módulo Maven). Para um didático, o overhead supera o benefício:

| Critério | Single-Module | Multi-Module |
|----------|--------------|-------------|
| Clareza pedagógica | ALTA — estrutura visível na árvore de pacotes | MÉDIA — split entre projetos confunde iniciantes |
| Curva de entrada | Baixa — um `mvn spring-boot:run` | Alta — ordem de build, módulos pai/filho |
| Enforçamento de dependências | Só via convenção (documentada) | Via compilação (mais robusto) |
| Adequado para time treinando | SIM | Não recomendado no início |
| Adequado para produção | NÃO (sem enforçamento real) | SIM |

**Decisão:** Single-module. Documentar no README que a separação é por convenção, e mencionar multi-module como próximo passo natural em projetos reais.

---

### Estrutura pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.3</version>
        <relativePath/>
    </parent>

    <groupId>br.com.escola</groupId>
    <artifactId>matricula</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>matricula</name>
    <description>Projeto Didático DDD — Matrícula Escolar</description>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- MyBatis -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>3.0.5</version>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
            <!-- versão gerenciada pelo Spring Boot BOM: 42.7.x -->
        </dependency>

        <!-- Validação de entrada (camada de interface) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Flyway — migrações de schema -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
            <!-- versão gerenciada pelo Spring Boot BOM -->
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <!-- módulo necessário para PostgreSQL no Flyway 10+ -->
        </dependency>

        <!-- Testes -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

**Por que Flyway em vez de scripts SQL manuais:**

Flyway é mais simples que Liquibase (SQL puro, convenção de nomes `V1__descricao.sql`) e automaticamente inicializa o schema no `docker compose up`. Para o didático, o aluno não precisa gerenciar ordem de execução manual de scripts.

**Por que versão explícita para MyBatis:**

`mybatis-spring-boot-starter` **não está no BOM do Spring Boot** — precisa de versão explícita. As demais dependências (`postgresql`, `flyway-core`) são gerenciadas pelo `spring-boot-starter-parent` e não precisam de versão no POM.

**Confiança:** ALTA — versões verificadas no Maven Central em 2026-06-20.

---

## Recomendações

### Bibliotecas — Versões Verificadas

| Biblioteca | Versão | Fonte | Confiança |
|-----------|--------|-------|-----------|
| Spring Boot | 3.5.3 | Maven Central (2026-06-20) | ALTA |
| mybatis-spring-boot-starter | 3.0.5 | GitHub Releases + Maven Central | ALTA |
| MyBatis Core | 3.5.19 | Dependência transitiva do starter 3.0.5 | ALTA |
| Java | 21 LTS | Fixado no projeto | ALTA |
| PostgreSQL JDBC | 42.7.x | Gerenciado pelo Spring Boot BOM | ALTA |
| Flyway | 10.x | Gerenciado pelo Spring Boot BOM | ALTA |
| Docker Engine | 24+ | Requerido pelo Docker Compose v2 | MÉDIA |

---

### O que NÃO usar e por quê

| O que | Por que não |
|-------|------------|
| **JPA/Hibernate** | Vaza abstrações de persistência (`@Entity`, `@Id`, `@Column`) no modelo de domínio — contradiz o ponto pedagógico central do projeto |
| **Spring Data JPA** | Mesmo motivo. Repositórios que `extends JpaRepository` escondem a separação domínio/infraestrutura |
| **Spring Data JDBC** | Mais próximo de DDD que JPA, mas ainda impõe restrições ao modelo (anotações `@Table`, `@Id`). MyBatis é mais explícito e mais didático |
| **Lombok** | Esconde o boilerplate que o aluno precisa entender. Java 21 records eliminam a necessidade de Lombok para Value Objects |
| **MapStruct** | Geração de código de mapeamento esconde a conversão domínio/persistência que é explicitamente pedagógica aqui |
| **MyBatis-Plus** | Adiciona auto-CRUD que contradiz o mapeamento explícito do MyBatis puro — perde o ponto pedagógico |
| **Multi-module Maven** | Complexidade desnecessária para treinamento; usar pacotes bem definidos no single-module |
| **Liquibase** | Mais complexo que Flyway (XML/YAML changelogs) sem benefício proporcional para este projeto |

---

### Verificações de Confiança por Área

| Área | Confiança | Justificativa |
|------|-----------|---------------|
| Java 21 features (records, sealed, pattern matching) | ALTA | JEPs finalizados no Java 21 LTS; verificado em documentação JDK |
| Spring Boot 3.5.3 como versão corrente | ALTA | Maven Central consultado em 2026-06-20 |
| mybatis-spring-boot-starter 3.0.5 para Spring Boot 3.5 | ALTA | POM verificado no Maven Central; targets `spring-boot.version=3.5.0` |
| Padrão JOIN + ResultMap `<collection>` para aggregates | ALTA | Documentação oficial MyBatis (Context7) + padrão estabelecido |
| TypeHandlers para Value Objects | ALTA | Documentação oficial MyBatis |
| Schema PostgreSQL (design de tabelas) | ALTA | Padrões SQL padrão + princípios DDD verificados |
| Single-module vs multi-module para didático | MÉDIA | Consenso da comunidade; sem fonte única autoritativa |
| Estratégia replace-all para persistência de coleções | MÉDIA | Padrão prático comum; não documentado em especificação |
| Virtual Threads + Spring Boot 3.2+ | ALTA | Documentação Spring Boot; JEP 444 finalizado Java 21 |

---

### Referências

- MyBatis Result Maps (oficial): https://mybatis.org/mybatis-3/sqlmap-xml.html
- mybatis-spring-boot-starter releases: https://github.com/mybatis/spring-boot-starter/releases
- mybatis-spring-boot-autoconfigure: https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/
- Spring Boot 3.5 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes
- Separating Persistence and Domain Models: https://urgo.medium.com/separating-persistence-and-domain-models-cc3a7e7cd4e5
- DDD and Spring Boot Multi-Module Maven: https://dzone.com/articles/ddd-spring-boot-multi-module-maven-project
- Modern Java: Records, Sealed Classes, Pattern Matching: https://www.javacodegeeks.com/2025/12/modern-java-language-features-records-sealed-classes-pattern-matching.html
- Sealed Classes and Pattern Matching API Design: https://www.javacodegeeks.com/2026/04/sealed-classes-and-exhaustive-pattern-matching-how-they-change-api-design-not-just-syntax.html
