# ADR-002: Implementar Apenas o Bounded Context de Matrícula

**Status:** Aceito
**Data:** 2026-06-20
**Contexto da fase:** Fase 1 — Design Estratégico

## Contexto

Um ERP escolar real teria múltiplos Bounded Contexts implementados: Matrícula, Financeiro, Acadêmico, Secretaria — cada um com seu modelo de domínio, suas tabelas e sua lógica de negócio. Este projeto implementa apenas o BC Matrícula, deixando Financeiro e Acadêmico como stubs. Esta decisão precisa ser justificada — do contrário, parece uma omissão ou preguiça de implementação.

O problema de implementar múltiplos BCs num projeto didático: integração real entre contextos exige infraestrutura de mensageria (Kafka, RabbitMQ) ou contratos de API REST entre serviços. Para que essa integração funcione de forma confiável, é preciso lidar com consistência eventual, retentativas, idempotência de handlers, and circuit breakers — problemas de infraestrutura distribuída que existem independentemente das regras de negócio. Se o objetivo é ensinar os padrões táticos do Core Domain, adicionar essa camada de complexidade antes que o domínio esteja claro é a sequência errada de aprendizado. O desenvolvedor não consegue aprender DDD e sistemas distribuídos ao mesmo tempo com a mesma atenção.

## Alternativas Consideradas

### Opção A: Implementar Financeiro Parcialmente Como Consumidor Real

**Prós:**
- Mais realista — o aluno veria consistência eventual real funcionando
- O evento `AlunoMatriculado` geraria um contrato financeiro concreto

**Contras:**
- Adiciona complexidade de infraestrutura antes do domínio estar consolidado
- O foco do aprendizado se divide entre DDD tático e integração entre contextos
- Os padrões táticos que Financeiro demonstraria são os mesmos que Matrícula já demonstra

### Opção B: Implementar via REST Entre Bounded Contexts como Serviços Separados

**Prós:**
- Arquitetura real de microserviços — mais próxima de produção
- Cada BC teria seu próprio processo e banco de dados

**Contras:**
- Adiciona complexidade HTTP, serialização/deserialização, timeouts, retentativas antes do domínio estar estável
- Exige infraestrutura de service discovery, load balancer ou pelo menos um `docker-compose` com 3+ serviços
- Contradiz a decisão de single-module Maven (CLAUDE.md) para manter a curva de entrada baixa

### Opção C: Implementar Financeiro e Acadêmico Completos

**Prós:**
- Projeto completo e realista
- O desenvolvedor vê os mesmos padrões aplicados em múltiplos contextos

**Contras:**
- Desvio de foco — os padrões táticos são os mesmos que Matrícula já demonstra. Duplicar implementação não acrescenta conceitos novos ao aprendizado de DDD
- Triplica o escopo de implementação sem triplicar o aprendizado
- Mantém o foco no Core Domain — Financeiro e Acadêmico são Supporting Domains; investimento de engenharia no Core tem maior retorno pedagógico

## Decisão

Apenas o BC Matrícula está implementado com toda a lógica de negócio. Financeiro e Acadêmico existem como **listeners stub** com `@TransactionalEventListener` que demonstram o contrato de integração sem implementar a lógica real dos contextos downstream.

```java
// Exemplo de stub — demonstra o contrato sem implementar lógica real
@Component
public class FinanceiroIntegracaoListener {

    @TransactionalEventListener
    public void aoAlunoMatriculado(AlunoMatriculadoEvent evento) {
        // STUB: Em produção, criaria contrato financeiro para o aluno
        // AlunoId alunoId = evento.alunoId();
        // PeriodoLetivo periodo = evento.periodo();
        // financeiroService.criarContrato(alunoId, periodo);
        log.info("BC Financeiro recebeu AlunoMatriculado para aluno {}", evento.alunoId());
    }
}
```

O stub serve como prova de que a integração existe — o contrato do evento está definido e um consumidor seria implementado aqui. O desenvolvedor que lê o stub entende o padrão sem precisar que a lógica financeira esteja implementada.

## Consequências

### Positivas

- Foco total nos padrões táticos do Core Domain — Aggregate, Value Object, Domain Events, Repository, Application Service — tudo demonstrado em profundidade dentro de Matrícula
- Sem overhead de infraestrutura de mensageria: o projeto roda com `docker-compose up` apontando apenas para o PostgreSQL
- O código de stub demonstra o padrão de integração via eventos sem poluir o código com lógica financeira/acadêmica não pedagógica
- Curva de aprendizado linear: o desenvolvedor domina os padrões táticos antes de enfrentar os desafios de integração entre contextos

### Negativas (Trade-offs)

- O desenvolvedor não vê consistência eventual real entre contextos — não experimenta o que acontece quando o evento falha na entrega ou quando o handler falha após receber
- Não experimenta o desafio de manter contratos de evento entre equipes — a evolução de um evento sem quebrar consumidores é uma habilidade real que este projeto não exercita
- Integração in-process com Spring `ApplicationEvents` (usado nos stubs) é fundamentalmente diferente de messaging real (Kafka/RabbitMQ) em produção: sem persistência de mensagens, sem replay, sem garantia de entrega at-least-once

## Referências

- Bounded Context — Martin Fowler: https://martinfowler.com/bliki/BoundedContext.html
- Strategic DDD: https://www.domainlanguage.com/ddd/reference/

## Na prática

Os dois stubs de integração demonstram o padrão de evento sem implementar a lógica real dos contextos downstream:

**[FinanceiroEventListener.java](../../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/eventos/FinanceiroEventListener.java)** — escuta `AlunoMatriculado` e `MatriculaCancelada` via `@TransactionalEventListener`. No método `aoMatricular()`, o Javadoc documenta a implementação real esperada: "criar contrato de cobrança para o período letivo."

**[AcademicoEventListener.java](../../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/eventos/AcademicoEventListener.java)** — escuta `AlunoMatriculado` e `DisciplinaAdicionada` via `@TransactionalEventListener`. No método `aoMatricular()`, o Javadoc documenta: "registrar vínculo aluno-turma no sistema acadêmico, gerar lista de chamada."

Os dois listeners são `@Component` com `@TransactionalEventListener`: recebem o evento após o commit da matrícula, sem que `Matricula.java` conheça a existência deles. A integração existe via evento, não via chamada direta. `Matricula.java` não tem nenhuma referência a `FinanceiroEventListener` ou `AcademicoEventListener` — zero acoplamento entre o Core Domain e os Supporting Domains.

O ponto pedagógico central: `@TransactionalEventListener` (default `AFTER_COMMIT`) garante que os listeners só executam se a transação do UseCase commitou com sucesso. O BC Financeiro nunca processa uma matrícula que ainda pode sofrer rollback.
