# Módulo Camadas — O "Antes" Didático

Este módulo existe para responder uma pergunta concreta: como é o código quando uma equipe experiente em Spring Boot constrói um sistema de matrícula sem aplicar DDD?

---

## O que é este módulo

`erp-matricula-camadas` é uma aplicação Spring Boot completa com arquitetura em camadas (Controller → Service → Repository). O sistema implementa as mesmas três operações do módulo DDD: matricular aluno, adicionar disciplina e cancelar matrícula.

O código é funcional. Compila, sobe com `docker compose up`, responde às mesmas requisições HTTP que o módulo DDD. Não é pseudocódigo nem exemplo didático isolado — é um sistema integrado onde seis anti-padrões coexistem naturalmente, como ocorre em projetos reais.

---

## Por que não "código errado"

A arquitetura em camadas não é errada. É adequada para sistemas com regras de negócio simples, equipes pequenas e requisitos estáveis. Os anti-padrões que aparecem neste módulo não surgem da escolha da arquitetura — surgem da ausência de um modelo de domínio rico.

O mesmo Controller → Service → Repository funcionaria igualmente bem com entidades que encapsulam comportamento e serviços que apenas orquestram. O que este módulo demonstra não é "arquitetura em camadas é ruim" — é "quando o modelo de domínio não tem comportamento, a lógica de negócio migra para o Service, e o Service cresce sem parar".

Essa distinção está documentada na decisão D-09 em `.planning/phases/05-diagnostico-codigo-com-anti-padroes/05-CONTEXT.md`.

---

## Como navegar este módulo

Cada anti-padrão tem um arquivo de documentação dedicado. Leia a documentação, depois vá direto ao arquivo Java indicado — os comentários `// ANTI-PADRAO:` no código repetem o diagnóstico inline.

| Anti-padrão | Documentação | Arquivo Java de referência |
|-------------|-------------|---------------------------|
| Service Anêmico | [01-service-anemico.md](01-service-anemico.md) | `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java` |
| Entidade Anêmica | [02-entidade-anemica.md](02-entidade-anemica.md) | `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java` |
| Service Deus | [03-service-deus.md](03-service-deus.md) | `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java` |
| Duplicação de Regras | [04-duplicacao-regras.md](04-duplicacao-regras.md) | `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java` |
| Regras na Interface | [05-regras-na-interface.md](05-regras-na-interface.md) | `erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/MatriculaController.java` |
| Acoplamento ao Banco | [06-acoplamento-banco.md](06-acoplamento-banco.md) | `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java` |

---

## O comparativo

Dois módulos, mesmo domínio, mesma stack, mesmo banco relacional:

| | Módulo Camadas | Módulo DDD |
|-|----------------|------------|
| Diretório | `erp-matricula-camadas/` | `erp-matricula-ddd/` |
| Porta (via Docker Compose) | `http://localhost:8081` | `http://localhost:8080` |
| Arquitetura | Controller → Service → Repository | Domínio → Aplicação → Infraestrutura |
| Modelo de domínio | Entidades anêmicas (apenas dados) | Entidades ricas (dados + comportamento) |
| Regras de negócio | Concentradas no Service | Distribuídas entre domínio e UseCases |

Para subir os dois módulos simultaneamente:

```bash
docker compose up
```

---

## Próximo passo

Após ler os seis anti-padrões nesta pasta, a Fase 6 mostra como o módulo DDD resolve cada um deles. O ponto de comparação é direto: para cada anti-padrão identificado aqui, existe um equivalente DDD em `erp-matricula-ddd/` que resolve o mesmo problema de design.

Ver: [docs/04-material-didatico/ddd-vs-camadas.md](../04-material-didatico/ddd-vs-camadas.md) para a comparação lado a lado de cada padrão.

---

## Fase 6 — O "depois" DDD: conceitos aplicados

Após identificar os anti-padrões, esta seção mostra como o módulo `erp-matricula-ddd`
resolve cada um. Comece pelo guia de leitura para ver a transformação completa de uma
operação, depois mergulhe nos conceitos individuais.

| Conceito | Documentação | Requirement |
|----------|-------------|-------------|
| Guia de leitura comparativo | [guia-leitura-comparativo.md](guia-leitura-comparativo.md) | REFD-01, REFD-02 |
| Linguagem Ubíqua | [07-linguagem-ubiqua.md](07-linguagem-ubiqua.md) | DDD-01 |
| Entidades | [08-entidades.md](08-entidades.md) | DDD-02 |
| Value Objects | [09-value-objects.md](09-value-objects.md) | DDD-03 |
| Agregados | [10-agregados.md](10-agregados.md) | DDD-04 |
| Repositórios | [11-repositorios.md](11-repositorios.md) | DDD-05 |
| Exercício de Classificação | [exercicio-classificacao.md](exercicio-classificacao.md) | REFD-03 |
| Análise Final | [12-analise-final.md](12-analise-final.md) | DID-03 |
