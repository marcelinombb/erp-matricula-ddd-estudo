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
