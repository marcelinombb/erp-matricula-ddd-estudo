# Phase 5: Diagnostico — Codigo com Anti-padroes — Research

**Pesquisado:** 2026-06-21
**Domínio:** Maven multi-module, Spring Boot layered architecture, Java 21, pedagogical anti-patterns
**Confiança:** HIGH

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01:** Novo Maven module `erp-matricula-camadas/` na raiz do projeto para o código com arquitetura em camadas
- **D-02:** Módulo existente `erp-matricula-app/` será renomeado para `erp-matricula-ddd/` — simetria de nomenclatura (camadas vs. ddd)
- **D-03:** Exceção documentada à restrição CLAUDE.md sobre multi-module Maven — a separação física entre os dois módulos É o ponto pedagógico. Esta exceção é intencional e deve ser documentada no módulo.
- **D-04:** Mesmo domínio de Matrícula Escolar em ambos os módulos — a comparação direta é o ponto didático
- **D-05:** Mesmo package `br.com.escola.matricula` em ambos os módulos — a separação é física (Maven module), não de nome
- **D-06:** `erp-matricula-camadas` é Spring Boot completo rodando — mesmo stack (Spring Boot 3.x, MyBatis, PostgreSQL, Docker)
- **D-07:** O `docker-compose.yml` da raiz (ou um novo) deve subir ambos os módulos
- **D-08:** Sistema único integrado — todos os 6 anti-padrões coexistem naturalmente em uma aplicação coesa. Não classes isoladas por anti-padrão.
- **D-09:** Nomeação dos anti-padrões: evitar julgamento explícito nos nomes. A arquitetura em camadas não é "errada" — é adequada para certos contextos.
- **D-10:** Documentação Markdown em `docs/00-ddd-sem-mudar-arquitetura/` — nova pasta numerada, segue o padrão `docs/01-design-estrategico/`, `docs/02-design-tatico/`. O zero reforça que é o "pré-DDD".
- **D-11:** Cada anti-padrão identificado no código com comentários explicativos — o desenvolvedor lê o código e entende o problema sem precisar de doc externa.

### Claude's Discretion

- Estrutura interna do `erp-matricula-camadas` (sub-packages, nomes das classes) — Claude decide seguindo o padrão Controller/Service/Repository típico de Spring Boot
- Conteúdo específico do schema de banco do módulo camadas — pode reutilizar as mesmas migrations V1-V3 ou criar esquema próprio mais simples

### Deferred Ideas (OUT OF SCOPE)

- Testes automáticos para os exemplos — fora do escopo de v1.1
- Renomear módulo `erp-matricula-app` → `erp-matricula-ddd` pode impactar histórico de git e referências existentes — o planner deve avaliar se renomear ou criar novo módulo e deprecar o antigo
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Descrição | Suporte da Pesquisa |
|----|-----------|---------------------|
| DIAG-01 | Desenvolvedor identifica anti-padrão "Service Anêmico" em código Java exemplo com todas as regras de negócio concentradas no Service | `MatriculaServiceAnemicoImpl` com 150-200 linhas — validação, persistência e eventos no mesmo método |
| DIAG-02 | Desenvolvedor identifica anti-padrão "Entidade Anêmica" em código Java exemplo com apenas atributos, getters e setters sem comportamento | `Matricula` como entidade anêmica com campos públicos e setters; sem `adicionarDisciplina()` nem `cancelar()` |
| DIAG-03 | Desenvolvedor identifica anti-padrão "Service Deus" em código Java exemplo com centenas de linhas de regras misturadas | O mesmo `MatriculaServiceImpl` com operações de matrícula, cancelamento, adição de disciplinas, relatórios e limpeza — 200+ linhas |
| DIAG-04 | Desenvolvedor identifica anti-padrão "Duplicação de Regras" em código Java exemplo com mesma validação espalhada em múltiplos Services | Validação `aluno.ativo` duplicada em `MatriculaServiceImpl.matricular()` e em `DisciplinaServiceImpl.adicionarDisciplina()` |
| DIAG-05 | Desenvolvedor identifica anti-padrão "Regras na Interface" em código Java exemplo com validações executadas apenas no Controller | `MatriculaController` faz `if (alunoId == null)` antes de chamar o service — a regra só existe na borda HTTP |
| DIAG-06 | Desenvolvedor identifica anti-padrão "Acoplamento ao Banco" em código Java exemplo com regras modeladas em função das tabelas | `Matricula` como `@Entity` com `@Column` e lógica de limite de disciplinas baseada em `SELECT COUNT(*)` no Service, não no objeto |
| DID-01 | Módulo apresenta código Java completo "antes" — arquitetura tradicional com anti-padrões identificados e anotados com comentários explicativos | Módulo `erp-matricula-camadas` compilável, dockerizado, com comentários `// ANTI-PADRAO: <nome>` inline |
</phase_requirements>

---

## Summary

Esta fase cria o módulo `erp-matricula-camadas` — uma aplicação Spring Boot completa com arquitetura em camadas (Controller → Service → Repository) onde os 6 anti-padrões coexistem naturalmente. É o "antes" didático que o módulo DDD existente (`erp-matricula-app`) responde como "depois".

O trabalho tem três dimensões distintas que o planner deve tratar separadamente: (1) infraestrutura de projeto — criar parent pom, renomear módulo existente, criar novo módulo, atualizar Docker; (2) código Java — as classes do módulo `erp-matricula-camadas` com os 6 anti-padrões; (3) documentação — pasta `docs/00-ddd-sem-mudar-arquitetura/` explicando o contexto pedagógico.

O ponto mais delicado é a renomeação `erp-matricula-app` → `erp-matricula-ddd`. Há referências em mais de 20 arquivos (docs, plans arquivados, Dockerfile, docker-compose.yml). O CONTEXT.md marca esta questão como "deferred" para o planner decidir: renomear (com atualização de referências) ou criar um novo diretório e deprecar o antigo. Este RESEARCH recomenda renomeação limpa com `git mv`, documentando o raciocínio.

**Recomendação primária:** Estruturar o trabalho em 3 waves: Wave 0 = Maven multi-module + renaming; Wave 1 = código do módulo camadas; Wave 2 = documentação e docker.

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Maven parent POM | Build | — | Centraliza BOM e versões; os dois módulos herdam |
| `erp-matricula-camadas` app | Módulo Java | Docker | Spring Boot completo com DB próprio (porta 8081) |
| `erp-matricula-ddd` (renomeado) | Módulo Java | Docker | Módulo existente; porta 8080 mantida |
| `docker-compose.yml` | Infraestrutura | — | Define dois serviços app; um banco compartilhado |
| Anti-padrão Service Anêmico | `service/` | — | Toda regra no Service, entidade só tem setters |
| Anti-padrão Entidade Anêmica | `model/` | — | Classe com atributos + getters/setters, sem comportamento |
| Anti-padrão Service Deus | `service/` | — | Uma classe com 200+ linhas fazendo tudo |
| Anti-padrão Duplicação de Regras | `service/` | — | Mesma validação em vários Services |
| Anti-padrão Regras na Interface | `controller/` | — | Validações de negócio no Controller |
| Anti-padrão Acoplamento ao Banco | `model/` + `repository/` | — | Modelo espelha schema SQL; lógica depende de COUNT(*) |
| Documentação `docs/00-*/` | Markdown | — | Explica o contexto pedagógico do módulo |

---

## Standard Stack

### Core

| Biblioteca | Versão | Propósito | Por que padrão |
|-----------|--------|-----------|----------------|
| Spring Boot | 3.5.3 | Framework base | Mesma versão do módulo DDD existente [VERIFIED: CLAUDE.md] |
| mybatis-spring-boot-starter | 3.0.5 | Persistência explícita | Mesmo stack do módulo DDD — mantém consistência [VERIFIED: CLAUDE.md] |
| PostgreSQL JDBC | Gerenciado pelo BOM | Driver JDBC | Gerenciado pelo Spring Boot BOM [VERIFIED: erp-matricula-app/pom.xml] |
| Flyway Core | Gerenciado pelo BOM | Migrações | Migrations V1-V3 existentes reutilizáveis [VERIFIED: erp-matricula-app/pom.xml] |
| flyway-database-postgresql | Gerenciado pelo BOM | Suporte Flyway 10+ para PostgreSQL | Obrigatório em Flyway 10+ [VERIFIED: erp-matricula-app/pom.xml] |
| spring-boot-starter-web | Gerenciado pelo BOM | Camada HTTP REST | Controller expõe endpoints idênticos ao módulo DDD [ASSUMED] |
| spring-boot-starter-validation | Gerenciado pelo BOM | Bean Validation nos Controllers | Anti-padrão "Regras na Interface" precisa de `@Valid` no Controller para demonstrar o padrão [ASSUMED] |
| Java | 21 LTS | Runtime | Stack obrigatória do projeto [VERIFIED: CLAUDE.md] |

### Estrutura Maven Multi-Module

O projeto **não tem parent pom na raiz atualmente** — `erp-matricula-app/pom.xml` herda diretamente do `spring-boot-starter-parent` e não há `pom.xml` na raiz do projeto. [VERIFIED: listagem de arquivos pom.xml]

Para o multi-module, a estrutura necessária é:

```
erp-matricula/           ← raiz do projeto
├── pom.xml              ← NOVO parent POM (coordenadas: br.com.escola:erp-matricula-parent)
├── erp-matricula-ddd/   ← renomeado de erp-matricula-app/
│   └── pom.xml          ← herda do parent local, não mais do spring-boot-starter-parent diretamente
└── erp-matricula-camadas/  ← NOVO módulo
    └── pom.xml          ← herda do parent local
```

**Instalação:**
```bash
# No root do projeto, após criar parent pom.xml:
mvn -q install -DskipTests -pl erp-matricula-ddd,erp-matricula-camadas

# Para rodar apenas o módulo camadas:
mvn -q spring-boot:run -pl erp-matricula-camadas -DskipTests
```

---

## Package Legitimacy Audit

> Esta fase não instala novos pacotes externos. Todas as dependências são as mesmas já usadas no módulo `erp-matricula-app` (Spring Boot BOM + mybatis-spring-boot-starter 3.0.5), verificadas nas fases anteriores e registradas no CLAUDE.md.

| Pacote | Origem | Situação |
|--------|--------|----------|
| spring-boot-starter-parent 3.5.3 | Maven Central | Aprovado — verificado em CLAUDE.md como HIGH |
| mybatis-spring-boot-starter 3.0.5 | Maven Central | Aprovado — verificado em CLAUDE.md como HIGH |
| postgresql (JDBC) | Spring Boot BOM | Aprovado — já em uso |
| flyway-core | Spring Boot BOM | Aprovado — já em uso |
| flyway-database-postgresql | Spring Boot BOM | Aprovado — já em uso |

**Pacotes removidos por slopcheck:** nenhum (sem novos pacotes externos).

---

## Architecture Patterns

### System Architecture Diagram

```
                    Desenvolvedor
                         │
              ┌──────────┴──────────┐
              │                     │
   http://localhost:8081         http://localhost:8080
              │                     │
   ┌──────────▼────────────┐  ┌─────▼───────────────────┐
   │  erp-matricula-camadas │  │   erp-matricula-ddd      │
   │  (arquitetura em       │  │   (DDD — o "depois")     │
   │   camadas — o "antes") │  │                          │
   │                        │  │  dominio/ aplicacao/     │
   │  controller/            │  │  infraestrutura/        │
   │  service/               │  │  interfaces/            │
   │  repository/            │  │                         │
   │  model/                 │  │                         │
   └──────────┬─────────────┘  └──────────┬──────────────┘
              │                            │
              └──────────┬─────────────────┘
                         │
              ┌──────────▼──────────┐
              │   PostgreSQL 16     │
              │  erp_matricula      │
              │  (banco compartilhado│
              │   — schema idêntico) │
              └─────────────────────┘
```

**Nota sobre banco compartilhado:** os dois módulos usam o mesmo schema PostgreSQL (migrations V1-V3 idênticas). Flyway usa `flyway_schema_history` por schema; se ambos apontarem para o mesmo banco e mesmo schema, haverá conflito de migrations. A solução recomendada: módulo `erp-matricula-camadas` usa **schema separado** (`camadas`) ou **banco separado** (`erp_matricula_camadas`). Ver Pitfall 1 abaixo.

### Estrutura de Pacotes do Módulo Camadas

```
erp-matricula-camadas/src/main/java/br/com/escola/matricula/
├── controller/
│   └── MatriculaController.java      — endpoints REST; contém anti-padrão "Regras na Interface"
├── service/
│   ├── MatriculaService.java         — interface (opcional, para ilustrar a inversão)
│   └── MatriculaServiceImpl.java     — Service Anêmico + Service Deus + Duplicação de Regras
├── repository/
│   ├── MatriculaRepository.java      — interface que estende padrão DAO genérico
│   └── MatriculaRepositoryImpl.java  — implementação MyBatis; COUNT(*) expõe Acoplamento ao Banco
└── model/
    ├── Matricula.java                — Entidade Anêmica: apenas campos, getters, setters
    ├── ItemMatricula.java            — entidade filha anêmica
    ├── Aluno.java                    — entidade de referência anêmica
    └── Turma.java                    — entidade de referência anêmica
```

**Por que não usar `@Entity` + JPA:** CLAUDE.md proíbe JPA/Hibernate explicitamente. O anti-padrão "Acoplamento ao Banco" é demonstrado via comentários e lógica no Service que faz COUNT(*), não via anotações JPA — o ponto pedagógico é o mesmo sem violar o stack obrigatório. [VERIFIED: CLAUDE.md]

### Pattern 1: Parent POM sem Spring Boot Parent Direto

O parent POM da raiz herda do `spring-boot-starter-parent` e os módulos filhos herdam do parent local:

```xml
<!-- pom.xml raiz (NOVO) -->
<project>
  <groupId>br.com.escola</groupId>
  <artifactId>erp-matricula-parent</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.3</version>
    <relativePath/>
  </parent>

  <modules>
    <module>erp-matricula-ddd</module>
    <module>erp-matricula-camadas</module>
  </modules>
</project>
```

```xml
<!-- erp-matricula-ddd/pom.xml (ATUALIZADO: parent muda de spring-boot-starter-parent para parent local) -->
<parent>
  <groupId>br.com.escola</groupId>
  <artifactId>erp-matricula-parent</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <relativePath>../pom.xml</relativePath>
</parent>
```

[ASSUMED — padrão multi-module Maven convencional; estrutura verificável em documentação oficial Maven]

### Pattern 2: Dockerfile Multi-Stage para Módulo Específico

O `Dockerfile` raiz atual usa `-f erp-matricula-app/pom.xml`. Com multi-module, cada módulo pode ter seu próprio Dockerfile ou o compose pode usar `target` de cada módulo:

```dockerfile
# Dockerfile para erp-matricula-camadas (novo arquivo)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY . .
# Compila apenas o módulo camadas usando o parent pom na raiz
RUN mvn -q package -DskipTests -pl erp-matricula-camadas
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/erp-matricula-camadas/target/erp-matricula-camadas-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

[ASSUMED — padrão Docker multi-stage para Maven multi-module]

### Pattern 3: docker-compose.yml com Dois Serviços Spring Boot

```yaml
services:
  postgres:
    image: postgres:16-alpine
    # ... (igual ao atual)

  app-ddd:
    build:
      context: .
      dockerfile: Dockerfile        # Dockerfile existente, aponta para erp-matricula-ddd
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy

  app-camadas:
    build:
      context: .
      dockerfile: Dockerfile.camadas  # Novo Dockerfile para o módulo camadas
    ports:
      - "8081:8080"                 # porta 8081 no host, 8080 no container
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/erp_matricula_camadas
    depends_on:
      postgres:
        condition: service_healthy
```

[ASSUMED — configuração padrão Docker Compose para múltiplos serviços Spring Boot]

### Anti-Patterns to Avoid

- **Usar nomes pejorativos no código:** Não usar `BadMatricularService` ou `AntiPatternMatricula`. O código deve parecer "código real de uma equipe real" — o comentário explica o problema, não o nome.
- **Isolar anti-padrões em classes separadas:** D-08 determina que todos coexistem naturalmente em um único sistema integrado. Não criar `ServiceAnemico.java` como classe isolada.
- **Usar JPA/Hibernate:** CLAUDE.md proíbe — mesmo que o anti-padrão "Acoplamento ao Banco" seja clássico com `@Entity`. MyBatis simula o mesmo ponto com tabelas mapeadas explicitamente.
- **Esquecer que o módulo precisa compilar:** Código com anti-padrões deve compilar e executar — não é pseudocódigo.

---

## Estado Atual do Código (Codebase Inventory)

### O que existe em `erp-matricula-app/` [VERIFIED: leitura direta do código]

**Arquivos Java principais:**
- `dominio/modelo/Matricula.java` — Aggregate Root rico com `adicionarDisciplina()`, `cancelar()`, `coletarEventos()`; sem imports de Spring
- `dominio/modelo/Aluno.java` — Entidade com `estaAtivo()`, `desativar()`, `equals/hashCode` por identidade
- `dominio/modelo/Turma.java` — Entidade com `periodoEstaAberto()`
- `dominio/modelo/ItemMatricula.java` — `record` imutável (entidade interna)
- `dominio/modelo/StatusMatricula.java` — `sealed interface` com `Ativa`, `Cancelada`, `Concluida`
- `dominio/vo/` — Records: `Cpf`, `AlunoId`, `TurmaId`, `MatriculaId`, `NomeDisciplina`, `PeriodoLetivo`
- `dominio/repositorio/MatriculaRepositorio.java` — interface pura sem imports de framework
- `dominio/servico/VerificadorElegibilidadeMatricula.java` — Domain Service sem `@Service`
- `aplicacao/MatricularAlunoUseCase.java` — `@Service @Transactional`, orquestra sem decidir
- `aplicacao/AdicionarDisciplinaUseCase.java`, `CancelarMatriculaUseCase.java`
- `infraestrutura/persistencia/MatriculaMapper.java` + `MatriculaMapper.xml`
- `infraestrutura/persistencia/MatriculaRepositorioMyBatis.java`
- `interfaces/MatriculaController.java` — 3 endpoints: POST `/matriculas`, `/{id}/disciplinas`, `/{id}/cancelamento`
- `ErpMatriculaApplication.java` — `@SpringBootApplication`

**Migrations Flyway existentes:**
- `V1__schema.sql` — tabelas `alunos`, `turmas`, `matriculas`, `itens_matricula` (com `adicionada_em` em V3)
- `V2__seeds.sql` — seeds fixos: Maria Silva, João Santos, Turma 2026-1
- `V3__adicionar_adicionada_em.sql` — `ADD COLUMN adicionada_em` em `itens_matricula`

**Porta HTTP:** 8080

**application.yml:** aponta para `localhost:5432/erp_matricula`, usuário `matricula/matricula`

### Referências a `erp-matricula-app` que precisam ser atualizadas [VERIFIED: grep]

| Arquivo | Tipo de referência | Ação necessária |
|---------|-------------------|-----------------|
| `Dockerfile` | Path no COPY e RUN (`-f erp-matricula-app/pom.xml`) | Atualizar para `erp-matricula-ddd` |
| `docker-compose.yml` | `build: .` (implicitamente usa Dockerfile) | Atualizar contexto de build |
| `docs/04-material-didatico/estrutura-pastas.md` | Paths no diagrama de pastas | Atualizar `erp-matricula-app/` → `erp-matricula-ddd/` |
| `docs/04-material-didatico/ddd-vs-camadas.md` | Comentários com path no código | Atualizar referências inline |
| `docs/04-material-didatico/guia-consulta.md` | Links para código-fonte | Atualizar paths |
| `docs/04-material-didatico/licoes-aprendidas.md` | Possível referência | Verificar e atualizar |
| `docs/adrs/*.md` | ADRs referenciam paths do código | Verificar e atualizar |
| `.planning/milestones/v1.0-phases/**` | Planos arquivados | **Não atualizar** — são registros históricos |
| `README.md` | Referência à estrutura do projeto | Atualizar seção "Stack técnico" e "Material Didático" |

**Arquivos no `.planning/milestones/` (arquivados):** não devem ser atualizados. São registros históricos e referenciar `erp-matricula-app` é correto para eles.

---

## Os Seis Anti-Padrões — Design de Implementação

Cada anti-padrão é uma característica do sistema integrado, não uma classe isolada. Abaixo: o que cada um representa no código, como coexiste com os outros, e o comentário inline que o identifica.

### DIAG-01: Service Anêmico

**Manifestação no código:** `MatriculaServiceImpl.matricular()` contém toda a lógica de negócio: validação do aluno, validação do período, verificação de duplicidade, criação do objeto, persistência.

```java
@Service
@Transactional
public class MatriculaServiceImpl implements MatriculaService {

    // ANTI-PADRAO: Service Anêmico
    // Toda regra de negócio de matrícula vive aqui. A entidade Matricula
    // não tem comportamento — ela é apenas um carrier de dados.
    // Consequência: quando a regra "máximo 6 disciplinas" muda, você
    // precisa encontrar todos os lugares onde disciplinas são adicionadas.
    public MatriculaResponse matricular(UUID alunoId, UUID turmaId, String periodo) {
        Aluno aluno = alunoRepository.findById(alunoId)
            .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo");
        }
        // ... mais 50 linhas de lógica
    }
}
```

**Relação com DIAG-02:** a entidade `Matricula` existe mas só tem atributos + getters/setters — daí o "anêmico".

### DIAG-02: Entidade Anêmica

**Manifestação no código:** `Matricula.java` no módulo camadas é uma classe com campos `private`, getters e setters públicos. Sem `adicionarDisciplina()`, sem `cancelar()`. Um método de negócio seria incomum numa entity típica.

```java
// model/Matricula.java no módulo camadas
// ANTI-PADRAO: Entidade Anêmica
// Esta classe não tem comportamento. Ela é um container de dados —
// um mapeamento 1:1 com a tabela matriculas.
// Quem protege a invariante "máximo 6 disciplinas"? O Service.
// Quem protege "matrícula cancelada não aceita disciplinas"? O Service.
// A entidade não pode se defender — ela é passiva.
public class Matricula {
    private UUID id;
    private UUID alunoId;
    private UUID turmaId;
    private String periodoInicio;
    private String periodoFim;
    private String status;  // String livre — "ATIVA", "CANCELADA", "CONCLUIDA"

    // getters e setters...
    public void setStatus(String status) { this.status = status; } // sem validação
}
```

**Por que `String status` em vez de `sealed interface`:** no módulo camadas, usar String expõe o anti-padrão de magic strings. O módulo DDD tem `StatusMatricula` como `sealed interface` — a comparação é direta.

### DIAG-03: Service Deus

**Manifestação no código:** `MatriculaServiceImpl` acumula TODAS as operações: matricular, cancelar, adicionar disciplina, listar por aluno, verificar elegibilidade, limpar matrículas antigas. Resultado: 200+ linhas.

```java
// ANTI-PADRAO: Service Deus
// Esta classe tem 200+ linhas e cresce sem parar.
// Toda nova regra de matrícula vem parar aqui.
// Um Service Deus viola o Princípio da Responsabilidade Única —
// ele sabe de tudo, faz de tudo, muda por qualquer razão.
@Service
public class MatriculaServiceImpl implements MatriculaService {
    // matricular() — 60 linhas
    // adicionarDisciplina() — 40 linhas
    // cancelar() — 30 linhas
    // buscarPorAluno() — 20 linhas
    // verificarElegibilidade() — 30 linhas — DUPLICADO também em DisciplinaServiceImpl
    // limparMatriculasAntigas() — 20 linhas
}
```

**Relação com DIAG-01:** Service Deus e Service Anêmico coexistem: o service está inchado (Deus) exatamente porque ele é anêmico — como o modelo não tem comportamento, tudo migra para o Service.

### DIAG-04: Duplicação de Regras

**Manifestação no código:** a validação `if (!aluno.isAtivo()) throw ...` aparece tanto em `MatriculaServiceImpl.matricular()` quanto em `DisciplinaServiceImpl.adicionarDisciplina()`. Com o tempo, as implementações divergem.

```java
// DisciplinaServiceImpl.java
@Service
public class DisciplinaServiceImpl {

    public void adicionarDisciplina(UUID matriculaId, String disciplina) {
        Matricula matricula = matriculaRepository.findById(matriculaId)...;

        // ANTI-PADRAO: Duplicação de Regras
        // Esta validação do aluno já existe em MatriculaServiceImpl.matricular().
        // Daqui a 6 meses, alguém adiciona uma exceção em um lugar e esquece do outro.
        // A regra "aluno inativo não pode adicionar disciplinas" está espalhada.
        Aluno aluno = alunoRepository.findById(matricula.getAlunoId())...;
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo");
        }
        // ...
    }
}
```

### DIAG-05: Regras na Interface

**Manifestação no código:** `MatriculaController.matricular()` faz validações de regra de negócio antes de chamar o service. Essas validações não existem no Service — se o endpoint for chamado internamente (batch job, teste de integração), as regras são puladas.

```java
// MatriculaController.java no módulo camadas
@PostMapping
public ResponseEntity<?> matricular(@RequestBody MatricularRequest request) {

    // ANTI-PADRAO: Regras na Interface
    // Esta validação existe APENAS aqui no Controller.
    // Um batch job que chama MatriculaService.matricular() diretamente
    // não passa por esta validação — o período pode estar fechado.
    // A regra de negócio depende do protocolo HTTP para existir.
    if (request.getPeriodoInicio().isBefore(LocalDate.now().minusMonths(1))) {
        return ResponseEntity.badRequest().body("Período muito antigo");
    }

    return ResponseEntity.ok(matriculaService.matricular(...));
}
```

### DIAG-06: Acoplamento ao Banco

**Manifestação no código:** o model `Matricula.java` espelha exatamente a tabela (`aluno_id`, `turma_id` como `UUID` simples). A regra "máximo 6 disciplinas" é verificada no Service via `COUNT(*)` no banco — não existe como invariante no objeto.

```java
// MatriculaServiceImpl.java — verificação baseada em COUNT(*)
// ANTI-PADRAO: Acoplamento ao Banco
// A regra de negócio "máximo 6 disciplinas" não existe no modelo de domínio.
// Ela existe como uma query SQL. Se você ler a classe Matricula, você não
// encontra essa regra. Para descobrir o limite, você precisa ler a query.
// O negócio está escondido na infraestrutura.
int quantidadeAtual = matriculaRepository.countDisciplinas(matriculaId);
if (quantidadeAtual >= 6) {
    throw new RuntimeException("Limite de disciplinas atingido");
}
itemMatriculaRepository.save(new ItemMatricula(matriculaId, disciplina));
```

---

## Don't Hand-Roll

| Problema | Não construir | Usar em vez | Por que |
|---------|--------------|-------------|---------|
| Schema SQL do módulo camadas | Schema novo diferente | Reutilizar migrations V1-V3 do módulo DDD | O schema é idêntico — o que muda é o código Java que o acessa. Criar schema separado adiciona trabalho sem ganho pedagógico. |
| Build multi-module do zero | pom.xml multi-module customizado | Padrão `spring-boot-starter-parent` como grandparent | Maven multi-module com Spring Boot BOM tem padrão estabelecido — não reinventar |
| Docker network personalizada | Network customizada | Docker Compose default network | Compose cria network automática; serviços se comunicam pelo nome do service |
| Flyway schema isolation | Configuração manual | `spring.flyway.schemas` ou banco separado | Flyway tem suporte nativo a schemas separados — não reinventar isolamento |

**Insight chave:** O módulo `erp-matricula-camadas` usa **o mesmo schema** que o módulo DDD. A diferença é o código Java, não a estrutura de dados. Isso reforça o ponto pedagógico: os dados são os mesmos, o design do código é que muda.

---

## Common Pitfalls

### Pitfall 1: Conflito de Flyway entre dois módulos no mesmo banco

**O que dá errado:** Ambos os módulos apontam para `erp_matricula` no mesmo PostgreSQL. Os dois tentam aplicar as migrations V1-V3. O segundo módulo que sobe falha com "Migration V1 checksum mismatch" ou "already applied".

**Por que acontece:** Flyway usa a tabela `flyway_schema_history` para rastrear migrations. Com dois módulos no mesmo schema, ambos escrevem na mesma tabela.

**Como evitar:** Usar banco separado (`erp_matricula_camadas`) OU schema separado via `spring.flyway.schemas=camadas` + `spring.flyway.default-schema=camadas`. A opção banco separado é mais simples para fins pedagógicos e mais clara no `docker-compose.yml`.

**Configuração recomendada no `application.yml` do módulo camadas:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/erp_matricula_camadas
```

**No docker-compose.yml:** o `postgres` service precisa de init script para criar o banco `erp_matricula_camadas`, OU o Flyway pode criá-lo via `spring.flyway.create-schemas=true` + `spring.flyway.schemas`.

### Pitfall 2: Parent POM com spring-boot-repackage quebrando o módulo pai

**O que dá errado:** O parent POM herda de `spring-boot-starter-parent`, que inclui o plugin `spring-boot-maven-plugin`. Com `packaging=pom`, o Spring Boot tenta empacotar o parent como JAR e falha.

**Por que acontece:** O plugin `spring-boot-maven-plugin` é herdado por todos os módulos via parent. O módulo pai com `<packaging>pom</packaging>` não deve executar o `repackage`.

**Como evitar:** No parent POM, declarar o plugin explicitamente com `<skip>true</skip>`:
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-maven-plugin</artifactId>
      <configuration>
        <skip>true</skip>
      </configuration>
    </plugin>
  </plugins>
</build>
```
[ASSUMED — pitfall documentado em comunidade Spring Boot; verificar comportamento real]

### Pitfall 3: Dockerfile raiz quebrando após renomeação

**O que dá errado:** O `Dockerfile` atual tem `RUN mvn -q package -DskipTests -f erp-matricula-app/pom.xml`. Após renomear para `erp-matricula-ddd`, o build do Docker quebra.

**Por que acontece:** Path hardcoded no Dockerfile.

**Como evitar:** Atualizar o Dockerfile antes (ou junto) da renomeação. Com multi-module parent, o comando muda para:
```dockerfile
RUN mvn -q package -DskipTests -pl erp-matricula-ddd
```
O `-pl` (project list) seleciona o módulo dentro do multi-module.

### Pitfall 4: Comentários de anti-padrão parecendo julgamento de valor

**O que dá errado:** Comentários como `// RUIM: isso está errado` criam resistência. O desenvolvedor defende o código em vez de aprender com ele.

**Por que acontece:** Linguagem prescritiva sobre "certo/errado" ativa mecanismo de defesa.

**Como evitar:** Comentários descritivos: `// ANTI-PADRAO: Service Anêmico — toda regra de negócio concentrada no Service`. Descreve o padrão e a consequência, sem julgamento. Segue D-09 do CONTEXT.md.

### Pitfall 5: Módulo camadas com código tão ruim que não compila

**O que dá errado:** O desenvolvedor tenta entender o anti-padrão mas o código tem erros de compilação, NPEs em runtime, ou funciona de forma diferente do esperado.

**Por que acontece:** Código com anti-padrões pode ser descuidado na execução.

**Como evitar:** O código deve ser "código real de uma equipe real que não conhece DDD" — não código propositalmente quebrado. Funciona, compila, responde às mesmas requisições HTTP que o módulo DDD. Os anti-padrões são problemas de design, não bugs.

### Pitfall 6: `git mv` vs diretório novo para a renomeação

**O que dá errado:** Criar `erp-matricula-ddd/` como cópia e deletar `erp-matricula-app/` — git perde o histórico do arquivo.

**Por que acontece:** Git detecta rename pelo conteúdo (similaridade), mas a operação `mv` seguida de `add`/`rm` nem sempre é rastreada corretamente.

**Como evitar:** Usar `git mv erp-matricula-app erp-matricula-ddd` — git preserva o histórico e marca como rename na diff. Após o mv, atualizar as referências nos arquivos afetados.

---

## Code Examples

### Parent POM mínimo para este projeto

```xml
<!-- /pom.xml (NOVO) — parent POM da raiz -->
<!-- Source: padrão Maven multi-module [ASSUMED] -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
             https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.3</version>
        <relativePath/>
    </parent>

    <groupId>br.com.escola</groupId>
    <artifactId>erp-matricula-parent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>ERP Matrícula — Parent</name>
    <description>
        Parent POM multi-module — Phase 5 (v1.1 DDD sem Mudar a Arquitetura)
        Exceção documentada à restrição CLAUDE.md sobre multi-module Maven:
        a separação física entre erp-matricula-ddd e erp-matricula-camadas
        É o ponto pedagógico central desta fase. D-03 em 05-CONTEXT.md.
    </description>

    <modules>
        <module>erp-matricula-ddd</module>
        <module>erp-matricula-camadas</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- Desabilita repackage no parent — só módulos com main class devem repacotar -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Entidade Anêmica (contraste com DDD)

```java
// erp-matricula-camadas: model/Matricula.java
// Source: padrão arquitetura em camadas tradicional [ASSUMED]

// ANTI-PADRAO: Entidade Anêmica + Acoplamento ao Banco
// Esta classe é um mapeamento direto da tabela matriculas.
// Campos nomeados igual às colunas SQL.
// Sem comportamento — quem protege as invariantes é o MatriculaServiceImpl.
public class Matricula {
    private UUID id;
    private UUID alunoId;        // igual à coluna aluno_id
    private UUID turmaId;        // igual à coluna turma_id
    private String periodoInicio; // igual à coluna periodo_inicio
    private String periodoFim;    // igual à coluna periodo_fim
    private String status;        // string livre: "ATIVA", "CANCELADA" — sem tipo seguro

    // Getters e setters gerados — sem lógica
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // sem validação de transição
    // ...
}
```

### Service Anêmico + Service Deus (integrado)

```java
// erp-matricula-camadas: service/MatriculaServiceImpl.java
// ANTI-PADRAO: Service Anêmico — toda lógica de negócio aqui
// ANTI-PADRAO: Service Deus — esta classe faz tudo que envolve matrícula
@Service
@Transactional
public class MatriculaServiceImpl implements MatriculaService {

    // ANTI-PADRAO: Service Anêmico — regra de negócio (aluno inativo não matricula)
    // vivendo no Service, não na entidade Aluno
    public UUID matricular(UUID alunoId, UUID turmaId, String periodo) {
        Aluno aluno = alunoRepository.findById(alunoId)
            .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + alunoId));

        if (!aluno.isAtivo()) {               // regra de negócio no Service
            throw new RuntimeException("Aluno inativo não pode ser matriculado");
        }
        // ... lógica de período, duplicidade, persistência — tudo aqui
    }

    // ANTI-PADRAO: Duplicação de Regras — mesma validação de aluno.isAtivo()
    // que existe em adicionarDisciplina() abaixo
    public void adicionarDisciplina(UUID matriculaId, String disciplina) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
            .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));

        // ANTI-PADRAO: Duplicação de Regras — copiado de matricular()
        Aluno aluno = alunoRepository.findById(matricula.getAlunoId())...;
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo");
        }

        // ANTI-PADRAO: Acoplamento ao Banco — limite de disciplinas como COUNT(*)
        int qtd = itemMatriculaRepository.countByMatriculaId(matriculaId);
        if (qtd >= 6) {
            throw new RuntimeException("Limite de 6 disciplinas atingido");
        }
        // ...
    }
}
```

### Esquema de Banco para o Módulo Camadas

O módulo camadas pode reutilizar as **migrations V1-V3 idênticas** do módulo DDD, aplicadas em um banco separado `erp_matricula_camadas`. O schema é o mesmo — o ponto pedagógico está no código Java que o acessa de formas opostas.

```yaml
# erp-matricula-camadas/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/erp_matricula_camadas
    username: matricula
    password: matricula
  flyway:
    enabled: true
    locations: classpath:db/migration  # mesmas migrations copiadas de erp-matricula-ddd
```

---

## Runtime State Inventory

> Fase de criação de novo módulo (não renomeação de strings de dados). Sem estado runtime a migrar, mas o renameamento do diretório `erp-matricula-app` tem impactos que precisam ser tratados.

| Categoria | Itens encontrados | Ação necessária |
|-----------|-------------------|-----------------|
| Stored data | Seeds no volume Docker `postgres_data` — UUIDs fixos (`a0...001`, `b0...001`, `c0...001`) não mudam | Nenhuma — os UUIDs são estáveis |
| Live service config | `docker-compose.yml` raiz — serviço `app` aponta para `build: .` (usa Dockerfile que referencia `erp-matricula-app`) | Atualizar `Dockerfile` e `docker-compose.yml` para `erp-matricula-ddd` |
| OS-registered state | Nenhum — sem systemd, PM2, ou Task Scheduler | Nenhuma |
| Secrets/env vars | `DB_USER`, `DB_PASSWORD` em `.env` ou inline no compose — independentes do nome do módulo | Nenhuma |
| Build artifacts | `erp-matricula-app/target/` — contém JAR compilado do build anterior | `git mv erp-matricula-app erp-matricula-ddd` + `mvn clean` remove automaticamente |

**Referências em arquivos fonte que precisam ser atualizadas após `git mv`:**
- `Dockerfile` — linha `RUN mvn ... -f erp-matricula-app/pom.xml` e `COPY --from=builder /build/erp-matricula-app/target/...`
- `docker-compose.yml` — `build: .` usa Dockerfile que referencia o path
- `docs/04-material-didatico/estrutura-pastas.md` — diagrama ASCII com path `erp-matricula-app/`
- `docs/04-material-didatico/ddd-vs-camadas.md` — comentários inline com path de arquivo
- `docs/04-material-didatico/guia-consulta.md` — links para código-fonte
- `docs/adrs/ADR-001*.md`, `ADR-002*.md`, `ADR-003*.md` — possivelmente referenciam paths

**Arquivos que NÃO devem ser atualizados:** qualquer arquivo em `.planning/milestones/v1.0-phases/` — são registros históricos.

---

## State of the Art

| Abordagem antiga | Abordagem atual | Impacto para esta fase |
|-----------------|-----------------|----------------------|
| Multi-module com módulo pai sem `<skip>` no repackage | Desabilitar `spring-boot-maven-plugin` no módulo pai | Sem o skip, o build do parent falha |
| `Dockerfile` único para todo o projeto | Um Dockerfile por módulo (ou argumento `--build-arg`) | Com 2 módulos, o Docker Compose precisa de 2 builds separados |
| Flyway sem isolamento de schema entre serviços | Schema ou banco separado por serviço | Dois serviços Spring Boot no mesmo schema Flyway causam conflito |

---

## Assumptions Log

| # | Claim | Seção | Risco se errado |
|---|-------|-------|-----------------|
| A1 | Parent POM com `spring-boot-maven-plugin` `<skip>true` evita falha de repackage no módulo pai | Architecture Patterns — Pattern 1 | Build multi-module falha; verificar comportamento real antes de committar |
| A2 | `spring-boot-starter-web` e `spring-boot-starter-validation` são as dependências corretas para o módulo camadas | Standard Stack | Módulo não sobe — mas é improvável, pois são as mesmas dependências do módulo DDD existente |
| A3 | `mvn -q package -DskipTests -pl erp-matricula-camadas` funciona corretamente para build do módulo específico no Docker | Code Examples — Dockerfile | Dockerfile do módulo camadas não compila; ajustar para `-pl erp-matricula-camadas -am` se necessário |
| A4 | Docker Compose com dois serviços Spring Boot na mesma network padrão funciona sem configuração extra | Architecture Patterns — Pattern 3 | Serviços não se comunicam; improvável para este cenário (não precisam comunicar entre si) |
| A5 | `git mv erp-matricula-app erp-matricula-ddd` preserva histórico de commits dos arquivos Java | Runtime State Inventory | Histórico perdido; `git log --follow` confirma antes de concluir |

---

## Open Questions

1. **Renomeação vs. criação de novo diretório para `erp-matricula-ddd`**
   - O que sabemos: o CONTEXT.md marca como deferred para o planner decidir
   - O que está em jogo: `git mv` preserva histórico; criar novo e deletar antigo não
   - Recomendação: usar `git mv erp-matricula-app erp-matricula-ddd` e atualizar referências. Histórico pedagógico é importante — o desenvolvedor pode ver `git log erp-matricula-ddd/src/main/java/.../Matricula.java` e ver a evolução da Fase 1 até agora.

2. **Banco compartilhado vs. banco separado para o módulo camadas**
   - O que sabemos: banco compartilhado causa conflito de Flyway migrations (Pitfall 1)
   - O que está em jogo: banco separado (`erp_matricula_camadas`) é mais simples; schema separado é mais configuração
   - Recomendação: banco separado — mais simples no docker-compose, mais claro na configuração, sem risco de conflito.

3. **Seeds para o módulo camadas**
   - O que sabemos: o módulo DDD usa seeds com UUIDs fixos em V2__seeds.sql
   - O que está em jogo: se o módulo camadas usa banco separado, precisa dos mesmos seeds para o `DemoRunner` funcionar
   - Recomendação: copiar as migrations V1-V3 para `erp-matricula-camadas/src/main/resources/db/migration/` sem modificação. Os dados de demo são os mesmos.

---

## Environment Availability

| Dependência | Requerida por | Disponível | Versão | Fallback |
|-------------|--------------|------------|--------|---------|
| Docker Engine | `docker compose up` para subir os dois módulos | A verificar | — | Rodar localmente com `mvn spring-boot:run` |
| Maven | Build do projeto multi-module | A verificar (dentro do Docker — `eclipse-temurin:21-jdk-alpine` instala via `apk`) | — | `./mvnw` se wrapper existisse; não existe no projeto atual |
| Java 21 | Runtime | Dentro do Docker: garantido via `eclipse-temurin:21` | 21 LTS | — |
| PostgreSQL 16 | Banco de dados | Via Docker Compose | 16-alpine | — |

**Nota:** Maven não está instalado no host — apenas dentro do container Docker (ver Dockerfile linha `RUN apk add --no-cache maven`). Para desenvolvedores que queiram rodar localmente sem Docker, precisam de Maven instalado. Isso é comportamento atual do projeto, não uma mudança desta fase.

---

## Security Domain

> `security_enforcement` não está configurado em `.planning/config.json` — tratado como habilitado.

Esta fase cria código **intencionalmente sem boas práticas de segurança** em alguns aspectos para fins pedagógicos (o módulo "antes"). As verificações ASVS abaixo se aplicam ao módulo `erp-matricula-ddd` (existente) e às adições desta fase.

| Categoria ASVS | Aplica | Controle padrão |
|----------------|--------|-----------------|
| V2 Authentication | Não | — (projeto didático sem auth) |
| V3 Session Management | Não | — |
| V4 Access Control | Não | — |
| V5 Input Validation | Sim (módulo DDD) | `@Valid` nos Controllers, Bean Validation |
| V6 Cryptography | Não | — |

**Para o módulo `erp-matricula-camadas`:** o anti-padrão "Regras na Interface" (DIAG-05) demonstra propositalmente validação incompleta no Controller — isso é o ponto pedagógico, não uma vulnerabilidade real em produção. O código é para treinamento, não deployment.

**Ameaças conhecidas no stack desta fase:**

| Padrão | STRIDE | Mitigação padrão |
|--------|--------|-----------------|
| SQL injection via `#{param}` MyBatis | Tampering | MyBatis usa prepared statements via `#{}` — não interpolação `${}` |
| Hardcoded credentials em application.yml | Information Disclosure | Credenciais de dev apenas; produção usa env vars via Docker Compose |

---

## Sources

### Primary (HIGH confidence)
- Código-fonte `erp-matricula-app/` — leitura direta de todos os arquivos Java, pom.xml, application.yml, migrations, Dockerfile, docker-compose.yml
- `CLAUDE.md` — stack obrigatória, restrições e exceções documentadas
- `05-CONTEXT.md` — decisões travadas D-01 a D-11
- `ddd-sem-mudar-arquitetura.md` — spec dos 6 anti-padrões e critério de sucesso
- `.planning/REQUIREMENTS.md` — DIAG-01 a DIAG-06, DID-01
- `.planning/ROADMAP.md` — goal e success criteria da Phase 5

### Secondary (MEDIUM confidence)
- `docs/04-material-didatico/ddd-vs-camadas.md` — snippets "antes" que confirmam o estilo de código esperado; fornece modelos de entidade anêmica e service anêmico
- `docs/02-design-tatico/agregados.md` — confirma o contraste desejado entre modelo rico e anêmico

### Tertiary (LOW confidence — marcados como [ASSUMED])
- Padrão Maven multi-module com `spring-boot-starter-parent` como grandparent — baseado em conhecimento de treinamento; verificar comportamento real do `<skip>` no repackage
- Sintaxe `mvn -pl` para build de módulo específico dentro do Docker

---

## Metadata

**Breakdown de confiança:**
- Inventário do codebase existente: HIGH — leitura direta de todos os arquivos
- Design dos 6 anti-padrões: HIGH — especificados no CONTEXT.md e ddd-sem-mudar-arquitetura.md
- Maven multi-module: MEDIUM — padrão estabelecido, mas `<skip>` no repackage precisa de verificação real
- Configuração Docker Compose dual-service: MEDIUM — padrão convencional, banco separado recomendado
- Renomeação git: HIGH — `git mv` é o comando correto para preservar histórico

**Data da pesquisa:** 2026-06-21
**Válido até:** 2026-07-21 (stack estável; nenhuma dependência de bibliotecas de movimento rápido)
