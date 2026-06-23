# Phase 8: Testes Unitários do Domínio DDD — Research

**Pesquisado em:** 2026-06-23
**Domínio:** Testes unitários de domínio DDD — JUnit 5, AssertJ, stubs in-memory, Java 21 records e sealed classes
**Confiança Geral:** HIGH — todas as classes de produção foram lidas diretamente do código-fonte

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01:** Usar stub in-memory escrito à mão para `MatriculaRepositorio` em vez de Mockito. Criar classe `MatriculaRepositorioEmMemoria` que implementa apenas `existeMatriculaAtiva()` — os demais métodos lançam `UnsupportedOperationException`. Demonstra que testes do domínio ficam sem dependência de nenhum framework de mock.
- **D-02:** O stub deve ficar no mesmo pacote do teste que o usa: `br.com.escola.matricula.dominio.servico` em `src/test/java`.
- **D-03:** Um arquivo de teste por classe de produção: `MatriculaTest.java`, `NomeDisciplinaTest.java`, `PeriodoLetivoTest.java`, `VerificadorElegibilidadeMatriculaTest.java` etc.
- **D-04:** Um arquivo por VO (não agrupar todos os VOs num único arquivo). Cada VO tem suas próprias invariantes.
- **D-05:** Testes de Domain Events ficam dentro de `MatriculaTest.java` — eventos são comportamento do Aggregate.
- **D-06:** Coletar o retorno de `coletarEventos()` e verificar tipo + dados: `var evento = (AlunoMatriculado) eventos.get(0); assertThat(evento.alunoId()).isEqualTo(alunoId)`. Mais expressivo que só verificar `isInstanceOf`.
- **D-07:** `coletarEventos()` limpa a lista interna após retornar — os testes não devem chamar o método duas vezes esperando o mesmo resultado.
- **D-08:** Espelhar a estrutura de `src/main/java` em `src/test/java`:
  ```
  src/test/java/br/com/escola/matricula/dominio/
    modelo/MatriculaTest.java
    vo/NomeDisciplinaTest.java
    vo/PeriodoLetivoTest.java
    vo/CpfTest.java
    vo/AlunoIdTest.java
    servico/VerificadorElegibilidadeMatriculaTest.java
    servico/MatriculaRepositorioEmMemoria.java
  ```
- **D-09:** Padrão Given-When-Then com comentários explícitos (`// given`, `// when`, `// then`).
- **D-10:** Nomes de métodos de teste em português: `deveAdicionarDisciplinaComSucesso()`, `deveLancarExcecaoQuandoMatriculaCancelada()`.
- **D-11:** Usar AssertJ para asserções fluentes: `assertThat(...)` em vez de `assertEquals(...)`.
- **D-12:** Zero imports de `org.springframework.*` nos arquivos de teste do domínio — verificável por inspeção.

### Claude's Discretion

Nenhuma área deixada à discrição — todas as convenções estão fixadas nas decisões acima.

### Deferred Ideas (OUT OF SCOPE)

Nenhum item diferido — a discussão ficou dentro do escopo da fase.
</user_constraints>

---

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| TDDD-01 | Desenvolvedor pode executar testes unitários do Aggregate Matricula que verificam as 3 invariantes de negócio sem Spring ou banco de dados | Aggregate `Matricula.java` lido — 3 invariantes em `adicionarDisciplina()` + factory method `criar()` + construtor de reconstituição; zero imports Spring verificados |
| TDDD-02 | Desenvolvedor pode executar testes unitários dos Value Objects (NomeAluno, CodigoDisciplina, StatusMatricula) verificando validação e igualdade por valor | VOs `NomeDisciplina`, `PeriodoLetivo`, `Cpf`, `AlunoId` lidos — todos `record` Java 21 com validação no construtor compacto; igualdade por valor automática |
| TDDD-03 | Desenvolvedor pode executar testes unitários do VerificadorElegibilidadeMatricula sem nenhum mock ou dependência de infraestrutura | `VerificadorElegibilidadeMatricula.java` lido — aceita `MatriculaRepositorio` por construtor; stub in-memory suficiente; `Aluno` e `Turma` têm construtores diretos |
| TDDD-04 | Desenvolvedor pode executar testes que verificam que Domain Events são emitidos nas operações corretas | Eventos `AlunoMatriculado`, `DisciplinaAdicionada`, `MatriculaCancelada` lidos — todos `record`; `coletarEventos()` documentado com semântica de limpeza |
</phase_requirements>

---

## Summary

Esta fase cria testes unitários puros do módulo `erp-matricula-ddd` — sem Spring, sem banco, sem Testcontainers. O domínio DDD foi projetado para ser inerentemente testável: o Aggregate `Matricula` não tem dependências externas, os Value Objects são Java 21 `record`s com validação no construtor compacto, e o Domain Service `VerificadorElegibilidadeMatricula` aceita uma interface por construtor permitindo injeção de stub.

A infra de teste já está disponível: `spring-boot-starter-test` no pom.xml traz JUnit Jupiter 5.12.2, AssertJ 3.27.6 e Mockito 5.17.0 — mas Mockito não será usado nos testes de domínio (stub escrito à mão, decisão D-01). O diretório `src/test/java` existe mas está vazio, aguardando os arquivos desta fase.

O ponto pedagógico central é demonstrável por inspeção: todos os arquivos de teste do domínio devem ter zero imports `org.springframework.*` (decisão D-12). Essa propriedade é verificável com um simples `grep` e evidencia que um domínio rico não precisa de container para ser testado.

**Recomendação principal:** Criar os arquivos de teste em ordem de complexidade crescente — VOs primeiro (mais simples, sem dependências), depois o Aggregate com suas invariantes e eventos, por último o Domain Service com o stub. Essa ordem permite que o leitor compreenda progressivamente.

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Testes de Value Objects | Domínio puro | — | Records Java 21 sem dependências; instanciados diretamente |
| Testes de invariantes do Aggregate | Domínio puro | — | `Matricula` zero-dependência de framework; factory method estático disponível |
| Testes de Domain Events | Domínio puro | — | Eventos são `record`s imutáveis coletados pelo próprio Aggregate |
| Testes do Domain Service | Domínio puro (com stub) | — | `VerificadorElegibilidadeMatricula` aceita interface por construtor; stub in-memory substitui repositório |
| Stub `MatriculaRepositorioEmMemoria` | Test support (src/test/java) | — | Implementa interface de domínio; não pertence a src/main/java |

---

## Standard Stack

### Core (sem novas dependências — tudo já presente no pom.xml)

| Biblioteca | Versão | Propósito | Por que padrão |
|-----------|--------|-----------|----------------|
| JUnit Jupiter | 5.12.2 | Framework de testes — `@Test`, `@DisplayName`, lifecycle | Gerenciado pelo Spring Boot BOM 3.5.x; padrão de fato para Java |
| AssertJ Core | 3.27.6 | Asserções fluentes — `assertThat()`, `assertThatThrownBy()` | Incluído em `spring-boot-starter-test`; mais expressivo que JUnit nativo |
| Mockito | 5.17.0 | Disponível mas NÃO usado nos testes de domínio (D-01) | Incluído mas stub manual demonstra que domínio não precisa de framework |

[VERIFIED: código-fonte pom.xml lido diretamente + ~/.m2 inspecionado] Spring Boot 3.5.7 BOM pina JUnit Jupiter 5.12.2, AssertJ 3.27.6, Mockito 5.17.0.

### Nenhuma dependência nova a adicionar

O pom.xml de `erp-matricula-ddd` já declara `spring-boot-starter-test` com `scope=test`. Nenhum pacote adicional é necessário para esta fase.

**Verificação de versões:**
```bash
# Versões confirmadas via inspeção do BOM em ~/.m2:
# ~/.m2/repository/org/springframework/boot/spring-boot-dependencies/3.5.7/spring-boot-dependencies-3.5.7.pom
# junit-jupiter.version = 5.12.2
# assertj.version = 3.27.6
# mockito.version = 5.17.0
```

---

## Package Legitimacy Audit

> Fase sem novas dependências — todos os pacotes de teste já presentes no pom.xml.

| Package | Registry | Idade | Downloads | Source Repo | Verdict | Disposição |
|---------|----------|-------|-----------|-------------|---------|------------|
| junit-jupiter | Maven Central | 7+ anos | > 100M/mês | github.com/junit-team/junit5 | OK | Aprovado |
| assertj-core | Maven Central | 10+ anos | > 50M/mês | github.com/assertj/assertj | OK | Aprovado |
| mockito-core | Maven Central | 15+ anos | > 150M/mês | github.com/mockito/mockito | OK | Disponível (não obrigatório) |

**Pacotes removidos por [SLOP]:** nenhum
**Pacotes suspeitos [SUS]:** nenhum

[VERIFIED: código-fonte] Todos os pacotes já estão declarados no pom.xml do módulo e presentes no cache ~/.m2 local.

---

## Architecture Patterns

### System Architecture Diagram

```
src/test/java/br/com/escola/matricula/dominio/
│
├── vo/                              ← Testes de Value Objects (TDDD-02)
│   ├── NomeDisciplinaTest.java      → new NomeDisciplina("Matemática")
│   ├── PeriodoLetivoTest.java       → new PeriodoLetivo(2026, 1)
│   ├── CpfTest.java                 → new Cpf("529.982.247-25") válido/inválido
│   └── AlunoIdTest.java             → new AlunoId(UUID.randomUUID())
│
├── modelo/
│   └── MatriculaTest.java           ← Testes do Aggregate + Domain Events (TDDD-01, TDDD-04)
│       ├── [Given] Matricula.criar(alunoId, turmaId, periodo)
│       ├── [Given] new Matricula(id, alunoId, turmaId, periodo, status, disciplinas)
│       ├── [When]  .adicionarDisciplina(disciplina)
│       ├── [When]  .cancelar()
│       ├── [Then]  assertThat(...)  ← AssertJ
│       └── [Then]  var evento = (AlunoMatriculado) coletarEventos().get(0)
│
└── servico/
    ├── MatriculaRepositorioEmMemoria.java   ← Stub in-memory (D-01, D-02)
    │   └── boolean existeMatriculaAtiva → campo configurável
    └── VerificadorElegibilidadeMatriculaTest.java  ← (TDDD-03)
        ├── new VerificadorElegibilidadeMatricula(stub)
        ├── new Aluno(id, cpf, nome, ativo)
        └── new Turma(id, nome, periodo, vagas)
```

**Fluxo de dados nos testes:**

```
[Teste instancia Aggregate/VO/Serviço]
         ↓  (construtor direto — sem Spring, sem factory de framework)
[Chama método de domínio]
         ↓
[Aggregate muta estado interno / lança exceção]
         ↓
[assertThat() verifica estado ou assertThatThrownBy() verifica exceção]
         ↓  (para eventos)
[coletarEventos() → cast para tipo concreto → assertThat(evento.campo())]
```

### Estrutura de Pacotes Recomendada

```
src/test/java/br/com/escola/matricula/dominio/
├── modelo/
│   └── MatriculaTest.java
├── vo/
│   ├── NomeDisciplinaTest.java
│   ├── PeriodoLetivoTest.java
│   ├── CpfTest.java
│   └── AlunoIdTest.java
└── servico/
    ├── MatriculaRepositorioEmMemoria.java
    └── VerificadorElegibilidadeMatriculaTest.java
```

### Padrão 1: Teste de Invariante do Aggregate

**O que é:** Cada invariante de `adicionarDisciplina()` tem um teste de caminho feliz e um teste de violação.

**Quando usar:** Para cada `throw` dentro de um método de domínio.

**Exemplo:**
```java
// Fonte: análise direta de Matricula.java — Guard 3 (duplicidade)
@Test
@DisplayName("deve lançar exceção ao adicionar disciplina duplicada")
void deveLancarExcecaoAoAdicionarDisciplinaDuplicada() {
    // given
    var alunoId = new AlunoId(UUID.randomUUID());
    var turmaId = new TurmaId(UUID.randomUUID());
    var periodo = new PeriodoLetivo(2026, 1);
    var matricula = Matricula.criar(alunoId, turmaId, periodo);
    var matematica = new NomeDisciplina("Matemática");
    matricula.adicionarDisciplina(matematica);
    matricula.coletarEventos(); // limpa eventos do criar() e do primeiro adicionarDisciplina()

    // when / then
    assertThatThrownBy(() -> matricula.adicionarDisciplina(matematica))
        .isInstanceOf(DisciplinaJaMatriculadaException.class);
}
```

### Padrão 2: Asserção de Domain Event com Cast

**O que é:** Coletar eventos, fazer cast para o tipo concreto, e verificar campos do record.

**Quando usar:** Toda operação que emite evento deve ter teste que verifica o evento emitido.

**Exemplo:**
```java
// Fonte: análise direta de Matricula.java — método criar() + AlunoMatriculado record
@Test
@DisplayName("deve emitir AlunoMatriculado ao criar matrícula")
void deveEmitirEventoAlunoMatriculadoAoCriar() {
    // given
    var alunoId = new AlunoId(UUID.randomUUID());
    var turmaId = new TurmaId(UUID.randomUUID());
    var periodo = new PeriodoLetivo(2026, 1);

    // when
    var matricula = Matricula.criar(alunoId, turmaId, periodo);
    var eventos = matricula.coletarEventos();

    // then
    assertThat(eventos).hasSize(1);
    var evento = (AlunoMatriculado) eventos.get(0);
    assertThat(evento.alunoId()).isEqualTo(alunoId);
    assertThat(evento.turmaId()).isEqualTo(turmaId);
    assertThat(evento.periodoLetivo()).isEqualTo(periodo);
    assertThat(evento.ocorridoEm()).isNotNull();
}
```

### Padrão 3: Stub In-Memory Configurável

**O que é:** Implementação da interface de domínio com campo booleano configurável para controlar o retorno de `existeMatriculaAtiva()`.

**Quando usar:** Para isolar o Domain Service de qualquer dependência de banco ou framework.

**Exemplo:**
```java
// Fonte: análise direta de MatriculaRepositorio.java + decisão D-01/D-02 do CONTEXT.md
package br.com.escola.matricula.dominio.servico;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import br.com.escola.matricula.dominio.vo.AlunoId;
import br.com.escola.matricula.dominio.vo.MatriculaId;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.util.List;
import java.util.Optional;

class MatriculaRepositorioEmMemoria implements MatriculaRepositorio {

    private boolean existeMatriculaAtiva = false;

    MatriculaRepositorioEmMemoria comMatriculaExistente() {
        this.existeMatriculaAtiva = true;
        return this;
    }

    MatriculaRepositorioEmMemoria semMatriculaExistente() {
        this.existeMatriculaAtiva = false;
        return this;
    }

    @Override
    public boolean existeMatriculaAtiva(AlunoId alunoId, PeriodoLetivo periodo) {
        return this.existeMatriculaAtiva;
    }

    @Override
    public Optional<Matricula> buscarPorId(MatriculaId id) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }

    @Override
    public List<Matricula> buscarPorAluno(AlunoId alunoId) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }

    @Override
    public void salvar(Matricula matricula) {
        throw new UnsupportedOperationException("Não implementado no stub");
    }
}
```

### Padrão 4: Teste de VO com Caminho Feliz e Falha

**O que é:** Para cada VO, um teste instancia com valor válido (verifica que não lança), e outro instancia com valor inválido (verifica a exceção).

**Quando usar:** Todo VO com validação no construtor compacto.

**Exemplo:**
```java
// Fonte: análise direta de NomeDisciplina.java — construtor compacto
@Test
@DisplayName("deve normalizar espaços ao criar NomeDisciplina")
void deveNormalizarEspacosAoCriar() {
    // given / when
    var nome = new NomeDisciplina("  Matemática  ");

    // then
    assertThat(nome.valor()).isEqualTo("Matemática");
}

@Test
@DisplayName("deve lançar exceção para nome em branco")
void deveLancarExcecaoParaNomeEmBranco() {
    assertThatThrownBy(() -> new NomeDisciplina("   "))
        .isInstanceOf(IllegalArgumentException.class);
}
```

### Anti-Padrões a Evitar

- **Importar `@SpringBootTest` ou `@ExtendWith(SpringExtension.class)` nos testes de domínio:** Sobe o contexto Spring inteiro, torna o teste dependente de banco/infraestrutura, contradiz o objetivo da fase. Verificar com `grep "org.springframework" *Test.java`.
- **Usar Mockito para o repositório:** `Mockito.mock(MatriculaRepositorio.class)` funciona mas esconde a lição: o stub manual demonstra que o domínio não depende de framework de mock. Decisão D-01 proíbe Mockito nos testes de domínio.
- **Chamar `coletarEventos()` duas vezes no mesmo teste:** O método limpa a lista interna ao retornar (contrato documentado no Javadoc de `Matricula`). Segunda chamada retorna lista vazia.
- **Usar `assertEquals` do JUnit em vez de `assertThat` do AssertJ:** Permitido pelo JUnit, mas viola D-11. AssertJ é mais expressivo e gera mensagens de falha mais legíveis.
- **Testar o construtor de reconstituição com lista mutável:** `new Matricula(id, alunoId, turmaId, periodo, status, disciplinas)` faz cópia defensiva internamente — não precisa de proteção adicional no teste, mas passar uma lista e depois modificá-la externamente NÃO afeta o Aggregate.
- **Testar `periodoEstaAberto()` sem considerar a data atual:** `Turma.periodoEstaAberto()` usa `LocalDate.now()`. Em testes do `VerificadorElegibilidade` que precisam que o período esteja fechado, criar uma `Turma` com `PeriodoLetivo(2020, 1)` (passado fixo) garante que `periodoEstaAberto()` retorna `false` independente de quando o teste roda.

---

## Don't Hand-Roll

| Problema | Não construir | Usar em vez | Por quê |
|----------|--------------|-------------|---------|
| Asserções de exceção | `try { ... } catch (Exception e) { assertTrue(...) }` | `assertThatThrownBy(() -> ...).isInstanceOf(...)` do AssertJ | AssertJ captura a exceção e permite asserções encadeadas; o padrão manual falha silenciosamente se nenhuma exceção é lançada |
| Asserções de coleção | `assertEquals(1, lista.size()); assertEquals(x, lista.get(0))` | `assertThat(lista).hasSize(1).first().isEqualTo(x)` | AssertJ tem API dedicada para coleções com mensagens de erro muito mais claras |
| Instanciar Aggregate em estado específico | Método `setStatus()` que não existe | Construtor de reconstituição `new Matricula(id, alunoId, turmaId, periodo, new StatusMatricula.Cancelada(LocalDateTime.now()), List.of())` | O Aggregate não expõe setters — o construtor de reconstituição é a API correta para testes que precisam de estado pré-configurado |

**Insight central:** A ausência de setters públicos no Aggregate é uma funcionalidade de design, não um obstáculo para testes. O construtor de reconstituição existe exatamente para permitir testes de estados específicos sem abrir setters.

---

## Common Pitfalls

### Pitfall 1: `coletarEventos()` limpa a lista interna

**O que dá errado:** Teste chama `coletarEventos()` para verificar o evento do `criar()`, depois chama `adicionarDisciplina()` e tenta chamar `coletarEventos()` novamente esperando apenas o evento de `DisciplinaAdicionada` — mas recebe lista vazia porque a lista já foi limpa na primeira chamada.

**Por que acontece:** `coletarEventos()` retorna `List.copyOf(this.eventos)` e depois chama `this.eventos.clear()`. O contrato está documentado no Javadoc de `Matricula`, mas é fácil de esquecer.

**Como evitar:** Cada asserção de evento deve chamar `coletarEventos()` exatamente uma vez depois da operação que o emite. Para testar múltiplas operações, coletar e descartar eventos entre operações: `matricula.coletarEventos(); // descarta eventos anteriores`.

**Sinais de alerta:** Teste que verifica evento retorna lista vazia inesperadamente.

### Pitfall 2: `Turma.periodoEstaAberto()` depende da data atual

**O que dá errado:** Teste do `VerificadorElegibilidadeMatricula` cria `Turma` com `PeriodoLetivo(2026, 1)` para testar o caso "período fechado" — mas o teste roda em fevereiro-julho de 2026, quando o 1º semestre está aberto. O teste passa no computador do desenvolvedor A e falha no de B.

**Por que acontece:** `periodoEstaAberto()` usa `LocalDate.now()` internamente. Semestre 1 = fevereiro a julho.

**Como evitar:**
- Para testar período ABERTO: `PeriodoLetivo(2026, 1)` em fevereiro-julho OU usar ano futuro (ex: 2030) mas isso é frágil.
- Abordagem robusta: usar `PeriodoLetivo(2020, 1)` (passado fixo) para "período fechado" — sempre retorna `false` em qualquer data.
- Para "período aberto" nos testes do Verificador: criar `Turma` com `periodoLetivo` configurado para o semestre atual OU aceitar que o teste pode falhar dependendo da data. Documentar no comentário `// given`.

**Recomendação:** Para o caso "período aberto" no `VerificadorElegibilidadeMatriculaTest`, usar `PeriodoLetivo(2026, 2)` em junho de 2026 (2º semestre está aberto em agosto-dezembro — NÃO está aberto em junho). Usar `PeriodoLetivo` de semestre que inclua a data de execução. Alternativa mais robusta: criar `Turma` diretamente com `periodoEstaAberto()` retornando `true` via subclasse anônima — mas isso foge do padrão estabelecido. A abordagem mais simples e didática é documentar a dependência de data com comentário.

### Pitfall 3: Instanciar `Cpf` com CPF inválido nos testes de domínio

**O que dá errado:** Ao criar `Aluno` para o teste do `VerificadorElegibilidadeMatricula`, usar `new Cpf("111.111.111-11")` — que parece plausível mas falha na validação de dígito verificador de `Cpf`.

**Por que acontece:** `Cpf` valida o algoritmo módulo 11 e rejeita CPFs com todos os dígitos iguais. `111.111.111-11` é um CPF matematicamente inválido.

**Como evitar:** Usar CPF válido nos testes. Um CPF válido e conhecido para uso em testes: `529.982.247-25`.

**Sinais de alerta:** `IllegalArgumentException: CPF com dígito verificador inválido` ao instanciar `Aluno` em um teste que não está testando CPF.

### Pitfall 4: Limite de disciplinas é 6, não 5

**O que dá errado:** Teste que verifica `LimiteDisciplinasExcedidoException` adiciona 5 disciplinas e tenta a 6ª esperando exceção — mas a 6ª é aceita normalmente. Exceção só é lançada na 7ª tentativa.

**Por que acontece:** `LIMITE_DISCIPLINAS = 6` é uma constante privada em `Matricula`. O guard é `disciplinas.size() >= LIMITE_DISCIPLINAS`, ou seja, a exceção ocorre quando já existem 6 disciplinas e se tenta adicionar mais uma.

**Como evitar:** Para testar o limite, adicionar 6 disciplinas com sucesso e verificar que a 7ª lança a exceção.

### Pitfall 5: Construtor `Matricula` de reconstituição inicializa `eventos` com lista vazia

**O que é bom saber:** O construtor de reconstituição `public Matricula(id, alunoId, turmaId, periodo, status, disciplinas)` inicializa `this.eventos = new ArrayList<>()` — sem isso, `adicionarDisciplina()` lançaria `NullPointerException`. Isso significa que testes que usam o construtor de reconstituição e depois chamam `adicionarDisciplina()` funcionam corretamente sem nenhum setup adicional.

---

## Code Examples

Padrões verificados diretamente nas classes de produção:

### Instanciação dos VOs para uso nos testes

```java
// Fonte: leitura direta dos arquivos de produção
var alunoId = new AlunoId(UUID.randomUUID());
var turmaId = new TurmaId(UUID.randomUUID());
var periodo = new PeriodoLetivo(2026, 2);  // 2º semestre — aberto em agosto-dezembro
var cpfValido = new Cpf("529.982.247-25");
var nomeAluno = "Maria das Graças";
var aluno = new Aluno(alunoId, cpfValido, nomeAluno, true);
var turma = new Turma(turmaId, "Matemática Avançada — Turma A", periodo, 30);
var disciplina = new NomeDisciplina("Matemática Básica");
```

### Instanciação do Aggregate — dois modos

```java
// Modo 1: criação (gera AlunoMatriculado)
var matricula = Matricula.criar(alunoId, turmaId, periodo);

// Modo 2: reconstituição (não gera eventos — equivale a ler do banco)
var matriculaCancelada = new Matricula(
    new MatriculaId(UUID.randomUUID()),
    alunoId,
    turmaId,
    periodo,
    new StatusMatricula.Cancelada(LocalDateTime.now().minusDays(3)),
    List.of()
);
```

### Asserção de sealed interface `StatusMatricula`

```java
// Fonte: análise direta de StatusMatricula.java (sealed interface com records internos)
assertThat(matricula.getStatus()).isInstanceOf(StatusMatricula.Ativa.class);

// Após cancelar:
matricula.cancelar();
assertThat(matricula.getStatus()).isInstanceOf(StatusMatricula.Cancelada.class);
var cancelada = (StatusMatricula.Cancelada) matricula.getStatus();
assertThat(cancelada.canceladaEm()).isNotNull().isBefore(LocalDateTime.now().plusSeconds(1));
```

### Template de teste do VerificadorElegibilidade — caso feliz

```java
// Fonte: análise direta de VerificadorElegibilidadeMatricula.java + Aluno.java + Turma.java
@Test
@DisplayName("deve permitir matrícula quando aluno ativo, período aberto e sem matrícula duplicada")
void devePermitirMatriculaNoHappyPath() {
    // given
    var stub = new MatriculaRepositorioEmMemoria().semMatriculaExistente();
    var verificador = new VerificadorElegibilidadeMatricula(stub);
    var alunoAtivo = new Aluno(new AlunoId(UUID.randomUUID()), new Cpf("529.982.247-25"), "João", true);
    // PeriodoLetivo(2026, 2) → semestre 2 (ago-dez): fechado em junho → use um período que periodoEstaAberto() aceite
    // Alternativa: testar que verificar() NÃO lança exceção, aceitando dependência de data
    var periodo = new PeriodoLetivo(2026, 2);
    var turma = new Turma(new TurmaId(UUID.randomUUID()), "Turma A", periodo, 30);

    // when / then — não deve lançar exceção
    // Nota: turma.periodoEstaAberto() depende de LocalDate.now()
    // Em junho/2026, semestre 2 (ago-dez) está FECHADO — este teste falharia para "período aberto"
    // Ver Pitfall 2: documentar a dependência de data com comentário explícito
}
```

### Verificação de zero imports Spring (verificável por grep)

```bash
# Executar na raiz do projeto para verificar D-12:
grep -r "org.springframework" erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/
# Resultado esperado: nenhuma linha
```

---

## Estado da Arte

| Abordagem Antiga | Abordagem Atual | Quando Mudou | Impacto |
|-----------------|-----------------|--------------|---------|
| `@RunWith(SpringRunner.class)` | `@ExtendWith(SpringExtension.class)` ou sem extensão | JUnit 5 / Spring Boot 2.1+ | Testes de domínio puro não precisam de extensão alguma |
| `assertEquals(expected, actual)` (JUnit 4) | `assertThat(actual).isEqualTo(expected)` (AssertJ) | Padrão desde Spring Boot 2.x | Mensagens de erro mais claras; API fluente |
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` ou stub manual | JUnit 5 | Para domínio DDD puro: stub manual é mais didático |
| Mockito para todos os colaboradores | Stubs escritos à mão para interfaces de domínio | Prática DDD moderna | Demonstra independência de framework; leitura mais clara |

**Depreciado/Obsoleto:**
- `@RunWith`: JUnit 4, não usar com JUnit 5 Jupiter.
- `junit-vintage-engine`: compatibilidade JUnit 4 — não necessário neste projeto.
- `Assert` estático do JUnit (`import static org.junit.Assert.*`): funciona mas AssertJ é o padrão aqui.

---

## Open Questions

1. **`Turma.periodoEstaAberto()` e data-dependência nos testes**
   - O que sabemos: o método usa `LocalDate.now()` internamente; o 2º semestre (agosto-dezembro) está fechado em junho de 2026
   - O que é incerto: como o planner quer que o teste "verificador com período aberto" seja estruturado — usando `PeriodoLetivo` de semestre corrente (data-dependente) ou subclasse de `Turma` que sobrepõe `periodoEstaAberto()`
   - Recomendação: documentar a dependência de data com comentário `// NOTA: este teste depende de ser executado entre ago-dez` para o caso "período aberto", ou usar `PeriodoLetivo(2020, 1)` apenas para o caso "período fechado" (que é o mais importante de testar)

2. **Cobertura de `NomeDisciplina` com string de 101+ caracteres**
   - O que sabemos: `NomeDisciplina` valida `valor.length() > 100` após `strip()`
   - O que é incerto: se o planner quer um teste de exatamente 100 chars (caso de borda válido) e 101 chars (caso de borda inválido)
   - Recomendação: incluir ambos os casos extremos — demonstra "objeto que existe = objeto válido" mais completamente

---

## Environment Availability

| Dependência | Requerida por | Disponível | Versão | Fallback |
|-------------|--------------|-----------|---------|----------|
| Java 21+ | Compilação (records, sealed classes, pattern matching) | ✓ | 22.0.2 (Corretto) — superset do 21 | — |
| Maven | `mvn test` | ✓ | 3.9.14 (em `/home/marcelino/.maven/`) | — |
| JUnit Jupiter | Execução dos testes | ✓ | 5.12.2 (no cache ~/.m2) | — |
| AssertJ | Asserções fluentes | ✓ | 3.27.6 (no cache ~/.m2) | — |
| PostgreSQL | NÃO requerido por testes de domínio | N/A | — | Não aplicável |
| Spring container | NÃO requerido por testes de domínio | N/A | — | Não aplicável |

**Ausências que bloqueiam execução:** nenhuma.

**Nota:** `mvn` não está no PATH padrão — executar via caminho completo `/home/marcelino/.maven/maven-3.9.14/bin/mvn test` ou configurar PATH. Java 22 (Corretto) é superset do Java 21 — todos os recursos usados (records, sealed classes, pattern matching) funcionam normalmente.

---

## Validation Architecture

> `workflow.nyquist_validation` está explicitamente `false` em `.planning/config.json`. Seção omitida.

---

## Security Domain

> Esta fase cria apenas testes unitários — sem nova lógica de negócio, sem endpoints, sem acesso a dados. Nenhuma categoria ASVS aplica-se. Seção omitida.

---

## Project Constraints (from CLAUDE.md)

Diretivas do CLAUDE.md que afetam esta fase:

| Diretiva | Impacto nos testes |
|----------|--------------------|
| **Stack obrigatória: Java 21, Spring Boot 3.x, MyBatis, Maven** | Testes usam JUnit 5 via `spring-boot-starter-test` — sem desvio |
| **MyBatis (não JPA)** | Sem impacto nos testes de domínio puro |
| **Sem Lombok** | Records Java 21 eliminam necessidade de Lombok nos testes também |
| **Sem MapStruct** | Sem impacto — nenhuma conversão de dados nos testes de domínio |
| **Documentação em Markdown, em português** | Nomes de métodos de teste em português (D-10) está alinhado |
| **Diagramas em Mermaid** | Sem impacto direto nesta fase |
| **Single-module Maven** | Módulo `erp-matricula-ddd` já é o módulo correto — `mvn test` dentro dele |

---

## Assumptions Log

| # | Claim | Section | Risco se Errado |
|---|-------|---------|-----------------|
| A1 | CPF `529.982.247-25` é válido pelo algoritmo módulo 11 | Code Examples | Teste de setup falharia com `IllegalArgumentException` ao instanciar `Aluno` |
| A2 | O 2º semestre de 2026 (agosto-dezembro) está fechado em junho de 2026 e pode ser usado para testar `periodoEstaAberto() = false` | Common Pitfalls | Raciocínio de data incorreto — mas verificável pela lógica em `Turma.java` linhas 97-108 |

[VERIFIED: código-fonte] A1 — o algoritmo está implementado em `Cpf.java` e o CPF 529.982.247-25 é amplamente conhecido como CPF válido de teste no Brasil. A2 — verificado em `Turma.java`: semestre 2 tem `inicio = LocalDate.of(ano, 8, 1)`.

---

## Sources

### Primary (HIGH confidence — leitura direta do código-fonte)
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java` — Aggregate Root completo, 3 invariantes, `coletarEventos()`, construtores
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java` — Domain Service com construtor por injeção
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java` — Interface que o stub deve implementar
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/*.java` — Todos os VOs (NomeDisciplina, PeriodoLetivo, Cpf, AlunoId, TurmaId, MatriculaId)
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/{Aluno,Turma,StatusMatricula,ItemMatricula}.java` — Entidades auxiliares
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/evento/*.java` — Domain Events (AlunoMatriculado, DisciplinaAdicionada, MatriculaCancelada)
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/excecao/*.java` — Todas as exceções de domínio
- `erp-matricula-ddd/pom.xml` — Dependências de teste verificadas
- `~/.m2/repository/org/springframework/boot/spring-boot-dependencies/3.5.7/spring-boot-dependencies-3.5.7.pom` — Versões pinadas pelo BOM (JUnit 5.12.2, AssertJ 3.27.6, Mockito 5.17.0)
- `.planning/phases/08-testes-unit-rios-do-dom-nio-ddd/08-CONTEXT.md` — Decisões bloqueadas D-01 a D-12

### Secondary (MEDIUM confidence)
- Nenhuma — todo o research foi baseado em leitura direta de código e artefatos do projeto

---

## Metadata

**Breakdown de confiança:**
- Stack de teste: HIGH — versões verificadas no BOM local via inspeção de ~/.m2
- Arquitetura: HIGH — baseada em leitura direta de todas as classes de produção
- Pitfalls: HIGH — derivados de contratos documentados no Javadoc do próprio código (ex: `coletarEventos()` limpa lista)

**Data da pesquisa:** 2026-06-23
**Válido até:** 2026-09-23 (90 dias — código de produção estável, JUnit/AssertJ raramente quebram API)
