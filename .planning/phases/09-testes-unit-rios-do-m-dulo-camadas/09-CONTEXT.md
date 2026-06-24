# Phase 9: Testes Unitários do Módulo Camadas - Context

**Gathered:** 2026-06-24
**Status:** Ready for planning

<domain>
## Phase Boundary

Criar testes unitários das classes `MatriculaServiceImpl`, `DisciplinaServiceImpl` e `MatriculaController` do módulo `erp-matricula-camadas` — sem tocar no módulo `erp-matricula-ddd`. Os testes devem evidenciar visualmente os anti-padrões através do código de teste em si: bloco `@BeforeEach` com múltiplos mocks, regras duplicadas aparecendo como testes duplicados, e regras de negócio no controller exigindo mocks adicionais e contexto Spring.

O módulo `erp-matricula-ddd` já tem seus testes (Phase 8). Esta fase foca exclusivamente no módulo camadas.

</domain>

<decisions>
## Implementation Decisions

### Estratégia de Mocks

- **D-01:** Usar `@ExtendWith(MockitoExtension.class)` + campos `@Mock` + `@InjectMocks`. Zero Spring context nos testes do `MatriculaServiceImpl` e `DisciplinaServiceImpl`. O contraste com `@WebMvcTest` do controller fica explícito.
- **D-02:** Declarar **todos os 4 repositórios** como `@Mock` no `MatriculaServiceImplTest`, mesmo que um teste individual use apenas 1 ou 2. O impacto visual é intencional — o leitor vê de uma vez a "lista de dependências" do God Service (contraste com Phase 8: zero mocks).
- **D-03:** `@BeforeEach` configura o **happy path** como padrão: `alunoRepository.findById()` retorna aluno ativo, `matriculaRepository.findById()` retorna matrícula ativa, `countDisciplinas()` retorna 3. Cada teste de erro sobrescreve apenas o que precisa mudar.

### Escopo dos Testes

- **D-04:** `MatriculaServiceImpl`: testar `matricular()`, `adicionarDisciplina()`, e `cancelar()`. Cada método: happy path + casos de erro. `buscarPorAluno()` omitido — é query simples sem regras de negócio relevantes para o contraste.
- **D-05:** `DisciplinaServiceImpl`: testar `adicionarDisciplinaExtra()` e `verificarStatusMatricula()`. `adicionarDisciplinaExtra()` mostra a duplicação de mocks e regras em relação ao `MatriculaServiceImpl`; `verificarStatusMatricula()` revela a proliferação de métodos verificadores que ninguém usa.
- **D-06:** Exceções: usar `assertThatThrownBy(...).isInstanceOf(RuntimeException.class).hasMessage(...)` (AssertJ fluente). Consistente com Phase 8. O contraste com tipos específicos (`MatriculaCanceladaException`) do módulo DDD fica explícito via comentário DIAG.

### Testes do Controller

- **D-07:** Usar `@WebMvcTest(MatriculaController.class)` + `MockMvc` + `@MockBean MatriculaService`. Sobe apenas a camada MVC. O fato de precisar de Spring context para testar o controller é parte do contraste pedagógico com Phase 8.
- **D-08:** Focar nos cenários **DIAG-05**: (1) período começando com "199" bloqueado pelo controller mas aceito diretamente pelo service; (2) nome curto bloqueado pelo controller (< 3 chars); (3) happy path `POST /matriculas`. Esses cenários demonstram que a regra existe *só na interface* — invisível para chamadores que bypassam o HTTP.

### Evidência Pedagógica

- **D-09:** Usar **comentários DIAG inline** nos métodos de teste nos pontos mais reveladores. Exemplos:
  - No `@BeforeEach`: `// DIAG-03: 4 repositórios mockados — contraste com Phase 8 (zero mocks)`
  - No teste de duplicação: `// DIAG-04: mesma regra testada aqui e em MatriculaServiceImplTest`
  - No teste de contagem: `// DIAG-06: countDisciplinas() no repositório em vez do domínio`
- **D-10:** Nomes de métodos descrevem **comportamento**, não anti-padrões. Padrão: `deveMatricularAlunoComSucesso()`, `deveLancarExcecaoQuandoAlunoInativo()`. Consistente com Phase 8 (D-10 do 08-CONTEXT.md).
- **D-11:** Padrão Given-When-Then com comentários `// given`, `// when`, `// then` — idêntico ao Phase 8 (D-09 do 08-CONTEXT.md).
- **D-12:** Nomes em português — idêntico ao Phase 8.
- **D-13:** AssertJ para asserções fluentes (`assertThat(...)`) — idêntico ao Phase 8.

### Estrutura de Pacotes

- **D-14:** Espelhar `src/main/java` em `src/test/java` no módulo `erp-matricula-camadas`:
  ```
  src/test/java/br/com/escola/matricula/service/
    MatriculaServiceImplTest.java
    DisciplinaServiceImplTest.java
  src/test/java/br/com/escola/matricula/controller/
    MatriculaControllerTest.java
  ```

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Código de Produção (módulo camadas — fonte da verdade)

- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java` — God Service com 4 dependências, anti-padrões DIAG-01, DIAG-03, DIAG-04, DIAG-06 anotados inline
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java` — DIAG-04: duplica validações do MatriculaServiceImpl
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/MatriculaController.java` — DIAG-05: regras de negócio na interface HTTP
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Aluno.java` — entidade anêmica (DIAG-02), `isAtivo()` como getter
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java` — entidade anêmica, status como String livre
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java` — inclui `countDisciplinas()` e `updateStatus()` (lógica de negócio no repositório)

### Dependências de Teste

- `erp-matricula-camadas/pom.xml` — `spring-boot-starter-test` inclui JUnit 5 + AssertJ + Mockito + MockMvc (via spring-test)

### Referência de Contraste (Phase 8 — módulo DDD)

- `.planning/phases/08-testes-unit-rios-do-dom-nio-ddd/08-CONTEXT.md` — decisões D-09..D-12 estabelecem o padrão de nomenclatura, Given-When-Then e AssertJ que Phase 9 deve seguir
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatriculaTest.java` — exemplo de teste sem mocks (contraste direto com D-02)
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/modelo/MatriculaTest.java` — exemplo de teste com Given-When-Then e zero imports Spring

### Requirements

- `.planning/REQUIREMENTS.md` §TCAM-01..03 — TCAM-01 (mocks pesados visíveis), TCAM-02 (regras duplicadas como testes duplicados), TCAM-03 (dificuldade de testar regras no controller)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `MatriculaRepository`: interface com `insert()`, `findById()`, `findByAlunoId()`, `countDisciplinas()`, `updateStatus()` — todos os métodos que precisam ser mockados já conhecidos
- `AlunoRepository`: interface com `findById()` — único método usado pelos Services
- `TurmaRepository`: interface com `findById()` — usado em `matricular()` do MatriculaServiceImpl
- `ItemMatriculaRepository`: interface com `insert()` — usado nos métodos de adição de disciplina

### Established Patterns

- **Mockito puro nos services:** `@ExtendWith(MockitoExtension.class)` é o padrão correto para serviços sem Spring context. O `spring-boot-starter-test` já inclui Mockito.
- **@WebMvcTest para controller:** Sobe apenas o contexto MVC. `@MockBean` substitui o `MatriculaService` real.
- **Happy path como default no @BeforeEach:** Stubs padrão configurados uma vez; testes de erro sobrescrevem localmente com `when(...).thenReturn(...)`.
- **Aluno anêmico:** `Aluno` não tem construtor com args — verificar se tem setters para construir nos testes ou se precisa de `Mockito.mock(Aluno.class)`.

### Integration Points

- `MatriculaServiceImpl` é instanciado via `@InjectMocks` — Mockito injeta os mocks pelo construtor (construtor com 4 args explícito em `MatriculaServiceImpl.java:47`).
- `MatriculaController` usa `@Valid` + Bean Validation — `@WebMvcTest` habilita Bean Validation automaticamente via `spring-boot-starter-validation` (verificar se está no pom.xml).

</code_context>

<specifics>
## Specific Ideas

- O `@BeforeEach` do `MatriculaServiceImplTest` deve ter comentário de cabeçalho explicitando o número de mocks: `// 4 dependências mockadas para isolar MatriculaServiceImpl — contraste: VerificadorElegibilidadeMatriculaTest (Phase 8) usa 0 mocks`.
- Para `DisciplinaServiceImplTest`, criar um teste chamado `deveAdicionarDisciplinaExtra_duplicandoRegraDeAlunoAtivo()` que referencia explicitamente (via comentário DIAG-04) que a mesma asserção de "aluno inativo" também existe no `MatriculaServiceImplTest` — a duplicação de testes reflete a duplicação de regras.
- O teste de "período 199x" no `MatriculaControllerTest` deve chamar `matriculaService.matricular()` com `verify(matriculaService, never().matricular(...))` para confirmar que o service nunca foi chamado — a regra parou no controller e o service nem ficou sabendo.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 9-Testes Unitários do Módulo Camadas*
*Context gathered: 2026-06-24*
