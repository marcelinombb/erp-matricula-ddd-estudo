# Phase 3: Implementacao — Research

**Researched:** 2026-06-20
**Domain:** Java 21 DDD — camadas domínio, aplicação e infraestrutura com MyBatis + PostgreSQL
**Confidence:** HIGH

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Estrutura de Pacotes**
- D-01: Pacote raiz `br.com.escola.matricula`
- D-02: Sub-pacotes do domínio: `dominio.modelo/`, `dominio.vo/`, `dominio.evento/`, `dominio.repositorio/`, `dominio.servico/`, `dominio.excecao/`
- D-03: Camada de aplicação flat: `aplicacao/` (sem sub-pacotes)
- D-04: Infraestrutura: `infraestrutura.persistencia/`, `infraestrutura.eventos/`, `infraestrutura.config/`
- D-05: `ErpMatriculaApplication.java` na raiz de `br.com.escola.matricula`

**Bootstrap**
- D-06: `groupId: br.com.escola`, `artifactId: erp-matricula`, `version: 0.1.0-SNAPSHOT`, Java 21, Spring Boot 3.5.3
- D-07: Starters: `spring-boot-starter` + `mybatis-spring-boot-starter:3.0.5` + `postgresql` + `flyway-core` + `spring-boot-starter-test`. SEM `spring-boot-starter-web`
- D-08: `application.yml` único, datasource `localhost:5432/erp_matricula`, Flyway `classpath:db/migration`, MyBatis `classpath:mapper/**/*.xml`

**Eventos**
- D-09: Aggregate `Matricula` coleta eventos em `List<Object> eventos`, método `coletarEventos()` retorna `List.copyOf(eventos)` e limpa. Zero Spring no domínio
- D-10: UseCase publica via `ApplicationEventPublisher` APÓS `repositorio.salvar()`. Ordem: validar → operar → salvar → publicar
- D-11: Domain Events são records independentes sem interface base. Lista interna é `List<Object>`

**Persistência**
- D-12: Estratégia replace-all: INSERT/UPDATE root → DELETE itens → INSERT itens
- D-13: `MatriculaRow` (campos planos da tabela) + `ItemMatriculaRow` + `MatriculaRowMapper` (conversão explícita)
- D-14: `PeriodoLetivo` → `periodo_inicio DATE` + `periodo_fim DATE`. `Cpf` → `cpf VARCHAR(11)`. IDs → `UUID`

### Claude's Discretion
- Número exato de seeds (mínimo: 1 Aluno ativo, 1 Turma com período aberto, 1 Matrícula existente)
- Abordagem exata do `PeriodoLetivoTypeHandler` para dois campos: TypeHandler separado por campo vs. mapeamento inline no XML ResultMap
- Configuração de transaction manager: padrão Spring Boot (DataSourceTransactionManager), sem customização

### Deferred Ideas (OUT OF SCOPE)
- Docker Compose (Fase 4)
- Controllers REST (Fase 4)
- Testes automatizados (fora do escopo v1)
- Optimistic locking completo (apenas nota no código)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| DOM-01 | Value Objects como Java 21 records com validação no construtor compacto | Seção Java 21 Features — records com compact constructor |
| DOM-02 | Entidade `Aluno` com `AlunoId` tipado; equals/hashCode por identidade | Seção Domain Layer — classe com campos final, sem record |
| DOM-03 | Entidade `Turma` com `TurmaId` tipado e capacidade máxima | Seção Domain Layer — mesmo padrão de Aluno |
| DOM-04 | Aggregate Root `Matricula` com invariantes encapsuladas | Seção Domain Layer — adicionarDisciplina() com 3 guards |
| DOM-05 | `StatusMatricula` como sealed interface | Seção Java 21 Features — sealed interface + record interno |
| DOM-06 | Domain Events como records imutáveis | Seção Domain Layer — AlunoMatriculado, DisciplinaAdicionada, MatriculaCancelada |
| DOM-07 | Mecanismo `coletarEventos()` sem Spring | Seção Domain Layer — List<Object> no Aggregate |
| DOM-08 | Domain Service `VerificadorElegibilidadeMatricula` | Seção Domain Layer — serviço puro sem Spring |
| DOM-09 | Interface `MatriculaRepositorio` no domínio, zero imports de framework | Seção Domain Layer — interface pura |
| DOM-10 | Exceções tipadas com contexto | Seção Domain Layer — campos estruturados (limite, atual) |
| APL-01 | `MatricularAlunoUseCase` | Seção Application Layer — UseCase com @Service + @Transactional |
| APL-02 | `AdicionarDisciplinaUseCase` | Seção Application Layer |
| APL-03 | `CancelarMatriculaUseCase` | Seção Application Layer |
| APL-04 | DTOs e Commands | Seção Application Layer — records de entrada/saída |
| APL-05 | Listeners stub com @TransactionalEventListener | Seção Application Layer — configuração sem web |
| INF-01 | Schema PostgreSQL completo | Seção PostgreSQL Schema |
| INF-02 | Flyway migrations | Seção Flyway Migrations |
| INF-03 | Seeds para 3 fluxos | Seção Flyway Migrations |
| INF-04 | `MatriculaMapper.xml` com JOIN + `<collection notNullColumn>` | Seção MyBatis Aggregate Reconstruction |
| INF-05 | TypeHandlers para Value Objects | Seção TypeHandlers |
| INF-06 | `MatriculaRowMapper` — conversão explícita Row ↔ domínio | Seção Infrastructure Layer |
| INF-07 | `MatriculaRepositorioMyBatis` implementando interface do domínio | Seção Infrastructure Layer |
</phase_requirements>

---

## Summary

Esta fase implementa as três camadas DDD (domínio, aplicação, infraestrutura) em Java 21 + Spring Boot 3.5.3 + MyBatis. A pilha está completamente verificada: versões de todas as dependências foram confirmadas em Maven Central. A ausência de `spring-boot-starter-web` é intencional — demonstra que DDD não depende de HTTP.

O desafio técnico central é o mapeamento MyBatis: o `MatriculaMapper.xml` deve reconstruir o Aggregate `Matricula` com sua `List<ItemMatricula>` em uma única query usando JOIN + `<collection notNullColumn>`. Sem `notNullColumn`, matrículas sem itens ganham um elemento fantasma na lista. Este é o pitfall mais frequente.

A decisão D-14 muda o schema de `PeriodoLetivo` comparado com o design tático: o design usa `ano + semestre` (int, int), mas D-14 define `periodo_inicio DATE + periodo_fim DATE`. O planner deve decidir entre (a) manter o VO com ano+semestre e mapear para DATE via TypeHandler, ou (b) atualizar o VO para `LocalDate inicio, LocalDate fim`. Esta inconsistência precisa ser resolvida na PLAN.md.

**Primary recommendation:** Implementar na ordem — domínio (sem Spring, sem compilar com contexto Spring), depois aplicação, depois infraestrutura. Verificar `grep -r "import org.springframework" src/main/java/br/com/escola/matricula/dominio/` após cada arquivo criado em `dominio/`.

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Invariantes de matrícula (limite, duplicidade, estado terminal) | Domain — Aggregate `Matricula` | — | O Aggregate protege suas próprias invariantes; sem verificação fora dele |
| Verificação de elegibilidade pré-matrícula | Domain — Domain Service `VerificadorElegibilidadeMatricula` | — | Lógica que cruza Aluno + PeriodoLetivo não pertence a um único Aggregate |
| Orquestração dos fluxos (buscar, operar, salvar, publicar) | Application — UseCases | — | Application Service apenas orquestra, não decide |
| Publicação de Domain Events | Application — UseCases via `ApplicationEventPublisher` | — | Após commit; domínio apenas coleta, não publica |
| Conversão Row ↔ Domínio | Infrastructure — `MatriculaRowMapper` | — | Única classe que conhece ambos os modelos |
| SQL e ResultMaps | Infrastructure — `MatriculaMapper` + XML | — | Detalhe de persistência isolado na infraestrutura |
| Schema e migrations | Infrastructure — Flyway (`db/migration/`) | — | Versionamento de DDL separado do código |
| Consumo de eventos (stubs) | Infrastructure — `infraestrutura.eventos/` | — | Listeners pertencem à infra, não à aplicação |

---

## 1. MyBatis Aggregate Reconstruction (INF-04)

### ResultMap XML com JOIN + collection

O padrão correto para reconstruir `Matricula` + `List<ItemMatricula>` em uma query. [VERIFIED: mybatis.org/mybatis-3/sqlmap-xml.html]

**Regra crítica:** `notNullColumn="item_disciplina"` é OBRIGATÓRIO quando usa LEFT JOIN. Sem ele, MyBatis cria um `ItemMatriculaRow` com todos os campos nulos para matrículas sem itens, resultando em uma lista com um elemento fantasma.

```xml
<!-- src/main/resources/mapper/MatriculaMapper.xml -->
<mapper namespace="br.com.escola.matricula.infraestrutura.persistencia.MatriculaMapper">

  <resultMap id="MatriculaResultMap" type="MatriculaRow">
    <id     property="id"            column="matricula_id"/>
    <result property="alunoId"       column="aluno_id"/>
    <result property="turmaId"       column="turma_id"/>
    <result property="periodoInicio" column="periodo_inicio"/>
    <result property="periodoFim"    column="periodo_fim"/>
    <result property="status"        column="status"/>
    <result property="canceladaEm"   column="cancelada_em"/>
    <result property="concluidaEm"   column="concluida_em"/>
    <collection property="itens"
                ofType="ItemMatriculaRow"
                notNullColumn="item_disciplina">
      <result property="disciplina"  column="item_disciplina"/>
    </collection>
  </resultMap>

  <select id="buscarPorId" resultMap="MatriculaResultMap">
    SELECT
      m.id             AS matricula_id,
      m.aluno_id       AS aluno_id,
      m.turma_id       AS turma_id,
      m.periodo_inicio AS periodo_inicio,
      m.periodo_fim    AS periodo_fim,
      m.status         AS status,
      m.cancelada_em   AS cancelada_em,
      m.concluida_em   AS concluida_em,
      i.disciplina     AS item_disciplina
    FROM matriculas m
    LEFT JOIN itens_matricula i ON i.matricula_id = m.id
    WHERE m.id = #{id, jdbcType=OTHER}
  </select>

  <select id="buscarPorAluno" resultMap="MatriculaResultMap">
    SELECT
      m.id             AS matricula_id,
      m.aluno_id       AS aluno_id,
      m.turma_id       AS turma_id,
      m.periodo_inicio AS periodo_inicio,
      m.periodo_fim    AS periodo_fim,
      m.status         AS status,
      m.cancelada_em   AS cancelada_em,
      m.concluida_em   AS concluida_em,
      i.disciplina     AS item_disciplina
    FROM matriculas m
    LEFT JOIN itens_matricula i ON i.matricula_id = m.id
    WHERE m.aluno_id = #{alunoId, jdbcType=OTHER}
  </select>

  <select id="existeMatriculaAtiva" resultType="boolean">
    SELECT COUNT(*) > 0
    FROM matriculas
    WHERE aluno_id = #{alunoId, jdbcType=OTHER}
      AND periodo_inicio = #{periodoInicio}
      AND periodo_fim    = #{periodoFim}
      AND status = 'ATIVA'
  </select>

  <insert id="inserirMatricula" parameterType="MatriculaRow">
    INSERT INTO matriculas
      (id, aluno_id, turma_id, periodo_inicio, periodo_fim, status)
    VALUES
      (#{id, jdbcType=OTHER},
       #{alunoId, jdbcType=OTHER},
       #{turmaId, jdbcType=OTHER},
       #{periodoInicio},
       #{periodoFim},
       #{status})
  </insert>

  <update id="atualizarMatricula" parameterType="MatriculaRow">
    UPDATE matriculas SET
      status       = #{status},
      cancelada_em = #{canceladaEm},
      concluida_em = #{concluidaEm}
    WHERE id = #{id, jdbcType=OTHER}
  </update>

  <delete id="deletarItensPorMatriculaId">
    DELETE FROM itens_matricula WHERE matricula_id = #{matriculaId, jdbcType=OTHER}
  </delete>

  <insert id="inserirItens">
    INSERT INTO itens_matricula (matricula_id, disciplina)
    VALUES
    <foreach collection="list" item="item" separator=",">
      (#{item.matriculaId, jdbcType=OTHER}, #{item.disciplina})
    </foreach>
  </insert>

</mapper>
```

**Por que `jdbcType=OTHER` para UUID:** PostgreSQL requer esse type hint para colunas UUID. Sem ele, MyBatis tenta passar como VARCHAR e o driver PostgreSQL pode recusar. [VERIFIED: mybatis.org/mybatis-3/sqlmap-xml.html]

**Anti-padrão a evitar — nested select:**
```xml
<!-- ERRADO: dispara N+1 queries -->
<collection property="itens"
            select="buscarItensPorMatriculaId"
            column="matricula_id"/>
```

---

## 2. TypeHandlers para Value Objects (INF-05)

### Abordagem: TypeHandler por campo simples + mapeamento inline para PeriodoLetivo

[VERIFIED: mybatis.org/mybatis-3/sqlmap-xml.html]

**CpfTypeHandler** — String (11 dígitos) ↔ `Cpf`:

```java
// src/main/java/br/com/escola/matricula/infraestrutura/persistencia/typehandler/CpfTypeHandler.java
package br.com.escola.matricula.infraestrutura.persistencia.typehandler;

import br.com.escola.matricula.dominio.vo.Cpf;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;

@MappedTypes(Cpf.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class CpfTypeHandler extends BaseTypeHandler<Cpf> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Cpf cpf, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, cpf.valor());
    }

    @Override
    public Cpf getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String valor = rs.getString(columnName);
        return valor != null ? new Cpf(valor) : null;
    }

    @Override
    public Cpf getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String valor = rs.getString(columnIndex);
        return valor != null ? new Cpf(valor) : null;
    }

    @Override
    public Cpf getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String valor = cs.getString(columnIndex);
        return valor != null ? new Cpf(valor) : null;
    }
}
```

**PeriodoLetivo — abordagem recomendada: mapeamento inline no ResultMap**

`PeriodoLetivo` tem dois campos (`ano int`, `semestre int`) que D-14 mapeia para duas colunas DATE (`periodo_inicio`, `periodo_fim`). Há uma inconsistência entre o design tático (ano+semestre) e D-14 (LocalDate inicio+fim). **O planner deve decidir antes de implementar:**

- **Opção A (manter VO original `ano+semestre`):** Mudar D-14 para usar `periodo_ano SMALLINT + periodo_semestre SMALLINT`. O TypeHandler fica simples (int ↔ SMALLINT). Recomendado — preserva a pedagogia do VO documentado em Fase 2.
- **Opção B (adotar D-14 com LocalDate):** Mudar o VO `PeriodoLetivo` para `record PeriodoLetivo(LocalDate inicio, LocalDate fim)`. Atualiza a documentação de Fase 2. Requer TypeHandler para cada campo DATE → LocalDate (já suportado nativamente pelo MyBatis).
- **Opção C (duas colunas DATE, mapeamento inline):** `MatriculaRow` carrega `LocalDate periodoInicio, LocalDate periodoFim`; `MatriculaRowMapper.toDomain()` constrói `PeriodoLetivo` a partir deles. Nenhum TypeHandler customizado para PeriodoLetivo — o mapeamento DATE↔LocalDate é nativo. **Esta é a abordagem de menor fricção com D-14.**

**Recomendação do planner:** Opção C (mapeamento inline). MatriculaRow já carrega os dois LocalDates; MatriculaRowMapper os combina ao construir o VO. Evita TypeHandler customizado para PeriodoLetivo enquanto preserva o VO no domínio.

**Registro de TypeHandlers no application.yml:**
```yaml
mybatis:
  type-handlers-package: br.com.escola.matricula.infraestrutura.persistencia.typehandler
```

**Alternativa via @Bean em MyBatisConfig:** Para TypeHandlers que precisam de dependências injetadas (este caso não se aplica — todos são stateless).

---

## 3. PostgreSQL Schema (INF-01)

### Schema completo alinhado com D-14

[VERIFIED: padrões SQL padrão PostgreSQL + princípios DDD verificados em STACK.md]

```sql
-- V1__schema.sql

-- Alunos (entidade de referência — não é um Aggregate desta Fase)
CREATE TABLE alunos (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    cpf         VARCHAR(11)  NOT NULL UNIQUE,
    nome        VARCHAR(200) NOT NULL,
    ativo       BOOLEAN      NOT NULL DEFAULT true,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  alunos          IS 'Referência ao Aggregate Aluno (BC Matrícula usa apenas AlunoId)';
COMMENT ON COLUMN alunos.cpf      IS 'CPF armazenado sem máscara (11 dígitos numéricos)';

-- Turmas (entidade de referência)
CREATE TABLE turmas (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome             VARCHAR(200) NOT NULL,
    periodo_inicio   DATE         NOT NULL,
    periodo_fim      DATE         NOT NULL,
    vagas_maximas    SMALLINT     NOT NULL CHECK (vagas_maximas > 0),
    criada_em        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE turmas IS 'Referência ao Aggregate Turma (BC Matrícula usa TurmaId)';

-- Matrículas — Aggregate Root
CREATE TABLE matriculas (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id        UUID         NOT NULL,
    -- Deliberadamente sem FK para alunos — referência por ID entre Aggregates. Ver ADR-003.
    turma_id        UUID         NOT NULL,
    -- Deliberadamente sem FK para turmas — mesma razão.
    periodo_inicio  DATE         NOT NULL,
    periodo_fim     DATE         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ATIVA'
                                 CHECK (status IN ('ATIVA', 'CANCELADA', 'CONCLUIDA')),
    cancelada_em    TIMESTAMPTZ,
    concluida_em    TIMESTAMPTZ,
    criada_em       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  matriculas           IS 'Aggregate Root: Matricula (BC Matrícula)';
COMMENT ON COLUMN matriculas.aluno_id  IS 'Referência por ID ao Aggregate Aluno — sem FK intencional (ADR-003)';
COMMENT ON COLUMN matriculas.turma_id  IS 'Referência por ID ao Aggregate Turma — sem FK intencional (ADR-003)';

CREATE UNIQUE INDEX uq_matricula_aluno_periodo
    ON matriculas (aluno_id, periodo_inicio, periodo_fim)
    WHERE status = 'ATIVA';

CREATE INDEX idx_matriculas_aluno_id ON matriculas (aluno_id);

-- Itens de matrícula — entidade interna do Aggregate Matricula
CREATE TABLE itens_matricula (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    matricula_id  UUID         NOT NULL REFERENCES matriculas(id) ON DELETE CASCADE,
    disciplina    VARCHAR(100) NOT NULL,
    incluida_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_item_disciplina_por_matricula UNIQUE (matricula_id, disciplina)
);

COMMENT ON TABLE  itens_matricula              IS 'Entidade interna do Aggregate Matricula — não referenciar diretamente';
COMMENT ON COLUMN itens_matricula.matricula_id IS 'FK para o Aggregate Root (matriculas.id) — ON DELETE CASCADE';

CREATE INDEX idx_itens_matricula_id ON itens_matricula (matricula_id);
```

**Decisões de schema a destacar no código:**
1. `aluno_id` e `turma_id` sem FK — ADR-003 (referência por ID entre Aggregates)
2. `itens_matricula` com FK para `matriculas` com CASCADE — entidade interna, ciclo de vida acoplado
3. Index condicional `WHERE status = 'ATIVA'` para unicidade — um aluno pode ter matrículas canceladas no mesmo período
4. `status` como VARCHAR com CHECK — persistência de `StatusMatricula` (sealed interface)
5. `cancelada_em` e `concluida_em` nullable — só existem nos estados correspondentes

---

## 4. Flyway Migrations (INF-02/03)

### Estrutura de arquivos

[VERIFIED: STACK.md — Flyway 10.x gerenciado pelo Spring Boot BOM]

```
src/main/resources/
├── db/
│   └── migration/
│       ├── V1__schema.sql     # INF-01: todas as CREATE TABLE
│       └── V2__seeds.sql      # INF-03: dados de demonstração
└── mapper/
    └── MatriculaMapper.xml    # INF-04
```

**IMPORTANTE para Flyway 10+ com PostgreSQL:** Requer `flyway-database-postgresql` como dependência adicional. [VERIFIED: STACK.md — seção pom.xml]

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <!-- versão gerenciada pelo Spring Boot BOM -->
</dependency>
```

**application.yml (datasource + flyway):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/erp_matricula
    username: matricula
    password: matricula
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration
```

**V2__seeds.sql — mínimo para cobrir 3 fluxos:**

```sql
-- Aluno ativo (para fluxo: matricular)
INSERT INTO alunos (id, cpf, nome, ativo)
VALUES ('a0000000-0000-0000-0000-000000000001', '12345678901', 'Maria Silva', true);

-- Aluno para matrícula pré-existente (para fluxos: adicionar, cancelar)
INSERT INTO alunos (id, cpf, nome, ativo)
VALUES ('a0000000-0000-0000-0000-000000000002', '98765432100', 'João Santos', true);

-- Turma com período aberto (para fluxo: matricular)
INSERT INTO turmas (id, nome, periodo_inicio, periodo_fim, vagas_maximas)
VALUES ('b0000000-0000-0000-0000-000000000001',
        'Turma 2026-1', '2026-02-01', '2026-07-31', 30);

-- Matrícula ATIVA pré-existente com 1 disciplina
-- (para fluxos: adicionar disciplina E cancelar)
INSERT INTO matriculas (id, aluno_id, turma_id, periodo_inicio, periodo_fim, status)
VALUES ('c0000000-0000-0000-0000-000000000001',
        'a0000000-0000-0000-0000-000000000002',
        'b0000000-0000-0000-0000-000000000001',
        '2026-02-01', '2026-07-31', 'ATIVA');

INSERT INTO itens_matricula (matricula_id, disciplina)
VALUES ('c0000000-0000-0000-0000-000000000001', 'Matemática Básica');
```

**Naming convention Flyway:** `V{versão}__{descricao}.sql` — dois underscores entre versão e descrição. [VERIFIED: STACK.md]

---

## 5. Spring Boot sem Web (Bootstrap)

### Main class sem Tomcat

[VERIFIED: STACK.md — spring-boot-starter sem web]

Sem `spring-boot-starter-web`, o contexto Spring sobe e fica ativo mas não abre porta HTTP. O `CommandLineRunner` (opcional) pode ser usado para invocar UseCases programaticamente em testes manuais.

```java
// src/main/java/br/com/escola/matricula/ErpMatriculaApplication.java
package br.com.escola.matricula;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("br.com.escola.matricula.infraestrutura.persistencia")
public class ErpMatriculaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpMatriculaApplication.class, args);
    }
}
```

**Sem web: o contexto levanta, Flyway roda, mas a JVM encerra imediatamente** porque não há servidor HTTP para manter o processo vivo. Para manter o processo ativo sem web starter, adicionar ao `application.yml`:

```yaml
spring:
  main:
    web-application-type: none
```

Isso diz ao Spring Boot explicitamente que é uma aplicação não-web, evitando que tente detectar e iniciar servidor. A aplicação levanta, roda Flyway, e encerra. Para executar um fluxo, usar `CommandLineRunner` ou chamar UseCases via teste de integração.

**Alternativa pedagógica:** Manter `web-application-type: none` e incluir um `CommandLineRunner` no `infraestrutura.config/` que executa os três fluxos com os seeds, imprimindo saída no console. Isso demonstra os fluxos sem precisar de HTTP.

**application.yml completo para Fase 3:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/erp_matricula
    username: matricula
    password: matricula
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration
  main:
    web-application-type: none

mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: br.com.escola.matricula.infraestrutura.persistencia
  type-handlers-package: br.com.escola.matricula.infraestrutura.persistencia.typehandler
  configuration:
    map-underscore-to-camel-case: false
    lazy-loading-enabled: false

logging:
  level:
    br.com.escola.matricula: DEBUG
    org.mybatis: DEBUG
```

**Por que `map-underscore-to-camel-case: false`:** Com mapeamento explícito via aliases no SQL (ex: `m.id AS matricula_id`), o auto-camelCase pode interferir. Manter false e usar aliases explícitos no SQL — mais pedagógico.

---

## 6. Java 21 Features

### Records com validação no construtor compacto (DOM-01)

[VERIFIED: STACK.md — documentação JDK Java 21]

```java
// Compact constructor — sem parâmetros explícitos, `valor` já está no escopo
public record Cpf(String valor) {
    public Cpf {
        Objects.requireNonNull(valor, "CPF não pode ser nulo");
        String apenasDigitos = valor.replaceAll("[^0-9]", "");
        if (apenasDigitos.length() != 11) {
            throw new IllegalArgumentException("CPF deve ter 11 dígitos. Recebido: " + valor);
        }
        if (!cpfValido(apenasDigitos)) {
            throw new IllegalArgumentException("CPF com dígito verificador inválido: " + valor);
        }
        valor = apenasDigitos; // normalização — reatribuir o componente no compact constructor
    }
    private static boolean cpfValido(String d) { /* algoritmo módulo 11 */ return true; }
    public String formatado() { return d.substring(0,3)+"."+d.substring(3,6)+"."+d.substring(6,9)+"-"+d.substring(9,11); }
}
```

**Regra do compact constructor:** Pode reatribuir os componentes (`valor = apenasDigitos`) — é o mecanismo de normalização. Qualquer reatribuição acontece ANTES do `this` ser construído.

### Sealed interface StatusMatricula (DOM-05)

[VERIFIED: STACK.md — JEP 409, finalizado Java 17; pattern matching JEP 441 finalizado Java 21]

```java
// dominio/modelo/StatusMatricula.java — OU dominio/vo/StatusMatricula.java (planner decide)
package br.com.escola.matricula.dominio.modelo;

import java.time.LocalDateTime;

public sealed interface StatusMatricula
        permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida {

    record Ativa() implements StatusMatricula {}

    record Cancelada(LocalDateTime canceladaEm) implements StatusMatricula {}

    record Concluida(LocalDateTime concluidaEm) implements StatusMatricula {}
}
```

**Switch exaustivo (sem default):**
```java
String descricao = switch (this.status) {
    case StatusMatricula.Ativa a     -> "Em andamento";
    case StatusMatricula.Cancelada c -> "Cancelada em " + c.canceladaEm();
    case StatusMatricula.Concluida c -> "Concluída em " + c.concluidaEm();
    // sem default — compilador exige todos os casos
};
```

**Persistência de StatusMatricula:** O `MatriculaRow` armazena `String status` + `LocalDateTime canceladaEm` + `LocalDateTime concluidaEm`. O `MatriculaRowMapper` reconstrói o sealed interface:

```java
private StatusMatricula reconstruirStatus(MatriculaRow row) {
    return switch (row.status()) {
        case "ATIVA"     -> new StatusMatricula.Ativa();
        case "CANCELADA" -> new StatusMatricula.Cancelada(row.canceladaEm());
        case "CONCLUIDA" -> new StatusMatricula.Concluida(row.concluidaEm());
        default -> throw new IllegalStateException("Status desconhecido: " + row.status());
    };
}
```

**Escrita de StatusMatricula para banco (em MatriculaRow):**
```java
// No MatriculaRowMapper.fromDomain(Matricula):
String status = switch (matricula.getStatus()) {
    case StatusMatricula.Ativa a     -> "ATIVA";
    case StatusMatricula.Cancelada c -> "CANCELADA";
    case StatusMatricula.Concluida c -> "CONCLUIDA";
};
LocalDateTime canceladaEm = (matricula.getStatus() instanceof StatusMatricula.Cancelada c)
    ? c.canceladaEm() : null;
LocalDateTime concluidaEm = (matricula.getStatus() instanceof StatusMatricula.Concluida c)
    ? c.concluidaEm() : null;
```

---

## 7. Domain Layer (DOM-01..10)

### Checklist completo com assinaturas

**Pacote `dominio.vo/` (DOM-01):**

| Classe | Tipo Java | Campos | Validações |
|--------|-----------|--------|------------|
| `Cpf` | `record` | `String valor` | não-nulo, 11 dígitos, algoritmo módulo 11 |
| `PeriodoLetivo` | `record` | `int ano, int semestre` | ano >= 2000, semestre 1..2 |
| `MatriculaId` | `record` | `UUID valor` | não-nulo |
| `AlunoId` | `record` | `UUID valor` | não-nulo |
| `TurmaId` | `record` | `UUID valor` | não-nulo |
| `NomeDisciplina` | `record` | `String valor` | não-nulo, não-branco, max 100 |

**Atenção:** `PeriodoLetivo` do design tático tem `(int ano, int semestre)`. D-14 define `periodo_inicio DATE + periodo_fim DATE` no schema. O planner deve decidir se atualiza o VO ou usa mapeamento inline (ver seção 2).

**Pacote `dominio.modelo/` (DOM-02..05, DOM-07):**

```java
// Aluno.java — Entidade (não record: tem ciclo de vida, status mutável)
public class Aluno {
    private final AlunoId id;
    private final Cpf cpf;
    private final String nome;
    private boolean ativo;

    public Aluno(AlunoId id, Cpf cpf, String nome, boolean ativo) { ... }

    // equals/hashCode por identidade (id) — padrão de Entidade DDD
    @Override public boolean equals(Object o) { ... id.equals(...) ... }
    @Override public int hashCode() { return id.hashCode(); }

    public boolean estaAtivo() { return ativo; }
    // getters, sem setters públicos
}

// Turma.java — Entidade
public class Turma {
    private final TurmaId id;
    private final String nome;
    private final PeriodoLetivo periodoLetivo;
    private final int vagasMaximas;

    public Turma(TurmaId id, String nome, PeriodoLetivo periodoLetivo, int vagasMaximas) { ... }

    public boolean periodoEstaAberto() {
        // Verifica se hoje está dentro do período — lógica aqui, não no service
        return true; // TODO: implementar com LocalDate.now()
    }
    // equals/hashCode por TurmaId
}

// ItemMatricula.java — entidade interna (record: imutável, sem ID próprio)
public record ItemMatricula(NomeDisciplina disciplina) {}

// Matricula.java — Aggregate Root
public class Matricula {
    private static final int LIMITE_DISCIPLINAS = 6;

    private final MatriculaId id;
    private final AlunoId alunoId;
    private final TurmaId turmaId;
    private final PeriodoLetivo periodoLetivo;
    private StatusMatricula status;
    private final List<ItemMatricula> disciplinas;
    private final List<Object> eventos;

    // Construtor para novo aggregate (create)
    public static Matricula criar(AlunoId alunoId, TurmaId turmaId, PeriodoLetivo periodo) { ... }

    // Construtor para reconstituição (do banco)
    public Matricula(MatriculaId id, AlunoId alunoId, TurmaId turmaId,
                     PeriodoLetivo periodoLetivo, StatusMatricula status,
                     List<ItemMatricula> disciplinas) { ... }

    public void adicionarDisciplina(NomeDisciplina disciplina) { /* 3 guards + evento */ }
    public void cancelar() { /* guard + evento */ }

    public List<Object> coletarEventos() {
        List<Object> copia = List.copyOf(this.eventos);
        this.eventos.clear();
        return copia;
    }

    // Getters sem setters
}
```

**Pacote `dominio.evento/` (DOM-06):**

```java
public record AlunoMatriculado(MatriculaId matriculaId, AlunoId alunoId,
    TurmaId turmaId, PeriodoLetivo periodoLetivo, LocalDateTime ocorridoEm) {}

public record DisciplinaAdicionada(MatriculaId matriculaId, AlunoId alunoId,
    NomeDisciplina disciplina, LocalDateTime ocorridoEm) {}

public record MatriculaCancelada(MatriculaId matriculaId, AlunoId alunoId,
    PeriodoLetivo periodoLetivo, LocalDateTime ocorridoEm) {}
```

**Pacote `dominio.repositorio/` (DOM-09):**

```java
public interface MatriculaRepositorio {
    Optional<Matricula> buscarPorId(MatriculaId id);
    List<Matricula> buscarPorAluno(AlunoId alunoId);
    boolean existeMatriculaAtiva(AlunoId alunoId, PeriodoLetivo periodo);
    void salvar(Matricula matricula);
}
// ZERO imports de org.springframework.*, org.mybatis.*, jakarta.*
```

**Pacote `dominio.servico/` (DOM-08):**

```java
// Domain Service — sem @Service, sem Spring
public class VerificadorElegibilidadeMatricula {

    private final MatriculaRepositorio repositorio;

    public VerificadorElegibilidadeMatricula(MatriculaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public void verificar(Aluno aluno, Turma turma, PeriodoLetivo periodo) {
        if (!aluno.estaAtivo()) throw new AlunoInativoException(aluno.getId());
        if (!turma.periodoEstaAberto()) throw new PeriodoFechadoException(periodo);
        if (repositorio.existeMatriculaAtiva(aluno.getId(), periodo))
            throw new MatriculaDuplicadaException(aluno.getId(), periodo);
    }
}
```

**Pacote `dominio.excecao/` (DOM-10):**

```java
// Exceções com campos estruturados — não apenas mensagem String
public class LimiteDisciplinasExcedidoException extends RuntimeException {
    private final int limite;
    private final int atual;
    public LimiteDisciplinasExcedidoException(int limite, MatriculaId id) {
        super("Limite de " + limite + " disciplinas excedido na matrícula " + id.valor());
        this.limite = limite;
        this.atual = limite; // caller sabe que tentou exceder
    }
    public int getLimite() { return limite; }
    public int getAtual() { return atual; }
}

// Demais: DisciplinaJaMatriculadaException, MatriculaCanceladaException,
//         AlunoInativoException, PeriodoFechadoException
// Todas estendem RuntimeException com campos contextuais relevantes
```

**Regra de ouro para o domínio:** Nenhum arquivo em `dominio/` pode ter `import org.springframework`, `import org.mybatis`, `import jakarta.persistence`. Verificar após cada arquivo com grep.

---

## 8. Application Layer (APL-01..05)

### UseCase Pattern

[VERIFIED: STACK.md — padrão Application Service + @Transactional nos UseCases]

```java
// aplicacao/MatricularAlunoUseCase.java
@Service
@Transactional
public class MatricularAlunoUseCase {

    private final MatriculaRepositorio repositorio;
    private final VerificadorElegibilidadeMatricula verificador;
    private final ApplicationEventPublisher publicador;

    // Injeção por construtor — sem @Autowired no construtor (Spring 4.3+)
    public MatricularAlunoUseCase(MatriculaRepositorio repositorio,
                                   VerificadorElegibilidadeMatricula verificador,
                                   ApplicationEventPublisher publicador) {
        this.repositorio = repositorio;
        this.verificador = verificador;
        this.publicador = publicador;
    }

    public MatriculaId executar(MatricularAlunoCommand command) {
        // 1. Validar elegibilidade (Domain Service)
        verificador.verificar(command.aluno(), command.turma(), command.periodo());

        // 2. Criar Aggregate (decisão de negócio no domínio)
        Matricula matricula = Matricula.criar(
            command.aluno().getId(),
            command.turma().getId(),
            command.periodo()
        );

        // 3. Persistir ANTES de publicar (D-10: ordem obrigatória)
        repositorio.salvar(matricula);

        // 4. Publicar eventos APÓS persistência
        // @Transactional garante que @TransactionalEventListener
        // só executa após commit — sem risco de processar evento de transação falha
        matricula.coletarEventos().forEach(publicador::publishEvent);

        return matricula.getId();
    }
}
```

```java
// aplicacao/AdicionarDisciplinaUseCase.java
@Service
@Transactional
public class AdicionarDisciplinaUseCase {

    private final MatriculaRepositorio repositorio;
    private final ApplicationEventPublisher publicador;

    public AdicionarDisciplinaUseCase(MatriculaRepositorio repositorio,
                                       ApplicationEventPublisher publicador) { ... }

    public void executar(AdicionarDisciplinaCommand command) {
        Matricula matricula = repositorio.buscarPorId(command.matriculaId())
            .orElseThrow(() -> new MatriculaNaoEncontradaException(command.matriculaId()));

        // Aggregate decide — lança exceção se invariante violada
        matricula.adicionarDisciplina(command.disciplina());

        repositorio.salvar(matricula);
        matricula.coletarEventos().forEach(publicador::publishEvent);
    }
}
```

```java
// aplicacao/CancelarMatriculaUseCase.java — mesmo padrão
```

**Commands (APL-04):**

```java
// Records como Commands — imutáveis, carregam objetos de domínio (não primitivos)
public record MatricularAlunoCommand(Aluno aluno, Turma turma, PeriodoLetivo periodo) {}
public record AdicionarDisciplinaCommand(MatriculaId matriculaId, NomeDisciplina disciplina) {}
public record CancelarMatriculaCommand(MatriculaId matriculaId) {}
```

**Atenção:** Fase 4 adicionará Controllers que converterão HTTPRequest → Command. Na Fase 3, os Commands são criados diretamente (via CommandLineRunner ou teste).

**Listeners Stub (APL-05):**

```java
// infraestrutura/eventos/FinanceiroEventListener.java
@Component
public class FinanceiroEventListener {

    private static final Logger log = LoggerFactory.getLogger(FinanceiroEventListener.class);

    // @TransactionalEventListener garante execução APÓS commit
    // Sem isso, o listener executaria dentro da transação do UseCase
    // e poderia processar um evento de uma transação que ainda vai falhar
    @TransactionalEventListener
    public void aoMatricular(AlunoMatriculado evento) {
        log.info("[BC Financeiro] Criando contrato de cobrança para matrícula {}",
                 evento.matriculaId().valor());
        // Stub — implementação real seria Fase BC-01 (v2)
    }

    @TransactionalEventListener
    public void aoCancelar(MatriculaCancelada evento) {
        log.info("[BC Financeiro] Processando cancelamento de cobrança para matrícula {}",
                 evento.matriculaId().valor());
    }
}

// infraestrutura/eventos/AcademicoEventListener.java — padrão similar
```

**Por que `@TransactionalEventListener` e não `@EventListener`:**
- `@EventListener`: executa dentro da transação do publicador — se a transação fizer rollback DEPOIS do listener, o listener já executou com dados que serão desfeitos
- `@TransactionalEventListener` (default `AFTER_COMMIT`): só executa se e quando a transação commitou — garante que o evento representa um fato persistido

---

## 9. Infrastructure Layer (INF-01..07)

### MatriculaMapper interface

```java
// infraestrutura/persistencia/MatriculaMapper.java
package br.com.escola.matricula.infraestrutura.persistencia;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface MatriculaMapper {
    MatriculaRow buscarPorId(@Param("id") UUID id);
    List<MatriculaRow> buscarPorAluno(@Param("alunoId") UUID alunoId);
    boolean existeMatriculaAtiva(@Param("alunoId") UUID alunoId,
                                  @Param("periodoInicio") java.time.LocalDate periodoInicio,
                                  @Param("periodoFim") java.time.LocalDate periodoFim);
    int inserirMatricula(MatriculaRow row);
    int atualizarMatricula(MatriculaRow row);
    int deletarItensPorMatriculaId(@Param("matriculaId") UUID matriculaId);
    int inserirItens(@Param("list") List<ItemMatriculaRow> itens);
}
```

### MatriculaRow e ItemMatriculaRow

```java
// infraestrutura/persistencia/MatriculaRow.java
// Classe simples — espelha colunas da tabela, SEM lógica de negócio
// O contraste com Matricula.java (que tem comportamento) é o ponto pedagógico (INF-06)
public class MatriculaRow {
    public UUID id;
    public UUID alunoId;
    public UUID turmaId;
    public java.time.LocalDate periodoInicio;
    public java.time.LocalDate periodoFim;
    public String status;
    public java.time.LocalDateTime canceladaEm;
    public java.time.LocalDateTime concluidaEm;
    public List<ItemMatriculaRow> itens = new ArrayList<>();
}

// infraestrutura/persistencia/ItemMatriculaRow.java
public class ItemMatriculaRow {
    public UUID matriculaId;   // necessário para INSERT em lote
    public String disciplina;
}
```

### MatriculaRowMapper (INF-06)

```java
// infraestrutura/persistencia/MatriculaRowMapper.java
// ÚNICO arquivo que conhece tanto MatriculaRow quanto Matricula
// Lendo este arquivo, o aluno vê exatamente onde domínio e persistência se separam
@Component
public class MatriculaRowMapper {

    public Matricula toDomain(MatriculaRow row) {
        var periodo = new PeriodoLetivo(row.periodoInicio.getYear(),
                                        row.periodoInicio.getMonthValue() <= 6 ? 1 : 2);
        // Nota: a conversão DATE → (ano, semestre) é uma regra de negócio
        // Para o VO com LocalDate, seria: new PeriodoLetivo(row.periodoInicio, row.periodoFim)

        var status = reconstruirStatus(row);

        var disciplinas = row.itens.stream()
            .map(i -> new ItemMatricula(new NomeDisciplina(i.disciplina)))
            .toList();

        return new Matricula(
            new MatriculaId(row.id),
            new AlunoId(row.alunoId),
            new TurmaId(row.turmaId),
            periodo,
            status,
            disciplinas
        );
    }

    public MatriculaRow fromDomain(Matricula m) {
        var row = new MatriculaRow();
        row.id = m.getId().valor();
        row.alunoId = m.getAlunoId().valor();
        row.turmaId = m.getTurmaId().valor();
        // conversão PeriodoLetivo → LocalDate (semestre 1 = fev-jul, semestre 2 = ago-jan)
        row.periodoInicio = LocalDate.of(m.getPeriodoLetivo().ano(),
                                         m.getPeriodoLetivo().semestre() == 1 ? 2 : 8, 1);
        row.periodoFim    = LocalDate.of(m.getPeriodoLetivo().ano(),
                                         m.getPeriodoLetivo().semestre() == 1 ? 7 : 12, 31);
        row.status = switch (m.getStatus()) {
            case StatusMatricula.Ativa a     -> "ATIVA";
            case StatusMatricula.Cancelada c -> "CANCELADA";
            case StatusMatricula.Concluida c -> "CONCLUIDA";
        };
        row.canceladaEm = (m.getStatus() instanceof StatusMatricula.Cancelada c) ? c.canceladaEm() : null;
        row.concluidaEm = (m.getStatus() instanceof StatusMatricula.Concluida c) ? c.concluidaEm() : null;
        return row;
    }

    public List<ItemMatriculaRow> itemsFromDomain(MatriculaId matriculaId, List<ItemMatricula> items) {
        return items.stream().map(item -> {
            var row = new ItemMatriculaRow();
            row.matriculaId = matriculaId.valor();
            row.disciplina  = item.disciplina().valor();
            return row;
        }).toList();
    }

    private StatusMatricula reconstruirStatus(MatriculaRow row) {
        return switch (row.status) {
            case "ATIVA"     -> new StatusMatricula.Ativa();
            case "CANCELADA" -> new StatusMatricula.Cancelada(row.canceladaEm);
            case "CONCLUIDA" -> new StatusMatricula.Concluida(row.concluidaEm);
            default -> throw new IllegalStateException("Status desconhecido: " + row.status);
        };
    }
}
```

### MatriculaRepositorioMyBatis (INF-07)

```java
// infraestrutura/persistencia/MatriculaRepositorioMyBatis.java
@Repository
public class MatriculaRepositorioMyBatis implements MatriculaRepositorio {

    private final MatriculaMapper mapper;
    private final MatriculaRowMapper rowMapper;

    public MatriculaRepositorioMyBatis(MatriculaMapper mapper, MatriculaRowMapper rowMapper) {
        this.mapper = mapper;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<Matricula> buscarPorId(MatriculaId id) {
        MatriculaRow row = mapper.buscarPorId(id.valor());
        return Optional.ofNullable(row).map(rowMapper::toDomain);
    }

    @Override
    public List<Matricula> buscarPorAluno(AlunoId alunoId) {
        return mapper.buscarPorAluno(alunoId.valor()).stream()
            .map(rowMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existeMatriculaAtiva(AlunoId alunoId, PeriodoLetivo periodo) {
        LocalDate inicio = LocalDate.of(periodo.ano(),
                                         periodo.semestre() == 1 ? 2 : 8, 1);
        LocalDate fim    = LocalDate.of(periodo.ano(),
                                         periodo.semestre() == 1 ? 7 : 12, 31);
        return mapper.existeMatriculaAtiva(alunoId.valor(), inicio, fim);
    }

    @Override
    public void salvar(Matricula matricula) {
        MatriculaRow row = rowMapper.fromDomain(matricula);
        // Estratégia replace-all — pedagogicamente explícita (D-12)
        // INSERT se novo, UPDATE se existente
        if (mapper.atualizarMatricula(row) == 0) {
            mapper.inserirMatricula(row);
        }
        // DELETE todos os itens atuais
        mapper.deletarItensPorMatriculaId(matricula.getId().valor());
        // INSERT todos os itens do estado atual do Aggregate
        List<ItemMatriculaRow> itens = rowMapper.itemsFromDomain(
            matricula.getId(), matricula.getDisciplinas());
        if (!itens.isEmpty()) {
            mapper.inserirItens(itens);
        }
    }
}
```

### MyBatisConfig

```java
// infraestrutura/config/MyBatisConfig.java
@Configuration
public class MyBatisConfig {
    // TypeHandlers são registrados via type-handlers-package no application.yml
    // Esta classe existe para configurações adicionais futuras (Fase 4: web, etc.)
    // Pedagogicamente: mostra que configuração de infraestrutura fica em infraestrutura/config/
}
```

---

## 10. Pitfalls e Landmines

### Pitfall 1: `notNullColumn` ausente no ResultMap
**O que vai errado:** Matrícula sem disciplinas retorna `List<ItemMatricula>` com um elemento `ItemMatricula(null)`.
**Por quê:** LEFT JOIN retorna uma linha com NULLs para a matrícula sem itens. Sem `notNullColumn`, MyBatis cria um objeto de qualquer forma.
**Como evitar:** SEMPRE `notNullColumn="item_disciplina"` no `<collection>`. Testar com um seed de matrícula sem itens. [VERIFIED: mybatis.org/mybatis-3/sqlmap-xml.html]

### Pitfall 2: UUID como VARCHAR no PostgreSQL
**O que vai errado:** `WHERE id = #{id}` falha com `ERROR: operator does not exist: uuid = character varying`.
**Por quê:** PostgreSQL tem tipo nativo `UUID`; MyBatis passa como VARCHAR por default.
**Como evitar:** Usar `#{id, jdbcType=OTHER}` para todos os parâmetros UUID. [VERIFIED: STACK.md]

### Pitfall 3: Flyway 10+ sem módulo PostgreSQL
**O que vai errado:** `Could not find driver 'org.postgresql.Driver'` ou `No dialect found for database 'PostgreSQL'` ao iniciar.
**Por quê:** Flyway 10 separou suporte de bancos em módulos separados.
**Como evitar:** Adicionar `flyway-database-postgresql` ao pom.xml além de `flyway-core`. [VERIFIED: STACK.md]

### Pitfall 4: Spring não encontra Mappers MyBatis
**O que vai errado:** `Field mapper in ... required a bean of type 'MatriculaMapper' that could not be found`.
**Por quê:** Falta `@MapperScan` na classe principal OU falta `@Mapper` na interface.
**Como evitar:** Usar `@MapperScan("br.com.escola.matricula.infraestrutura.persistencia")` na main class. [VERIFIED: mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/]

### Pitfall 5: Aplicação encerra imediatamente sem web
**O que vai errado:** Flyway roda, contexto sobe, JVM encerra — sem erro, mas sem executar nada.
**Por quê:** Sem servidor HTTP, Spring Boot encerra após inicialização.
**Como evitar:** `spring.main.web-application-type: none` é necessário para clareza. Para executar os fluxos, usar `CommandLineRunner` no `infraestrutura.config/`.

### Pitfall 6: `coletarEventos()` chamado fora da ordem D-10
**O que vai errado:** Eventos publicados ANTES de `repositorio.salvar()` → listeners executam sobre estado não persistido; se a transação fizer rollback depois, o evento foi processado de forma errada.
**Por quê:** `@TransactionalEventListener(AFTER_COMMIT)` não executa se chamado fora de transação ou antes do commit.
**Como evitar:** Ordem explícita: `operar → salvar → coletarEventos() → forEach(publishEvent)`. Comentar no código com referência a D-10.

### Pitfall 7: import de Spring/MyBatis no pacote `dominio/`
**O que vai errado:** O ponto pedagógico central do projeto — domínio sem dependência de framework — é violado silenciosamente.
**Como detectar:** `grep -r "import org.springframework" src/main/java/br/com/escola/matricula/dominio/`
**Como evitar:** Verificar após cada arquivo criado. Este grep é o Success Criterion 1 da fase.

### Pitfall 8: `VerificadorElegibilidadeMatricula` com `@Service`
**O que vai errado:** Domain Service vira bean Spring, importa `org.springframework.stereotype.Service`, viola DOM-08 (serviço puro sem Spring).
**Como evitar:** Domain Service é Java puro. O UseCase o instancia via injeção do `MatriculaRepositorio`. Se quiser como bean Spring, mover para `aplicacao/` e rebatizar como ApplicationService — mas então não é um Domain Service.

### Pitfall 9: `eventos` não inicializado no construtor de reconstituição
**O que vai errado:** `MatriculaRowMapper.toDomain()` cria `Matricula` via construtor de reconstituição; se `eventos` não for inicializado, `adicionarDisciplina()` lança NPE ao tentar `this.eventos.add(...)`.
**Como evitar:** Construtor de reconstituição SEMPRE inicializa `this.eventos = new ArrayList<>()`.

### Pitfall 10: `map-underscore-to-camel-case: true` com aliases explícitos
**O que vai errado:** `aluno_id AS alunoId` no SQL + `map-underscore-to-camel-case: true` = MyBatis pode aplicar a conversão sobre o alias já convertido, resultando em mapeamento incorreto.
**Como evitar:** Desabilitar `map-underscore-to-camel-case: false` e usar aliases explícitos no SQL. Mais verboso, mais pedagógico.

---

## 11. Validation Architecture

`nyquist_validation: false` no config.json — seção de framework de teste omitida conforme configuração do projeto.

### Verificações de Qualidade (grep-based — sem teste automatizado)

As verificações a seguir são os Success Criteria da fase e devem ser executadas ao final:

**SC-1: Domínio sem vazamento de framework**
```bash
grep -r "import org.springframework" src/main/java/br/com/escola/matricula/dominio/
# Resultado esperado: VAZIO
grep -r "import org.mybatis" src/main/java/br/com/escola/matricula/dominio/
# Resultado esperado: VAZIO
```

**SC-2: Aggregate rejeita invariante corretamente**
```bash
# Verificar que LimiteDisciplinasExcedidoException tem campos `limite` e `atual`
grep -r "int limite" src/main/java/br/com/escola/matricula/dominio/excecao/
grep -r "int atual" src/main/java/br/com/escola/matricula/dominio/excecao/
```

**SC-3: Compilação**
```bash
mvn compile -q
# Resultado esperado: BUILD SUCCESS sem warnings de deprecation
```

**SC-4: Flyway + seeds aplicados**
```bash
# PostgreSQL deve estar rodando em localhost:5432
mvn spring-boot:run
# Verificar nos logs: "Successfully applied 2 migrations"
```

**SC-5: MatriculaRowMapper explícito**
```bash
# Verificar que MatriculaRow não tem nenhum método de negócio
grep -c "public void\|public boolean\|public int" src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRow.java
# Resultado esperado: 0 (apenas campos públicos ou getters simples)
```

**SC-5: Listeners recebem evento**
```bash
# No log após executar MatricularAlunoUseCase via CommandLineRunner:
grep "BC Financeiro.*criando contrato" application.log
grep "BC Academico.*registrando vinculo" application.log
```

---

## Don't Hand-Roll

| Problema | Não construir | Usar em vez disso | Por quê |
|----------|--------------|-------------------|---------|
| Registro de TypeHandlers | Registrador manual via `SqlSessionFactory` customizada | `mybatis.type-handlers-package` no application.yml | Auto-scanning funciona; manual é propenso a esquecimento |
| Migrations de schema | Scripts SQL executados manualmente | Flyway `V1__schema.sql` | Idempotência e controle de versão automáticos |
| Publicação de eventos transacionais | Callbacks manuais pós-save | `@TransactionalEventListener` do Spring | Garantia de AFTER_COMMIT sem código extra |
| Reconhecimento de beans MyBatis | Configuração manual de `SqlSessionFactory` | `@MapperScan` ou `@Mapper` | O autoconfigure do mybatis-spring-boot-starter já faz |
| Verificação de nulo com `if` | Chains de `if (x != null)` | `Optional<T>` retornado pelo repositório | Força o caller a tratar ausência explicitamente |
| Mapeamento de `List<Object>` para `forEach(publisher::publishEvent)` | Loop manual com cast | `forEach(publicador::publishEvent)` | `ApplicationEventPublisher.publishEvent(Object)` aceita qualquer tipo |

---

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Conversão de `PeriodoLetivo(ano, semestre)` para `LocalDate` usa semestre 1 = fevereiro, semestre 2 = agosto como meses de início | Seção 9 — MatriculaRowMapper | Datas de início/fim de período erradas no banco; os seeds precisariam atualizar também |
| A2 | `VerificadorElegibilidadeMatricula` é instanciado pelo Spring como `@Component` para que o UseCase possa injetá-lo | Seção 7/8 | Planner pode querer que seja Java puro sem anotação Spring — se sim, UseCase instancia diretamente passando o repositório |
| A3 | A inconsistência `PeriodoLetivo(ano, semestre)` vs D-14 `(LocalDate inicio, LocalDate fim)` deve ser resolvida mantendo o VO como está e fazendo conversão no mapper | Seção 2 — TypeHandlers | Se o planner decidir mudar o VO, a documentação de Fase 2 precisa ser atualizada também |

---

## Open Questions

1. **PeriodoLetivo: VO com (ano, semestre) vs D-14 com (LocalDate, LocalDate)**
   - O que sabemos: Design tático documenta `record PeriodoLetivo(int ano, int semestre)`; D-14 define `periodo_inicio DATE + periodo_fim DATE` no schema
   - O que está unclear: Qual está correto — o VO ou o schema?
   - Recomendação: Manter o VO como `(ano, semestre)` e fazer a conversão no `MatriculaRowMapper` (Opção C da Seção 2). Se o planner preferir datas reais, atualizar o VO antes de implementar.

2. **`VerificadorElegibilidadeMatricula`: Domain Service puro vs. bean Spring?**
   - O que sabemos: É um Domain Service sem anotações Spring
   - O que está unclear: Como o UseCase o obtém? Se for `@Component`, tem `import org.springframework` mas em `dominio.servico/` — viola a regra
   - Recomendação: Ou (a) `@Service` em `dominio.servico/` como exceção documentada, ou (b) o UseCase o instancia diretamente passando `repositorio`. Opção (b) é mais pura para DDD.

3. **CommandLineRunner para demonstrar fluxos sem HTTP**
   - O que sabemos: Sem `spring-boot-starter-web`, a JVM encerra após inicialização
   - O que está unclear: O planner quer um `CommandLineRunner` explícito, ou apenas garantir que os fluxos são chamáveis via teste?
   - Recomendação: Incluir um `DemoRunner` em `infraestrutura.config/` que executa os 3 fluxos com os UUIDs dos seeds — demonstra a fase sem precisar de HTTP.

---

## Sources

### Primary (HIGH confidence)
- `docs/02-design-tatico/value-objects.md` — Snippets Java 21 dos VOs com construtor compacto
- `docs/02-design-tatico/agregados.md` — StatusMatricula sealed interface, adicionarDisciplina() completo
- `docs/02-design-tatico/domain-events.md` — Catálogo de eventos, pattern coletarEventos()
- `docs/02-design-tatico/repositorios.md` — Interface MatriculaRepositorio
- `docs/adrs/ADR-001-mybatis-vs-jpa.md` — Decisão MyBatis, consequências
- `.planning/research/STACK.md` — Versões verificadas, padrões MyBatis XML, schema PostgreSQL
- `.planning/research/PITFALLS.md` — N+1, resultMap sem id, mapper confundido com repositório
- `.planning/phases/03-implementacao/03-CONTEXT.md` — Todas as decisões D-01..D-14

### Secondary (MEDIUM confidence)
- [mybatis.org/mybatis-3/sqlmap-xml.html](https://mybatis.org/mybatis-3/sqlmap-xml.html) — ResultMaps com collection, notNullColumn, TypeHandlers
- [mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/) — @MapperScan, type-handlers-package

---

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — versões verificadas em Maven Central em 2026-06-20 (STACK.md)
- MyBatis XML patterns: HIGH — documentação oficial consultada (STACK.md via Context7)
- Architecture patterns: HIGH — baseado em documentação de Fase 2 que é a spec desta fase
- PeriodoLetivo TypeHandler approach: MEDIUM — há inconsistência entre VO (ano+semestre) e D-14 (LocalDate); abordagem de mapeamento inline é inferência do pesquisador
- Domain Service instanciação: MEDIUM — duas abordagens válidas, decisão não locked no CONTEXT.md

**Research date:** 2026-06-20
**Valid until:** 2026-08-20 (stack estável; MyBatis 3.0.5 + Spring Boot 3.5.3 são releases recentes)
