# Requirements: ERP Matrícula — Projeto Didático DDD

**Defined:** 2026-06-20
**Core Value:** Um desenvolvedor deve conseguir, sozinho, ler o projeto do início ao fim e entender por que DDD existe, onde diverge da arquitetura tradicional e como aplicar cada padrão tático em código Java real.

---

## v1 Requirements

### Documentação Estratégica

- [ ] **ESTR-01**: O projeto documenta o problema de negócio da matrícula escolar — o que o sistema resolve, quem são os usuários e quais são os principais fluxos
- [ ] **ESTR-02**: O projeto possui Glossário de Linguagem Ubíqua com termo, definição e responsável para cada conceito do domínio (Aluno, Turma, Matrícula, Vaga, PeriodoLetivo, Responsável Financeiro)
- [ ] **ESTR-03**: Os subdomínios são classificados e justificados como Core Domain (Matrícula), Supporting Domain (Financeiro, Acadêmico, Secretaria) ou Generic Domain (Autenticação, Notificações)
- [ ] **ESTR-04**: O projeto documenta todos os Bounded Contexts com suas responsabilidades, limites, linguagem própria e dados próprios
- [ ] **ESTR-05**: O projeto possui Context Map em Mermaid mostrando relações entre contextos, dependências e fluxos de eventos (Matrícula → Financeiro, Matrícula → Acadêmico via AlunoMatriculado)
- [ ] **ESTR-06**: As decisões arquiteturais estão documentadas com alternativas consideradas, vantagens, desvantagens e motivo da escolha final

### Documentação Tática

- [ ] **TAT-01**: Cada Entidade documentada com identidade, ciclo de vida e responsabilidades
- [ ] **TAT-02**: Cada Value Object documentado explicando motivo de ser VO, imutabilidade e regras de validação
- [ ] **TAT-03**: Cada Agregado documentado com Aggregate Root, entidades internas e invariantes protegidas
- [ ] **TAT-04**: Domain Services documentados justificando por que a lógica não pertence a uma entidade
- [ ] **TAT-05**: Domain Events documentados com evento, gatilho e consumidores
- [ ] **TAT-06**: Repositórios documentados como interfaces de domínio, explicando por que pertencem ao domínio e não à infraestrutura

### Modelagem Visual

- [ ] **MOD-01**: Diagrama de classes do domínio em Mermaid
- [ ] **MOD-02**: Diagrama de agregados em Mermaid
- [ ] **MOD-03**: Fluxos de negócio em Mermaid Flowchart (ao menos: Realizar Matrícula, Adicionar Disciplina, Cancelar Matrícula)
- [ ] **MOD-04**: Sequence diagrams dos casos de uso em Mermaid (ao menos: Realizar Matrícula completo)

### Camada de Domínio

- [ ] **DOM-01**: Value Objects implementados como Java 21 records com validação no construtor compacto — `Cpf`, `PeriodoLetivo`, `MatriculaId`, `AlunoId`, `TurmaId`, `NomeDisciplina`
- [ ] **DOM-02**: Entidade `Aluno` com `AlunoId` tipado; `equals`/`hashCode` por identidade
- [ ] **DOM-03**: Entidade `Turma` com `TurmaId` tipado e capacidade máxima de vagas
- [ ] **DOM-04**: Aggregate Root `Matricula` com invariantes encapsuladas: limite de disciplinas, não duplicar disciplina, não adicionar disciplina após cancelamento
- [ ] **DOM-05**: `StatusMatricula` implementado como sealed interface ou enum com transições de estado documentadas
- [ ] **DOM-06**: Domain Events implementados como records imutáveis: `AlunoMatriculado`, `DisciplinaAdicionada`, `MatriculaCancelada`
- [ ] **DOM-07**: Mecanismo de coleta de eventos no agregado (`coletarEventos()`) sem dependência do Spring
- [ ] **DOM-08**: Domain Service `VerificadorElegibilidadeMatricula` verificando: aluno ativo, período letivo aberto, ausência de matrícula duplicada no período
- [ ] **DOM-09**: Interface `MatriculaRepositorio` declarada no pacote `dominio/` — sem qualquer import de framework
- [ ] **DOM-10**: Exceções de domínio tipadas com contexto: `LimiteDisciplinasExcedidoException`, `DisciplinaJaMatriculadaException`, `MatriculaCanceladaException`, `AlunoInativoException`

### Camada de Aplicação

- [ ] **APL-01**: Caso de uso `MatricularAlunoUseCase` orquestrando: validar elegibilidade → criar Matrícula → salvar → publicar eventos
- [ ] **APL-02**: Caso de uso `AdicionarDisciplinaUseCase` orquestrando: buscar Matrícula → adicionar disciplina (Aggregate decide) → salvar → publicar eventos
- [ ] **APL-03**: Caso de uso `CancelarMatriculaUseCase` orquestrando: buscar Matrícula → cancelar (Aggregate decide) → salvar → publicar eventos
- [ ] **APL-04**: DTOs e Commands para entrada/saída de cada caso de uso
- [ ] **APL-05**: Listeners stub com `@TransactionalEventListener` para `Financeiro` e `Acadêmico` demonstrando integração via eventos

### Camada de Infraestrutura

- [ ] **INF-01**: Schema PostgreSQL completo com CREATE SCHEMA, CREATE TABLE, CONSTRAINTS, INDEXES e FOREIGN KEYS — com comentários explicando relação com o modelo de domínio
- [ ] **INF-02**: Flyway migrations para aplicar o schema de forma reproduzível
- [ ] **INF-03**: Seeds com dados suficientes para demonstrar os três fluxos (matrícula, adição de disciplina, cancelamento)
- [ ] **INF-04**: `MatriculaMapper.xml` com ResultMap usando JOIN + `<collection notNullColumn>` para reconstrução do agregado sem N+1
- [ ] **INF-05**: TypeHandlers MyBatis para Value Objects (`CpfTypeHandler`, `PeriodoLetivoTypeHandler`)
- [ ] **INF-06**: `MatriculaRowMapper` — conversão explícita entre modelo relacional (`MatriculaRow`) e modelo de domínio (`Matricula`)
- [ ] **INF-07**: `MatriculaRepositorioMyBatis` implementando `MatriculaRepositorio` do domínio

### Camada de Interface

- [ ] **IFX-01**: Controller REST `MatriculaController` com endpoints para os três casos de uso
- [ ] **IFX-02**: `@ControllerAdvice` mapeando exceções de domínio para respostas HTTP semânticas (409 para conflitos, 422 para violações de invariante)
- [ ] **IFX-03**: Validações de entrada nos request bodies (Bean Validation)

### Docker

- [ ] **DCK-01**: `docker-compose.yml` com PostgreSQL e Aplicação configurados e funcionando
- [ ] **DCK-02**: Documentação de uso: como subir, como derrubar, como resetar banco

### Material Didático

- [ ] **DID-01**: Seção "DDD para quem vem da Arquitetura em Camadas" — comparação lado a lado entre Controller/Service/Repository/Entity e ApplicationService/DomainService/Aggregate/RepositoryInterface/RepositoryImpl com exemplos concretos do fluxo de matrícula
- [ ] **DID-02**: ADR-001: Por que MyBatis e não JPA/Hibernate (com código antes/depois mostrando o problema de anotações de persistência em entidades de domínio)
- [ ] **DID-03**: ADR-002: Por que somente o Bounded Context de Matrícula foi implementado
- [ ] **DID-04**: ADR-003: Por que referência por ID entre Aggregates (`AlunoId` e não `Aluno`)
- [ ] **DID-05**: ADR-004: Por que o código está em português
- [ ] **DID-06**: Guia de Consulta mapeando cada conceito DDD para arquivos concretos do projeto (ex: "Aggregate Root → `Matricula.java`")
- [ ] **DID-07**: Seção "Lições Aprendidas" explicando o que seria feito em arquitetura tradicional vs o que foi feito em DDD, com benefícios e trade-offs honestos
- [ ] **DID-08**: Estrutura de pastas documentada com explicação do propósito de cada diretório e por que ele existe ali

---

## v2 Requirements

### Testes Automatizados

- **TEST-01**: Testes unitários do Aggregate `Matricula` cobrindo todas as invariantes
- **TEST-02**: Testes de integração dos casos de uso com banco real (TestContainers)
- **TEST-03**: ArchUnit enforçando regras de dependência entre camadas

### Qualidade de Produção

- **PROD-01**: Optimistic locking com coluna `version` para concorrência em agregados
- **PROD-02**: Observabilidade (Actuator + Micrometer)
- **PROD-03**: Paginação nas queries de listagem

### Bounded Contexts Adicionais

- **BC-01**: Implementação parcial do contexto Financeiro como consumidor real de `AlunoMatriculado`
- **BC-02**: Implementação parcial do contexto Acadêmico como consumidor real de `AlunoMatriculado`

---

## Out of Scope

| Feature | Reason |
|---------|--------|
| Frontend / UI | Projeto é backend didático; a documentação é a interface |
| Autenticação e autorização | Generic Domain — adiciona ruído ao aprendizado de DDD tático |
| CQRS completo | Aumentaria a complexidade sem conceito novo para o nível de aprendizado alvo |
| Event Sourcing | Avançado demais para uma primeira exposição ao DDD |
| Messaging externo (Kafka, RabbitMQ) | Spring ApplicationEvents in-process é suficiente para demonstrar o padrão e evitar overhead de infra |
| Implementação completa de Financeiro/Acadêmico | Adicionaria complexidade de integração sem conceitos novos |
| Contexto Secretaria | Fora do fluxo principal; não maximiza a demonstração de conceitos DDD |
| Lombok / MapStruct | Mascarariam o boilerplate intencional que demonstra a separação domínio/infraestrutura |
| JPA / Spring Data | Excluídos pedagogicamente: anotações @Entity no domínio violam a separação de camadas |

---

## Traceability

*(Preenchido pelo roadmapper — 2026-06-20)*

| Requirement | Phase | Status |
|-------------|-------|--------|
| ESTR-01 | Phase 1 | Pending |
| ESTR-02 | Phase 1 | Pending |
| ESTR-03 | Phase 1 | Pending |
| ESTR-04 | Phase 1 | Pending |
| ESTR-05 | Phase 1 | Pending |
| ESTR-06 | Phase 1 | Pending |
| TAT-01 | Phase 2 | Pending |
| TAT-02 | Phase 2 | Pending |
| TAT-03 | Phase 2 | Pending |
| TAT-04 | Phase 2 | Pending |
| TAT-05 | Phase 2 | Pending |
| TAT-06 | Phase 2 | Pending |
| MOD-01 | Phase 2 | Pending |
| MOD-02 | Phase 2 | Pending |
| MOD-03 | Phase 2 | Pending |
| MOD-04 | Phase 2 | Pending |
| DOM-01 | Phase 3 | Pending |
| DOM-02 | Phase 3 | Pending |
| DOM-03 | Phase 3 | Pending |
| DOM-04 | Phase 3 | Pending |
| DOM-05 | Phase 3 | Pending |
| DOM-06 | Phase 3 | Pending |
| DOM-07 | Phase 3 | Pending |
| DOM-08 | Phase 3 | Pending |
| DOM-09 | Phase 3 | Pending |
| DOM-10 | Phase 3 | Pending |
| APL-01 | Phase 3 | Pending |
| APL-02 | Phase 3 | Pending |
| APL-03 | Phase 3 | Pending |
| APL-04 | Phase 3 | Pending |
| APL-05 | Phase 3 | Pending |
| INF-01 | Phase 3 | Pending |
| INF-02 | Phase 3 | Pending |
| INF-03 | Phase 3 | Pending |
| INF-04 | Phase 3 | Pending |
| INF-05 | Phase 3 | Pending |
| INF-06 | Phase 3 | Pending |
| INF-07 | Phase 3 | Pending |
| IFX-01 | Phase 4 | Pending |
| IFX-02 | Phase 4 | Pending |
| IFX-03 | Phase 4 | Pending |
| DCK-01 | Phase 4 | Pending |
| DCK-02 | Phase 4 | Pending |
| DID-01 | Phase 4 | Pending |
| DID-02 | Phase 4 | Pending |
| DID-03 | Phase 4 | Pending |
| DID-04 | Phase 4 | Pending |
| DID-05 | Phase 4 | Pending |
| DID-06 | Phase 4 | Pending |
| DID-07 | Phase 4 | Pending |
| DID-08 | Phase 4 | Pending |

**Coverage:**
- v1 requirements: 51 total (6 ESTR + 6 TAT + 4 MOD + 10 DOM + 5 APL + 7 INF + 3 IFX + 2 DCK + 8 DID)
- Mapped to phases: 51 / 51
- Unmapped: 0 — coverage complete

---
*Requirements defined: 2026-06-20*
*Last updated: 2026-06-20 — traceability filled by roadmapper*
