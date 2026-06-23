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

### Fase 3: Implementação

Código Java do módulo DDD (`erp-matricula-ddd`) com anotações pedagógicas inline:

- [erp-matricula-ddd/src/…/domain/Matricula.java](erp-matricula-ddd/src/main/java/br/com/erp/matricula/domain/Matricula.java) — Aggregate Root com invariantes de domínio
- [erp-matricula-ddd/src/…/application/MatricularAlunoUseCase.java](erp-matricula-ddd/src/main/java/br/com/erp/matricula/application/MatricularAlunoUseCase.java) — Application Service orquestrador
- [erp-matricula-ddd/src/…/domain/MatriculaRepositorio.java](erp-matricula-ddd/src/main/java/br/com/erp/matricula/domain/MatriculaRepositorio.java) — porta de repositório (interface de domínio)

### Fase 4: Material Didático

- [DDD para quem vem da Arquitetura em Camadas](docs/04-material-didatico/ddd-vs-camadas.md) — comparação lado a lado: o que mudou, por que mudou e os benefícios de cada decisão
- [Guia de Consulta: conceito DDD → arquivo](docs/04-material-didatico/guia-consulta.md) — mapa de conceitos DDD para os arquivos concretos do projeto
- [Lições Aprendidas](docs/04-material-didatico/licoes-aprendidas.md) — decisões que pareceram estranhas no início e o raciocínio que as justifica
- [Estrutura de Pastas Explicada](docs/04-material-didatico/estrutura-pastas.md) — por que `domain/`, `application/`, `infrastructure/` e `interfaces/` existem

### Fase 5: Diagnóstico — Código com Anti-padrões

Módulo `erp-matricula-camadas` — o "antes" com os seis anti-padrões anotados:

- [Introdução ao módulo DDD sem mudar a arquitetura](docs/00-ddd-sem-mudar-arquitetura/00-introducao.md) — ponto de entrada para as Fases 5-7
- [Anti-padrão 1: Service Anêmico](docs/00-ddd-sem-mudar-arquitetura/01-service-anemico.md)
- [Anti-padrão 2: Entidade Anêmica](docs/00-ddd-sem-mudar-arquitetura/02-entidade-anemica.md)
- [Anti-padrão 3: Service Deus](docs/00-ddd-sem-mudar-arquitetura/03-service-deus.md)
- [Anti-padrão 4: Duplicação de Regras](docs/00-ddd-sem-mudar-arquitetura/04-duplicacao-regras.md)
- [Anti-padrão 5: Regras na Interface](docs/00-ddd-sem-mudar-arquitetura/05-regras-na-interface.md)
- [Anti-padrão 6: Acoplamento ao Banco](docs/00-ddd-sem-mudar-arquitetura/06-acoplamento-banco.md)

### Fase 6: Refatoração DDD na Arquitetura Tradicional

Conceitos DDD aplicados dentro do stack Controller→Service→Repository, com comparativo explícito ao módulo "antes":

- [Linguagem Ubíqua](docs/00-ddd-sem-mudar-arquitetura/07-linguagem-ubiqua.md) — como a nomenclatura do código reflete o domínio
- [Entidades](docs/00-ddd-sem-mudar-arquitetura/08-entidades.md) — identidade, ciclo de vida e diferença para Value Objects
- [Value Objects](docs/00-ddd-sem-mudar-arquitetura/09-value-objects.md) — imutabilidade e igualdade por valor
- [Agregados](docs/00-ddd-sem-mudar-arquitetura/10-agregados.md) — fronteira de consistência e Aggregate Root
- [Repositórios](docs/00-ddd-sem-mudar-arquitetura/11-repositorios.md) — porta de domínio vs. detalhe de infraestrutura
- [Guia de Leitura Comparativo](docs/00-ddd-sem-mudar-arquitetura/guia-leitura-comparativo.md) — percurso antes→depois com pontos de atenção por arquivo
- [Exercício de Classificação](docs/00-ddd-sem-mudar-arquitetura/exercicio-classificacao.md) — regras de negócio para classificar entre "de Domínio" e "de Aplicação"

### Fase 7: Análise Final e Balanço Didático

- [Análise Final](docs/00-ddd-sem-mudar-arquitetura/12-analise-final.md) — balanço crítico: complexidade introduzida, benefícios obtidos, quando vale a pena adotar DDD dentro da arquitetura em camadas

---

## Estrutura de módulos

O projeto tem dois módulos Maven que implementam o mesmo domínio de Matrícula Escolar de formas opostas:

| Módulo | Porta | Arquitetura | Descrição |
|--------|-------|-------------|-----------|
| `erp-matricula-ddd` | 8080 | DDD | O "depois" — domínio rico, UseCases, Value Objects, Aggregate Root |
| `erp-matricula-camadas` | 8081 | Camadas | O "antes" — Controller→Service→Repository com os 6 anti-padrões identificados |

Esta separação física (Maven multi-module) É o ponto pedagógico central da Fase 5: o desenvolvedor vê dois sistemas completos com o mesmo domínio resolvidos de formas opostas — mesmas requisições HTTP, mesmos dados no banco, decisões de design completamente diferentes.

## Stack técnico

- Java 21
- Spring Boot 3.5.3
- MyBatis 3.0.5 (mybatis-spring-boot-starter)
- PostgreSQL
- Docker
- Maven (multi-module — exceção documentada em pom.xml)

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

Aguarde as mensagens `Started ErpMatriculaApplication` (módulo DDD, porta 8080) e `Started ErpMatriculaCamadasApplication` (módulo camadas, porta 8081) no log.

```bash
# Módulo DDD (porta 8080) — arquitetura DDD com domínio rico
curl -s -X POST http://localhost:8080/matriculas \
  -H "Content-Type: application/json" \
  -d '{"alunoId": "00000000-0000-0000-0000-000000000001", "periodoLetivo": "2024.1"}' \
  | jq .

# Módulo Camadas (porta 8081) — arquitetura em camadas com anti-padrões
curl -s -X POST http://localhost:8081/matriculas \
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

