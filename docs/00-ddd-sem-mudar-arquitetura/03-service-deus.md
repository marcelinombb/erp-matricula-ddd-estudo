# Anti-padrão: Service Deus

## O que é

Uma classe que cresce sem parar. Toda operação relacionada ao domínio de matrícula termina nela. Com o tempo, o Service acumula responsabilidades que deveriam pertencer a objetos separados, e a classe vira o ponto central de toda modificação de comportamento.

O nome vem do padrão "God Object" — um objeto que sabe de tudo e faz de tudo.

---

## Manifestação no módulo camadas

`MatriculaServiceImpl` acumula todas as operações de matrícula em uma única classe:

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java

// ANTI-PADRAO: Service Deus (DIAG-03)
// Esta classe tem 200+ linhas e cresce sem parar.
// Toda nova regra de matrícula vem parar aqui.
// Métodos presentes:
//   matricular()            — 60 linhas: validação de aluno, período, duplicidade, persistência
//   adicionarDisciplina()   — 50 linhas: validação, limite de disciplinas, persistência
//   cancelar()              — 30 linhas: validação de estado, atualização, persistência
//   buscarPorAluno()        — 15 linhas: consulta com filtro
//   verificarElegibilidade()— 15 linhas: regra duplicada de DisciplinaServiceImpl
//   limparMatriculasAntigas()— 20 linhas: operação batch de limpeza
// Total: 200+ linhas em uma única classe de serviço
@Service
@Transactional
public class MatriculaServiceImpl implements MatriculaService {

    public UUID matricular(UUID alunoId, UUID turmaId, String periodoInicio, String periodoFim) {
        // 60 linhas: busca aluno, valida ativo, verifica período, verifica duplicidade,
        // cria objeto Matricula, persiste, retorna ID
    }

    public void adicionarDisciplina(UUID matriculaId, String nomeDisciplina) {
        // 50 linhas: busca matrícula, busca aluno, valida ativo (duplicado de matricular()),
        // conta disciplinas via COUNT(*), adiciona item, persiste
    }

    public void cancelar(UUID matriculaId) {
        // 30 linhas: busca matrícula, valida que está ativa, muda status, persiste
    }

    public List<Matricula> buscarPorAluno(UUID alunoId) {
        // 15 linhas: delega ao repository com alguma transformação
    }

    // ... mais métodos conforme o sistema cresce
}
```

Arquivo Java completo: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java`

---

## Por que acontece

A relação causal com os anti-padrões anteriores é direta:

1. A entidade `Matricula` não tem comportamento (DIAG-02 — Entidade Anêmica)
2. A lógica que deveria estar em `Matricula` migra para o Service (DIAG-01 — Service Anêmico)
3. O Service cresce a cada nova funcionalidade relacionada a matrículas
4. Nova feature de matrícula? Novo método no `MatriculaServiceImpl`
5. Resultado: Service Deus

Não é uma decisão explícita de ninguém. É o caminho natural quando o modelo não tem comportamento.

---

## Contraste DDD

No módulo DDD, cada operação tem sua própria classe de UseCase:

```java
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/
//
//   MatricularAlunoUseCase.java      — ~109 linhas — apenas orquestra a matrícula
//   AdicionarDisciplinaUseCase.java  — ~60  linhas — apenas orquestra adição de disciplina
//   CancelarMatriculaUseCase.java    — ~50  linhas — apenas orquestra cancelamento
//
// Cada UseCase tem uma única responsabilidade: orquestrar uma operação específica.
// Nenhum deles toma decisões de negócio — delegam para o domínio.
```

`MatricularAlunoUseCase` tem ~109 linhas e só orquestra. Quando uma nova operação é necessária (por exemplo, transferência de turma), um novo UseCase é criado — não se modifica um Service existente com 200 linhas.

---

## Consequência prática

Com o Service Deus, dois desenvolvedores trabalhando em features simultâneas de matrícula provavelmente modificam o mesmo arquivo `MatriculaServiceImpl.java`. Conflitos de merge são frequentes, e cada conflito exige entender o contexto das duas mudanças.

Com UseCases separados, cada feature tem seu próprio arquivo. Conflitos de merge em operações diferentes de matrícula são raros.
