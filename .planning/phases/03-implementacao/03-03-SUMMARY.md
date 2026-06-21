---
phase: "03-implementacao"
plan: "03"
subsystem: "aplicacao"
tags: ["ddd", "application-layer", "use-cases", "commands", "dto", "spring", "transactional", "java21"]
dependency_graph:
  requires:
    - phase: "03-02"
      provides: "Matricula aggregate root, MatriculaRepositorio interface, VerificadorElegibilidadeMatricula, MatriculaNaoEncontradaException"
  provides:
    - "aplicacao.MatricularAlunoCommand: record imutável carregando Aluno, Turma, PeriodoLetivo"
    - "aplicacao.AdicionarDisciplinaCommand: record imutável com MatriculaId e NomeDisciplina"
    - "aplicacao.CancelarMatriculaCommand: record imutável com MatriculaId"
    - "aplicacao.MatriculaDto: DTO de leitura com factory method de(Matricula)"
    - "aplicacao.MatricularAlunoUseCase: orquestração com verificar → criar → salvar → publicar (D-10)"
    - "aplicacao.AdicionarDisciplinaUseCase: orquestração com buscar → operar → salvar → publicar"
    - "aplicacao.CancelarMatriculaUseCase: orquestração com buscar → cancelar → salvar → publicar"
  affects:
    - "03-04-PLAN (infraestrutura): MatriculaRepositorioMyBatis implementa MatriculaRepositorio usada pelos UseCases"
    - "Fase 4 (Controllers REST): UseCases são o ponto de entrada para Controllers HTTP"
tech_stack:
  added:
    - "Spring @Service e @Transactional nos três UseCases"
    - "Spring ApplicationEventPublisher para publicação de Domain Events após persistência"
  patterns:
    - "Application Service Pattern: UseCases orquestram sem decidir — toda lógica no Aggregate/Domain Service"
    - "Command Pattern: Commands como Java 21 records imutáveis carregando objetos de domínio"
    - "D-10: sequência invariante verificar → criar/operar → salvar → coletarEventos → publicar"
    - "DTO factory method: MatriculaDto.de(Matricula) com pattern matching exaustivo em StatusMatricula"
    - "Injeção por construtor sem @Autowired: Spring 4.3+ injeta automaticamente com construtor único"
key_files:
  created:
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoCommand.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/AdicionarDisciplinaCommand.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/CancelarMatriculaCommand.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatriculaDto.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/AdicionarDisciplinaUseCase.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/CancelarMatriculaUseCase.java"
  modified: []
key_decisions:
  - "Commands carregam objetos de domínio (Aluno, Turma), não primitivos — Controller na Fase 4 constrói os objetos"
  - "MatriculaDto usa pattern matching exaustivo no switch de StatusMatricula — compilador aponta se novo estado for adicionado sem tratamento"
  - "AdicionarDisciplinaUseCase sem VerificadorElegibilidadeMatricula — elegibilidade já foi verificada no momento da matrícula; invariantes adicionais protegidas pelo Aggregate"
  - "Injeção por construtor sem @Autowired explícito: Spring 4.3+ suficiente, código mais limpo"
patterns-established:
  - "UseCase.executar(Command): entry point para todos os fluxos de escrita da aplicação"
  - "Sequência D-10: [verificar →] operar → salvar → coletarEventos().forEach(publicar) invariante em todos os UseCases"
  - "Exceções de domínio propagam naturalmente: UseCases não tratam exceções, deixam subir ao caller"
requirements-completed: [APL-01, APL-02, APL-03, APL-04]
duration: "~15 minutos"
completed: "2026-06-21"
---

# Phase 03 Plan 03: Camada de Aplicação (UseCases) — Summary

**Três Application Services com @Service + @Transactional orquestrando os fluxos matricular, adicionar disciplina e cancelar, usando Commands como Java 21 records imutáveis e seguindo a sequência D-10 invariante: operar → salvar → publicar eventos**

---

## Performance

- **Duration:** ~15 minutos
- **Started:** 2026-06-21T00:00:00Z
- **Completed:** 2026-06-21T00:15:00Z
- **Tasks:** 2
- **Files modified:** 7 criados

---

## Accomplishments

- 3 Commands como Java 21 records imutáveis carregando objetos de domínio (não primitivos)
- `MatriculaDto` com factory method `de(Matricula)` usando pattern matching exaustivo na sealed interface `StatusMatricula`
- 3 UseCases com `@Service + @Transactional`, cada um respeitando a sequência D-10 sem exceção
- Zero lógica de negócio nos UseCases — toda decisão delegada ao Aggregate e ao Domain Service

---

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Commands, DTO e MatricularAlunoUseCase | a7c8eaa | 5 arquivos |
| 2 | AdicionarDisciplinaUseCase e CancelarMatriculaUseCase | 4a8ea84 | 2 arquivos |

---

## What Was Built

### Task 1: Commands, DTO e MatricularAlunoUseCase

**Commands (aplicacao/)**

- `MatricularAlunoCommand(Aluno, Turma, PeriodoLetivo)`: carrega objetos de domínio já construídos. Javadoc pedagógico explica a separação de responsabilidades com o Controller (Fase 4).
- `AdicionarDisciplinaCommand(MatriculaId, NomeDisciplina)`: identifica matrícula existente e disciplina a adicionar.
- `CancelarMatriculaCommand(MatriculaId)`: Command mínimo — cancelar requer apenas identidade.

**MatriculaDto**

Record com `(matriculaId, alunoId, statusDescricao, totalDisciplinas)`. Factory method `de(Matricula)` com `switch` exaustivo sobre `StatusMatricula` sealed interface — compilador aponta se estado novo for adicionado sem tratamento correspondente.

**MatricularAlunoUseCase**

`@Service + @Transactional`. Injeção por construtor de `MatriculaRepositorio`, `VerificadorElegibilidadeMatricula`, `ApplicationEventPublisher`. Sequência D-10 comentada passo a passo:
1. `verificador.verificar(command.aluno(), command.turma(), command.periodo())` — Domain Service decide
2. `Matricula.criar(...)` — Aggregate gera UUID, coleta `AlunoMatriculado`
3. `repositorio.salvar(matricula)` — persistência antes dos eventos
4. `matricula.coletarEventos().forEach(publicador::publishEvent)` — publicação após commit

### Task 2: AdicionarDisciplinaUseCase e CancelarMatriculaUseCase

**AdicionarDisciplinaUseCase**

`@Service + @Transactional`. Construtor com `MatriculaRepositorio` e `ApplicationEventPublisher` (sem verificador — elegibilidade já verificada na criação). Usa `orElseThrow(() -> new MatriculaNaoEncontradaException(...))`. Delega `adicionarDisciplina()` ao Aggregate — este protege as 3 invariantes (estado, limite, duplicidade). Sequência D-10 respeitada.

**CancelarMatriculaUseCase**

Mesmo padrão. Delega `cancelar()` ao Aggregate. Publica `MatriculaCancelada` — listeners stub `FinanceiroEventListener` e `AcademicoEventListener` (Plano 04) recebem após commit via `@TransactionalEventListener`.

---

## Verification Results

```
mvn compile -q                                     → BUILD SUCCESS (exit 0)
7 arquivos em aplicacao/                           → PASSED
@Service em todos os 3 UseCases                    → PASSED
@Transactional em todos os 3 UseCases              → PASSED
Ordem verificar(91) < criar(94) < salvar(100) < coletarEventos(105) → PASSED
orElseThrow em AdicionarDisciplina e Cancelar      → PASSED
Nenhum if (status|disciplina|limite) nos UseCases  → PASSED (apenas Javadoc/comentários)
Zero HTTP annotations em aplicacao/               → PASSED
```

---

## Deviations from Plan

None — plano executado exatamente como especificado. A sequência D-10 foi implementada nos três UseCases conforme especificação. O `MatriculaDto` com pattern matching exaustivo foi implementado conforme planejado.

---

## Known Stubs

Nenhum — os 7 arquivos têm implementação completa. Não existem valores hardcoded vazios, placeholders ou TODOs de funcionalidade. Os UseCases dependem de `MatriculaRepositorio` (implementado pelo Plano 04 — `MatriculaRepositorioMyBatis`) e de `VerificadorElegibilidadeMatricula` (já implementado no Plano 02 como classe Java pura, configurado como `@Bean` no Plano 04).

---

## Threat Surface Scan

Nenhum novo endpoint de rede, caminho de autenticação ou acesso a arquivo foi introduzido. A camada de aplicação não expõe HTTP — sem `@RestController`, `@RequestMapping` ou equivalentes. O `ApplicationEventPublisher` é intra-processo (Spring) — zero surface de rede. Ameaças T-03-04 e T-03-05 do threat model aceitas conforme planejado.

---

## Next Phase Readiness

A camada de aplicação está completa. O Plano 04 (infraestrutura) pode implementar `MatriculaRepositorioMyBatis` que será injetado nos UseCases via a interface `MatriculaRepositorio`. Os três UseCases são os entry points que os Controllers REST da Fase 4 chamarão diretamente.

---

## Self-Check: PASSED

- [x] MatricularAlunoCommand.java existe
- [x] AdicionarDisciplinaCommand.java existe
- [x] CancelarMatriculaCommand.java existe
- [x] MatriculaDto.java existe
- [x] MatricularAlunoUseCase.java existe
- [x] AdicionarDisciplinaUseCase.java existe
- [x] CancelarMatriculaUseCase.java existe
- [x] Commit a7c8eaa (Task 1) existe em git log
- [x] Commit 4a8ea84 (Task 2) existe em git log
- [x] mvn compile -q retorna BUILD SUCCESS (exit 0)

---

*Phase: 03-implementacao*
*Completed: 2026-06-21*
