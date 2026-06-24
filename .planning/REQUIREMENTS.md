# Requirements: ERP Matrícula — Projeto Didático DDD

**Defined:** 2026-06-23
**Milestone:** v1.2 — Testes como Evidência de Design
**Core Value:** Um desenvolvedor da equipe deve conseguir, sozinho, ler o projeto do início ao fim e entender **por que** DDD existe, **onde** ele diverge da arquitetura tradicional e **como** aplicar cada padrão tático no código Java real.

## v1.2 Requirements

### Testes DDD (módulo erp-matricula-ddd)

- [ ] **TDDD-01**: Desenvolvedor pode executar testes unitários do Aggregate Matricula que verificam as 3 invariantes de negócio sem Spring ou banco de dados
- [x] **TDDD-02**: Desenvolvedor pode executar testes unitários dos Value Objects (NomeAluno, CodigoDisciplina, StatusMatricula) verificando validação e igualdade por valor
- [ ] **TDDD-03**: Desenvolvedor pode executar testes unitários do VerificadorElegibilidadeMatricula sem nenhum mock ou dependência de infraestrutura
- [ ] **TDDD-04**: Desenvolvedor pode executar testes que verificam que Domain Events (MatriculaRealizada, DisciplinaAdicionada, MatriculaCancelada) são emitidos nas operações corretas

### Testes Camadas (módulo erp-matricula-camadas)

- [ ] **TCAM-01**: Desenvolvedor pode executar testes do MatriculaServiceImpl que evidenciam a quantidade de mocks necessários para isolar o God Service
- [ ] **TCAM-02**: Desenvolvedor pode executar testes do DisciplinaServiceImpl que mostram regras duplicadas se manifestando como testes duplicados
- [ ] **TCAM-03**: Desenvolvedor pode executar testes do MatriculaController que demonstram a dificuldade de testar regras de negócio no controller

### Documentação Didática

- [ ] **TDOC-01**: Desenvolvedor pode ler guia comparativo de testabilidade — por que DDD facilita testes, dados concretos (linhas de setup, número de mocks)
- [ ] **TDOC-02**: Desenvolvedor pode ler análise de padrões de teste: Given-When-Then no módulo DDD vs. arrange pesado com mocks no módulo camadas

## Requisitos Futuros (v1.3+)

### Testes de Integração

- **TINT-01**: Testes de integração dos Application Services com banco real (Testcontainers + PostgreSQL)
- **TINT-02**: Testes de contrato do MatriculaController (MockMvc + Spring Boot Test)
- **TINT-03**: Cobertura de testes reportada (JaCoCo) com meta de 80% no módulo DDD

## Out of Scope

| Feature | Motivo |
|---------|--------|
| Testes de integração com Testcontainers | Adiciona dependência de Docker no build — adiado para v1.3 |
| Cobertura JaCoCo configurada | Configuração de CI/CD fora do escopo pedagógico atual |
| Testes end-to-end via HTTP (RestAssured) | Complexidade desnecessária para o objetivo comparativo |
| Testes do contexto Financeiro/Acadêmico (stub listeners) | Contextos não implementados — fora de escopo desde v1.0 |
| Mutation testing (PIT) | Nível avançado, não central para o comparativo pedagógico |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| TDDD-01 | Phase 8 | Pending |
| TDDD-02 | Phase 8 | Complete |
| TDDD-03 | Phase 8 | Pending |
| TDDD-04 | Phase 8 | Pending |
| TCAM-01 | Phase 9 | Pending |
| TCAM-02 | Phase 9 | Pending |
| TCAM-03 | Phase 9 | Pending |
| TDOC-01 | Phase 10 | Pending |
| TDOC-02 | Phase 10 | Pending |

**Coverage:**
- v1.2 requirements: 9 total
- Mapped to phases: 9 ✓
- Unmapped: 0 ✓

---
*Requirements defined: 2026-06-23*
*Last updated: 2026-06-23 — traceability mapped to Phases 8-10*
