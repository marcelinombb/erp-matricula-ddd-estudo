# Phase 6: Refatoracao DDD na Arquitetura Tradicional - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-22
**Phase:** 06-refatoracao-ddd-na-arquitetura-tradicional
**Areas discussed:** Estrutura do módulo "depois", Comparativo lado a lado, Granularidade dos conceitos DDD, Exercício de classificação REFD-03

---

## Estrutura do Módulo "Depois"

### Pergunta 1: Como tratar o erp-matricula-ddd existente?

| Option | Description | Selected |
|--------|-------------|----------|
| Usar erp-matricula-ddd como está | O módulo já tem Aggregate Matricula com invariantes, Value Objects, Application Services e Repository. A Phase 6 documenta e rotula esse código como o "depois". Zero risco de regressão. | ✓ |
| Criar erp-matricula-refatorado separado | Novo módulo Maven que parte literalmente do erp-matricula-camadas e mostra cada passo de refatoração. Mais didático como "jornada", mas adiciona terceiro módulo. | |
| Enriquecer erp-matricula-camadas com branch DDD | Adicionar classes DDD no mesmo módulo. Mais compacto, mas mistura antes e depois. | |

**User's choice:** Usar erp-matricula-ddd como está
**Notes:** Sem notas adicionais — escolha direta.

### Pergunta 2: Quais arquivos ganham comentários REFD?

| Option | Description | Selected |
|--------|-------------|----------|
| Somente os pivots principais | Comentários REFD em: Matricula.java, MatricularAlunoUseCase.java, MatriculaRepositorio.java. Foco nos 3 contrastes mais impactantes. | ✓ |
| Todos os arquivos do domínio | Comentários REFD em todos os 8 arquivos de domínio. Mais completo, mas dilui a atenção. | |
| Nenhum comentário no código | Toda orientação nos Markdowns. O código DDD fica completamente limpo. | |

**User's choice:** Somente os pivots principais
**Notes:** Sem notas adicionais.

---

## Comparativo Lado a Lado

### Pergunta 1: Formato do comparativo

| Option | Description | Selected |
|--------|-------------|----------|
| Guia de leitura com trails explícitos | Markdown que instrui abrir arquivos específicos e observar linhas específicas. Rastreamento explode do problema para a solução. | ✓ |
| Snippets antes/depois em cada doc de conceito | Cada documento tem seção com snippet ANTES e DEPOIS embutidos. Mais autossuficiente, mas pode ficar repetitivo. | |
| Diff de código estilo PR | Apresentar transformações como pull request com blocos +/-. Familiar para quem usa GitHub. | |

**User's choice:** Guia de leitura com trails explícitos
**Notes:** Sem notas adicionais.

### Pergunta 2: Profundidade dos trails

| Option | Description | Selected |
|--------|-------------|----------|
| Uma transformação completa + referência às demais | Trail profundo para "matricular". As demais operações referenciadas como exercício para explorar sozinho. | ✓ |
| As 3 operações completas | Trail detalhado para matricular, adicionar disciplina e cancelar. Mais completo, mas o padrão fica repetitivo. | |
| Só snippets-chave sem trail de navegação | Mostrar trechos mais reveladores sem instruir o estudante a abrir arquivos. Mais conciso. | |

**User's choice:** Uma transformação completa + referência às demais
**Notes:** Sem notas adicionais.

---

## Granularidade dos Conceitos DDD

### Pergunta 1: Organização dos 5 conceitos

| Option | Description | Selected |
|--------|-------------|----------|
| Um arquivo por conceito com snippet antes/depois | 5 Markdowns numerados (07-11) em docs/00-ddd-sem-mudar-arquitetura/. Continua o padrão de docs/02-design-tatico/. | ✓ |
| Um documento único de conceitos DDD | Um único "conceitos-ddd.md". Menos fricção de navegação, mas quebra o padrão estabelecido. | |
| Integrar os conceitos ao guia de leitura | Cada conceito explicado dentro do trail quando aparece organicamente. Mais fluido, menos consultável. | |

**User's choice:** Um arquivo por conceito com snippet antes/depois
**Notes:** Sem notas adicionais.

### Pergunta 2: Novos arquivos ou enriquecer existentes?

| Option | Description | Selected |
|--------|-------------|----------|
| Criar novos docs em docs/00-ddd-sem-mudar-arquitetura/ | Os docs da Phase 2 são teoria geral DDD. Novos docs são aplicação específica "sem mudar a arquitetura". Perspectivas complementares sem duplicação. | ✓ |
| Enriquecer os docs existentes de docs/02-design-tatico/ | Adicionar seções "Aplicação na Arquitetura Tradicional" nos arquivos existentes. Ponto único de referência por conceito. | |

**User's choice:** Criar novos docs em docs/00-ddd-sem-mudar-arquitetura/
**Notes:** Sem notas adicionais.

---

## Exercício de Classificação REFD-03

### Pergunta 1: Formato do exercício

| Option | Description | Selected |
|--------|-------------|----------|
| Markdown com lista de regras + gabarito em details/summary | 8-10 regras, estudante classifica mentalmente, expande <details> para ver resposta com justificativa. Aprendizado ativo sem infraestrutura extra. | ✓ |
| Guia de leitura com perguntas retóricas | Perguntas dentro do doc comparativo sem gabarito formal. Mais fluido, menos estruturado como exercício. | |
| Tabela de classificação com exemplos do código real | Tabela com coluna "Onde está no código". Mais direta como referência, menos interativa. | |

**User's choice:** Markdown com lista de regras + gabarito em details/summary
**Notes:** Deve incluir casos ambíguos como "validar formato de CPF" para forçar reflexão.

### Pergunta 2: Localização do exercício

| Option | Description | Selected |
|--------|-------------|----------|
| Arquivo dedicado exercicio-classificacao.md | Separado do guia de leitura. Progressão clara: diagnóstico → conceitos → prática. Fácil de referenciar isoladamente. | ✓ |
| Integrado ao guia de leitura | Perguntas de classificação aparecem ao longo do trail. Mais contextual, mas sem "exercício" claro. | |

**User's choice:** Arquivo dedicado
**Notes:** Sem notas adicionais.

---

## Claude's Discretion

- Número exato de linhas de código nos snippets (mínimo suficiente para ilustrar o contraste)
- Nomenclatura dos arquivos numerados 07-11 (sequência após os 6 existentes)
- Seleção das 8-10 regras para o exercício de classificação (baseando-se nas regras reais de Matricula.java e MatricularAlunoUseCase.java)

## Deferred Ideas

- Correção do trail DIAG-06 quebrado em docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md — identificado na Phase 5 como blocker pedagógico. Não é escopo da Phase 6, mas deve ser resolvido antes da publicação do material.
- DisciplinaServiceImpl sem endpoint HTTP — pode ser mencionado no guia de leitura como exemplo de código unreachable, mas correção (adicionar endpoint) fica fora do escopo.
