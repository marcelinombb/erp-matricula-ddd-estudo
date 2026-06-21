# DDD Aplicado ao Contexto de Matrícula Escolar

Este resumo percorre desde o **Strategic Design** até as **decisões de persistência**, usando o contexto de **Matrícula Escolar** como exemplo.

---

# 1. Strategic Design (Entendendo o Negócio)

Antes de criar entidades, tabelas ou APIs, é preciso entender o negócio.

## Objetivo

Responder:

```text
O que é o negócio?
Quais são suas áreas?
Onde estão os limites?
Quem é responsável por cada informação?
```

---

## Core Domain

É a parte que gera maior valor para a instituição.

Exemplo:

```text
ERP Escolar

Core Domain
└─ Matrícula

Supporting Domains
├─ Financeiro
├─ Acadêmico
└─ Secretaria

Generic Domains
├─ Autenticação
├─ Notificações
└─ E-mail
```

A matrícula costuma ser o coração da operação escolar.

Sem matrícula:

* não existe aluno ativo
* não existe cobrança
* não existe vínculo acadêmico

---

## Subdomains

Dividem o negócio.

```text
Matrícula
Financeiro
Acadêmico
Secretaria
```

Cada um possui regras próprias.

---

## Bounded Contexts

Definem fronteiras de responsabilidade.

```text
+----------------------+
| Matrícula Context    |
+----------------------+

+----------------------+
| Financeiro Context   |
+----------------------+

+----------------------+
| Acadêmico Context    |
+----------------------+
```

---

## O conceito de Aluno muda conforme o contexto

### Matrícula

```text
Aluno
- pode se matricular?
- está ativo?
- possui pendências?
```

### Financeiro

```text
Aluno
- possui débitos?
- possui bolsas?
```

### Acadêmico

```text
Aluno
- está cursando disciplinas?
- possui notas?
```

Mesmo nome.

Modelos diferentes.

---

## Context Map

Define comunicação entre contextos.

```text
Matrícula
   |
   +-------> Financeiro

Matrícula
   |
   +-------> Acadêmico
```

Exemplo:

Quando uma matrícula é concluída:

```text
AlunoMatriculado
```

evento publicado por Matrícula.

Consumido por:

```text
Financeiro
Acadêmico
```

---

# 2. Ubiquitous Language

Linguagem compartilhada entre especialistas e desenvolvedores.

Exemplo:

```text
Aluno
Turma
Período Letivo
Matrícula
Vaga
Responsável Financeiro
```

Evite nomes técnicos que não existem no negócio.

Ruim:

```text
StudentEntity
StudentDTO
```

Bom:

```text
Aluno
Matrícula
```

---

# 3. Tactical Design (Modelando o Domínio)

Agora entramos dentro do contexto Matrícula.

Pergunta principal:

```text
Como representar as regras do negócio?
```

---

# 4. Descobrindo as Regras

Exemplo:

```text
Aluno deve estar ativo.

Turma deve possuir vagas.

Período letivo deve estar aberto.

Não pode existir matrícula duplicada.

Matrícula cancelada não pode receber disciplinas.
```

Essas regras são mais importantes que as tabelas.

---

# 5. Entity

Possui identidade.

Exemplo:

```java
class Matricula {
    MatriculaId id;
}
```

Mesmo mudando seus atributos:

```text
status
turma
disciplinas
```

continua sendo a mesma matrícula.

---

# 6. Value Objects

Não possuem identidade.

Representam conceitos.

Exemplo:

```java
record CPF(String valor) {}
```

```java
record Endereco(
    String rua,
    String cidade
) {}
```

```java
record PeriodoLetivo(
    int ano,
    int semestre
) {}
```

Comparados por valor.

Geralmente imutáveis.

---

# 7. Aggregate

Grupo de objetos que precisam permanecer consistentes.

Exemplo:

```text
Matricula
│
├── AlunoId
├── TurmaId
├── Status
├── Disciplinas
└── PeriodoLetivo
```

---

## Invariantes protegidas pelo Aggregate

```text
Não matricular aluno inativo.

Não exceder vagas.

Não permitir disciplinas após cancelamento.
```

---

# 8. Aggregate Root

A porta de entrada.

```java
class Matricula {
    void adicionarDisciplina(...)
    void cancelar()
}
```

Nunca:

```java
disciplina.setCodigo(...)
```

Diretamente.

Tudo passa pela raiz.

---

# 9. Domain Services

Quando a regra não pertence claramente a uma entidade.

Exemplo:

```java
class ElegibilidadeMatriculaService
```

Responsável por verificar:

```text
Aluno ativo?
Período aberto?
Possui pendências?
```

---

# 10. Domain Events

Representam algo que aconteceu.

Exemplo:

```java
AlunoMatriculado
```

```java
MatriculaCancelada
```

```java
DisciplinaAdicionada
```

---

## Benefício

Permitem integração desacoplada.

```text
AlunoMatriculado
       |
       +-----> Financeiro
       |
       +-----> Acadêmico
```

---

# 11. Repository

Responsável por carregar e salvar Aggregates.

Exemplo:

```java
interface MatriculaRepository {
    Matricula buscarPorId(MatriculaId id);
    void salvar(Matricula matricula);
}
```

O domínio não conhece:

```text
PostgreSQL
JPA
Hibernate
MyBatis
```

---

# 12. Pensamento Correto Sobre Persistência

DDD NÃO diz:

```text
Entidade = Tabela
```

DDD diz:

```text
Domínio
 ↓
Modelo
 ↓
Persistência
```

---

# 13. Matrícula Não Precisa Virar Tabela

Muitos assumem:

```text
Matricula
↓
tb_matricula
```

Mas isso não é obrigatório.

---

## Possibilidade A

Tabela própria.

```sql
matricula
---------
id
aluno_id
turma_id
status
```

---

## Possibilidade B

Sem tabela matrícula.

```sql
aluno_periodo
-------------
aluno_id
periodo_id

aluno_disciplina
----------------
aluno_id
disciplina_id
```

O Aggregate Matrícula pode ser reconstruído a partir desses dados.

---

# 14. Value Objects Geralmente Não Viram Tabelas

Exemplo:

```java
record CPF(String valor)
```

Persistência:

```sql
cpf varchar(11)
```

e não:

```sql
cpf
id
valor
```

---

# 15. Relacionamentos Entre Aggregates

DDD recomenda referência por ID.

Evite:

```java
class Matricula {
    Aluno aluno;
}
```

Prefira:

```java
class Matricula {
    AlunoId alunoId;
}
```

---

## Banco

```sql
matricula
----------
id
aluno_id
```

Sem necessidade de carregar o Aggregate Aluno.

---

# 16. Modelo Relacional Possível

Exemplo simples:

```sql
aluno
-----
id
nome

turma
-----
id
descricao
vagas

matricula
----------
id
aluno_id
turma_id
status
periodo_letivo

matricula_disciplina
--------------------
matricula_id
disciplina_id
```

---

# 17. Modelo Orientado ao Aggregate

O foco não é:

```text
Quantas tabelas existem?
```

Mas:

```text
Consigo reconstruir uma Matrícula válida?
```

---

# 18. Banco Segue o Aggregate

Primeiro:

```text
Aggregate Matricula
```

Depois:

```text
Como persisti-lo?
```

Nunca o contrário.

---

# 19. Perguntas Que Devem Guiar a Modelagem

## Estratégicas

```text
O que é uma matrícula?

Quem é dono dela?

Qual contexto controla suas regras?

Quem pode alterá-la?

Quais contextos dependem dela?
```

---

## Táticas

```text
Quais regras devem ser protegidas?

O que é Entity?

O que é Value Object?

Qual Aggregate garante consistência?

Quais eventos devem ser publicados?
```

---

## Persistência

```text
Como reconstruir uma matrícula?

O Aggregate cabe em uma transação?

Posso armazenar esse Value Object como colunas?

Preciso realmente de uma tabela para esse conceito?
```

---

# Fluxo Completo

```text
1. Entender o negócio
   ↓
2. Identificar Core Domain
   ↓
3. Identificar Subdomains
   ↓
4. Criar Bounded Contexts
   ↓
5. Definir Ubiquitous Language
   ↓
6. Descobrir regras da matrícula
   ↓
7. Modelar Entities
   ↓
8. Modelar Value Objects
   ↓
9. Definir Aggregate Matricula
   ↓
10. Criar Domain Events
   ↓
11. Criar Repositories
   ↓
12. Escolher estratégia de persistência
   ↓
13. Modelar banco para suportar o Aggregate
```

A principal mudança de mentalidade é:

> **Em DDD você não começa desenhando tabelas de aluno, matrícula e turma.**
>
> Você começa entendendo o que significa uma matrícula para o negócio, quais regras ela protege e qual Aggregate é responsável por manter essas regras consistentes. Somente depois o banco é modelado para sustentar esse domínio.

