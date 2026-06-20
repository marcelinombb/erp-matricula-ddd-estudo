# ERP Matrícula — Projeto Didático DDD

## What This Is

Projeto de treinamento completo em Domain-Driven Design (DDD), usando o domínio de Matrícula Escolar como campo de prática. Construído para desenvolvedores que dominam Spring Boot e arquitetura em camadas (Controller → Service → Repository) mas nunca aplicaram DDD de forma estruturada. O projeto é uma referência autônoma — se explica sem instrutor, através de documentação progressiva que acompanha cada decisão de código.

## Core Value

Um desenvolvedor da equipe deve conseguir, sozinho, ler o projeto do início ao fim e entender **por que** DDD existe, **onde** ele diverge da arquitetura tradicional e **como** aplicar cada padrão tático no código Java real.

## Requirements

### Validated

- [x] Documentação da Fase 1: Descoberta do Domínio — Problema de Negócio, Linguagem Ubíqua (glossário 4 colunas, seção de Conceitos Ambíguos), Subdomínios classificados, Bounded Contexts, Context Map Mermaid, 4 ADRs com code examples *(Validated in Phase 1: design-estrategico, 2026-06-20)*
- [x] Documentação da Fase 2: Design Tático — 7 arquivos Markdown (Value Objects, Entidades, Agregados, Domain Services, Domain Events, Repositórios, Modelagem Visual com 4 diagramas Mermaid) com Java 21 snippets, seções ERRADO/CERTO e cross-references entre documentos *(Validated in Phase 2: design-tatico-e-modelagem-visual, 2026-06-20)*

### Active

- [ ] Documentação da Fase 2: Design Estratégico (Bounded Contexts, Context Map em Mermaid, Decisões Arquiteturais)
- [ ] Documentação da Fase 3: Design Tático (Entidades, Value Objects, Agregados, Domain Services, Domain Events, Repositórios — todos com justificativas pedagógicas)
- [ ] Documentação da Fase 4: Modelagem (Diagrama de Classes, Agregados, Fluxos de Negócio, Sequence Diagrams — todos em Mermaid)
- [ ] Documentação da Fase 5: Persistência PostgreSQL (Scripts SQL completos: schema, tabelas, constraints, indexes, foreign keys + seeds para demonstração)
- [ ] Implementação da Fase 6: API funcional com fluxo de matrícula completo (matricular aluno, adicionar disciplinas, cancelar matrícula) em Java 21 + Spring Boot 3.x + MyBatis
- [ ] Estrutura de pastas DDD explicada: `domain/`, `application/`, `infrastructure/`, `interfaces/`
- [ ] Camada de Domínio: Entidades, Value Objects (CPF, PeriodoLetivo etc.), Agregado Matrícula com invariantes, Domain Events (AlunoMatriculado, MatriculaCancelada, DisciplinaAdicionada), interfaces de Repositório
- [ ] Camada de Aplicação: Casos de uso, DTOs, Commands
- [ ] Camada de Infraestrutura: MyBatis Mappers + implementações dos Repositórios + configurações
- [ ] Camada de Interface: Controllers REST, validações, tratamento de erros
- [ ] Fase 7: Docker Compose com PostgreSQL + Aplicação (instruções: subir, derrubar, resetar banco)
- [ ] Fase 8: Seção "DDD para quem vem da Arquitetura em Camadas" — comparação concreta linha a linha
- [ ] Fase 9: ADRs (Architecture Decision Records) — por que MyBatis vs JPA, decisões de Bounded Context, decisões de Aggregate
- [ ] Fase 10: Guia de consulta final mapeando cada conceito DDD a arquivos concretos do projeto

### Out of Scope

- Contextos Financeiro, Acadêmico e Secretaria implementados — mencionados no Context Map e nos eventos de domínio, mas não codificados (adicionar complexidade desnecessária sem ganho pedagógico no v1)
- Frontend / UI — projeto é backend didático; a interface é a documentação
- Autenticação e autorização — Generic Domain, adiciona ruído ao aprendizado de DDD
- Testes automatizados completos — podem ser adicionados depois; o foco inicial é clareza do código de produção
- Deploy em cloud — Docker local é suficiente para o propósito de treinamento

## Context

- **Audiência**: Desenvolvedores com experiência em Spring Boot, banco relacional e arquitetura em camadas; zero experiência estruturada com DDD
- **Estilo pedagógico**: Para cada conceito DDD introduzido, mostrar o paralelo com arquitetura tradicional. Explicar o que mudou, por que mudou, os benefícios e os trade-offs
- **Idioma do código**: Português — reforça a Linguagem Ubíqua do domínio (Matrícula, Aluno, Turma, PeriodoLetivo, não StudentEntity ou RegistrationDTO)
- **Uso esperado**: Referência autônoma — desenvolvedor estuda sem instrutor. A documentação precisa ser suficientemente detalhada para funcionar como guia de leitura
- **Domínio escolhido**: Matrícula Escolar maximiza a demonstração dos padrões DDD por ter regras de negócio ricas, múltiplos contextos com o mesmo conceito (Aluno) sob visões diferentes, e fluxos claros de eventos entre contextos

## Constraints

- **Stack obrigatória**: Java 21, Spring Boot 3.x, MyBatis, PostgreSQL, Docker, Docker Compose, Maven — não negociável (é o stack da equipe)
- **MyBatis (não JPA)**: Mapeamento explícito reforça a separação entre modelo de domínio e modelo relacional — um dos pontos pedagógicos centrais
- **Diagramas**: Todos em Mermaid (sem ferramentas externas, funciona direto no Markdown/GitHub)
- **Documentação**: Todo em Markdown, em português
- **Bounded Context implementado**: Apenas Matrícula. Financeiro e Acadêmico existem apenas como consumidores de eventos no Context Map

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| MyBatis em vez de JPA/Hibernate | Mapeamento explícito evidencia a separação domínio/persistência — um ponto pedagógico central do DDD; JPA tende a vazar abstrações de persistência no domínio | ADR-001 — Validated in Phase 1 |
| Código em português | Reforça Linguagem Ubíqua; um glossário em inglês quebraria a coerência com os termos do negócio | ADR-004 — Validated in Phase 1 |
| Apenas contexto Matrícula implementado | Implementar Financeiro e Acadêmico adicionaria complexidade de integração sem acrescentar conceitos novos ao aprendizado de DDD tático | ADR-002 — Validated in Phase 1 |
| Referência por ID entre Aggregates | `Matricula` guarda `AlunoId` (não `Aluno`) — demonstra o padrão DDD de referência entre agregados sem acoplamento de carregamento | ADR-003 — Validated in Phase 1 |

---

## Evolution

Este documento evolui em transições de fase e marcos de milestone.

**Após cada transição de fase** (via `/gsd-transition`):
1. Requirements invalidados? → Mover para Out of Scope com motivo
2. Requirements validados? → Mover para Validated com referência de fase
3. Novos requirements emergiram? → Adicionar em Active
4. Decisões a registrar? → Adicionar em Key Decisions
5. "What This Is" ainda preciso? → Atualizar se tiver divergido

**Após cada milestone** (via `/gsd-complete-milestone`):
1. Revisão completa de todas as seções
2. Core Value check — ainda é a prioridade certa?
3. Auditoria de Out of Scope — motivos ainda válidos?
4. Atualizar Context com estado atual

---
*Last updated: 2026-06-20 — Phase 1 (design-estrategico) complete*
