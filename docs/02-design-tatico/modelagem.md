# Modelagem Visual — Matrícula Escolar

Este documento é o mapa visual do modelo de domínio documentado nos seis arquivos táticos anteriores: [value-objects.md](./value-objects.md), [entidades.md](./entidades.md), [agregados.md](./agregados.md), [domain-services.md](./domain-services.md), [domain-events.md](./domain-events.md) e [repositorios.md](./repositorios.md). Não é redundante — é uma perspectiva diferente do mesmo modelo. A prosa descreve o raciocínio; os diagramas mostram a estrutura e o fluxo de uma vez só.

Cada diagrama responde uma pergunta diferente sobre o mesmo domínio: o diagrama de classes mostra quais tipos existem e como se relacionam; o diagrama de agregados mostra os limites de consistência; os flowcharts mostram os caminhos de negócio com suas exceções; o sequence diagram mostra como os objetos colaboram em tempo de execução.

Use este documento como referência rápida depois de ter lido os seis arquivos táticos — e como ponto de entrada visual se ainda não os leu. Cada diagrama inclui links para o documento que detalha o que você está vendo.

---

## MOD-01: Diagrama de Classes

O diagrama de classes mostra todos os elementos do domínio de Matrícula e seus relacionamentos estáticos. Observe que `Matricula` referencia o aluno e a turma por `UUID` — não pelos objetos `Aluno` e `Turma` — implementando [ADR-003](../adrs/ADR-003-referencia-por-id.md). `StatusMatricula` é uma `sealed interface` com três implementações como `record` — ver [agregados.md](./agregados.md).

```mermaid
classDiagram
    %% Aggregate Root
    class Matricula {
        +UUID id
        +UUID alunoId
        +UUID turmaId
        +PeriodoLetivo periodoLetivo
        +StatusMatricula status
        +List~ItemMatricula~ disciplinas
        +criar(UUID, UUID, PeriodoLetivo) Matricula
        +adicionarDisciplina(NomeDisciplina)
        +cancelar()
        +coletarEventos() List
    }
    class ItemMatricula {
        +NomeDisciplina disciplina
    }
    class StatusMatricula {
        <<sealed interface>>
    }
    class Ativa {
        <<record>>
    }
    class Cancelada {
        +LocalDateTime canceladaEm
        <<record>>
    }
    class Concluida {
        +LocalDateTime concluidaEm
        <<record>>
    }
    %% Value Objects
    class PeriodoLetivo {
        +int ano
        +int semestre
        <<record>>
    }
    class NomeDisciplina {
        +String valor
        <<record>>
    }
    class Cpf {
        +String valor
        <<record>>
    }
    %% Entidades
    class Aluno {
        +UUID id
        +Cpf cpf
        +boolean ativo
        +estaAtivo() boolean
    }
    class Turma {
        +UUID id
        +int capacidadeMaxima
        +PeriodoLetivo periodo
    }
    %% Repository Interface
    class MatriculaRepositorio {
        <<interface>>
        +buscarPorId(UUID) Optional~Matricula~
        +buscarPorAluno(UUID) List~Matricula~
        +salvar(Matricula)
    }

    Matricula *-- "1..*" ItemMatricula : contém
    Matricula --> StatusMatricula : tem status
    StatusMatricula <|-- Ativa
    StatusMatricula <|-- Cancelada
    StatusMatricula <|-- Concluida
    Matricula --> PeriodoLetivo : ocorre em
    ItemMatricula --> NomeDisciplina : nomeia
    Aluno --> Cpf : tem
    MatriculaRepositorio ..> Matricula : persiste
```

> **O que observar:** `Matricula` armazena `alunoId` e `turmaId` como `UUID` — não como objetos `Aluno` e `Turma`. Esse é o padrão de referência por ID (ADR-003) visualizado. Domain Events não aparecem aqui — estão no sequence diagram.

---

## MOD-02: Diagrama de Agregados

O diagrama de agregados mostra os limites do Aggregate `Matricula` — o que está dentro (controlado pelo Aggregate Root) e o que é referenciado por ID fora do limite de consistência. Compare com o diagrama de classes: o de classes mostra tipos; este mostra fronteiras de consistência. Ver [agregados.md](./agregados.md) para o raciocínio de por que esses limites existem.

```mermaid
flowchart LR
    subgraph AGG["Aggregate: Matricula (limite de consistência)"]
        MAT["UUID id\nUUID alunoId\nUUID turmaId\nPeriodoLetivo\nStatusMatricula"]
        IT1["ItemMatricula\n(NomeDisciplina)"]
        IT2["ItemMatricula\n(NomeDisciplina)"]
        MAT --> IT1
        MAT --> IT2
    end

    ALUNO["Aluno\n(Aggregate separado)"]
    TURMA["Turma\n(Aggregate separado)"]
    REPO["MatriculaRepositorio\n(interface de domínio)"]

    AGG -->|"referência por UUID alunoId\n(ADR-003 — sem objeto Aluno)"| ALUNO
    AGG -->|"referência por UUID turmaId\n(ADR-003 — sem objeto Turma)"| TURMA
    REPO -->|"carrega / salva"| AGG
```

> **Limite de consistência:** Tudo dentro do `subgraph` é carregado e salvo atomicamente pelo `MatriculaRepositorio`. `Aluno` e `Turma` são Aggregates separados — cada um com seu próprio Repositório (que não é implementado no v1).

---

## MOD-03: Flowcharts de Negócio

Os três fluxos de negócio mostram o caminho feliz e os caminhos de erro para cada caso de uso. Os diamantes `{}` representam as invariantes do Aggregate verificadas em `adicionarDisciplina()`, `cancelar()` e `Matricula.criar()` — ver [agregados.md](./agregados.md). Cada exceção nomeada mapeia para uma exceção de domínio tipada.

### Fluxo 1: Realizar Matrícula

```mermaid
flowchart TD
    START([Secretaria inicia matrícula]) --> VER_ALUNO{Aluno está ativo?}
    VER_ALUNO -->|Não| ERR1[AlunoInativoException]
    VER_ALUNO -->|Sim| VER_PERIODO{Período letivo aberto?}
    VER_PERIODO -->|Não| ERR2[PeriodoFechadoException]
    VER_PERIODO -->|Sim| VER_DUP{Matrícula duplicada no período?}
    VER_DUP -->|Sim| ERR3[MatriculaDuplicadaException]
    VER_DUP -->|Não| CRIA[Matricula.criar]
    CRIA --> SALVA[MatriculaRepositorio.salvar]
    SALVA --> EVENTO[Publica AlunoMatriculado]
    EVENTO --> FIN([Matrícula realizada])
    ERR1 --> FAIL([Falha — 422])
    ERR2 --> FAIL
    ERR3 --> FAIL
```

### Fluxo 2: Adicionar Disciplina

```mermaid
flowchart TD
    START([Aluno adiciona disciplina]) --> BUSCA[Busca Matricula por id]
    BUSCA --> VER_CANCEL{Matrícula cancelada?}
    VER_CANCEL -->|Sim| ERR1[MatriculaCanceladaException]
    VER_CANCEL -->|Não| VER_DUP{Disciplina já incluída?}
    VER_DUP -->|Sim| ERR2[DisciplinaJaMatriculadaException]
    VER_DUP -->|Não| VER_LIMITE{Atingiu limite de disciplinas?}
    VER_LIMITE -->|Sim| ERR3[LimiteDisciplinasExcedidoException]
    VER_LIMITE -->|Não| ADD[Matricula.adicionarDisciplina]
    ADD --> SALVA[MatriculaRepositorio.salvar]
    SALVA --> EVENTO[Publica DisciplinaAdicionada]
    EVENTO --> FIN([Disciplina adicionada])
    ERR1 --> FAIL([Falha — 422])
    ERR2 --> FAIL
    ERR3 --> FAIL
```

### Fluxo 3: Cancelar Matrícula

```mermaid
flowchart TD
    START([Secretaria cancela matrícula]) --> BUSCA[Busca Matricula por id]
    BUSCA --> VER_EXISTS{Matrícula existe?}
    VER_EXISTS -->|Não| ERR1[MatriculaNaoEncontradaException]
    VER_EXISTS -->|Sim| VER_CANCEL{Já está cancelada?}
    VER_CANCEL -->|Sim| ERR2[MatriculaJaCanceladaException]
    VER_CANCEL -->|Não| CANCEL[Matricula.cancelar]
    CANCEL --> SALVA[MatriculaRepositorio.salvar]
    SALVA --> EVENTO[Publica MatriculaCancelada]
    EVENTO --> FIN([Matrícula cancelada])
    ERR1 --> FAIL([Falha — 404 / 422])
    ERR2 --> FAIL
```

> **Nota:** Os nomes de exceção nos fluxos (`AlunoInativoException`, `LimiteDisciplinasExcedidoException`, etc.) correspondem às exceções tipadas documentadas em [agregados.md](./agregados.md). Em HTTP, elas resultam em respostas 422 (violação de invariante) ou 409 (conflito) — mapeamento detalhado na Fase 4.

---

## MOD-04: Sequence Diagram — Realizar Matrícula

O sequence diagram mostra o fluxo completo de "Realizar Matrícula" do ponto de vista técnico: como os objetos colaboram, em que ordem, e onde cada regra de negócio é verificada. A passagem pelo `VerificadorElegibilidadeMatricula` antes de `Matricula.criar()` implementa a separação Domain Service (regra cross-entidade) / Aggregate (invariantes internas) documentada em [domain-services.md](./domain-services.md).

```mermaid
sequenceDiagram
    actor Sec as Secretaria
    participant HTTP as HTTP Request
    participant Ctrl as MatriculaController
    participant UC as MatricularAlunoUseCase
    participant Verif as VerificadorElegibilidade
    participant Agg as Matricula (Aggregate)
    participant Repo as MatriculaRepositorio
    participant Pub as EventPublisher

    Sec->>HTTP: POST /matriculas {alunoId, turmaId, periodo}
    HTTP->>Ctrl: matricular(command)
    activate Ctrl

    Ctrl->>UC: executar(alunoId, turmaId, periodo)
    activate UC

    UC->>Repo: buscarAluno(alunoId)
    Repo-->>UC: Aluno

    UC->>Verif: verificar(aluno, periodo)
    activate Verif
    alt Aluno inativo
        Verif-->>UC: AlunoInativoException
    else Período fechado
        Verif-->>UC: PeriodoFechadoException
    else Matrícula duplicada
        Verif-->>UC: MatriculaDuplicadaException
    else Elegível
        Verif-->>UC: OK
    end
    deactivate Verif

    UC->>Agg: Matricula.criar(alunoId, turmaId, periodo)
    activate Agg
    Note over Agg: Cria matrícula e coleta<br/>AlunoMatriculado internamente
    Agg-->>UC: Matricula (com evento coletado)
    deactivate Agg

    UC->>Repo: salvar(matricula)
    Repo-->>UC: OK

    UC->>Pub: publicar(matricula.coletarEventos())
    Note over Pub: Publica AlunoMatriculado<br/>para BC Financeiro e Acadêmico

    UC-->>Ctrl: MatriculaCriadaDto
    deactivate UC

    Ctrl-->>HTTP: 201 Created {matriculaId}
    deactivate Ctrl
    HTTP-->>Sec: Matrícula realizada
```

> **O fluxo completo:** Este sequence diagram é o success criteria da fase — "um desenvolvedor acompanha o fluxo do início ao fim e consegue descrever cada passo". Use-o como referência ao ler o código da Fase 3.
