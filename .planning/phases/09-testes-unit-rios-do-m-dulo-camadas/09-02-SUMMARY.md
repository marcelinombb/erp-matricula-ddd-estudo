---
plan: 09-02
phase: 09
status: complete
completed_at: "2026-06-24"
key-files:
  created:
    - erp-matricula-camadas/src/test/java/br/com/escola/matricula/service/DisciplinaServiceImplTest.java
  modified: []
commits:
  - "test(09-02): criar DisciplinaServiceImplTest evidenciando DIAG-04"
---

# Summary: 09-02 DisciplinaServiceImplTest — Duplicação de Regras (DIAG-04)

## O que foi construído

`DisciplinaServiceImplTest.java` com 5 testes Mockito puros demonstrando que a duplicação de código de produção (DIAG-04) gera duplicação de código de teste. O mesmo cenário de erro (aluno inativo) exige setup idêntico em dois arquivos de teste distintos, e a divergência de mensagens ("Aluno inativo" vs "Aluno inativo não pode ser matriculado") é o bug silencioso documentado nos comentários.

## Decisões tomadas

- Mesmo padrão `@MockitoSettings(strictness = Strictness.LENIENT)` de 09-01, pelo mesmo motivo: stubs globais do `@BeforeEach` são irrelevantes para alguns testes
- O teste `deveLancarExcecaoQuandoAlunoInativo` usa `.hasMessage("Aluno inativo")` (string exata, sem sufixo) conforme `DisciplinaServiceImpl.java:83` — não `.hasMessageContaining(...)` para maximizar o impacto pedagógico da divergência

## Verificação

- `mvn test -Dtest=DisciplinaServiceImplTest`: **BUILD SUCCESS** — Tests run: 5, Failures: 0, Errors: 0
- 3 campos `@Mock` com comentário DIAG-04
- Comentário explícito sobre divergência de mensagem entre os dois services
- Teste `deveVerificarSeMatriculaEstaAtiva` demonstra proliferação de métodos verificadores (DIAG-04)
- Zero imports `org.springframework`

## Self-Check: PASSED
