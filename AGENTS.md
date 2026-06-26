# AGENTS.md — Guia Operacional para Agentes de IA

Este guia cobre o módulo **`erp-matricula-ddd/`** — implementação com Domain-Driven Design (porta 8080).

Para contexto pedagógico completo (por que DDD, decisões de design, ADRs), consulte [README.md](README.md).

---

## 1. Estrutura de Pacotes do Módulo DDD

Raiz do código-fonte: `erp-matricula-ddd/src/main/java/br/com/escola/matricula/`

| Pacote | Responsabilidade | Importa de |
|--------|-----------------|------------|
| `dominio/` | Modelo rico, invariantes, repositório (interface), domain service | Nada externo |
| `aplicacao/` | UseCases, Commands, DTOs | `dominio/` |
| `infraestrutura/` | Implementação repositório (MyBatis), TypeHandlers, configuração Spring | `dominio/`, Spring, MyBatis |
| `interfaces/` | Controllers REST, ExceptionHandlers | `aplicacao/`, Spring Web |

**Regra de dependência:** `dominio/` nunca importa `org.springframework.*`, `org.apache.ibatis.*` ou `jakarta.*`. Verificavel com:

```bash
grep -r "import org.springframework\|import org.apache.ibatis\|import jakarta" \
  erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/
```

Subpacotes de `dominio/`:
- `dominio/modelo/` — Aggregates, Entidades
- `dominio/vo/` — Value Objects
- `dominio/repositorio/` — interfaces de repositório
- `dominio/servico/` — Domain Services (sem `@Service`)
- `dominio/evento/` — Domain Events
- `dominio/excecao/` — exceções de domínio

---

## 2. Adicionando um Novo Use Case de Escrita

### 2.1 Checklist de artefatos (em ordem de dependência)

```
1. [ ] dominio/vo/NovoVO.java                          — se precisar de novo Value Object
2. [ ] dominio/modelo/NovaEntidade.java                — se precisar de nova entidade ou alterar Aggregate
3. [ ] dominio/repositorio/NovoRepositorio.java        — interface, se precisar de nova porta
4. [ ] aplicacao/NovoCommand.java                      — dados de entrada do use case (record imutável)
5. [ ] aplicacao/NovoUseCase.java                      — orquestra; não decide; delega ao domínio
6. [ ] infraestrutura/persistencia/NovoMapper.java     — interface @Mapper MyBatis
7. [ ] infraestrutura/persistencia/NovoRepositorioMyBatis.java — implementa interface de domínio
8. [ ] src/main/resources/mapper/NovoMapper.xml        — queries SQL com ResultMap
9. [ ] interfaces/NovoController.java                  — traduz HTTP → Command → UseCase
```

### 2.2 Padrão do UseCase de Escrita

O `MatricularAlunoUseCase` é o exemplo canônico. Seu método `executar()` segue quatro passos obrigatórios:

1. **Delegar ao Domain Service** — verifica regras sem `if/else` no UseCase
2. **Chamar factory method do Aggregate** — o Aggregate decide seu estado inicial
3. **Chamar `repositorio.salvar()`** — persistência ANTES de publicar eventos
4. **Coletar e publicar eventos APÓS salvar** — `matricula.coletarEventos().forEach(publicador::publishEvent)`

Referência: `aplicacao/MatricularAlunoUseCase.java`

### 2.3 Regras do UseCase (proibições explícitas)

- **NUNCA** colocar `if/else` de negócio no UseCase — mover para Aggregate ou Domain Service
- **NUNCA** retornar o Aggregate diretamente — retornar DTO ou UUID
- **NUNCA** injetar `MatriculaMapper` diretamente no UseCase — injetar a interface `MatriculaRepositorio`
- **SEMPRE** publicar eventos APÓS `repositorio.salvar()`, nunca antes
- **SEMPRE** usar `@Transactional` no UseCase de escrita

### 2.4 Padrão do Command

```java
// aplicacao/NovoCommand.java
public record NovoCommand(
        Entidade entidade,       // objetos de domínio já validados, não primitivos
        ValorObjeto valorObjeto) {
}
```

O Controller é responsável por construir os objetos de domínio a partir dos primitivos HTTP e criar o Command. O UseCase nunca valida primitivos.

Referência: `aplicacao/MatricularAlunoCommand.java`

---

## 3. Adicionando um Use Case de Leitura (Query)

Este projeto **não usa CQRS formal**. Queries são UseCases simples sem Command.

### 3.1 Padrão de query sem CQRS

```java
// aplicacao/BuscarMatriculaUseCase.java
@Service
public class BuscarMatriculaUseCase {

    private final MatriculaRepositorio repositorio;

    public BuscarMatriculaUseCase(MatriculaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public MatriculaDto executar(UUID matriculaId) {
        Matricula matricula = repositorio.buscarPorId(matriculaId)
            .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
        return MatriculaDto.de(matricula);
    }
}
```

Sem `@Transactional` para queries somente-leitura (a menos que necessário por consistência de sessão).

### 3.2 Fluxo para adicionar uma nova query

1. Adicionar assinatura na interface `dominio/repositorio/MatriculaRepositorio.java`
2. Implementar em `infraestrutura/persistencia/MatriculaRepositorioMyBatis.java` — chamar o Mapper
3. Adicionar método no `infraestrutura/persistencia/MatriculaMapper.java` com `@Param`
4. Adicionar `<select>` no `src/main/resources/mapper/MatriculaMapper.xml` com `resultMap` apropriado

### 3.3 Quando usar query direta no Mapper vs. passar pelo Repositório

**Sempre passar pelo Repositório.** O Mapper é detalhe de infraestrutura — UseCases nunca injetam `@Mapper` diretamente.

---

## 4. Como o MyBatis Reconstrói Objetos de Domínio

O MyBatis não sabe criar `Matricula`. Ele sabe criar `MatriculaRow`. A conversão é responsabilidade do `MatriculaRowMapper`. O fluxo completo é:

```
SQL query
   │
   ▼
XML ResultMap              ← mapeia colunas SQL → MatriculaRow (+ List<ItemMatriculaRow>)
   │
   ▼
MatriculaRow               ← objeto plano, só dados, espelho da tabela, sem comportamento
   │
   ▼
MatriculaRowMapper.toDomain()  ← ÚNICO lugar que conhece os dois modelos
   │
   ▼
Matricula (domínio)        ← objeto rico com comportamento (cancelar, adicionarDisciplina…)
```

### 4.1 Os três componentes e seus papéis

| Classe | Responsabilidade | Pacote |
|--------|-----------------|--------|
| `MatriculaRow` | Espelha as colunas da tabela — só campos públicos, sem lógica | `infraestrutura/persistencia/` |
| `MatriculaRowMapper` | Converte Row ↔ Domínio — o único que conhece os dois mundos | `infraestrutura/persistencia/` |
| `MatriculaRepositorioMyBatis` | Chama Mapper SQL → obtém Row → chama RowMapper → retorna domínio | `infraestrutura/persistencia/` |

### 4.2 `MatriculaRow` — por que não é um `record`?

`MatriculaRow` é uma classe com campos públicos porque o MyBatis popula via reflexão sem construtor com argumentos. Não tem comportamento — é só um contêiner de dados. Compare:

```java
// MatriculaRow — dados planos (infraestrutura)
public class MatriculaRow {
    public UUID id;
    public UUID alunoId;
    public String status;           // "ATIVA", "CANCELADA", "CONCLUIDA"
    public LocalDate periodoInicio; // banco guarda datas; domínio usa PeriodoLetivo(ano, semestre)
    public LocalDateTime canceladaEm;
    public List<ItemMatriculaRow> itens = new ArrayList<>();
}

// Matricula — comportamento rico (domínio)
// tem cancelar(), adicionarDisciplina(), coletarEventos()…
// usa PeriodoLetivo(ano, semestre), StatusMatricula (sealed), List<ItemMatricula>
```

### 4.3 `MatriculaRowMapper` — a fronteira explícita

```java
// toDomain: banco → domínio
public Matricula toDomain(MatriculaRow row) {
    var periodo = new PeriodoLetivo(
        row.periodoInicio.getYear(),
        row.periodoInicio.getMonthValue() <= 6 ? 1 : 2  // data SQL → semestre
    );
    var status = reconstruirStatus(row);  // String → sealed StatusMatricula
    var disciplinas = row.itens.stream()
        .map(item -> new ItemMatricula(new NomeDisciplina(item.disciplina)))
        .toList();
    return new Matricula(row.id, row.alunoId, row.turmaId, periodo, status, disciplinas);
}

// fromDomain: domínio → banco
public MatriculaRow fromDomain(Matricula matricula) {
    var row = new MatriculaRow();
    row.id = matricula.getId();
    row.periodoInicio = LocalDate.of(ano, semestre == 1 ? 2 : 8, 1);  // semestre → data SQL
    row.status = switch (matricula.getStatus()) {
        case StatusMatricula.Ativa a     -> "ATIVA";
        case StatusMatricula.Cancelada c -> "CANCELADA";
        case StatusMatricula.Concluida c -> "CONCLUIDA";
    };
    // …
    return row;
}
```

Conversões não-triviais acontecem aqui (e só aqui): `PeriodoLetivo` ↔ datas SQL, `StatusMatricula` sealed ↔ `VARCHAR`.

### 4.4 O XML só produz `Row` — nunca domínio

O `resultMap` no XML aponta para `MatriculaRow`, não para `Matricula`:

```xml
<resultMap id="MatriculaResultMap" type="MatriculaRow">
  <id     property="id"      column="matricula_id"/>
  <result property="status"  column="status"/>
  <collection property="itens" ofType="ItemMatriculaRow" notNullColumn="item_disciplina">
    <result property="disciplina" column="item_disciplina"/>
  </collection>
</resultMap>
```

O XML não conhece `Matricula`. Quem faz a ponte é o `MatriculaRepositorioMyBatis`:

```java
public Optional<Matricula> buscarPorId(UUID id) {
    MatriculaRow row = mapper.buscarPorId(id);          // XML → Row
    return Optional.ofNullable(row).map(rowMapper::toDomain); // Row → Domínio
}
```

### 4.5 Para adicionar conversão de novo campo

1. Adicionar coluna ao SQL + alias no `<select>`
2. Adicionar campo em `XxxRow`
3. Mapear no `<resultMap>` com `<result property="…" column="…"/>`
4. Converter em `XxxRowMapper.toDomain()` e `fromDomain()`

Referências: `MatriculaRow.java`, `MatriculaRowMapper.java`, `MatriculaMapper.xml`, `MatriculaRepositorioMyBatis.java`

### 4.6 Quando usar TypeHandler em vez de RowMapper

Dois mecanismos distintos fazem a conversão VO ↔ banco:

| Mecanismo | Usa quando | Exemplo |
|-----------|-----------|---------|
| `RowMapper` | VO tem **mais de uma coluna** no banco | `PeriodoLetivo(ano, semestre)` → duas colunas `DATE` |
| `TypeHandler` | VO tem **exatamente uma coluna** no banco | `Cpf` → uma coluna `VARCHAR(11)` |

**TypeHandler** é registrado uma vez e o MyBatis aplica automaticamente em qualquer query que retorne ou receba aquele tipo Java — não é preciso escrever conversão no RowMapper.

#### Estrutura canônica — `CpfTypeHandler`

```java
@MappedTypes(Cpf.class)          // tipo Java que este handler converte
@MappedJdbcTypes(JdbcType.VARCHAR) // tipo JDBC correspondente
public class CpfTypeHandler extends BaseTypeHandler<Cpf> {

    // escrita: Java → banco (INSERT / UPDATE / WHERE)
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Cpf cpf, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, cpf.valor()); // extrai o valor primitivo do VO
    }

    // leitura por nome de coluna (ResultMap e ResultType)
    @Override
    public Cpf getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String valor = rs.getString(columnName);
        return valor != null ? new Cpf(valor) : null; // reconstrói o VO
    }

    // leitura por índice (ResultSet posicional)
    @Override
    public Cpf getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String valor = rs.getString(columnIndex);
        return valor != null ? new Cpf(valor) : null;
    }

    // leitura de stored procedures (obrigatório pela interface)
    @Override
    public Cpf getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String valor = cs.getString(columnIndex);
        return valor != null ? new Cpf(valor) : null;
    }
}
```

#### Registro automático — `application.yml`

```yaml
mybatis:
  type-handlers-package: br.com.escola.matricula.infraestrutura.persistencia.typehandler
```

Todos os handlers no pacote são detectados automaticamente. Não é preciso registrar `@Bean` manualmente.

#### Para adicionar um TypeHandler para um novo VO

1. Criar `NovoVOTypeHandler` em `infraestrutura/persistencia/typehandler/`
2. Herdar `BaseTypeHandler<NovoVO>`
3. Anotar com `@MappedTypes(NovoVO.class)` e `@MappedJdbcTypes(JdbcType.VARCHAR)` (ou o tipo JDBC correto)
4. Implementar os 4 métodos — `setNonNullParameter` extrai o valor primitivo; `getNullableResult` reconstrói o VO
5. **Nada mais** — o package scanning registra automaticamente

Referências: `typehandler/CpfTypeHandler.java`, `typehandler/UUIDTypeHandler.java`

---

## 5. MyBatis: Pitfalls Críticos

### Pitfall 1 — UUID PostgreSQL exige `jdbcType=OTHER`

```xml
<!-- ERRADO — causa "operator does not exist: uuid = character varying" -->
WHERE id = #{id}

<!-- CERTO -->
WHERE id = #{id, jdbcType=OTHER}
```

**Regra:** todo parâmetro UUID no XML precisa de `jdbcType=OTHER`. Aplica-se a INSERT, UPDATE, DELETE e SELECT. Ver `MatriculaMapper.xml` — todos os parâmetros UUID usam essa anotação.

### Pitfall 2 — LEFT JOIN sem `notNullColumn` cria objetos fantasma

```xml
<!-- ERRADO — matrícula sem itens gera ItemMatriculaRow(disciplina=null) na lista -->
<collection property="itens" ofType="ItemMatriculaRow">

<!-- CERTO — lista fica vazia quando não há itens -->
<collection property="itens" ofType="ItemMatriculaRow" notNullColumn="item_disciplina">
```

**Regra:** sempre usar `notNullColumn` em `<collection>` com LEFT JOIN. O valor deve ser o alias de uma coluna que é NOT NULL quando a coleção existe. Ver `MatriculaMapper.xml`, bloco `MatriculaResultMap`.

### Pitfall 3 — Construtor de reconstituição precisa inicializar `eventos`

Ao criar um novo Aggregate com lista de Domain Events, o construtor de reconstituição (chamado pelo RowMapper via banco) **deve** incluir `this.eventos = new ArrayList<>()`. Sem isso, chamar métodos que adicionam eventos lança `NullPointerException`.

Ver `dominio/modelo/Matricula.java` — o construtor público de reconstituição inicializa `eventos`.

### Pitfall 4 — Domain Service sem `@Service`

Domain Services em `dominio/servico/` **não têm** `@Service` — o pacote `dominio/` não importa Spring. Eles são configurados como `@Bean` em `infraestrutura/config/DomainServicesConfig.java`.

```java
// infraestrutura/config/DomainServicesConfig.java
@Configuration
public class DomainServicesConfig {
    @Bean
    public VerificadorElegibilidadeMatricula verificadorElegibilidade(MatriculaRepositorio repositorio) {
        return new VerificadorElegibilidadeMatricula(repositorio);
    }
}
```

### Pitfall 5 — Estratégia replace-all para coleções

A persistência de `salvar()` usa replace-all para itens da coleção:
1. `mapper.atualizarMatricula(row)` — tenta UPDATE; se retornar 0, faz INSERT
2. `mapper.deletarItensPorMatriculaId(matricula.getId())` — apaga todos os itens
3. `mapper.inserirItens(itens)` — reinsere o estado atual completo

Nunca fazer INSERT/UPDATE item-a-item com diff — tratar a coleção como unidade atômica.

---

## 6. Value Objects: Padrão com Java 21 Record

```java
// dominio/vo/NovoVO.java
public record NovoVO(String valor) {
    public NovoVO {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("NovoVO não pode ser vazio");
        }
        valor = valor.trim(); // normalização no construtor compacto
    }
}
```

- Igualdade por valor é automática com `record` — não implementar `equals`/`hashCode` manualmente
- Validação no construtor compacto garante invariantes na criação
- Referência para VO com validação complexa (dígito verificador): `dominio/vo/Cpf.java`

---

## 7. Schema PostgreSQL: Convenções

Detalhes em `src/main/resources/db/migration/V1__schema.sql` e `docs/adrs/ADR-003-*.md`.

Regras resumidas:
- Uma tabela por Aggregate Root + tabelas de entidades internas (com FK + `ON DELETE CASCADE`)
- Referências a outros Aggregates: apenas UUID **sem FOREIGN KEY** (ADR-003)
- Status como `VARCHAR(20)` com `CHECK` constraint — não usar enum PostgreSQL
- UUID como tipo nativo — não `VARCHAR(36)`
- Migrações em `src/main/resources/db/migration/` com prefixo `V{N}__descricao.sql`

---

## 8. Convenções de Nomenclatura

Todos os identificadores em **português** (ADR-004). Ver `docs/adrs/ADR-004-codigo-em-portugues.md`.

| Artefato | Convenção | Exemplo |
|----------|-----------|---------|
| Aggregate | Substantivo singular | `Matricula` |
| UseCase (escrita) | Verbo + Substantivo + UseCase | `MatricularAlunoUseCase` |
| UseCase (leitura) | Buscar/Listar + Substantivo + UseCase | `BuscarMatriculaUseCase` |
| Command | Verbo + Substantivo + Command | `MatricularAlunoCommand` |
| DTO | Substantivo + Dto | `MatriculaDto` |
| Repositório (interface) | Substantivo + Repositorio | `MatriculaRepositorio` |
| Repositório (impl) | Substantivo + Repositorio + MyBatis | `MatriculaRepositorioMyBatis` |
| Mapper | Substantivo + Mapper | `MatriculaMapper` |
| Row | Substantivo + Row | `MatriculaRow` |
| RowMapper | Substantivo + RowMapper | `MatriculaRowMapper` |
| TypeHandler | NomeVO + TypeHandler | `CpfTypeHandler` |
| Domain Service | VerboDeRegra + Substantivo | `VerificadorElegibilidadeMatricula` |
| Domain Event | EventoOcorrido (passado) | `AlunoMatriculado`, `MatriculaCancelada` |
| Exceção de domínio | SituacaoException | `AlunoInativoException` |

---

## 9. Referências Rápidas

Caminhos relativos a `erp-matricula-ddd/src/main/java/br/com/escola/matricula/` salvo indicação:

| Tipo | Arquivo de referência |
|------|-----------------------|
| Value Object simples | `dominio/vo/PeriodoLetivo.java` |
| Value Object com validação complexa | `dominio/vo/Cpf.java` |
| Aggregate Root completo | `dominio/modelo/Matricula.java` |
| Status com sealed interface | `dominio/modelo/StatusMatricula.java` |
| Domain Service sem Spring | `dominio/servico/VerificadorElegibilidadeMatricula.java` |
| Repositório (interface) | `dominio/repositorio/MatriculaRepositorio.java` |
| UseCase de escrita | `aplicacao/MatricularAlunoUseCase.java` |
| Command | `aplicacao/MatricularAlunoCommand.java` |
| DTO com factory method | `aplicacao/MatriculaDto.java` |
| Repositório (impl MyBatis) | `infraestrutura/persistencia/MatriculaRepositorioMyBatis.java` |
| Mapper interface | `infraestrutura/persistencia/MatriculaMapper.java` |
| RowMapper (conversão domínio/banco) | `infraestrutura/persistencia/MatriculaRowMapper.java` |
| XML com JOIN + collection | `erp-matricula-ddd/src/main/resources/mapper/MatriculaMapper.xml` |
| Controller REST | `interfaces/MatriculaController.java` |
| Config de Domain Services | `infraestrutura/config/DomainServicesConfig.java` |
| Schema PostgreSQL | `erp-matricula-ddd/src/main/resources/db/migration/V1__schema.sql` |
