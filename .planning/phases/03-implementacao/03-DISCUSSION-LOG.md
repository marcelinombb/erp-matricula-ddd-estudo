# Phase 3: Implementacao - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-20
**Phase:** 3-implementacao
**Areas discussed:** Estrutura de pacotes, Bootstrap do projeto, Coleta e publicação de eventos, Persistência do Aggregate

---

## Estrutura de Pacotes

### Pacote raiz

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| `br.com.escola.matricula` | Padrão Java de produção; mais realista para referência da equipe | ✓ |
| `matricula` | Sem org, simples para didático | |
| `com.erp.matricula` | Meio-termo, remete ao nome do projeto | |

**Escolha:** `br.com.escola.matricula`

---

### Sub-pacotes do domínio

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| Flat: `dominio/` com todos os tipos | Pedagogicamente claro, o aluno abre e vê o modelo inteiro | |
| Sub-pacotes por tipo: `dominio.modelo`, `dominio.vo`, `dominio.evento`, `dominio.repositorio`, `dominio.servico` | Mais granular, separa tipos por responsabilidade | ✓ |

**Escolha:** Sub-pacotes por tipo
**Notas:** Usuário optou pela granularidade maior, que facilita identificar a responsabilidade de cada artefato.

---

### Sub-pacotes de aplicacao/ e infraestrutura/

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| `aplicacao/` flat + `infraestrutura/` com sub-pacotes | Use cases e DTOs flat; infra dividida por tecnologia (`persistencia/`, `eventos/`, `config/`) | ✓ |
| Ambas flat | Todos os artefatos sem sub-divisão | |

**Escolha:** `aplicacao/` flat + `infraestrutura/` com sub-pacotes por tecnologia

---

### Localização das exceções de domínio

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| `dominio.excecao/` (sub-pacote dedicado) | Agrupa exceções separadas do modelo; facilita import nos Controllers da Fase 4 | ✓ |
| `dominio.modelo/` (junto com o modelo) | Exceção como parte do contrato do Aggregate | |

**Escolha:** `dominio.excecao/`

---

## Bootstrap do Projeto

### groupId / artifactId

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| `br.com.escola` / `erp-matricula` | Consistente com pacote raiz; groupId genérico | ✓ |
| `com.erp` / `matricula` | Mais curto | |
| `br.com.escola.matricula` / `erp-matricula` | groupId idêntico ao pacote raiz | |

**Escolha:** `groupId: br.com.escola`, `artifactId: erp-matricula`

---

### Starters Spring Boot

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| Mínimo viável sem web | `spring-boot-starter`, `mybatis-spring-boot-starter`, postgresql, flyway. Sem `starter-web`. | ✓ |
| Incluir web já | Adicionar `spring-boot-starter-web` agora mesmo sem Controllers na Fase 3 | |

**Escolha:** Mínimo viável — sem `spring-boot-starter-web`
**Notas:** Decisão pedagógica — mostra que DDD não depende de HTTP.

---

### application.yml

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| `application.yml` com perfil local + Docker | Um único arquivo, datasource em localhost:5432 | ✓ |
| `application.yml` + `application-test.yml` | Perfil test com H2/Testcontainers | |

**Escolha:** `application.yml` único sem profiles adicionais na Fase 3

---

## Coleta e Publicação de Eventos

### Mecanismo de coleta no Aggregate (DOM-07)

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| Lista interna com `coletarEventos()` | `List<Object>` no Aggregate; método retorna cópia imutável e limpa a lista. Zero Spring. | ✓ |
| Interface `EventPublisher` no domínio | Interface no domínio, implementação na infraestrutura. Mais port-and-adapter. | |

**Escolha:** Lista interna com `coletarEventos()`

---

### Como o UseCase publica os eventos

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| `ApplicationEventPublisher` direto no UseCase | Injeção por construtor; publica após `repositorio.salvar()` | ✓ |
| Método `publicarEventos()` separado | Helper na camada de aplicação | |

**Escolha:** `ApplicationEventPublisher` injetado no UseCase

---

### Tipo base dos Domain Events

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| Sem tipo base — records independentes | `List<Object>` no Aggregate; sem interface DomainEvent | ✓ |
| Interface marcadora `DomainEvent` | `List<DomainEvent>` no Aggregate; records implementam a interface | |

**Escolha:** Records independentes sem tipo base

---

## Persistência do Aggregate (MyBatis)

### Estratégia de atualização da coleção

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| Replace-all: DELETE + INSERT | DELETE todos itens + INSERT atuais. Simples, explícito, pedagógico. | ✓ |
| Merge/diff: rastrear mudanças | Aggregate rastreia `itensAdicionados`/`itensCancelados`. Mais complexo. | |

**Escolha:** Replace-all

---

### MatriculaRow vs. mapeamento direto

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| `MatriculaRow` separada + `MatriculaRowMapper` explícito | Separação domínio/persistência visível; alinhado com INF-06 | ✓ (revisitado) |
| Sem `MatriculaRow` — MyBatis mapeia direto para `Matricula` | Menos código, mas viola separação e INF-06 | (inicial, revisitado) |

**Escolha:** `MatriculaRow` + `MatriculaRowMapper`
**Notas:** Usuário inicialmente escolheu "sem MatriculaRow", mas revisitou após identificar conflito com INF-06 e o princípio pedagógico de separação explícita.

---

### Value Objects com múltiplos campos no banco

| Opção | Descrição | Selecionado |
|-------|-----------|-------------|
| Colunas separadas por campo | `periodo_inicio DATE` + `periodo_fim DATE`; mais SQL explícito e consultável | ✓ |
| Coluna JSON para VOs compostos | `JSONB`; flex, mas esconde estrutura | |

**Escolha:** Colunas separadas por campo

---

## Claude's Discretion

- Número exato de seeds (mínimo suficiente para 3 fluxos)
- Nome exato do banco PostgreSQL (`erp_matricula` sugerido)
- Abordagem para `PeriodoLetivoTypeHandler` com dois campos (TypeHandler por campo vs. mapeamento inline no ResultMap XML)
- Configuração de transaction manager (padrão Spring Boot)

## Deferred Ideas

- **Docker Compose** — Fase 4 (DCK-01, DCK-02)
- **Controllers REST** — Fase 4 (IFX-01, IFX-02, IFX-03)
- **Testes automatizados** — Fora do escopo do projeto v1
- **Optimistic locking completo** — Nota explicativa apenas nesta fase
