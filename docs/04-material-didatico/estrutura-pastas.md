# Estrutura de Pastas — Propósito Pedagógico

Cada pacote reflete uma camada da arquitetura DDD. A localização do arquivo é uma declaração de intenção: arquivos no pacote `dominio/` nunca importam Spring; arquivos em `interfaces/` traduzem protocolos.

---

## Estrutura Java

```
erp-matricula-ddd/src/main/java/br/com/escola/matricula/
├── dominio/               — O coração do sistema. Zero imports de framework. Compilável sem Spring.
│   ├── excecao/           — Exceções tipadas com dados contextuais (não RuntimeException genérica)
│   ├── evento/            — Domain Events: records imutáveis representando fatos do negócio
│   ├── modelo/            — Entidades e Aggregate Root (Matricula, Aluno, Turma, ItemMatricula)
│   ├── repositorio/       — Interfaces de repositório: domínio DEFINE, infraestrutura IMPLEMENTA
│   ├── servico/           — Domain Services: lógica que cruza múltiplos Aggregates
│   └── vo/                — Value Objects: tipos que substituem primitivos com validação embutida
├── aplicacao/             — UseCases: orquestram sem decidir. Importam Spring para transações.
│   └── (Commands, DTOs, UseCases)
├── infraestrutura/        — Tudo que depende de tecnologia externa. Domínio não a conhece.
│   ├── config/            — Beans Spring, DemoRunner de demonstração
│   ├── eventos/           — Listeners de Domain Events (stubs de integração)
│   └── persistencia/      — MyBatis: Mappers XML, TypeHandlers, RowMappers, Row classes
└── interfaces/            — Fronteira HTTP. Traduz protocolo ↔ linguagem do domínio.
    ├── MatriculaController.java   — Três endpoints: POST /matriculas, /disciplinas, /cancelamento
    └── ExcecaoHandler.java        — Única saída de exceções de domínio para respostas HTTP
```

---

## Por que cada pacote está aqui — e o que você NUNCA verá lá

### `dominio/`

**Por que está aqui:** É o coração do sistema. Contém as regras de negócio, os invariantes, as definições de domínio. É o pacote que deveria poder existir antes de qualquer framework ser escolhido.

**O que você NUNCA verá aqui:** `@Autowired`, `@Component`, `@Entity`, `@Column`, `@Id`, `@Service`, nenhum import de `org.springframework.*`, `jakarta.persistence.*`, ou `org.apache.ibatis.*`.

Verifique: `grep -r "import org.springframework" dominio/` deve retornar zero resultados.

---

### `dominio/excecao/`

**Por que está aqui:** Exceções de negócio pertencem ao domínio — elas expressam violações de invariantes em termos do domínio. `LimiteDisciplinasExcedidoException` carrega `getLimite()` e `getAtual()` porque o domínio conhece o contexto do problema.

**O que você NUNCA verá aqui:** `@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)` — a semântica HTTP pertence à camada `interfaces/`. O domínio lança a exceção; a interface decide qual status HTTP retornar.

---

### `dominio/evento/`

**Por que está aqui:** Domain Events representam fatos que aconteceram no domínio. Eles pertencem ao domínio porque só o domínio sabe quando um fato importante ocorreu — quando uma matrícula foi criada, quando uma disciplina foi adicionada.

**O que você NUNCA verá aqui:** `@Component`, `@EventListener`, código de publicação. Os eventos são records imutáveis com dados do fato. Quem os publica é o UseCase; quem os recebe são os listeners em `infraestrutura/eventos/`.

---

### `dominio/modelo/`

**Por que está aqui:** Entidades e o Aggregate Root. `Matricula` é o Aggregate Root — o único ponto de entrada para modificar o estado do Aggregate. `Aluno`, `Turma` e `ItemMatricula` são entidades do domínio.

**O que você NUNCA verá aqui:** Lógica de persistência, queries SQL, conversão de tipos de banco. `Matricula.java` não sabe que o banco existe.

---

### `dominio/repositorio/`

**Por que está aqui:** Interfaces de repositório. O domínio define o contrato porque os UseCases precisam dessas interfaces para funcionar. A interface define os métodos em termos do domínio: `buscarPorAluno(UUID)` com nome expressivo, não `findByAlunoId(UUID)` com nome de framework.

**O que você NUNCA verá aqui:** `extends JpaRepository`, `import org.springframework.data`, `import org.apache.ibatis`. A interface é puro Java — sem dependência de framework.

---

### `dominio/servico/`

**Por que está aqui:** Domain Services contêm lógica que não pertence naturalmente a um único Aggregate. `VerificadorElegibilidadeMatricula` verifica se um aluno pode se matricular — lógica que envolve `Aluno` (está ativo?), `Turma` (período está aberto?) e `Matricula` (já existe matrícula ativa?).

**O que você NUNCA verá aqui:** Chamadas HTTP, acesso direto ao banco, lógica de persistência. O Domain Service trabalha com objetos de domínio e interfaces de repositório.

---

### `dominio/vo/`

**Por que está aqui:** Value Objects substituem primitivos com validação embutida e semântica clara. `Cpf` valida o dígito verificador no construtor — nunca existe um `Cpf` inválido no sistema. `PeriodoLetivo` valida ano e semestre — nunca existe um período inválido. `NomeDisciplina` valida comprimento e conteúdo.

**O que você NUNCA verá aqui:** Setters, identidade por referência, estado mutável. Value Objects são imutáveis por definição — criados uma vez, comparados por valor.

---

### `aplicacao/`

**Por que está aqui:** Application Services (UseCases) orquestram a sequência sem decidir. `MatricularAlunoUseCase` chama o Domain Service, cria o Aggregate, persiste e publica eventos — nessa ordem, sempre. A decisão de *se* pode matricular fica no Domain Service; a decisão de *como* é representado o estado fica no Aggregate.

**O que você NUNCA verá aqui:** `if (aluno.ativo)`, lógica de negócio direta, queries SQL. O UseCase apenas coordena. Se você encontrar um `if` com regra de negócio aqui, ele pertence ao domínio.

---

### `infraestrutura/`

**Por que está aqui:** Tudo que depende de tecnologia externa. Banco de dados, framework, mensageria. O domínio não conhece este pacote — a dependência vai em uma só direção: `infraestrutura/` depende de `dominio/`, nunca o contrário.

**O que você NUNCA verá aqui:** Regras de negócio, invariantes, lógica de domínio. Se `MatriculaRepositorioMyBatis` tem um `if` de negócio, ele está no lugar errado.

---

### `infraestrutura/persistencia/`

**Por que está aqui:** Implementações MyBatis dos repositórios de domínio. `MatriculaRepositorioMyBatis` implementa `MatriculaRepositorio` — a inversão de dependência em ação. `MatriculaRowMapper` é o ponto pedagógico central: único arquivo que conhece tanto o modelo relacional (`MatriculaRow`) quanto o modelo de domínio (`Matricula`).

**O que você NUNCA verá aqui:** Regras de negócio, lançamento de exceções de domínio diretamente. A conversão entre banco e domínio não envolve lógica de negócio — apenas mapeamento de dados.

---

### `interfaces/`

**Por que está aqui:** A fronteira HTTP. Traduz primitivos HTTP (Strings UUID, JSON) para objetos de domínio (Value Objects, Commands) e delega para os UseCases. `MatriculaController` não tem lógica de negócio — cada endpoint é basicamente: construir command, chamar usecase, retornar DTO.

**O que você NUNCA verá aqui:** `try/catch` para exceções de negócio (pertencem ao `ExcecaoHandler`), validações de regra de negócio, acesso direto ao banco.

---

## Estrutura de Resources

```
src/main/resources/
├── application.yml       — Configuração Spring Boot (datasource, MyBatis, logging)
├── db/migration/         — Flyway: V1 (schema), V2 (seeds), V3 (adicionada_em)
└── mapper/               — XMLs MyBatis: queries SQL e ResultMaps do Aggregate
```

O diretório `mapper/` separa as queries SQL do código Java — uma das vantagens pedagógicas do MyBatis sobre JPA: as queries estão explícitas e legíveis, não escondidas em anotações ou geradas por um framework.

---

## Estrutura de Docs

```
docs/
├── 01-design-estrategico/  — Problema de negócio, Linguagem Ubíqua, Bounded Contexts, Context Map
├── 02-design-tatico/       — Padrões táticos DDD documentados, diagramas Mermaid
├── adrs/                   — ADR-001..004: decisões arquiteturais com trade-offs
└── 04-material-didatico/   — Comparativo DDD vs camadas, Guia de Consulta, Lições Aprendidas (este diretório)
```

A documentação segue a mesma progressão do projeto: do design estratégico (problema de negócio, fronteiras de contexto) para o design tático (padrões, diagramas), passando pelas decisões arquiteturais (ADRs) e chegando ao material didático de consolidação.
