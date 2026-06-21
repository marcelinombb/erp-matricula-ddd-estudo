# Phase 5: Diagnostico — Codigo com Anti-padroes - Pattern Map

**Mapped:** 2026-06-21
**Files analyzed:** 18 (novos arquivos a criar ou modificar)
**Analogs found:** 16 / 18

---

## File Classification

| Arquivo Novo/Modificado | Role | Data Flow | Analog Mais Próximo | Qualidade |
|-------------------------|------|-----------|---------------------|-----------|
| `/pom.xml` (parent, NOVO) | config | build | `erp-matricula-app/pom.xml` | role-match |
| `erp-matricula-camadas/pom.xml` (NOVO) | config | build | `erp-matricula-app/pom.xml` | exact |
| `erp-matricula-ddd/pom.xml` (MODIFICADO — era `erp-matricula-app`) | config | build | `erp-matricula-app/pom.xml` | exact |
| `Dockerfile` (MODIFICADO) | config | build | `Dockerfile` | exact |
| `Dockerfile.camadas` (NOVO) | config | build | `Dockerfile` | exact |
| `docker-compose.yml` (MODIFICADO) | config | request-response | `docker-compose.yml` | exact |
| `erp-matricula-camadas/.../MatriculaController.java` (NOVO) | controller | request-response | `erp-matricula-app/.../interfaces/MatriculaController.java` | role-match |
| `erp-matricula-camadas/.../MatriculaService.java` (NOVO) | service | request-response | `erp-matricula-app/.../aplicacao/MatricularAlunoUseCase.java` | role-match |
| `erp-matricula-camadas/.../MatriculaServiceImpl.java` (NOVO) | service | CRUD | `erp-matricula-app/.../aplicacao/MatricularAlunoUseCase.java` | role-match |
| `erp-matricula-camadas/.../DisciplinaServiceImpl.java` (NOVO) | service | CRUD | `erp-matricula-app/.../aplicacao/AdicionarDisciplinaUseCase.java` | role-match |
| `erp-matricula-camadas/.../MatriculaRepository.java` (NOVO) | repository | CRUD | `erp-matricula-app/.../infraestrutura/persistencia/MatriculaMapper.java` | role-match |
| `erp-matricula-camadas/.../MatriculaRepositoryImpl.java` (NOVO) | repository | CRUD | `erp-matricula-app/.../infraestrutura/persistencia/MatriculaRepositorioMyBatis.java` | role-match |
| `erp-matricula-camadas/.../model/Matricula.java` (NOVO) | model | CRUD | `erp-matricula-app/.../dominio/modelo/Matricula.java` | role-match (contraste intencional) |
| `erp-matricula-camadas/.../model/Aluno.java` (NOVO) | model | CRUD | `erp-matricula-app/.../dominio/modelo/Aluno.java` | role-match (contraste intencional) |
| `erp-matricula-camadas/.../model/ItemMatricula.java` (NOVO) | model | CRUD | `erp-matricula-app/.../dominio/modelo/ItemMatricula.java` | role-match (contraste intencional) |
| `erp-matricula-camadas/.../model/Turma.java` (NOVO) | model | CRUD | `erp-matricula-app/.../dominio/modelo/Turma.java` | role-match (contraste intencional) |
| `erp-matricula-camadas/src/main/resources/application.yml` (NOVO) | config | CRUD | `erp-matricula-app/src/main/resources/application.yml` | exact |
| `docs/00-ddd-sem-mudar-arquitetura/*.md` (NOVOS) | doc | — | `docs/02-design-tatico/agregados.md` | role-match |

> **Nota sobre "contraste intencional":** Os models do módulo `erp-matricula-camadas` são
> intencionalmente opostos aos do módulo DDD. O analog existe como referência de contraste —
> o novo código deve fazer o oposto do que o analog faz, produzindo a entidade anêmica.

---

## Pattern Assignments

### `/pom.xml` — Parent POM Multi-Module (NOVO)

**Analog:** `erp-matricula-app/pom.xml` (linhas 19-24 e 117-125)

**Divergência do analog:** O parent POM NÃO tem `<packaging>jar</packaging>` — usa
`<packaging>pom</packaging>`. NÃO executa `spring-boot:repackage` — precisa de `<skip>true</skip>`
no plugin. Lista os dois módulos filhos via `<modules>`.

**Imports / coordenadas Maven** (baseado em `erp-matricula-app/pom.xml` linhas 19-44):
```xml
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

<properties>
    <java.version>21</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
</properties>
```

**Módulos declarados** (bloco obrigatório, sem analog direto):
```xml
<modules>
    <module>erp-matricula-ddd</module>
    <module>erp-matricula-camadas</module>
</modules>
```

**Plugin com skip** (Pitfall 2 do RESEARCH.md — baseado em `erp-matricula-app/pom.xml` linhas 117-125):
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <skip>true</skip>   <!-- parent não é JAR executável — evita falha de repackage -->
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Comentário pedagógico obrigatório** (segue estilo do `erp-matricula-app/pom.xml` linhas 1-9):
```xml
<!--
  ERP Matrícula — Parent POM Multi-Module
  ========================================
  Exceção documentada à restrição CLAUDE.md sobre multi-module Maven.
  A separação física entre erp-matricula-ddd e erp-matricula-camadas É o ponto
  pedagógico central da Fase 5: o desenvolvedor vê dois sistemas completos com
  o mesmo domínio resolvidos de formas opostas.
  Decisão D-03 em .planning/phases/05-diagnostico-codigo-com-anti-padroes/05-CONTEXT.md
-->
```

---

### `erp-matricula-camadas/pom.xml` (NOVO)

**Analog:** `erp-matricula-app/pom.xml` (linhas inteiras)

**Diferença do analog:** `<parent>` aponta para o parent local (`erp-matricula-parent`) em vez
de `spring-boot-starter-parent` diretamente. `<artifactId>` muda para `erp-matricula-camadas`.

**Parent block** (novo padrão multi-module):
```xml
<parent>
    <groupId>br.com.escola</groupId>
    <artifactId>erp-matricula-parent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

**Dependências** (copiar exatamente de `erp-matricula-app/pom.xml` linhas 49-111):
Mesmas dependências: `spring-boot-starter`, `spring-boot-starter-web`,
`spring-boot-starter-validation`, `mybatis-spring-boot-starter 3.0.5`,
`postgresql`, `flyway-core`, `flyway-database-postgresql`, `spring-boot-starter-test`.

**Plugin spring-boot-maven-plugin** (copiar de `erp-matricula-app/pom.xml` linhas 117-125):
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <!-- sem <skip> — este módulo É um JAR executável -->
        </plugin>
    </plugins>
</build>
```

---

### `erp-matricula-ddd/pom.xml` (MODIFICADO — renomeado de `erp-matricula-app/pom.xml`)

**Analog:** `erp-matricula-app/pom.xml` (arquivo inteiro)

**Única mudança necessária:** substituir o bloco `<parent>` (linhas 19-24) para apontar
para o parent local:
```xml
<!-- DE: -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.3</version>
    <relativePath/>
</parent>

<!-- PARA: -->
<parent>
    <groupId>br.com.escola</groupId>
    <artifactId>erp-matricula-parent</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

Manter `<artifactId>erp-matricula</artifactId>` (linha 30) — o artifactId não precisa mudar
junto com o diretório. Todas as outras dependências e plugins permanecem idênticos.

---

### `Dockerfile` (MODIFICADO — referências a `erp-matricula-app` viram `erp-matricula-ddd`)

**Analog:** `Dockerfile` (arquivo inteiro, linhas 1-39)

**Padrão de build multi-stage** (copiar de `Dockerfile` linhas 11-39):
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY . .
# DE: RUN mvn -q package -DskipTests -f erp-matricula-app/pom.xml
# PARA (com parent pom na raiz, usar -pl para selecionar módulo):
RUN mvn -q package -DskipTests -pl erp-matricula-ddd

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# DE: COPY --from=builder /build/erp-matricula-app/target/erp-matricula-*.jar app.jar
# PARA:
COPY --from=builder /build/erp-matricula-ddd/target/erp-matricula-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### `Dockerfile.camadas` (NOVO)

**Analog:** `Dockerfile` (linhas 11-39) — cópia com substituição de `erp-matricula-ddd`
por `erp-matricula-camadas`:

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY . .
RUN mvn -q package -DskipTests -pl erp-matricula-camadas
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/erp-matricula-camadas/target/erp-matricula-camadas-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### `docker-compose.yml` (MODIFICADO)

**Analog:** `docker-compose.yml` (linhas 1-57)

**Padrão de serviço PostgreSQL** (copiar de `docker-compose.yml` linhas 20-32):
```yaml
postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: erp_matricula
      POSTGRES_USER: ${DB_USER:-matricula}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-matricula}
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-matricula} -d erp_matricula"]
      interval: 5s
      timeout: 5s
      retries: 5
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

**Padrão de serviço app** (baseado em `docker-compose.yml` linhas 37-52, adaptado):

Serviço `app-ddd` (módulo DDD renomeado de `app`):
```yaml
app-ddd:
    build:
      context: .
      dockerfile: Dockerfile      # Dockerfile atualizado aponta para erp-matricula-ddd
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/erp_matricula
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-matricula}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-matricula}
    depends_on:
      postgres:
        condition: service_healthy
```

Serviço `app-camadas` (NOVO, banco separado para evitar conflito de Flyway — Pitfall 1 RESEARCH.md):
```yaml
app-camadas:
    build:
      context: .
      dockerfile: Dockerfile.camadas
    ports:
      - "8081:8080"               # porta 8081 no host, 8080 no container
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/erp_matricula_camadas
      SPRING_DATASOURCE_USERNAME: ${DB_USER:-matricula}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD:-matricula}
    depends_on:
      postgres:
        condition: service_healthy
```

**Banco `erp_matricula_camadas`:** o serviço `postgres` precisa criar os dois bancos na
inicialização. Solução: montar um script SQL de init via volume ou usar variável de ambiente
`POSTGRES_MULTIPLE_DATABASES` com imagem customizada. Alternativa simples: usar init script:
```yaml
postgres:
    # ... (configuração existente)
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql  # cria erp_matricula_camadas
```

---

### `erp-matricula-camadas/.../controller/MatriculaController.java` (NOVO)

**Analog:** `erp-matricula-app/.../interfaces/MatriculaController.java` (linhas 1-223)

**Imports pattern** (baseado em linhas 1-31 do analog — simplificado, sem VOs de domínio):
```java
package br.com.escola.matricula.controller;

import br.com.escola.matricula.model.Matricula;
import br.com.escola.matricula.service.MatriculaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;
```

**Padrão de endpoint** (baseado em linhas 126-128 do analog):
```java
@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse matricular(@RequestBody @Valid MatricularRequest request) {
        // ANTI-PADRAO: Regras na Interface (DIAG-05)
        // Esta validação de negócio existe APENAS aqui no Controller.
        // Um batch job que chama MatriculaService.matricular() diretamente
        // não passa por esta validação — a regra depende do protocolo HTTP para existir.
        if (request.periodoInicio().isBefore(LocalDate.now().minusMonths(6))) {
            return ResponseEntity.badRequest().body("Período muito antigo");
        }
        // ...
    }
}
```

**Records de request** (baseado em linhas 76-95 do analog):
```java
public record MatricularRequest(
        @NotNull(message = "O ID do aluno é obrigatório")
        UUID alunoId,

        @NotNull(message = "O ID da turma é obrigatório")
        UUID turmaId,

        @NotNull(message = "A data de início do período é obrigatória")
        LocalDate periodoInicio
) {}
```

**Diferença do analog:** O controller do módulo `camadas` FAZ validações de regra de negócio
(DIAG-05). O analog DDD NÃO faz — delega tudo para o UseCase. Essa é a inversão intencional.

---

### `erp-matricula-camadas/.../service/MatriculaService.java` (NOVO — interface)

**Analog:** Sem analog direto no módulo DDD (UseCases não têm interfaces explícitas).
Padrão Java convencional de interface de serviço.

```java
package br.com.escola.matricula.service;

import java.util.UUID;

public interface MatriculaService {
    UUID matricular(UUID alunoId, UUID turmaId, String periodoInicio, String periodoFim);
    void adicionarDisciplina(UUID matriculaId, String nomeDisciplina);
    void cancelar(UUID matriculaId);
    // ... outros métodos conforme necessário para o Service Deus (DIAG-03)
}
```

---

### `erp-matricula-camadas/.../service/MatriculaServiceImpl.java` (NOVO — Service Deus + Anêmico)

**Analog:** `erp-matricula-app/.../aplicacao/MatricularAlunoUseCase.java` (linhas 1-109)
— usado como referência de contraste, o novo código faz o oposto.

**Imports pattern** (baseado no estilo do módulo DDD, adaptado para camadas):
```java
package br.com.escola.matricula.service;

import br.com.escola.matricula.model.Aluno;
import br.com.escola.matricula.model.ItemMatricula;
import br.com.escola.matricula.model.Matricula;
import br.com.escola.matricula.repository.AlunoRepository;
import br.com.escola.matricula.repository.ItemMatriculaRepository;
import br.com.escola.matricula.repository.MatriculaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
```

**Padrão Service Anêmico** (referência do RESEARCH.md — DIAG-01):
```java
// ANTI-PADRAO: Service Anêmico (DIAG-01)
// Toda regra de negócio de matrícula vive aqui. A entidade Matricula
// não tem comportamento — ela é apenas um carrier de dados.
// Consequência: quando a regra "máximo 6 disciplinas" muda, você
// precisa encontrar todos os lugares onde disciplinas são adicionadas.
@Service
@Transactional
public class MatriculaServiceImpl implements MatriculaService {

    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;
    private final ItemMatriculaRepository itemMatriculaRepository;

    // Injeção por construtor — igual ao padrão do módulo DDD
    // (erp-matricula-app/.../aplicacao/MatricularAlunoUseCase.java linhas 69-76)
    public MatriculaServiceImpl(AlunoRepository alunoRepository,
                                MatriculaRepository matriculaRepository,
                                ItemMatriculaRepository itemMatriculaRepository) {
        this.alunoRepository = alunoRepository;
        this.matriculaRepository = matriculaRepository;
        this.itemMatriculaRepository = itemMatriculaRepository;
    }
```

**Padrão Service Deus + Duplicação de Regras** (DIAG-03 + DIAG-04):
```java
    // ANTI-PADRAO: Service Deus (DIAG-03)
    // Esta classe tem 200+ linhas e cresce sem parar.
    // Toda nova regra de matrícula vem parar aqui.
    public UUID matricular(UUID alunoId, UUID turmaId, String periodoInicio, String periodoFim) {
        Aluno aluno = alunoRepository.findById(alunoId)
            .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + alunoId));

        // ANTI-PADRAO: Service Anêmico (DIAG-01)
        // Regra de negócio (aluno inativo não matricula) vivendo no Service, não na entidade Aluno.
        // No módulo DDD: aluno.estaAtivo() encapsula a mesma verificação no próprio objeto.
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo não pode ser matriculado");
        }
        // ...
    }

    public void adicionarDisciplina(UUID matriculaId, String nomeDisciplina) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
            .orElseThrow(() -> new RuntimeException("Matrícula não encontrada: " + matriculaId));

        // ANTI-PADRAO: Duplicação de Regras (DIAG-04)
        // Esta validação do aluno já existe em matricular() acima.
        // Daqui a 6 meses, alguém adiciona uma exceção em um lugar e esquece do outro.
        Aluno aluno = alunoRepository.findById(matricula.getAlunoId())
            .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo não pode adicionar disciplinas");
        }

        // ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
        // A regra "máximo 6 disciplinas" não existe no modelo de domínio.
        // Ela existe como uma query SQL no repositório.
        int quantidadeAtual = itemMatriculaRepository.countByMatriculaId(matriculaId);
        if (quantidadeAtual >= 6) {
            throw new RuntimeException("Limite de 6 disciplinas atingido");
        }
        // ...
    }
```

---

### `erp-matricula-camadas/.../service/DisciplinaServiceImpl.java` (NOVO)

**Analog:** `erp-matricula-app/.../aplicacao/AdicionarDisciplinaUseCase.java`
— referência de contraste.

**Propósito:** Existe para demonstrar a Duplicação de Regras (DIAG-04) — contém a mesma
validação `aluno.isAtivo()` que aparece em `MatriculaServiceImpl`. Classe separada que
duplica verificações que já existem no service principal.

```java
package br.com.escola.matricula.service;

import br.com.escola.matricula.model.Aluno;
import br.com.escola.matricula.model.Matricula;
import br.com.escola.matricula.repository.AlunoRepository;
import br.com.escola.matricula.repository.MatriculaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

// ANTI-PADRAO: Duplicação de Regras (DIAG-04)
// Validações duplicadas aqui e em MatriculaServiceImpl surgem naturalmente
// quando não há um modelo de domínio rico que encapsule as regras.
@Service
@Transactional
public class DisciplinaServiceImpl {
    // ...
}
```

---

### `erp-matricula-camadas/.../repository/MatriculaRepository.java` (NOVO — interface)

**Analog:** `erp-matricula-app/.../infraestrutura/persistencia/MatriculaMapper.java` (linhas 27-103)

**Imports pattern** (baseado nas linhas 1-9 do analog):
```java
package br.com.escola.matricula.repository;

import br.com.escola.matricula.model.Matricula;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface MatriculaRepository {
    Optional<Matricula> findById(@Param("id") UUID id);
    List<Matricula> findByAlunoId(@Param("alunoId") UUID alunoId);
    void save(Matricula matricula);
    // COUNT(*) expõe o Acoplamento ao Banco (DIAG-06):
    int countDisciplinas(@Param("matriculaId") UUID matriculaId);
}
```

**Diferença do analog:** No módulo DDD, o `MatriculaMapper` é separado do `MatriculaRepositorio`
(interface de domínio pura vs. implementação de infraestrutura). No módulo `camadas`, o
`@Mapper` MyBatis É o repositório diretamente — sem separação de camadas.

---

### `erp-matricula-camadas/.../repository/MatriculaRepositoryImpl.java` (NOVO)

**Analog:** `erp-matricula-app/.../infraestrutura/persistencia/MatriculaRepositorioMyBatis.java`
(linhas 1-131)

**Imports pattern** (baseado em linhas 1-13 do analog — simplificado):
```java
package br.com.escola.matricula.repository;

import br.com.escola.matricula.model.Matricula;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
```

**Padrão de implementação** (baseado em linhas 56-130 do analog):
```java
@Repository
public class MatriculaRepositoryImpl implements MatriculaRepository {

    private final MatriculaMapper mapper;

    public MatriculaRepositoryImpl(MatriculaMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Matricula> findById(UUID id) {
        return Optional.ofNullable(mapper.findById(id));
    }
```

---

### `erp-matricula-camadas/.../model/Matricula.java` (NOVO — Entidade Anêmica)

**Analog (de contraste):** `erp-matricula-app/.../dominio/modelo/Matricula.java` (linhas 1-289)

O analog é o oposto intencional. A nova classe deve ser o contraste direto:
- Analog DDD: `private final MatriculaId id;` — imutável, Value Object
- Nova classe camadas: `private UUID id;` — mutável, UUID primitivo
- Analog DDD: `adicionarDisciplina(NomeDisciplina)` — comportamento encapsulado
- Nova classe camadas: apenas `getters/setters` — sem comportamento

**Padrão de entidade anêmica** (DIAG-02):
```java
package br.com.escola.matricula.model;

// ANTI-PADRAO: Entidade Anêmica (DIAG-02)
// Esta classe não tem comportamento. Ela é um container de dados —
// um mapeamento 1:1 com a tabela matriculas.
// Quem protege a invariante "máximo 6 disciplinas"? O Service.
// Quem protege "matrícula cancelada não aceita disciplinas"? O Service.
// A entidade não pode se defender — ela é passiva.
//
// Contraste: erp-matricula-ddd/.../dominio/modelo/Matricula.java tem
// adicionarDisciplina(), cancelar(), e protege suas invariantes internamente.
public class Matricula {
    private UUID id;
    private UUID alunoId;       // igual à coluna aluno_id — Acoplamento ao Banco (DIAG-06)
    private UUID turmaId;       // igual à coluna turma_id
    private String periodoInicio;
    private String periodoFim;
    private String status;      // String livre: "ATIVA", "CANCELADA" — sem tipo seguro

    // Getters e setters gerados — sem lógica
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; } // sem validação de transição
    // ...
}
```

---

### `erp-matricula-camadas/.../model/Aluno.java` (NOVO — Entidade Anêmica)

**Analog (de contraste):** `erp-matricula-app/.../dominio/modelo/Aluno.java` (linhas 1-122)

**Diferença intencional:**
- Analog DDD: `estaAtivo()` método com semântica de negócio; `desativar()` método de domínio
- Nova classe camadas: `isAtivo()` getter booleano simples; `setAtivo(boolean)` setter público

```java
package br.com.escola.matricula.model;

// ANTI-PADRAO: Entidade Anêmica (DIAG-02)
// Aluno como data class sem comportamento.
// Contraste: erp-matricula-ddd/.../dominio/modelo/Aluno.java tem estaAtivo() e desativar()
// com semântica de negócio.
public class Aluno {
    private UUID id;
    private String cpf;   // String simples — no módulo DDD é Cpf (Value Object validado)
    private String nome;
    private boolean ativo;

    // Getters e setters — sem validação, sem comportamento
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    // ...
}
```

---

### `erp-matricula-camadas/.../model/ItemMatricula.java` (NOVO)

**Analog (de contraste):** `erp-matricula-app/.../dominio/modelo/ItemMatricula.java`

**Diferença intencional:** O analog DDD é um `record` imutável com `NomeDisciplina` (Value Object).
A nova classe é uma entidade anêmica com `String disciplina` mutável.

```java
package br.com.escola.matricula.model;

import java.time.LocalDateTime;
import java.util.UUID;

// ANTI-PADRAO: Entidade Anêmica (DIAG-02) + Acoplamento ao Banco (DIAG-06)
// Campo adicionada_em espelha coluna do banco. Sem comportamento.
public class ItemMatricula {
    private UUID id;
    private UUID matriculaId;
    private String disciplina;       // String simples — no DDD é NomeDisciplina (VO validado)
    private LocalDateTime adicionadaEm;

    // Getters e setters
}
```

---

### `erp-matricula-camadas/.../model/Turma.java` (NOVO)

**Analog (de contraste):** `erp-matricula-app/.../dominio/modelo/Turma.java`

**Diferença intencional:** O analog DDD tem `periodoEstaAberto()` — comportamento de domínio.
A nova classe apenas carrega dados, sem esse método. Quem verifica se o período está aberto?
O `MatriculaServiceImpl` (DIAG-01).

---

### `erp-matricula-camadas/src/main/resources/application.yml` (NOVO)

**Analog:** `erp-matricula-app/src/main/resources/application.yml` (linhas 1-72)

**Copiar exatamente, substituindo apenas o banco** (Pitfall 1 do RESEARCH.md):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/erp_matricula_camadas  # banco separado — sem conflito de Flyway
    username: matricula
    password: matricula
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration  # mesmas migrations V1-V3 copiadas

mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: br.com.escola.matricula.repository
  configuration:
    map-underscore-to-camel-case: false  # igual ao módulo DDD (linha 56 do analog)
    lazy-loading-enabled: false

logging:
  level:
    br.com.escola.matricula: DEBUG
    org.mybatis: DEBUG

server:
  port: 8080   # porta 8080 no container (docker-compose mapeia para 8081 no host)
```

---

### `erp-matricula-camadas/src/main/java/.../ErpMatriculaCamadasApplication.java` (NOVO)

**Analog:** `erp-matricula-app/.../ErpMatriculaApplication.java` (linhas 1-31)

**Copiar padrão exato** (linhas 3-4 e 23-26):
```java
package br.com.escola.matricula;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("br.com.escola.matricula.repository")  // pacote diferente do módulo DDD
public class ErpMatriculaCamadasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpMatriculaCamadasApplication.class, args);
    }
}
```

---

### `docs/00-ddd-sem-mudar-arquitetura/*.md` (NOVOS)

**Analog:** `docs/02-design-tatico/agregados.md` e `docs/04-material-didatico/ddd-vs-camadas.md`

**Estilo de formatação** (baseado no estilo existente do projeto):
- Cabeçalho H1 com nome do documento
- Seções H2 com conceito e código comparativo
- Blocos de código Java com `// comentário explicativo` acima do ponto relevante
- Referências cruzadas para código: path absoluto do arquivo (`erp-matricula-camadas/...`)
- Sem emojis (convenção do projeto)

**Estrutura de pasta** (D-10 — zero antes do 1 indica pré-DDD):
```
docs/00-ddd-sem-mudar-arquitetura/
├── 00-introducao.md        — contexto pedagógico da fase 0 (o "antes")
├── 01-service-anemico.md   — DIAG-01: Anti-padrão Service Anêmico
├── 02-entidade-anemica.md  — DIAG-02: Anti-padrão Entidade Anêmica
├── 03-service-deus.md      — DIAG-03: Anti-padrão Service Deus
├── 04-duplicacao-regras.md — DIAG-04: Anti-padrão Duplicação de Regras
├── 05-regras-na-interface.md — DIAG-05: Anti-padrão Regras na Interface
└── 06-acoplamento-banco.md — DIAG-06: Anti-padrão Acoplamento ao Banco
```

---

## Shared Patterns

### Injeção por Construtor (sem `@Autowired`)

**Fonte:** `erp-matricula-app/.../aplicacao/MatricularAlunoUseCase.java` (linhas 69-76)
**Aplicar a:** Todos os `@Service`, `@Repository`, `@Controller` do módulo camadas

```java
// Construtor único — Spring 4.3+ injeta automaticamente sem @Autowired
public MatriculaServiceImpl(AlunoRepository alunoRepository,
                             MatriculaRepository matriculaRepository,
                             ItemMatriculaRepository itemMatriculaRepository) {
    this.alunoRepository = alunoRepository;
    this.matriculaRepository = matriculaRepository;
    this.itemMatriculaRepository = itemMatriculaRepository;
}
```

### Comentários de Anti-padrão

**Fonte:** RESEARCH.md seção "Pitfall 4 — Comentários de anti-padrão parecendo julgamento"
**Aplicar a:** Todos os arquivos Java do módulo camadas com anti-padrões

Formato obrigatório:
```java
// ANTI-PADRAO: <Nome do Anti-padrão> (<ID do requirement>)
// <Consequência concreta de 1-2 linhas — descritivo, não prescritivo>
// Contraste: <caminho para o equivalente DDD>
```

Exemplos corretos:
```java
// ANTI-PADRAO: Service Anêmico (DIAG-01)
// Toda regra de negócio de matrícula concentrada no Service.
// Contraste: erp-matricula-ddd/.../dominio/modelo/Matricula.java tem adicionarDisciplina()

// ANTI-PADRAO: Entidade Anêmica (DIAG-02)
// Classe sem comportamento — mapeamento 1:1 com a tabela matriculas.
// Contraste: erp-matricula-ddd/.../dominio/modelo/Matricula.java protege invariantes internamente
```

### `@Transactional` em Services

**Fonte:** `erp-matricula-app/.../aplicacao/MatricularAlunoUseCase.java` (linhas 51-53)
**Aplicar a:** `MatriculaServiceImpl` e `DisciplinaServiceImpl`

```java
@Service
@Transactional
public class MatriculaServiceImpl implements MatriculaService {
```

### Tratamento de Not Found

**Fonte:** `erp-matricula-app/.../aplicacao/MatricularAlunoUseCase.java` (linhas 89-108)
**Aplicar a:** Todos os métodos de busca por ID nos Services

```java
// Padrão do módulo DDD adaptado para Services da arquitetura em camadas
Aluno aluno = alunoRepository.findById(alunoId)
    .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + alunoId));
```

> **Nota pedagógica:** No módulo camadas, usa-se `RuntimeException` genérica — no módulo
> DDD, usa-se `MatriculaNaoEncontradaException` (exceção tipada). O contraste é intencional
> e pode ser comentado no código com `// ANTI-PADRAO: erros genéricos sem tipo`.

### Estilo MyBatis: `@Mapper` + XML

**Fonte:** `erp-matricula-app/.../infraestrutura/persistencia/MatriculaMapper.java` (linhas 26-103)
**Aplicar a:** Todos os `@Mapper` do módulo camadas

```java
@Mapper
public interface MatriculaRepository {
    // Parâmetros com @Param quando há mais de um argumento
    Optional<Matricula> findById(@Param("id") UUID id);
}
```

### Padrão de Resposta HTTP nos Controllers

**Fonte:** `erp-matricula-app/.../interfaces/MatriculaController.java` (linhas 126-128)
**Aplicar a:** `MatriculaController` do módulo camadas

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public MatriculaResponse nomeDoMetodo(@RequestBody @Valid NomeDoRequest request) {
```

---

## Arquivos de Referência a Atualizar (não criar)

Estes arquivos precisam de atualização de referências após `git mv erp-matricula-app erp-matricula-ddd`
(identificados no RESEARCH.md — Runtime State Inventory):

| Arquivo | Referência a atualizar |
|---------|------------------------|
| `Dockerfile` | `erp-matricula-app` → `erp-matricula-ddd` (2 ocorrências) |
| `docker-compose.yml` | Adicionar `app-camadas` service; renomear `app` para `app-ddd` |
| `docs/04-material-didatico/estrutura-pastas.md` | Path `erp-matricula-app/` no diagrama ASCII |
| `docs/04-material-didatico/ddd-vs-camadas.md` | Paths de arquivo inline nos comentários |
| `docs/04-material-didatico/guia-consulta.md` | Links para código-fonte |
| `README.md` | Seção de estrutura do projeto |

**NÃO atualizar:** qualquer arquivo em `.planning/milestones/v1.0-phases/` — são registros históricos.

---

## No Analog Found

| Arquivo | Role | Data Flow | Motivo |
|---------|------|-----------|--------|
| `/pom.xml` (parent multi-module) | config | build | Não existe parent POM multi-module no projeto — apenas single-module até agora |
| `erp-matricula-camadas/.../service/MatriculaService.java` (interface) | service | CRUD | UseCases no módulo DDD não têm interfaces — padrão diferente |

---

## Metadata

**Escopo de busca de analogs:** `erp-matricula-app/src/`, `Dockerfile`, `docker-compose.yml`, `docs/`
**Arquivos scanned:** 18 arquivos Java + 3 arquivos de infra + 4 docs
**Data do mapeamento:** 2026-06-21

**Notas para o Planner:**
1. A renomeação `erp-matricula-app` → `erp-matricula-ddd` deve ser a primeira ação (Wave 0) —
   todos os outros planos dependem da nova estrutura.
2. O módulo `erp-matricula-camadas` usa os mesmos padrões MyBatis do módulo DDD, mas sem a
   separação domain/infrastructure — Mapper é o Repositório diretamente.
3. As migrations Flyway (V1-V3) devem ser copiadas sem modificação para
   `erp-matricula-camadas/src/main/resources/db/migration/` — banco separado evita conflito.
4. O `DemoRunner` do módulo DDD (`infraestrutura/config/DemoRunner.java` linhas 1-146) é o
   padrão para o `DemoRunner` do módulo camadas — mesmos UUIDs de seed, mesmos 3 fluxos.
