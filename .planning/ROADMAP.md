# Roadmap: ERP Matrícula — Projeto Didático DDD

**Generated:** 2026-06-20
**Phases:** 4
**Requirements:** 51 v1 requirements (ESTR-01..06, TAT-01..06, MOD-01..04, DOM-01..10, APL-01..05, INF-01..07, IFX-01..03, DCK-01..02, DID-01..08)

---

## Phases

- [ ] **Phase 1: Design Estrategico** — Domínio descoberto, linguagem ubíqua estabelecida, bounded contexts e context map documentados, decisões arquiteturais registradas
- [ ] **Phase 2: Design Tatico e Modelagem Visual** — Padrões táticos DDD documentados com justificativas pedagógicas; diagramas Mermaid do domínio completos
- [ ] **Phase 3: Implementacao** — Camadas de domínio, aplicação e infraestrutura implementadas; fluxo completo de matrícula executável
- [ ] **Phase 4: Interface, Docker e Material Didatico** — API REST funcional, ambiente Docker operacional, material comparativo e guias de referência completos

---

## Overview

| # | Phase | Goal | Requirements |
|---|-------|------|--------------|
| 1 | Design Estrategico | Documentar o problema de negócio, linguagem ubíqua, subdomínios e decisões arquiteturais que justificam a abordagem DDD | ESTR-01..06 |
| 2 | Design Tatico e Modelagem Visual | Documentar cada padrão tático DDD com justificativa pedagógica e produzir diagramas Mermaid do modelo de domínio | TAT-01..06, MOD-01..04 |
| 3 | Implementacao | Implementar as camadas de domínio, aplicação e infraestrutura com código Java 21 puro, casos de uso e persistência MyBatis | DOM-01..10, APL-01..05, INF-01..07 |
| 4 | Interface, Docker e Material Didatico | Expor a API REST, containerizar com Docker Compose e produzir o material comparativo e os guias de consulta | IFX-01..03, DCK-01..02, DID-01..08 |

---

## Phase Details

### Phase 1: Design Estrategico
**Goal:** O desenvolvedor consegue ler a documentação e entender qual problema de negócio DDD resolve, quais são os contextos do sistema, como eles se relacionam e quais decisões arquiteturais foram tomadas e por quê.
**Depends on:** Nothing (first phase)
**Requirements:** ESTR-01, ESTR-02, ESTR-03, ESTR-04, ESTR-05, ESTR-06
**Success Criteria** (what must be TRUE):
  1. Um desenvolvedor lendo o projeto encontra uma descrição clara do problema de negócio — quem são os usuários, o que o sistema resolve e quais são os fluxos principais
  2. Um desenvolvedor encontra um glossário de Linguagem Ubíqua com todos os termos do domínio (Aluno, Turma, Matrícula, PeriodoLetivo, Vaga, Responsável Financeiro) definidos com contexto e responsável
  3. Um desenvolvedor consegue classificar cada subdomínio (Core, Supporting, Generic) lendo a documentação com justificativa explícita de por que cada um pertence à categoria
  4. Um desenvolvedor visualiza o Context Map em Mermaid e compreende os limites do Bounded Context Matrícula, suas dependências downstream (Financeiro, Acadêmico) e os eventos que cruzam fronteiras
  5. Um desenvolvedor encontra todas as decisões arquiteturais com alternativas consideradas, vantagens, desvantagens e motivo da escolha final documentados
**Plans:** 3 planos em 2 waves

Plans:
**Wave 1**
- [x] 01-01-PLAN.md — README.md + problema-negocio.md + linguagem-ubiqua.md (ESTR-01, ESTR-02)
- [x] 01-02-PLAN.md — bounded-contexts.md + context-map.md (ESTR-03, ESTR-04, ESTR-05)

**Wave 2** *(blocked on Wave 1 completion)*
- [x] 01-03-PLAN.md — ADR-001 a ADR-004 (ESTR-06)

### Phase 2: Design Tatico e Modelagem Visual
**Goal:** O desenvolvedor compreende cada padrão tático DDD — Entidade, Value Object, Agregado, Domain Service, Domain Event, Repositório — através de documentação que explica o problema que cada padrão resolve, acompanhada de diagramas visuais do modelo de domínio.
**Depends on:** Phase 1
**Requirements:** TAT-01, TAT-02, TAT-03, TAT-04, TAT-05, TAT-06, MOD-01, MOD-02, MOD-03, MOD-04
**Success Criteria** (what must be TRUE):
  1. Um desenvolvedor lendo a documentação tática consegue explicar a diferença entre Entidade e Value Object, por que `Cpf` é um VO e por que `Aluno` é uma Entidade — sem ambiguidade
  2. Um desenvolvedor entende por que as invariantes de `Matricula` (limite de disciplinas, sem duplicidade, sem adição após cancelamento) pertencem ao Agregado e não a um Service
  3. Um desenvolvedor lendo a documentação de Domain Events consegue dizer quem publica `AlunoMatriculado`, quem o consome e em qual contexto — e entende por que isso cruza fronteiras
  4. Um desenvolvedor visualiza o diagrama de classes em Mermaid e consegue identificar todos os elementos do domínio e seus relacionamentos sem consultar código
  5. Um desenvolvedor acompanha o sequence diagram de "Realizar Matrícula" do início ao fim e consegue descrever cada passo do fluxo (HTTP → Controller → UseCase → Agregado → Repositório → Evento)
**Plans:** 4 planos em 3 waves

Plans:
**Wave 1** *(paralelos — sem dependência entre si)*
- [x] 02-01-PLAN.md — value-objects.md + entidades.md (TAT-01, TAT-02)

**Wave 2** *(paralelos — dependem de Wave 1)*
- [x] 02-02-PLAN.md — agregados.md (TAT-03)
- [x] 02-03-PLAN.md — domain-services.md + domain-events.md + repositorios.md (TAT-04, TAT-05, TAT-06)

**Wave 3** *(blocked on Wave 2 completion)*
- [x] 02-04-PLAN.md — modelagem.md + README.md update (MOD-01, MOD-02, MOD-03, MOD-04)

### Phase 3: Implementacao
**Goal:** O código Java 21 das camadas de domínio, aplicação e infraestrutura está implementado, compilando e executando os três fluxos de negócio (matricular, adicionar disciplina, cancelar) com banco PostgreSQL real.
**Depends on:** Phase 2
**Requirements:** DOM-01, DOM-02, DOM-03, DOM-04, DOM-05, DOM-06, DOM-07, DOM-08, DOM-09, DOM-10, APL-01, APL-02, APL-03, APL-04, APL-05, INF-01, INF-02, INF-03, INF-04, INF-05, INF-06, INF-07
**Success Criteria** (what must be TRUE):
  1. Um desenvolvedor executa `grep -r "import org.springframework" src/main/java/*/dominio/` e o resultado é vazio — domínio sem vazamento de framework
  2. Um desenvolvedor chama `matricula.adicionarDisciplina(disciplinaExcedente)` em um agregado com 6 disciplinas e recebe `LimiteDisciplinasExcedidoException` com o limite e o valor atual — sem nenhum `if` fora do agregado
  3. Um desenvolvedor executa os seeds Flyway e consegue chamar os três casos de uso (matricular, adicionar disciplina, cancelar) com dados reais persistidos no PostgreSQL
  4. Um desenvolvedor lê `MatriculaRowMapper.java` e consegue ver explicitamente a conversão entre modelo relacional (`MatriculaRow`) e modelo de domínio (`Matricula`) — sem magia de framework
  5. Os listeners stub `FinanceiroEventListener` e `AcademicoEventListener` recebem `AlunoMatriculado` após persistência — demonstrando integração via eventos sem acoplamento direto
**Plans:** TBD

### Phase 4: Interface, Docker e Material Didatico
**Goal:** O projeto está completo e operacional: API REST documentada expõe os três fluxos, Docker Compose sobe o ambiente com um comando, e o material didático comparativo transforma o código em lição compreensível para qualquer desenvolvedor vindo de arquitetura em camadas.
**Depends on:** Phase 3
**Requirements:** IFX-01, IFX-02, IFX-03, DCK-01, DCK-02, DID-01, DID-02, DID-03, DID-04, DID-05, DID-06, DID-07, DID-08
**Success Criteria** (what must be TRUE):
  1. Um desenvolvedor executa `docker compose up` e consegue fazer uma requisição HTTP de matrícula que persiste no banco — sem configuração manual além do comando
  2. Um desenvolvedor envia um payload inválido para o controller e recebe erro HTTP 422 com mensagem descritiva; envia matrícula duplicada e recebe 409 — com `@ControllerAdvice` mapeando exceções de domínio
  3. Um desenvolvedor lendo "DDD para quem vem da Arquitetura em Camadas" consegue identificar o equivalente DDD de cada artefato tradicional (Service → UseCase + DomainService, Repository interface + impl, Entity com comportamento) usando exemplos concretos do fluxo de matrícula
  4. Um desenvolvedor lendo os ADRs (ADR-001 a ADR-004) consegue explicar cada decisão arquitetural — MyBatis vs JPA, escopo único do BC, referência por ID entre agregados, código em português — com o problema original, a decisão tomada e o trade-off honesto
  5. Um desenvolvedor consulta o Guia de Consulta e navega diretamente do conceito DDD ("Aggregate Root") ao arquivo concreto (`Matricula.java`) sem precisar procurar na estrutura de pastas
**Plans:** TBD
**UI hint**: yes

---

## Progress Table

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Design Estrategico | 0/3 | Planned | - |
| 2. Design Tatico e Modelagem Visual | 0/4 | Planned | - |
| 3. Implementacao | 0/2 | Not started | - |
| 4. Interface, Docker e Material Didatico | 0/1 | Not started | - |
