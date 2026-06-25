---
quick_id: 260625-siy
slug: refatorar-ids-do-erp-matr-cula-ddd-usar-
description: "Refatorar IDs do ERP Matrícula DDD: usar UUID diretamente em vez de Value Objects para IDs; manter Value Objects apenas para conceitos de domínio com comportamento real"
date: 2026-06-25
must_haves:
  truths:
    - "AlunoId, MatriculaId, TurmaId removidos — UUID usado diretamente em todos os lugares"
    - "Cpf, PeriodoLetivo, NomeDisciplina mantidos — têm validação/comportamento real"
    - "Todos os testes existentes continuam passando"
  artifacts:
    - "dominio/vo/AlunoId.java DELETADO"
    - "dominio/vo/MatriculaId.java DELETADO"
    - "dominio/vo/TurmaId.java DELETADO"
    - "test/.../vo/AlunoIdTest.java DELETADO"
---

# Quick Task 260625-siy: Refatorar IDs — UUID direto

## Contexto

O projeto usa Value Objects tipados para IDs (AlunoId, MatriculaId, TurmaId) com o
argumento de segurança em tempo de compilação (ADR-003). A decisão pedagógica é
simplificar: UUID diretamente para IDs, Value Objects apenas para conceitos com
comportamento/regras de negócio reais.

VOs que ficam: Cpf (validação algoritmo), PeriodoLetivo (cálculo semestre/datas),
NomeDisciplina (max 100 chars, strip, non-blank).

## Tarefa 1 — Remover VO de IDs e refatorar domínio

**Arquivos a deletar:**
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/AlunoId.java`
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/MatriculaId.java`
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/TurmaId.java`
- `erp-matricula-ddd/src/test/java/br/com/escola/matricula/dominio/vo/AlunoIdTest.java`

**Arquivos a modificar (camada domínio):**
- `dominio/modelo/Matricula.java` — campos UUID, construtores, getters, factory
- `dominio/modelo/Aluno.java` — campo UUID id, construtor, getter
- `dominio/modelo/Turma.java` — campo UUID id, construtor, getter
- `dominio/evento/AlunoMatriculado.java` — UUID para matriculaId, alunoId, turmaId
- `dominio/evento/DisciplinaAdicionada.java` — UUID para matriculaId, alunoId
- `dominio/evento/MatriculaCancelada.java` — UUID para matriculaId, alunoId
- `dominio/excecao/AlunoInativoException.java` — UUID para alunoId
- `dominio/excecao/MatriculaDuplicadaException.java` — UUID para alunoId
- `dominio/excecao/MatriculaNaoEncontradaException.java` — UUID para matriculaId
- `dominio/excecao/MatriculaCanceladaException.java` — UUID para matriculaId
- `dominio/excecao/LimiteDisciplinasExcedidoException.java` — UUID para matriculaId
- `dominio/excecao/DisciplinaJaMatriculadaException.java` — UUID para matriculaId
- `dominio/repositorio/MatriculaRepositorio.java` — UUID nos parâmetros

## Tarefa 2 — Refatorar camada de aplicação e infraestrutura

**Aplicação:**
- `aplicacao/AdicionarDisciplinaCommand.java` — UUID para matriculaId
- `aplicacao/CancelarMatriculaCommand.java` — UUID para matriculaId
- `aplicacao/MatricularAlunoUseCase.java` — retorno UUID em vez de MatriculaId
- `aplicacao/MatriculaDto.java` — remover .valor() de getId() e getAlunoId()

**Infraestrutura:**
- `infraestrutura/persistencia/MatriculaRepositorioMyBatis.java` — remover .valor()
- `infraestrutura/persistencia/MatriculaRowMapper.java` — remover wrappers VO de ID
- `infraestrutura/config/DemoRunner.java` — UUID diretamente, sem wrappers

**Interfaces:**
- `interfaces/MatriculaController.java` — UUID diretamente, sem wrappers

## Tarefa 3 — Refatorar testes

- `test/dominio/modelo/MatriculaTest.java` — UUID diretamente
- `test/dominio/servico/MatriculaRepositorioEmMemoria.java` — UUID nos métodos
- `test/dominio/servico/VerificadorElegibilidadeMatriculaTest.java` — UUID diretamente
