# Fase 0 — DDD sem mudar a arquitetura

Antes de introduzir qualquer conceito avançado de DDD, demonstre como aplicar os princípios fundamentais do Domain-Driven Design em uma arquitetura tradicional de mercado.

O objetivo desta fase é mostrar que DDD não depende de:

* Clean Architecture
* Hexagonal Architecture
* Onion Architecture
* CQRS
* Event Sourcing
* Ports & Adapters
* Mediator Pattern
* Use Cases separados por interface

O foco deve ser exclusivamente no modelo de domínio.

---

## Objetivo Pedagógico

A equipe está acostumada com a arquitetura:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Banco de Dados
```

Demonstrar que é possível obter benefícios significativos do DDD sem alterar essa estrutura.

Explicar que:

* DDD não é uma arquitetura.
* DDD não é um framework.
* DDD não exige novas camadas.
* DDD é uma abordagem para modelar o domínio de negócio.

---

## Diagnóstico da Arquitetura Tradicional

Antes de apresentar a solução, criar exemplos de código representando problemas comuns encontrados em sistemas corporativos.

Exemplos:

### Service Anêmico

Toda regra de negócio concentrada em Services.

### Entidades Anêmicas

Classes contendo apenas:

* atributos
* getters
* setters

sem comportamento.

### Services Deus

Classes contendo centenas ou milhares de linhas.

### Duplicação de Regras

Mesma validação espalhada em múltiplos Services.

### Regras Dependentes da Interface

Validações executadas apenas no Controller.

### Acoplamento ao Banco

Regras modeladas em função das tabelas e não do negócio.

---

## Refatoração para DDD

Utilizando exatamente a mesma arquitetura de camadas, demonstrar a evolução do modelo.

### Antes

```text
Controller
    ↓
Service (regras)
    ↓
Repository
```

### Depois

```text
Controller
    ↓
Application Service (orquestra)
    ↓
Domínio (regras)
    ↓
Repository
```

Explicar detalhadamente que a principal mudança não é arquitetural.

A principal mudança é a localização da lógica de negócio.

---

## Exercício de Identificação de Regras

Para cada funcionalidade implementada:

1. Identificar as regras existentes.
2. Classificar cada regra.
3. Justificar onde ela deve ficar.

Categorias:

### Regra de Aplicação

Exemplos:

* abrir transação
* enviar e-mail
* chamar API externa
* coordenar múltiplos agregados

### Regra de Domínio

Exemplos:

* pedido não pode ser finalizado sem itens
* matrícula exige vaga disponível
* nota deve estar entre 0 e 10

Demonstrar claramente a diferença.

---

## Entidades Ricas

Transformar entidades anêmicas em entidades comportamentais.

Mostrar:

### Modelo Anêmico

```java
pedido.setStatus(FECHADO);
```

### Modelo Rico

```java
pedido.finalizar();
```

Explicar:

* O que mudou.
* O que foi encapsulado.
* Qual regra passou a ser protegida.

---

## Introdução Gradual aos Conceitos de DDD

Apresentar os conceitos na seguinte ordem:

### 1. Linguagem Ubíqua

Identificar termos do negócio.

Demonstrar como os nomes das classes e métodos refletem a linguagem utilizada pelos especialistas.

---

### 2. Entidades

Explicar identidade e ciclo de vida.

---

### 3. Value Objects

Identificar objetos sem identidade.

Explicar imutabilidade.

Demonstrar quando substituir tipos primitivos por conceitos do domínio.

Exemplo:

```java
Email
CPF
Dinheiro
Periodo
```

---

### 4. Agregados

Somente após a equipe compreender entidades e regras.

Explicar:

* limites de consistência
* invariantes
* proteção do modelo

Evitar definições excessivamente acadêmicas.

Sempre utilizar exemplos concretos.

---

### 5. Repositórios

Mostrar que o repositório existe para recuperar agregados.

Evitar tratar repositórios como DAO genérico.

---

## O Que NÃO Fazer Nesta Fase

Não introduzir:

* Ports
* Adapters
* Input Ports
* Output Ports
* Gateways
* Mediators
* CQRS
* Event Sourcing
* Mensageria
* Arquitetura Hexagonal

a menos que exista uma justificativa explícita.

Caso algum desses conceitos seja utilizado posteriormente, explicar:

* qual problema resolve
* por que não era necessário inicialmente
* em que momento passou a agregar valor

---

## Critério de Sucesso

Ao final desta fase, a equipe deve compreender que:

DDD não consiste em adicionar novas camadas.

DDD consiste em construir um modelo de domínio capaz de representar corretamente as regras do negócio.

A principal evolução observada deve ser:

* Services menores
* Entidades mais ricas
* Regras centralizadas
* Menor duplicação
* Linguagem mais próxima do negócio

mesmo mantendo praticamente a mesma arquitetura utilizada antes da adoção do DDD.

---

## Pergunta Final Obrigatória

Antes de avançar para o Design Estratégico e Design Tático completos, produzir uma análise chamada:

"Quais benefícios obtivemos aplicando DDD sem alterar a arquitetura?"

Comparar:

* Complexidade introduzida
* Benefícios obtidos
* Curva de aprendizado
* Facilidade de adoção pela equipe

e justificar por que a próxima etapa do projeto realmente necessita dos conceitos mais avançados do DDD.
