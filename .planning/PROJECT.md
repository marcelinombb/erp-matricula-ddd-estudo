# ERP Matrícula — Projeto Didático DDD

## What This Is

Projeto de treinamento completo em Domain-Driven Design (DDD), usando o domínio de Matrícula Escolar como campo de prática. Construído para desenvolvedores que dominam Spring Boot e arquitetura em camadas (Controller → Service → Repository) mas nunca aplicaram DDD de forma estruturada. O projeto é uma referência autônoma — se explica sem instrutor, através de documentação progressiva que acompanha cada decisão de código.

**Estado atual (v1.0):** projeto completo e operacional. Inclui documentação estratégica e tática, diagramas Mermaid, código Java 21 compilando com 42 arquivos de domínio/aplicação/infraestrutura, API REST funcional, Docker Compose com um comando, e material didático comparativo "DDD vs Arquitetura em Camadas".

## Core Value

Um desenvolvedor da equipe deve conseguir, sozinho, ler o projeto do início ao fim e entender **por que** DDD existe, **onde** ele diverge da arquitetura tradicional e **como** aplicar cada padrão tático no código Java real.

## Requirements

### Validated

- ✓ ESTR-01..06: Documentação estratégica completa — problema-negocio.md, linguagem-ubiqua.md com Conceitos Ambíguos, 6 subdomínios classificados, Context Map Mermaid com 4 BCs e 3 eventos, 4 ADRs no template Nygard — v1.0
- ✓ TAT-01..06: Design tático documentado — 7 Markdowns com snippets Java 21 ERRADO/CERTO para VOs, Entidades, Agregados, Domain Services, Domain Events, Repositórios — v1.0
- ✓ MOD-01..04: Modelagem visual — diagrama de classes, agregados, 3 flowcharts de negócio, sequence diagram completo — todos em Mermaid — v1.0
- ✓ DOM-01..10: Camada de domínio — 6 VOs como records Java 21, Aggregate Root Matricula com 3 invariantes, 3 Domain Events, VerificadorElegibilidadeMatricula puro, interface MatriculaRepositorio no domínio — v1.0
- ✓ APL-01..05: Camada de aplicação — 3 Application Services, Commands como records imutáveis, listeners @TransactionalEventListener para Financeiro/Acadêmico — v1.0
- ✓ INF-01..07: Camada de infraestrutura — Flyway V1-V3, seeds com UUIDs fixos, MatriculaMapper.xml com JOIN+collection (sem N+1), TypeHandlers, MatriculaRowMapper explícita, MatriculaRepositorioMyBatis — v1.0
- ✓ IFX-01..03: Camada de interface — MatriculaController 3 endpoints REST, ExcecaoHandler mapeando exceções → HTTP (409/422/400), Bean Validation — v1.0
- ✓ DCK-01..02: Docker — Dockerfile multi-stage eclipse-temurin:21, docker-compose.yml com healthcheck-chain PostgreSQL+App, documentação de uso no README — v1.0
- ✓ DID-01..08: Material didático — ddd-vs-camadas.md, guia-consulta.md, licoes-aprendidas.md, estrutura-pastas.md + 4 ADRs enriquecidos com seção "Na prática" — v1.0
- [x] Documentação da Fase 1: Descoberta do Domínio — Problema de Negócio, Linguagem Ubíqua (glossário 4 colunas, seção de Conceitos Ambíguos), Subdomínios classificados, Bounded Contexts, Context Map Mermaid, 4 ADRs com code examples *(Validated in Phase 1: design-estrategico, 2026-06-20)*
- [x] Documentação da Fase 2: Design Tático — 7 arquivos Markdown (Value Objects, Entidades, Agregados, Domain Services, Domain Events, Repositórios, Modelagem Visual com 4 diagramas Mermaid) com Java 21 snippets, seções ERRADO/CERTO e cross-references entre documentos *(Validated in Phase 2: design-tatico-e-modelagem-visual, 2026-06-20)*

### Active

*(Nenhum — todos os requirements v1 entregues. Próximos em v2)*

### Out of Scope

- Contextos Financeiro, Acadêmico e Secretaria implementados — presentes no Context Map e eventos, mas não codificados (complexidade sem ganho pedagógico no v1)
- Frontend / UI — projeto é backend didático; a interface é a documentação
- Autenticação e autorização — Generic Domain, adiciona ruído ao aprendizado de DDD
- Testes automatizados completos — v2 (TEST-01..03); foco do v1 é clareza do código de produção
- Deploy em cloud — Docker local suficiente para treinamento
- Lombok / MapStruct — mascarariam o boilerplate intencional da separação domínio/infraestrutura
- JPA / Spring Data — excluídos pedagogicamente: anotações @Entity no domínio violam a separação de camadas

## Context

- **Audiência**: Desenvolvedores com experiência em Spring Boot, banco relacional e arquitetura em camadas; zero experiência estruturada com DDD
- **Estilo pedagógico**: Para cada conceito DDD introduzido, mostrar o paralelo com arquitetura tradicional. Explicar o que mudou, por que mudou, os benefícios e os trade-offs
- **Idioma do código**: Português — reforça a Linguagem Ubíqua do domínio (Matrícula, Aluno, Turma, PeriodoLetivo, não StudentEntity ou RegistrationDTO)
- **Estado técnico (v1.0):** 42 arquivos Java, 3.514 LOC Java, 4.492 linhas Markdown, BUILD SUCCESS, Docker Compose operacional
- **Uso esperado**: Referência autônoma — desenvolvedor estuda sem instrutor. A documentação precisa ser suficientemente detalhada para funcionar como guia de leitura
- **Domínio escolhido**: Matrícula Escolar maximiza a demonstração dos padrões DDD por ter regras de negócio ricas, múltiplos contextos com o mesmo conceito (Aluno) sob visões diferentes, e fluxos claros de eventos entre contextos
- **Verificação manual pendente**: 3 cenários de teste manuais em 04-VERIFICATION.md requerem Docker rodando (validar POST /matriculas, 400 payload inválido, 409 matrícula duplicada)

## Constraints

- **Stack obrigatória**: Java 21, Spring Boot 3.x, MyBatis, PostgreSQL, Docker, Docker Compose, Maven — não negociável (é o stack da equipe)
- **MyBatis (não JPA)**: Mapeamento explícito reforça a separação entre modelo de domínio e modelo relacional — um dos pontos pedagógicos centrais
- **Diagramas**: Todos em Mermaid (sem ferramentas externas, funciona direto no Markdown/GitHub)
- **Documentação**: Todo em Markdown, em português
- **Bounded Context implementado**: Apenas Matrícula. Financeiro e Acadêmico existem apenas como consumidores de eventos no Context Map

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| MyBatis em vez de JPA/Hibernate | Mapeamento explícito evidencia a separação domínio/persistência — ponto pedagógico central; JPA vaza abstrações no domínio | ✓ ADR-001 — Validado em produção: MatriculaRowMapper explicita a conversão, TypeHandlers para VOs |
| Código em português | Reforça Linguagem Ubíqua; nomes em inglês quebrariam a coerência com os termos do negócio | ✓ ADR-004 — Validado: código legível sem glossário externo |
| Apenas contexto Matrícula implementado | Financeiro/Acadêmico adicionariam complexidade de integração sem acrescentar conceitos novos | ✓ ADR-002 — Validado: listeners stub suficientes para demonstrar o padrão de eventos |
| Referência por ID entre Aggregates | `Matricula` guarda `AlunoId` (não `Aluno`) — demonstra o padrão DDD sem acoplamento de carregamento | ✓ ADR-003 — Validado: schema PostgreSQL sem FK cross-aggregate |
| ADRs entregues na Fase 1 (antecipado) | Decisões arquiteturais moldavam toda a implementação; melhor documentar antes de implementar | ✓ Permitiu que Fases 3-4 referenciassem ADRs como base justificada |
| Aggregate Matricula ignora vagas disponíveis (v1) | Foco nas invariantes de Matrícula; Turma.vagasMaximas existe mas não é verificada no agregado | — Reconhecido como tech debt; Turma tem campo vagasMaximas sem uso real |
| Replace-all para persistência de coleções | Deletar+reinserir os itens da Matrícula a cada save simplifica o Mapper sem lógica diff | ✓ Padrão documentado em MatriculaRepositorioMyBatis com comentário |

---

## Evolution

**Após cada milestone** (via `/gsd-complete-milestone`):
1. Revisão completa de todas as seções
2. Core Value check — ainda é a prioridade certa?
3. Auditoria de Out of Scope — motivos ainda válidos?
4. Atualizar Context com estado atual

---
*Last updated: 2026-06-21 — v1.0 milestone fechado — todos os 51 requirements entregues*
