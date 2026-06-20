<!-- GSD:project-start source:PROJECT.md -->
## Project

**ERP Matrícula — Projeto Didático DDD**

Projeto de treinamento completo em Domain-Driven Design (DDD), usando o domínio de Matrícula Escolar como campo de prática. Construído para desenvolvedores que dominam Spring Boot e arquitetura em camadas (Controller → Service → Repository) mas nunca aplicaram DDD de forma estruturada. O projeto é uma referência autônoma — se explica sem instrutor, através de documentação progressiva que acompanha cada decisão de código.

**Core Value:** Um desenvolvedor da equipe deve conseguir, sozinho, ler o projeto do início ao fim e entender **por que** DDD existe, **onde** ele diverge da arquitetura tradicional e **como** aplicar cada padrão tático no código Java real.

### Constraints

- **Stack obrigatória**: Java 21, Spring Boot 3.x, MyBatis, PostgreSQL, Docker, Docker Compose, Maven — não negociável (é o stack da equipe)
- **MyBatis (não JPA)**: Mapeamento explícito reforça a separação entre modelo de domínio e modelo relacional — um dos pontos pedagógicos centrais
- **Diagramas**: Todos em Mermaid (sem ferramentas externas, funciona direto no Markdown/GitHub)
- **Documentação**: Todo em Markdown, em português
- **Bounded Context implementado**: Apenas Matrícula. Financeiro e Acadêmico existem apenas como consumidores de eventos no Context Map
<!-- GSD:project-end -->

<!-- GSD:stack-start source:research/STACK.md -->
## Technology Stack

## Java 21 para DDD
### Records como Value Objects
### Sealed Classes para Estados de Domínio
### Pattern Matching para Regras de Negócio
### Virtual Threads (Project Loom) — Relevância para Didático
## Spring Boot 3.x para DDD
### Estrutura de Pacotes: A Regra de Dependência
### Configuração Spring Boot: Evitando Vazamento de Infraestrutura
### Transaction Management
## MyBatis para persistência de Aggregates
### Padrão Fundamental: Aggregate como Unidade de Leitura
### Evitando N+1 com MyBatis
| Situação | Estratégia | Motivo |
|----------|-----------|--------|
| Buscar 1 Matrícula por ID | JOIN + `<collection>` nested results | Aggregate completo em 1 query |
| Listar Matrículas de 1 Aluno | JOIN + `<collection>` nested results | Mesma query, filtro diferente |
| Buscar só metadados (lista sem itens) | Query simples sem JOIN | Quando itens não são necessários |
| Persistir (INSERT/UPDATE) | Queries separadas na ordem certa | Aggregate root primeiro, filhos depois |
### TypeHandlers para Value Objects
### Persistência de Aggregate: Ordem de Operações
## PostgreSQL — Schema patterns
### Princípio: Uma tabela por Aggregate Root (mais tabelas de filhos)
### Por que `aluno_id UUID` em vez de `FOREIGN KEY REFERENCES alunos(id)`
### Value Objects como Colunas Inline
### Seeds para Demonstração
## Maven — Estrutura de módulos
### Recomendação: Single-Module com Pacotes bem Definidos
| Critério | Single-Module | Multi-Module |
|----------|--------------|-------------|
| Clareza pedagógica | ALTA — estrutura visível na árvore de pacotes | MÉDIA — split entre projetos confunde iniciantes |
| Curva de entrada | Baixa — um `mvn spring-boot:run` | Alta — ordem de build, módulos pai/filho |
| Enforçamento de dependências | Só via convenção (documentada) | Via compilação (mais robusto) |
| Adequado para time treinando | SIM | Não recomendado no início |
| Adequado para produção | NÃO (sem enforçamento real) | SIM |
### Estrutura pom.xml
## Recomendações
### Bibliotecas — Versões Verificadas
| Biblioteca | Versão | Fonte | Confiança |
|-----------|--------|-------|-----------|
| Spring Boot | 3.5.3 | Maven Central (2026-06-20) | ALTA |
| mybatis-spring-boot-starter | 3.0.5 | GitHub Releases + Maven Central | ALTA |
| MyBatis Core | 3.5.19 | Dependência transitiva do starter 3.0.5 | ALTA |
| Java | 21 LTS | Fixado no projeto | ALTA |
| PostgreSQL JDBC | 42.7.x | Gerenciado pelo Spring Boot BOM | ALTA |
| Flyway | 10.x | Gerenciado pelo Spring Boot BOM | ALTA |
| Docker Engine | 24+ | Requerido pelo Docker Compose v2 | MÉDIA |
### O que NÃO usar e por quê
| O que | Por que não |
|-------|------------|
| **JPA/Hibernate** | Vaza abstrações de persistência (`@Entity`, `@Id`, `@Column`) no modelo de domínio — contradiz o ponto pedagógico central do projeto |
| **Spring Data JPA** | Mesmo motivo. Repositórios que `extends JpaRepository` escondem a separação domínio/infraestrutura |
| **Spring Data JDBC** | Mais próximo de DDD que JPA, mas ainda impõe restrições ao modelo (anotações `@Table`, `@Id`). MyBatis é mais explícito e mais didático |
| **Lombok** | Esconde o boilerplate que o aluno precisa entender. Java 21 records eliminam a necessidade de Lombok para Value Objects |
| **MapStruct** | Geração de código de mapeamento esconde a conversão domínio/persistência que é explicitamente pedagógica aqui |
| **MyBatis-Plus** | Adiciona auto-CRUD que contradiz o mapeamento explícito do MyBatis puro — perde o ponto pedagógico |
| **Multi-module Maven** | Complexidade desnecessária para treinamento; usar pacotes bem definidos no single-module |
| **Liquibase** | Mais complexo que Flyway (XML/YAML changelogs) sem benefício proporcional para este projeto |
### Verificações de Confiança por Área
| Área | Confiança | Justificativa |
|------|-----------|---------------|
| Java 21 features (records, sealed, pattern matching) | ALTA | JEPs finalizados no Java 21 LTS; verificado em documentação JDK |
| Spring Boot 3.5.3 como versão corrente | ALTA | Maven Central consultado em 2026-06-20 |
| mybatis-spring-boot-starter 3.0.5 para Spring Boot 3.5 | ALTA | POM verificado no Maven Central; targets `spring-boot.version=3.5.0` |
| Padrão JOIN + ResultMap `<collection>` para aggregates | ALTA | Documentação oficial MyBatis (Context7) + padrão estabelecido |
| TypeHandlers para Value Objects | ALTA | Documentação oficial MyBatis |
| Schema PostgreSQL (design de tabelas) | ALTA | Padrões SQL padrão + princípios DDD verificados |
| Single-module vs multi-module para didático | MÉDIA | Consenso da comunidade; sem fonte única autoritativa |
| Estratégia replace-all para persistência de coleções | MÉDIA | Padrão prático comum; não documentado em especificação |
| Virtual Threads + Spring Boot 3.2+ | ALTA | Documentação Spring Boot; JEP 444 finalizado Java 21 |
### Referências
- MyBatis Result Maps (oficial): https://mybatis.org/mybatis-3/sqlmap-xml.html
- mybatis-spring-boot-starter releases: https://github.com/mybatis/spring-boot-starter/releases
- mybatis-spring-boot-autoconfigure: https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/
- Spring Boot 3.5 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes
- Separating Persistence and Domain Models: https://urgo.medium.com/separating-persistence-and-domain-models-cc3a7e7cd4e5
- DDD and Spring Boot Multi-Module Maven: https://dzone.com/articles/ddd-spring-boot-multi-module-maven-project
- Modern Java: Records, Sealed Classes, Pattern Matching: https://www.javacodegeeks.com/2025/12/modern-java-language-features-records-sealed-classes-pattern-matching.html
- Sealed Classes and Pattern Matching API Design: https://www.javacodegeeks.com/2026/04/sealed-classes-and-exhaustive-pattern-matching-how-they-change-api-design-not-just-syntax.html
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->
## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, `.github/skills/`, or `.codex/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
