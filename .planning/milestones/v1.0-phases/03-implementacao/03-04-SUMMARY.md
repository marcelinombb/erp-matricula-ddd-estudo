---
phase: "03-implementacao"
plan: "04"
subsystem: "infraestrutura"
tags: ["ddd", "infrastructure-layer", "mybatis", "flyway", "postgresql", "typehandler", "replace-all", "domain-events", "java21"]
dependency_graph:
  requires:
    - phase: "03-01"
      provides: "pom.xml, application.yml, estrutura de pacotes, @MapperScan"
    - phase: "03-02"
      provides: "Matricula aggregate root, MatriculaRepositorio interface, StatusMatricula sealed interface, domínio puro"
    - phase: "03-03"
      provides: "MatricularAlunoUseCase, AdicionarDisciplinaUseCase, CancelarMatriculaUseCase"
  provides:
    - "V1__schema.sql: schema PostgreSQL com 4 tabelas e comentários pedagógicos"
    - "V2__seeds.sql: seeds para os 3 fluxos de demonstração (UUIDs fixos)"
    - "MatriculaMapper.xml: ResultMap JOIN + notNullColumn + jdbcType=OTHER"
    - "MatriculaRow / ItemMatriculaRow: modelos relacionais sem lógica de negócio"
    - "MatriculaMapper: interface @Mapper com 7 operações"
    - "CpfTypeHandler: BaseTypeHandler<Cpf> registrado via type-handlers-package"
    - "MatriculaRowMapper: conversão explícita domínio↔persistência com PeriodoLetivo inline"
    - "MatriculaRepositorioMyBatis: implementação MatriculaRepositorio com replace-all"
    - "FinanceiroEventListener + AcademicoEventListener: @TransactionalEventListener stubs"
    - "DomainServicesConfig: @Bean para VerificadorElegibilidadeMatricula"
    - "DemoRunner: CommandLineRunner executando 3 fluxos no startup"
  affects:
    - "Fase 4: schema e seeds persistem; DemoRunner substituído por Controllers REST"
    - "Ponto de verificação: mvn spring-boot:run com PostgreSQL mostra os 3 fluxos"
tech_stack:
  added:
    - "MyBatis ResultMap com JOIN + collection notNullColumn (padrão aggregate reconstruction)"
    - "TypeHandler custom BaseTypeHandler<Cpf> com package scanning automático"
    - "Estratégia replace-all para coleções de Aggregate (UPDATE-or-INSERT + DELETE + INSERT)"
    - "Spring @TransactionalEventListener para eventos pós-commit"
    - "@Bean para Domain Service puro (sem @Service no domínio)"
  patterns:
    - "MatriculaRow (dados planos) vs Matricula (comportamento) — separação explícita ADR-001"
    - "MatriculaRowMapper como único ponto de conversão domínio↔persistência"
    - "PeriodoLetivo(ano, semestre) → DATE inline no mapper (Opção C, sem TypeHandler customizado)"
    - "notNullColumn='item_disciplina' no collection para prevenir elemento fantasma (Pitfall 1)"
    - "jdbcType=OTHER em todos os parâmetros UUID PostgreSQL (Pitfall 2)"
key_files:
  created:
    - "erp-matricula-app/src/main/resources/db/migration/V1__schema.sql"
    - "erp-matricula-app/src/main/resources/db/migration/V2__seeds.sql"
    - "erp-matricula-app/src/main/resources/mapper/MatriculaMapper.xml"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRow.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/ItemMatriculaRow.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaMapper.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/typehandler/CpfTypeHandler.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRowMapper.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRepositorioMyBatis.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/eventos/FinanceiroEventListener.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/eventos/AcademicoEventListener.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/config/MyBatisConfig.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/config/DomainServicesConfig.java"
    - "erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/config/DemoRunner.java"
  modified: []
decisions:
  - "CPF nos seeds alterado de 12345678901 para 52998224725 — 12345678901 não passa o algoritmo módulo 11 implementado em Cpf.java; 52998224725 é um CPF matematicamente válido"
  - "PeriodoLetivo mantido como (ano, semestre) com conversão inline em MatriculaRowMapper (Opção C — sem TypeHandler customizado)"
  - "VerificadorElegibilidadeMatricula instanciado via @Bean em DomainServicesConfig — sem @Service no domínio (Pitfall 8)"
  - "DemoRunner usa UUIDs fixos dos seeds para reprodutibilidade da demonstração"
metrics:
  duration: "~21 minutos"
  completed_date: "2026-06-21"
  tasks_completed: 2
  tasks_total: 3
  files_created: 14
  files_modified: 0
---

# Phase 03 Plan 04: Camada de Infraestrutura Completa — Summary

**Schema PostgreSQL V1 com 4 tabelas e comentários pedagógicos ADR-003, seeds V2 com UUIDs fixos para 3 fluxos, MatriculaMapper.xml com notNullColumn+jdbcType=OTHER, MatriculaRow/MatriculaRowMapper como separação explícita domínio↔persistência, MatriculaRepositorioMyBatis com replace-all, listeners @TransactionalEventListener e DemoRunner executando os 3 fluxos — projeto compila com 42 arquivos Java (BUILD SUCCESS)**

---

## Tasks Completed

| Task | Name | Commit | Key Files |
|------|------|--------|-----------|
| 1 | Schema SQL, Seeds, MatriculaMapper.xml, Row Objects, MatriculaMapper interface | `1372d72` | V1__schema.sql, V2__seeds.sql, MatriculaMapper.xml, MatriculaRow.java, ItemMatriculaRow.java, MatriculaMapper.java |
| 2 | TypeHandler, MatriculaRowMapper, Repositório, Listeners, Config, DemoRunner | `e418de9` | CpfTypeHandler.java, MatriculaRowMapper.java, MatriculaRepositorioMyBatis.java, FinanceiroEventListener.java, AcademicoEventListener.java, MyBatisConfig.java, DomainServicesConfig.java, DemoRunner.java |
| 3 | Checkpoint human-verify | — | Aguardando verificação humana |

---

## What Was Built

### Task 1: Schema SQL, Seeds, MapperXML e Row Objects

**V1__schema.sql**

4 tabelas com comentários pedagógicos:
- `alunos` e `turmas` — tabelas de referência para os seeds; BC Matrícula usa apenas AlunoId/TurmaId
- `matriculas` — Aggregate Root com `aluno_id` e `turma_id` **sem FK** (ADR-003 — referência por ID entre Aggregates). Status como `VARCHAR(20) CHECK (IN ('ATIVA', 'CANCELADA', 'CONCLUIDA'))` — mais portável que enum PostgreSQL. Index condicional `WHERE status = 'ATIVA'` para unicidade — aluno pode ter matrículas canceladas no mesmo período.
- `itens_matricula` — entidade interna **com FK** `ON DELETE CASCADE` — ciclo de vida acoplado ao Aggregate Root. Comentário explica o contraste com aluno_id/turma_id sem FK.

**V2__seeds.sql**

5 INSERTs com UUIDs fixos:
- Aluno Maria Silva (`a0...001`, CPF `52998224725`) — para o Fluxo 1 (matricular)
- Aluno João Santos (`a0...002`, CPF `98765432100`) — para os Fluxos 2 e 3
- Turma 2026-1 (`b0...001`, período 2026-02-01 a 2026-07-31)
- Matrícula pré-existente (`c0...001`, João/Turma 2026-1/ATIVA) — para Fluxos 2 e 3
- Item de matrícula: "Matemática Básica" na matrícula pré-existente

**MatriculaMapper.xml**

ResultMap `MatriculaResultMap` com:
- `<collection property="itens" ofType="ItemMatriculaRow" notNullColumn="item_disciplina">` — mitiga Pitfall 1 (elemento fantasma para matrículas sem itens no LEFT JOIN)
- 7 queries: `buscarPorId`, `buscarPorAluno`, `existeMatriculaAtiva`, `inserirMatricula`, `atualizarMatricula`, `deletarItensPorMatriculaId`, `inserirItens`
- 18 ocorrências de `jdbcType=OTHER` — todos os parâmetros UUID usam tipo nativo PostgreSQL (Pitfall 2)
- `<foreach>` no `inserirItens` — INSERT em lote em um único statement

**MatriculaRow / ItemMatriculaRow**

Classes simples com campos públicos, zero métodos de negócio. `MatriculaRow` tem `List<ItemMatriculaRow> itens = new ArrayList<>()` inicializada. Javadoc referencia `Matricula.java` para o contraste comportamento vs dados.

**MatriculaMapper.java**

Interface `@Mapper` com 7 assinaturas, Javadoc explicando que é detalhe de infraestrutura — usar `MatriculaRepositorioMyBatis` (que implementa o contrato de domínio).

### Task 2: TypeHandler, RowMapper, Repositório, Listeners, Config e DemoRunner

**CpfTypeHandler.java**

`@MappedTypes(Cpf.class) @MappedJdbcTypes(JdbcType.VARCHAR)`. Estende `BaseTypeHandler<Cpf>`. 4 métodos implementados. Registrado automaticamente via `mybatis.type-handlers-package` no `application.yml` — sem `@Bean` manual.

**MatriculaRowMapper.java**

`@Component` — o único arquivo que conhece tanto `MatriculaRow` quanto `Matricula`. Três métodos:
- `toDomain(MatriculaRow)`: mês ≤ 6 → semestre 1; mês > 6 → semestre 2
- `fromDomain(Matricula)`: semestre 1 → fev/jul; semestre 2 → ago/dez
- `itemsFromDomain(MatriculaId, List<ItemMatricula>)`: para INSERT em lote
- `reconstruirStatus(MatriculaRow)`: switch sobre String com `default` lançando exceção

**MatriculaRepositorioMyBatis.java**

`@Repository implements MatriculaRepositorio`. Estratégia replace-all em `salvar()`:
1. `atualizarMatricula(row)` — retorna 0 se novo
2. Se 0: `inserirMatricula(row)`
3. `deletarItensPorMatriculaId(id)` — coleção tratada atomicamente
4. `inserirItens(itens)` — apenas se não vazia

**FinanceiroEventListener.java / AcademicoEventListener.java**

`@Component` com `@TransactionalEventListener` — executa APÓS commit (não dentro da transação). Javadoc explica por que não `@EventListener`. Logs formatados para verificação visual no terminal.

**DomainServicesConfig.java**

`@Configuration @Bean VerificadorElegibilidadeMatricula verificadorElegibilidadeMatricula(MatriculaRepositorio)` — Domain Service puro instanciado como bean Spring sem `@Service` no domínio.

**DemoRunner.java**

`@Component implements CommandLineRunner`. Executa os 3 fluxos com UUIDs dos seeds. CPFs construídos como `new Cpf(...)` — usam valores matematicamente válidos (ver Desvio 1).

---

## Verification Results

```
grep -r "^import org.springframework" dominio/   → VAZIO (PASSED — Success Criterion 1)
grep -r "^import org.mybatis" dominio/           → VAZIO (PASSED)
mvn compile -q                                   → BUILD SUCCESS, 42 arquivos (PASSED)
notNullColumn="item_disciplina" no XML           → PRESENTE (PASSED — Pitfall 1)
jdbcType=OTHER count no XML                      → 18 (PASSED — Pitfall 2)
MatriculaRow: grep -c "public void|public boolean|public int" → 0 (PASSED)
implements MatriculaRepositorio                  → PRESENTE em MatriculaRepositorioMyBatis
@TransactionalEventListener em ambos os listeners → PRESENTE (PASSED)
@Bean em DomainServicesConfig                    → PRESENTE (PASSED)
@Service/@Component em VerificadorElegibilidade → AUSENTE no domínio (PASSED)
```

---

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] CPF inválido nos seeds substituído por CPF matematicamente válido**

- **Found during:** Task 1 — ao preparar o DemoRunner, verificou-se que `12345678901` (CPF do seed de Maria Silva especificado no RESEARCH.md) não passa o algoritmo módulo 11 implementado em `Cpf.java` (Plan 02). O `new Cpf("12345678901")` lançaria `IllegalArgumentException`.
- **Fix:** Substituído por `52998224725` — CPF matematicamente válido. Atualizado em `V2__seeds.sql` (campo `cpf` do aluno Maria) e em `DemoRunner.java` (construção do objeto `Aluno`).
- **Impacto:** Zero — CPFs nos seeds são dados de demonstração sem significado funcional. O UUID do aluno (`a0...001`) permanece inalterado — é ele que conecta o seed ao DemoRunner.
- **Files modified:** `V2__seeds.sql`, `DemoRunner.java`
- **Commits:** `1372d72` (seeds), `e418de9` (DemoRunner)

---

## Known Stubs

- `FinanceiroEventListener.aoMatricular` e `aoCancelar`: implementação real é v2 BC-01 (Bounded Context Financeiro). Stubs registram no log para verificação visual.
- `AcademicoEventListener.aoMatricular` e `aoDisciplinaAdicionada`: implementação real é v2 BC-02 (Bounded Context Acadêmico). Stubs registram no log.

Estes stubs são **intencionais e documentados**. Cumprem o requisito APL-05 — demonstrar o padrão `@TransactionalEventListener` sem implementar os Bounded Contexts adjacentes (fora do escopo desta fase).

---

## Checkpoint Pending

**Task 3 (checkpoint:human-verify):** O projeto está compilando com sucesso. Para verificar a execução completa, é necessário PostgreSQL disponível.

**Verificação solicitada:**

1. Inicie o PostgreSQL:
   ```
   docker run --name erp-postgres -e POSTGRES_DB=erp_matricula -e POSTGRES_USER=matricula -e POSTGRES_PASSWORD=matricula -p 5432:5432 -d postgres:16
   ```

2. Execute o projeto:
   ```
   cd erp-matricula-app && mvn spring-boot:run
   ```

3. Verifique nos logs:
   - `"Successfully applied 2 migrations"` (Flyway aplicou V1 e V2)
   - `"[DemoRunner] FLUXO 1: Matrícula criada com ID ..."` (UUID gerado)
   - `"[DemoRunner] FLUXO 2: Disciplina adicionada à matrícula ..."`
   - `"[DemoRunner] FLUXO 3: Matrícula ... cancelada"`
   - `"[BC Financeiro] Criando contrato de cobrança ..."` (listener stub ativado)
   - `"[BC Academico] Registrando vínculo aluno-turma ..."` (listener stub ativado)

4. Verificação estrutural final:
   ```
   grep -r "^import org.springframework" erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/
   ```
   → deve retornar **VAZIO**

---

## Threat Surface Scan

| Flag | File | Description |
|------|------|-------------|
| accept: sql-seeds-dev-only | V2__seeds.sql | Seeds com UUIDs fixos para desenvolvimento. Não devem rodar em produção — Fase 4 introduzirá profiles Spring. UUIDs fixos não são segredo (T-03-07 accepted). |
| accept: log-matricula-id | DemoRunner.java | Logs com MatriculaId no nível INFO — contexto de desenvolvimento. IDs de matrícula não são dados sensíveis neste contexto didático (T-03-08 accepted). |

---

## Self-Check

**Files exist:**

- FOUND: erp-matricula-app/src/main/resources/db/migration/V1__schema.sql
- FOUND: erp-matricula-app/src/main/resources/db/migration/V2__seeds.sql
- FOUND: erp-matricula-app/src/main/resources/mapper/MatriculaMapper.xml
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/persistencia/MatriculaRow.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/persistencia/ItemMatriculaRow.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/persistencia/MatriculaMapper.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/persistencia/typehandler/CpfTypeHandler.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/persistencia/MatriculaRowMapper.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/persistencia/MatriculaRepositorioMyBatis.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/eventos/FinanceiroEventListener.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/eventos/AcademicoEventListener.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/config/MyBatisConfig.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/config/DomainServicesConfig.java
- FOUND: erp-matricula-app/src/main/java/.../infraestrutura/config/DemoRunner.java

**Commits exist:**

- FOUND: `1372d72` — feat(03-04): schema SQL, seeds, MatriculaMapper.xml e Row Objects
- FOUND: `e418de9` — feat(03-04): TypeHandler, MatriculaRowMapper, Repositório, Listeners e DemoRunner

## Self-Check: PASSED
