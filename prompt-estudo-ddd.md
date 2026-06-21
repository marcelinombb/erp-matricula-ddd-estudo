# Objetivo

Você é um Arquiteto de Software especialista em Domain-Driven Design (DDD), Spring Boot, MyBatis, PostgreSQL e Docker.

Seu objetivo é criar um projeto didático completo que sirva como material de treinamento para uma equipe acostumada com arquitetura tradicional em camadas (Controller → Service → Repository).

O foco principal NÃO é apenas entregar código funcionando, mas demonstrar de forma prática como aplicar DDD estratégico e tático em um projeto real.

---

# Stack Tecnológica Obrigatória

* Java 21
* Spring Boot 3.x
* MyBatis
* PostgreSQL
* Docker
* Docker Compose
* Maven

---

# Domínio

Escolha um domínio que maximize a demonstração dos conceitos de DDD.

Sugestão preferencial: @contexto-matricula.md

O domínio deve possuir complexidade suficiente para demonstrar:

* Entidades
* Value Objects
* Agregados
* Aggregate Roots
* Regras de negócio
* Eventos de domínio
* Casos de uso
* Contextos delimitados (Bounded Contexts)
* Linguagem Ubíqua
* Invariantes
* Integração entre contextos

---

# Objetivo Pedagógico

A documentação deve ser escrita para desenvolvedores que:

* Conhecem Spring Boot
* Conhecem Banco de Dados Relacional
* Conhecem arquitetura em camadas tradicional
* Nunca utilizaram DDD de forma estruturada

Sempre que possível, faça paralelos entre:

Arquitetura Tradicional:

Controller
→ Service
→ Repository
→ Banco

e

DDD:

Application Layer
→ Domain Layer
→ Infrastructure Layer

Explicando:

* O que mudou
* Por que mudou
* Benefícios
* Trade-offs

---

# Fase 1 - Descoberta do Domínio

Documente:

## Problema de Negócio

* Qual problema o sistema resolve
* Quem são os usuários
* Quais são os principais fluxos

## Linguagem Ubíqua

Criar um glossário contendo:

* Termo
* Definição
* Responsável pelo termo

## Subdomínios

Classificar cada subdomínio como:

* Core Domain
* Supporting Domain
* Generic Domain

Justificar cada classificação.

---

# Fase 2 - Design Estratégico

Documentar detalhadamente:

## Bounded Contexts

Identificar todos os contextos.

Para cada contexto:

* Responsabilidades
* Limites
* Linguagem própria
* Dados próprios

## Context Map

Criar diagramas demonstrando:

* Relações entre contextos
* Dependências
* Fluxos de informação

Utilizar Mermaid.

## Decisões Arquiteturais

Registrar:

* Alternativas consideradas
* Vantagens
* Desvantagens
* Motivo da escolha final

---

# Fase 3 - Design Tático

Para cada Bounded Context:

## Entidades

Documentar:

* Identidade
* Ciclo de vida
* Responsabilidades

## Value Objects

Explicar:

* Motivo de serem Value Objects
* Imutabilidade
* Regras

## Agregados

Documentar:

* Aggregate Root
* Entidades internas
* Invariantes protegidas

## Domain Services

Quando utilizar.

Justificar por que a lógica não pertence a uma entidade.

## Domain Events

Documentar:

* Evento
* Gatilho
* Consumidores

## Repositórios

Definir interfaces de domínio.

Explicar por que elas pertencem ao domínio e não à infraestrutura.

---

# Fase 4 - Modelagem

Criar:

## Diagrama de Classes

Utilizando Mermaid.

## Diagrama de Agregados

Utilizando Mermaid.

## Fluxos de Negócio

Utilizando Mermaid Flowchart.

## Fluxos de Casos de Uso

Utilizando Mermaid Sequence Diagram.

---

# Fase 5 - Persistência

Projetar o banco de dados PostgreSQL.

Para cada tabela:

* Motivo da existência
* Relação com o modelo de domínio
* Trade-offs de persistência

Explicar claramente:

* Onde o modelo relacional diverge do modelo de domínio
* Por que essa divergência é aceitável

Gerar:

## Scripts SQL

* CREATE SCHEMA
* CREATE TABLE
* CONSTRAINTS
* INDEXES
* FOREIGN KEYS

## Seeds

Inserir dados suficientes para demonstração.

---

# Fase 6 - Implementação

Criar uma API mínima funcional.

## Estrutura de Pastas

Explicar cada pasta.

Exemplo:

domain/
application/
infrastructure/
interfaces/

## Camada de Domínio

Implementar:

* Entidades
* Value Objects
* Eventos
* Repositórios

## Camada de Aplicação

Implementar:

* Casos de uso
* DTOs
* Commands

## Camada de Infraestrutura

Implementar:

* MyBatis Mappers
* Configurações
* Implementações dos Repositórios

## Camada de Interface

Implementar:

* Controllers REST
* Validações
* Tratamento de erros

---

# Fase 7 - Docker

Criar:

docker-compose.yml

Com:

* PostgreSQL
* Aplicação

Documentar:

* Como subir
* Como derrubar
* Como resetar banco

---

# Fase 8 - Comparação com Arquitetura Tradicional

Criar uma seção específica chamada:

"DDD para quem vem da Arquitetura em Camadas"

Comparar:

* Controller
* Service
* Repository
* Entity

com:

* Application Service
* Domain Service
* Aggregate
* Repository Interface
* Repository Implementation

Mostrar exemplos concretos.

---

# Fase 9 - Registro das Decisões

Criar uma seção ADR (Architecture Decision Records).

Para cada decisão importante registrar:

* Contexto
* Opções consideradas
* Escolha
* Consequências

Exemplos:

* Por que MyBatis e não JPA
* Por que Aggregate X protege Y
* Por que determinado Bounded Context foi separado

---

# Fase 10 - Material de Consulta

Ao final gerar um guia resumido contendo:

## Conceitos DDD Utilizados

* Linguagem Ubíqua
* Entidade
* Value Object
* Agregado
* Evento de Domínio
* Repositório
* Bounded Context
* Context Map

## Onde aparecem no projeto

Mapear cada conceito para arquivos concretos.

## Lições Aprendidas

Explicar:

* O que seria feito em arquitetura tradicional
* O que foi feito em DDD
* Benefícios obtidos

---

# Regras Importantes

* Justifique TODAS as decisões.
* Nunca introduza um conceito de DDD sem explicar sua motivação.
* Sempre relacione conceitos abstratos com código concreto.
* Sempre explique os trade-offs.
* Utilize Mermaid para todos os diagramas.
* Produza documentação em Markdown.
* A documentação deve servir como material de treinamento para novos membros da equipe.
* O foco principal é ensinar DDD através da construção do sistema, e não apenas entregar código.

