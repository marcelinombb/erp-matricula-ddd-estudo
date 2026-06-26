# Domain Services — Matrícula Escolar

Para matricular um aluno, precisamos verificar três coisas: ele está ativo? O período letivo está aberto? Já existe uma matrícula dele neste período?

Parece simples. Mas onde colocar essa verificação? Olhe as entidades disponíveis: `Aluno` sabe se está ativo, mas não tem acesso ao repositório de matrículas para verificar duplicatas. `PeriodoLetivo` sabe suas datas de início e fim, mas não consulta banco de dados. `Matricula` ainda não existe no momento da verificação — é exatamente o que queremos criar. Nenhuma entidade sozinha tem acesso a todas as três informações necessárias.

A verificação de elegibilidade para matrícula é uma **regra de negócio com nome reconhecível pela Secretaria**: "verificar se o aluno pode ser matriculado" é um verbo do domínio, não um detalhe técnico. Quando a lógica não pertence a nenhuma Entidade específica, mas ainda é uma regra de negócio pura do domínio, o padrão DDD tem um nome: **Domain Service** (Serviço de Domínio).

---

## Por que não pertence a nenhuma Entidade?

Cada candidato natural para hospedar essa lógica apresenta um problema:

- **Método de `Aluno`?** `Aluno` precisaria depender do `MatriculaRepositorio` para verificar duplicatas — o que o tornaria um Service disfarçado de Entidade. Uma Entidade que injeta repositório viola o princípio de que o domínio não conhece infraestrutura.

- **Método de `Matricula`?** `Matricula` ainda não existe quando verificamos elegibilidade. A verificação precede a criação — seria um método estático em uma classe que ainda está sendo criada, o que não faz sentido no modelo.

- **Lógica no UseCase?** A regra "aluno ativo + período aberto + sem duplicata" é uma regra de negócio com semântica clara para a Secretaria. Colocá-la no UseCase mistura orquestração com domínio: o UseCase deixaria de apenas coordenar e passaria a tomar decisões de negócio. Se outro UseCase também precisasse verificar elegibilidade (ex: `ReativarMatriculaUseCase`), a lógica seria duplicada.

A solução correta é criar uma classe no pacote `dominio/` que encapsula exatamente essa responsabilidade: `VerificadorElegibilidadeMatricula`.

---

## O Domain Service

```java
// Java 21: sem anotações de framework — classe Java pura no pacote dominio/
// DDD fit: lógica de negócio que cruza múltiplas Entidades, com nome reconhecível pelo domínio
public class VerificadorElegibilidadeMatricula {

    private final MatriculaRepositorio repositorio;

    public VerificadorElegibilidadeMatricula(MatriculaRepositorio repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public void verificar(Aluno aluno, PeriodoLetivo periodo) {
        if (!aluno.estaAtivo()) {
            throw new AlunoInativoException(aluno.getId());
        }
        if (!periodo.estaAberto()) {
            throw new PeriodoFechadoException(periodo);
        }
        boolean matriculaExistente = repositorio.existeMatriculaAtiva(aluno.getId(), periodo);
        if (matriculaExistente) {
            throw new MatriculaDuplicadaException(aluno.getId(), periodo);
        }
    }
}
```

Note o que está ausente: sem `@Service`, sem `@Autowired`, sem import de framework. `VerificadorElegibilidadeMatricula` é uma classe Java pura no pacote `dominio/`. A dependência `MatriculaRepositorio` é uma interface de domínio — não uma classe do Spring nem do MyBatis.

> **Por que injetar `MatriculaRepositorio` via construtor e não `@Autowired`?** Um Domain Service no pacote `dominio/` não deve ter anotações do Spring. O Spring consegue instanciar e injetar esse serviço via construtor sem nenhuma anotação no domínio — a configuração do Spring vive em `infraestrutura/` ou `aplicacao/`, não no domínio.

---

## Domain Service vs Application Service

A confusão mais comum é entre Domain Service e Application Service (UseCase). Ambos "fazem algo" com o domínio, mas com responsabilidades muito diferentes:

| Aspecto | Domain Service | Application Service (UseCase) |
|---------|---------------|-------------------------------|
| Onde vive | `dominio/` — sem import de framework | `aplicacao/` — pode usar Spring |
| O que faz | Regra de negócio pura que cruza entidades | Orquestra: busca dados, chama domínio, salva, publica eventos |
| Dependências | Só interfaces de domínio | Usa repositório, event publisher, outros serviços |
| Transação | Não gerencia transação | Pode demarcar `@Transactional` |
| Nomeação | Verbo do domínio: `VerificadorElegibilidade` | Caso de uso: `MatricularAlunoUseCase` |
| Exemplo | `VerificadorElegibilidade.verificar()` | `MatricularAlunoUseCase.executar()` |

O teste decisivo: "a Secretaria reconheceria esse nome como um processo de negócio?" `VerificadorElegibilidadeMatricula` — sim, a Secretaria verifica elegibilidade antes de matricular. `MatricularAlunoUseCase` — sim, é o caso de uso de matricular. `MatriculaRepository` — não, a Secretaria não sabe o que é um repositório. Tudo que a Secretaria reconhece como processo de negócio pertence ao domínio ou à aplicação; tudo que é detalhe técnico pertence à infraestrutura.

---

## Como o UseCase Consome o Domain Service

O UseCase orquestra — busca dados, chama o Domain Service para a decisão de elegibilidade, cria o Aggregate, salva e publica eventos:

```java
// Application Service — vive em aplicacao/, pode usar Spring
public class MatricularAlunoUseCase {

    private final VerificadorElegibilidadeMatricula verificador;
    private final AlunoRepositorio alunoRepositorio;
    private final MatriculaRepositorio matriculaRepositorio;
    private final EventPublisher publicador;

    public MatriculaCriadaDto executar(UUID alunoId, UUID turmaId, PeriodoLetivo periodo) {
        Aluno aluno = alunoRepositorio.buscarPorId(alunoId)
            .orElseThrow(() -> new AlunoNaoEncontradoException(alunoId));

        // Domain Service cuida da regra de elegibilidade
        verificador.verificar(aluno, periodo);

        // Aggregate Root encapsula as invariantes da matrícula
        Matricula matricula = Matricula.criar(alunoId, turmaId, periodo);

        matriculaRepositorio.salvar(matricula);
        publicador.publicar(matricula.coletarEventos());

        return new MatriculaCriadaDto(matricula.getId());
    }
}
// o UseCase orquestra; o Domain Service decide a regra de negócio;
// o Aggregate garante as invariantes — três responsabilidades distintas
```

A orquestração completa, com todos os participantes e caminhos de erro, é visualizada no [sequence diagram](./modelagem.md).

---

## Erros Comuns

### Erro 1: Lógica de negócio no Application Service

O desenvolvedor vindo de arquitetura em camadas coloca a verificação diretamente no Service — é o padrão familiar. O problema: lógica de negócio espalhada nos UseCases é difícil de reutilizar e difícil de nomear. Quando um segundo UseCase precisar verificar elegibilidade, a lógica será duplicada.

```java
// ERRADO — regra de elegibilidade diretamente no Application Service
@Service
public class MatricularAlunoUseCase {

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    public void executar(UUID alunoId, String periodo) {
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow();

        // Regra de negócio aqui, no UseCase — onde fica se outro UseCase precisar?
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo");
        }
        if (matriculaRepository.existsByAlunoIdAndPeriodo(alunoId, periodo)) {
            throw new RuntimeException("Matrícula duplicada");
        }

        // ... cria matrícula
    }
}
```

```java
// CERTO — Application Service delega a regra ao Domain Service
public class MatricularAlunoUseCase {

    private final VerificadorElegibilidadeMatricula verificador;
    private final AlunoRepositorio alunoRepositorio;
    private final MatriculaRepositorio matriculaRepositorio;

    public void executar(UUID alunoId, UUID turmaId, PeriodoLetivo periodo) {
        Aluno aluno = alunoRepositorio.buscarPorId(alunoId).orElseThrow();

        // Domain Service cuida da decisão — reutilizável, nomeado pelo domínio
        verificador.verificar(aluno, periodo);

        Matricula matricula = Matricula.criar(alunoId, turmaId, periodo);
        matriculaRepositorio.salvar(matricula);
        publicar(matricula.coletarEventos());
    }
}
```

### Erro 2: Domain Service com `@Service` do Spring e `@Autowired`

Colocar `@Service` e `@Autowired` no Domain Service viola a separação entre domínio e framework. O domínio `dominio/` não deve importar nada do Spring.

```java
// ERRADO — Domain Service com anotações de framework no pacote dominio/
@Service
public class VerificadorElegibilidade {

    @Autowired
    private MatriculaRepository matriculaRepo; // interface do Spring Data, não do domínio

    public void verificar(Aluno aluno, String periodo) {
        if (!aluno.isAtivo()) { // isAtivo() em inglês — quebra a Linguagem Ubíqua
            throw new RuntimeException("Inativo");
        }
        // ...
    }
}
```

```java
// CERTO — Domain Service sem anotações, no pacote dominio/, com interface de domínio
// Java 21: sem anotações de framework — classe Java pura no pacote dominio/
// DDD fit: lógica de negócio que cruza múltiplas Entidades, com nome reconhecível pelo domínio
public class VerificadorElegibilidadeMatricula {

    // MatriculaRepositorio é interface de domínio — zero import de Spring Data ou MyBatis
    private final MatriculaRepositorio repositorio;

    public VerificadorElegibilidadeMatricula(MatriculaRepositorio repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    public void verificar(Aluno aluno, PeriodoLetivo periodo) {
        if (!aluno.estaAtivo()) { // estaAtivo() em português — Linguagem Ubíqua
            throw new AlunoInativoException(aluno.getId());
        }
        if (!periodo.estaAberto()) {
            throw new PeriodoFechadoException(periodo);
        }
        if (repositorio.existeMatriculaAtiva(aluno.getId(), periodo)) {
            throw new MatriculaDuplicadaException(aluno.getId(), periodo);
        }
    }
}
```

A diferença central: no CERTO, `VerificadorElegibilidadeMatricula` vive em `dominio/` sem nenhum import do Spring. A injeção acontece via construtor — o Spring injeta a implementação sem que o domínio saiba que o Spring existe. No ERRADO, a classe importa `@Service` e `@Autowired` do Spring, e usa `MatriculaRepository` (Spring Data) em vez de `MatriculaRepositorio` (interface de domínio). Ver [repositorios.md](./repositorios.md) para a distinção entre as duas interfaces.
