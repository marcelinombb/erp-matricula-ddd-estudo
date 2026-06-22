# Phase 6: Refatoracao DDD na Arquitetura Tradicional - Context

**Gathered:** 2026-06-22
**Status:** Ready for planning

<domain>
## Phase Boundary

Documentar e rotular o módulo `erp-matricula-ddd` existente como o "depois" do comparativo DDD, produzindo:

1. **Anotações REFD no código** — comentários explicativos nos 3 pivots principais do `erp-matricula-ddd`
2. **Guia de leitura comparativo** — trail explícito mostrando a transformação completa da operação "matricular"
3. **5 documentos de conceitos DDD** — um por conceito (Linguagem Ubíqua, Entidades, Value Objects, Agregados, Repositórios) com snippet antes/depois
4. **Exercício de classificação** — arquivo dedicado com regras do domínio de matrícula para o estudante classificar como "Domínio" ou "Aplicação"

Esta fase NÃO cria novo módulo Maven, NÃO modifica a lógica do `erp-matricula-ddd`, e NÃO inclui análise final (Phase 7).

</domain>

<decisions>
## Implementation Decisions

### Estrutura do Módulo "Depois"

- **D-01:** Usar `erp-matricula-ddd` como está — não criar módulo novo nem `erp-matricula-refatorado`. O módulo já tem Aggregate Matricula com invariantes, Value Objects como records Java 21, Application Services, e Repository. É o "depois" definitivo.
- **D-02:** Adicionar comentários `REFD` somente nos 3 pivots principais — não em todo o código:
  - `erp-matricula-ddd/.../dominio/modelo/Matricula.java` — invariantes protegidas pelo Aggregate
  - `erp-matricula-ddd/.../aplicacao/MatricularAlunoUseCase.java` — Application Service orquestrador vs. Service anêmico
  - `erp-matricula-ddd/.../dominio/repositorio/MatriculaRepositorio.java` — Repository vs. DAO genérico
- **D-03:** O restante do código DDD fica limpo — toda a orientação para os demais arquivos fica nos Markdowns, não no código.

### Comparativo Lado a Lado

- **D-04:** Formato de guia de leitura com trails explícitos — um Markdown que instrui o estudante a abrir arquivos específicos e observar linhas específicas. Exemplo: "Abra `MatriculaServiceImpl.java` linha 83 (camadas) e `MatricularAlunoUseCase.java` linha 12 (ddd). Observe que a regra 'aluno deve estar ativo' saiu do Service."
- **D-05:** Trail profundo para uma transformação completa: operação "matricular" (a mais rica — cobre REFD-01, REFD-02, DDD-02, DDD-04). As operações "adicionar disciplina" e "cancelar" são referenciadas como exercício para o estudante explorar sozinho.
- **D-06:** O guia de leitura deve mostrar explicitamente: o que mudou, o que foi encapsulado, qual invariante passou a ser protegida.

### Granularidade dos Conceitos DDD

- **D-07:** Um arquivo Markdown por conceito DDD, criados em `docs/00-ddd-sem-mudar-arquitetura/`:
  - `07-linguagem-ubiqua.md` — DDD-01
  - `08-entidades.md` — DDD-02
  - `09-value-objects.md` — DDD-03
  - `10-agregados.md` — DDD-04
  - `11-repositorios.md` — DDD-05
- **D-08:** Cada arquivo segue o padrão estabelecido em `docs/02-design-tatico/`: definição simples (sem jargão excessivo), snippet ANTES (erp-matricula-camadas), snippet DEPOIS (erp-matricula-ddd), e o que foi ganho com a mudança.
- **D-09:** Arquivos novos — não enriquecer os existentes em `docs/02-design-tatico/`. Os docs da Phase 2 são teoria geral DDD; os novos são aplicação específica "sem mudar a arquitetura".

### Exercício de Classificação REFD-03

- **D-10:** Arquivo dedicado `exercicio-classificacao.md` em `docs/00-ddd-sem-mudar-arquitetura/`. Não integrado ao guia de leitura.
- **D-11:** Formato: lista de 8-10 regras do domínio de matrícula. O estudante classifica mentalmente como "Domínio" ou "Aplicação", depois expande o `<details>` HTML para ver a resposta com justificativa. Aprende ativamente.
- **D-12:** Regras a incluir devem cobrir casos claros e casos ambíguos para forçar reflexão (ex: "validar formato de CPF" é menos óbvio que "aluno deve ter vaga na turma").

### Claude's Discretion

- Número exato de linhas de código nos snippets (mínimo suficiente para ilustrar o contraste)
- Nomenclatura dos arquivos numerados (07 a 11) — sequência após os 6 existentes em `docs/00-ddd-sem-mudar-arquitetura/`
- Seleção das 8-10 regras para o exercício de classificação (baseando-se nas regras reais de `Matricula.java` e `MatricularAlunoUseCase.java`)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Código "Antes" (erp-matricula-camadas)

- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java` — Service anêmico de referência (227 linhas). Pivots de comparação: linha 83 (isAtivo), linha 121 (adicionarDisciplina com countByMatriculaId), linha 149 (countByMatriculaId)
- `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java` — Entidade anêmica DIAG-02. Sem comportamento, só getters/setters + String status

### Código "Depois" (erp-matricula-ddd) — Pivots REFD

- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java` — Aggregate Root com invariantes. Receberá comentários REFD-D-02.
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java` — Application Service orquestrador. Receberá comentários REFD-D-01.
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java` — Interface Repository no domínio. Receberá comentários REFD-D-05.

### Documentação Existente (contexto e padrão)

- `docs/00-ddd-sem-mudar-arquitetura/` — pasta onde vivem os 6 docs existentes da Phase 5. Novos docs (07-11 + guia + exercício) entram aqui.
- `docs/00-ddd-sem-mudar-arquitetura/00-introducao.md` — deve ser atualizado com links para os novos documentos da Phase 6
- `docs/02-design-tatico/` — padrão de formato ERRADO/CERTO a seguir para snippets nos docs de conceitos DDD
- `docs/04-material-didatico/ddd-vs-camadas.md` — comparativo já existente; NÃO duplicar. Phase 6 aprofunda o "depois" com referências ao código real.

### Requirements e Roadmap

- `.planning/REQUIREMENTS.md` — Phase 6 cobre REFD-01, REFD-02, REFD-03, DDD-01..05, DID-02 (9 requirements)
- `.planning/ROADMAP.md` — Phase 6 goal e 5 success criteria
- `.planning/PROJECT.md` — Core value, Constraints (stack obrigatória), Key Decisions (ADRs)

### Contexto da Phase Anterior

- `.planning/phases/05-diagnostico-codigo-com-anti-padroes/05-CONTEXT.md` — decisões D-01..D-11 da Phase 5 (nomenclatura, estrutura de módulos, estilo de documentação)
- `.planning/phases/05-diagnostico-codigo-com-anti-padroes/05-VERIFICATION.md` — gaps conhecidos na Phase 5 (trail DIAG-06 quebrado, DisciplinaServiceImpl sem endpoint). Phase 6 não precisa corrigir, mas os docs comparativos devem apontar para o método que realmente existe (`countByMatriculaId`)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java` — Aggregate Root com métodos `matricular()`, `adicionarDisciplina()`, `cancelar()`. Invariantes reais: aluno ativo, período aberto, limite de disciplinas, sem duplicatas.
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/` — 6 Value Objects como records Java 21: AlunoId, Cpf, MatriculaId, NomeDisciplina, PeriodoLetivo, TurmaId. Todos imutáveis por design.
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/` — 3 Use Cases (MatricularAlunoUseCase, AdicionarDisciplinaUseCase, CancelarMatriculaUseCase) + Commands como records imutáveis
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java` — Domain Service puro (sem anotações Spring)

### Established Patterns

- `docs/02-design-tatico/` — formato dos docs: seção ERRADO (snippet antes), seção CERTO (snippet depois), seção "O que ganhámos". Replicar em todos os 5 docs de conceitos.
- Numeração sequencial dos docs em `docs/00-ddd-sem-mudar-arquitetura/` — existem 00 a 06. Novos começam em 07.
- Comentários pedagógicos no código usam padrão `// ANTI-PADRAO: X (DIAG-XX)` na Phase 5 — Phase 6 usa padrão `// REFD-XX: [explicação]`

### Integration Points

- `docs/00-ddd-sem-mudar-arquitetura/00-introducao.md` — tabela de navegação precisa de linhas para os documentos novos
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java` — 3 arquivos receberão comentários REFD; não alterar lógica, apenas adicionar comentários
- Nenhuma alteração em `erp-matricula-camadas/` — módulo "antes" está finalizado na Phase 5

</code_context>

<specifics>
## Specific Ideas

- **Trail de leitura**: Começar pelo problema visível no `MatriculaServiceImpl.java` (linha 83, isAtivo check no service), rastrear para onde essa responsabilidade foi para o DDD (`Matricula.java` — no aggregate), e mostrar por que isso protege a invariante de forma que o Service não consegue.
- **Exercício REFD-03**: Incluir pelo menos 2 casos ambíguos — "validar formato de CPF" (parece domínio, mas é technicality) e "garantir que não há matrícula duplicada no mesmo período" (claramente domínio). O gabarito no `<details>` deve explicar o critério de decisão, não só dar a resposta.
- **Nomeação**: Os docs de conceitos DDD são "aplicados" — devem ter nomes que reforcem isso: "09-value-objects-na-pratica.md" é melhor que "09-value-objects.md" para diferenciar dos docs teóricos da Phase 2.

</specifics>

<deferred>
## Deferred Ideas

- Corrrigir trail DIAG-06 quebrado (doc `06-acoplamento-banco.md` vs código real) — identificado em 05-VERIFICATION.md como blocker pedagógico. A Phase 6 NÃO corrige isso; os docs comparativos simplesmente referenciam `countByMatriculaId` que é o método real. Mas a correção do doc da Phase 5 é uma tarefa pendente que o usuário deve decidir antes de publicar o material.
- DisciplinaServiceImpl sem endpoint HTTP — DID-04 demonstrável só por leitura. Phase 6 pode mencionar isso no guia de leitura como "código que existe mas não é chamado — anti-padrão visível pela ausência de wiring".
- Numeração dos docs (07-11): se o usuário quiser reorganizar os docs existentes 01-06, isso é uma refatoração separada fora do escopo desta fase.

</deferred>

---

*Phase: 6-Refatoracao DDD na Arquitetura Tradicional*
*Context gathered: 2026-06-22*
