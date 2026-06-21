# Anti-padrão: Duplicação de Regras

## O que é

A mesma regra de negócio implementada em mais de um lugar. Cada implementação começa idêntica, mas com o tempo — à medida que o entendimento do negócio evolui — as implementações divergem. O sistema passa a ter comportamento incoerente: a mesma regra produz resultados diferentes dependendo de qual caminho de código foi executado.

---

## Manifestação no módulo camadas

A validação "aluno inativo não pode realizar operações de matrícula" aparece tanto em `MatriculaServiceImpl` quanto em `DisciplinaServiceImpl`:

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java

public UUID matricular(UUID alunoId, UUID turmaId, String periodoInicio, String periodoFim) {
    Aluno aluno = alunoRepository.findById(alunoId)
        .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + alunoId));

    // Validação de aluno inativo — primeira ocorrência
    if (!aluno.isAtivo()) {
        throw new RuntimeException("Aluno inativo não pode ser matriculado");
    }
    // ...
}
```

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java

// ANTI-PADRAO: Duplicação de Regras (DIAG-04)
// Esta validação do aluno já existe em MatriculaServiceImpl.matricular().
// Daqui a 6 meses, alguém adiciona uma exceção em um lugar e esquece do outro.
// A regra "aluno inativo não pode adicionar disciplinas" está espalhada.
public void adicionarDisciplina(UUID matriculaId, String nomeDisciplina) {
    Matricula matricula = matriculaRepository.findById(matriculaId)
        .orElseThrow(() -> new RuntimeException("Matrícula não encontrada: " + matriculaId));

    // Validação de aluno inativo — segunda ocorrência, copiada de MatriculaServiceImpl
    Aluno aluno = alunoRepository.findById(matricula.getAlunoId())
        .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
    if (!aluno.isAtivo()) {
        throw new RuntimeException("Aluno inativo não pode adicionar disciplinas");
    }
    // ...
}
```

Arquivo Java 1: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java`
Arquivo Java 2: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/DisciplinaServiceImpl.java`

---

## Como a divergência acontece

Um cenário concreto: seis meses após o código ser escrito, o negócio decide que alunos em período de adaptação podem adicionar disciplinas mesmo que seu status ainda seja "inativo" no sistema (aguardando confirmação de dados).

O desenvolvedor que implementa a exceção modifica `MatriculaServiceImpl` porque é onde está trabalhando. `DisciplinaServiceImpl` não é alterado — talvez nem seja lembrado.

Resultado: um aluno em período de adaptação pode adicionar disciplinas a uma matrícula existente, mas não pode criar uma nova matrícula. O comportamento é incoerente, e a inconsistência só é descoberta quando um usuário reporta o problema.

---

## Por que acontece

Sem um modelo de domínio com comportamento, regras vivem nos Services. Quando um segundo Service precisa da mesma regra, a solução mais direta é copiar o bloco de código. Não há um único lugar responsável pela regra — cada Service é responsável por validar as suas próprias operações.

O problema não é o copy-paste em si. É que não existe um mecanismo que force as duas cópias a permanecerem sincronizadas.

---

## Contraste DDD

No módulo DDD, a regra de elegibilidade vive em `VerificadorElegibilidadeMatricula` — um Domain Service. Todos os UseCases que precisam verificar se um aluno pode realizar uma operação chamam o mesmo objeto:

```java
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java

// A regra existe em um único lugar. Qualquer mudança aqui afeta todos os UseCases.
// Não há segunda implementação para lembrar de atualizar.
public void verificar(Aluno aluno, Turma turma, PeriodoLetivo periodo) {
    if (!aluno.estaAtivo()) {
        throw new AlunoInativoException(aluno.getId());
    }
    // ... demais verificações
}
```

`MatricularAlunoUseCase` chama `verificador.verificar(...)`. `AdicionarDisciplinaUseCase` chama o mesmo `verificador.verificar(...)`. Quando a regra muda, muda em um lugar, e os dois UseCases herdam a mudança automaticamente.
