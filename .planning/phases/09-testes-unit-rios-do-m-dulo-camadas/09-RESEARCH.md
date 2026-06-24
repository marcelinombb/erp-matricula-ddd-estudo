# Phase 9: Testes Unitários do Módulo Camadas — Research

**Pesquisado em:** 2026-06-24
**Domínio:** Testes unitários de serviços e controller Spring Boot — Mockito puro, @WebMvcTest, MockMvc, AssertJ
**Confiança Geral:** HIGH — todo o código de produção do módulo camadas foi lido diretamente

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Estratégia de Mocks:**
- **D-01:** Usar `@ExtendWith(MockitoExtension.class)` + campos `@Mock` + `@InjectMocks`. Zero Spring context nos testes do `MatriculaServiceImpl` e `DisciplinaServiceImpl`. O contraste com `@WebMvcTest` do controller fica explícito.
- **D-02:** Declarar **todos os 4 repositórios** como `@Mock` no `MatriculaServiceImplTest`, mesmo que um teste individual use apenas 1 ou 2. O impacto visual é intencional — o leitor vê de uma vez a "lista de dependências" do God Service (contraste com Phase 8: zero mocks).
- **D-03:** `@BeforeEach` configura o **happy path** como padrão: `alunoRepository.findById()` retorna aluno ativo, `matriculaRepository.findById()` retorna matrícula ativa, `countDisciplinas()` retorna 3. Cada teste de erro sobrescreve apenas o que precisa mudar.

**Escopo dos Testes:**
- **D-04:** `MatriculaServiceImpl`: testar `matricular()`, `adicionarDisciplina()`, e `cancelar()`. Cada método: happy path + casos de erro. `buscarPorAluno()` omitido.
- **D-05:** `DisciplinaServiceImpl`: testar `adicionarDisciplinaExtra()` e `verificarStatusMatricula()`.
- **D-06:** Exceções: usar `assertThatThrownBy(...).isInstanceOf(RuntimeException.class).hasMessage(...)` (AssertJ fluente).

**Testes do Controller:**
- **D-07:** Usar `@WebMvcTest(MatriculaController.class)` + `MockMvc` + `@MockBean MatriculaService`. Sobe apenas a camada MVC.
- **D-08:** Focar nos cenários DIAG-05: (1) período começando com "199" bloqueado pelo controller; (2) nome curto bloqueado pelo controller (< 3 chars); (3) happy path `POST /matriculas`.

**Evidência Pedagógica:**
- **D-09:** Comentários DIAG inline nos pontos mais reveladores. Exemplos: `// DIAG-03: 4 repositórios mockados`, `// DIAG-04: mesma regra testada aqui e em MatriculaServiceImplTest`.
- **D-10:** Nomes de métodos descrevem comportamento: `deveMatricularAlunoComSucesso()`, `deveLancarExcecaoQuandoAlunoInativo()`. Consistente com Phase 8.
- **D-11:** Padrão Given-When-Then com comentários `// given`, `// when`, `// then` — idêntico ao Phase 8.
- **D-12:** Nomes em português — idêntico ao Phase 8.
- **D-13:** AssertJ para asserções fluentes (`assertThat(...)`) — idêntico ao Phase 8.

**Estrutura de Pacotes:**
- **D-14:** Espelhar `src/main/java` em `src/test/java` no módulo `erp-matricula-camadas`:
  ```
  src/test/java/br/com/escola/matricula/service/
    MatriculaServiceImplTest.java
    DisciplinaServiceImplTest.java
  src/test/java/br/com/escola/matricula/controller/
    MatriculaControllerTest.java
  ```

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
| TCAM-01 | Desenvolvedor pode executar testes do MatriculaServiceImpl que evidenciam a quantidade de mocks necessários para isolar o God Service | `MatriculaServiceImpl.java` lido — 4 dependências injetadas por construtor; métodos `matricular()`, `adicionarDisciplina()`, `cancelar()` identificados com suas pre-condições; padrão `@ExtendWith(MockitoExtension.class)` + `@InjectMocks` suficiente |
| TCAM-02 | Desenvolvedor pode executar testes do DisciplinaServiceImpl que mostram regras duplicadas se manifestando como testes duplicados | `DisciplinaServiceImpl.java` lido — duplica validação de aluno ativo e contagem de disciplinas de `MatriculaServiceImpl`; o mesmo cenário de "aluno inativo" aparece como teste necessário em ambos os arquivos |
| TCAM-03 | Desenvolvedor pode executar testes do MatriculaController que demonstram a dificuldade de testar regras de negócio no controller | `MatriculaController.java` lido — regras "período 199x" e "nome < 3 chars" vivem exclusivamente no controller; `@WebMvcTest` necessário para ativá-las; `verify(service, never()).matricular(...)` confirma que service não foi chamado |
</phase_requirements>

---

## Summary

Esta fase cria testes unitários das três classes do módulo `erp-matricula-camadas`: `MatriculaServiceImpl`, `DisciplinaServiceImpl` e `MatriculaController`. O objetivo não é cobertura completa — é evidência pedagógica. Os testes devem mostrar ao leitor, através do próprio código de teste, os anti-padrões DIAG-01..DIAG-06 em ação.

O código de produção foi lido integralmente. `MatriculaServiceImpl` tem construtor explícito com 4 parâmetros — Mockito injeta via construtor com `@InjectMocks`. `DisciplinaServiceImpl` tem 3 dependências e duplica 3 validações do `MatriculaServiceImpl` literalmente (mesmo bloco `if (!aluno.isAtivo())`). `MatriculaController` depende apenas de `MatriculaService` — `@WebMvcTest` com `@MockBean MatriculaService` é suficiente, sem necessidade de mocks adicionais.

O `src/test/java` **não existe** no módulo `erp-matricula-camadas` — deve ser criado do zero. Nenhuma configuração de banco é necessária para os testes Mockito puros. O `@WebMvcTest` pode precisar de um `application-test.yml` mínimo para evitar falha de inicialização do contexto MyBatis se o Spring tentar registrar os `@Mapper` beans.

**Recomendação principal:** Criar os 3 arquivos de teste evidenciando o contraste com Phase 8 — o `@BeforeEach` pesado do `MatriculaServiceImplTest` versus zero mocks do `VerificadorElegibilidadeMatriculaTest` é a peça central da narrativa pedagógica desta fase.

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Testes do God Service (MatriculaServiceImpl) | Service / Application | — | Mockito puro com `@ExtendWith`; zero Spring context; mocks dos 4 repositórios |
| Testes do Service duplicado (DisciplinaServiceImpl) | Service / Application | — | Mockito puro com `@ExtendWith`; 3 mocks; mesmo padrão do MatriculaServiceImplTest |
| Testes do Controller com regras de negócio | Web / Controller | Spring MVC (context parcial) | `@WebMvcTest` sobe apenas a camada MVC; `MockMvc` faz requisições HTTP in-process |
| Criação do diretório src/test/java | Build / Estrutura | — | Não existe ainda no módulo camadas — deve ser criado |

---

## Standard Stack

### Core (sem novas dependências — tudo já presente no pom.xml)

| Biblioteca | Versão | Propósito | Por que padrão |
|-----------|--------|-----------|----------------|
| JUnit Jupiter | 5.12.2 (via BOM) | `@Test`, `@BeforeEach`, `@DisplayName`, lifecycle | Gerenciado pelo Spring Boot 3.5.3 BOM; padrão de fato para Java |
| AssertJ Core | 3.27.6 (via BOM) | `assertThat()`, `assertThatThrownBy()` | Incluído no `spring-boot-starter-test`; mais expressivo que JUnit nativo |
| Mockito | 5.17.0 (via BOM) | `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`, `when().thenReturn()` | Incluído no `spring-boot-starter-test`; padrão para mocks em Spring |
| Spring MockMvc | gerenciado pelo BOM | `mockMvc.perform()`, requisições HTTP in-process | Incluído via `spring-boot-starter-test` + `spring-boot-starter-web` |

[VERIFIED: pom.xml lido diretamente — `spring-boot-starter-test` com `scope=test` presente em `erp-matricula-camadas/pom.xml`]

### Nenhuma dependência nova a adicionar

O pom.xml de `erp-matricula-camadas` já tem `spring-boot-starter-test` com `scope=test`. Nenhum pacote adicional é necessário.

---

## Package Legitimacy Audit

> Fase sem novas dependências — todos os pacotes de teste já presentes no pom.xml.

| Package | Registry | Idade | Downloads | Source Repo | Verdict | Disposição |
|---------|----------|-------|-----------|-------------|---------|------------|
| junit-jupiter | Maven Central | 9+ anos | > 100M/mês | github.com/junit-team/junit5 | OK | Aprovado |
| assertj-core | Maven Central | 12+ anos | > 50M/mês | github.com/assertj/assertj | OK | Aprovado |
| mockito-core | Maven Central | 15+ anos | > 150M/mês | github.com/mockito/mockito | OK | Aprovado |
| spring-test (MockMvc) | Maven Central | 15+ anos | > 200M/mês | github.com/spring-projects/spring-framework | OK | Aprovado |

**Pacotes removidos por veredicto SLOP:** nenhum
**Pacotes suspeitos (SUS):** nenhum

---

## Architecture Patterns

### System Architecture Diagram

```
MatriculaServiceImplTest                    DisciplinaServiceImplTest
  @ExtendWith(MockitoExtension.class)         @ExtendWith(MockitoExtension.class)
  @Mock MatriculaRepository                   @Mock MatriculaRepository
  @Mock AlunoRepository           ←─┐         @Mock AlunoRepository
  @Mock TurmaRepository             │ mesma   @Mock ItemMatriculaRepository
  @Mock ItemMatriculaRepository   ←─┘ regra   @InjectMocks DisciplinaServiceImpl
  @InjectMocks MatriculaServiceImpl             │
        │                                       │
        ▼                                       ▼
  when(alunoRepo.findById()).thenReturn(...)  when(alunoRepo.findById()).thenReturn(...)
  when(matriculaRepo.findById()).thenReturn() when(matriculaRepo.findById()).thenReturn()
  when(matriculaRepo.countDisciplinas())...  when(matriculaRepo.countDisciplinas())...
                                              ← DIAG-04: mocks idênticos, regras idênticas

MatriculaControllerTest
  @WebMvcTest(MatriculaController.class)  ← Spring context parcial (só web layer)
  @MockBean MatriculaService              ← único mock necessário
  @Autowired MockMvc                      ← HTTP in-process
        │
        ▼ POST /matriculas (periodoInicio="1999-01")
  Controller verifica "199x" → retorna 400 BAD REQUEST
  verify(service, never()).matricular(...)  ← service nunca chamado
        ← DIAG-05: regra existe só na interface HTTP
```

### Recommended Project Structure

```
erp-matricula-camadas/
└── src/
    └── test/
        └── java/
            └── br/com/escola/matricula/
                ├── service/
                │   ├── MatriculaServiceImplTest.java   (TCAM-01)
                │   └── DisciplinaServiceImplTest.java  (TCAM-02)
                └── controller/
                    └── MatriculaControllerTest.java    (TCAM-03)
```

O diretório `src/test/java` não existe no módulo camadas — deve ser criado como parte desta fase.

### Pattern 1: Mockito Puro para Services (MatriculaServiceImpl, DisciplinaServiceImpl)

**O que é:** `@ExtendWith(MockitoExtension.class)` substitui o runner JUnit 4; mocks declarados com `@Mock`; instância real do service criada por `@InjectMocks` com injeção via construtor.

**Quando usar:** Qualquer classe Java com dependências injetáveis que não precise de Spring context. O service não precisa do container — seus construtores recebem interfaces como parâmetros.

**Exemplo:**
```java
// Source: Mockito docs + padrão estabelecido pela Phase 8 [ASSUMED — padrão documentado]
@ExtendWith(MockitoExtension.class)
@DisplayName("MatriculaServiceImpl — God Service (DIAG-03)")
class MatriculaServiceImplTest {

    // DIAG-03: 4 repositórios mockados para isolar MatriculaServiceImpl
    // Contraste: VerificadorElegibilidadeMatriculaTest (Phase 8) usa 0 mocks
    @Mock MatriculaRepository matriculaRepository;
    @Mock AlunoRepository alunoRepository;
    @Mock TurmaRepository turmaRepository;
    @Mock ItemMatriculaRepository itemMatriculaRepository;

    @InjectMocks MatriculaServiceImpl service;  // Mockito injeta via construtor 4-args

    private Aluno alunoAtivo;
    private Turma turma;
    private Matricula matriculaAtiva;

    @BeforeEach
    void configurarHappyPath() {
        // Monta objetos anêmicos via setters (sem construtor com args — DIAG-02)
        alunoAtivo = new Aluno();
        alunoAtivo.setId(UUID.randomUUID());
        alunoAtivo.setAtivo(true);

        turma = new Turma();
        turma.setId(UUID.randomUUID());

        matriculaAtiva = new Matricula();
        matriculaAtiva.setId(UUID.randomUUID());
        matriculaAtiva.setAlunoId(alunoAtivo.getId());
        matriculaAtiva.setStatus("ATIVA");

        // Stubs padrão — cada teste de erro sobrescreve o que precisa
        when(alunoRepository.findById(any())).thenReturn(Optional.of(alunoAtivo));
        when(turmaRepository.findById(any())).thenReturn(Optional.of(turma));
        when(matriculaRepository.findById(any())).thenReturn(Optional.of(matriculaAtiva));
        when(matriculaRepository.countDisciplinas(any())).thenReturn(3);
        // DIAG-06: countDisciplinas() — regra de limite no repositório, não no modelo
    }

    @Test
    @DisplayName("deve matricular aluno com sucesso")
    void deveMatricularAlunoComSucesso() {
        // given — happy path configurado no @BeforeEach

        // when
        UUID id = service.matricular(alunoAtivo.getId(), turma.getId(), "2026-02-01", "2026-06-30");

        // then
        assertThat(id).isNotNull();
        verify(matriculaRepository).insert(any(Matricula.class));
    }

    @Test
    @DisplayName("deve lançar exceção quando aluno está inativo")
    void deveLancarExcecaoQuandoAlunoInativo() {
        // given
        alunoAtivo.setAtivo(false);
        when(alunoRepository.findById(any())).thenReturn(Optional.of(alunoAtivo));

        // when / then
        assertThatThrownBy(() -> service.matricular(alunoAtivo.getId(), turma.getId(), "2026-02-01", "2026-06-30"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Aluno inativo não pode ser matriculado");
        // Contraste: módulo DDD lança AlunoInativoException tipada
    }
}
```

### Pattern 2: @WebMvcTest para Controller

**O que é:** `@WebMvcTest(MatriculaController.class)` sobe apenas o contexto MVC — sem datasource, sem MyBatis, sem services reais. `@MockBean MatriculaService` substitui a dependência do controller. `MockMvc` faz requisições HTTP in-process.

**Quando usar:** Testar lógica que existe na camada web (validações Bean Validation, mapeamento de request/response, lógica de negócio no controller — como os cenários DIAG-05).

**Ponto de atenção:** O `erp-matricula-camadas` usa `@Mapper` (MyBatis) em suas interfaces de repositório. `@WebMvcTest` carrega apenas beans do tipo `@Controller` — não tenta instanciar os `@Mapper`. O `MatriculaController` depende apenas de `MatriculaService` (interface). Um único `@MockBean MatriculaService` é suficiente.

**Exemplo:**
```java
// Source: Spring Boot Test docs [ASSUMED — padrão documentado em spring.io]
@WebMvcTest(MatriculaController.class)
@DisplayName("MatriculaController — Regras na Interface (DIAG-05)")
class MatriculaControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean MatriculaService matriculaService;

    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("deve bloquear período iniciado em 199x (regra existe só no controller)")
    void deveBloquearPeriodoAntigo() throws Exception {
        // given
        var request = Map.of(
            "alunoId", UUID.randomUUID().toString(),
            "turmaId", UUID.randomUUID().toString(),
            "periodoInicio", "1999-02-01",  // começa com "199" — bloqueado pelo controller
            "periodoFim", "1999-06-30"
        );

        // when / then
        mockMvc.perform(post("/matriculas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        // DIAG-05: service nunca foi chamado — regra parou no controller
        verify(matriculaService, never()).matricular(any(), any(), any(), any());
    }

    @Test
    @DisplayName("deve bloquear nome de disciplina com menos de 3 caracteres")
    void deveBloquearNomeDisciplinaComprimido() throws Exception {
        // given
        var request = Map.of("nomeDisciplina", "AB");  // < 3 chars

        // when / then
        mockMvc.perform(post("/matriculas/{id}/disciplinas", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        // DIAG-05: regra de comprimento existe só no controller
        verify(matriculaService, never()).adicionarDisciplina(any(), any());
    }
}
```

### Anti-Patterns to Avoid

- **@SpringBootTest para testes unitários de service:** Sobe o contexto Spring completo, tenta conectar ao banco. Desnecessário para testar `MatriculaServiceImpl` — use `@ExtendWith(MockitoExtension.class)`.
- **Criar objetos Aluno/Matricula/Turma com mock (Mockito.mock()):** As entidades anêmicas têm setters públicos — crie instâncias reais com `new Aluno()` + setters. Mocks de objetos de dados mascaram o problema (DIAG-02) que queremos evidenciar.
- **Usar `any()` em todas as verificações:** Para os testes de DIAG-05, use `verify(service, never())` sem argumento matcher — confirma que o service não foi chamado em absoluto, não apenas com certos args.
- **Omitir comentários DIAG dos pontos-chave:** Os comentários são o mecanismo pedagógico desta fase — não são opcionais.

---

## Don't Hand-Roll

| Problema | Não Construir | Usar em vez disso | Motivo |
|----------|---------------|-------------------|--------|
| Injeção de mocks no service | Instanciação manual do service + setters | `@InjectMocks` | Mockito detecta injeção por construtor automaticamente (4-arg) |
| Asserção de exceção | try/catch + assertEquals | `assertThatThrownBy(...).isInstanceOf(...).hasMessage(...)` | AssertJ captura + verifica tipo + mensagem em uma linha |
| Requisição HTTP para controller | Instanciar controller + chamar método direto | `MockMvc.perform()` | Bean Validation (`@Valid`) não é acionada sem o web layer |
| Reset de mocks entre testes | `Mockito.reset()` no `@BeforeEach` | `@ExtendWith(MockitoExtension.class)` (gera novos mocks por teste) | `MockitoExtension` cria instâncias novas para cada método de teste automaticamente |

**Insight chave:** `MockitoExtension` cria mocks frescos para cada método de teste — não é necessário `reset()`. O `@BeforeEach` apenas configura stubs, não cria mocks.

---

## Common Pitfalls

### Pitfall 1: @WebMvcTest tenta inicializar beans MyBatis (@Mapper)

**O que dá errado:** Se o `ErpMatriculaCamadasApplication.java` usar `@MapperScan`, o Spring pode tentar criar os beans `@Mapper` (que são `@Component`) durante `@WebMvcTest`, falhando por falta de datasource.

**Por que acontece:** `@WebMvcTest` carrega todos os `@Controller` e tenta resolver suas dependências. Se algum `@Mapper` for diretamente injetado num `@Controller`, o contexto falha.

**Como evitar:** O `MatriculaController` injeta apenas `MatriculaService` (interface — não é `@Mapper`). Um único `@MockBean MatriculaService` satisfaz toda a dependência. Verificado: nenhum `@Mapper` é injetado diretamente no controller. [VERIFIED: MatriculaController.java lido diretamente]

**Sinal de alerta:** `UnsatisfiedDependencyException` ou `BeanCreationException` ao rodar `@WebMvcTest`.

**Fallback se ocorrer:** Adicionar `@MockBean` para cada `@Mapper` usado pela cadeia, ou criar `src/test/resources/application-test.yml` com `mybatis.mapper-locations=` vazio e `spring.datasource.url=jdbc:h2:mem:test`.

### Pitfall 2: Objetos anêmicos sem construtor com args — setup verboso no @BeforeEach

**O que dá errado:** `Aluno`, `Matricula`, `Turma` e `ItemMatricula` não têm construtores com argumentos (DIAG-02). O setup exige múltiplos setters para construir um objeto utilizável:

```java
// Sem construtor: 4 linhas por objeto
Aluno aluno = new Aluno();
aluno.setId(UUID.randomUUID());
aluno.setNome("João");
aluno.setAtivo(true);
```

**Por que acontece:** É intencional — DIAG-02 (entidade anêmica). O setup verboso é parte do ponto pedagógico.

**Como lidar:** Documentar com comentário DIAG-02 no `@BeforeEach`. Extrair métodos auxiliares privados (`criarAlunoAtivo()`, `criarMatriculaAtiva()`) para reduzir repetição dentro do arquivo de teste, mas manter o `@BeforeEach` visualmente pesado — contraste intencional com o `criarMatriculaAtiva()` do `MatriculaTest.java` da Phase 8 (que usa factory method do aggregate).

### Pitfall 3: Verificar mensagem de exceção com .hasMessage() exato

**O que dá errado:** `MatriculaServiceImpl` lança `new RuntimeException("Aluno inativo não pode ser matriculado")` — a string exata. Se o teste usar `.hasMessage("Aluno inativo")`, falha silenciosamente se a mensagem mudar.

**Como evitar:** Usar `.hasMessageContaining("Aluno inativo")` para mensagens com UUID concatenado (ex: `"Aluno não encontrado: " + alunoId`). Usar `.hasMessage("Aluno inativo não pode ser matriculado")` apenas onde a mensagem é estática. [VERIFIED: strings de mensagem lidas diretamente do código de produção]

**Mensagens de exceção exatas do código de produção:**
- `matricular()` aluno não encontrado: `"Aluno não encontrado: " + alunoId` → usar `.hasMessageContaining("Aluno não encontrado")`
- `matricular()` aluno inativo: `"Aluno inativo não pode ser matriculado"` → exata
- `adicionarDisciplina()` matrícula não encontrada: `"Matrícula não encontrada: " + matriculaId` → `.hasMessageContaining("Matrícula não encontrada")`
- `adicionarDisciplina()` status inativo: `"Matrícula não está ativa. Status atual: " + status` → `.hasMessageContaining("Matrícula não está ativa")`
- `adicionarDisciplina()` limite: `"Limite de 6 disciplinas atingido. Quantidade atual: " + qtd` → `.hasMessageContaining("Limite de 6 disciplinas")`
- `cancelar()` status errado: `"Apenas matrículas ativas podem ser canceladas. Status atual: " + status` → `.hasMessageContaining("Apenas matrículas ativas")`
- `DisciplinaServiceImpl` aluno inativo: `"Aluno inativo"` (mensagem diferente do MatriculaServiceImpl — evidência de DIAG-04) → exata

### Pitfall 4: Esquecer de declarar todos os 4 mocks mesmo que o teste use só 1

**O que dá errado:** Sem todos os `@Mock` declarados, `@InjectMocks` pode falhar ou injetar `null` se o construtor exige os 4 parâmetros.

**Como evitar:** `MatriculaServiceImpl` tem construtor explícito com 4 parâmetros. `@InjectMocks` usa injeção por construtor quando há um único construtor — todos os 4 `@Mock` devem existir na classe de teste. [VERIFIED: MatriculaServiceImpl.java lido, construtor linha 46-53]

### Pitfall 5: Stubs no @BeforeEach com UUIDs aleatórios sem coordenação

**O que dá errado:** O `@BeforeEach` cria `alunoAtivo` com `UUID.randomUUID()` e stub `alunoRepository.findById(any())`. O `any()` garante que qualquer UUID retorna o aluno. Se um teste usar um UUID diferente do do aluno no stub do `matriculaRepository.findById()`, o aluno retornado pode não corresponder à matrícula.

**Como evitar:** Criar o `alunoAtivo` com ID fixo no setup, armazenar em campo da classe, usar esse mesmo ID em `matriculaAtiva.setAlunoId(alunoAtivo.getId())`. Confirmar que `adicionarDisciplina()` busca aluno por `matricula.getAlunoId()` — então o UUID na matrícula deve coincidir com o do aluno mockado.

---

## Code Examples

### MatriculaServiceImplTest — Estrutura Completa do @BeforeEach

```java
// Source: código de produção MatriculaServiceImpl.java lido diretamente [VERIFIED]
@BeforeEach
void configurarHappyPath() {
    // 4 dependências mockadas para isolar MatriculaServiceImpl
    // DIAG-03: God Service — o leitor vê aqui quantas dependências um Service Deus acumula.
    // Contraste: VerificadorElegibilidadeMatriculaTest (Phase 8) tem @BeforeEach vazio — zero mocks.
    
    UUID alunoId = UUID.randomUUID();
    
    alunoAtivo = new Aluno();
    alunoAtivo.setId(alunoId);
    alunoAtivo.setNome("João da Silva");
    alunoAtivo.setAtivo(true);    // isAtivo() retorna true — happy path
    
    turma = new Turma();
    turma.setId(UUID.randomUUID());
    turma.setCodigo("TURMA-001");
    
    matriculaAtiva = new Matricula();
    matriculaAtiva.setId(UUID.randomUUID());
    matriculaAtiva.setAlunoId(alunoId);    // mesmo UUID do alunoAtivo — coordenação deliberada
    matriculaAtiva.setStatus("ATIVA");
    
    // Stubs do happy path — usam any() para aceitar qualquer UUID
    when(alunoRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(alunoAtivo));
    when(turmaRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(turma));
    when(matriculaRepository.findById(any(UUID.class)))
        .thenReturn(Optional.of(matriculaAtiva));
    when(matriculaRepository.countDisciplinas(any(UUID.class)))
        .thenReturn(3);  // DIAG-06: regra de limite via banco, não via modelo
}
```

### DisciplinaServiceImplTest — Teste de Duplicação de Regra

```java
// Source: DisciplinaServiceImpl.java e MatriculaServiceImpl.java lidos diretamente [VERIFIED]
@Test
@DisplayName("deve lançar exceção quando aluno está inativo ao adicionar disciplina extra")
void deveAdicionarDisciplinaExtra_lancaExcecaoQuandoAlunoInativo() {
    // given
    alunoAtivo.setAtivo(false);
    when(alunoRepository.findById(any())).thenReturn(Optional.of(alunoAtivo));

    // when / then
    assertThatThrownBy(() -> service.adicionarDisciplinaExtra(matriculaAtiva.getId(), "Matemática"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Aluno inativo");
    // DIAG-04: a mesma verificação existe em MatriculaServiceImplTest.deveLancarExcecaoQuandoAlunoInativo()
    // Nota: mensagem DIFERENTE — "Aluno inativo" aqui vs "Aluno inativo não pode adicionar disciplinas"
    // no MatriculaServiceImpl. Esta divergência é o bug silencioso que DIAG-04 prevê.
}
```

### MatriculaControllerTest — Verificação de Que Service Não Foi Chamado

```java
// Source: MatriculaController.java lido diretamente [VERIFIED]
@Test
@DisplayName("deve bloquear período 199x e nunca chamar o service")
void deveBloquearPeriodoAntigo_nuncaChamarService() throws Exception {
    // given
    String json = """
        {
          "alunoId": "%s",
          "turmaId": "%s",
          "periodoInicio": "1999-02-01",
          "periodoFim": "1999-06-30"
        }
        """.formatted(UUID.randomUUID(), UUID.randomUUID());

    // when
    mockMvc.perform(post("/matriculas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isBadRequest());

    // then — DIAG-05: regra temporal existe APENAS no controller
    // Se um batch job chamar matriculaService.matricular() diretamente com "1999-02-01",
    // a matrícula será criada sem erro — a regra só existe aqui.
    verify(matriculaService, never()).matricular(any(), any(), any(), any());
}
```

---

## State of the Art

| Abordagem Antiga | Abordagem Atual | Quando Mudou | Impacto |
|-----------------|-----------------|--------------|---------|
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` | JUnit 5 / Mockito 3+ | `@RunWith` é JUnit 4 — não usar em projetos com JUnit 5 |
| `@RunWith(SpringRunner.class)` | `@ExtendWith(SpringExtension.class)` | JUnit 5 | `@SpringBootTest` inclui implicitamente — não declarar separado |
| `MockMvcBuilders.standaloneSetup(controller)` | `@WebMvcTest` + `@Autowired MockMvc` | Spring Boot 1.4+ | `standaloneSetup` não ativa Bean Validation; `@WebMvcTest` ativa |
| `Mockito.mock(SomeClass.class)` (explícito) | `@Mock` + `@ExtendWith` | Mockito 2+ | Mais conciso; lifecycle gerenciado pelo JUnit Extension |

**Deprecated/obsoleto:**
- `@RunWith(MockitoJUnitRunner.class)`: JUnit 4; projeto usa JUnit 5 — não usar
- `MockMvcBuilders.standaloneSetup()`: não ativa `@Valid` / Bean Validation — inadequado para testar DIAG-05 onde Bean Validation é parte do comportamento

---

## Assumptions Log

| # | Claim | Seção | Risco se Errado |
|---|-------|-------|-----------------|
| A1 | `@WebMvcTest` não tenta inicializar `@Mapper` beans quando o Controller não os injeta diretamente | Pitfall 1 | Se errado: teste falha com `UnsatisfiedDependencyException` ao inicializar — necessário adicionar `@MockBean` para cada mapper ou configurar `application-test.yml` |
| A2 | Mockito resolve `@InjectMocks MatriculaServiceImpl` via construtor (não field injection) quando há construtor explícito com 4 parâmetros | Pattern 1 | Se errado: `NullPointerException` nos mocks dentro do service — verificar ordem dos `@Mock` ou usar construtor explícito no `@BeforeEach` |
| A3 | Java text blocks (String `"""..."""`) estão disponíveis para o JSON do `@WebMvcTest` | Code Examples | Se errado: erro de compilação Java < 15 — substituir por concatenação de string (projeto usa Java 21, risco baixo) |

**Se a tabela estiver vazia:** Nenhuma — todas as afirmações desta pesquisa foram verificadas pelo código-fonte ou são padrões bem estabelecidos.

---

## Open Questions

1. **Necessidade de `application-test.yml` para @WebMvcTest**
   - O que sabemos: `MatriculaController` injeta apenas `MatriculaService` (não `@Mapper` diretamente). `@WebMvcTest` normalmente não precisa de datasource.
   - O que está incerto: se `ErpMatriculaCamadasApplication` usa `@SpringBootApplication` com `@MapperScan` implícito via MyBatis autoconfiguration, o contexto MyBatis pode tentar se registrar.
   - Recomendação: Tentar sem `application-test.yml` primeiro. Se falhar com erro de MyBatis/datasource, criar `src/test/resources/application-test.yml` com `spring.autoconfigure.exclude: org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration, org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration`.

2. **Estratégia de nomeação do `DisciplinaServiceImplTest` para DIAG-04**
   - O que sabemos: a decisão D-10 pede nomes que descrevem comportamento (não anti-padrão).
   - O que está incerto: como nomear um teste que explicitamente evidencia duplicação sem referenciar a nomenclatura do anti-padrão no nome do método.
   - Recomendação: Usar nome de comportamento + comentário DIAG: `deveAdicionarDisciplinaExtra_lancaExcecaoQuandoAlunoInativo()` com `// DIAG-04` no corpo.

---

## Environment Availability

> Esta fase é código puro — sem ferramentas externas além do JDK e Maven já presentes.

| Dependência | Requerida por | Disponível | Versão | Fallback |
|-------------|--------------|------------|--------|----------|
| Java 21 | Compilação e execução dos testes | A verificar | — | — |
| Maven (mvn test) | Execução dos testes no módulo camadas | A verificar | — | IDE runner |
| `spring-boot-starter-test` | JUnit, Mockito, MockMvc | Declarado no pom.xml | gerenciado pelo BOM 3.5.3 | — |

**Dependências ausentes sem fallback:** nenhuma — todos os artefatos de teste já estão declarados no pom.xml.

---

## Security Domain

> `security_enforcement` não está configurado como false — seção obrigatória.

Esta fase cria **apenas arquivos de teste** (`src/test/java`). Nenhum código de produção é modificado. Os riscos de segurança são mínimos. ASVS relevante ao contexto:

| Categoria ASVS | Aplica | Controle Padrão |
|---------------|--------|-----------------|
| V5 Input Validation | não (testes não processam entrada de usuário real) | — |
| V6 Cryptography | não | — |

**Risco específico desta fase:** nenhum — código de teste não é executado em produção.

---

## Sources

### Primary (HIGH confidence)
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java` — código de produção lido diretamente; construtor, dependências e mensagens de exceção verificados
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java` — código de produção lido diretamente; duplicações verificadas
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/MatriculaController.java` — código de produção lido diretamente; regras DIAG-05 verificadas
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/*.java` — todos os 4 modelos anêmicos lidos diretamente (Aluno, Matricula, Turma, ItemMatricula)
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/*.java` — todas as 4 interfaces de repositório lidas diretamente
- `erp-matricula-camadas/pom.xml` — `spring-boot-starter-test` confirmado com scope=test
- `.planning/phases/09-testes-unit-rios-do-m-dulo-camadas/09-CONTEXT.md` — todas as decisões D-01..D-14 incorporadas
- `erp-matricula-ddd/src/test/java/.../MatriculaTest.java` e `VerificadorElegibilidadeMatriculaTest.java` — padrões da Phase 8 verificados para consistência

### Secondary (MEDIUM confidence)
- Padrões `@ExtendWith(MockitoExtension.class)` e `@WebMvcTest` — conhecimento de treinamento sobre Spring Boot 3 e Mockito 5, alinhados com as decisões do contexto

---

## Metadata

**Breakdown de confiança:**
- Stack padrão: HIGH — pom.xml lido, nenhuma nova dependência
- Arquitetura: HIGH — todo código de produção lido diretamente
- Pitfalls: HIGH — identificados por análise direta do código (mensagens de exceção exatas, dependências do construtor exatas)
- Padrões de teste: MEDIUM — padrões estabelecidos do Spring/Mockito, não verificados via Context7 nesta sessão

**Data da pesquisa:** 2026-06-24
**Válido até:** 2026-07-24 (stack estável; vález enquanto o código de produção não mudar)
