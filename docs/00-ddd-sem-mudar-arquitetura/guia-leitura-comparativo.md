# Guia de Leitura: A Operação "Matricular" nos Dois Módulos

Este guia conduz você pela mesma operação — matricular um aluno — implementada de duas
formas: a arquitetura tradicional em camadas (`erp-matricula-camadas`) e a arquitetura
com DDD (`erp-matricula-ddd`). Você não precisa executar o código — apenas abrir os
arquivos e observar onde cada decisão de negócio foi parar.

## Como usar este guia

**Setup:** Abra o projeto na sua IDE ou editor com os dois módulos visíveis. O guia vai
instruir você a abrir arquivos lado a lado — `erp-matricula-camadas/` de um lado,
`erp-matricula-ddd/` do outro.

**Sequência:** Siga os passos em ordem. Cada passo mostra o que mudou, o que foi
encapsulado, e qual invariante passou a ser protegida.

---

## Passo 1 — O problema (camadas)

**Abra** `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java`,
método `matricular()`.

```java
// Arquivo: erp-matricula-camadas/.../service/MatriculaServiceImpl.java

// Regra de negócio no Service — não na entidade Aluno:
if (!aluno.isAtivo()) {
    throw new RuntimeException("Aluno inativo não pode ser matriculado");
}

// Validação de período no Service — não na entidade Turma:
if (periodoInicio == null || periodoInicio.isBlank()) {
    throw new RuntimeException("Período de início é obrigatório");
}

// Criação com setters — estado inicial definido externamente:
Matricula matricula = new Matricula();
matricula.setId(UUID.randomUUID());
matricula.setAlunoId(alunoId);
matricula.setStatus("ATIVA");  // String livre — qualquer valor seria aceito
```

**Observe:** a entidade `Aluno` tem `isAtivo()` — mas quem interpreta o boolean e decide
"pode matricular?" é o Service. A regra está no Service, não no objeto que a regra descreve.
Se outro método precisar verificar se o aluno está ativo, vai reescrever a mesma lógica.

**Compare:** `MatriculaServiceImpl.java` tem mais de 200 linhas porque o modelo não tem
comportamento — toda lógica migrou para cá.

---

## Passo 2 — Onde foi parar: elegibilidade (ddd)

**Abra** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java`,
método `verificar()`.

```java
// Arquivo: erp-matricula-ddd/.../dominio/servico/VerificadorElegibilidadeMatricula.java

public void verificar(Aluno aluno, Turma turma, PeriodoLetivo periodo) {
    if (!aluno.estaAtivo()) {
        throw new AlunoInativoException(aluno.getId());
    }

    if (!turma.periodoEstaAberto()) {
        throw new PeriodoFechadoException(periodo);
    }

    if (repositorio.existeMatriculaAtiva(aluno.getId(), periodo)) {
        throw new MatriculaDuplicadaException(aluno.getId(), periodo);
    }
}
```

**Compare:** a regra "aluno deve estar ativo" saiu do Service e foi para o Domain Service.
O UseCase não sabe como verificar elegibilidade — ele só chama `verificador.verificar(...)`.

**Observe:** `estaAtivo()` em vez de `isAtivo()`. Um é getter booleano; o outro expressa
semântica de negócio da secretaria. A mudança de nome não é cosmética — é Linguagem Ubíqua.

**O que foi ganho:** uma única responsabilidade no lugar certo. Qualquer UseCase que precise
verificar elegibilidade usa o mesmo Domain Service — sem duplicação.

---

## Passo 3 — Criação segura (ddd)

**Abra** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java`,
factory method `criar()` e o construtor privado.

```java
// Arquivo: erp-matricula-ddd/.../dominio/modelo/Matricula.java

public static Matricula criar(AlunoId alunoId, TurmaId turmaId, PeriodoLetivo periodoLetivo) {
    return new Matricula(alunoId, turmaId, periodoLetivo);
}

// Construtor privado — estado inicial garantido pelo próprio Aggregate:
private Matricula(AlunoId alunoId, TurmaId turmaId, PeriodoLetivo periodoLetivo) {
    this.id = new MatriculaId(UUID.randomUUID());
    this.status = new StatusMatricula.Ativa();  // sempre Ativa na criação — sem setter possível
    this.disciplinas = new ArrayList<>();
    this.eventos = new ArrayList<>();
    this.eventos.add(new AlunoMatriculado(this.id, this.alunoId, ...));
}
```

**Compare:** no módulo camadas, o status era definido externamente — `matricula.setStatus("ATIVA")`.
Aqui, o próprio Aggregate garante o estado inicial. O construtor é `private` — nenhum código
externo pode criar uma `Matricula` sem estado inicial correto.

**Qual invariante passou a ser protegida:** "matrícula recém-criada sempre começa como Ativa"
— protegida pelo construtor, não por convenção ou disciplina dos chamadores.

**Observe também:** o evento `AlunoMatriculado` é coletado automaticamente no construtor.
No módulo camadas não havia eventos — integrações eram acopladas diretamente ao Service.

---

## Passo 4 — O orquestrador (ddd)

**Abra** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java`,
método `executar()`.

```java
// Arquivo: erp-matricula-ddd/.../aplicacao/MatricularAlunoUseCase.java

public MatriculaId executar(MatricularAlunoCommand command) {
    // 1. Verificar elegibilidade — Domain Service decide, lança exceção se não pode
    verificador.verificar(command.aluno(), command.turma(), command.periodo());

    // 2. Criar Aggregate — domínio decide o estado inicial
    Matricula matricula = Matricula.criar(
            command.aluno().getId(), command.turma().getId(), command.periodo());

    // 3. Persistir ANTES de publicar eventos
    repositorio.salvar(matricula);

    // 4. Publicar eventos APÓS persistência
    matricula.coletarEventos().forEach(publicador::publishEvent);

    return matricula.getId();
}
```

**Compare os tamanhos:** `MatriculaServiceImpl.matricular()` tem ~35 linhas de lógica de
negócio. `MatricularAlunoUseCase.executar()` tem 4 linhas de orquestração.

**Responda:** quantas decisões de negócio você encontra no UseCase? Nenhuma — cada decisão
foi delegada para o Domain Service (`verificador`) ou para o Aggregate (`Matricula.criar()`).
O UseCase sabe a sequência, não as regras.

---

## Passo 5 — Exercício

**Abra** `erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/AdicionarDisciplinaUseCase.java`
e `CancelarMatriculaUseCase.java`.

O padrão se repete: buscar → delegar ao Aggregate → salvar → publicar.

**Observe:** as regras estão em `Matricula.adicionarDisciplina()` (três guards: estado,
limite, duplicidade) e em `Matricula.cancelar()` — não nos UseCases.

**Quais decisões de negócio você encontra nesses UseCases?** Nenhuma. Os UseCases
orquestram; o Aggregate decide.

---

## Resumo da transformação

| Responsabilidade | Antes — módulo camadas | Depois — módulo DDD |
|------------------|------------------------|----------------------|
| Verificar elegibilidade | `MatriculaServiceImpl` (~10 linhas inline) | `VerificadorElegibilidadeMatricula.verificar()` |
| Criar com estado inicial | `new Matricula()` + `setStatus("ATIVA")` externo | `Matricula.criar()` — estado garantido no construtor |
| Verificar limite de disciplinas | `itemMatriculaRepository.countByMatriculaId()` no Service | `this.disciplinas.size()` em `Matricula.adicionarDisciplina()` |
| Regras de adição | `MatriculaServiceImpl.adicionarDisciplina()` | `Matricula.adicionarDisciplina()` (3 guards) |
| Transição de estado (cancelar) | `matriculaRepository.updateStatus("CANCELADA")` no Service | `Matricula.cancelar()` — método de domínio |

Para aprofundar em cada conceito individualmente, consulte os documentos em
`docs/00-ddd-sem-mudar-arquitetura/`:

- [07-linguagem-ubiqua.md](07-linguagem-ubiqua.md) — nomes que refletem o domínio
- [08-entidades.md](08-entidades.md) — identidade e comportamento
- [09-value-objects.md](09-value-objects.md) — imutabilidade e validação
- [10-agregados.md](10-agregados.md) — invariantes protegidas
- [11-repositorios.md](11-repositorios.md) — interface de domínio vs. DAO

Para praticar a classificação de regras, veja
[exercicio-classificacao.md](exercicio-classificacao.md).
