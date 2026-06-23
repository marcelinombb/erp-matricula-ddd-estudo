# ERP Matrícula — Projeto Didático DDD

## What This Is

Projeto de treinamento completo em Domain-Driven Design (DDD), usando o domínio de Matrícula Escolar como campo de prática. Construído para desenvolvedores que dominam Spring Boot e arquitetura em camadas (Controller → Service → Repository) mas nunca aplicaram DDD de forma estruturada. O projeto é uma referência autônoma — se explica sem instrutor, através de documentação progressiva que acompanha cada decisão de código.

## Core Value

Um desenvolvedor da equipe deve conseguir, sozinho, ler o projeto do início ao fim e entender **por que** DDD existe, **onde** ele diverge da arquitetura tradicional e **como** aplicar cada padrão tático no código Java real.

## Current State (v1.1 — shipped 2026-06-22)

O projeto está completo em dois milestones entregues:

**v1.0 — Projeto Didático DDD Base** (Phases 1-4, shipped 2026-06-21)
- Documentação estratégica completa (Linguagem Ubíqua, Bounded Contexts, Context Map, 4 ADRs)
- Design tático documentado (7 Markdowns com VOs, Entidades, Agregados, Domain Services, Events, Repositórios)
- Código Java 21 completo: 42 arquivos de domínio/aplicação/infraestrutura, 3.514 LOC, 3 invariantes
- API REST funcional, Docker Compose com um comando
- Material didático comparativo "DDD vs Arquitetura em Camadas"

**v1.1 — DDD sem Mudar a Arquitetura** (Phases 5-7, shipped 2026-06-22)
- Módulo `erp-matricula-camadas` (porta 8081): o "antes" — arquitetura em camadas com 6 anti-padrões anotados
- 12 documentos pedagógicos em `docs/00-ddd-sem-mudar-arquitetura/` cobrindo diagnóstico, conceitos DDD e análise final
- Comentários REFD inline nos 3 pivots Java do módulo DDD (Matricula.java, MatricularAlunoUseCase.java, MatriculaRepositorio.java)
- `12-analise-final.md`: balanço crítico com dados concretos do projeto (42 arquivos, 3.514 LOC, 227 linhas do God Service)

**Estrutura atual:**
- 2 módulos Maven: `erp-matricula-ddd` (porta 8080, DDD) + `erp-matricula-camadas` (porta 8081, camadas)
- `docs/` com 5 diretórios de documentação pedagógica progressiva
- Docker Compose sobe ambos os módulos com um comando

## Current Milestone: v1.2 Testes como Evidência de Design

**Goal:** Demonstrar, através de comparativo de testabilidade, por que um domínio rico (DDD) é mais fácil de testar do que um God Service (arquitetura em camadas), usando testes reais em ambos os módulos.

**Target features:**
- Unit tests do módulo DDD (Aggregate Matricula, Value Objects, Domain Services) sem Spring — testabilidade pura do domínio
- Unit tests do módulo camadas (MatriculaServiceImpl) — evidenciando a dificuldade: mocks pesados, acoplamento implícito
- Documentação didática comparativa: por que um é mais difícil, o que o DDD tornou possível, analogias para o desenvolvedor

## Requirements

### Validated (v1.0 + v1.1)

- ✓ ESTR-01..06: Documentação estratégica — v1.0
- ✓ TAT-01..06: Design tático — v1.0
- ✓ MOD-01..04: Modelagem visual Mermaid — v1.0
- ✓ DOM-01..10: Camada de domínio — v1.0
- ✓ APL-01..05: Camada de aplicação — v1.0
- ✓ INF-01..07: Camada de infraestrutura — v1.0
- ✓ IFX-01..03: Camada de interface — v1.0
- ✓ DCK-01..02: Docker — v1.0
- ✓ DID-01..08: Material didático v1.0 — v1.0
- ✓ DIAG-01..06: 6 anti-padrões em código Java anotado — v1.1
- ✓ REFD-01..03: Refatoração DDD na arquitetura tradicional — v1.1
- ✓ DDD-01..05: Conceitos DDD introduzidos gradualmente — v1.1
- ✓ DID-01..03 (v1.1): Módulo completo antes/depois + análise final — v1.1

### Active (v1.2)

- [ ] TEST-01: Unit tests do Aggregate Matricula e Value Objects (módulo DDD)
- [ ] TEST-02: Unit tests do Domain Service VerificadorElegibilidade (módulo DDD)
- [ ] TEST-03: Unit tests do MatriculaServiceImpl (módulo camadas) — evidenciando mocks pesados
- [ ] TEST-04: Documentação didática comparativa de testabilidade

### Out of Scope (mantido)

- Contextos Financeiro, Acadêmico e Secretaria implementados
- Frontend / UI
- Autenticação e autorização
- Testes de integração com banco real (Testcontainers) — adiado para v1.3
- Deploy em cloud
- Lombok / MapStruct
- JPA / Spring Data

## Context

- **Audiência**: Desenvolvedores com experiência em Spring Boot, banco relacional e arquitetura em camadas; zero experiência estruturada com DDD
- **Estilo pedagógico**: Para cada conceito DDD introduzido, mostrar o paralelo com arquitetura tradicional. Explicar o que mudou, por que mudou, os benefícios e os trade-offs
- **Idioma do código**: Português — reforça a Linguagem Ubíqua do domínio
- **Estado técnico atual:** 2 módulos Maven, ~155 arquivos, 14.045+ linhas adicionadas no v1.1, BUILD SUCCESS, Docker Compose operacional
- **Uso esperado**: Referência autônoma — desenvolvedor estuda sem instrutor

## Constraints

- **Stack obrigatória**: Java 21, Spring Boot 3.x, MyBatis, PostgreSQL, Docker, Docker Compose, Maven — não negociável
- **MyBatis (não JPA)**: Mapeamento explícito reforça a separação domínio/persistência — ponto pedagógico central
- **Diagramas**: Todos em Mermaid
- **Documentação**: Markdown, em português
- **Bounded Context implementado**: Apenas Matrícula

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| MyBatis em vez de JPA/Hibernate | Mapeamento explícito evidencia a separação domínio/persistência | ✓ ADR-001 — Validado em produção |
| Código em português | Reforça Linguagem Ubíqua | ✓ ADR-004 — Validado |
| Apenas contexto Matrícula implementado | Complexidade sem ganho pedagógico | ✓ ADR-002 — Listeners stub suficientes |
| Referência por ID entre Aggregates | Demonstra padrão DDD sem acoplamento | ✓ ADR-003 — Schema sem FK cross-aggregate |
| Multi-module Maven (v1.1) | Dois sistemas completos lado a lado para comparação pedagógica | ✓ Exceção documentada — ponto central da Fase 5 |
| Aggregate Matrícula ignora vagas (v1) | Foco nas invariantes de Matrícula | — Tech debt reconhecido |
| Replace-all para persistência de coleções | Simplifica o Mapper sem lógica diff | ✓ Padrão documentado |

---

## Evolution

**Após cada milestone** (via `/gsd-complete-milestone`):
1. Revisão completa de todas as seções
2. Core Value check — ainda é a prioridade certa?
3. Auditoria de Out of Scope — motivos ainda válidos?
4. Atualizar Context com estado atual

---

<details>
<summary>v1.1 milestone goals (archived)</summary>

**Goal:** Criar módulo pedagógico "Fase 0" que demonstra a aplicação dos princípios fundamentais do DDD dentro da arquitetura em camadas tradicional (Controller→Service→Repository), sem introduzir Clean Architecture, Hexagonal Architecture ou padrões táticos avançados.

**Target features:**
- Diagnóstico de anti-padrões: exemplos Java com Service Anêmico, Entidade Anêmica, Service Deus, Duplicação de Regras, Regras na Interface e Acoplamento ao Banco
- Refatoração DDD dentro da mesma arquitetura: Application Service orquestrador + modelo de domínio rico
- Exercício de classificação: Regras de Domínio vs. Regras de Aplicação
- Entidades Ricas: modelo comportamental (pedido.finalizar()) vs. anêmico (pedido.setStatus())
- Introdução gradual de conceitos: Linguagem Ubíqua → Entidades → Value Objects → Agregados → Repositórios
- Análise final: "Quais benefícios obtivemos aplicando DDD sem alterar a arquitetura?"

</details>

*Last updated: 2026-06-23 — v1.2 milestone started*
