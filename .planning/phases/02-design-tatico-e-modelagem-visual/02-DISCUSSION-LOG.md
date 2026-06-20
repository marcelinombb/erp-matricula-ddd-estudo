# Phase 2: Design Tatico e Modelagem Visual - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-20
**Phase:** 02-design-tatico-e-modelagem-visual
**Areas discussed:** Estrutura de arquivos, Template por padrão, Código Java nos docs, Localização dos diagramas

---

## Estrutura de arquivos

### Organização dos documentos táticos

| Option | Description | Selected |
|--------|-------------|----------|
| 1 arquivo por padrão DDD | 6 arquivos temáticos em docs/02-design-tatico/ — entidades.md, value-objects.md, agregados.md, domain-services.md, domain-events.md, repositorios.md | ✓ |
| 1 arquivo por conceito do domínio | Cada entidade/VO/agregado tem seu próprio arquivo (10+ arquivos) | |
| 1 arquivo consolidado por fase | Um único design-tatico.md com todos os 6 padrões em seções | |

**User's choice:** 1 arquivo por padrão DDD
**Notes:** Consistente com o padrão estabelecido na Fase 1 (4 arquivos temáticos em docs/01-design-estrategico/).

### Localização dos diagramas Mermaid

| Option | Description | Selected |
|--------|-------------|----------|
| modelagem.md separado | Arquivo dedicado docs/02-design-tatico/modelagem.md com os 4 diagramas | ✓ |
| Embutidos nos padrões relevantes | Diagrama de classes em entidades.md, agregados em agregados.md, etc. | |

**User's choice:** modelagem.md separado
**Notes:** Separação clara — padrões descrevem o que é; diagramas mostram como se conecta.

### Índice de navegação

| Option | Description | Selected |
|--------|-------------|----------|
| README.md atualizado | Adicionar seção ## Design Tático no README.md da raiz | ✓ |
| Índice próprio na pasta | Criar docs/02-design-tatico/README.md como índice local | |

**User's choice:** README.md da raiz atualizado
**Notes:** Navegação centralizada, sem fragmentação.

---

## Template por padrão

### Abordagem pedagógica

| Option | Description | Selected |
|--------|-------------|----------|
| Bottom-up: domínio → padrão | Começa com problema concreto do domínio, depois nomeia o padrão DDD | ✓ |
| Top-down: padrão → domínio | Começa com definição DDD, depois mostra instâncias no domínio | |
| Comparativo: DDD vs camadas | Para cada padrão, mostra o equivalente em arquitetura tradicional | |

**User's choice:** Bottom-up: domínio → padrão
**Notes:** Mais pedagógico para desenvolvedores sem experiência DDD — o problema motiva o padrão.

### Seção de anti-patterns

| Option | Description | Selected |
|--------|-------------|----------|
| Sim, anti-patterns por padrão | Cada arquivo termina com "Erros Comuns" | ✓ |
| Não, focar no positivo | Documentar apenas o que fazer | |

**User's choice:** Sim, anti-patterns por padrão
**Notes:** Reforça o aprendizado mostrando o que evitar.

### Invariantes do Agregado

| Option | Description | Selected |
|--------|-------------|----------|
| Narrativa + tabela resumo | Explica contexto de cada invariante, depois tabela de referência | ✓ |
| Só tabela | Tabela direta sem narrativa explicativa | |

**User's choice:** Narrativa + tabela resumo
**Notes:** Inclui o argumento de concorrência para justificar por que a invariante pertence ao Agregado.

---

## Código Java nos docs

### Presença de código

| Option | Description | Selected |
|--------|-------------|----------|
| Snippets Java 21 prospectivos | Código que SERÁ implementado na Fase 3 | ✓ |
| Pseudocódigo descritivo | Representação simplificada, não Java real | |
| Sem código nos docs táticos | Documentação conceitual pura | |

**User's choice:** Snippets Java 21 prospectivos
**Notes:** Os documentos táticos funcionam como especificação técnica para a Fase 3.

### Destaque de features Java 21

| Option | Description | Selected |
|--------|-------------|----------|
| Sim, destacar Java 21 features | Comentários explicando fit DDD (record → VO, sealed → estados finitos) | ✓ |
| Java genérico, sem destaque | Código funcional sem ênfase em features Java 21 | |

**User's choice:** Sim, destacar Java 21 features
**Notes:** Adiciona camada didática sobre modernidade do Java 21 e seu alinhamento natural com DDD.

### Anti-patterns em código

| Option | Description | Selected |
|--------|-------------|----------|
| ERRADO/CERTO em código | Snippet arquitetura em camadas (ERRADO) + DDD (CERTO) | ✓ |
| Apenas CERTO, sem o ERRADO | Mostrar apenas implementação DDD correta | |

**User's choice:** ERRADO/CERTO em código
**Notes:** O snippet ERRADO deve mostrar código estilo Spring Service que o desenvolvedor reconhece do seu dia a dia.

---

## Localização dos diagramas

### Profundidade do sequence diagram

| Option | Description | Selected |
|--------|-------------|----------|
| Fluxo completo + publicação de evento | HTTP → Controller → UseCase → VerificadorElegibilidade → Agregado → Repositório → EventPublisher | ✓ |
| Fluxo simplificado sem internos do UseCase | Apenas camadas principais sem Domain Service e evento | |

**User's choice:** Fluxo completo + publicação de evento
**Notes:** Cobre exatamente o success criteria da fase.

### Caminhos de erro nos flowcharts

| Option | Description | Selected |
|--------|-------------|----------|
| Happy path + caminhos de erro | Ramificações para cada exceção de domínio | ✓ |
| Só happy path | Flowchart linear sem ramificações de erro | |

**User's choice:** Happy path + caminhos de erro
**Notes:** Une visualmente os fluxos com as invariantes documentadas em TAT-03.

### Contextualização no modelagem.md

| Option | Description | Selected |
|--------|-------------|----------|
| Texto introdutório por diagrama | 2-3 linhas contextualizando antes de cada bloco Mermaid | ✓ |
| Apenas diagramas com títulos | Sem texto entre os diagramas | |

**User's choice:** Texto introdutório por diagrama
**Notes:** Texto aponta o que observar e referencia ADRs relevantes — especialmente ADR-003 no diagrama de classes.

---

## Claude's Discretion

- Quantidade exata de VOs cobertos (todos os DOM-01: Cpf, PeriodoLetivo, MatriculaId, AlunoId, TurmaId, NomeDisciplina) com profundidade proporcional à complexidade de validação.
- Granularidade do diagrama de classes (atributos e métodos relevantes sem poluir).
- Se ItemMatricula aparece em entidades.md ou só em agregados.md.

## Deferred Ideas

None — a discussão se manteve dentro do escopo da Fase 2.
