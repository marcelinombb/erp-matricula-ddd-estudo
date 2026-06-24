# Roadmap: ERP Matrícula — Projeto Didático DDD

## Milestones

- ✅ **v1.0 Projeto Didático DDD** — Phases 1-4 (shipped 2026-06-21)
- ✅ **v1.1 DDD sem Mudar a Arquitetura** — Phases 5-7 (shipped 2026-06-22)
- 🚧 **v1.2 Testes como Evidência de Design** — Phases 8-10 (in progress)

## Phases

<details>
<summary>✅ v1.0 Projeto Didático DDD (Phases 1-4) — SHIPPED 2026-06-21</summary>

- [x] Phase 1: Design Estrategico (3/3 plans) — completed 2026-06-20
- [x] Phase 2: Design Tatico e Modelagem Visual (4/4 plans) — completed 2026-06-20
- [x] Phase 3: Implementacao (4/4 plans) — completed 2026-06-20
- [x] Phase 4: Interface, Docker e Material Didatico (4/4 plans) — completed 2026-06-21

Full details: [.planning/milestones/v1.0-ROADMAP.md](milestones/v1.0-ROADMAP.md)

</details>

<details>
<summary>✅ v1.1 DDD sem Mudar a Arquitetura (Phases 5-7) — SHIPPED 2026-06-22</summary>

- [x] Phase 5: Diagnostico — Codigo com Anti-padroes (6/6 plans) — completed 2026-06-22
- [x] Phase 6: Refatoracao DDD na Arquitetura Tradicional (5/5 plans) — completed 2026-06-22
- [x] Phase 7: Analise Final e Balanco Didatico (1/1 plans) — completed 2026-06-22

Full details: [.planning/milestones/v1.1-ROADMAP.md](milestones/v1.1-ROADMAP.md)

</details>

### 🚧 v1.2 Testes como Evidência de Design (In Progress)

**Milestone Goal:** Demonstrar, através de testes reais em ambos os módulos, que um domínio rico (DDD) é mais fácil de testar do que um God Service (arquitetura em camadas).

- [ ] **Phase 8: Testes Unitários do Domínio DDD** - Testes sem Spring/banco que evidenciam a testabilidade pura do domínio rico
- [ ] **Phase 9: Testes Unitários do Módulo Camadas** - Testes do God Service que evidenciam o custo de mocks pesados e regras duplicadas
- [ ] **Phase 10: Documentação Comparativa de Testabilidade** - Guia didático com dados concretos do comparativo de testabilidade

## Phase Details

### Phase 8: Testes Unitários do Domínio DDD

**Goal**: Desenvolvedores podem executar testes unitários completos do módulo DDD sem nenhuma dependência de Spring ou banco, demonstrando que um domínio rico é inerentemente testável
**Depends on**: Phase 7 (módulo erp-matricula-ddd implementado)
**Requirements**: TDDD-01, TDDD-02, TDDD-03, TDDD-04
**Success Criteria** (what must be TRUE):

  1. Desenvolvedor executa `mvn test` no módulo erp-matricula-ddd e todos os testes do Aggregate Matricula passam — as 3 invariantes de negócio verificadas sem Spring ou banco
  2. Desenvolvedor executa testes dos Value Objects (NomeAluno, CodigoDisciplina, StatusMatricula) e vê validação de regras de criação e igualdade por valor sendo verificadas com JUnit 5 puro
  3. Desenvolvedor executa testes do VerificadorElegibilidadeMatricula sem nenhum mock ou import de Spring — zero dependências de infraestrutura visíveis no arquivo de teste
  4. Desenvolvedor executa testes de Domain Events e vê asserções que confirmam MatriculaRealizada, DisciplinaAdicionada e MatriculaCancelada sendo emitidos nas operações corretas do Aggregate

**Plans**: 4 plans
Plans:

- [x] 08-01-PLAN.md — VOs NomeDisciplina e PeriodoLetivo: 5+4 testes de validação e igualdade por valor
- [x] 08-02-PLAN.md — VOs Cpf e AlunoId: 4+3 testes de normalização, dígito verificador e identidade
- [x] 08-03-PLAN.md — Aggregate Matricula: 4 invariantes + 4 Domain Events (AlunoMatriculado, DisciplinaAdicionada, MatriculaCancelada)
- [ ] 08-04-PLAN.md — Stub MatriculaRepositorioEmMemoria + 4 testes do VerificadorElegibilidadeMatricula sem Mockito

**Cross-cutting constraints:**

- Nenhum arquivo de teste contém import org.springframework.* — verificável por grep

### Phase 9: Testes Unitários do Módulo Camadas

**Goal**: Desenvolvedores podem executar testes unitários do módulo camadas que evidenciam visualmente a dificuldade de testar um God Service — mocks pesados, acoplamento implícito e regras duplicadas
**Depends on**: Phase 8
**Requirements**: TCAM-01, TCAM-02, TCAM-03
**Success Criteria** (what must be TRUE):

  1. Desenvolvedor executa testes do MatriculaServiceImpl e vê o bloco `@BeforeEach` de setup com vários mocks necessários para isolar o God Service — contraste perceptível com os testes do domínio DDD
  2. Desenvolvedor executa testes do DisciplinaServiceImpl e observa asserções duplicadas para regras que também aparecem no MatriculaServiceImpl — a duplicação de regras se manifesta como duplicação de testes
  3. Desenvolvedor executa testes do MatriculaController e vê a dificuldade: regras de negócio no controller obrigam mocks adicionais e testes de lógica que deveriam estar no domínio

**Plans**: TBD

### Phase 10: Documentação Comparativa de Testabilidade

**Goal**: Desenvolvedores podem ler documentação didática que sintetiza o comparativo de testabilidade com dados concretos do projeto, tornando o impacto do DDD na testabilidade inegável e quantificável
**Depends on**: Phase 9
**Requirements**: TDOC-01, TDOC-02
**Success Criteria** (what must be TRUE):

  1. Desenvolvedor lê guia comparativo com dados concretos (número de mocks por teste, linhas de setup, número de imports de Spring) que mostram a diferença objetiva entre os módulos
  2. Desenvolvedor lê análise dos padrões de teste Given-When-Then aplicados ao domínio DDD versus o padrão Arrange-pesado-com-mocks do módulo camadas, com exemplos reais dos testes criados nas Phases 8 e 9

**Plans**: TBD
**UI hint**: no

## Progress

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Design Estrategico | v1.0 | 3/3 | Complete | 2026-06-20 |
| 2. Design Tatico e Modelagem Visual | v1.0 | 4/4 | Complete | 2026-06-20 |
| 3. Implementacao | v1.0 | 4/4 | Complete | 2026-06-20 |
| 4. Interface, Docker e Material Didatico | v1.0 | 4/4 | Complete | 2026-06-21 |
| 5. Diagnostico — Codigo com Anti-padroes | v1.1 | 6/6 | Complete | 2026-06-22 |
| 6. Refatoracao DDD na Arquitetura Tradicional | v1.1 | 5/5 | Complete | 2026-06-22 |
| 7. Analise Final e Balanco Didatico | v1.1 | 1/1 | Complete | 2026-06-22 |
| 8. Testes Unitários do Domínio DDD | v1.2 | 3/4 | In Progress|  |
| 9. Testes Unitários do Módulo Camadas | v1.2 | 0/TBD | Not started | - |
| 10. Documentação Comparativa de Testabilidade | v1.2 | 0/TBD | Not started | - |
