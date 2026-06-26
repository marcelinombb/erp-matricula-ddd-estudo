# Agregados — Matrícula Escolar

Onde deveria ficar a regra que diz que uma matrícula pode ter no máximo 6 disciplinas?

A resposta parece óbvia: "no Service". É lá que fica a lógica de negócio em arquitetura em camadas. Mas qual Service? Se existe um `MatriculaService` e um `DisciplinaService`, cada um precisaria conhecer essa regra. E se amanhã surgir um `ImportacaoService` que também adiciona disciplinas em lote — ele precisa replicar a mesma verificação? Cada ponto que precisa conhecer a regra é um ponto onde a regra pode divergir.

E tem outro problema, mais sutil. Dois usuários diferentes tentam adicionar disciplinas à mesma matrícula ao mesmo tempo. Cada um carrega a matrícula do banco — ambos veem 5 disciplinas. Cada um verifica: "5 < 6, pode adicionar". Cada um salva. Resultado: 7 disciplinas na matrícula, quebrando a regra que "protegemos" no Service. Não foi falta de código — o código estava lá. Foi falta de garantia: a verificação e a modificação aconteceram em momentos separados, sem coordenação.

A solução DDD para esse problema se chama **Aggregate** (Agregado). Um Aggregate é uma fronteira de consistência — um conjunto de objetos relacionados que deve ser modificado como uma unidade, com uma única porta de entrada que garante as invariantes. Essa porta de entrada é o **Aggregate Root** (Raiz do Agregado): o objeto que encapsula o grupo e expõe os métodos que modificam o estado interno com as devidas verificações.

No domínio de Matrícula, o Aggregate Root é `Matricula`. Todas as regras sobre o que uma matrícula pode e não pode fazer vivem dentro desse objeto — não nos Services que o chamam.

## Estrutura do Aggregate Matricula

```
Aggregate Root: Matricula
├── UUID id (identidade)
├── UUID alunoId (referência por ID, ADR-003)
├── UUID turmaId (referência por ID, ADR-003)
├── PeriodoLetivo (VO — parte do Aggregate)
├── StatusMatricula (sealed interface)
├── List<ItemMatricula> (entidade interna)
└── List<DomainEvent> (eventos coletados — sem dependência de Spring)
```

`alunoId` e `turmaId` são referências por UUID — o Aggregate não carrega os objetos `Aluno` ou `Turma` dentro de si. Isso é intencional: `Matricula` precisa saber apenas que existe um aluno identificado por esse UUID, não seu nome, CPF, endereço ou histórico financeiro. Carregar o `Aluno` completo criaria acoplamento entre Aggregates de Bounded Contexts distintos. Ver [ADR-003](../adrs/ADR-003-referencia-por-id.md).

Os VOs `PeriodoLetivo` e `NomeDisciplina` são documentados em [value-objects.md](./value-objects.md).

---

### ItemMatricula — Entidade Interna

`ItemMatricula` representa uma disciplina incluída na matrícula. Diferente de `Aluno` e `Turma`, `ItemMatricula` não tem significado fora do Aggregate `Matricula` — uma disciplina incluída só existe como parte de uma matrícula. Por isso, `ItemMatricula` é uma **entidade interna** do Aggregate, e não uma Entidade independente (seu catálogo fica aqui, não em `entidades.md`).

Como entidade interna, `ItemMatricula` não precisa de ID próprio. Sua identidade é derivada da `Matricula` que a contém — é identificada pelo par (UUID da matrícula, NomeDisciplina). Por ser imutável após criação e não ter ciclo de vida independente, pode ser implementada como `record`:

```java
// ItemMatricula: entidade interna do Aggregate — sem ID próprio
// Exceção ao padrão "class para Entidade": dentro do Aggregate,
// ItemMatricula é tratada como composição imutável (sem ciclo de vida próprio).
// O record é adequado aqui porque um item criado não muda — só é adicionado ou removido.
public record ItemMatricula(NomeDisciplina disciplina) {}
```

A decisão de usar `record` para `ItemMatricula` reflete a realidade do domínio: um item de matrícula, uma vez criado, não muda. Se o aluno quiser substituir uma disciplina, ele remove a atual e adiciona uma nova. Não há "modificar uma disciplina já matriculada" — a operação correta é remover e incluir. Isso é diferente de `Aluno`, cujo status pode mudar enquanto o aluno permanece o mesmo.

---

## Invariantes do Aggregate Matricula

Uma invariante é uma regra de negócio que deve ser verdadeira em todo momento em que o Aggregate está em um estado válido. Não é uma regra que "normalmente vale" — é uma regra que o Aggregate garante ativamente, recusando qualquer operação que a violaria.

O Aggregate `Matricula` protege três invariantes:

**Limite de disciplinas:** Uma matrícula pode ter no máximo `LIMITE_DISCIPLINAS` disciplinas (valor padrão 6, configurável pela instituição). Esse limite existe por restrições operacionais e acadêmicas — um aluno não pode cursar mais disciplinas do que é razoável em um período. O número exato pode variar por instituição, por isso é uma constante nomeada, não um número mágico no código.

**Sem duplicidade:** A mesma disciplina não pode aparecer duas vezes na mesma matrícula. Uma matrícula com "Matemática Básica" listada duas vezes não faz sentido no domínio — a secretaria não saberia como calcular carga horária nem gerar o histórico acadêmico corretamente.

**Estado terminal:** Uma matrícula cancelada não pode receber novas disciplinas. Cancelamento é um estado terminal: após o cancelamento, a matrícula existe apenas como registro histórico. Qualquer tentativa de modificá-la é um erro de fluxo — provavelmente um bug na interface ou uma chamada fora de ordem.

| Invariante | Regra | Exceção Lançada |
|-----------|-------|-----------------|
| Limite de disciplinas | Máximo `LIMITE_DISCIPLINAS` (constante, valor padrão 6 — configurável pela instituição) por matrícula | `LimiteDisciplinasExcedidoException` |
| Sem duplicidade | A mesma disciplina não pode aparecer duas vezes na mesma matrícula | `DisciplinaJaMatriculadaException` |
| Estado terminal | Matrícula cancelada não recebe novas disciplinas | `MatriculaCanceladaException` |

> **Por que a invariante pertence ao Aggregate, não ao Service:** Se a verificação do limite de disciplinas estivesse no Service, duas threads concorrentes poderiam cada uma consultar a matrícula, encontrar 5 disciplinas, e adicionar mais uma — resultando em 7 disciplinas no banco. O Aggregate protege a invariante porque a verificação e a modificação acontecem dentro da mesma transação, no mesmo objeto carregado em memória. Não é apenas organização — é correção do sistema.

---

## Ciclo de Vida — StatusMatricula

Um Aggregate tem um ciclo de vida. `Matricula` pode estar em três estados possíveis — e somente três: ativa (pode receber e remover disciplinas), cancelada (estado terminal, sem modificações possíveis) ou concluída (período letivo encerrado com sucesso).

Modelar esses estados como `enum` parece natural. Mas `enum` não carrega dados adicionais por estado. `Cancelada` precisa registrar `canceladaEm: LocalDateTime` — quando o cancelamento ocorreu — para fins de histórico e auditoria. `Concluida` precisa de `concluidaEm`. Um `enum` com um campo compartilhado por todos os estados misturaria dados que só fazem sentido em estados específicos.

`sealed interface` com `record` interno resolve isso: cada estado tem seus próprios dados, e o compilador ainda garante exaustividade nas expressões `switch`.

```java
// Java 21: sealed interface = estados finitos garantidos em compilação
// DDD fit: o compilador exige que todos os estados sejam tratados — sem default esquecido
public sealed interface StatusMatricula
    permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida {

    // Java 21: record interno — cada estado tem seus próprios dados
    record Ativa() implements StatusMatricula {}

    record Cancelada(LocalDateTime canceladaEm) implements StatusMatricula {}

    record Concluida(LocalDateTime concluidaEm) implements StatusMatricula {}
}
```

Com `sealed interface`, o switch de pattern matching é exaustivo sem `default`:

```java
// Java 21: pattern matching para switch — exaustivo, sem default
// DDD fit: se um novo estado for adicionado à sealed interface,
// o compilador avisa em TODOS os pontos do código que fazem switch — sem silent bug
public boolean podeAdicionarDisciplina() {
    return switch (this.status) {
        case StatusMatricula.Ativa a    -> true;
        case StatusMatricula.Cancelada c -> false;  // compilador garante que
        case StatusMatricula.Concluida c -> false;  // todos os casos são cobertos
        // sem default: se novo estado for adicionado, compilador avisa aqui
    };
}
```

> **Por que `sealed interface` e não `enum`?** `enum` não carrega dados adicionais em cada estado. `Cancelada` precisa registrar `canceladaEm: LocalDateTime`. `sealed interface` com `record` interno resolve isso: cada estado pode ter seus próprios dados, e o compilador ainda garante exaustividade. Se amanhã surgir um estado `Trancada(LocalDateTime trancadaEm, String motivo)`, basta adicionar à `sealed interface` — e o compilador aponta todos os switches que precisam tratar o novo caso.

---

## O Aggregate em Ação — adicionarDisciplina()

O método `adicionarDisciplina()` é o coração do Aggregate: aplica as três invariantes em ordem, e só modifica o estado se todas forem satisfeitas. As verificações ocorrem na mesma operação — sem janela de concorrência entre "verificar" e "modificar".

```java
public class Matricula {

    // valor configurável — definido pela instituição
    private static final int LIMITE_DISCIPLINAS = 6;

    private final UUID id;
    private final UUID alunoId;          // referência por ID — ver ADR-003
    private final UUID turmaId;          // referência por ID — ver ADR-003
    private final PeriodoLetivo periodoLetivo;
    private StatusMatricula status;
    private final List<ItemMatricula> disciplinas;
    private final List<Object> eventos;  // eventos coletados — sem dependência de Spring

    public void adicionarDisciplina(NomeDisciplina disciplina) {
        // Invariante 1: estado — matrícula cancelada não aceita novas disciplinas
        if (!(this.status instanceof StatusMatricula.Ativa)) {
            throw new MatriculaCanceladaException(this.id);
        }

        // Invariante 2: duplicidade — a mesma disciplina não pode aparecer duas vezes
        boolean jaMatriculada = this.disciplinas.stream()
            .anyMatch(item -> item.disciplina().equals(disciplina));
        if (jaMatriculada) {
            throw new DisciplinaJaMatriculadaException(disciplina, this.id);
        }

        // Invariante 3: limite — máximo de LIMITE_DISCIPLINAS disciplinas por matrícula
        if (this.disciplinas.size() >= LIMITE_DISCIPLINAS) {
            throw new LimiteDisciplinasExcedidoException(LIMITE_DISCIPLINAS, this.id);
        }

        this.disciplinas.add(new ItemMatricula(disciplina));
        this.eventos.add(new DisciplinaAdicionada(this.id, disciplina, LocalDateTime.now()));
        // O Aggregate coleta o evento internamente — sem chamar nenhum publisher do Spring.
        // O UseCase publica os eventos depois de salvar. Ver domain-events.md.
    }

    // Chamado pelo UseCase após salvar — retorna cópia e limpa a lista interna
    public List<Object> coletarEventos() {
        List<Object> copia = List.copyOf(this.eventos);
        this.eventos.clear();
        return copia;
    }
}
```

A coleta de eventos internamente (sem dependência de Spring) é documentada em [domain-events.md](./domain-events.md).

---

## Erros Comuns

### Erro 1: Invariante no Service — vulnerável a concorrência e duplicação

O desenvolvedor acostumado com arquitetura em camadas coloca a verificação no `@Service` — parece o lugar natural para a lógica de negócio. O problema é duplo: a invariante fica fora do Aggregate (pode ser esquecida em outro Service que faça operações similares) e fica vulnerável a condições de corrida.

```java
// ERRADO — invariante no Service: vulnerável a concorrência e espalhada pelo sistema
@Service
public class MatriculaService {

    @Autowired
    private MatriculaRepository matriculaRepository;

    public void adicionarDisciplina(UUID matriculaId, String disciplina) {
        Matricula matricula = matriculaRepository.findById(matriculaId).orElseThrow();

        // Lógica de negócio fora do Aggregate — pode ser duplicada em outros Services
        if (matricula.getDisciplinas().size() >= 6) {
            throw new RuntimeException("Limite de disciplinas atingido");
        }

        // Acesso direto à lista — burla qualquer encapsulamento do Aggregate
        matricula.getDisciplinas().add(new ItemMatricula(disciplina));
        matriculaRepository.save(matricula);
    }
}
```

```java
// CERTO — invariante encapsulada no Aggregate Root
// O UseCase orquestra (busca, chama, salva, publica); o Aggregate decide (verifica, modifica)
public class AdicionarDisciplinaUseCase {

    private final MatriculaRepositorio repositorio;
    private final EventPublisher publicador;

    public void executar(UUID id, NomeDisciplina disciplina) {
        Matricula matricula = repositorio.buscarPorId(id)
            .orElseThrow(() -> new MatriculaNaoEncontradaException(id));

        // Aggregate decide — lança exceção se alguma invariante for violada
        matricula.adicionarDisciplina(disciplina);

        repositorio.salvar(matricula);
        publicador.publicar(matricula.coletarEventos());
    }
}
```

No CERTO, o UseCase não sabe como verificar o limite, nem sabe o que é duplicidade. Isso é responsabilidade do Aggregate. O UseCase apenas orquestra: busca, delega a decisão, salva o resultado e publica os eventos.

### Erro 2: `enum` simples para status — sem dados por estado

Com `enum`, todos os estados compartilham os mesmos campos — ou nenhum. Para registrar `canceladaEm`, seria necessário um campo na entidade que só faz sentido quando o status é `CANCELADA`. Com `sealed interface`, cada estado carrega exatamente seus próprios dados.

```java
// ERRADO — enum sem dados por estado: canceladaEm fica solto na classe Matricula
public enum Status {
    ATIVA,
    CANCELADA,
    CONCLUIDA
    // Nenhum estado carrega dados próprios — o campo canceladaEm fica na classe:
}

public class Matricula {
    private Status status;
    private LocalDateTime canceladaEm;   // só faz sentido quando status == CANCELADA
    private LocalDateTime concluidaEm;   // só faz sentido quando status == CONCLUIDA
    // Esses campos existem para todos os estados — mas são null na maioria das vezes.
    // O compilador não sabe que canceladaEm não deveria existir quando status == ATIVA.
}
```

```java
// CERTO — sealed interface com record: cada estado tem seus próprios dados
// Java 21: sealed interface garante exaustividade; record garante imutabilidade dos dados do estado
public sealed interface StatusMatricula
    permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida {

    record Ativa() implements StatusMatricula {}

    // canceladaEm existe APENAS quando o status é Cancelada — sem campo opcional na entidade
    record Cancelada(LocalDateTime canceladaEm) implements StatusMatricula {}

    record Concluida(LocalDateTime concluidaEm) implements StatusMatricula {}
}

// O switch é exaustivo — sem default, o compilador garante que todos os estados são tratados
String descricao = switch (matricula.getStatus()) {
    case StatusMatricula.Ativa a      -> "Em andamento";
    case StatusMatricula.Cancelada c  -> "Cancelada em " + c.canceladaEm();
    case StatusMatricula.Concluida c  -> "Concluída em " + c.concluidaEm();
};
```
