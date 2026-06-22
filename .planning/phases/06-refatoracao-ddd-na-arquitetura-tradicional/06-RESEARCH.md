# Phase 06: Refatoracao DDD na Arquitetura Tradicional — Research

**Researched:** 2026-06-22
**Domain:** Documentação pedagógica DDD — anotações de código + Markdown comparativos
**Confidence:** HIGH

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Estrutura do Módulo "Depois"**
- D-01: Usar `erp-matricula-ddd` como está — não criar módulo novo nem `erp-matricula-refatorado`. O módulo já tem Aggregate Matricula com invariantes, Value Objects como records Java 21, Application Services, e Repository. É o "depois" definitivo.
- D-02: Adicionar comentários `REFD` somente nos 3 pivots principais — não em todo o código:
  - `erp-matricula-ddd/.../dominio/modelo/Matricula.java` — invariantes protegidas pelo Aggregate
  - `erp-matricula-ddd/.../aplicacao/MatricularAlunoUseCase.java` — Application Service orquestrador vs. Service anêmico
  - `erp-matricula-ddd/.../dominio/repositorio/MatriculaRepositorio.java` — Repository vs. DAO genérico
- D-03: O restante do código DDD fica limpo — toda a orientação para os demais arquivos fica nos Markdowns, não no código.

**Comparativo Lado a Lado**
- D-04: Formato de guia de leitura com trails explícitos — um Markdown que instrui o estudante a abrir arquivos específicos e observar linhas específicas.
- D-05: Trail profundo para uma transformação completa: operação "matricular" (cobre REFD-01, REFD-02, DDD-02, DDD-04). As operações "adicionar disciplina" e "cancelar" são referenciadas como exercício.
- D-06: O guia de leitura deve mostrar explicitamente: o que mudou, o que foi encapsulado, qual invariante passou a ser protegida.

**Granularidade dos Conceitos DDD**
- D-07: Um arquivo Markdown por conceito DDD, criados em `docs/00-ddd-sem-mudar-arquitetura/`:
  - `07-linguagem-ubiqua.md` — DDD-01
  - `08-entidades.md` — DDD-02
  - `09-value-objects.md` — DDD-03
  - `10-agregados.md` — DDD-04
  - `11-repositorios.md` — DDD-05
- D-08: Cada arquivo segue o padrão estabelecido em `docs/02-design-tatico/`: definição simples (sem jargão excessivo), snippet ANTES (erp-matricula-camadas), snippet DEPOIS (erp-matricula-ddd), e o que foi ganho com a mudança.
- D-09: Arquivos novos — não enriquecer os existentes em `docs/02-design-tatico/`. Os docs da Phase 2 são teoria geral DDD; os novos são aplicação específica "sem mudar a arquitetura".

**Exercício de Classificação REFD-03**
- D-10: Arquivo dedicado `exercicio-classificacao.md` em `docs/00-ddd-sem-mudar-arquitetura/`. Não integrado ao guia de leitura.
- D-11: Formato: lista de 8-10 regras do domínio de matrícula. O estudante classifica mentalmente como "Domínio" ou "Aplicação", depois expande o `<details>` HTML para ver a resposta com justificativa.
- D-12: Regras a incluir devem cobrir casos claros e casos ambíguos para forçar reflexão.

### Claude's Discretion
- Número exato de linhas de código nos snippets (mínimo suficiente para ilustrar o contraste)
- Nomenclatura dos arquivos numerados (07 a 11) — sequência após os 6 existentes em `docs/00-ddd-sem-mudar-arquitetura/`
- Seleção das 8-10 regras para o exercício de classificação (baseando-se nas regras reais de `Matricula.java` e `MatricularAlunoUseCase.java`)

### Deferred Ideas (OUT OF SCOPE)
- Corrigir trail DIAG-06 quebrado (doc `06-acoplamento-banco.md` vs código real) — identificado em 05-VERIFICATION.md como blocker pedagógico. A Phase 6 NÃO corrige isso; os docs comparativos simplesmente referenciam `countByMatriculaId` que é o método real.
- DisciplinaServiceImpl sem endpoint HTTP — DID-04 demonstrável só por leitura.
- Numeração dos docs (07-11): se o usuário quiser reorganizar os docs existentes 01-06, isso é uma refatoração separada fora do escopo desta fase.
</user_constraints>

---

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| REFD-01 | Desenvolvedor visualiza diferença entre Service anêmico (contém regras) e Application Service orquestrador (delega ao domínio) na mesma arquitetura Controller→Service→Repository | Trail de leitura + comentários REFD em MatricularAlunoUseCase.java |
| REFD-02 | Desenvolvedor visualiza entidade rica com comportamento (`pedido.finalizar()`) vs. anêmica (`pedido.setStatus(FECHADO)`) e compreende o que foi encapsulado e qual regra passou a ser protegida | Trail de leitura + comentários REFD em Matricula.java + doc 08-entidades.md |
| REFD-03 | Desenvolvedor classifica corretamente regras como "de Domínio" ou "de Aplicação" | Arquivo exercicio-classificacao.md com 8-10 regras + gabarito em `<details>` |
| DDD-01 | Projeto demonstra Linguagem Ubíqua — nomes de classes e métodos em código Java refletem termos dos especialistas do domínio | Doc 07-linguagem-ubiqua.md com antes/depois de nomenclatura |
| DDD-02 | Projeto demonstra identidade e ciclo de vida de Entidades com exemplos concretos | Doc 08-entidades.md com Aluno.java (classe com ciclo de vida) vs modelo anêmico |
| DDD-03 | Projeto demonstra Value Objects imutáveis como alternativa a tipos primitivos | Doc 09-value-objects.md com Cpf.java, NomeDisciplina.java, PeriodoLetivo.java vs String primitivo |
| DDD-04 | Projeto demonstra Agregados como limites de consistência e proteção de invariantes | Doc 10-agregados.md com Matricula.adicionarDisciplina() vs lógica no Service |
| DDD-05 | Projeto demonstra Repositórios como recuperadores de Agregados, diferenciando do DAO genérico | Doc 11-repositorios.md com MatriculaRepositorio (interface no domínio) vs MatriculaRepository (mapper MyBatis) |
| DID-02 | Módulo apresenta código Java completo "depois" — DDD aplicado na mesma arquitetura com comparativo explícito | Todos os 5 docs de conceitos + guia de leitura comparativo + atualização de 00-introducao.md |
</phase_requirements>

---

## Summary

Esta fase é **puramente de documentação** — não cria novo código Java, não instala pacotes, não altera a lógica do `erp-matricula-ddd`. O objetivo é fazer o código existente falar: adicionar comentários REFD nos 3 pivots do módulo DDD e produzir documentos Markdown que guiam o estudante pela transformação DDD.

O `erp-matricula-ddd` já existe completo e funciona como o "depois" definitivo. O `erp-matricula-camadas` está finalizado como o "antes". Phase 6 cria a ponte pedagógica entre os dois: trails de leitura que apontam para linhas específicas, documentos que mostram o contraste antes/depois para cada conceito DDD, e um exercício de classificação interativo.

O risco principal é **incoerência de referências**: o docs da Phase 5 já têm um trail quebrado (DIAG-06 aponta para `countDisciplinas` quando o service chama `countByMatriculaId`). Os novos docs DEVEM referenciar apenas o que existe no código real — verificado linha a linha nesta pesquisa.

**Primary recommendation:** Produzir 8 artefatos na ordem: (1) comentários REFD nos 3 arquivos Java, (2) guia de leitura comparativo, (3) 5 docs de conceitos DDD, (4) exercício de classificação, (5) atualização de `00-introducao.md`.

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Comentários REFD no código Java | Código existente (domínio) | — | Modificação cirúrgica em 3 arquivos Java; sem alteração de lógica |
| Guia de leitura comparativo | Documentação (Markdown) | — | Artefato de navegação que referencia arquivos dos dois módulos |
| Docs de conceitos DDD (07-11) | Documentação (Markdown) | — | Novos arquivos em `docs/00-ddd-sem-mudar-arquitetura/` |
| Exercício de classificação | Documentação (Markdown) | — | HTML `<details>` dentro de Markdown para interatividade simples |
| Atualização de introducao.md | Documentação (Markdown) | — | Tabela de navegação recebe links para os novos docs |

---

## Standard Stack

Esta fase não instala pacotes. O stack é o que já existe no projeto.

### Ferramentas Existentes (já no projeto)
| Ferramenta | Versão | Propósito |
|------------|--------|-----------|
| Java 21 | 21 LTS | Linguagem dos snippets ANTES e DEPOIS |
| Spring Boot | 3.5.3 | Framework do código existente nos dois módulos |
| MyBatis | 3.5.19 | Persistência nos dois módulos |
| Markdown | — | Formato de toda a documentação |
| HTML `<details>` | HTML5 | Gabarito ocultável no exercício de classificação |

### Package Legitimacy Audit

> Não aplicável. Esta fase não instala pacotes externos.

---

## Architecture Patterns

### Padrão Estabelecido: Formato ANTES/DEPOIS dos Docs

O padrão canônico vem de `docs/02-design-tatico/` (e já replicado em `docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md`). Cada doc de conceito DDD DEVE seguir esta estrutura:

```markdown
# [Conceito] — Aplicado na Arquitetura Tradicional

## O que é
[Definição em 2-3 parágrafos sem jargão acadêmico]

---

## Manifestação no módulo camadas (ANTES)
[snippet Java com comentário // ANTES: e referência ao arquivo completo]

---

## Aplicação no módulo DDD (DEPOIS)
[snippet Java com comentário // DEPOIS: e referência ao arquivo completo]

---

## O que foi ganho
[Benefício concreto: menos duplicação, invariante protegida, melhor testabilidade, etc.]
```

**Por que este padrão funciona:** o estudante não precisa navegar entre dois projetos para ver o contraste — o doc coloca os dois fragmentos lado a lado e nomeia exatamente o que mudou.

### Padrão Estabelecido: Comentários REFD no Código

Da Phase 5, o padrão de comentários pedagógicos é:
```java
// ANTI-PADRAO: Service Anêmico (DIAG-01)
```

Para Phase 6 o padrão equivalente é:
```java
// REFD-01: [explicação do que mudou e por que]
```

**Regra de uso:** comentários REFD são adicionados em blocos `// REFD-XX:` logo acima ou ao lado do trecho que ilustra o ponto pedagógico. Não alterar lógica, apenas adicionar texto explicativo.

### Padrão Estabelecido: Trail de Leitura com Instruções Explícitas

Do CONTEXT.md D-04, o formato do guia de leitura é instrucional — não analítico:

```markdown
**Passo 2:** Abra `MatriculaServiceImpl.java` linha 75 (camadas) e `MatricularAlunoUseCase.java` linha 91 (ddd) lado a lado.
Observe: a verificação `if (!aluno.isAtivo())` sumiu do UseCase DDD. Onde foi parar?
```

O trail guia o movimento do olho do estudante, não explica em abstrato.

### Padrão HTML `<details>` para Gabarito

```html
<details>
<summary>Ver resposta</summary>

**Classificação: Domínio**

**Justificativa:** [explicação do critério de decisão]
</details>
```

Este padrão funciona em GitHub Markdown e na maioria dos visualizadores Markdown modernos. [ASSUMED]

---

## Arquivos a Criar / Modificar

### Arquivos Novos (criar)

| Arquivo | Requirement | Conteúdo Principal |
|---------|-------------|-------------------|
| `docs/00-ddd-sem-mudar-arquitetura/guia-leitura-comparativo.md` | REFD-01, REFD-02, DDD-02, DDD-04 | Trail profundo da operação "matricular" |
| `docs/00-ddd-sem-mudar-arquitetura/07-linguagem-ubiqua.md` | DDD-01 | Antes/depois de nomenclatura de classes e métodos |
| `docs/00-ddd-sem-mudar-arquitetura/08-entidades.md` | DDD-02 | Antes/depois de Aluno anêmico vs Aluno com ciclo de vida |
| `docs/00-ddd-sem-mudar-arquitetura/09-value-objects.md` | DDD-03 | Antes/depois de String vs records imutáveis (Cpf, NomeDisciplina, PeriodoLetivo) |
| `docs/00-ddd-sem-mudar-arquitetura/10-agregados.md` | DDD-04 | Antes/depois de regras no Service vs invariantes no Aggregate |
| `docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md` | DDD-05 | Antes/depois de MatriculaRepository (mapper) vs MatriculaRepositorio (interface domínio) |
| `docs/00-ddd-sem-mudar-arquitetura/exercicio-classificacao.md` | REFD-03 | 8-10 regras com gabarito `<details>` |

### Arquivos a Modificar (adicionar comentários REFD — sem alterar lógica)

| Arquivo | Requirement | Comentários a Adicionar |
|---------|-------------|------------------------|
| `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java` | REFD-02, DDD-04 | Comentários REFD nos guards de `adicionarDisciplina()` e no factory `criar()` |
| `erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java` | REFD-01 | Comentários REFD na sequência de orquestração (verificar → criar → salvar → publicar) |
| `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java` | DDD-05 | Comentários REFD na interface mostrando que ela vive no domínio, sem import de framework |

### Arquivos a Modificar (atualizar links)

| Arquivo | Modificação |
|---------|------------|
| `docs/00-ddd-sem-mudar-arquitetura/00-introducao.md` | Adicionar linhas na tabela de navegação para os 7 novos arquivos |

---

## Don't Hand-Roll

| Problema | Não Construir | Usar em vez | Por que |
|----------|--------------|-------------|---------|
| Gabarito ocultável | JavaScript/CSS customizado | HTML `<details>` nativo | Funciona em GitHub Markdown sem dependência externa |
| Numeração de arquivos | Sistema de numeração automático | Prefixo manual 07-11 | A sequência já está estabelecida no projeto (00-06 existem); numeração manual é simples e consistente |
| Cross-referência entre docs | Índice central gerado | Links relativos inline | O padrão existente em `00-introducao.md` já usa tabela manual com links relativos |

---

## Inventory do Código "Depois" — Estado Real Verificado

Esta seção mapeia o que existe EXATAMENTE no `erp-matricula-ddd` para que os snippets nos docs não referenciem código que não existe.

### Aggregate Matricula (`Matricula.java`) — Pivots REFD Verificados

| Método | Linha Aprox. | Ponto Pedagógico |
|--------|-------------|-----------------|
| `criar(alunoId, turmaId, periodoLetivo)` | 156 | Factory method — estado inicial ATIVA garantido, evento coletado automaticamente |
| `adicionarDisciplina(NomeDisciplina)` | 183 | Guards em ordem: Cancelada → limite → duplicidade. Tudo no mesmo método, sem janela de concorrência |
| `cancelar()` | 215 | Transição de estado terminal — sem setter público |
| `coletarEventos()` | 245 | Domínio coleta eventos; UseCase publica APÓS persistência |
| `getDisciplinas()` | 286 | Retorna `List.copyOf()` — sem setter, sem acesso direto à lista interna |

**Invariantes protegidas pelo Aggregate (verificadas no código):**
1. Limite de disciplinas: `LIMITE_DISCIPLINAS = 6` (constante nomeada, linha ~50)
2. Sem duplicidade: `this.disciplinas.stream().anyMatch(item -> item.disciplina().equals(disciplina))`
3. Estado terminal: `if (this.status instanceof StatusMatricula.Cancelada)`

**Zero dependências de framework:** `Matricula.java` não importa `org.springframework.*`, `jakarta.*`, `org.mybatis.*` — verificado pelo grep implícito ao ler o arquivo. [VERIFIED: codebase]

### MatricularAlunoUseCase (`MatricularAlunoUseCase.java`) — Pivots REFD Verificados

Sequência de 4 passos no método `executar()` (linhas 89-108):
1. `verificador.verificar(...)` — Domain Service decide elegibilidade
2. `Matricula.criar(...)` — Aggregate decide estado inicial
3. `repositorio.salvar(matricula)` — persistência ANTES de publicar
4. `matricula.coletarEventos().forEach(publicador::publishEvent)` — eventos APÓS commit

O Javadoc existente já documenta "O que faz" e "O que NÃO faz" — os comentários REFD devem complementar, não duplicar o Javadoc. [VERIFIED: codebase]

### MatriculaRepositorio (interface no domínio) — Pivots REFD Verificados

| Método | Propósito DDD |
|--------|--------------|
| `buscarPorId(MatriculaId)` | Retorna `Optional<Matricula>` — tipo do domínio, não DTO |
| `buscarPorAluno(AlunoId)` | Sem parâmetro `UUID` cru — usa `AlunoId` (Value Object) |
| `existeMatriculaAtiva(AlunoId, PeriodoLetivo)` | Semântica de negócio, não `countWhere` |
| `salvar(Matricula)` | Persiste o Aggregate inteiro — não rows separadas |

**O que NÃO tem na interface:** sem `extends JpaRepository`, sem `import org.springframework.data`, sem `import org.apache.ibatis`. Interface definida em termos do domínio. [VERIFIED: codebase]

### Value Objects Disponíveis para Snippets

| VO | Arquivo | Validação Principal |
|----|---------|---------------------|
| `Cpf` | `dominio/vo/Cpf.java` | Algoritmo módulo 11, remove máscara, rejeita dígitos repetidos |
| `PeriodoLetivo` | `dominio/vo/PeriodoLetivo.java` | `ano >= 2000`, `semestre in {1,2}` |
| `NomeDisciplina` | `dominio/vo/NomeDisciplina.java` | não nulo, não branco, strip(), máx 100 chars |
| `MatriculaId` | `dominio/vo/MatriculaId.java` | `requireNonNull(valor)` |
| `AlunoId` | `dominio/vo/AlunoId.java` | `requireNonNull(valor)` |
| `TurmaId` | `dominio/vo/TurmaId.java` | `requireNonNull(valor)` |

### Entidade Aluno — Para DDD-02

`Aluno.java` demonstra o contraste Entidade vs. Value Object:
- Tem identidade (`AlunoId id` — final, nunca muda)
- Tem estado mutável (`boolean ativo` — pode mudar)
- `estaAtivo()` encapsula semântica de negócio (vs. `isAtivo()` getter booleano no módulo camadas)
- `desativar()` método comportamental — sem setter `setAtivo(false)` público
- `equals/hashCode` baseados APENAS em `AlunoId` — dois alunos são o mesmo se têm o mesmo ID

### Código "Antes" — Contrastes para Snippets

**Entidade Anêmica (`erp-matricula-camadas/model/Matricula.java`):**
- `String status` — sem tipo seguro (DIAG-06 também)
- Sem `adicionarDisciplina()`, sem `cancelar()` — só getters/setters
- 75 linhas, zero comportamento

**Service Anêmico (`MatriculaServiceImpl.java`, verificado linha a linha):**
- Linha 75: `if (!aluno.isAtivo())` — regra de negócio no Service
- Linha 125: `if (!"ATIVA".equals(matricula.getStatus()))` — verificação de estado no Service
- Linha 149: `int qtd = matriculaRepository.countDisciplinas(matriculaId)` — regra via query SQL (NOTA: a doc da Phase 5 aponta para este método como `countDisciplinas` em `matriculaRepository`, que é dead code; o service realmente chama `itemMatriculaRepository.countByMatriculaId()`)
- Linha 179: `if (!"ATIVA".equals(matricula.getStatus()))` — mesmo check duplicado em `cancelar()`

**ATENÇÃO PARA O PLANNER:** o DIAG-06 trail está quebrado (ver deferred items acima e 05-VERIFICATION.md). Os docs da Phase 6 DEVEM referenciar `itemMatriculaRepository.countByMatriculaId(matriculaId)` (linha 149) quando mostrar o antes do "acoplamento ao banco" — não `matriculaRepository.countDisciplinas()`. [VERIFIED: codebase]

---

## Common Pitfalls

### Pitfall 1: Referenciar Linhas de Código que Não Existem
**O que dá errado:** Trail de leitura instrui o estudante a ir para "linha 83" de `MatriculaServiceImpl.java`, mas o código mudou após a escrita do doc.
**Por que acontece:** A Phase 5 já sofreu isso com DIAG-06 — doc aponta para `countDisciplinas` que é dead code.
**Como evitar:** Nos trails, referenciar por **método** primeiro, linha depois. Exemplo: "Abra `MatriculaServiceImpl.adicionarDisciplina()`, busque a chamada a `countByMatriculaId`". Linhas são frágeis; nomes de método são rastreáveis.
**Sinais de alerta:** qualquer trail que só usa números de linha sem identificar o método/bloco.

### Pitfall 2: Duplicar Conteúdo dos Docs da Phase 2
**O que dá errado:** Os 5 docs novos (07-11) replicam as explicações teóricas de `docs/02-design-tatico/` em vez de focar na aplicação prática "sem mudar a arquitetura".
**Por que acontece:** É fácil copiar a definição de Value Object de `value-objects.md` e colar no `09-value-objects.md`.
**Como evitar:** Os docs novos devem ser mais curtos e mais específicos que os da Phase 2. Apontar para os docs da Phase 2 para teoria; os novos devem mostrar apenas o snippet ANTES/DEPOIS e o que foi ganho.
**Sinal de alerta:** se um doc novo tem mais de 150 linhas, provavelmente está duplicando teoria.

### Pitfall 3: Comentários REFD Que Alteram a Semântica do Javadoc Existente
**O que dá errado:** Adicionar comentário REFD em `MatricularAlunoUseCase.java` que contradiz ou duplica o Javadoc existente na classe.
**Por que acontece:** `MatricularAlunoUseCase.java` já tem Javadoc pedagógico extenso. Um comentário REFD colocado na mesma posição cria ruído.
**Como evitar:** Comentários REFD devem ser adicionados em pontos que o Javadoc NOT COVER — tipicamente inline dentro do método `executar()`, não na documentação da classe. O Javadoc da classe já diz "O que faz / Não faz"; o REFD inline aponta para o contraste específico com o módulo camadas.
**Sinal de alerta:** se o comentário REFD diz o mesmo que o Javadoc acima dele, ele é desnecessário.

### Pitfall 4: Exercício com Regras Ambíguas Sem Critério de Decisão
**O que dá errado:** O gabarito no `<details>` diz "Domínio" ou "Aplicação" mas não explica o critério de decisão — o estudante memoriza a resposta mas não aprende a raciocinar.
**Por que acontece:** É mais fácil dar a resposta do que explicar o critério.
**Como evitar:** Cada `<details>` deve incluir o critério: "Classificado como Domínio porque esta regra seria verdadeira independentemente de qual banco de dados, framework ou canal de entrega o sistema use."
**Sinal de alerta:** gabarito com apenas uma palavra ("Domínio") sem justificativa.

### Pitfall 5: Snippets Muito Longos que Perdem o Ponto
**O que dá errado:** O snippet ANTES inclui 60 linhas de `MatriculaServiceImpl.java` quando o ponto pedagógico está em 5 linhas.
**Por que acontece:** Cópia do arquivo inteiro é mais fácil que selecionar o fragmento relevante.
**Como evitar:** Cada snippet deve incluir apenas o trecho que demonstra o contraste. Máximo ~20 linhas por snippet. Adicionar comentário `// ... outros métodos omitidos` se for necessário mostrar contexto estrutural.
**Sinal de alerta:** snippet que não cabe em uma tela (>30 linhas).

---

## Mapeamento de Conceitos DDD para Código Real

Esta seção mapeia cada conceito DDD que precisa ser documentado ao código específico que o demonstra, para garantir que cada doc tenha snippets reais e verificados.

### DDD-01: Linguagem Ubíqua

**ANTES (camadas):** Nomenclatura técnica desconectada do domínio
- `MatriculaServiceImpl.matricular()` — "impl" e "service" são termos técnicos, não de domínio
- `AlunoRepository.findById()` — "find" é termo de banco, não de negócio
- `String status` com valores `"ATIVA"`, `"CANCELADA"` — strings mágicas sem tipo

**DEPOIS (ddd):** Linguagem da Secretaria
- `MatricularAlunoUseCase.executar()` — "matricular aluno" é como a secretaria nomeia a operação
- `MatriculaRepositorio.buscarPorAluno()` — "buscar" em português do domínio, não "findBy"
- `StatusMatricula.Ativa`, `StatusMatricula.Cancelada` — tipos selados, não strings
- `Aluno.estaAtivo()` — semântica de negócio, não getter booleano
- `Matricula.cancelar()` — verbo de domínio, não `setStatus("CANCELADA")`
- Pacotes em português: `dominio/`, `aplicacao/`, `infraestrutura/` — [VERIFIED: codebase]

### DDD-02: Entidades

**ANTES (camadas):** `erp-matricula-camadas/model/Matricula.java`
- 75 linhas, apenas getters/setters
- Sem `equals/hashCode` explícito
- `setStatus(String)` aceita qualquer String

**DEPOIS (ddd):** `erp-matricula-ddd/dominio/modelo/Aluno.java`
- `equals/hashCode` baseado em `AlunoId` (não em atributos)
- `estaAtivo()` com semântica de negócio
- `desativar()` método comportamental
- `id` final — identidade imutável

**Contrast pedagógico:** Dois alunos com o mesmo nome são pessoas diferentes (identidade por ID). Dois `PeriodoLetivo(2026,1)` são o mesmo período (Value Object, identidade por valor).

### DDD-03: Value Objects

**ANTES (camadas):** Primitivos e Strings
- `String periodoInicio`, `String periodoFim` — sem validação
- `UUID alunoId` — UUID sem semântica de domínio
- `String status` — qualquer valor é aceito

**DEPOIS (ddd):** Records imutáveis com validação no construtor
- `Cpf` — remove máscara, valida 11 dígitos, algoritmo módulo 11
- `PeriodoLetivo(int ano, int semestre)` — `ano >= 2000`, `semestre in {1,2}`
- `NomeDisciplina` — strip(), não branco, máx 100 chars
- `AlunoId(UUID valor)` — `requireNonNull(valor)`

**Ponto pedagógico central:** um `Cpf` inválido é impossível de criar — o construtor lança exceção antes que o objeto exista. Com `String cpf`, um CPF inválido passa pela compilação sem reclamar.

### DDD-04: Agregados

**ANTES (camadas):** `MatriculaServiceImpl.adicionarDisciplina()`
- Linha 149: `int qtd = itemMatriculaRepository.countByMatriculaId(matriculaId)`
- Linha 150: `if (qtd >= 6)` — regra em query SQL + if no Service
- Linha 125: `if (!"ATIVA".equals(matricula.getStatus()))` — verificação de estado no Service

**DEPOIS (ddd):** `Matricula.adicionarDisciplina()`
- Guard 1: `if (this.status instanceof StatusMatricula.Cancelada)` — dentro do método
- Guard 2: `if (this.disciplinas.size() >= LIMITE_DISCIPLINAS)` — em memória, sem query
- Guard 3: stream anyMatch para duplicidade — em memória, sem query

**Invariante demonstrável:** "No módulo camadas, dois usuários simultâneos podem cada um ver 5 disciplinas e adicionar — resultado: 7 disciplinas. No módulo DDD, o Aggregate carregado é a fonte de verdade; a verificação e a adição ocorrem no mesmo objeto, na mesma transação."

### DDD-05: Repositórios

**ANTES (camadas):** `MatriculaRepository` (interface MyBatis mapper)
- `@Mapper` — anotação de infraestrutura na interface
- `findById(UUID)` — retorna `Optional<Matricula>` mas com UUID cru
- `countDisciplinas(UUID)` — método que expõe consulta de infra (dead code por sinal)
- Não tem semântica de negócio — é um DAO

**DEPOIS (ddd):** `MatriculaRepositorio` (interface no domínio)
- Zero imports de framework (sem `@Mapper`, sem `import org.apache.ibatis`)
- `buscarPorId(MatriculaId)` — recebe e retorna tipos do domínio
- `existeMatriculaAtiva(AlunoId, PeriodoLetivo)` — semântica de negócio, não SQL
- `salvar(Matricula)` — persiste o Aggregate inteiro, não rows separadas
- Interface declarada no domínio; implementação (`MatriculaRepositorioMyBatis`) na infraestrutura — Dependency Inversion Principle visível pelo pacote

---

## Regras para o Exercício de Classificação (REFD-03)

Com base no código real de `Matricula.java`, `MatricularAlunoUseCase.java`, e `VerificadorElegibilidadeMatricula.java`, estas são as 10 regras candidatas classificadas por dificuldade:

| # | Regra | Classificação | Dificuldade |
|---|-------|--------------|-------------|
| 1 | "Matrícula cancelada não aceita novas disciplinas" | Domínio | CLARA |
| 2 | "Máximo 6 disciplinas por matrícula" | Domínio | CLARA |
| 3 | "A mesma disciplina não pode aparecer duas vezes na mesma matrícula" | Domínio | CLARA |
| 4 | "Aluno inativo não pode ser matriculado" | Domínio | CLARA |
| 5 | "Abrir transação para garantir atomicidade" | Aplicação | CLARA |
| 6 | "Enviar e-mail de confirmação após matricular" | Aplicação | CLARA |
| 7 | "Verificar se o período letivo da turma está aberto" | Domínio | MÉDIA (parece "validação de entrada") |
| 8 | "Validar formato de CPF (11 dígitos, dígito verificador)" | Domínio | AMBÍGUA (parece regra técnica) |
| 9 | "Coordenar Financeiro para criar cobrança após matrícula" | Aplicação | AMBÍGUA (parece regra de negócio) |
| 10 | "Não pode existir matrícula ativa duplicada para o mesmo aluno e período" | Domínio | AMBÍGUA (parece validação de banco) |

**Critério de decisão para o gabarito:** "Uma regra é de Domínio se seria verdadeira independentemente de qual banco de dados, framework, canal de entrega (HTTP, batch, CLI) ou sistema externo o sistema use. Uma regra é de Aplicação se só existe porque o sistema tem uma arquitetura específica, orquestra múltiplos contextos, ou existe por requisito de infraestrutura."

---

## Estrutura de Arquivos a Criar

```
docs/00-ddd-sem-mudar-arquitetura/
├── 00-introducao.md                [MODIFICAR — adicionar links]
├── 01-service-anemico.md           [existente — não modificar]
├── 02-entidade-anemica.md          [existente — não modificar]
├── 03-service-deus.md              [existente — não modificar]
├── 04-duplicacao-regras.md         [existente — não modificar]
├── 05-regras-na-interface.md       [existente — não modificar]
├── 06-acoplamento-banco.md         [existente — não modificar]
├── guia-leitura-comparativo.md     [CRIAR — trail profundo operação "matricular"]
├── 07-linguagem-ubiqua.md          [CRIAR — DDD-01]
├── 08-entidades.md                 [CRIAR — DDD-02]
├── 09-value-objects.md             [CRIAR — DDD-03]
├── 10-agregados.md                 [CRIAR — DDD-04]
├── 11-repositorios.md              [CRIAR — DDD-05]
└── exercicio-classificacao.md      [CRIAR — REFD-03]

erp-matricula-ddd/src/main/java/br/com/escola/matricula/
├── dominio/modelo/Matricula.java       [MODIFICAR — comentários REFD]
├── aplicacao/MatricularAlunoUseCase.java  [MODIFICAR — comentários REFD]
└── dominio/repositorio/MatriculaRepositorio.java [MODIFICAR — comentários REFD]
```

**Total:** 7 arquivos novos + 4 arquivos modificados = 11 artefatos

---

## Guia de Leitura — Estrutura do Trail "matricular"

O trail profundo da operação "matricular" deve cobrir esta sequência de passos:

**Passo 1 — O problema (camadas):** `MatriculaServiceImpl.matricular()` — mostrar as regras `isAtivo`, `periodoInicio == null`, criação com setters, `setStatus("ATIVA")`

**Passo 2 — Onde foi parar: elegibilidade (ddd):** `VerificadorElegibilidadeMatricula.verificar()` — aluno.estaAtivo(), turma.periodoEstaAberto(), existeMatriculaAtiva(). Regras saíram do Service anêmico e foram para o Domain Service

**Passo 3 — Onde foi parar: criação segura (ddd):** `Matricula.criar()` — estado inicial ATIVA garantido, evento coletado automaticamente. Sem setters.

**Passo 4 — O orquestrador (ddd):** `MatricularAlunoUseCase.executar()` — 4 linhas de lógica, zero decisões de negócio. Contraste com `MatriculaServiceImpl.matricular()` que tem ~30 linhas de lógica.

**Passo 5 — Exercício:** "Agora leia `AdicionarDisciplinaUseCase.executar()` e `CancelarMatriculaUseCase.executar()`. O padrão se repete: verificar → delegar ao Aggregate → salvar → publicar. Quais regras de negócio você encontra no UseCase? Nenhuma."

---

## Environment Availability

Esta fase é puramente de documentação. Nenhuma dependência de runtime.

| Dependência | Requerida Por | Disponível | Observação |
|-------------|--------------|-----------|------------|
| Editor de texto/IDE | Edição de Java e Markdown | ✓ | Qualquer editor serve |
| Git | Commit dos artefatos | ✓ | Projeto já usa Git |
| Java compiler | Verificar que comentários REFD não quebram compilação | ✓ | Java 21 já instalado no projeto |

**Verificação de compilação:** após adicionar os comentários REFD nos 3 arquivos Java, uma compilação rápida (`mvn compile -pl erp-matricula-ddd`) garante que nenhum comentário mal formatado foi introduzido. Os comentários são `//` padrão Java — sem risco real, mas a verificação é boa prática.

---

## Validation Architecture

> `workflow.nyquist_validation` está explicitamente `false` em `.planning/config.json` — esta seção é omitida.

---

## Security Domain

Esta fase não introduz código executável, endpoints, ou processamento de dados. Sem aplicabilidade de ASVS.

---

## Assumptions Log

| # | Claim | Section | Risk se Errado |
|---|-------|---------|----------------|
| A1 | HTML `<details>` funciona em todos os visualizadores Markdown usados pelo estudante | Architecture Patterns | Baixo — `<details>` é suportado no GitHub, GitLab, VS Code. Em editores antigos pode aparecer como HTML cru, ainda legível |
| A2 | Nomenclatura `guia-leitura-comparativo.md` (sem número) é aceitável junto com os arquivos numerados 07-11 | Estrutura de Arquivos | Baixo — é uma convenção de nomenclatura; o planner pode renomear para `12-guia-leitura-comparativo.md` se preferir numeração consistente |

---

## Open Questions

1. **Ordem do guia de leitura vs. docs de conceitos no `00-introducao.md`**
   - O que sabemos: o guia de leitura é uma visão geral da transformação; os docs 07-11 são deep-dives por conceito
   - O que está unclear: deve o `00-introducao.md` listar o guia de leitura ANTES ou DEPOIS dos docs 07-11?
   - Recomendação: listar o guia de leitura PRIMEIRO — é o ponto de entrada para quem quer entender a transformação completa antes de mergulhar em cada conceito

2. **Numeração do guia de leitura**
   - O que sabemos: os anti-padrões são 00-06, os conceitos DDD são 07-11
   - O que está unclear: o CONTEXT.md D-07 lista apenas os 5 conceitos como 07-11; o guia de leitura não recebeu número
   - Recomendação: nomear como `guia-leitura-comparativo.md` sem número, pois é um artefato de navegação diferente dos docs de conceito — ou alternativamente `12-guia-leitura-comparativo.md` como último da série

---

## Sources

### Primary (HIGH confidence)
- Codebase: `erp-matricula-ddd/src/main/java/` — leitura direta de todos os arquivos Java mencionados [VERIFIED: codebase]
- Codebase: `erp-matricula-camadas/src/main/java/` — leitura direta de `MatriculaServiceImpl.java` e `model/Matricula.java` [VERIFIED: codebase]
- Codebase: `docs/00-ddd-sem-mudar-arquitetura/` — leitura de `00-introducao.md` e `01-service-anemico.md` para confirmar padrões de formatação [VERIFIED: codebase]
- Codebase: `docs/02-design-tatico/agregados.md` — padrão de formato ANTES/DEPOIS [VERIFIED: codebase]
- `.planning/phases/06-refatoracao-ddd-na-arquitetura-tradicional/06-CONTEXT.md` — decisões D-01..D-12 [VERIFIED: codebase]
- `.planning/phases/05-diagnostico-codigo-com-anti-padroes/05-VERIFICATION.md` — trail DIAG-06 quebrado documentado [VERIFIED: codebase]

### Secondary (MEDIUM confidence)
- `docs/04-material-didatico/ddd-vs-camadas.md` — padrão de comparativo existente [VERIFIED: codebase]

---

## Metadata

**Confidence breakdown:**
- Inventário de código: HIGH — leitura direta dos arquivos Java verificados
- Estrutura de artefatos: HIGH — derivado das decisões D-01..D-12 do CONTEXT.md
- Conteúdo dos snippets ANTES/DEPOIS: HIGH — baseado em código real lido nesta sessão
- Regras para o exercício: HIGH — baseado nas invariantes reais de `Matricula.java` e `VerificadorElegibilidadeMatricula.java`
- HTML `<details>` compatibilidade: MEDIUM — padrão HTML5 amplamente suportado, não verificado no ambiente específico do usuário

**Research date:** 2026-06-22
**Valid until:** 2026-12-22 (fase de documentação; código Java não muda sem nova fase)
