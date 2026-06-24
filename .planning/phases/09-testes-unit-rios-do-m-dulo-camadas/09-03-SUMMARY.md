---
plan: 09-03
phase: 09
status: complete
completed_at: "2026-06-24"
key-files:
  created:
    - erp-matricula-camadas/src/test/java/br/com/escola/matricula/controller/MatriculaControllerTest.java
    - erp-matricula-camadas/src/test/resources/application.properties
  modified: []
commits:
  - "test(09-03): criar MatriculaControllerTest evidenciando DIAG-05"
---

# Summary: 09-03 MatriculaControllerTest — Regras na Interface (DIAG-05)

## O que foi construído

`MatriculaControllerTest.java` com 3 testes `@WebMvcTest` + MockMvc demonstrando que testar regras no controller exige Spring context parcial — contraste direto com `MatriculaServiceImplTest` que usa `@ExtendWith(MockitoExtension)` sem Spring. `verify(service, never())` nos dois testes de bloqueio comprova que o service não é chamado quando as regras do controller barram a requisição.

## Decisões tomadas

- **Fallback aplicado (RESEARCH.md Pitfall 1)**: `@MapperScan` na `ErpMatriculaCamadasApplication` força o MyBatis scanner a registrar os 4 mappers como `MapperFactoryBean` no contexto Spring mesmo em `@WebMvcTest`. A solução foi adicionar `@MockBean` para os 4 repositórios — não para uso nos testes (o controller não os acessa diretamente), mas para satisfazer a inicialização do contexto. Comentário inline documenta o motivo.
- `src/test/resources/application.properties` criado com exclusão de `MybatisAutoConfiguration` e `DataSourceAutoConfiguration` — condição necessária mas não suficiente; os `@MockBean` dos repositórios foram o que realmente resolveu.

## Verificação

- `mvn test -Dtest=MatriculaControllerTest`: **BUILD SUCCESS** — Tests run: 3, Failures: 0, Errors: 0
- `@WebMvcTest(MatriculaController.class)` presente
- `verify(matriculaService, never())` em 2 testes de bloqueio
- `"1999-02-01"` e `"AB"` presentes como valores de bloqueio
- `.andExpect(status().isCreated())` no happy path
- Zero `MockMvcBuilders.standaloneSetup`

## Self-Check: PASSED
