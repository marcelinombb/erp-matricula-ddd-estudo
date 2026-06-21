# Requirements: ERP Matrícula — v1.1 DDD sem Mudar a Arquitetura

**Defined:** 2026-06-21
**Core Value:** Um desenvolvedor da equipe deve conseguir, sozinho, ler o projeto do início ao fim e entender **por que** DDD existe, **onde** ele diverge da arquitetura tradicional e **como** aplicar cada padrão tático no código Java real.

## v1.1 Requirements

Módulo pedagógico "Fase 0": demonstrar DDD aplicado na arquitetura tradicional Controller→Service→Repository sem introduzir arquiteturas avançadas.

### Diagnóstico de Anti-padrões

- [ ] **DIAG-01**: Desenvolvedor identifica anti-padrão "Service Anêmico" em código Java exemplo com todas as regras de negócio concentradas no Service
- [ ] **DIAG-02**: Desenvolvedor identifica anti-padrão "Entidade Anêmica" em código Java exemplo com apenas atributos, getters e setters sem comportamento
- [ ] **DIAG-03**: Desenvolvedor identifica anti-padrão "Service Deus" em código Java exemplo com centenas de linhas de regras misturadas
- [ ] **DIAG-04**: Desenvolvedor identifica anti-padrão "Duplicação de Regras" em código Java exemplo com mesma validação espalhada em múltiplos Services
- [ ] **DIAG-05**: Desenvolvedor identifica anti-padrão "Regras na Interface" em código Java exemplo com validações executadas apenas no Controller
- [ ] **DIAG-06**: Desenvolvedor identifica anti-padrão "Acoplamento ao Banco" em código Java exemplo com regras modeladas em função das tabelas

### Refatoração DDD na Arquitetura Tradicional

- [ ] **REFD-01**: Desenvolvedor visualiza diferença entre Service anêmico (contém regras) e Application Service orquestrador (delega ao domínio) na mesma arquitetura Controller→Service→Repository
- [ ] **REFD-02**: Desenvolvedor visualiza entidade rica com comportamento (`pedido.finalizar()`) vs. anêmica (`pedido.setStatus(FECHADO)`) e compreende o que foi encapsulado e qual regra passou a ser protegida
- [ ] **REFD-03**: Desenvolvedor classifica corretamente regras como "de Domínio" (matrícula exige vaga disponível, nota entre 0 e 10) ou "de Aplicação" (abrir transação, enviar e-mail, coordenar agregados)

### Conceitos DDD Introduzidos Gradualmente

- [ ] **DDD-01**: Projeto demonstra Linguagem Ubíqua — nomes de classes e métodos em código Java refletem termos utilizados pelos especialistas do domínio
- [ ] **DDD-02**: Projeto demonstra identidade e ciclo de vida de Entidades com exemplos concretos do domínio de matrícula
- [ ] **DDD-03**: Projeto demonstra Value Objects imutáveis (Email, CPF, Dinheiro, Periodo) como alternativa a tipos primitivos, com imutabilidade explicada
- [ ] **DDD-04**: Projeto demonstra Agregados como limites de consistência e proteção de invariantes com exemplos concretos — sem definições excessivamente acadêmicas
- [ ] **DDD-05**: Projeto demonstra Repositórios como recuperadores de Agregados, diferenciando do DAO genérico, dentro da arquitetura tradicional

### Material Didático

- [ ] **DID-01**: Módulo apresenta código Java completo "antes" — arquitetura tradicional com anti-padrões identificados e anotados com comentários explicativos
- [ ] **DID-02**: Módulo apresenta código Java completo "depois" — DDD aplicado na mesma arquitetura com comparativo explícito, mostrando o que mudou, o que foi encapsulado e qual regra passou a ser protegida
- [ ] **DID-03**: Documento de análise final "Quais benefícios obtivemos aplicando DDD sem alterar a arquitetura?" comparando Complexidade introduzida, Benefícios obtidos, Curva de aprendizado e Facilidade de adoção pela equipe

## Futuro (v1.2+)

### Exercícios Interativos

- **EX-01**: Exercícios de refatoração guiada para o desenvolvedor praticar a transformação por conta própria
- **EX-02**: Kata de código: partir de um Service Deus e chegar no modelo rico passo a passo

### Extensão para Outros Contextos

- **EXT-01**: Demonstrar os mesmos anti-padrões e refatorações no contexto Financeiro
- **EXT-02**: Comparação entre os 6 subdomínios e como DDD se aplica diferente em cada um

## Out of Scope

| Feature | Reason |
|---------|--------|
| Clean Architecture, Hexagonal, Onion | Objetivo explícito da fase é demonstrar DDD SEM essas arquiteturas — introduzi-las confundiria a mensagem pedagógica |
| CQRS, Event Sourcing, Mediator Pattern | Mesmo motivo — fora do escopo da "Fase 0" |
| Ports & Adapters, Input/Output Ports, Gateways | Explicitamente fora do escopo conforme `ddd-sem-mudar-arquitetura.md` |
| Testes automatizados | Coberto no backlog (TEST-01..03 da v1.0) — não interfere com este módulo pedagógico |
| Outros Bounded Contexts implementados | Financeiro/Acadêmico como BCs completos adicionam complexidade sem ganho pedagógico na "Fase 0" |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| DIAG-01 | Phase 5 | Pending |
| DIAG-02 | Phase 5 | Pending |
| DIAG-03 | Phase 5 | Pending |
| DIAG-04 | Phase 5 | Pending |
| DIAG-05 | Phase 5 | Pending |
| DIAG-06 | Phase 5 | Pending |
| REFD-01 | Phase 6 | Pending |
| REFD-02 | Phase 6 | Pending |
| REFD-03 | Phase 6 | Pending |
| DDD-01 | Phase 6 | Pending |
| DDD-02 | Phase 6 | Pending |
| DDD-03 | Phase 6 | Pending |
| DDD-04 | Phase 6 | Pending |
| DDD-05 | Phase 6 | Pending |
| DID-01 | Phase 5 | Pending |
| DID-02 | Phase 6 | Pending |
| DID-03 | Phase 7 | Pending |

**Coverage:**
- v1.1 requirements: 17 total
- Mapped to phases: 17
- Unmapped: 0 ✓

---
*Requirements defined: 2026-06-21*
*Last updated: 2026-06-21 after initial definition for milestone v1.1*
