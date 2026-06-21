# Anti-padrão: Acoplamento ao Banco

## O que é

Regras de negócio modeladas em função das tabelas do banco de dados. A estrutura do banco dita a estrutura dos objetos. Para entender uma regra de negócio, é necessário rastrear desde o objeto Java até a query SQL que implementa a verificação.

---

## Manifestação 1 — O modelo espelha o schema

`Matricula.java` no módulo camadas tem campos nomeados de acordo com as colunas SQL (`alunoId`, `turmaId`, `periodoInicio`). O campo `status` é uma `String` que armazena exatamente o valor que vai para a coluna:

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java

// ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
// Esta classe é um mapeamento próximo à tabela matriculas.
// Os nomes de campo correspondem às colunas SQL.
// O tipo String para status reflete o tipo VARCHAR da coluna —
// não um tipo de domínio que represente os estados possíveis.
public class Matricula {
    private UUID id;
    private UUID alunoId;        // coluna: aluno_id
    private UUID turmaId;        // coluna: turma_id
    private String periodoInicio; // coluna: periodo_inicio
    private String periodoFim;    // coluna: periodo_fim
    private String status;        // coluna: status VARCHAR — "ATIVA", "CANCELADA", "CONCLUIDA"
    // ...
}
```

Arquivo Java: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java`

---

## Manifestação 2 — A regra vive na query

A regra de negócio "máximo 6 disciplinas por matrícula" não existe em `Matricula.java`. Ela existe como `SELECT COUNT(*)` executado antes de cada inserção de disciplina:

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java

public void adicionarDisciplina(UUID matriculaId, String nomeDisciplina) {
    // ...

    // ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
    // A regra de negócio "máximo 6 disciplinas" não existe no modelo de domínio.
    // Ela existe como uma query SQL. Se você ler a classe Matricula, você não
    // encontra essa regra. Para descobrir o limite, você precisa ler o Service,
    // rastrear até countDisciplinas(), e ler o SQL no mapper.
    int quantidadeAtual = matriculaRepository.countDisciplinas(matriculaId);
    if (quantidadeAtual >= 6) {
        throw new RuntimeException("Limite de 6 disciplinas atingido");
    }

    // ...
}
```

A interface do repositório expõe o método que torna isso possível:

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java

@Mapper
public interface MatriculaRepository {
    // ... outros métodos

    // countDisciplinas existe porque a regra de limite precisa consultar o banco.
    // No módulo DDD, esse método não existe — Matricula.adicionarDisciplina()
    // verifica getItens().size() internamente, sem consulta extra.
    int countDisciplinas(@Param("matriculaId") UUID matriculaId);
}
```

Arquivo Java: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java`

---

## O problema de descoberta

Um desenvolvedor novo que precisa entender o limite de disciplinas de uma matrícula percorre:

1. Lê `Matricula.java` — nenhuma informação sobre limite
2. Lê `MatriculaServiceImpl.adicionarDisciplina()` — encontra o `if (quantidadeAtual >= 6)`
3. Rastreia `countDisciplinas()` até o mapper XML — encontra o `SELECT COUNT(*)`

Três arquivos para encontrar uma regra de negócio. Em um sistema maior, esse rastreamento pode envolver mais arquivos e mais camadas de indireção.

---

## Contraste DDD

No módulo DDD, `Matricula.adicionarDisciplina()` verifica o limite internamente. A regra está no objeto que ela protege:

```java
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java

// A regra vive no modelo — sem consulta extra ao banco.
// Um desenvolvedor lê Matricula.java e encontra a regra imediatamente.
public void adicionarDisciplina(NomeDisciplina nomeDisciplina) {
    if (this.itens.size() >= 6) {
        throw new LimiteDisciplinasAtingidoException(this.id);
    }
    // ...
}
```

O método `countDisciplinas` não existe no módulo DDD. A lista de itens da matrícula é carregada junto com o aggregate (via JOIN no mapper XML), e a verificação acontece em memória.

---

## Nota

O anti-padrão não é usar SQL. É usar SQL como repositório de regras de negócio. MyBatis com SQL explícito é totalmente compatível com DDD — o módulo DDD usa MyBatis para persistência, com JOINs e XMLs de mapeamento detalhados. A diferença é que, no módulo DDD, o SQL é responsável por buscar e persistir dados; as regras de negócio são responsabilidade do modelo de domínio carregado em memória.
