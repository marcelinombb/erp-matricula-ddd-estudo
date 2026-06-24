---
plan: 09-01
phase: 09
status: complete
completed_at: "2026-06-24"
key-files:
  created:
    - erp-matricula-camadas/src/test/java/br/com/escola/matricula/service/MatriculaServiceImplTest.java
  modified: []
commits:
  - "test(09-01): criar MatriculaServiceImplTest evidenciando DIAG-03"
---

# Summary: 09-01 MatriculaServiceImplTest — God Service (DIAG-03)

## O que foi construído

`MatriculaServiceImplTest.java` com 7 testes Mockito puros demonstrando o custo de isolar o God Service: 4 campos `@Mock` necessários para um único `@InjectMocks MatriculaServiceImpl`.

## Decisões tomadas

- Usado `@MockitoSettings(strictness = Strictness.LENIENT)` para permitir stubs globais no `@BeforeEach` sem `UnnecessaryStubbingException` — Mockito strict mode rejeita stubs configurados globalmente mas não usados em todos os testes (ex: `turmaRepository` e `matriculaRepository.findById` são irrelevantes para os testes de `adicionarDisciplina`)
- Comentários DIAG-02, DIAG-03 e DIAG-06 posicionados nos pontos pedagógicos exatos conforme o plano

## Verificação

- `mvn test -Dtest=MatriculaServiceImplTest`: **BUILD SUCCESS** — Tests run: 7, Failures: 0, Errors: 0
- 4 campos `@Mock` com comentário DIAG-03
- Zero imports `org.springframework`
- 5 chamadas `assertThatThrownBy` (testes de exceção)
- Mensagem exata `"Aluno inativo não pode ser matriculado"` presente
- Mensagem parcial `"Limite de 6 disciplinas"` verificada via `hasMessageContaining`

## Self-Check: PASSED
