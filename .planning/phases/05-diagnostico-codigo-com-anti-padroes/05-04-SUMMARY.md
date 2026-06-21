---
phase: 05-diagnostico-codigo-com-anti-padroes
plan: "04"
subsystem: erp-matricula-camadas/service
tags:
  - anti-patterns
  - service-anemico
  - service-deus
  - duplicacao-regras
  - acoplamento-banco
  - didatico
dependency_graph:
  requires:
    - "05-02: model classes (Aluno, Matricula, ItemMatricula, Turma)"
    - "05-03: repository interfaces (MatriculaRepository, ItemMatriculaRepository, AlunoRepository, TurmaRepository)"
  provides:
    - "MatriculaService interface com 4 métodos"
    - "MatriculaServiceImpl com DIAG-01 + DIAG-03 + DIAG-04 + DIAG-06"
    - "DisciplinaServiceImpl com DIAG-04 explícito"
  affects:
    - "05-05: MatriculaController (usa MatriculaService)"
tech_stack:
  added: []
  patterns:
    - "Service interface + impl com @Service @Transactional"
    - "Injeção por construtor sem @Autowired (Spring 4.3+)"
    - "RuntimeException genérica em vez de exceções tipadas (anti-padrão intencional)"
key_files:
  created:
    - "erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaService.java"
    - "erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java"
    - "erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java"
  modified: []
decisions:
  - "MatriculaServiceImpl.java escrito com 227 linhas (meta era 200+) para garantir demonstração visual do Service Deus"
  - "Método verificarElegibilidade() private incluído para demonstrar que extração para helper sem modelo rico não elimina a duplicação"
  - "DisciplinaServiceImpl não implementa MatriculaService — é um Service separado que demonstra como Services proliferam em arquitetura anêmica"
metrics:
  duration: "7 minutos"
  completed_date: "2026-06-21"
  tasks_completed: 2
  tasks_total: 2
  files_created: 3
  files_modified: 0
---

# Phase 05 Plan 04: Camada de Serviço com Anti-padrões Summary

Service layer anêmico com 227 linhas de MatriculaServiceImpl demonstrando quatro anti-padrões (DIAG-01, 03, 04, 06) e DisciplinaServiceImpl com duplicação explícita de regras (DIAG-04).

## What Was Built

Três arquivos de serviço para o módulo `erp-matricula-camadas`:

1. **MatriculaService.java** — Interface com 4 métodos declarados (matricular, adicionarDisciplina, cancelar, buscarPorAluno). Convencional no padrão Controller→Service→Repository; contrasta com módulo DDD onde UseCases não têm interfaces explícitas.

2. **MatriculaServiceImpl.java** — 227 linhas demonstrando:
   - **DIAG-01 (Service Anêmico)**: Regras de negócio no Service — `aluno.isAtivo()`, verificação de período, verificação de status de matrícula — que deveriam estar encapsuladas nas entidades
   - **DIAG-03 (Service Deus)**: Classe com 200+ linhas, todos os métodos de matrícula concentrados, método `verificarElegibilidade()` private que foi criado para eliminar duplicação mas não é usado consistentemente
   - **DIAG-04 (Duplicação de Regras)**: Validação `aluno.isAtivo()` duplicada em `matricular()` e `adicionarDisciplina()`
   - **DIAG-06 (Acoplamento ao Banco)**: Regra "máximo 6 disciplinas" expressa como `itemMatriculaRepository.countByMatriculaId()` em vez de invariante no objeto

3. **DisciplinaServiceImpl.java** — 131 linhas demonstrando:
   - **DIAG-04 (Duplicação de Regras)**: Cópia exata da validação `aluno.isAtivo()` de MatriculaServiceImpl, com comentário narrando o cenário real de divergência em 6 meses
   - **DIAG-06**: countByMatriculaId duplicado também neste Service
   - Método `verificarStatusMatricula()` que foi criado para reutilização mas não é usado pelos outros Services

## Acceptance Criteria — Verificação

| Critério | Status |
|----------|--------|
| MatriculaServiceImpl.java >= 200 linhas (wc -l: 227) | PASS |
| Contém "ANTI-PADRAO: Service Anêmico (DIAG-01)" | PASS |
| Contém "ANTI-PADRAO: Service Deus (DIAG-03)" | PASS |
| Contém "ANTI-PADRAO: Duplicação de Regras (DIAG-04)" | PASS |
| Contém "ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)" | PASS |
| Contém itemMatriculaRepository.countByMatriculaId | PASS |
| Contém if (!aluno.isAtivo()) | PASS (3 ocorrências) |
| MatriculaService.java é interface | PASS |
| DisciplinaServiceImpl contém DIAG-04 | PASS (4 ocorrências) |
| DisciplinaServiceImpl contém if (!aluno.isAtivo()) | PASS |
| DisciplinaServiceImpl contém countByMatriculaId | PASS (2 ocorrências) |
| DisciplinaServiceImpl tem @Service e @Transactional | PASS |
| mvn -pl erp-matricula-camadas compile -q exit 0 | PASS |
| Três services existem (interface + 2 impls) | PASS |

## Tasks Completed

| Task | Commit | Files |
|------|--------|-------|
| Tarefa 1: MatriculaService + MatriculaServiceImpl | b7dc28e | MatriculaService.java, MatriculaServiceImpl.java |
| Tarefa 2: DisciplinaServiceImpl | 749a17f | DisciplinaServiceImpl.java |

## Deviations from Plan

None — plan executed exactly as written.

MatriculaServiceImpl atingiu 227 linhas (meta era 200+). O plano especificava um método `verificarElegibilidade()` private para demonstrar a fragilidade de extrações sem modelo rico — incluído conforme especificado.

## Key Decisions

1. **MatriculaServiceImpl 227 linhas**: Atingiu o alvo de 200+ com 8 marcadores ANTI-PADRAO para garantir leiturabilidade pedagógica — cada ponto de anti-padrão tem contexto completo.

2. **DisciplinaServiceImpl não implementa interface**: Classe standalone (sem interface própria) para demonstrar como Services proliferam sem disciplina arquitetural — não há `DisciplinaService` interface.

3. **Compilação requer JAVA_HOME=Java21**: O ambiente tem Java 17 como padrão do sistema. A compilação foi executada com `JAVA_HOME=/home/marcelino/.jdks/ms-21.0.10` para honrar o requisito Java 21 do projeto.

## Threat Flags

Nenhuma nova superfície de segurança introduzida. Os anti-padrões são intencionais e documentados no threat model do plano:

- RuntimeException com mensagem verbosa ("Aluno não encontrado: " + id) — aceito como ponto pedagógico (T-05-03)
- Sem novos pacotes além do parent POM já auditado (T-05-SC)

## Self-Check: PASSED

- [x] MatriculaService.java existe: `/erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaService.java`
- [x] MatriculaServiceImpl.java existe: `/erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java`
- [x] DisciplinaServiceImpl.java existe: `/erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java`
- [x] Commit b7dc28e existe no log
- [x] Commit 749a17f existe no log
- [x] mvn compile exit 0 confirmado
