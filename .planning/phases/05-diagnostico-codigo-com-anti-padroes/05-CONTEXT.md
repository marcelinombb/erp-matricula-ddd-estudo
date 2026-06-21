# Phase 5: Diagnostico — Codigo com Anti-padroes - Context

**Gathered:** 2026-06-21
**Status:** Ready for planning

<domain>
## Phase Boundary

Criar o módulo `erp-matricula-camadas` — uma aplicação Spring Boot completa usando arquitetura em camadas (Controller → Service → Repository) onde os 6 anti-padrões coexistem naturalmente em um sistema integrado e funcional. O módulo é o "antes" do projeto: código real que compila, sobe com Docker, e usa o mesmo domínio de Matrícula Escolar do módulo DDD existente.

Esta fase NÃO inclui refatoração, comparativo ou conceitos DDD — apenas o sistema tradicional bem construído com os anti-padrões claramente identificados.

</domain>

<decisions>
## Implementation Decisions

### Estrutura de Módulos Maven

- **D-01:** Novo Maven module `erp-matricula-camadas/` na raiz do projeto para o código com arquitetura em camadas
- **D-02:** Módulo existente `erp-matricula-app/` será renomeado para `erp-matricula-ddd/` — simetria de nomenclatura (camadas vs. ddd)
- **D-03:** Exceção documentada à restrição CLAUDE.md sobre multi-module Maven — a separação física entre os dois módulos É o ponto pedagógico. O desenvolvedor vê duas "aplicações" distintas, não só packages diferentes. Esta exceção é intencional e deve ser documentada no módulo.

### Domínio e Package

- **D-04:** Mesmo domínio de Matrícula Escolar em ambos os módulos — a comparação direta é o ponto didático
- **D-05:** Mesmo package `br.com.escola.matricula` em ambos os módulos — a separação é física (Maven module), não de nome. O desenvolvedor reconhece as mesmas classes de negócio resolvidas de formas opostas.

### Stack e Executabilidade

- **D-06:** `erp-matricula-camadas` é Spring Boot completo rodando — mesmo stack (Spring Boot 3.x, MyBatis, PostgreSQL, Docker). O desenvolvedor pode rodar `docker compose up` e ver os dois sistemas funcionando.
- **D-07:** O `docker-compose.yml` da raiz (ou um novo) deve subir ambos os módulos — o desenvolvedor compara o comportamento em execução, não só o código-fonte.

### Estilo dos Anti-padrões

- **D-08:** Sistema único integrado — todos os 6 anti-padrões coexistem naturalmente em uma aplicação coesa. Não classes isoladas por anti-padrão. O realismo é parte do ponto didático: é assim que sistemas reais ficam.
- **D-09:** Nomeação dos anti-padrões: evitar o julgamento explícito nos nomes. A arquitetura em camadas não é "errada" — é adequada para certos contextos. Os anti-padrões surgem da falta de modelo de domínio rico, não da arquitetura em si.

### Documentação do Módulo

- **D-10:** Documentação Markdown em `docs/00-ddd-sem-mudar-arquitetura/` — nova pasta numerada, segue o padrão `docs/01-design-estrategico/`, `docs/02-design-tatico/`. O zero antes do 1 reforça que é o "pré-DDD".
- **D-11:** Cada anti-padrão identificado no código com comentários explicativos — o desenvolvedor lê o código e entende o problema sem precisar de doc externa.

### Claude's Discretion

- Estrutura interna do `erp-matricula-camadas` (sub-packages, nomes das classes) — Claude decide seguindo o padrão Controller/Service/Repository típico de Spring Boot
- Conteúdo específico do schema de banco do módulo camadas — pode reutilizar as mesmas migrations V1-V3 ou criar esquema próprio mais simples

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Spec do Milestone

- `ddd-sem-mudar-arquitetura.md` — documento de spec completo da Fase 0. Define os 6 anti-padrões, os conceitos a introduzir, e o critério de sucesso. Leitura obrigatória.

### Requirements e Roadmap

- `.planning/REQUIREMENTS.md` — 17 requirements v1.1, categorias DIAG/REFD/DDD/DID. Phase 5 cobre DIAG-01..06 e DID-01.
- `.planning/ROADMAP.md` — Phase 5 goal e success criteria

### Material Existente (não duplicar)

- `docs/04-material-didatico/ddd-vs-camadas.md` — já tem snippets de before/after. Phase 5 aprofunda o "antes" com código real. Não duplicar o conteúdo existente.
- `docs/02-design-tatico/` (pasta) — formato ERRADO/CERTO estabelecido. Phase 5 usa estilo similar nos docs Markdown.

### Constraints do Projeto

- `CLAUDE.md` — stack obrigatória (Java 21, Spring Boot 3.x, MyBatis, PostgreSQL, Docker, Maven) e lista de exclusões. Multi-module Maven é exceção deliberada documentada em D-03.
- `.planning/PROJECT.md` — Core value, Context (audiência, estilo pedagógico), Key Decisions (ADRs)

### Código DDD de Referência

- `erp-matricula-app/src/main/java/br/com/escola/matricula/` — implementação DDD existente. É o "depois". Phase 5 cria o "antes" com a mesma estrutura de negócio mas sem modelo de domínio rico.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `erp-matricula-app/src/main/resources/db/migration/` — migrations Flyway V1-V3 com schema PostgreSQL. O módulo camadas pode reutilizar as mesmas tabelas ou ter schema simplificado (decisão para o planner).
- `docker-compose.yml` raiz — estrutura base para adicionar o segundo serviço Spring Boot

### Established Patterns

- Package structure `br.com.escola.matricula.{dominio,aplicacao,interfaces,infraestrutura}` — no módulo camadas será achatado: `br.com.escola.matricula.{controller,service,repository,model}` (arquitetura em camadas típica)
- `docs/04-material-didatico/ddd-vs-camadas.md` — mostra o estilo de comparação esperado. Tem antes/depois em snippets. Confirma que o "antes" tem `MatriculaService` com validações, persistência e eventos no mesmo método.

### Integration Points

- O `docker-compose.yml` raiz precisará de atualização para subir o novo módulo (`erp-matricula-camadas`) junto com o existente
- O módulo DDD existente será renomeado de `erp-matricula-app` → `erp-matricula-ddd` — isso implica atualizar `pom.xml` raiz (se existir), `Dockerfile`, `docker-compose.yml`

</code_context>

<specifics>
## Specific Ideas

- **Nomeação intencional**: Evitar nomes como "antipadroes" ou "errado" — descrever o estilo arquitetural, não classificar como errado. `erp-matricula-camadas` é o nome correto.
- **Realismo do sistema anêmico**: O `MatriculaService` deve ter 150-200+ linhas com todas as regras misturadas (validação, regras de negócio, persistência, eventos) — assim o desenvolvedor sente o problema, não apenas lê sobre ele.
- **Referência cruzada explícita**: O `README.md` do módulo `erp-matricula-camadas` deve apontar para o módulo `erp-matricula-ddd` como comparação direta.

</specifics>

<deferred>
## Deferred Ideas

- Testes automáticos para os exemplos — fora do escopo de v1.1 (listado como Out of Scope no REQUIREMENTS.md)
- Renomear módulo `erp-matricula-app` → `erp-matricula-ddd` pode impactar histórico de git e referências existentes — o planner deve avaliar se renomear ou criar novo módulo e deprecar o antigo

</deferred>

---

*Phase: 5-Diagnostico — Codigo com Anti-padroes*
*Context gathered: 2026-06-21*
