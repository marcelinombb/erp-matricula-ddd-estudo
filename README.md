# ERP Matrícula — Projeto Didático DDD

## O que é este projeto

Projeto de treinamento completo em Domain-Driven Design (DDD), usando o domínio de Matrícula Escolar como campo de prática. Construído para desenvolvedores que dominam Spring Boot e arquitetura em camadas (Controller → Service → Repository) mas nunca aplicaram DDD de forma estruturada. O projeto é uma referência autônoma — se explica sem instrutor, através de documentação progressiva que acompanha cada decisão de código.

## Por onde começar

Siga esta sequência de leitura para construir o entendimento progressivo do projeto:

1. [Problema de Negócio](docs/01-design-estrategico/problema-negocio.md) — o que o sistema resolve, quem são os usuários e por que DDD é adequado para este domínio
2. [Linguagem Ubíqua](docs/01-design-estrategico/linguagem-ubiqua.md) — glossário compartilhado entre negócio e código, com os conceitos que aparecem no código Java
3. [Bounded Contexts](docs/01-design-estrategico/bounded-contexts.md) — os contextos do sistema, seus limites e responsabilidades
4. [Context Map](docs/01-design-estrategico/context-map.md) — como os contextos se relacionam e quais eventos cruzam suas fronteiras
5. [ADRs](docs/adrs/ADR-001-mybatis-vs-jpa.md) — as decisões arquiteturais tomadas e por quê
6. [Value Objects](docs/02-design-tatico/value-objects.md) — padrões táticos DDD: VOs, Entidades, Agregados, Serviços, Eventos e Repositórios (Fase 2)
7. [Modelagem Visual](docs/02-design-tatico/modelagem.md) — diagramas Mermaid do modelo de domínio completo

## Documentação por fase

### Fase 1: Design Estratégico

- [Problema de Negócio](docs/01-design-estrategico/problema-negocio.md)
- [Linguagem Ubíqua](docs/01-design-estrategico/linguagem-ubiqua.md)
- [Bounded Contexts](docs/01-design-estrategico/bounded-contexts.md)
- [Context Map](docs/01-design-estrategico/context-map.md)

### Decisões Arquiteturais (ADRs)

- [ADR-001: MyBatis vs JPA](docs/adrs/ADR-001-mybatis-vs-jpa.md)
- [ADR-002: Escopo do Bounded Context](docs/adrs/ADR-002-escopo-bounded-context.md)
- [ADR-003: Referência por ID entre Aggregates](docs/adrs/ADR-003-referencia-por-id.md)
- [ADR-004: Código em Português](docs/adrs/ADR-004-codigo-em-portugues.md)

## Design Tático

- [Value Objects](docs/02-design-tatico/value-objects.md)
- [Entidades](docs/02-design-tatico/entidades.md)
- [Agregados](docs/02-design-tatico/agregados.md)
- [Domain Services](docs/02-design-tatico/domain-services.md)
- [Domain Events](docs/02-design-tatico/domain-events.md)
- [Repositórios](docs/02-design-tatico/repositorios.md)
- [Modelagem Visual (Diagramas)](docs/02-design-tatico/modelagem.md)

## Stack técnico

- Java 21
- Spring Boot 3.5.3
- MyBatis 3.0.5 (mybatis-spring-boot-starter)
- PostgreSQL
- Docker
- Maven (single-module)

## Como executar

Instruções de execução serão adicionadas na Fase 3 (Implementação).
