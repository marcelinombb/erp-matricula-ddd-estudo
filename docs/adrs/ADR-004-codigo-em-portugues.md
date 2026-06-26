# ADR-004: Código, SQL e Arquivos em Português

**Status:** Aceito
**Data:** 2026-06-20
**Contexto da fase:** Fase 1 — Design Estratégico

## Contexto

A convenção universal em projetos Java é nomear classes, métodos e variáveis em inglês. Este projeto faz o oposto deliberadamente. A motivação é pedagógica e fundamentada no princípio DDD de Linguagem Ubíqua: a Linguagem Ubíqua não é apenas documentação — ela vive no código.

```java
// INGLÊS TÉCNICO (padrão de mercado)
matriculaRepository.findById(id);
enrollment.addSubject(subject);
student.isActive();
```

```java
// PORTUGUÊS (decisão deste projeto)
matriculaRepositorio.buscarPorId(id);
matricula.adicionarDisciplina(disciplina);
aluno.estaAtivo();
```

No segundo exemplo, um especialista de negócio — alguém da Secretaria ou coordenação pedagógica — consegue ler o código e reconhecer os conceitos do seu domínio. `buscarPorId` é o mesmo verbo que ele usa ao falar sobre a operação. `adicionarDisciplina` é exatamente o que acontece no processo de negócio. `estaAtivo` reflete diretamente a pergunta que o processo faz sobre o aluno.

A tradução de inglês para português — mesmo que inconsciente — cria uma barreira cognitiva entre a fala do domínio e o código. Quando o especialista diz "verificar se o aluno pode se matricular" e o desenvolvedor escreve `student.canEnroll()`, algo se perde na tradução: não o significado técnico, mas a conexão viva entre o vocabulário do negócio e o vocabulário do código. DDD chama isso de Linguagem Ubíqua — a mesma linguagem usada em conversas de negócio, documentos e código-fonte.

## Alternativas Consideradas

### Opção A: Inglês Para Tudo (Padrão de Mercado)

**Prós:**
- Padrão universal — qualquer desenvolvedor Java do mundo consegue ler sem barreiras linguísticas
- Ferramentas de análise estática, linters e plugins de IDE são otimizados para nomenclatura em inglês
- Stack Overflow, documentações e exemplos de DDD na internet usam inglês — cópia/adaptação de código é mais fácil
- AI assistants (GitHub Copilot, etc.) treinados primariamente em código em inglês sugerem nomes mais precisos

**Contras:**
- Cria duas linguagens: o negócio fala em português e o código fala em inglês
- O desenvolvedor precisa "traduzir mentalmente" ao ler código — `Enrollment` não é imediatamente reconhecível como o conceito de Matrícula para um especialista do domínio
- Contradiz o princípio pedagógico central do projeto: que o código deve expressar o domínio, não escondê-lo em abstrações técnicas

### Opção B: Português Para Documentação, Inglês Para Código

**Prós:**
- Convenção mais comum em empresas brasileiras — docs em português, código em inglês
- Elimina a barreira linguística para leitura de código por desenvolvedores internacionais

**Contras:**
- Elimina a barreira cognitiva só parcialmente — os documentos usam "Matrícula" mas o código usa "Enrollment"
- O desenvolvedor ainda precisa manter dois vocabulários em mente ao navegar entre docs e código
- Enfraquece o ponto pedagógico central: a Linguagem Ubíqua deve ser ubíqua — presente em todos os artefatos, inclusive o código

### Opção C: Português Apenas Para Nomes de Domínio, Inglês Para Infraestrutura

**Prós:**
- Equilíbrio entre idioma do domínio e idioma técnico
- Classes de domínio em português, controllers e mappers em inglês

**Contras:**
- Complexidade de regra — difícil de aplicar consistentemente quando a fronteira entre "domínio" e "infraestrutura" é justamente o que o projeto está ensinando
- Cria inconsistência perceptível: `MatriculaService` vs `MatriculaController` em inglês seria confuso

## Decisão

Todos os identificadores de código Java (`class`, `interface`, `record`, nomes de método, nomes de variável), nomes de tabela e coluna SQL, e nomes de arquivo de configuração usam **português**.

```java
// Classes de domínio
public class Matricula { ... }
public class Aluno { ... }
public sealed interface StatusMatricula permits StatusMatricula.Ativa, StatusMatricula.Cancelada, StatusMatricula.Concluida { ... }

// Métodos de domínio
public Matricula matricular(UUID alunoId, UUID turmaId, PeriodoLetivo periodo) { ... }
public void adicionarDisciplina(NomeDisciplina disciplina) { ... }
public void cancelar() { ... }

// Repositório (interface de domínio)
public interface MatriculaRepositorio {
    Optional<Matricula> buscarPorId(UUID id);
    List<Matricula> buscarPorAluno(UUID alunoId);
    void salvar(Matricula matricula);
}
```

**Exceções permitidas:**
- Palavras-chave da linguagem Java (`public`, `class`, `void`, `interface`, `record`)
- Anotações de framework (`@Service`, `@Component`, `@RestController`, `@TransactionalEventListener`)
- Identificadores de bibliotecas externas (`ResultSet`, `HttpStatus`, `ObjectMapper`)
- Nomes de packages técnicos (`repository`, `mapper`, `config`) quando não houver equivalente em português sem perda de clareza

## Consequências

### Positivas

- Código como Linguagem Ubíqua — sem tradução entre a fala do negócio e o código: o que o especialista chama de "matrícula ativa" aparece no código como `StatusMatricula.ATIVA`
- Especialistas de domínio conseguem revisar regras de negócio no código durante sessões de pair programming ou revisão — a barreira de idioma não existe
- Reforça o princípio DDD a cada linha escrita: o desenvolvedor não pode fugir da Linguagem Ubíqua porque ela está literalmente em todo lugar
- O projeto cumpre seu papel pedagógico: demonstra que Linguagem Ubíqua não é um conceito abstrato, é uma prática diária de nomenclatura

### Negativas (Trade-offs)

- Ferramentas de análise estática, AI assistants e Stack Overflow funcionam melhor com inglês — o desenvolvedor paga esse custo em produtividade de ferramentas. Um método chamado `buscarPorId` recebe sugestões de autocompletar menos precisas que `findById`
- Código em português é incomum no mercado e pode causar estranhamento em code reviews com pessoas externas ao projeto ou em contribuições open source
- Algumas IDEs e plugins assumem nomenclatura em inglês para sugestões de nome — o desenvolvedor verá sugestões como `getBuscarPorId()` onde esperaria `buscarPorId()`
- Não é um padrão adotável em todos os projetos: para um time com desenvolvedores internacionais, código em português seria uma barreira. Esta é uma decisão adequada para o contexto específico de um time brasileiro treinando com conceitos de um domínio brasileiro

## Referências

- Domain-Driven Design — Eric Evans (Capítulo 2: Comunicação e uso da linguagem)
- contexto-matricula.md §2 (Ubiquitous Language)
- Ubiquitous Language — Martin Fowler: https://martinfowler.com/bliki/UbiquitousLanguage.html

## Na prática

O código implementado nas Fases 3 e 4 demonstra a decisão em todos os níveis:

**Nomes de classes e interfaces:**
- `MatricularAlunoUseCase` (não `EnrollStudentUseCase`)
- `VerificadorElegibilidadeMatricula` (não `EligibilityChecker`)
- `MatriculaRepositorio` (não `MatriculaRepository`)
- `ExcecaoHandler` (não `ExceptionHandler`)

**Nomes de métodos de domínio:**
- `adicionarDisciplina()` (não `addDiscipline()`)
- `cancelar()` (não `cancel()`)
- `coletarEventos()` (não `collectEvents()`)
- `estaAtivo()` (não `isActive()`)
- `buscarPorId()` (não `findById()`)
- `existeMatriculaAtiva()` (não `hasActiveEnrollment()`)

Em inglês, os equivalentes seriam: `EnrollStudentUseCase`, `EligibilityChecker`, `addDiscipline()`, `cancel()`. O impacto é mais visível quando um especialista de matrícula escolar lê `verificador.verificar(aluno, turma, periodo)` — ele reconhece imediatamente a operação de negócio que está sendo executada.

A Linguagem Ubíqua foi definida em [docs/01-design-estrategico/linguagem-ubiqua.md](../../docs/01-design-estrategico/linguagem-ubiqua.md) — o código é a implementação direta dessa linguagem. Os termos do glossário ("Matrícula", "Aluno", "Turma", "PeriodoLetivo") aparecem sem tradução tanto nos documentos quanto nas classes Java.
