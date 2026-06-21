# Roadmap: ERP Matrícula — Projeto Didático DDD

## Milestones

- ✅ **v1.0 Projeto Didático DDD** — Phases 1-4 (shipped 2026-06-21)
- **v1.1 DDD sem Mudar a Arquitetura** — Phases 5-7 (active)

## Phases

<details>
<summary>✅ v1.0 Projeto Didático DDD (Phases 1-4) — SHIPPED 2026-06-21</summary>

- [x] Phase 1: Design Estrategico (3/3 plans) — completed 2026-06-20
- [x] Phase 2: Design Tatico e Modelagem Visual (4/4 plans) — completed 2026-06-20
- [x] Phase 3: Implementacao (4/4 plans) — completed 2026-06-20
- [x] Phase 4: Interface, Docker e Material Didatico (4/4 plans) — completed 2026-06-21

Full details: [.planning/milestones/v1.0-ROADMAP.md](milestones/v1.0-ROADMAP.md)

</details>

### v1.1 DDD sem Mudar a Arquitetura

- [ ] **Phase 5: Diagnostico — Codigo com Anti-padroes** - Módulo "antes": seis exemplos Java documentados de anti-padrões comuns na arquitetura tradicional
- [ ] **Phase 6: Refatoracao DDD na Arquitetura Tradicional** - Módulo "depois": refatoração para modelo rico e Application Service, com comparativo explícito e introdução gradual de conceitos DDD
- [ ] **Phase 7: Analise Final e Balanco Didatico** - Documento de síntese: "Quais benefícios obtivemos aplicando DDD sem alterar a arquitetura?"

## Phase Details

### Phase 5: Diagnostico — Codigo com Anti-padroes
**Goal**: Desenvolvedor consegue reconhecer e nomear seis anti-padrões recorrentes na arquitetura tradicional a partir de exemplos Java concretos e anotados
**Depends on**: Phase 4 (v1.0 base project)
**Requirements**: DIAG-01, DIAG-02, DIAG-03, DIAG-04, DIAG-05, DIAG-06, DID-01
**Success Criteria** (what must be TRUE):
  1. Desenvolvedor lê o código "antes" e aponta sem ajuda qual anti-padrão cada classe exemplifica
  2. Desenvolvedor explica em suas próprias palavras o que está errado em cada exemplo e qual problema isso causa na manutenção
  3. Desenvolvedor distingue os seis anti-padrões entre si (Service Anêmico vs. Entidade Anêmica, Service Deus vs. Duplicação de Regras, Regras na Interface vs. Acoplamento ao Banco)
  4. Desenvolvedor associa cada anti-padrão a situações que já viveu em projetos reais com arquitetura em camadas
**Plans**: 6 plans

Plans:
- [x] 05-01-PLAN.md — Infraestrutura Maven multi-module: renomear erp-matricula-app → erp-matricula-ddd, criar parent POM, Dockerfiles, docker-compose.yml dual-service
- [x] 05-02-PLAN.md — Módulo camadas: pom.xml, main class, quatro model classes anêmicas (DIAG-02, DIAG-06)
- [x] 05-03-PLAN.md — Módulo camadas: quatro @Mapper repositories, XMLs MyBatis, migrations V1-V3 copiadas (DIAG-06)
- [x] 05-04-PLAN.md — Módulo camadas: MatriculaServiceImpl 200+ linhas (DIAG-01, DIAG-03, DIAG-04, DIAG-06) + DisciplinaServiceImpl
- [ ] 05-05-PLAN.md — Módulo camadas: MatriculaController com DIAG-05 (Regras na Interface)
- [x] 05-06-PLAN.md — Documentação Markdown docs/00-ddd-sem-mudar-arquitetura/ (7 arquivos, todos 6 anti-padrões)

### Phase 6: Refatoracao DDD na Arquitetura Tradicional
**Goal**: Desenvolvedor compreende como aplicar os princípios fundamentais do DDD (Linguagem Ubíqua, Entidades, Value Objects, Agregados, Repositórios) dentro do stack Controller→Service→Repository sem introduzir arquiteturas avançadas
**Depends on**: Phase 5
**Requirements**: REFD-01, REFD-02, REFD-03, DDD-01, DDD-02, DDD-03, DDD-04, DDD-05, DID-02
**Success Criteria** (what must be TRUE):
  1. Desenvolvedor lê o código "depois" ao lado do "antes" e identifica exatamente o que mudou em cada classe e por quê
  2. Desenvolvedor explica a diferença entre Application Service orquestrador e Service anêmico que contém regras, usando o código como evidência
  3. Desenvolvedor demonstra por que `pedido.finalizar()` protege uma invariante enquanto `pedido.setStatus(FECHADO)` não, apontando o método no código
  4. Desenvolvedor classifica corretamente uma lista de regras de negócio hipotéticas entre "de Domínio" e "de Aplicação" após estudar o módulo
  5. Desenvolvedor explica o papel de cada conceito DDD introduzido (Linguagem Ubíqua, Entidade, Value Object, Agregado, Repositório) usando exemplos do código Java do projeto
**Plans**: TBD

### Phase 7: Analise Final e Balanco Didatico
**Goal**: Desenvolvedor consegue avaliar criticamente a aplicação de DDD sem mudança de arquitetura, pesando Complexidade introduzida, Benefícios obtidos, Curva de aprendizado e Facilidade de adoção pela equipe
**Depends on**: Phase 6
**Requirements**: DID-03
**Success Criteria** (what must be TRUE):
  1. Desenvolvedor lê o documento de análise final e consegue argumentar a favor ou contra adotar DDD no próximo projeto da equipe usando dados concretos do comparativo
  2. Desenvolvedor identifica quais benefícios do DDD foram alcançados sem mudar a arquitetura e quais exigiriam uma reestruturação maior
  3. Desenvolvedor sai do módulo com uma posição informada: "quando vale a pena aplicar DDD dentro da arquitetura em camadas e quando não vale"
**Plans**: TBD

## Progress

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Design Estrategico | v1.0 | 3/3 | Complete | 2026-06-20 |
| 2. Design Tatico e Modelagem Visual | v1.0 | 4/4 | Complete | 2026-06-20 |
| 3. Implementacao | v1.0 | 4/4 | Complete | 2026-06-20 |
| 4. Interface, Docker e Material Didatico | v1.0 | 4/4 | Complete | 2026-06-21 |
| 5. Diagnostico — Codigo com Anti-padroes | v1.1 | 0/6 | Planned | - |
| 6. Refatoracao DDD na Arquitetura Tradicional | v1.1 | 0/? | Not started | - |
| 7. Analise Final e Balanco Didatico | v1.1 | 0/? | Not started | - |
