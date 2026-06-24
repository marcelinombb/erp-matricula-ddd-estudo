# Phase 9: Testes Unitários do Módulo Camadas - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-24
**Phase:** 9-Testes Unitários do Módulo Camadas
**Areas discussed:** Estratégia de Mocks, Escopo dos Testes, Testes do Controller, Evidência Pedagógica

---

## Estratégia de Mocks

| Option | Description | Selected |
|--------|-------------|----------|
| MockitoExtension (recomendado) | `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks`. Zero Spring context. | ✓ |
| Mockito.mock() manual no @BeforeEach | Sem extension: `service = new MatriculaServiceImpl(mock(Repo.class), ...)`. Mais verboso, construção explícita. | |
| @SpringBootTest com @MockBean | Sobe contexto Spring, mais pesado, contradiz unit test rápido. | |

**User's choice:** MockitoExtension (recomendado)
**Notes:** —

| Option | Description | Selected |
|--------|-------------|----------|
| Todos os 4 repositórios (recomendado) | Declara todos os 4 `@Mock` mesmo que um teste use apenas 1 ou 2. Impacto visual máximo. | ✓ |
| Só os necessários por teste | Mocks inline em cada método. Menos evidente. | |
| Você decide | Sem preferência. | |

**User's choice:** Todos os 4 repositórios

| Option | Description | Selected |
|--------|-------------|----------|
| Happy path no @BeforeEach + overrides por teste (recomendado) | Stubs padrão no @BeforeEach, cada teste de erro sobrescreve o necessário. | ✓ |
| Tudo inline em cada teste | Nenhum stub no @BeforeEach — cada método configura do zero. | |
| Você decide | Sem preferência. | |

**User's choice:** Happy path no @BeforeEach + overrides por teste

---

## Escopo dos Testes

| Option | Description | Selected |
|--------|-------------|----------|
| matricular() + adicionarDisciplina() + cancelar() (recomendado) | Os 3 métodos com regras de negócio. `buscarPorAluno()` omitido. | ✓ |
| Todos os métodos públicos incluindo buscarPorAluno() | Cobertura total. | |
| Só adicionarDisciplina() | Foco no método com mais anti-padrões. | |

**User's choice:** matricular() + adicionarDisciplina() + cancelar()

| Option | Description | Selected |
|--------|-------------|----------|
| Sim, os 2 métodos (recomendado) | `adicionarDisciplinaExtra()` mostra duplicação, `verificarStatusMatricula()` mostra proliferação. | ✓ |
| Só adicionarDisciplinaExtra() | Foco na duplicação de lógica. | |
| Você decide | Sem preferência. | |

**User's choice:** Sim, os 2 métodos do DisciplinaServiceImpl

| Option | Description | Selected |
|--------|-------------|----------|
| assertThatThrownBy com isInstanceOf(RuntimeException.class) e hasMessage() (recomendado) | AssertJ fluente, consistente com Phase 8. | ✓ |
| assertThrows do JUnit 5 | Equivalente mas inconsistente com Phase 8. | |
| Você decide | Sem preferência. | |

**User's choice:** assertThatThrownBy (AssertJ)

---

## Testes do Controller

| Option | Description | Selected |
|--------|-------------|----------|
| @WebMvcTest + MockMvc (recomendado) | Sobe camada MVC do Spring + `@MockBean`. Mostra que até um "unit test do controller" precisa de Spring. | ✓ |
| Instância direta sem Spring | `controller = new MatriculaController(mock(...))`. Mais rápido, mas não testa @Valid nem @ExceptionHandler. | |
| Você decide | Sem preferência. | |

**User's choice:** @WebMvcTest + MockMvc

| Option | Description | Selected |
|--------|-------------|----------|
| Regras DIAG-05 + happy path (recomendado) | Período 199x + nome curto + happy path. Foco no que é invisível via service direto. | ✓ |
| Cobertura total do controller | Todos os 3 endpoints, todos os cenários. | |
| Você decide | Deixar planejador decidir. | |

**User's choice:** Regras DIAG-05 + happy path

---

## Evidência Pedagógica

| Option | Description | Selected |
|--------|-------------|----------|
| Comentários DIAG inline nos testes (recomendado) | Comentários nos métodos onde o anti-padrão se manifesta. | ✓ |
| Javadoc na classe de teste explicando o contraste | Mais concentrado, menos verboso nos métodos. | |
| Combinar: Javadoc na classe + comentários chave nos métodos | Equilibra verbosidade e clareza. | |

**User's choice:** Comentários DIAG inline nos testes

| Option | Description | Selected |
|--------|-------------|----------|
| Não — nomes descrevem o comportamento como Phase 8 (recomendado) | `deveMatricularAlunoComSucesso()` — consistente com Phase 8. | ✓ |
| Sim — incluir o código DIAG no nome | `diag04_regrasAlunoInativo_DuplicadaEmDisciplinaService()` — quebra a convenção. | |
| Você decide | Sem preferência. | |

**User's choice:** Nomes descrevem comportamento (sem referência DIAG nos nomes)

---

## Claude's Discretion

Nenhuma área foi deixada para Claude decidir — todas as questões foram respondidas pelo usuário.

## Deferred Ideas

None — discussion stayed within phase scope.
