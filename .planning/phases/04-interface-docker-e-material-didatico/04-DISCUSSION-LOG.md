# Phase 4: Interface, Docker e Material Didatico - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-20
**Phase:** 04-interface-docker-e-material-didatico
**Areas discussed:** Design dos Endpoints REST, Resposta de Erro HTTP, Docker Compose, Organização do Material Didático

---

## Design dos Endpoints REST

### Cancelar matrícula — verbo/URL

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| POST /matriculas/{id}/cancelamento | Sub-recurso da matrícula; consistente com /disciplinas | ✓ |
| PATCH /matriculas/{id} | Atualização parcial genérica com {status: CANCELADA} | |
| DELETE /matriculas/{id} | DELETE semântico HTTP — mas matrícula não é deletada do banco | |

**Escolha:** POST /matriculas/{id}/cancelamento
**Notas:** Padrão consistente com POST /matriculas/{id}/disciplinas — ações de mudança de estado como sub-recursos.

---

### Payload de criação de matrícula (POST /matriculas)

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| alunoId + turmaId + periodoLetivo | Mínimo para MatricularAlunoCommand; disciplinas adicionadas depois | ✓ |
| alunoId + turmaId + periodoLetivo + disciplinas[] | Cria já com disciplinas; mistura dois use cases | |

**Escolha:** alunoId + turmaId + periodoInicio + periodoFim
**Notas:** Mapeia diretamente para MatricularAlunoCommand existente sem acoplamento.

---

### Nome do pacote da camada de interface

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| interfaces/ | Espelha terminologia DDD "Interface Layer" | ✓ |
| web/ ou rest/ | Mais descritivo do protocolo, menos DDD | |

**Escolha:** interfaces/

---

### Campos de disciplinas no MatriculaDto

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Lista plana ["Matemática"] | Simples; NomeDisciplina é VO sem estado adicional | |
| Lista de objetos [{nome, adicionadaEm}] | Mais rico; requer nova coluna adicionada_em no banco | ✓ |

**Escolha:** Lista de objetos [{nome, adicionadaEm}]
**Notas:** Requer Flyway V3 com `ALTER TABLE itens_matricula ADD COLUMN adicionada_em TIMESTAMP NOT NULL DEFAULT NOW()`.

---

## Resposta de Erro HTTP

### Formato base do JSON de erro

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Simples {erro, mensagem} | Direto, didático, campos extras quando necessário | ✓ |
| RFC 7807 Problem Details | Padrão moderno mas verboso; adiciona complexidade sem ganho pedagógico | |

**Escolha:** {erro: "SNAKE_CASE_UPPER", mensagem: "texto legível"} + campos estruturados extras quando a exceção os possui.

---

### Código HTTP para MatriculaNaoEncontradaException

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| 404 Not Found | Semântica HTTP correta para recurso ausente | ✓ |
| 422 Unprocessable | Trata como validação; perde semântica diferenciada | |

**Escolha:** 404 Not Found

---

### Bean Validation — código e formato

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| 400 Bad Request + lista de campos | Semanticamente correto; diferencia dado malformado de invariante de domínio | ✓ |
| 422 igual às invariantes de domínio | Simplifica o @ControllerAdvice mas mistura semânticas | |

**Escolha:** 400 Bad Request com `{erro: "DADOS_INVALIDOS", mensagem, campos: [{campo, mensagem}]}`

---

## Docker Compose

### Estratégia do Dockerfile

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Multi-stage: Maven build + JRE runtime | Autônomo; mostra padrão de produção; docker compose up funciona direto | ✓ |
| Cópia simples do JAR pré-compilado | Mais simples mas requer mvn package prévio | |

**Escolha:** Multi-stage com eclipse-temurin:21-jdk-alpine (build) + eclipse-temurin:21-jre-alpine (runtime)

---

### Estratégia de startup ordering

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| depends_on com healthcheck pg_isready | Docker Compose nativo; sem scripts externos | ✓ |
| Retry logic na aplicação | Funciona mas esconde orquestração no código Java | |

**Escolha:** depends_on com condition: service_healthy + healthcheck pg_isready no serviço postgres

---

### Credenciais no docker-compose.yml

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Variáveis de ambiente com defaults ${DB_USER:-matricula} | Prática de produção; funciona sem .env | ✓ |
| Hardcoded no compose | Mais simples; adequado para didático | |

**Escolha:** Variáveis com defaults — mostra a prática correta mesmo em projeto didático

---

## Organização do Material Didático

### O que fazer com os ADRs existentes (DID-02..05)

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Enriquecer os ADRs existentes com seção "Na prática" | Sem duplicação; ADRs ganham links para código real | ✓ |
| Criar docs/04-material-didatico/ com novos arquivos | Mais separação; ADRs originais ficam intocados mas cria duplicação | |

**Escolha:** Enriquecer ADR-001..004 com seção `## Na prática` linkando arquivos implementados

---

### Localização de DID-01, DID-07 e DID-08

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| docs/04-material-didatico/ com 3 arquivos | Pasta dedicada; paralelo com docs/01, docs/02 | ✓ |
| Integrado no README.md da raiz | Mais acessível mas README fica muito longo | |

**Escolha:** docs/04-material-didatico/ com ddd-vs-camadas.md, licoes-aprendidas.md, estrutura-pastas.md

---

### Formato do Guia de Consulta (DID-06)

| Opção | Descrição | Selecionada |
|-------|-----------|-------------|
| Tabela Markdown em guia-consulta.md | Fácil de navegar, renderiza bem no GitHub | ✓ |
| Seções narrativas por conceito | Mais rico mas difícil de usar como referência rápida | |

**Escolha:** Tabela `| Conceito DDD | Arquivo | O que observar |` em docs/04-material-didatico/guia-consulta.md

---

## Claude's Discretion

- Porta exposta no docker-compose.yml (8080 recomendado — default Spring Boot)
- Estratégia de datasource local vs Docker: SPRING_DATASOURCE_URL env var sobrescreve application.yml automaticamente — sem profiles Spring
- Conteúdo exato do Guia de Consulta: cobrir todos os padrões táticos + padrões de infraestrutura

## Deferred Ideas

- Documentação OpenAPI/Swagger — não nos requisitos v1; pode ser v2
- Testes automatizados — fora do escopo v1 (TEST-01..03 são v2 requirements)
- Optimistic locking completo — apenas mencionado nas Lições Aprendidas (PROD-01 é v2)
