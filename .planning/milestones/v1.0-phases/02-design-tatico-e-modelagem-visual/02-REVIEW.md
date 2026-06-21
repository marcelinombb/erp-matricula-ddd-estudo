---
phase: 02-design-tatico-e-modelagem-visual
reviewed: 2026-06-20T00:00:00Z
depth: standard
files_reviewed: 7
files_reviewed_list:
  - docs/02-design-tatico/value-objects.md
  - docs/02-design-tatico/entidades.md
  - docs/02-design-tatico/agregados.md
  - docs/02-design-tatico/domain-services.md
  - docs/02-design-tatico/domain-events.md
  - docs/02-design-tatico/repositorios.md
  - docs/02-design-tatico/modelagem.md
findings:
  critical: 5
  warning: 6
  info: 3
  total: 14
status: issues_found
---

# Phase 02: Code Review Report

**Reviewed:** 2026-06-20
**Depth:** standard
**Files Reviewed:** 7
**Status:** issues_found

## Summary

Revisão dos sete documentos de design tático da Fase 02. O material tem alta qualidade pedagógica no geral — a estrutura progressiva, os contrastes ERRADO/CERTO e o uso correto de Java 21 (sealed interfaces, records, pattern matching) são pontos fortes. A sintaxe Mermaid está correta (tildes em classDiagram, flowchart/sequenceDiagram bem formados).

Foram encontrados cinco blockers: um construtor incorreto em snippet de código (argumento faltando), a lista `eventos` declarada sem inicialização em um snippet (NullPointerException garantido), a omissão de `AlunoRepositorio` do documento `repositorios.md`, o participante do sequence diagram conflacionando dois repositórios distintos em um único `Repo`, e uma inconsistência de tipo entre o texto da estrutura do Aggregate e o código. Seis warnings adicionais cobrem imprecisões conceituais e lacunas pedagógicas.

---

## Critical Issues

### CR-01: Construtor de `DisciplinaAdicionada` com argumento faltando em `agregados.md`

**File:** `docs/02-design-tatico/agregados.md:152`
**Issue:** O snippet de `adicionarDisciplina()` cria `DisciplinaAdicionada` com três argumentos:
```java
this.eventos.add(new DisciplinaAdicionada(this.id, disciplina, LocalDateTime.now()));
```
Mas o record `DisciplinaAdicionada` (definido em `domain-events.md`) tem quatro campos: `matriculaId`, `alunoId`, `disciplina`, `ocorridoEm`. O campo `alunoId` está ausente. O código não compilaria. Todos os outros snippets que instanciam `DisciplinaAdicionada` (em `domain-events.md` linhas 86, 132, 148) passam `this.alunoId` corretamente.

**Fix:**
```java
this.eventos.add(new DisciplinaAdicionada(this.id, this.alunoId, disciplina, LocalDateTime.now()));
```

---

### CR-02: Campo `eventos` declarado sem inicialização em `agregados.md`

**File:** `docs/02-design-tatico/agregados.md:131`
**Issue:** O trecho do corpo da classe `Matricula` declara:
```java
private final List<Object> eventos;  // eventos coletados — sem dependência de Spring
```
Sem inicialização e sem construtor visível no snippet, qualquer chamada a `this.eventos.add(...)` lançaria `NullPointerException`. O snippet correto em `domain-events.md` (linhas 79 e 143) usa `= new ArrayList<>()` na declaração. A inconsistência ensina o padrão errado ao aluno que copiar o código diretamente do snippet principal do Aggregate.

**Fix:**
```java
private final List<Object> eventos = new ArrayList<>();
```

---

### CR-03: `AlunoRepositorio` usado em `domain-services.md` mas nunca definido em `repositorios.md`

**File:** `docs/02-design-tatico/domain-services.md:85,151`
**Issue:** O `MatricularAlunoUseCase` declara `private final AlunoRepositorio alunoRepositorio` e o chama com `alunoRepositorio.buscarPorId(alunoId)`, mas `repositorios.md` documenta apenas `MatriculaRepositorio`. Não existe definição, interface, nem seção sobre `AlunoRepositorio` em nenhum dos sete documentos. Um leitor que seguir a documentação do zero não saberá qual é o contrato dessa interface, quais métodos ela tem ou onde ela vive.

**Fix:** Adicionar em `repositorios.md` (ou em `entidades.md`) uma seção mostrando:
```java
public interface AlunoRepositorio {
    Optional<Aluno> buscarPorId(AlunoId id);
}
```
com o mesmo raciocínio de interface-no-domínio que `MatriculaRepositorio` já exemplifica.

---

### CR-04: Sequence diagram conflaciona `AlunoRepositorio` e `MatriculaRepositorio` em um único participante `Repo`

**File:** `docs/02-design-tatico/modelagem.md:213,226,248`
**Issue:** O sequence diagram declara um único participante:
```
participant Repo as MatriculaRepositorio
```
Mas na linha 226 faz `UC->>Repo: buscarAluno(alunoId)` — esse método pertence ao `AlunoRepositorio`, não ao `MatriculaRepositorio`. `MatriculaRepositorio.buscarPorAluno()` retorna `List<Matricula>` para um aluno, não o `Aluno` em si. Na linha 248, `UC->>Repo: salvar(matricula)` está correto para `MatriculaRepositorio`. O diagrama ensina que existe um único repositório responsável por buscar aluno E salvar matrícula, o que contradiz diretamente o código em `domain-services.md` que usa dois repositórios distintos.

**Fix:** Adicionar um segundo participante `AlunoRepo as AlunoRepositorio` e separar as chamadas:
```
UC->>AlunoRepo: buscarPorId(alunoId)
AlunoRepo-->>UC: Aluno
...
UC->>Repo: salvar(matricula)
```

---

### CR-05: Estrutura do Aggregate menciona `List<DomainEvent>` mas o código usa `List<Object>`; `DomainEvent` não existe

**File:** `docs/02-design-tatico/agregados.md:23`
**Issue:** A representação em árvore da estrutura do Aggregate mostra:
```
└── List<DomainEvent> (eventos coletados — sem dependência de Spring)
```
Mas em nenhum arquivo dos sete documentos existe a definição ou menção de uma interface/classe `DomainEvent`. O código real usa `List<Object>` (linhas 131, 158, e em `domain-events.md` linhas 79, 143). O leitor que ler a estrutura e depois o código encontrará uma contradição não explicada — `DomainEvent` parece ser um tipo que deveria existir mas não existe.

**Fix:** Alterar a estrutura da árvore para refletir o código real:
```
└── List<Object> eventos (eventos coletados — sem dependência de Spring; ver domain-events.md)
```
Ou, se a intenção futura for criar uma interface `DomainEvent`, adicionar uma nota explícita de que o código usa `Object` como simplificação deliberada e que a interface marcadora é uma evolução planejada.

---

## Warnings

### WR-01: Invariante "Estado terminal" incorretamente restrita a `Cancelada`; `Concluida` é ignorada

**File:** `docs/02-design-tatico/agregados.md:60,66`
**Issue:** O texto da invariante diz "Matrícula **cancelada** não recebe novas disciplinas" e a tabela de invariantes lista a exceção `MatriculaCanceladaException`. Mas o código na linha 135-136 verifica `!(this.status instanceof StatusMatricula.Ativa)` — o que também rejeita o estado `Concluida`. Uma matrícula concluída também não pode receber disciplinas, mas isso não é mencionado. Além disso, `MatriculaCanceladaException` é lançado quando o status é `Concluida`, o que é semanticamente incorreto (o nome da exceção diz "cancelada" mas o status é "concluída").

**Fix:** Corrigir o texto da invariante para:
> "Matrícula não-ativa (cancelada ou concluída) não recebe novas disciplinas"

E considerar renomear a exceção para `MatriculaNaoAtivaException` ou verificar separadamente e lançar exceções específicas por estado. O flowchart MOD-03/Fluxo 2 em `modelagem.md` (linha 168) também deve ser atualizado: `VER_CANCEL{Matrícula cancelada?}` deveria ser `VER_ATIVA{Matrícula está ativa?}`.

---

### WR-02: `LIMITE_DISCIPLINAS` descrito como "configurável" mas é `static final` (compile-time constant)

**File:** `docs/02-design-tatico/agregados.md:56,64,122`
**Issue:** O texto diz "configurável pela instituição" e o comentário no código diz `// valor configurável — definido pela instituição`, mas a declaração é:
```java
private static final int LIMITE_DISCIPLINAS = 6;
```
`static final` é uma constante de compilação em Java — não é configurável em runtime sem recompilar. Isso é contradição entre texto e código, e cria expectativa falsa no aluno de que o sistema suporta configuração por instituição sem mudança de código.

**Fix:** Se a intenção é que seja imutável no v1, remover o adjetivo "configurável" do texto e comentário. Se a intenção for suportar configuração, o campo deve ser de instância e injetado — o que deveria ser documentado.

---

### WR-03: Participante `VerificadorElegibilidade` no sequence diagram não corresponde ao nome da classe

**File:** `docs/02-design-tatico/modelagem.md:214`
**Issue:** O participante é nomeado `VerificadorElegibilidade` no alias do sequence diagram, mas a classe definida em `domain-services.md` chama-se `VerificadorElegibilidadeMatricula`. O texto introdutório do próprio sequence diagram (linha 206) usa o nome correto `VerificadorElegibilidadeMatricula`. Um aluno procurando a classe pelo nome do diagrama não vai encontrá-la imediatamente.

**Fix:**
```
participant Verif as VerificadorElegibilidadeMatricula
```

---

### WR-04: Classe `Aluno` em `entidades.md` não tem campo `cpf` mas `modelagem.md` mostra `+Cpf cpf`

**File:** `docs/02-design-tatico/entidades.md:45-76` / `docs/02-design-tatico/modelagem.md:76`
**Issue:** O código Java de `Aluno` em `entidades.md` tem apenas dois campos:
```java
private final AlunoId id;
private boolean ativo;
```
Mas o diagrama de classes em `modelagem.md` mostra:
```
class Aluno {
    +AlunoId id
    +Cpf cpf          // <-- não existe no código
    +boolean ativo
```
E a relação `Aluno --> Cpf : tem` também aparece no diagrama. A ausência de `cpf` no código de `Aluno` não é explicada nem justificada — é possível que seja omissão pedagógica proposital ("simplificação para o exemplo"), mas não está documentada como tal. Um aluno que tenta reconciliar o diagrama com o código ficará confuso.

**Fix:** Ou adicionar o campo `cpf` à implementação de `Aluno` em `entidades.md`, ou adicionar uma nota no diagrama ou no documento explicando que o campo `cpf` foi omitido do snippet por brevidade e será adicionado na Fase 3.

---

### WR-05: `removerDisciplina()` mencionado na prosa mas ausente do código e do diagrama de classes

**File:** `docs/02-design-tatico/agregados.md:74` / `docs/02-design-tatico/modelagem.md:18-29`
**Issue:** O texto em `agregados.md` linha 74 afirma que no estado `Ativa` a matrícula "pode receber e **remover** disciplinas". O diagrama de classes em `modelagem.md` lista os métodos de `Matricula` como `criar()`, `adicionarDisciplina()`, `cancelar()`, `coletarEventos()` — sem `removerDisciplina()`. Nenhum snippet de código implementa ou esboça `removerDisciplina()`. A operação é prometida pela prosa e não entregue, deixando um gap entre expectativa e realidade.

**Fix:** Ou remover "remover" da descrição do estado Ativa (se v1 não suporta remoção), ou adicionar `removerDisciplina()` ao diagrama de classes e um esboço de código em `agregados.md`.

---

### WR-06: `MatriculaJaCanceladaException` no Fluxo 3 não cobre o estado `Concluida`

**File:** `docs/02-design-tatico/modelagem.md:190-191`
**Issue:** O Fluxo 3 verifica `{Já está cancelada?}` com exceção `MatriculaJaCanceladaException`. Se o cancelamento de uma matrícula `Concluida` fosse tentado, o fluxo não cobre esse caminho de erro. Consistentemente com WR-01, o estado `Concluida` é um estado terminal que também deveria ser protegido no fluxo de cancelamento.

**Fix:** Ajustar o diamante para `{Já está em estado terminal?}` ou adicionar um segundo caminho explícito para `Concluida`, para manter consistência com a implementação esperada de `cancelar()`.

---

## Info

### IN-01: Exemplo de CPF `"123.456.789-09"` é inválido pelo algoritmo módulo 11

**File:** `docs/02-design-tatico/value-objects.md:81-83`
**Issue:** O exemplo usa `"123.456.789-09"` (sequência conhecida inválida pelo algoritmo CPF). Como o `cpfComDigitoVerificadorValido()` tem um placeholder que sempre retorna `true`, o código funciona agora, mas quando a implementação real for adicionada na Fase 3, o exemplo de `equals` no próprio documento passará a falhar em runtime — confundindo o aluno que retornar ao documento após a Fase 3.

**Fix:** Substituir pelos exemplos pelo CPF real e válido `"529.982.247-25"` (amplamente usado em exemplos pedagógicos no Brasil) ou adicionar uma nota explícita de que `"123.456.789-09"` é propositalmente inválido e só funciona porque o validador do dígito é placeholder.

---

### IN-02: `Matricula.criar()` referenciado em dois documentos mas nunca implementado/esboçado

**File:** `docs/02-design-tatico/domain-services.md:97,160` / `docs/02-design-tatico/modelagem.md:242`
**Issue:** `Matricula.criar(alunoId, turmaId, periodo)` é chamado no UseCase (duas vezes em `domain-services.md`) e no sequence diagram, mas nenhum documento mostra a assinatura, a implementação ou sequer um esboço desse método. A classe `Matricula` em `agregados.md` também não tem construtor visível. O aluno não consegue inferir quais invariantes `criar()` verifica nem o que ele retorna além de uma `Matricula`.

**Fix:** Adicionar um esboço de `criar()` em `agregados.md`, mesmo que parcial:
```java
public static Matricula criar(AlunoId alunoId, TurmaId turmaId, PeriodoLetivo periodo) {
    // Fase 3: implementação completa
    // Aqui: inicializar status=Ativa, disciplinas=[], coleta AlunoMatriculado
}
```

---

### IN-03: `STATE.md` referenciado em `entidades.md` mas arquivo não existe no repositório

**File:** `docs/02-design-tatico/entidades.md:125`
**Issue:** O comentário no código `// verificação de vagas disponíveis: v2 — ver STATE.md open decisions` aponta para `STATE.md`, mas esse arquivo não existe no repositório (verificado). O link aponta para o nada.

**Fix:** Remover a referência `STATE.md` do comentário ou substituir por uma nota inline que explique que a verificação de vagas disponíveis é descoped no v1, sem apontar para um arquivo inexistente.

---

_Reviewed: 2026-06-20_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
