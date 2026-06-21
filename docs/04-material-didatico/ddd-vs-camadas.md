# DDD para quem vem da Arquitetura em Camadas

Este documento mostra como cada artefato familiar da arquitetura em camadas tem um equivalente DDD no código deste projeto. DDD não é apenas nomenclatura diferente — é responsabilidade diferente. Cada seção compara o padrão convencional com a abordagem DDD e explica por que a mudança importa.

---

## 1. De `@Service` para UseCase + Domain Service

### Arquitetura em Camadas

```java
@Service
public class MatriculaService {

    public void matricular(UUID alunoId, UUID turmaId, String periodo) {
        // Tudo no mesmo método: validação, persistência, eventos
        if (!aluno.ativo) throw new RuntimeException("Aluno inativo");
        if (!periodo.aberto) throw new RuntimeException("Período fechado");
        if (jaExisteMatricula(alunoId, periodo)) throw new RuntimeException("Duplicada");

        Matricula matricula = new Matricula(alunoId, turmaId, periodo);
        matriculaRepo.save(matricula);
        eventPublisher.publish(new AlunoMatriculadoEvent(matricula));
    }
}
```

O `@Service` cresce sem parar: validações, persistência, publicação de eventos — tudo no mesmo lugar. É o caminho natural para um God Object.

### DDD (neste projeto)

Três papéis separados:

```java
// 1. Domain Service: decide se pode matricular (lança exceção se não pode)
//    erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java
verificador.verificar(aluno, turma, periodo);

// 2. Aggregate: decide o estado inicial e protege invariantes
//    erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java
Matricula matricula = Matricula.criar(alunoId, turmaId, periodo);

// 3. UseCase: orquestra sem decidir — sequência obrigatória
//    erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java
repositorio.salvar(matricula);
matricula.coletarEventos().forEach(publicador::publishEvent);
```

Ver: [`MatricularAlunoUseCase.java`](../erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java) — o Javadoc documenta explicitamente "O que este UseCase faz" e "O que NÃO faz".

**Por que importa:** O `@Service` tradicional cresce e vira God Object. O `UseCase` orquestra e o `Domain Service` encapsula regras — papéis separados, responsabilidades claras. Quando a regra de elegibilidade muda, só `VerificadorElegibilidadeMatricula` muda.

---

## 2. De `@Entity` para Aggregate Root sem anotações

### Arquitetura em Camadas

```java
@Entity
@Table(name = "matriculas")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aluno_id")
    private UUID alunoId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "matricula_id")
    private List<ItemMatricula> itens;
    // O domínio importa jakarta.persistence.* — depende do framework para existir
}
```

As anotações `@Entity`, `@Id`, `@Column`, `@OneToMany` entram na classe de domínio. `Matricula` passa a depender de `jakarta.persistence.*` para compilar.

### DDD (neste projeto)

```java
// erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java
public class Matricula {
    private final MatriculaId id;
    private final AlunoId alunoId;
    private StatusMatricula status;
    private final List<ItemMatricula> disciplinas;

    // Métodos de domínio protegem invariantes:
    public void adicionarDisciplina(NomeDisciplina disciplina) {
        if (this.status instanceof StatusMatricula.Cancelada) throw new MatriculaCanceladaException(this.id);
        if (this.disciplinas.size() >= LIMITE_DISCIPLINAS) throw new LimiteDisciplinasExcedidoException(...);
        // ...
    }

    public void cancelar() { ... }
}
```

Verifique: `grep "import org.springframework" Matricula.java` retorna zero ocorrências. Zero imports de framework — o domínio existe independentemente de qualquer infraestrutura.

**Por que importa:** O Aggregate protege suas próprias invariantes via métodos de domínio. Nenhuma regra de negócio fica fora de `Matricula`. Sem DDD, `adicionarDisciplina()` não existe — a lógica de limite de disciplinas estaria num `@Service` e poderia ser duplicada ou contornada. Com o ADR-001 (MyBatis), a separação é explícita: `Matricula.java` nunca vê o banco.

---

## 3. De `@Repository extends JpaRepository` para Interface de Domínio + Implementação Separada

### Arquitetura em Camadas

```java
// O framework define a interface — o domínio depende do Spring Data
public interface MatriculaRepository extends JpaRepository<Matricula, UUID> {
    List<Matricula> findByAlunoId(UUID alunoId);
    boolean existsByAlunoIdAndStatusAndPeriodo(UUID alunoId, String status, String periodo);
}
```

A interface pertence ao framework: `extends JpaRepository` acopla o domínio ao Spring Data. Os nomes dos métodos seguem convenção do framework (`findBy`, `existsBy`), não a Linguagem Ubíqua.

### DDD (neste projeto)

Dois arquivos com papéis opostos:

```java
// 1. Interface no DOMÍNIO — o domínio define o contrato
//    erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java
public interface MatriculaRepositorio {
    Optional<Matricula> buscarPorId(MatriculaId id);
    List<Matricula> buscarPorAluno(AlunoId alunoId);
    boolean existeMatriculaAtiva(AlunoId alunoId, PeriodoLetivo periodo);
    void salvar(Matricula matricula);
    // Zero imports de framework — puro Java
}

// 2. Implementação na INFRAESTRUTURA — a infraestrutura obedece
//    erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRepositorioMyBatis.java
@Repository
public class MatriculaRepositorioMyBatis implements MatriculaRepositorio {
    // MyBatis aqui — o domínio não sabe que este arquivo existe
}
```

Verifique `MatriculaRepositorio.java`: nenhum `import org.springframework.*`, nenhum `import org.apache.ibatis.*`. A interface é definida em termos do próprio domínio.

**Por que importa:** A interface de repositório pertence ao domínio porque os casos de uso a usam — não à infraestrutura que a implementa. Princípio da Inversão de Dependência: a seta de dependência vai de `infraestrutura/` para `dominio/`, nunca o contrário. Isso torna o domínio testável sem banco real: basta implementar `MatriculaRepositorio` com um `Map` em memória.

---

## 4. De `String status` para Sealed Interface com Pattern Matching

### Arquitetura em Camadas

```java
// "Magic strings" — compilador não detecta estados inválidos
String status = "ATIVA";
if (status.equals("CANCELADA")) {
    // E se alguém escrever "CANCELADO" (com O)? Erro silencioso.
}
```

Nenhuma garantia em tempo de compilação. Um novo estado adicionado anos depois pode passar despercebido em todos os `if` espalhados pelo código.

### DDD (neste projeto)

```java
// erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/modelo/StatusMatricula.java
public sealed interface StatusMatricula
        permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida {
    record Ativa() implements StatusMatricula {}
    record Cancelada(LocalDateTime canceladaEm) implements StatusMatricula {}
    record Concluida(LocalDateTime concluidaEm) implements StatusMatricula {}
}
```

Pattern matching exaustivo sem `default`:

```java
// Em MatriculaRowMapper.java — switch exaustivo:
row.status = switch (matricula.getStatus()) {
    case StatusMatricula.Ativa a     -> "ATIVA";
    case StatusMatricula.Cancelada c -> "CANCELADA";
    case StatusMatricula.Concluida c -> "CONCLUIDA";
    // Sem default: se um novo estado for adicionado, este switch não compila
};
```

**Por que importa:** O compilador detecta estados não tratados. Se um quarto estado `Trancada` for adicionado no futuro, todos os switches que não o tratam deixam de compilar — o time recebe uma lista exata de onde o novo estado precisa ser tratado.

---

## 5. De `RuntimeException` genérica para Exceções Tipadas com Dados Estruturados

### Arquitetura em Camadas

```java
// Exceção genérica perde contexto
if (disciplinas.size() >= LIMITE) {
    throw new RuntimeException("Limite de disciplinas excedido");
    // A camada HTTP não sabe qual é o limite, qual é o valor atual,
    // qual é a matrícula — precisa parsear a String de mensagem
}
```

O handler HTTP recebe uma `RuntimeException` com uma mensagem String. Para retornar um response 422 estruturado com os campos `limite` e `atual`, precisaria parsear a mensagem — frágil e acoplado ao formato do texto.

### DDD (neste projeto)

```java
// erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/excecao/LimiteDisciplinasExcedidoException.java
public class LimiteDisciplinasExcedidoException extends RuntimeException {
    private final int limite;
    private final int atual;
    private final MatriculaId matriculaId;

    // getLimite(), getAtual(), getMatriculaId() — dados estruturados disponíveis
}
```

O `ExcecaoHandler` usa os dados diretamente:

```java
// erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/ExcecaoHandler.java
@ExceptionHandler(LimiteDisciplinasExcedidoException.class)
@ResponseStatus(UNPROCESSABLE_ENTITY)
public ErroLimiteResponse handleLimite(LimiteDisciplinasExcedidoException ex) {
    return new ErroLimiteResponse("LIMITE_DISCIPLINAS_EXCEDIDO",
        ex.getLimite(), ex.getAtual());
    // Sem parsear strings — dados estruturados do domínio chegam ao HTTP
}
```

**Por que importa:** Exceções tipadas com dados estruturados permitem que o handler HTTP produza responses ricos sem depender da formatação da mensagem de erro. O domínio comunica o contexto do problema; a interface apenas o formata para o protocolo HTTP.

---

## Conclusão

DDD adiciona estrutura, não burocracia. Cada camada tem um propósito claro:

| Camada | Papel | O que NÃO faz |
|--------|-------|----------------|
| `dominio/` | Decide as regras, protege invariantes | Não sabe que o banco existe |
| `aplicacao/` | Orquestra a sequência | Não contém regras de negócio |
| `infraestrutura/` | Implementa contratos técnicos | Não define comportamento de negócio |
| `interfaces/` | Traduz protocolo HTTP ↔ domínio | Não decide nada |

O resultado é um código que comunica sua intenção. Um desenvolvedor que lê `Matricula.adicionarDisciplina()` vê a regra de negócio — não uma consulta SQL, não um payload HTTP, não uma configuração de framework.
