# Pitfalls Research — Java DDD (Matrícula Escolar)

**Domínio:** ERP Matrícula Escolar — projeto didático
**Pesquisado:** 2026-06-20
**Confiança geral:** HIGH (múltiplas fontes autoritativas confirmadas)

---

## Armadilhas do Modelo de Domínio

### CRÍTICA — Modelo de Domínio Anêmico

**O que dá errado:** Entidades como `Matricula`, `Aluno`, `Turma` viram portadores de dados com apenas getters e setters. Toda a lógica de negócio migra para `MatriculaService`, `AlunoService`, etc. O resultado é programação procedural disfarçada de OO.

**Por que acontece:** Desenvolvedores Spring Boot estão condicionados ao fluxo Controller → Service → Repository. O `@Service` se torna o lugar natural para "colocar a lógica", e as entidades se tornam "apenas o que vai pro banco".

**Sinal concreto no domínio Matrícula:**
```java
// ERRADO — lógica de negócio no serviço
public class MatriculaService {
    public void adicionarDisciplina(UUID matriculaId, UUID disciplinaId) {
        Matricula m = repositorio.buscar(matriculaId);
        if (m.getDisciplinas().size() >= 6) throw new Exception("Limite atingido");
        if (m.getSituacao().equals("CANCELADA")) throw new Exception("Cancelada");
        m.getDisciplinas().add(disciplinaId);
        repositorio.salvar(m);
    }
}

// CERTO — invariante protegida dentro do agregado
public class Matricula {
    public void adicionarDisciplina(Disciplina disciplina) {
        if (this.cancelada()) throw new MatriculaCanceladaException();
        if (this.disciplinas.size() >= LIMITE_DISCIPLINAS) throw new LimiteDisciplinasException();
        this.disciplinas.add(new ItemMatricula(disciplina));
        this.registrarEvento(new DisciplinaAdicionada(this.id, disciplina.getId()));
    }
}
```

**Consequências:** A lógica fica espalhada e duplicada entre serviços; invariantes do negócio ficam desprotegidas; qualquer serviço pode colocar a `Matricula` em estado inválido diretamente.

**Prevenção:**
- Regra de ouro: se um método usa apenas dados da própria entidade, ele pertence à entidade.
- Serviços de Aplicação devem ser "finos" — orquestram, não decidem.
- Checklist de revisão: "Quem garante que uma matrícula nunca ultrapassa 6 disciplinas?" Se a resposta for "o serviço", é um modelo anêmico.

**Fase onde abordar:** Fase 3 (Design Tático) — deve ser o primeiro padrão explicado, com exemplo "antes/depois" explícito.

---

### CRÍTICA — Infrastructure Leakage no Modelo de Domínio

**O que dá errado:** Anotações de infraestrutura entram na camada de domínio. No contexto deste projeto, o risco não é JPA diretamente (MyBatis foi escolhido para evitar isso), mas Spring annotations como `@Component`, `@Service`, `@Transactional`, ou dependências de frameworks dentro das classes de domínio.

**Sinal concreto:**
```java
// ERRADO — domínio dependendo de infraestrutura
@Entity  // JPA no domínio
@Table(name = "matriculas")
public class Matricula {
    @Id UUID id;
    @OneToMany List<ItemMatricula> itens;
}

// ERRADO — mesmo sem JPA, injeção de Spring no domínio
public class Matricula {
    @Autowired ApplicationEventPublisher publisher; // jamais!
}
```

**Por que o MyBatis ajuda:** A escolha intencional de MyBatis elimina a tentação de colocar `@Entity`/`@Column` nas entidades de domínio — que é a forma mais comum de contaminação com JPA. Esta é uma justificativa pedagógica central do projeto que precisa ser articulada claramente.

**Consequências:** O domínio passa a depender do framework; testes unitários exigem contexto Spring; mudar de ORM exige alterar o modelo de negócio.

**Prevenção:**
- Camada `domain/` não importa nenhuma classe de `org.springframework.*`, `org.mybatis.*`, ou `javax.persistence.*`.
- Validar com ArchUnit (ou checklist manual) durante revisão de código.
- No projeto didático: mostrar o contraste explícito entre a entidade de domínio limpa e o `ResultMap` do MyBatis na camada de infraestrutura.

**Fase onde abordar:** Fase 3 (Design Tático) para regras, Fase 6 (API) para validação empírica com MyBatis.

---

### MODERADA — Value Objects Tratados como Primitivos

**O que dá errado:** Campos como CPF, e-mail, período letivo ficam como `String` ou `LocalDate` primitivos espalhados por toda a codebase. Regras de validação e formatação são repetidas em múltiplos pontos.

**Sinal concreto no domínio:**
```java
// ERRADO — CPF como String solta
public class Aluno {
    private String cpf; // formato validado onde? por quem?
    private String email;
}

// CERTO — Value Object com invariante embutida
public final class Cpf {
    private final String valor;
    public Cpf(String valor) {
        if (!validarFormato(valor)) throw new CpfInvalidoException(valor);
        this.valor = normalizar(valor);
    }
    // equals/hashCode baseados em valor, não referência
}
```

**Consequência direta para Matrícula:** `PeriodoLetivo` (2025-1, 2025-2) é um ótimo Value Object — encapsula regras de qual período é válido, comparação entre períodos, etc. Se virar `String`, essa lógica se espalha pelos serviços.

**Prevenção:**
- Identificar VOs no Event Storming antes de escrever código.
- Regra: imutável + igualdade por valor + valida ao construir = Value Object candidato.
- Java records são naturalmente Value Objects — usar quando possível.

**Fase onde abordar:** Fase 3 (Design Tático) — seção específica de Value Objects com exemplos `Cpf`, `PeriodoLetivo`, `Email`.

---

### MODERADA — Entidade vs. Value Object Confusão

**O que dá errado:** O desenvolvedor cria IDs para tudo por hábito de banco de dados. `Disciplina` dentro de uma matrícula vira uma entidade com `id` próprio quando deveria ser um Value Object `ItemMatricula`.

**Consequência:** Proliferação de entidades desnecessárias, repositórios desnecessários para "sub-entidades", e perda do benefício de VO.

**Prevenção:** Perguntar sempre: "Dois objetos com os mesmos valores são o mesmo objeto ou objetos distintos?" Se sim, é VO. Se a identidade importa além dos valores, é Entidade.

**Fase onde abordar:** Fase 3 (Design Tático).

---

## Armadilhas com MyBatis

### CRÍTICA — Confundir Mapper com Repository

**O que dá errado:** O desenvolvedor trata o `MatriculaMapper` (interface MyBatis) diretamente como o Repository DDD. Serviços de aplicação injetam `MatriculaMapper` e chamam `mapper.selectById()` diretamente — pulando a camada de domínio.

**Sinal concreto:**
```java
// ERRADO — serviço de aplicação acessa mapper diretamente
public class MatricularAlunoUseCase {
    @Autowired MatriculaMapper mapper; // infraestrutura no caso de uso!

    public void executar(MatricularAlunoCommand cmd) {
        MatriculaRow row = mapper.selectById(cmd.matriculaId());
        // lógica de negócio aqui...
        mapper.update(row);
    }
}

// CERTO — serviço de aplicação usa interface de repositório (domínio)
public class MatricularAlunoUseCase {
    private final MatriculaRepository repositorio; // interface no domínio

    public void executar(MatricularAlunoCommand cmd) {
        Matricula matricula = repositorio.buscar(cmd.matriculaId());
        matricula.adicionarDisciplina(...);
        repositorio.salvar(matricula);
    }
}

// Na infraestrutura: implementação que usa o Mapper
public class MatriculaRepositoryImpl implements MatriculaRepository {
    private final MatriculaMapper mapper;
    // mapeamento de domínio <-> linhas do banco aqui
}
```

**Por que acontece:** MyBatis expõe seus Mappers como beans Spring prontos para injeção. É tentador usá-los diretamente.

**Consequências:** Lógica de negócio exposta a detalhes de persistência; impossível trocar de banco ou ORM sem alterar casos de uso; o Mapper vira um "Repositório gordo" com métodos de negócio misturados.

**Prevenção:** Regra de ouro — nenhuma classe fora do pacote `infrastructure/` deve referenciar um `*Mapper` do MyBatis. A interface `MatriculaRepository` vive em `domain/`; `MatriculaMapper` vive em `infrastructure/`.

**Fase onde abordar:** Fase 6 (API) — mostrar a separação explícita `Repository interface (domain) → Mapper (infrastructure)` como ponto pedagógico central.

---

### CRÍTICA — N+1 ao Reconstruir Agregados

**O que dá errado:** O `MatriculaRepositoryImpl` busca a `Matricula` com uma query, depois faz queries separadas para cada `ItemMatricula` (disciplinas), para cada `DomainEvent` pendente, etc. Para uma lista de 50 matrículas, isso gera 150+ queries.

**Sinal concreto:**
```java
// ERRADO — N+1 disfarçado
public Matricula buscar(MatriculaId id) {
    MatriculaRow row = mapper.selectById(id.valor()); // 1 query
    List<ItemMatriculaRow> itens = itemMapper.selectByMatriculaId(id.valor()); // 1 query por matrícula
    // Para 50 matrículas = 51 queries
}

// CERTO — JOIN com resultMap aninhado no MyBatis
// No XML: resultMap com <collection> usando JOIN
// Uma query com LEFT JOIN item_matricula ON matricula.id = item_matricula.matricula_id
```

**Particularidade do MyBatis:** O MyBatis tem suporte nativo para `<collection>` e `<association>` em `resultMap` que resolvem N+1 via JOIN. O `<id>` no resultMap aninhado é crítico — sem ele, o MyBatis não agrupa corretamente as linhas do JOIN e cria objetos duplicados.

**Prevenção:**
- Mapear agregados com JOIN + `resultMap` hierárquico no XML.
- Sempre definir `<id>` nas coleções aninhadas.
- Para queries de leitura sem necessidade de reconstruir o agregado completo, usar projeções diretas (DTOs de leitura via Mapper, sem passar pelo Repository).

**Fase onde abordar:** Fase 5 (Persistência) e Fase 6 (API) — com exemplo de `resultMap` completo do agregado `Matricula`.

---

### MODERADA — Não Separar Modelo de Leitura e Escrita

**O que dá errado:** O mesmo `MatriculaRepository` é usado para salvar agregados completos E para queries de relatório/listagem que precisam de dados desnormalizados de múltiplas tabelas. O agregado cresce para acomodar todos os casos de leitura.

**Sinal concreto:**
```java
// ERRADO — agregado inflado para suportar queries de UI
public class Matricula {
    private NomeAluno nomeAluno; // só existe para a tela de listagem
    private NomeCurso nomeCurso; // não faz parte das invariantes
}

// CERTO — leitura direta com DTO de projeção
public interface MatriculaQueryMapper {
    List<MatriculaResumoDTO> listarPorAluno(UUID alunoId);
    MatriculaDetalheDTO buscarDetalhe(UUID matriculaId);
}
```

**Prevenção:** Separar claramente dois fluxos: (1) Commands → Repository → Aggregate → regras de negócio; (2) Queries → Mapper direto → DTO de leitura. O MyBatis é particularmente bem adequado para o lado de leitura com SQL customizado.

**Fase onde abordar:** Fase 6 (API) — introduzir o conceito CQRS leve como necessidade natural, não como padrão imposto.

---

### MENOR — `resultMap` Sem `<id>` em Coleções Aninhadas

**O que dá errado:** O MyBatis usa a coluna `<id>` para identificar quando criar um novo objeto vs. adicionar ao existente durante JOINs. Sem `<id>` correto, um JOIN que retorna 3 linhas para uma matrícula com 3 disciplinas cria 3 objetos `Matricula` em vez de 1 com 3 disciplinas.

**Prevenção:** Sempre mapear `<id column="matricula_id" property="id"/>` nos `resultMap` de agregados.

**Fase onde abordar:** Fase 5 (Persistência SQL) / Fase 6 (API).

---

## Armadilhas Pedagógicas

### CRÍTICA — Introduzir Conceitos Sem Motivação

**O que dá errado:** O projeto apresenta "Aggregate Root" ou "Value Object" como definição antes de mostrar o problema que eles resolvem. O desenvolvedor decora o padrão sem entender por que existe.

**Sintoma:** Desenvolvedores conseguem criar um `Cpf` record mas não sabem por que não usar `String`.

**Como evitar neste projeto:**
Cada conceito DDD deve seguir a sequência:
1. Mostrar o problema concreto ("O que acontece se `cpf` for `String`?")
2. Mostrar a consequência real ("Validação duplicada em 4 serviços")
3. Introduzir o padrão como solução ("Value Object resolve isso assim...")
4. Mostrar o trade-off ("O custo é: mais código na criação, mas zero duplicação de regra")

**Fase onde abordar:** Transversal — deve ser uma decisão editorial aplicada a todas as fases de documentação (Fases 1-4, 8).

---

### CRÍTICA — Nenhum Paralelo com Arquitetura Tradicional

**O que dá errado:** O projeto ensina DDD "no vácuo", sem mostrar como o mesmo problema seria resolvido na arquitetura Controller → Service → Repository tradicional. O desenvolvedor não tem âncora para comparação.

**Sintoma:** Desenvolvedores que leram o projeto conseguem reproduzir a estrutura mas voltam ao padrão antigo em novos projetos porque não internalizaram o porquê.

**Como evitar neste projeto:**
- Fase 8 (Comparação) é crítica — deve existir com exemplos linha-a-linha reais.
- Mostrar o mesmo fluxo "matricular aluno" em duas versões: Service tradicional vs. Application Service + Aggregate.
- Explicar o que muda, o que permanece igual (Spring, HTTP, banco), e por que a mudança importa.

**Fase onde abordar:** Fase 8 explicitamente, mas referências transversais durante Fases 3-6.

---

### CRÍTICA — Omitir Trade-offs Reais

**O que dá errado:** O projeto apresenta DDD como solução superior sem admitir os custos reais. Desenvolvedores adotam DDD em projetos simples onde é overengineering, ou abandonam quando enfrentam dificuldade porque não foram preparados.

**Trade-offs honestos que o projeto precisa abordar:**
- DDD aumenta a quantidade de código e arquivos (mais classes, mais mapeamentos)
- MyBatis exige mais SQL escrito manualmente que JPA
- Sem testes, as invariantes do agregado são fé, não garantia
- Reconstruir agregados grandes via SQL JOIN é mais complexo que `findById()` com JPA
- Bounded Contexts adicionam complexidade de integração (eventos entre contextos)

**Fase onde abordar:** Fase 9 (ADRs) deve capturar trade-offs explícitos. Fase 8 (Comparação) deve ser honesta sobre custos.

---

### MODERADA — Exemplos Demasiado Abstratos ou Triviais

**O que dá errado:** O domínio é simplificado ao ponto de o padrão não fazer sentido. Um `Order` com um `Product` não demonstra por que Aggregates existem. Uma `Matricula` que só tem um aluno e uma disciplina não mostra invariantes reais.

**Para este projeto:** O domínio Matrícula Escolar foi bem escolhido — tem invariantes ricas:
- Limite de disciplinas por matrícula
- Período letivo que determina quais turmas estão disponíveis
- Regras de cancelamento (não pode cancelar se já iniciou)
- Transição de estados (rascunho → ativa → cancelada/concluída)

**Prevenção:** Usar invariantes reais desde o início. Não simplificar o domínio para facilitar o exemplo — o domínio complexo é a justificativa para DDD.

**Fase onde abordar:** Fase 3 (Design Tático) — o Aggregate `Matricula` deve ter invariantes não-triviais desde o início.

---

### MODERADA — Nenhum "Momento Aha" Planejado

**O que dá errado:** Workshops DDD criam entusiasmo inicial que não se sustenta. O projeto precisa de pontos de ancoragem onde o valor de DDD é tangível, não apenas declarado.

**Momentos Aha a planejar:**
- Quando o desenvolvedor vê que o Aggregate rejeitou um estado inválido sem que nenhum serviço precisasse checar (invariante protegida).
- Quando vê que a interface de domínio `MatriculaRepository` não mudou, mas a implementação MyBatis pode ser trocada por qualquer outra.
- Quando lê `matricula.cancelar()` e entende a intenção sem ler a implementação.

**Fase onde abordar:** Fases 3 e 6 — documentação deve sinalizar explicitamente esses momentos.

---

### MENOR — Nenhum Guia de Consulta Rápida

**O que dá errado:** O projeto é estudado uma vez e não é revisitado como referência porque a estrutura não permite consulta rápida ("Onde fica o Domain Service mesmo?").

**Prevenção:** Fase 10 (Guia de Consulta) é essencial — mapa de conceito → arquivo concreto.

**Fase onde abordar:** Fase 10.

---

## Armadilhas de Arquitetura

### CRÍTICA — Serviços de Aplicação que Sabem Demais

**O que dá errado:** O `MatricularAlunoUseCase` (Application Service) contém regras de negócio que deveriam estar no Aggregate ou em um Domain Service. O serviço de aplicação vira um segundo modelo anêmico, mas desta vez na camada de aplicação.

**Sinal concreto:**
```java
// ERRADO — regra de negócio no serviço de aplicação
public class MatricularAlunoUseCase {
    public void executar(MatricularAlunoCommand cmd) {
        Aluno aluno = alunoRepo.buscar(cmd.alunoId());
        Turma turma = turmaRepo.buscar(cmd.turmaId());

        // REGRA DE NEGÓCIO AQUI? Não!
        if (turma.getVagasDisponiveis() <= 0) throw new TurmaLotadaException();
        if (!aluno.getPeriodo().equals(turma.getPeriodo())) throw new PeriodoIncompativelException();

        Matricula matricula = new Matricula(aluno.getId(), turma.getId());
        matriculaRepo.salvar(matricula);
    }
}

// CERTO — serviço de aplicação orquestra; regras ficam no domínio
public class MatricularAlunoUseCase {
    public void executar(MatricularAlunoCommand cmd) {
        Aluno aluno = alunoRepo.buscar(cmd.alunoId());
        Turma turma = turmaRepo.buscar(cmd.turmaId());

        // Matrícula protege suas próprias invariantes
        Matricula matricula = Matricula.criar(aluno.getId(), turma);
        matriculaRepo.salvar(matricula);
    }
}
```

**Prevenção:** Application Service deve: buscar agregados, chamar métodos de domínio, salvar resultado, publicar eventos. Não deve: tomar decisões de negócio.

**Fase onde abordar:** Fase 3 (Design Tático) + Fase 6 (API).

---

### CRÍTICA — Boundary Violation: Modificar Múltiplos Aggregates em Uma Transação

**O que dá errado:** Um caso de uso modifica `Matricula` e `Turma` (decrementando vagas) na mesma transação `@Transactional`. Isso viola o princípio de que um Aggregate é a unidade de consistência transacional.

**Sinal concreto:**
```java
@Transactional
public void matricular(MatricularAlunoCommand cmd) {
    Matricula matricula = matriculaRepo.buscar(...);
    Turma turma = turmaRepo.buscar(...); // segundo aggregate
    matricula.confirmar();
    turma.decrementarVagas(); // modificação cruzada!
    matriculaRepo.salvar(matricula);
    turmaRepo.salvar(turma); // dois aggregates, uma transação
}
```

**Consequências:** Locks de banco mais amplos, acoplamento temporal entre aggregates, dificuldade de escalar independentemente.

**Prevenção correta:** Usar Domain Events para propagação eventual. `Matricula.confirmar()` publica `AlunoMatriculado`; um handler atualiza `Turma` em transação separada. A documentação do projeto deve mostrar quando consistência eventual é aceitável vs. quando redesenhar boundaries.

**Fase onde abordar:** Fase 3 (Design Tático) — ao introduzir Domain Events; Fase 9 (ADRs) — decisão explícita de boundary.

---

### MODERADA — Aggregate Raiz Grande Demais

**O que dá errado:** Tudo relacionado a matrícula entra em `Matricula`: dados do aluno, dados das turmas, histórico completo, documentos. O aggregate vira um objeto de 20+ campos que deve ser carregado inteiro para qualquer operação.

**Sinal:** Se para cancelar uma matrícula você precisa carregar lista de disciplinas, dados do aluno, e histórico de eventos, o aggregate está grande demais para a operação.

**Para este projeto:** `Matricula` guarda `AlunoId` (não `Aluno`), `TurmaId` (não `Turma`). Referência por ID é a solução certa e um ponto pedagógico explícito já identificado nas Key Decisions do projeto.

**Prevenção:** Cada operação de negócio deve precisar apenas dos dados que a `Matricula` própria guarda. Dados de outros contextos chegam via eventos ou são consultados separadamente.

**Fase onde abordar:** Fase 2 (Design Estratégico) para boundary decisions; Fase 3 (Design Tático) para o padrão de referência por ID.

---

### MODERADA — Aggregate Raiz Pequena Demais

**O que dá errado:** O desenvolvedor, com medo de fazer o aggregate grande demais, fragmenta demais. `ItemMatricula` vira um Aggregate próprio com repositório próprio. Qualquer operação que envolva "adicionar disciplina à matrícula" agora requer coordenação entre dois repositórios.

**Prevenção:** O aggregate deve ser grande o suficiente para garantir todas as suas invariantes de negócio. Se a invariante é "matrícula não pode ter mais de 6 disciplinas", `ItemMatricula` deve ser parte de `Matricula`, não um aggregate separado.

**Fase onde abordar:** Fase 3 (Design Tático).

---

### MODERADA — Domain Events Usados como Simple Callbacks

**O que dá errado:** Domain Events são usados apenas para notificação de outro serviço no mesmo contexto, dentro da mesma transação, sem garantia de entrega. O `AlunoMatriculado` é publicado e consumido síncronamente por um handler que também faz parte da mesma transação — o que é basicamente uma chamada de método disfarçada.

**Problema real com Spring:** `ApplicationEventPublisher` publica eventos síncronos por padrão. `@TransactionalEventListener` garante que o listener só executa após commit, mas sem retentativa em caso de falha. O Outbox Pattern é a solução robusta para eventos cross-context, mas adiciona complexidade que está fora de escopo deste projeto.

**Para este projeto didático:** Usar Domain Events para demonstrar o padrão de propagação entre contextos (Matrícula → Financeiro → Acadêmico), mas ser explícito na documentação sobre o que é simplificado e quais trade-offs existem em produção real.

**Fase onde abordar:** Fase 3 (Design Tático) — introdução dos Domain Events com limitações explicadas honestamente.

---

### MENOR — Organização de Pacotes por Tipo Técnico, não por Domínio

**O que dá errado:** Estrutura de pacotes reflete camadas técnicas globais em vez de domínio:
```
❌  com.empresa.erp
    ├── entities/        (todos os Entity de todos os contextos)
    ├── services/        (todos os Service)
    ├── repositories/    (todos os Repository)
    └── controllers/     (todos os Controller)
```

O correto para este projeto:
```
✓   com.empresa.matricula
    ├── domain/
    │   ├── model/       (Matricula, Aluno, Turma — classes de domínio)
    │   ├── repository/  (interfaces MatriculaRepository)
    │   └── event/       (AlunoMatriculado, MatriculaCancelada)
    ├── application/
    │   ├── usecase/     (MatricularAlunoUseCase)
    │   └── dto/         (commands, results)
    ├── infrastructure/
    │   ├── persistence/ (MatriculaRepositoryImpl, MyBatis Mappers)
    │   └── config/      (MyBatis config, Spring beans)
    └── interfaces/
        └── rest/        (MatriculaController)
```

**Fase onde abordar:** Fase 6 (API) — a estrutura de pastas é um entregável explícito desta fase.

---

## Armadilhas de Persistência

### CRÍTICA — Schema SQL Desenhado Antes do Modelo de Domínio

**O que dá errado:** O desenvolvedor abre o DataGrip ou pgAdmin e começa a criar tabelas antes de ter o modelo de domínio definido. O schema SQL passa a guiar o código Java — o inverso do correto.

**Consequências diretas para Matrícula Escolar:**
- Tabela `aluno` normalizada para terceira forma normal → não há um Aggregate natural para `Matricula`
- FK de `matricula` para `aluno`, `turma`, `curso` → o desenvolvedor sente que precisa carregar tudo junto
- Campos da tabela `matricula` espelham exatamente os campos da entidade Java → não há separação de modelo

**O fluxo correto:**
1. Event Storming / descoberta de domínio → entender o negócio
2. Design de Aggregates e Value Objects → definir o modelo de domínio
3. Código Java das entidades de domínio → sem pensar em banco
4. Só então: traduzir o modelo de domínio para schema SQL

**Por que MyBatis reforça isso:** MyBatis com mapeamento explícito deixa claro que a tabela `matriculas` e a classe `Matricula` são coisas diferentes que precisam ser mapeadas. JPA com `@Entity` borram essa distinção.

**Prevenção:** A Fase 5 (Persistência) deve vir depois das Fases 1-4 (Domínio). A ordenação de fases do projeto já reflete isso corretamente.

**Fase onde abordar:** Fase 5 (Persistência) — documenta a ordem correta explicitamente e justifica.

---

### MODERADA — Normalização Prematura que Quebra Boundaries de Aggregate

**O que dá errado:** O DBA aplica normalização rigorosa ao schema, criando tabelas granulares que não respeitam os limites do Aggregate. `item_matricula` é referenciado diretamente por outros serviços; `turma_disciplina` tem FK para `disciplina` que é usada por vários contextos.

**Consequências:** Para reconstruir o Aggregate `Matricula`, são necessários JOINs complexos de 5+ tabelas; qualquer mudança no schema afeta múltiplos contextos; o repositório se torna um conjunto de queries SQL complexas.

**Prevenção:** Deixar o modelo de domínio guiar a granularidade do schema. Um Aggregate pode ser persistido em múltiplas tabelas, mas as tabelas internas de um Aggregate não devem ser referenciadas diretamente por outros Aggregates (use a FK para a raiz do Aggregate, não para entidades internas).

**Fase onde abordar:** Fase 5 (Persistência PostgreSQL).

---

### MODERADA — Validação Duplicada: Domínio + Banco

**O que dá errado:** A invariante "matrícula não pode ter mais de 6 disciplinas" existe no Aggregate Java E como constraint `CHECK` no PostgreSQL. Quando o schema evolui (limite muda para 8), é necessário alterar em dois lugares. Se os dois ficarem fora de sincronia, o banco rejeita operações que o domínio deveria aceitar.

**Prevenção:** O domínio é a fonte da verdade para regras de negócio. O banco valida integridade estrutural (NOT NULL, FK, tipos), não regras de negócio. Constraints de negócio no banco são uma camada de segurança, não a camada primária.

**Decisão para este projeto:** Documentar explicitamente quais constraints do schema são integridade referencial (corretas no banco) vs. quais são regras de negócio (pertencem ao Aggregate).

**Fase onde abordar:** Fase 5 (Persistência) e Fase 9 (ADRs).

---

### MODERADA — Race Conditions em Aggregates Sem Controle de Concorrência

**O que dá errado:** Dois requests simultâneos tentam adicionar a 6ª disciplina à mesma matrícula. Ambos leem o estado atual (5 disciplinas), ambos passam na validação, ambos salvam — resultado: 7 disciplinas.

**Solução para este projeto:** Optimistic locking via coluna `version` na tabela `matriculas`. MyBatis não oferece optimistic locking automático (ao contrário do JPA `@Version`), então deve ser implementado manualmente:
```sql
UPDATE matriculas SET ..., version = version + 1
WHERE id = #{id} AND version = #{versaoEsperada}
```
Se `rowsAffected == 0`, outro processo modificou o registro — lançar exceção de conflito.

**Fase onde abordar:** Fase 5 (Persistência) e Fase 6 (API) — como extensão avançada ou nota explicativa.

---

### MENOR — Linguagem Ubíqua Quebrada no SQL

**O que dá errado:** Tabelas e colunas recebem nomes técnicos ou em inglês que não correspondem à Linguagem Ubíqua do domínio. `enrollment` em vez de `matricula`, `student_registration_items` em vez de `item_matricula`.

**Consequências pedagógicas:** Desenvolvedores lendo o schema não reconhecem o domínio; a ponte entre modelo de domínio e schema SQL fica opaca; o propósito de MyBatis com mapeamento explícito se perde.

**Para este projeto:** A escolha de código em português estende-se ao schema SQL. `CREATE TABLE matriculas`, `CREATE TABLE itens_matricula`, coluna `situacao_matricula`, etc.

**Fase onde abordar:** Fase 5 (Persistência PostgreSQL).

---

## Sinais de Alerta e Prevenção

### Tabela de Sinais por Área

| Sinal de Alerta | Diagnóstico | Prevenção | Fase |
|-----------------|-------------|-----------|------|
| Service com mais de 3 métodos que modificam estado | Modelo anêmico | Mover lógica para o Aggregate | 3 |
| Entidade com getters/setters públicos para todos os campos | Domínio exposto | Encapsular; expor apenas o necessário para o aggregate | 3 |
| `import org.springframework.*` dentro de `domain/` | Infrastructure leakage | ArchUnit ou revisão de imports | 3, 6 |
| `import org.mybatis.*` dentro de `domain/` ou `application/` | Infrastructure leakage | Mesma regra — Mapper só em `infrastructure/` | 6 |
| Serviço de aplicação com `if/else` de negócio | Lógica no lugar errado | Mover para Aggregate ou Domain Service | 3, 6 |
| Mapper MyBatis injetado fora de `infrastructure/` | Mapper confundido com Repository | Interface Repository no domínio | 6 |
| `SELECT` para cada item em um loop | N+1 | JOIN com resultMap aninhado | 5, 6 |
| Classe Java com mesmo nome e estrutura que tabela SQL | Schema-first thinking | Domínio primeiro, schema depois | 3, 5 |
| `@Transactional` modificando dois Aggregates diferentes | Boundary violation | Domain Events + consistência eventual | 3 |
| Value Object com `setXxx()` público | VO mutável | Tornar imutável; construtor completo | 3 |
| Campo `cpf: String` na entidade de domínio | Primitivo onde deveria ser VO | Criar `Cpf` value object | 3 |
| Coluna SQL `CHECK (count(...) <= 6)` para regra de negócio | Duplicação domínio/banco | Invariante no Aggregate; banco só valida FK/NOT NULL | 5 |
| Documentação que define o padrão sem mostrar o problema | Armadilha pedagógica | Problema → consequência → solução → trade-off | 1-4 |
| Nenhuma seção "antes/depois" no projeto | Armadilha pedagógica | Fase 8 obrigatória | 8 |

---

### Estratégias de Prevenção por Fase

**Fase 1 — Descoberta do Domínio:**
- Não começar com diagramas de classe ou tabelas SQL.
- Usar linguagem de negócio pura — "o que acontece quando um aluno se matricula?" antes de qualquer código.
- Identificar termos que mudam de significado entre contextos (Aluno no contexto de Matrícula vs. no contexto Acadêmico).

**Fase 2 — Design Estratégico:**
- Decidir referência por ID entre Aggregates antes de escrever qualquer código.
- Documentar o Context Map com setas de dependência explícitas.
- Identificar o Core Domain (Matrícula) e justificar por que os outros (Financeiro, Acadêmico) são Supporting ou Generic.

**Fase 3 — Design Tático:**
- Cada conceito introduzido com: problema → solução → trade-off.
- Invariantes do Aggregate testadas conceitualmente antes do código.
- Value Objects identificados com critério explícito (imutável? igualdade por valor? valida ao construir?).

**Fases 4-5 — Modelagem e Persistência:**
- Modelo de domínio em diagrama de classes antes do schema SQL.
- Schema SQL derivado do modelo, não o contrário.
- `resultMap` do MyBatis como mapeamento explícito — documentar a tradução.

**Fase 6 — API:**
- Validar que `domain/` não importa nada de `infrastructure/` (revisão manual ou ArchUnit).
- Demonstrar o fluxo completo: HTTP Request → Controller → Application Service → Aggregate → Repository → Mapper → SQL.
- Mostrar o Domain Event sendo publicado e capturado.

**Fase 8 — Comparação:**
- Mostrar o mesmo caso de uso "matricular aluno" em duas implementações lado a lado.
- Ser honesto sobre o custo de DDD: mais arquivos, mais mapeamento, mais código.
- Mostrar quando DDD não vale a pena (domínio simples, CRUD puro).

**Fase 9 — ADRs:**
- ADR explícito: por que MyBatis, não JPA — e quais trade-offs foram aceitos.
- ADR explícito: por que código em português — e como isso impacta manutenção com ferramentas em inglês.
- ADR explícito: por que apenas o contexto Matrícula foi implementado.

---

## Fontes de Pesquisa

- [Anemic Domain Model — Martin Fowler](https://martinfowler.com/bliki/AnemicDomainModel.html) — HIGH confidence
- [The Biggest Flaw of Spring Web Applications — Petri Kainulainen](https://www.petrikainulainen.net/software-development/design/the-biggest-flaw-of-spring-web-applications/) — HIGH confidence
- [Anemic vs. Rich Domain Objects — Baeldung](https://www.baeldung.com/java-anemic-vs-rich-domain-objects) — HIGH confidence
- [Our Experience Using SQL with DDD — Inato/Medium](https://medium.com/inato/our-experience-using-sql-with-ddd-96c2024d435c) — MEDIUM confidence
- [Repository Pattern in DDD — Daniel Abrahamberg](https://www.abrahamberg.com/blog/repository-pattern-in-ddd-bridging-the-domain-and-data-models/) — MEDIUM confidence
- [DDD Aggregates — Best Practices — Alina Bo](https://alinabo.com/ddd-aggregates) — MEDIUM confidence
- [Clean DDD Lessons: Project Structure — Medium/UNIL](https://medium.com/unil-ci-software-engineering/clean-ddd-lessons-project-structure-and-naming-conventions-00d0b9c57610) — MEDIUM confidence
- [DDD with Spring — Domain Events — DEV Community](https://dev.to/peholmst/handling-domain-events-with-spring-2bmm) — MEDIUM confidence
- [Code Files and Folders Under DDD — Medium](https://medium.com/@opflucker/code-files-and-folders-under-ddd-common-structure-mistakes-0feefffd86b7) — MEDIUM confidence
- [The Failed Promise of DDD — No Kill Switch](https://no-kill-switch.ghost.io/the-failed-promise-of-domain-driven-design-part-3/) — MEDIUM confidence (crítica útil sobre falhas pedagógicas)
- [Hexagonal Architecture, DDD, and Spring — Baeldung](https://www.baeldung.com/hexagonal-architecture-ddd-spring) — HIGH confidence
- [MyBatis 3 — Mapper XML — mybatis.org](https://mybatis.org/mybatis-3/sqlmap-xml.html) — HIGH confidence (documentação oficial)
- [DDD and @DomainEvents — Baeldung](https://www.baeldung.com/spring-data-ddd) — HIGH confidence
- [Consistency Boundary: Aggregate — James Hickey](https://www.jamesmichaelhickey.com/consistency-boundary/) — HIGH confidence
