# ERP Matrícula — Projeto Didático DDD

## O que é este projeto

Projeto de treinamento completo em Domain-Driven Design (DDD), usando o domínio de Matrícula Escolar como campo de prática. Construído para desenvolvedores que dominam Spring Boot e arquitetura em camadas (Controller → Service → Repository) mas nunca aplicaram DDD de forma estruturada. O projeto é uma referência autônoma — se explica sem instrutor, através de documentação progressiva que acompanha cada decisão de código.

### O que é Domínio?

Em desenvolvimento de software, **domínio** é o "coração" do negócio. Ele representa a área de conhecimento, os processos e as regras de negócio do mundo real que o sistema precisa resolver ou automatizar. Exemplo: em um banco, o domínio envolve contas, transferências e empréstimos.

O **Domain-Driven Design (DDD)** utiliza o domínio como guia principal para estruturar o código e o design da aplicação — o software reflete diretamente a linguagem e os conceitos do negócio.

### Como o Domínio é dividido no DDD?

Para lidar com sistemas complexos, o domínio maior é fragmentado em partes menores para manter o código organizado:

| Parte | O que é |
|-------|---------|
| **Domínio Central (Core Domain)** | O principal motivo da existência do sistema — o grande diferencial competitivo do negócio. É aqui que o time deve concentrar mais esforço e cuidado. |
| **Subdomínios de Suporte** | Áreas que apoiam o Core Domain, mas não são o diferencial. Podem ser desenvolvidos internamente com menos rigor. |
| **Subdomínios Genéricos** | Funcionalidades comuns a muitos negócios (ex: autenticação, envio de e-mail). Podem ser terceirizados ou usar soluções prontas. |

Neste projeto, o **Core Domain é Matrícula** — é ele que contém as regras de negócio modeladas com DDD tático.

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

#### Decisões Arquiteturais (ADRs)

- [ADR-001: MyBatis vs JPA](docs/adrs/ADR-001-mybatis-vs-jpa.md)
- [ADR-002: Escopo do Bounded Context](docs/adrs/ADR-002-escopo-bounded-context.md)
- [ADR-003: Referência por ID entre Aggregates](docs/adrs/ADR-003-referencia-por-id.md)
- [ADR-004: Código em Português](docs/adrs/ADR-004-codigo-em-portugues.md)

### Fase 2 Design Tático

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

## Como Executar (Docker)

**Pré-requisito:** [Docker](https://docs.docker.com/get-docker/) instalado (inclui Docker Compose). Não é necessário Java ou Maven no host.

### Subir o ambiente

```bash
docker compose up
```

Isso compila a aplicação dentro do container (Stage 1 do Dockerfile) e sobe PostgreSQL + Spring Boot. Na primeira execução, o download das imagens pode levar alguns minutos.

Para rodar em background:

```bash
docker compose up -d
```

### Testar que está funcionando

Aguarde a mensagem `Started ErpMatriculaApplication` no log e então faça uma requisição:

```bash
# Matricular um aluno (substitua o alunoId por um UUID válido dos seeds)
curl -s -X POST http://localhost:8080/matriculas \
  -H "Content-Type: application/json" \
  -d '{"alunoId": "00000000-0000-0000-0000-000000000001", "periodoLetivo": "2024.1"}' \
  | jq .
```

### Parar o ambiente

```bash
docker compose down
```

### Resetar o banco de dados (apagar todos os dados)

```bash
docker compose down -v
```

O flag `-v` remove o volume `postgres_data`, apagando todos os dados persistidos. Na próxima vez que subir, o Flyway recria o schema do zero.

---

## Material Didático

Documentação pedagógica da Fase 4 — explica DDD na prática, com paralelos diretos à arquitetura em camadas:

- [DDD para quem vem da Arquitetura em Camadas](docs/04-material-didatico/ddd-vs-camadas.md) — comparação lado a lado: o que mudou, por que mudou e os benefícios de cada decisão
- [Guia de Consulta: conceito DDD → arquivo](docs/04-material-didatico/guia-consulta.md) — mapa de conceitos DDD (Aggregate, Value Object, Repository...) para os arquivos concretos do projeto
- [Lições Aprendidas](docs/04-material-didatico/licoes-aprendidas.md) — decisões que pareceram estranhas no início e o raciocínio que as justifica
- [Estrutura de Pastas Explicada](docs/04-material-didatico/estrutura-pastas.md) — por que `domain/`, `application/`, `infrastructure/` e `interfaces/` existem e o que cada uma pode ou não pode fazer
