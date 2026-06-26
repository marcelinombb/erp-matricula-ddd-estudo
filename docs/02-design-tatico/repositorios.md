# Repositórios — Matrícula Escolar

> Esta decisão é documentada em [ADR-001: MyBatis vs JPA](../adrs/ADR-001-mybatis-vs-jpa.md). O repositório como interface de domínio é a consequência direta dessa escolha arquitetural.

Como o Aggregate `Matricula` é salvo no banco?

A resposta mais natural em um projeto Spring Boot seria: adicionar `@Entity`, `@Table`, `@Column` à classe `Matricula` e deixar o JPA cuidar do resto. Mas olhe o que acontece com a classe de domínio: ela passa a ter `import jakarta.persistence.Entity`, `import jakarta.persistence.Table`, `import jakarta.persistence.OneToMany`. A classe `Matricula` — que representa uma regra de negócio sobre matrículas escolares — agora depende do framework de persistência para existir. Mudar de banco de dados, ou trocar JPA por outra tecnologia, exigiria mudar a classe de domínio. A infraestrutura ditou como o domínio deve ser escrito.

A solução DDD é a inversão dessa dependência: a classe `Matricula` não sabe como é persistida. Uma **interface no domínio** define o contrato de persistência em termos do próprio domínio; a **infraestrutura** implementa esse contrato usando a tecnologia escolhida. O domínio define o que quer; a infraestrutura sabe como fazer.

---

## A Interface no Domínio

```java
// Java 21: interface pura — zero imports de framework
// DDD fit: domínio define o contrato; infraestrutura cumpre — Regra de Dependência em ação
public interface MatriculaRepositorio {
    Optional<Matricula> buscarPorId(UUID id);
    List<Matricula> buscarPorAluno(UUID alunoId);
    boolean existeMatriculaAtiva(UUID alunoId, PeriodoLetivo periodo);
    void salvar(Matricula matricula);
}
```

Observe o que não está aqui: sem `extends JpaRepository`, sem `import org.springframework.data`, sem `import org.apache.ibatis`. A interface `MatriculaRepositorio` é definida em termos do próprio domínio: recebe e retorna `UUID`, `PeriodoLetivo`, `Matricula` — todos tipos do domínio ou do JDK.

Os nomes dos métodos são em português (`buscarPorId`, `buscarPorAluno`) — mantendo a Linguagem Ubíqua também nas interfaces. Ver [ADR-004](../adrs/ADR-004-codigo-em-portugues.md).

---

## A Implementação na Infraestrutura

A implementação MyBatis vive no pacote `infraestrutura/` e é a única parte do sistema que sabe como converter entre o modelo de domínio e o banco de dados:

```java
// infraestrutura/ — aqui entra o MyBatis
// O domínio não sabe que isso existe — depende de MatriculaRepositorio (interface), não desta classe
@Repository // única anotação Spring — e está na infraestrutura, não no domínio
public class MatriculaRepositorioMyBatis implements MatriculaRepositorio {
    // implementação com MyBatis Mapper — entregue na Fase 3 (Implementação)
}
```

A implementação MyBatis completa é entregue na Fase 3 (Implementação). Aqui interessa o contrato — o domínio define, a infraestrutura cumpre.

> **Regra de Dependência em ação:** O pacote `dominio/` define `MatriculaRepositorio` (interface). O pacote `infraestrutura/` implementa `MatriculaRepositorioMyBatis`. A seta de dependência vai de `infraestrutura/` para `dominio/` — nunca o contrário. O domínio não importa nada da infraestrutura.

---

## Verificação da Separação

O domínio não importa nenhuma classe do MyBatis. A verificação é simples:

```bash
grep -r "import org.apache.ibatis" src/main/java/*/dominio/
# Resultado esperado: nenhuma ocorrência
```

Esta verificação faz parte da disciplina do projeto. Se o grep retornar qualquer resultado, há vazamento de infraestrutura no domínio. A mesma verificação pode ser feita para qualquer framework que não deveria estar no domínio:

```bash
grep -r "import org.springframework" src/main/java/*/dominio/
# Resultado esperado: nenhuma ocorrência
# (Spring pertence à infraestrutura e à aplicação — nunca ao domínio)
```

---

## O que essa Escolha Implica

### Positivas

- Domínio sem imports de framework — verificável com um grep (veja acima)
- Fácil trocar MyBatis por outra tecnologia de persistência sem tocar nas classes de domínio: basta implementar `MatriculaRepositorio` com a nova tecnologia
- Testes de domínio sem banco real — basta criar um mock ou implementação em memória da interface `MatriculaRepositorio`
- Pedagogia clara: quando o aluno lê `MatriculaRepositorioMyBatis`, entende imediatamente por que essa classe existe em `infraestrutura/` e não em `dominio/`

### Negativas (Trade-offs)

- Mais código de mapeamento escrito manualmente: cada query, cada `ResultMap`, cada `<collection>` MyBatis precisam ser escritos explicitamente em XML
- Nenhuma auto-geração de queries — operações como "inserir uma matrícula com suas disciplinas" exigem queries separadas na ordem correta (Aggregate Root primeiro, filhos depois)
- Curva de aprendizado do XML MyBatis para desenvolvedores habituados ao Spring Data — a sintaxe de `ResultMap` com `<collection>` é inicialmente estranha

---

## Erros Comuns

### Erro 1: `extends JpaRepository` — domínio herda de framework

O padrão Spring Data JPA é `public interface MatriculaRepository extends JpaRepository<Matricula, UUID>`. À primeira vista parece limpo — a interface não tem implementação. Mas ela herda de `JpaRepository`, que está em `jakarta.persistence`. O domínio importa o framework de persistência.

```java
// ERRADO — Spring Data JPA: o domínio herda de JpaRepository (dependência de framework)
// MatriculaRepository interface (extends JpaRepository) vs MatriculaRepositorio (interface pura)
public interface MatriculaRepository extends JpaRepository<Matricula, UUID> {
    // JpaRepository está em jakarta.persistence — o domínio importa framework
    // findByAlunoId em inglês — quebra a Linguagem Ubíqua
    List<Matricula> findByAlunoId(UUID alunoId);
}
```

```java
// CERTO — Interface pura no domínio, sem herança de framework — ver ADR-001
// Java 21: interface pura — zero imports de framework
// DDD fit: domínio define o contrato; infraestrutura cumpre — Regra de Dependência em ação
public interface MatriculaRepositorio {
    Optional<Matricula> buscarPorId(UUID id);              // buscarPorId em português
    List<Matricula> buscarPorAluno(UUID alunoId);          // nomes expressam semântica de negócio
    boolean existeMatriculaAtiva(UUID alunoId, PeriodoLetivo periodo);
    void salvar(Matricula matricula);
    // zero imports de framework — o domínio define o contrato em seus próprios termos
}
```

O contraste central: `MatriculaRepository` (ERRADO) herda de `JpaRepository` — o domínio depende do framework. `MatriculaRepositorio` (CERTO) é interface pura — o domínio é independente. Os nomes dos métodos expressam a semântica de negócio (`buscarPorAluno`, `existeMatriculaAtiva`), não termos técnicos de framework. Ver [ADR-003](../adrs/ADR-003-referencia-por-id.md) para o princípio de referência por ID.

### Erro 2: `@Entity` na classe de domínio

Colocar `@Entity` na classe `Matricula` é o sintoma mais visível do vazamento de persistência no domínio — a infraestrutura dita como o domínio deve ser escrito.

```java
// ERRADO — anotações de persistência na classe de domínio
@Entity
@Table(name = "matriculas")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "matricula_id")
    private List<ItemMatricula> itens;

    // O domínio agora importa jakarta.persistence.* — depende do framework
    // fetch = FetchType.LAZY é uma decisão de persistência, não de negócio
}
```

```java
// CERTO — domínio limpo, sem anotações de persistência — ver ADR-001
public class Matricula {
    private final UUID id;
    private final UUID alunoId;
    private final UUID turmaId;
    private final PeriodoLetivo periodoLetivo;
    private StatusMatricula status;
    private final List<ItemMatricula> disciplinas;

    // zero anotações de persistência — ver ADR-001
    // MatriculaRepositorioMyBatis (infraestrutura/) sabe como persistir esta classe
}
```

No CERTO, `Matricula` é Java puro. Como ela chega ao banco? Via `MatriculaRepositorioMyBatis` em `infraestrutura/`, que sabe exatamente como transformar um objeto `Matricula` em queries SQL — sem que `Matricula` saiba que SQL existe.
