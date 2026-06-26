# Domain Events — Matrícula Escolar

Quando uma matrícula é realizada, o que mais acontece no sistema?

O BC Financeiro precisa criar o contrato de cobrança para o período — sem a matrícula confirmada, o aluno não tem vínculo financeiro com a instituição. O BC Acadêmico precisa registrar o vínculo do aluno com a turma — é o evento de matrícula que aciona o processo de alocação em sala. Mas o BC Matrícula não pode chamar diretamente o serviço financeiro nem o acadêmico. Essa chamada direta criaria acoplamento: Matrícula precisaria conhecer as APIs internas do Financeiro e do Acadêmico, e qualquer mudança em um deles poderia quebrar o código de Matrícula.

A solução DDD para comunicação entre Bounded Contexts sem acoplamento direto é o **Domain Event** (Evento de Domínio): um fato histórico que aconteceu no domínio, publicado como dado imutável que outros contextos podem consumir. O evento existe independentemente de quem está ouvindo — o BC Matrícula publica o fato "matrícula realizada" e não precisa saber que Financeiro e Acadêmico existem.

> O [Context Map](../01-design-estrategico/context-map.md) da Fase 1 documenta que o BC Matrícula é Upstream (Supplier) — ele publica os eventos. BC Financeiro e BC Acadêmico são Downstream (Customers) — eles consomem. `domain-events.md` detalha o contrato interno de cada evento publicado pelo BC Matrícula.

---

## Catálogo de Eventos

Os três eventos cobrem o ciclo de vida completo da matrícula do ponto de vista dos contextos consumidores: criação, evolução e encerramento.

| Evento | Publicado Por | Consumido Por | Gatilho | Campos |
|--------|--------------|---------------|---------|--------|
| `AlunoMatriculado` | BC Matrícula | BC Financeiro, BC Acadêmico | `Matricula.criar()` | `matriculaId`, `alunoId`, `turmaId`, `periodoLetivo`, `ocorridoEm` |
| `DisciplinaAdicionada` | BC Matrícula | BC Acadêmico | `Matricula.adicionarDisciplina()` | `matriculaId`, `alunoId`, `disciplina`, `ocorridoEm` |
| `MatriculaCancelada` | BC Matrícula | BC Financeiro, BC Acadêmico | `Matricula.cancelar()` | `matriculaId`, `alunoId`, `periodoLetivo`, `ocorridoEm` |

---

## AlunoMatriculado

`AlunoMatriculado` é o evento mais importante do BC Matrícula: é o fato que aciona o ciclo financeiro e acadêmico para um aluno em um período.

```java
// Java 21: record = imutabilidade por padrão = ideal para eventos
// DDD fit: evento é um fato histórico — o passado não muda, record garante isso
public record AlunoMatriculado(
    UUID matriculaId,
    UUID alunoId,
    UUID turmaId,
    PeriodoLetivo periodoLetivo,
    LocalDateTime ocorridoEm  // quando aconteceu — não quando foi processado
) {}
```

O `record` Java 21 é o tipo ideal para eventos de domínio: imutável por definição (sem setters possíveis), com `equals`/`hashCode` e `toString` gerados automaticamente, e sem boilerplate. Um evento é um fato histórico — "matrícula realizada no dia X" não muda depois que aconteceu. O `record` expressa essa imutabilidade na linguagem.

Os campos carregam apenas **IDs e Value Objects** — nunca o objeto de domínio inteiro. `AlunoMatriculado` carrega o `UUID` do aluno, não um objeto `Aluno` com todos os seus dados. Isso é deliberado: um evento que carrega um objeto mutável poderia ser modificado depois da publicação, tornando-o um fato histórico alterável — o que é uma contradição. Além disso, serializar o `Aluno` inteiro num evento acoplaria o consumidor ao modelo interno do BC Matrícula.

---

## DisciplinaAdicionada e MatriculaCancelada

Seguem o mesmo padrão de `AlunoMatriculado` — records imutáveis com apenas IDs e VOs:

```java
public record DisciplinaAdicionada(
    UUID matriculaId,
    UUID alunoId,
    NomeDisciplina disciplina,
    LocalDateTime ocorridoEm
) {}

public record MatriculaCancelada(
    UUID matriculaId,
    UUID alunoId,
    PeriodoLetivo periodoLetivo,
    LocalDateTime ocorridoEm
) {}
```

Para os detalhes de cada campo e sua semântica no contexto dos Bounded Contexts consumidores, ver o [Context Map](../01-design-estrategico/context-map.md#eventos-que-cruzam-fronteiras).

---

## Como o Aggregate Publica Eventos

O Aggregate `Matricula` coleta eventos internamente, em uma lista simples — sem dependência do Spring.

```java
// Aggregate coleta eventos internamente — sem dependência do Spring
public class Matricula {

    private final List<Object> eventos = new ArrayList<>();

    public void adicionarDisciplina(NomeDisciplina disciplina) {
        // ... verificação das invariantes ...
        this.disciplinas.add(new ItemMatricula(disciplina));

        // Coleta o evento — sem chamar nenhum publisher do Spring
        this.eventos.add(new DisciplinaAdicionada(this.id, this.alunoId, disciplina, LocalDateTime.now()));
    }

    // Chamado pelo UseCase após salvar — retorna cópia e limpa a lista interna
    public List<Object> coletarEventos() {
        List<Object> copia = List.copyOf(this.eventos);
        this.eventos.clear();
        return copia;
    }
}
```

O método `coletarEventos()` é documentado com detalhe em [agregados.md](./agregados.md#o-aggregate-em-ação--adicionardisciplina).

O ponto-chave: `Matricula` não tem `import org.springframework`. O Aggregate coleta eventos na lista interna; o UseCase publica após salvar:

```java
// No UseCase (aplicacao/) — o Spring existe aqui, não no domínio
public void executar(UUID id, NomeDisciplina disciplina) {
    Matricula matricula = repositorio.buscarPorId(id).orElseThrow();
    matricula.adicionarDisciplina(disciplina);  // Aggregate coleta o evento internamente
    repositorio.salvar(matricula);
    publicador.publicar(matricula.coletarEventos());  // UseCase publica após salvar
}
```

O detalhe `ApplicationEventPublisher` do Spring está na camada de aplicação, não no domínio. O Aggregate não conhece o publicador — ele apenas produz os eventos como dados; quem decide como publicá-los é o UseCase.

---

## Erros Comuns

### Erro 1: Publicar evento dentro do Aggregate com dependência de Spring

O desenvolvedor tenta publicar o evento diretamente no Aggregate, injetando o `ApplicationEventPublisher` do Spring. Isso viola a separação entre domínio e framework: o Aggregate passaria a depender do Spring para existir.

```java
// ERRADO — Aggregate com dependência de Spring para publicar eventos
public class Matricula {

    @Autowired
    private ApplicationEventPublisher pub; // import do Spring dentro do Aggregate = domínio dependendo de framework

    public void adicionarDisciplina(NomeDisciplina disciplina) {
        // ... invariantes ...
        this.disciplinas.add(new ItemMatricula(disciplina));
        pub.publishEvent(new DisciplinaAdicionada(this.id, this.alunoId, disciplina, LocalDateTime.now()));
        // Problema: o Aggregate agora depende do Spring — impossível testar sem contexto Spring
    }
}
```

```java
// CERTO — Aggregate coleta; UseCase publica via Spring
public class Matricula {

    // sem import de Spring — lista simples
    private final List<Object> eventos = new ArrayList<>();

    public void adicionarDisciplina(NomeDisciplina disciplina) {
        // ... invariantes ...
        this.disciplinas.add(new ItemMatricula(disciplina));
        this.eventos.add(new DisciplinaAdicionada(this.id, this.alunoId, disciplina, LocalDateTime.now()));
        // Aggregate não conhece o Spring — apenas coleta o evento como dado
    }

    public List<Object> coletarEventos() {
        List<Object> copia = List.copyOf(this.eventos);
        this.eventos.clear();
        return copia;
    }
}

// No listener downstream (BC Financeiro/Acadêmico) — apenas referência:
// @TransactionalEventListener
// public void aoMatricular(AlunoMatriculado evento) { ... }
// O @TransactionalEventListener garante que o listener só executa após o commit da transação
// que salvou a matrícula — sem risco de processar um evento de uma transação que falhou.
```

### Erro 2: Evento com objeto mutável como campo

Carregar o objeto de domínio inteiro no evento em vez de apenas seu ID — cria acoplamento entre o publicador e o consumidor, e quebra a imutabilidade do evento.

```java
// ERRADO — evento carrega objeto mutável
public class AlunoMatriculado {
    public Aluno aluno;       // objeto mutável — pode ser modificado após a publicação
    public Matricula matricula; // Matricula inteira — consumidor fica acoplado ao modelo interno

    // Se aluno.setStatus(INATIVO) for chamado depois de publicar o evento,
    // o consumidor que ainda mantém a referência verá um estado inconsistente
}
```

```java
// CERTO — evento carrega apenas IDs e VOs imutáveis
// Java 21: record = imutabilidade por padrão = ideal para eventos
// DDD fit: evento é um fato histórico — o passado não muda, record garante isso
public record AlunoMatriculado(
    UUID matriculaId,            // ID — não o objeto Matricula
    UUID alunoId,                // ID — não o objeto Aluno
    UUID turmaId,                // ID — não o objeto Turma
    PeriodoLetivo periodoLetivo, // VO imutável (record)
    LocalDateTime ocorridoEm    // timestamp — quando o fato ocorreu
) {}
// O consumidor usa os IDs para buscar o estado atual no seu próprio BC, se precisar
// O evento registra o fato histórico; o consumidor busca o contexto atual por conta própria
```
