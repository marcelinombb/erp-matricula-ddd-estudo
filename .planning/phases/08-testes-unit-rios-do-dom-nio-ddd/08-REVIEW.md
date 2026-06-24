---
phase: 08
status: findings
critical: 1
warning: 2
info: 4
reviewed_files: 7
depth: standard
---

# Phase 08: Code Review — Testes Unitários do Domínio DDD

**Revisado em:** 2026-06-24T08:50:00-03:00
**Profundidade:** standard
**Arquivos revisados:** 7
**Status:** findings

## Resumo

Os testes cobrem corretamente as invariantes do Aggregate `Matricula`, os três Value Objects de núcleo (`NomeDisciplina`, `PeriodoLetivo`, `Cpf`, `AlunoId`), os Domain Events e o Domain Service `VerificadorElegibilidadeMatricula`. Restrições obrigatórias D-12 (zero imports Spring), D-01 (zero Mockito) e D-09/D-10/D-11 (Given-When-Then, nomes em português, AssertJ) estão respeitadas em todos os sete arquivos.

Um achado crítico compromete a confiabilidade do suite de CI: dois testes em `VerificadorElegibilidadeMatriculaTest` têm dependência de data embutida em `LocalDate.now()` dentro de `Turma.periodoEstaAberto()`. Esses testes passam hoje (24/06/2026) mas vão falhar após 31/07/2026 — ainda este ano.

---

## Critical Issues

### CR-01: Dois testes falharão após 31/07/2026 — dependência de data não controlada

**Arquivo:** `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatriculaTest.java:88` e `:138`

**Issue:**
`devePermitirMatriculaQuandoAlunoAtivoEPeriodoAbertoElegivelSemDuplicidade` e `deveLancarExcecaoQuandoMatriculaDuplicada` criam uma `Turma` com `PeriodoLetivo(2026, 1)` (semestre 1: 01/fev–31/jul/2026). O método `Turma.periodoEstaAberto()` usa `LocalDate.now()` internamente — sem Clock injetável ou override.

- **A partir de 01/08/2026**, `periodoEstaAberto()` retornará `false` para `(2026, 1)`.
- `devePermitirMatriculaQuandoAlunoAtivoEPeriodoAbertoElegivelSemDuplicidade` (linha 88): o verificador lançará `PeriodoFechadoException` — o `assertThatCode(...).doesNotThrowAnyException()` **falha**.
- `deveLancarExcecaoQuandoMatriculaDuplicada` (linha 138): a execução não chega à verificação de duplicidade porque `PeriodoFechadoException` é lançada antes — o `isInstanceOf(MatriculaDuplicadaException.class)` **falha**.

O teste `deveLancarExcecaoQuandoPeriodoFechado` (linha 120) está correto — usa `PeriodoLetivo(2020, 1)` (passado fixo, sempre fechado).

**Fix (opção A — preferida para este projeto didático):** Extrair criação de `Turma` em fixture com período que só usa o passado fixo para o teste de duplicidade, e usar um `PeriodoLetivo` passado-mas-aberto impossível sem clock. A solução mais robusta é tornar `Turma.periodoEstaAberto()` testável via injeção de `Clock`:

```java
// Turma.java — adicionar overload para teste
public boolean periodoEstaAberto(LocalDate hoje) {
    int ano = periodoLetivo.ano();
    int semestre = periodoLetivo.semestre();
    LocalDate inicio = (semestre == 1)
        ? LocalDate.of(ano, 2, 1)
        : LocalDate.of(ano, 8, 1);
    LocalDate fim = (semestre == 1)
        ? LocalDate.of(ano, 7, 31)
        : LocalDate.of(ano, 12, 31);
    return !hoje.isBefore(inicio) && !hoje.isAfter(fim);
}

public boolean periodoEstaAberto() {
    return periodoEstaAberto(LocalDate.now());
}
```

Com isso, `VerificadorElegibilidadeMatricula.verificar()` pode receber `LocalDate` ou usar um `Clock` injetável para que os testes passem em qualquer data.

**Fix (opção B — mínima invasão, sem alterar produção):** Substituir `PeriodoLetivo(2026, 1)` por `PeriodoLetivo(semestre 2 do ano corrente)` de forma dinâmica — mas isso reintroduz a dependência de data. A solução correta é tornar o corte de data controlável via `Clock`.

---

## Warnings

### WR-01: `deveAceitarNomeComExatamente100Chars` constrói `NomeDisciplina` duas vezes — `assertThatCode` é redundante

**Arquivo:** `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/NomeDisciplinaTest.java:56-65`

**Issue:**
O teste chama o construtor duas vezes com o mesmo input:

```java
// linha 60
assertThatCode(() -> new NomeDisciplina(nome100Chars)).doesNotThrowAnyException();

// linha 63
var vo = new NomeDisciplina(nome100Chars);
assertThat(vo.valor()).hasSize(100);
```

Se a construção na linha 63 não lançar exceção, o teste passa. Se lançar, o teste falha — independente da assertiva na linha 60. O `assertThatCode` da linha 60 é completamente subsumed pela construção da linha 63 e não acrescenta nenhuma informação. A leitura dupla confunde quem lê o teste sobre qual linha é a "then" definitiva.

**Fix:**
```java
@Test
@DisplayName("deve aceitar nome com exatamente 100 caracteres")
void deveAceitarNomeComExatamente100Chars() {
    // given
    var nome100Chars = "A".repeat(100);

    // when
    var vo = new NomeDisciplina(nome100Chars);

    // then
    assertThat(vo.valor()).hasSize(100);
}
```

---

### WR-02: `MatriculaTest` não cobre o guard de `cancelar()` em matrícula já cancelada

**Arquivo:** `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/modelo/MatriculaTest.java`

**Issue:**
A produção tem um guard explícito em `Matricula.cancelar()` (linha 228–230):

```java
public void cancelar() {
    if (this.status instanceof StatusMatricula.Cancelada) {
        throw new MatriculaCanceladaException(this.id);
    }
    ...
}
```

O teste `deveLancarExcecaoAoAdicionarDisciplinaEmMatriculaCancelada` (Guard 1) cobre a proteção em `adicionarDisciplina()`, mas **não há nenhum teste** para `cancelar()` numa matrícula já cancelada. O guard documentado no Javadoc da produção fica sem cobertura.

Adicionalmente, não há cobertura para o estado `StatusMatricula.Concluida`: `adicionarDisciplina()` não guarda contra `Concluida` (só verifica `instanceof Cancelada`), o que significa que uma matrícula concluída aceita novas disciplinas silenciosamente — comportamento provavelmente incorreto, porém não testado.

**Fix:**
```java
@Test
@DisplayName("deve lançar exceção ao cancelar matrícula já cancelada")
void deveLancarExcecaoAoCancelarMatriculaJaCancelada() {
    // given
    Matricula matricula = criarMatriculaAtiva();
    matricula.cancelar();
    matricula.coletarEventos(); // descarta MatriculaCancelada

    // when / then — segundo cancelamento deve lançar MatriculaCanceladaException
    assertThatThrownBy(() -> matricula.cancelar())
        .isInstanceOf(MatriculaCanceladaException.class);
}
```

---

## Info

### IN-01: `CpfTest` e `AlunoIdTest` não testam `hashCode()` explicitamente

**Arquivos:**
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/CpfTest.java:56-64`
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/AlunoIdTest.java:48-53`

**Issue:**
`NomeDisciplinaTest` (linha 76) testa explicitamente `a.hashCode() == b.hashCode()` para dois objetos iguais. `CpfTest.doisCpfComMesmoValorDevemSerIguais` e `AlunoIdTest.doisComMesmoUuidDevemSerIguais` usam apenas `isEqualTo()` sem verificar `hashCode()`. Para `record` Java 21 isso é improvável de ser um bug, mas a cobertura é inconsistente entre os VOs.

**Fix:**
```java
// CpfTest.java
assertThat(cpfComMascara).isEqualTo(cpfNormalizado);
assertThat(cpfComMascara.hashCode()).isEqualTo(cpfNormalizado.hashCode()); // adicionar

// AlunoIdTest.java
assertThat(new AlunoId(uuid)).isEqualTo(new AlunoId(uuid));
assertThat(new AlunoId(uuid).hashCode()).isEqualTo(new AlunoId(uuid).hashCode()); // adicionar
```

---

### IN-02: `NomeDisciplinaTest` e `CpfTest` não cobrem entrada `null`

**Arquivos:**
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/NomeDisciplinaTest.java`
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/CpfTest.java`

**Issue:**
Ambas as classes de produção lançam `NullPointerException` para `null` via `Objects.requireNonNull`. `AlunoIdTest` cobre esse caso explicitamente (linha 39–44). `NomeDisciplinaTest` e `CpfTest` omitem o teste de `null`, deixando a validação nula sem cobertura.

**Fix:**
```java
// NomeDisciplinaTest.java
@Test
@DisplayName("deve lançar NullPointerException para nome nulo")
void deveLancarExcecaoParaNomeNulo() {
    assertThatNullPointerException()
        .isThrownBy(() -> new NomeDisciplina(null));
}

// CpfTest.java
@Test
@DisplayName("deve lançar NullPointerException para CPF nulo")
void deveLancarExcecaoParaCpfNulo() {
    assertThatNullPointerException()
        .isThrownBy(() -> new Cpf(null));
}
```

---

### IN-03: `PeriodoLetivoTest` não testa o limite inferior válido (ano 2000)

**Arquivo:** `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/PeriodoLetivoTest.java`

**Issue:**
O teste `deveLancarExcecaoParaAnoAnteriorA2000` usa `ano = 1999` (inválido). Não há teste para `ano = 2000` (válido — limite exato da fronteira). O critério `ano < 2000` é verificado, mas o boundary válido `PeriodoLetivo(2000, 1)` não é instanciado em nenhum teste.

**Fix:**
```java
@Test
@DisplayName("deve aceitar ano exatamente 2000 como válido (boundary)")
void deveAceitarAno2000ComoValido() {
    assertThatCode(() -> new PeriodoLetivo(2000, 1))
        .doesNotThrowAnyException();
}
```

---

### IN-04: `MatriculaTest` não testa `adicionarDisciplina()` em matrícula com status `Concluida`

**Arquivo:** `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/modelo/MatriculaTest.java`

**Issue:**
`Matricula.adicionarDisciplina()` usa `if (this.status instanceof StatusMatricula.Cancelada)` — verifica apenas `Cancelada`. O estado `Concluida` (também terminal) **não é guardado**: uma matrícula concluída aceita novas disciplinas sem lançar exceção. Essa lacuna de produção não é coberta por nenhum teste, o que significa que o comportamento incorreto não é visível no suite.

Um teste que documente o comportamento atual (mesmo que seja expor o bug) serve como especificação de regressão.

**Fix:**
```java
@Test
@DisplayName("deve lançar exceção ao adicionar disciplina em matrícula concluída")
void deveLancarExcecaoAoAdicionarDisciplinaEmMatriculaConcluida() {
    // given — reconstitui matrícula no estado Concluida
    var matriculaConcluida = new Matricula(
        new MatriculaId(UUID.randomUUID()),
        new AlunoId(UUID.randomUUID()),
        new TurmaId(UUID.randomUUID()),
        new PeriodoLetivo(2026, 1),
        new StatusMatricula.Concluida(LocalDateTime.now().minusDays(1)),
        List.of()
    );

    // when / then — atualmente NÃO lança (bug de produção) — este teste documenta a lacuna
    assertThatThrownBy(() -> matriculaConcluida.adicionarDisciplina(new NomeDisciplina("Física")))
        .isInstanceOf(IllegalStateException.class); // ou MatriculaConcluidaException quando criada
}
```

---

_Revisado em: 2026-06-24T08:50:00-03:00_
_Revisor: Claude (gsd-code-reviewer)_
_Profundidade: standard_
