# ADR-001: MyBatis em vez de JPA/Hibernate

**Status:** Aceito
**Data:** 2026-06-20
**Contexto da fase:** Fase 1 — Design Estratégico

## Contexto

Em projetos Java com banco relacional, a escolha padrão é JPA/Hibernate via Spring Data JPA. Este projeto deliberadamente rejeita essa escolha — a motivação precisa ser explícita.

O problema com JPA em projetos DDD: anotações de persistência entram nas classes de domínio. Veja o contraste abaixo:

```java
// COM JPA — anotações de persistência no modelo de domínio (PROBLEMA)
@Entity
@Table(name = "matriculas")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_id")
    private List<ItemMatricula> itens;

    // O domínio agora importa jakarta.persistence.* — depende do framework
}
```

```java
// COM MYBATIS — modelo de domínio limpo (DECISÃO TOMADA)
public class Matricula {
    private final MatriculaId id;
    private final List<ItemMatricula> itens;

    // Zero imports de framework — puro Java
    // A conversão para/de banco ocorre em MatriculaRowMapper (infraestrutura/)
}
```

O modelo de domínio com JPA importa `jakarta.persistence.*` — isso viola o princípio DDD de que o domínio não conhece infraestrutura. Detalhes como `fetch = FetchType.LAZY` e `cascade = CascadeType.ALL` são decisões de persistência que nada têm a ver com as regras de negócio de uma matrícula. O desenvolvedor vindo de Spring Boot normalmente não percebe o problema até ver o import explicitamente: a classe `Matricula` passou a depender do framework de persistência para existir.

## Alternativas Consideradas

### Opção A: JPA/Hibernate via Spring Data JPA

**Prós:**
- Padrão de mercado — todo desenvolvedor Java já conhece
- Auto-CRUD via `extends JpaRepository<Matricula, UUID>` — menos código a escrever
- Gerenciamento de ciclo de vida de entidades automático (dirty checking, cascade)
- Documentação e suporte extensivos

**Contras:**
- Anotações de persistência (`@Entity`, `@Id`, `@Column`, `@OneToMany`) entram nas classes de domínio
- O domínio passa a depender de `jakarta.persistence.*` — violação do princípio de independência de framework
- Lazy loading e proxy Hibernate criam comportamentos implícitos difíceis de rastrear
- O conceito de "entidade gerenciada" (managed entity) contradiz a imutabilidade desejada em Value Objects DDD

### Opção B: Spring Data JDBC

**Prós:**
- Mais próximo de DDD que JPA — sem lazy loading, sem dirty checking implícito
- Ainda usa anotações, mas menos invasivas que JPA
- Suporte nativo a aggregates com tabelas vinculadas

**Contras:**
- Ainda impõe `@Table` e `@Id` nas classes de domínio — o problema da dependência de framework não é eliminado, apenas reduzido
- Modelo de aggregate do Spring Data JDBC tem restrições (sem referencias circulares, sem coleções de entidades sem ID próprio)
- Menos flexibilidade para queries complexas com JOINs customizados

### Opção C: JDBC puro sem framework

**Prós:**
- Máximo controle e transparência — nada acontece sem código explícito
- Zero dependências de framework no domínio

**Contras:**
- Muito verboso: `ResultSet`, `PreparedStatement`, tratamento de `SQLException` em toda query
- Sem ganho didático adicional sobre MyBatis — o ponto pedagógico (separação domínio/persistência) já fica claro com MyBatis
- Mais código boilerplate dificulta a leitura do que importa: o mapeamento do Aggregate

## Decisão

Usamos **MyBatis** (`mybatis-spring-boot-starter` 3.0.5). O mapeamento SQL é explícito em arquivos `.xml` dentro de `infraestrutura/`. A conversão entre modelo relacional e modelo de domínio ocorre em classes `*RowMapper` dentro do pacote `infraestrutura/` — nunca dentro do domínio.

O domínio (`dominio/`) não importa nenhuma classe do MyBatis. A verificação é simples:

```bash
grep -r "import org.apache.ibatis" src/main/java/br/com/escola/matricula/dominio/
# Resultado esperado: nenhuma ocorrência
```

`MatriculaRowMapper` (infraestrutura) é o único lugar que sabe como transformar um `ResultSet` em um objeto `Matricula`. O domínio descreve o que uma matrícula é; a infraestrutura sabe como ela é armazenada.

## Consequências

### Positivas

- Domínio sem imports de framework — verificável com um `grep` (veja acima)
- Conversão explícita e legível em `*RowMapper`: o desenvolvedor sabe exatamente como o banco mapeia para o domínio
- Mapeamento de Aggregate complexo com `JOIN` + `<collection>` no XML: todas as disciplinas de uma matrícula são carregadas em uma única query, sem magic de lazy loading
- Pedagogia clara da separação domínio/persistência — quando o aluno lê `MatriculaRowMapper`, entende por que essa classe existe em `infraestrutura/` e não em `dominio/`

### Negativas (Trade-offs)

- Mais XML e código de mapeamento escrito manualmente — cada query, cada `ResultMap`, cada `<collection>` precisam ser escritos a mão
- Sem auto-CRUD: operações como "inserir uma matrícula com suas disciplinas" exigem queries separadas na ordem correta (Aggregate Root primeiro, filhos depois)
- Curva de aprendizado do XML MyBatis para desenvolvedores habituados ao Spring Data — a sintaxe de `ResultMap` com `<collection>` é inicialmente estranha

## Referências

- MyBatis Result Maps (oficial): https://mybatis.org/mybatis-3/sqlmap-xml.html
- Separating Persistence and Domain Models: https://urgo.medium.com/separating-persistence-and-domain-models-cc3a7e7cd4e5
- mybatis-spring-boot-starter releases: https://github.com/mybatis/spring-boot-starter/releases

## Na prática

Esta decisão é visível em dois arquivos que devem ser lidos juntos:

**[MatriculaRow.java](../../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRow.java)** — o modelo relacional plano. É um objeto Java sem comportamento, sem anotações de domínio, sem métodos de negócio. Seus campos espelham diretamente as colunas da tabela `matriculas`: `id`, `alunoId`, `turmaId`, `periodoInicio`, `periodoFim`, `status`, `canceladaEm`, `concluidaEm`. O MyBatis popula este objeto; o domínio nunca o vê.

**[MatriculaRowMapper.java](../../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRowMapper.java)** — a classe que separa explicitamente os dois mundos. O Javadoc da linha 17 nomeia isso diretamente: "ÚNICO arquivo que conhece tanto `MatriculaRow` quanto `Matricula`."

O padrão de conversão é explícito e navegável:
- **Banco → domínio:** `MatriculaRowMapper.toDomain()` converte `MatriculaRow` em `Matricula`, reconstruindo os Value Objects (`AlunoId`, `TurmaId`, `PeriodoLetivo`) e a sealed interface `StatusMatricula`.
- **Domínio → banco:** `MatriculaRowMapper.fromDomain()` converte `Matricula` em `MatriculaRow`, serializando os Value Objects de volta para tipos primitivos e strings.

Nenhuma linha de código do domínio sabe que o banco existe. Qualquer mudança no schema (adicionar coluna, renomear campo) impacta `MatriculaRow` e `MatriculaRowMapper` — nunca `Matricula.java`.
