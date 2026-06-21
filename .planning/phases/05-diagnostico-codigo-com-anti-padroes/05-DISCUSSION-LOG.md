# Phase 5: Diagnostico — Codigo com Anti-padroes - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-21
**Phase:** 5-Diagnostico — Codigo com Anti-padroes
**Areas discussed:** Formato do código, Domínio dos exemplos, Localização do módulo, Estilo pedagógico

---

## Formato do código

| Option | Description | Selected |
|--------|-------------|----------|
| Código Java compilável | Módulo Java real com classes que compilam. O desenvolvedor pode abrir, rodar, inspecionar no IDE. | ✓ |
| Snippets em Markdown | Documentos .md com trechos de código ERRADO/CERTO. | |
| Ambos | Markdown + classes Java compiláveis separadas. | |

**User's choice:** Código Java compilável

---

| Option | Description | Selected |
|--------|-------------|----------|
| Dentro do módulo existente erp-matricula-app | Package separado dentro do mesmo pom.xml. | |
| Novo Maven module separado | Módulo irmão na raiz. Isola completamente o código. | ✓ |
| Subpasta não-Maven | Arquivos .java sem pom, sem compile-check automático. | |

**User's choice:** Novo Maven module separado

---

| Option | Description | Selected |
|--------|-------------|----------|
| Sim, módulo separado é intencional | A separação física entre 'código sujo' e 'código DDD' é parte do ponto pedagógico. | ✓ |
| Não, package separado basta | Voltar para br.com.escola.matricula.exemplos dentro do módulo existente. | |

**User's choice:** Sim, módulo separado é intencional — exceção deliberada à restrição CLAUDE.md

**Notes:** O usuário confirmou que a separação física (Maven module) é parte do ponto pedagógico. O desenvolvedor vê duas "aplicações" distintas lado a lado.

---

## Domínio dos exemplos

| Option | Description | Selected |
|--------|-------------|----------|
| Mesmo domínio: Matrícula Escolar | Mesmo problema de negócio resolvido de formas opostas. | ✓ |
| Domínio neutro: Pedido/Produto (e-commerce) | Mais universal, sem ambiguidade com o código DDD existente. | |
| Híbrido: Matrícula + Pedido | Matrícula para comparáveis, Pedido para genéricos. | |

**User's choice:** Mesmo domínio: Matrícula Escolar

---

| Option | Description | Selected |
|--------|-------------|----------|
| Mesmo package, module Maven diferente | br.com.escola.matricula em ambos os modules. | ✓ |
| Package diferente no módulo anêmico | Ex: br.com.escola.matricula.tradicional. | |

**User's choice:** Mesmo package, module Maven diferente

---

## Localização do módulo

| Option | Description | Selected |
|--------|-------------|----------|
| erp-matricula-antipadroes/ na raiz | Nome simétrico ao erp-matricula-app. | |
| fase-0-arquitetura-tradicional/ na raiz | Nome didático. | |
| exemplos/arquitetura-tradicional/ | Dentro de pasta exemplos/. | |

**User's choice:** Freetext — "não gostei do nome antipadroes, quero que chame de erp-matricula-camadas"

**Notes:** O usuário rejeitou "antipadroes" por carregar um julgamento implícito (arquitetura em camadas não é necessariamente errada — depende da complexidade do projeto). Escolheu `erp-matricula-camadas` para descrever o estilo sem julgamento.

---

| Option | Description | Selected |
|--------|-------------|----------|
| erp-matricula-ddd | Simétrico: camadas vs. ddd. | ✓ |
| Manter erp-matricula-app | Não renomear módulo existente. | |
| erp-matricula-dominio-rico | Descreve sem mencionar DDD diretamente. | |

**User's choice:** erp-matricula-ddd

---

| Option | Description | Selected |
|--------|-------------|----------|
| docs/00-ddd-sem-mudar-arquitetura/ | Nova pasta numerada seguindo padrão existente. | ✓ |
| Dentro do módulo erp-matricula-camadas/docs/ | Docs junto ao código. | |
| docs/04-material-didatico/ (pasta existente) | Extensão do material didático existente. | |

**User's choice:** docs/00-ddd-sem-mudar-arquitetura/

---

## Estilo pedagógico

| Option | Description | Selected |
|--------|-------------|----------|
| Sistema único integrado com todos os 6 anti-padrões | Aplicação coesa, realista, anti-padrões coexistem. | ✓ |
| Exemplos isolados por anti-padrão | Subpastas ou classes dedicadas por problema. | |
| Híbrido: sistema integrado + README marcando cada anti-padrão | Sistema coeso com comentários indexados. | |

**User's choice:** Sistema único integrado com todos os 6 anti-padrões

---

| Option | Description | Selected |
|--------|-------------|----------|
| Spring Boot completo rodando | Controller → Service → Repository com banco. Docker compose sobe os dois sistemas. | ✓ |
| Só código Java, sem Spring Boot rodando | Classes que compilam mas não sobem servidor. | |
| Spring Boot mas sem banco (H2 in-memory) | Sobe sem Docker, menos realista. | |

**User's choice:** Spring Boot completo rodando

---

## Claude's Discretion

- Estrutura interna do `erp-matricula-camadas` (sub-packages, nomes das classes) — seguir padrão Controller/Service/Repository típico de Spring Boot
- Schema de banco do módulo camadas — pode reutilizar migrations V1-V3 ou criar esquema próprio mais simples

## Deferred Ideas

- Testes automatizados para os exemplos — listado como Out of Scope em REQUIREMENTS.md
- Renomear `erp-matricula-app` → `erp-matricula-ddd`: impacto em histórico git e referências — o planner deve avaliar se renomear diretório existente ou criar novo módulo e deprecar o antigo
