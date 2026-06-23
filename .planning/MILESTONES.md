# Milestones

## v1.0 Projeto Didático DDD (Shipped: 2026-06-21)

**Phases completed:** 4 fases, 15 planos, 117 commits
**Timeline:** 2026-06-20 → 2026-06-21 · 138 arquivos, 23.869 linhas · 3.514 LOC Java · 4.492 linhas docs Markdown

**Key accomplishments:**

- Design Estratégico: problema-negocio.md, linguagem-ubiqua.md com Conceitos Ambíguos, 6 subdomínios classificados, Context Map Mermaid com 4 BCs e 3 eventos, 4 ADRs no template Nygard com comparativo de código
- Design Tático: 7 documentos com snippets Java 21 ERRADO/CERTO para VOs, Entidades, Agregados, Domain Services, Domain Events e Repositórios + 4 diagramas Mermaid
- Domínio implementado: 6 VOs como records Java 21, Aggregate Root Matricula com 3 invariantes encapsuladas, 3 Domain Events, VerificadorElegibilidadeMatricula puro (sem Spring), interface MatriculaRepositorio no domínio
- Aplicação implementada: 3 Application Services (MatricularAluno, AdicionarDisciplina, CancelarMatricula), Commands como records imutáveis, listeners @TransactionalEventListener para Financeiro/Acadêmico
- Infraestrutura implementada: Flyway V1-V3, seeds com UUIDs fixos, MatriculaMapper.xml com JOIN+collection (sem N+1), TypeHandlers, MatriculaRowMapper separação explícita domínio↔persistência — BUILD SUCCESS 42 arquivos Java
- Interface e Docker: MatriculaController 3 endpoints REST, ExcecaoHandler mapeando exceções → HTTP (409/422/400), Dockerfile multi-stage, docker-compose.yml com healthcheck-chain
- Material Didático: ddd-vs-camadas.md, guia-consulta.md, licoes-aprendidas.md, estrutura-pastas.md + 4 ADRs enriquecidos com seção "Na prática"

**Known deferred items at close:** 1 (see STATE.md Deferred Items)
- 04-VERIFICATION.md: 3 testes manuais human_needed (docker compose up + HTTP — requerem Docker rodando)

---

## v1.1 DDD sem Mudar a Arquitetura (Shipped: 2026-06-22)

**Phases completed:** 3 fases (5-7), 12 planos, 73 commits
**Timeline:** 2026-06-21 → 2026-06-22 · 155 arquivos alterados, 14.045+ linhas adicionadas

**Key accomplishments:**

- Módulo Maven `erp-matricula-camadas` (porta 8081): o "antes" — MatriculaServiceImpl com 227 linhas, 4 model classes anêmicas, MatriculaController com regras no controller, DisciplinaServiceImpl com duplicação — 6 anti-padrões com marcadores `ANTI-PADRAO: DIAG-XX`
- 7 documentos de diagnóstico em `docs/00-ddd-sem-mudar-arquitetura/` (01 a 06 + introdução): cada anti-padrão explicado com código Java real, consequências e contraste DDD
- Comentários REFD inline em 3 pivots Java do módulo DDD (Matricula.java, MatricularAlunoUseCase.java, MatriculaRepositorio.java): sem alterar comportamento, evidenciando a diferença entre o "antes" e o "depois"
- 5 documentos de conceito DDD (`07-linguagem-ubiqua.md` a `11-repositorios.md`) com exemplos ANTES/DEPOIS reais do projeto
- `guia-leitura-comparativo.md` e `exercicio-classificacao.md` (10 regras com gabarito) para o percurso comparativo Fase 5→6
- `12-analise-final.md`: balanço crítico com dados concretos (42 arquivos, 3.514 LOC, 227 linhas do God Service, 5 conceitos), tabela 4 eixos, "Quando Vale a Pena" com 3+3 critérios

**Known deferred items at close:** 4 (see STATE.md Deferred Items)
- Phase 05 HUMAN-UAT: 2 julgamentos pedagógicos (trail countDisciplinas — falsa positiva; DisciplinaServiceImpl HTTP reachability)
- Phase 07 UAT: 3 verificações de capacidade argumentativa do leitor (requerem leitura humana)
- Phase 07 VERIFICATION: status human_needed (5/5 truths verificadas automaticamente)

---
