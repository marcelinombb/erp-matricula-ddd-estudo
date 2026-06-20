---
phase: 01-design-estrategico
reviewed: 2026-06-20T16:20:23Z
depth: standard
files_reviewed: 8
files_reviewed_list:
  - docs/01-design-estrategico/problema-negocio.md
  - docs/01-design-estrategico/linguagem-ubiqua.md
  - docs/01-design-estrategico/bounded-contexts.md
  - docs/01-design-estrategico/context-map.md
  - docs/adrs/ADR-001-mybatis-vs-jpa.md
  - docs/adrs/ADR-002-escopo-bounded-context.md
  - docs/adrs/ADR-003-referencia-por-id.md
  - docs/adrs/ADR-004-codigo-em-portugues.md
findings:
  critical: 2
  warning: 5
  info: 4
  total: 11
status: issues_found
---

# Fase 01: Relatório de Revisão — Design Estratégico

**Revisado:** 2026-06-20T16:20:23Z
**Profundidade:** standard
**Arquivos Revisados:** 8
**Status:** issues_found

## Resumo

Os oito documentos cobrem coerentemente o Design Estratégico do projeto didático DDD. A estrutura narrativa é clara, a justificativa pedagógica é consistente e os links entre documentos funcionam. Foram encontrados dois problemas críticos de precisão técnica que podem ensinar conceitos errados aos estudantes: o uso de terminologia Spring JDBC onde deveria ser MyBatis, e a referência a um "BC Aluno" inexistente na arquitetura. Há também cinco avisos de inconsistência interna que criam confusão quando os documentos são lidos em conjunto.

---

## Critical Issues

### CR-01: ADR-001 usa terminologia Spring JDBC, não MyBatis — `RowMapper` e `ResultSet` não são conceitos MyBatis

**File:** `docs/adrs/ADR-001-mybatis-vs-jpa.md:37,84,93,100,102`

**Issue:** O ADR-001 usa `MatriculaRowMapper`, `*RowMapper` e "transformar um `ResultSet`" para descrever como MyBatis faz o mapeamento. `RowMapper` é uma interface do **Spring JDBC** (`org.springframework.jdbc.core.RowMapper`) — não existe em MyBatis. No MyBatis, o mecanismo equivalente é um **Mapper interface** (anotado com `@Mapper`) combinado com um **XML ResultMap**. O desenvolvedor que seguir essa documentação e tentar criar uma classe `MatriculaRowMapper` não encontrará nenhuma interface MyBatis para implementar, e ao pesquisar `RowMapper` no MyBatis ficará confuso. Isso é especialmente danoso num projeto didático cujo ponto central é ensinar MyBatis corretamente.

**Fix:**
```
Substituir todas as ocorrências de "*RowMapper" por "*Mapper" (interface MyBatis) e
remover a referência a "transformar um ResultSet" (o desenvolvedor nunca toca ResultSet no MyBatis).

Linha 37:  // A conversão para/de banco ocorre em MatriculaMapper.xml (infraestrutura/)
Linha 84:  A conversão entre modelo relacional e modelo de domínio ocorre em interfaces
           `*Mapper` e arquivos XML dentro de `infraestrutura/` — nunca dentro do domínio.
Linha 93:  `MatriculaMapper` (infraestrutura) é o único lugar que define como o banco
           mapeia para o domínio, via ResultMap em `MatriculaMapper.xml`.
Linhas 100,102: Substituir `*RowMapper` por `*Mapper`.
```

---

### CR-02: ADR-003 referencia "BC Aluno" que não existe na arquitetura

**File:** `docs/adrs/ADR-003-referencia-por-id.md:42,53`

**Issue:** O ADR-003 usa a expressão "BC Aluno" em dois pontos ("modelo de domínio do BC Aluno", "BC Aluno renomear ou reestruturar sua tabela"). Segundo o glossário da Linguagem Ubíqua (`linguagem-ubiqua.md:11`) e o documento de Bounded Contexts, `Aluno` pertence ao **BC Matrícula** — não existe um Bounded Context separado chamado "BC Aluno" nesta arquitetura. Para um estudante lendo o ADR-003 em isolamento, essa referência cria a impressão de que existe um quinto BC (Aluno) além dos quatro definidos. Isso contradiz diretamente a estrutura que os demais documentos ensinam.

**Fix:**
```
Linha 42: "Acopla o BC Matrícula ao modelo de domínio de Aluno de outros contextos —
           qualquer mudança no modelo de Aluno em outro BC pode quebrar Matricula"

Linha 53: "Cria acoplamento de schema entre Bounded Contexts: se outro contexto renomear
           ou reestruturar sua tabela de alunos, o banco de dados de Matrícula quebra"

Alternativa mais precisa (reflete o que o projeto realmente modela):
Linha 42: "Acopla o BC Matrícula a um modelo de Aluno compartilhado — qualquer mudança
           nesse modelo afeta todos os contextos que o importam"
```

---

## Warnings

### WR-01: Definição de "Matrícula" inconsistente entre documentos (Aluno+PeriodoLetivo vs Aluno+Turma+PeriodoLetivo)

**File:** `docs/01-design-estrategico/linguagem-ubiqua.md:13`, `docs/01-design-estrategico/bounded-contexts.md:36,49`, `docs/01-design-estrategico/problema-negocio.md:24,32`

**Issue:** A definição canônica de Matrícula varia entre documentos:
- `linguagem-ubiqua.md:13` — "Vínculo de um Aluno a um **PeriodoLetivo**" (sem Turma)
- `bounded-contexts.md:49` — "Vínculo de um Aluno a um **PeriodoLetivo**" (sem Turma)
- `bounded-contexts.md:36` — "vínculo **aluno-turma-período**" (com Turma)
- `problema-negocio.md:24,32` — "matrícula para um aluno em **uma turma** em um período letivo"

A inconsistência importa pedagogicamente: se Turma faz parte da identidade da Matrícula (junto com Aluno e PeriodoLetivo), isso afeta a unicidade da matrícula, as invariantes e o schema. Se Turma é apenas uma referência conveniente, a definição curta "Aluno + PeriodoLetivo" está correta. O documento `linguagem-ubiqua.md:34` lista `turmaId` nos dados da Matrícula, confirmando que Turma É parte do Aggregate — mas a definição curta não o reflete.

**Fix:** Padronizar a definição curta em todos os documentos para:
> "Vínculo de um Aluno a uma Turma em um PeriodoLetivo"

Atualizar `linguagem-ubiqua.md:13` e `bounded-contexts.md:49` para incluir Turma.

---

### WR-02: ADR-002 código de exemplo usa `log.info` sem declarar o Logger

**File:** `docs/adrs/ADR-002-escopo-bounded-context.md:63`

**Issue:** O stub Java mostrado no ADR-002 usa `log.info(...)` na linha 63, mas nenhum campo `Logger log` ou anotação `@Slf4j` é declarado na classe de exemplo. Para um projeto didático voltado a desenvolvedores que "nunca aplicaram DDD de forma estruturada", código de exemplo com referência a uma variável não declarada é um ruído que desvia a atenção e pode gerar dúvidas. O estudante que tentar compilar o snippet receberá um erro de compilação imediato.

**Fix:**
```java
// Adicionar declaração de logger no início da classe
@Component
public class FinanceiroIntegracaoListener {

    private static final Logger log = LoggerFactory.getLogger(FinanceiroIntegracaoListener.class);

    @TransactionalEventListener
    public void aoAlunoMatriculado(AlunoMatriculadoEvent evento) {
        log.info("BC Financeiro recebeu AlunoMatriculado para aluno {}", evento.alunoId());
    }
}
```

---

### WR-03: Mermaid usa label `[Customer/Supplier]` na seta que sai do Supplier — ordem ambígua

**File:** `docs/01-design-estrategico/context-map.md:27,28`

**Issue:** As anotações das setas no diagrama Mermaid leem `[Customer/Supplier · OHS/PL]` com a seta apontando DE Matrícula (Supplier) PARA Financeiro/Acadêmico (Customer). A ordem "Customer/Supplier" no label sugere que o Customer vem primeiro — o que é contraintuitivo quando a seta representa o fluxo de eventos do Supplier para o Customer. O texto descritivo nas linhas 39-43 está correto, mas o diagrama que o estudante vê primeiro cria a impressão inversa. Em Context Maps DDD canônicos, o label normalmente descreve o relacionamento da perspectiva do nó de origem.

**Fix:**
```
Linha 27: M -- "AlunoMatriculado\nMatriculaCancelada\n[Supplier/Customer · OHS/PL]" --> F
Linha 28: M -- "AlunoMatriculado\nDisciplinaAdicionada\nMatriculaCancelada\n[Supplier/Customer · OHS/PL]" --> A
```

---

### WR-04: "Aluno deve estar ativo" e "Período letivo deve estar aberto" são pré-condições, não invariantes do Aggregate

**File:** `docs/01-design-estrategico/bounded-contexts.md:59-61`

**Issue:** A seção "Regras-chave protegidas pelo Aggregate" lista cinco itens, mas os dois últimos são conceitualmente diferentes dos três primeiros:

- Máximo de N disciplinas, sem duplicidade, sem adição após cancelamento — são **invariantes**: propriedades que devem ser verdadeiras em qualquer estado válido do Aggregate após sua criação.
- "Aluno deve estar ativo no momento da matrícula" e "Período letivo deve estar aberto para novas matrículas" — são **pré-condições de criação**: verificadas uma única vez antes de criar a matrícula. Após a criação, o Aggregate não mantém esses estados como parte de si mesmo.

Para estudantes aprendendo DDD tático (Aggregate, invariantes), essa distinção é fundamental: invariantes são protegidas pelo Aggregate em toda operação; pré-condições são verificadas pelo Application Service antes de delegar ao Aggregate. Misturá-las obscurece o design.

**Fix:** Separar as regras em duas subseções:
```
**Invariantes protegidas pelo Aggregate (verificadas em toda operação):**
- Máximo de N disciplinas por matrícula
- Sem disciplina duplicada na mesma matrícula
- Sem adição de disciplina após cancelamento

**Pré-condições verificadas na criação (responsabilidade do Application Service):**
- Aluno deve estar ativo no momento da matrícula
- Período letivo deve estar aberto para novas matrículas
```

---

### WR-05: ADR-002 texto em português mistura "and" em inglês

**File:** `docs/adrs/ADR-002-escopo-bounded-context.md:11`

**Issue:** O parágrafo de Contexto contém a frase: "é preciso lidar com consistência eventual, retentativas, idempotência de handlers, **and** circuit breakers". A palavra "and" em inglês no meio de um texto inteiramente em português é um erro de edição. Embora seja cosmético, em um projeto que defende explicitamente a coerência de idioma (ADR-004), a inconsistência tem peso.

**Fix:** Substituir "and" por "e":
> "é preciso lidar com consistência eventual, retentativas, idempotência de handlers, **e** circuit breakers"

---

## Info

### IN-01: "Disciplina" é usado extensivamente mas não tem entrada no Glossário

**File:** `docs/01-design-estrategico/linguagem-ubiqua.md:7-16`

**Issue:** `Disciplina` / `DisciplinaId` aparece em todas as operações de negócio (Adicionar Disciplina, `DisciplinaAdicionada`, limite de disciplinas por matrícula), mas o Glossário não tem entrada para esse termo. Qual é a diferença entre uma Disciplina e uma Turma? Uma Disciplina existe sem Turma? O limite de N disciplinas por matrícula é sobre instâncias de Disciplina ou sobre DisciplinaId únicos? Para estudantes construindo o modelo tático em fases posteriores, a ausência dessa definição é uma lacuna.

**Fix:** Adicionar ao Glossário:
```markdown
| Disciplina | Matéria/componente curricular oferecido dentro de uma Turma em um PeriodoLetivo.
              No BC Matrícula, referenciada apenas por `DisciplinaId`. O aluno adiciona
              Disciplinas à sua Matrícula dentro da mesma Turma/Período.
              | Matrícula | `Subject`, `Course`, `DisciplinaEntity` |
```

---

### IN-02: ADR-001 usa `ItemMatricula` / `itens` no código de exemplo, mas o modelo usa `disciplinas`

**File:** `docs/adrs/ADR-001-mybatis-vs-jpa.md:24,34`

**Issue:** O código de exemplo no ADR-001 mostra `List<ItemMatricula> itens` tanto no exemplo JPA (linha 24) quanto no exemplo MyBatis (linha 34). Os demais documentos usam consistentemente "lista de disciplinas" (`linguagem-ubiqua.md:34`), `adicionarDisciplina()`, e `DisciplinaId`. O nome `ItemMatricula` e o campo `itens` não aparecem em nenhum outro documento — sugerindo que o código de exemplo usa uma nomenclatura diferente do modelo de domínio que será implementado. Um estudante comparando o snippet do ADR com o glossário ficará confuso sobre se o nome correto é `ItemMatricula.itens` ou `DisciplinaId` / lista de disciplinas.

**Fix:** Atualizar o exemplo para refletir a nomenclatura canônica do domínio:
```java
// COM MYBATIS — modelo de domínio limpo (DECISÃO TOMADA)
public class Matricula {
    private final MatriculaId id;
    private final List<DisciplinaId> disciplinas;

    // Zero imports de framework — puro Java
}
```

---

### IN-03: bounded-contexts.md não explica a ausência de seções BC para Autenticação e Notificações

**File:** `docs/01-design-estrategico/bounded-contexts.md:27-28`

**Issue:** A tabela de Subdomínios lista seis entradas (Matrícula, Financeiro, Acadêmico, Secretaria, Autenticação, Notificações), mas apenas quatro têm seções de BC detalhadas. Generic Domains (Autenticação, Notificações) não têm seções BC — o que é correto (são delegados a soluções de mercado), mas o documento não explica explicitamente por que. Um estudante seguindo o documento esperará naturalmente encontrar "BC Autenticação" e "BC Notificações" e ficará se perguntando se são omissões.

**Fix:** Adicionar um parágrafo curto após a tabela de Subdomínios explicando:
> "Generic Domains não têm seções de BC neste documento porque não são implementados internamente — são delegados inteiramente a soluções de mercado (Keycloak para autenticação, SendGrid para e-mail). Não há BC a descrever porque não há código de domínio a escrever."

---

### IN-04: context-map.md descreve a interface pública do BC Matrícula apenas como "eventos de domínio"

**File:** `docs/01-design-estrategico/bounded-contexts.md:44`, `docs/01-design-estrategico/context-map.md:47`

**Issue:** Ambos os documentos afirmam que "a interface pública do BC Matrícula são seus eventos de domínio". Para um sistema single-module com Spring Boot, a interface pública real também inclui endpoints REST (Controller) e Application Services chamados por outros contextos dentro do mesmo processo. Os documentos não mencionam que no v1 (single-module, in-process) a comunicação entre contextos via `ApplicationEvents` é diferente de um OHS real, que geralmente implica HTTP ou mensageria. A `bounded-contexts.md:44` na verdade inclui a ressalva sobre não acessar tabelas ou métodos internos — mas não distingue entre integração in-process e integração real entre processos.

O ADR-002 Consequências (linha 83) reconhece este ponto para os stubs, mas não há advertência similar nos documentos de design estratégico.

**Fix:** Adicionar uma nota em `bounded-contexts.md` e `context-map.md` esclarecendo:
> "No v1 (aplicação single-module), os eventos são publicados in-process via `ApplicationEvents` do Spring. Em produção com serviços separados, o OHS seria implementado via HTTP ou mensageria. A estrutura do contrato (eventos e seus campos) é a mesma — o canal de entrega muda."

---

_Revisado: 2026-06-20T16:20:23Z_
_Revisor: Claude (gsd-code-reviewer)_
_Profundidade: standard_
