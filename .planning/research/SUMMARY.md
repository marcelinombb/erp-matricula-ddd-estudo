# Research Summary — ERP Matrícula DDD Didático

**Sintetizado em:** 2026-06-20
**Confiança geral:** ALTA — todas as quatro dimensões de pesquisa convergem; versões verificadas no Maven Central; padrões confirmados em fontes autoritativas.

---

## Stack Recomendado

| Componente | Versão | Nota |
|---|---|---|
| Java | 21 LTS | Records, sealed classes, pattern matching — fundamentos do didático |
| Spring Boot | 3.5.3 | Versão mais recente estável (Maven Central, 2026-06-20) |
| mybatis-spring-boot-starter | 3.0.5 | Versão explícita obrigatória — não está no BOM do Spring |
| PostgreSQL JDBC | 42.7.x | Gerenciado pelo Spring Boot BOM |
| Flyway | 10.x (+ módulo postgresql) | Gerenciado pelo BOM; `flyway-database-postgresql` necessário no Flyway 10+ |
| Maven | Single-module | Multi-module: complexidade desnecessária para didático |

**Não usar:** JPA/Hibernate, Spring Data JPA, Spring Data JDBC, Lombok, MapStruct, MyBatis-Plus, Liquibase.
A exclusão de JPA não é preferência técnica — é o argumento pedagógico central: MyBatis força mapeamento explícito, tornando a fronteira domínio/persistência visível e educativa.

---

## Achados-Chave

### 1. A separação domínio/persistência é o argumento central — MyBatis a torna inegável

Com JPA, anotações como `@Entity`, `@Id`, `@OneToMany` entram nas classes de domínio, misturando preocupações de negócio e de persistência. Com MyBatis, o modelo de domínio é POJO puro; a tradução ocorre num `ResultMapper` explícito dentro de `infraestrutura/`. O desenvolvedor vê o mapeamento acontecer, entende o custo e o benefício. Este é o momento pedagógico mais poderoso do projeto, e toda escolha de stack serve a ele.

### 2. Java 21 entrega os três pilares de Value Object nativamente

Records eliminam o boilerplate de imutabilidade, `equals()` e `hashCode()` estrutural — as três propriedades que definem um Value Object DDD. Sealed classes modelam ciclos de vida com exaustividade garantida em tempo de compilação. Pattern matching expressa regras de transição de estado sem `instanceof` cascateado. Juntas, essas features transformam conceitos DDD abstratos em construtos da linguagem, não em disciplina de código.

### 3. O anti-padrão a combater tem nome e endereço: modelo de domínio anêmico

A pesquisa de pitfalls confirma que o risco #1 para desenvolvedores Spring Boot é colocar lógica de negócio em `@Service` e usar entidades como portadores de dados. O projeto precisa destruir esse reflexo com exemplos "antes/depois" explícitos em cada padrão tático. Sem essa comparação, o desenvolvedor aprende a estrutura DDD mas não internaliza o porquê.

### 4. A ordem de implementação é pedagógica, não apenas técnica

A sequência correta — Value Objects → Entidades → Agregado (com invariantes) → Domain Events → Interface do repositório → Casos de uso → Persistência → API — não é só uma questão de dependências de compilação. Ela reforça que o domínio existe antes do banco, que a interface do repositório pertence ao domínio (não à infraestrutura), e que a camada HTTP é a última borda a ser adicionada. Inverter essa ordem (banco primeiro, domínio depois) é uma das armadilhas mais documentadas de projetos "DDD".

### 5. Três tensões requerem decisões explícitas no roadmap

- **Consistência transacional vs. boundaries de agregado:** modificar `Matricula` e `Turma` na mesma transação viola o princípio de consistência por agregado. A solução correta — Domain Events com consistência eventual — precisa aparecer com motivação real, não apenas prescrita.
- **Queries de leitura vs. modelo de escrita:** o repositório salva agregados completos; queries de listagem precisam de projeções leves. O projeto deve mostrar os dois fluxos sem introduzir CQRS completo como requisito.
- **Invariantes no domínio vs. constraints no banco:** CPF único, período único por aluno — essas regras existem no Aggregate Java e como UNIQUE constraints no PostgreSQL. O projeto deve documentar explicitamente qual é a fonte da verdade e por quê.

---

## Padrões Essenciais a Demonstrar

Cada padrão deve aparecer com: (a) exemplo de código funcional, (b) exemplo do anti-padrão equivalente, (c) comentário explicando o trade-off. Nenhum pode ser omitido.

| Padrão | Artefato principal | Ponto pedagógico obrigatório |
|---|---|---|
| **Value Object** | `Cpf`, `PeriodoLetivo`, `MatriculaId` como `record` | Validação mora no construtor; imutabilidade garantida pelo compilador; `equals` por valor sem código manual |
| **Entidade** | `Aluno` com `AlunoId` tipado | `equals`/`hashCode` por identidade, não atributos; tipo próprio para ID rejeita confusão entre `AlunoId` e `MatriculaId` em tempo de compilação |
| **Aggregate Root** | `Matricula` com `List<ItemMatricula>` | Invariantes encapsuladas em `adicionarDisciplina()`, `cancelar()`; nenhum código externo toca `ItemMatricula` diretamente; repositório salva o agregado inteiro |
| **Sealed classes para estados** | `StatusMatricula` sealed interface | Switch exaustivo obrigado pelo compilador; estado desconhecido impossível em runtime |
| **Domain Events** | `AlunoMatriculado`, `DisciplinaAdicionada`, `MatriculaCancelada` como `record` | Eventos acumulados no agregado (lista interna); publicados pelo Application Service após persistência; domínio sem import Spring |
| **Repository (porta de saída)** | `MatriculaRepositorio` interface em `dominio/`; `MatriculaRepositorioMyBatis` em `infraestrutura/` | Interface define o contrato; infraestrutura obedece; inversão de dependência tangível na estrutura de pacotes |
| **Application Service** | `MatricularAlunoUseCase` / Handler | Orquestra sem decidir: busca agregado → chama método de domínio → salva → publica eventos. Nenhum `if/else` de negócio |
| **Domain Service** | `VerificadorElegibilidadeMatricula` | Lógica que não pertence a nenhuma entidade isolada, mas claramente é regra de negócio; comentário explícito no código justificando a escolha |
| **Exceções de domínio tipadas** | `LimiteDisciplinasExcedidoException(limite, atual)`, `DisciplinaJaMatriculadaException(DisciplinaId)` | Exceções com contexto; auto-documentáveis; testáveis individualmente |
| **Linguagem Ubíqua no código** | `buscarPorId()`, `adicionarDisciplina()`, `Matricula.criar()` | Código em português elimina a barreira cognitiva entre linguagem do negócio e linguagem do código |
| **MyBatis ResultMap com collection** | `MatriculaMapper.xml` com JOIN + `<collection notNullColumn>` | Reconstrução do agregado completo em uma query; conversão explícita no `RowMapper`; sem N+1 |
| **ADRs pedagógicos** | ADR-001 (MyBatis vs JPA), ADR-002 (escopo único BC), ADR-003 (referência por ID), ADR-004 (código PT) | Cada decisão de arquitetura é uma lição DDD; o "porquê" é mais valioso que o "o quê" |

---

## Principais Armadilhas a Evitar

### CRÍTICA 1 — Modelo de domínio anêmico
**Sinal:** `MatriculaService` com métodos `adicionarDisciplina()` que checam invariantes. `Matricula` com apenas getters/setters.
**Prevenção:** Invariantes vivem no método do próprio agregado. Application Service chama `matricula.adicionarDisciplina(disciplina)` — não re-implementa a regra.
**Detectar:** "Quem garante que uma matrícula nunca ultrapassa 6 disciplinas?" Se a resposta for o serviço, é modelo anêmico.

### CRÍTICA 2 — Infrastructure leakage no domínio
**Sinal:** `import org.springframework.*` ou `import org.apache.ibatis.*` dentro de `dominio/`. `@Autowired ApplicationEventPublisher` dentro de `Matricula`.
**Prevenção:** A camada `dominio/` usa apenas `java.util.*`, `java.time.*` e classes próprias do domínio. Zero annotations de framework.
**Detectar:** `grep -r "import org.springframework" src/main/java/*/dominio/` deve retornar vazio.

### CRÍTICA 3 — Confundir MyBatis Mapper com Repository DDD
**Sinal:** `MatriculaMapper` injetado diretamente em casos de uso. `MatriculaMapper.selectById()` chamado fora de `infraestrutura/`.
**Prevenção:** Nenhuma classe fora do pacote `infraestrutura/` referencia um `*Mapper`. A interface `MatriculaRepositorio` é o único ponto de acesso ao domínio.

### CRÍTICA 4 — Modificar dois agregados na mesma transação
**Sinal:** `@Transactional` com `matriculaRepo.salvar(matricula)` e `turmaRepo.salvar(turma)` na mesma transação de um caso de uso.
**Prevenção:** Usar Domain Events. `Matricula.confirmar()` publica `AlunoMatriculado`; um handler separado atualiza `Turma` em transação própria. Documentar a escolha de consistência eventual com ADR.

### CRÍTICA 5 — N+1 ao reconstruir agregados
**Sinal:** Loop que busca `ItemMatricula` para cada `Matricula` em lista.
**Prevenção:** JOIN + `<collection>` com `notNullColumn` no ResultMap do MyBatis. Uma query, uma viagem ao banco. `<id>` obrigatório nas coleções aninhadas para agrupamento correto.

### CRÍTICA 6 — Schema SQL desenhado antes do modelo de domínio
**Sinal:** Tabelas criadas no banco antes de existir uma linha de código de domínio. Schema guia o Java em vez do contrário.
**Prevenção:** A Fase 5 (Persistência) vem depois das Fases 1-4 (Domínio). Documentar a ordem deliberada.

### MODERADA 7 — Exemplos sem motivação (armadilha pedagógica)
**Sinal:** "Aqui está um Value Object" sem mostrar o problema que ele resolve.
**Prevenção:** Estrutura obrigatória para cada conceito: problema concreto → consequência real → solução DDD → trade-off honesto.

### MODERADA 8 — Aggregate grande demais ou pequeno demais
- **Grande demais:** `Matricula` carrega `Aluno` completo, `Turma` completa, histórico. Solução: referência por ID (`AlunoId`, não `Aluno`).
- **Pequeno demais:** `ItemMatricula` com repositório próprio, impossibilitando enforçar o limite de 6 disciplinas no agregado. Solução: `ItemMatricula` é entidade interna de `Matricula`, sem repositório próprio.

---

## Recomendações para o Roadmap

### Princípio de sequenciamento

O roadmap deve respeitar três gradientes simultâneos:
1. **Dependência de compilação:** Value Objects antes de Entidades antes de Agregados antes de Casos de Uso.
2. **Dependência pedagógica:** Conceito abstrato sempre apresentado com o problema que resolve, antes de sua implementação.
3. **Testabilidade incremental:** Cada fase deve produzir código testável antes da próxima começar (domínio puro testável sem banco, casos de uso testáveis com repositório mockado, integração testável com banco real).

### Fases sugeridas

**Fase 1 — Descoberta do Domínio (documentação pura)**
Problema de negócio, Linguagem Ubíqua, subdomínios Core/Supporting/Generic. Sem código ainda.
Cobre: PROJECT.md requisito "Fase 1". Risco: abstração prematura — manter tudo ancorado em exemplos concretos do domínio Matrícula.

**Fase 2 — Design Estratégico (documentação + decisões)**
Bounded Contexts, Context Map em Mermaid, ADRs fundadores (ADR-001 a ADR-004).
Cobre: PROJECT.md requisito "Fase 2". Resultado: diagrama do Context Map com Financeiro e Acadêmico como stubs downstream. Esta fase estabelece o escopo único do BC Matrícula — decisão que previne scope creep em fases posteriores.

**Fase 3 — Design Tático (conceitos com código de domínio)**
Value Objects (records), Entidades com IDs tipados, Sealed classes para StatusMatricula, Agregado Matrícula com invariantes e coleta de eventos, Domain Events (records), Domain Service de elegibilidade, Interface do repositório, Exceções de domínio tipadas.
Cobre: PROJECT.md requisito "Fase 3". Esta é a fase de maior densidade pedagógica. Cada padrão com anti-padrão lado a lado. Testes unitários do agregado nesta fase — demonstrar testabilidade como benefício, não como requisito de cobertura.

**Fase 4 — Modelagem Visual (diagramas Mermaid)**
Diagrama de classes do domínio, sequence diagrams dos três casos de uso, diagrama de estados da `Matricula`.
Cobre: PROJECT.md requisito "Fase 4". Pode ser desenvolvida em paralelo com Fase 3 ou imediatamente após.

**Fase 5 — Persistência PostgreSQL (schema + MyBatis)**
Schema SQL (com comentários documentando decisões como ausência de FK entre agregados), Flyway migrations, TypeHandlers para Value Objects, ResultMap com JOIN + `<collection>`, estratégia replace-all para coleções, seeds de demonstração.
Cobre: PROJECT.md requisito "Fase 5". Requer Fase 3 concluída. O ponto pedagógico central: `MatriculaRowMapper` como conversão explícita entre modelo relacional e modelo de domínio.

**Fase 6 — API funcional (Spring Boot end-to-end)**
Implementações dos repositórios (MyBatis), casos de uso com `@Transactional` na camada de aplicação, publicação de eventos via `ApplicationEventPublisher`, listeners stub (Financeiro, Acadêmico) com `@TransactionalEventListener`, controllers REST, `@ControllerAdvice` para exceções de domínio.
Cobre: PROJECT.md requisito "Fase 6". Fluxo completo: HTTP → Controller → UseCase → Agregado → Repositório → Mapper → SQL → Evento → Listener.

**Fase 7 — Docker Compose**
`docker-compose.yml` com PostgreSQL e aplicação. Scripts de seed automático via Flyway. Instruções de uso.
Cobre: PROJECT.md requisito "Fase 7". Baixa complexidade técnica; alta importância para experiência de uso do didático.

**Fase 8 — Comparação com Arquitetura em Camadas**
Implementação lado a lado do fluxo "matricular aluno" na arquitetura tradicional vs. DDD. Honesta sobre custos: mais arquivos, mais mapeamento, mais código de domínio. Quando DDD vale a pena e quando é overengineering.
Cobre: PROJECT.md requisito "Fase 8". Esta fase é o "momento aha" que determina se o desenvolvedor internaliza ou apenas reproduz a estrutura.

**Fase 9 — ADRs completos**
ADR-001 (MyBatis vs JPA), ADR-002 (escopo único BC), ADR-003 (referência por ID entre agregados), ADR-004 (código em português). Cada ADR com contexto, decisão, consequências e trade-offs honestos.
Cobre: PROJECT.md requisito "Fase 9".

**Fase 10 — Guia de Consulta**
Mapa conceito DDD → arquivo concreto do projeto. Glossário. Checklist de revisão de código (sinais de alerta por padrão). Context Map final com eventos cruzando fronteiras.
Cobre: PROJECT.md requisito "Fase 10". Transforma o projeto de leitura linear em referência de consulta.

### Flags de pesquisa por fase

| Fase | Pesquisa adicional necessária? | Motivo |
|---|---|---|
| 1-2 | Não — padrões bem documentados | Event Storming e Context Mapping têm literatura extensa e consensual |
| 3 | Não para padrões táticos; sim para invariantes específicas | Os padrões são estáveis; as invariantes do domínio Matrícula foram suficientemente detalhadas na pesquisa |
| 4 | Não — Mermaid tem sintaxe estável | |
| 5 | Sim — optimistic locking com MyBatis | Race conditions em agregados foram identificadas como risco moderado; solução com `version` column requer implementação cuidadosa sem suporte nativo do MyBatis |
| 6 | Não — Spring ApplicationEvents bem documentados | `@TransactionalEventListener` é padrão estável |
| 7-10 | Não — padrões estabelecidos | |

---

## Decisões em Aberto

### 1. Granularidade do Aggregate Matrícula vs. Turma

A pesquisa identificou que a invariante "turma não pode ultrapassar capacidade" cria tensão: se `Turma` for um aggregate separado, decrementar vagas ao matricular requer modificação de dois aggregates na mesma transação (violação de princípio). Se `Turma` for interno a `Matrícula`, o aggregate cresce além do necessário.

**Opções a decidir no planejamento:**
- (a) Ignorar vagas disponíveis no v1 — foco em invariantes de `Matrícula` apenas **(recomendado)**
- (b) `Turma` com repositório próprio, verificação como query somente leitura antes da criação, sem modificação transacional cruzada
- (c) Domain Service que verifica vagas antes da criação (leitura apenas), com `Turma` sendo atualizada via Domain Event em transação separada

A opção (a) é recomendada para o v1 — mantém o foco nas invariantes de `Matrícula` sem introduzir complexidade de boundary transacional antes que o desenvolvedor entenda os fundamentos.

### 2. Optimistic Locking para concorrência em agregados

Race conditions em `adicionarDisciplina()` (dois requests simultâneos adicionando a 6ª disciplina) foram identificadas como risco moderado. MyBatis não oferece `@Version` nativo como JPA.

**A decidir:** incluir coluna `version` e UPDATE condicional no v1, ou documentar como extensão futura com nota de trade-off? Recomendação: nota explicativa na Fase 5 com o padrão `UPDATE ... WHERE version = #{versaoEsperada}`, sem implementação completa no v1.

### 3. Estrutura dos testes unitários do agregado

A pesquisa recomenda testes unitários de `Matricula` para demonstrar testabilidade como benefício do DDD, sem exigir cobertura completa. Falta definir:
- Quais cenários de invariante são obrigatórios para o didático?
- Testes de Application Service com repositório mockado: sim ou não no v1?

### 4. Projeções de leitura vs. uso do agregado

Queries de listagem precisam de dados de `Aluno` (nome, CPF) que não fazem parte do agregado `Matrícula`. Três abordagens possíveis:
- (a) Query direta via Mapper retornando DTO de projeção — sem passar pelo Repository/Aggregate
- (b) Application Service carrega `Matrícula` + `Aluno` separadamente e monta o DTO
- (c) View SQL no PostgreSQL

Abordagem (a) é tecnicamente correta e demonstra o padrão de leitura direta via Mapper. A decidir no planejamento das Fases 5/6.

### 5. Profundidade do Context Map em código

O Context Map deve mostrar Financeiro e Acadêmico como contextos downstream. A pesquisa de arquitetura recomenda classes stub com `@TransactionalEventListener` e comentários Javadoc explicando o contrato — tornando o Context Map visível no código, não só na documentação. Confirmar no planejamento das Fases 2 e 6.

---

## Fontes Agregadas

**Confiança por área:**

| Área | Confiança | Base |
|---|---|---|
| Stack (versões) | ALTA | Maven Central verificado 2026-06-20 |
| Java 21 features | ALTA | JEPs 440, 441, 444 finalizados; documentação JDK oficial |
| Padrões táticos DDD | ALTA | Literatura canônica (Fowler, Evans); múltiplas fontes convergentes |
| MyBatis mapping patterns | ALTA | Documentação oficial mybatis.org (Context7) |
| Pitfalls pedagógicos | ALTA | Múltiplas fontes; confirmados por padrões anti-DDD documentados |
| Granularidade de agregados | MÉDIA | Consenso da comunidade; sem especificação normativa única |
| Optimistic locking com MyBatis | MÉDIA | Padrão prático estabelecido; sem documentação única autoritativa |
| Single-module vs multi-module | MÉDIA | Consenso; decisão contextual |

**Fontes primárias:**
- MyBatis Result Maps: https://mybatis.org/mybatis-3/sqlmap-xml.html
- Spring Boot 3.5 Release Notes: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes
- Hexagonal Architecture, DDD, Spring — Baeldung: https://www.baeldung.com/hexagonal-architecture-ddd-spring
- Anemic Domain Model — Martin Fowler: https://martinfowler.com/bliki/AnemicDomainModel.html
- DDD, Hexagonal, Onion, Clean, CQRS — Herberto Graca: https://herbertograca.com/2017/11/16/explicit-architecture-01-ddd-hexagonal-onion-clean-cqrs-how-i-put-it-all-together/
- Clean DDD Lessons (UNIL Engineering): https://medium.com/unil-ci-software-engineering/clean-ddd-lessons-project-structure-and-naming-conventions-00d0b9c57610
- Consistency Boundary: Aggregate — James Hickey: https://www.jamesmichaelhickey.com/consistency-boundary/
- mybatis-spring-boot-starter releases: https://github.com/mybatis/spring-boot-starter/releases
