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

## 4. MyBatis: Pitfalls Críticos

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

## 5. Value Objects: Padrão com Java 21 Record

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

## 6. Schema PostgreSQL: Convenções

Detalhes em `src/main/resources/db/migration/V1__schema.sql` e `docs/adrs/ADR-003-*.md`.

Regras resumidas:
- Uma tabela por Aggregate Root + tabelas de entidades internas (com FK + `ON DELETE CASCADE`)
- Referências a outros Aggregates: apenas UUID **sem FOREIGN KEY** (ADR-003)
- Status como `VARCHAR(20)` com `CHECK` constraint — não usar enum PostgreSQL
- UUID como tipo nativo — não `VARCHAR(36)`
- Migrações em `src/main/resources/db/migration/` com prefixo `V{N}__descricao.sql`

---

## 7. Convenções de Nomenclatura

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

## 8. Referências Rápidas

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
