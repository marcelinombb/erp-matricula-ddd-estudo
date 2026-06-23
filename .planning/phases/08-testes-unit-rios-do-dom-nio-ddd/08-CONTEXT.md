# Phase 8: Testes Unitários do Domínio DDD - Context

**Gathered:** 2026-06-23
**Status:** Ready for planning

<domain>
## Phase Boundary

Criar testes unitários dos artefatos do módulo `erp-matricula-ddd` — Aggregate Matricula, Value Objects, Domain Service VerificadorElegibilidadeMatricula e Domain Events — **sem Spring, sem banco de dados**. Os testes evidenciam que um domínio rico é inerentemente testável de forma isolada.

O módulo `erp-matricula-camadas` é tocado apenas na Phase 9. Esta fase foca exclusivamente no módulo DDD.

</domain>

<decisions>
## Implementation Decisions

### Stub do Repositório para o Domain Service

- **D-01:** Usar stub in-memory escrito à mão para `MatriculaRepositorio` em vez de Mockito. Criar classe `MatriculaRepositorioEmMemoria` que implementa apenas `existeMatriculaAtiva()` — os demais métodos lançam `UnsupportedOperationException`. Isso demonstra que os testes do domínio ficam sem dependência de nenhum framework de mock.
- **D-02:** O stub deve ficar no mesmo pacote do teste que o usa: `br.com.escola.matricula.dominio.servico` em `src/test/java`.

### Organização dos Arquivos de Teste

- **D-03:** Um arquivo de teste por classe de produção: `MatriculaTest.java`, `NomeDisciplinaTest.java`, `PeriodoLetivoTest.java`, `VerificadorElegibilidadeMatriculaTest.java` etc. Convencional, fácil de navegar no IDE.
- **D-04:** Um arquivo por VO (não agrupar todos os VOs num único arquivo). Cada VO tem suas próprias invariantes que merecem arquivo dedicado.
- **D-05:** Testes de Domain Events ficam dentro de `MatriculaTest.java` — eventos são comportamento do Aggregate, não uma preocupação separada.

### Padrão de Asserção de Domain Events

- **D-06:** Coletar o retorno de `coletarEventos()` e verificar tipo + dados: `var evento = (AlunoMatriculado) eventos.get(0); assertThat(evento.alunoId()).isEqualTo(alunoId)`. Mais expressivo que só verificar `isInstanceOf`.
- **D-07:** Atenção: `coletarEventos()` limpa a lista interna após retornar — os testes não devem chamar o método duas vezes esperando o mesmo resultado.

### Estrutura de Pacotes

- **D-08:** Espelhar a estrutura de `src/main/java` em `src/test/java`:
  ```
  src/test/java/br/com/escola/matricula/dominio/
    modelo/MatriculaTest.java
    vo/NomeDisciplinaTest.java
    vo/PeriodoLetivoTest.java
    vo/CpfTest.java
    vo/AlunoIdTest.java
    servico/VerificadorElegibilidadeMatriculaTest.java
    servico/MatriculaRepositorioEmMemoria.java  ← stub de teste
  ```

### Convenções dos Testes

- **D-09:** Padrão Given-When-Then com comentários explícitos (`// given`, `// when`, `// then`) — reforça a narrativa pedagógica e facilita a leitura do teste como especificação de negócio.
- **D-10:** Nomes de métodos de teste em português, descrevendo o comportamento esperado: `deveAdicionarDisciplinaComSucesso()`, `deveLancarExcecaoQuandoMatriculaCancelada()`.
- **D-11:** Usar AssertJ (já incluído no `spring-boot-starter-test`) para asserções fluentes: `assertThat(...)` em vez de `assertEquals(...)`.
- **D-12:** Zero imports de `org.springframework.*` nos arquivos de teste do domínio — verificável por inspeção.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Aggregate e Invariantes

- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java` — Aggregate Root com 3 invariantes encapsuladas, método `coletarEventos()`, construtor de reconstituição
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java` — Domain Service com dependência em `MatriculaRepositorio` (interface de domínio)
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java` — interface de repositório que o stub deve implementar

### Value Objects

- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/NomeDisciplina.java` — record com validação de comprimento e strip
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/PeriodoLetivo.java` — record imutável (ano, semestre)
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/Cpf.java` — record com validação de dígito verificador
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/AlunoId.java` — record com UUID

### Domain Events

- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/evento/AlunoMatriculado.java`
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/evento/DisciplinaAdicionada.java`
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/evento/MatriculaCancelada.java`

### Exceções de Domínio

- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/excecao/` — todas as exceções lançadas pelas invariantes

### Dependências de Teste

- `erp-matricula-ddd/pom.xml` — `spring-boot-starter-test` inclui JUnit 5 + AssertJ + Mockito (Mockito disponível mas não obrigatório para testes do domínio)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `Matricula.criar(alunoId, turmaId, periodoLetivo)` — factory method estático, zero dependências para instanciar o Aggregate nos testes
- `Matricula(id, alunoId, turmaId, periodoLetivo, status, disciplinas)` — construtor de reconstituição para testes que precisam do Aggregate em estado específico (ex: já cancelado, com disciplinas pré-existentes)
- VOs são `record` Java 21 — `new NomeDisciplina("Matemática")` é suficiente para instanciar, sem builder ou factory
- `StatusMatricula.Ativa`, `StatusMatricula.Cancelada(LocalDateTime)` — sealed class, instanciar diretamente nos testes

### Established Patterns

- **Zero Spring nos testes do domínio:** `Matricula.java` não tem nenhum import `org.springframework.*`. Os testes devem manter o mesmo padrão.
- **Eventos via `coletarEventos()`:** método limpa a lista interna após retornar — padrão explicitado no Javadoc do Aggregate
- **Exceções são exceções de domínio sem Spring:** `LimiteDisciplinasExcedidoException`, `MatriculaCanceladaException` etc. — verificar com `assertThatThrownBy(...).isInstanceOf(...)` do AssertJ

### Integration Points

- `VerificadorElegibilidadeMatricula` depende de `MatriculaRepositorio` — interface no pacote `dominio/repositorio/`. O stub implementa apenas `existeMatriculaAtiva(AlunoId, PeriodoLetivo)`.
- `Aluno` e `Turma` — entidades usadas pelo Verificador. Verificar se têm construtores acessíveis para instanciar diretamente nos testes ou se precisam de builder.

</code_context>

<specifics>
## Specific Ideas

- O stub `MatriculaRepositorioEmMemoria` deve ter campo configurável para controlar o retorno de `existeMatriculaAtiva()`: `stub.comMatriculaExistente()` e `stub.semMatriculaExistente()` — padrão fluente de configuração de teste.
- O teste do VerificadorElegibilidade deve ter casos: aluno inativo, período fechado, matrícula duplicada, e o happy path — um teste por regra de negócio.
- Para VOs como `Cpf`, testar explicitamente CPFs inválidos que devem lançar exceção — demonstra que "objeto que existe = objeto válido".

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 8-Testes Unitários do Domínio DDD*
*Context gathered: 2026-06-23*
