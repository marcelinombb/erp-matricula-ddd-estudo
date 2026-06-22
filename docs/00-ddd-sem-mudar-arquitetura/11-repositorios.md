# Repositórios — Aplicados na Arquitetura Tradicional

## O que é

Repositório é a abstração que recupera e persiste Aggregates. O ponto central do DDD: a interface do Repositório vive no domínio — não na infraestrutura. Isso inverte a dependência: a infraestrutura implementa a interface do domínio, não o contrário.

A diferença de um DAO genérico: o Repositório fala a linguagem do domínio (`buscarPorAluno`, `existeMatriculaAtiva`), não a linguagem do banco (`findBy`, `countWhere`). Quem lê a interface entende as perguntas de negócio sem precisar abrir o SQL.

A consequência prática: os UseCases não importam nada de `org.apache.ibatis`, `org.springframework.data` ou qualquer tecnologia de persistência. A interface que eles usam está no mesmo pacote que o domínio — sem dependência de infraestrutura. Para teoria, ver `docs/02-design-tatico/repositorios.md`.

---

## Manifestação no módulo camadas (ANTES)

Em `erp-matricula-camadas`, `MatriculaRepository` é um `@Mapper` MyBatis — uma anotação de infraestrutura diretamente na interface de repositório:

```java
// ANTES: DAO com anotação de infraestrutura e termos técnicos de banco
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java
@Mapper  // anotação de infraestrutura na interface — domínio depende de MyBatis
public interface MatriculaRepository {

    Optional<Matricula> findById(@Param("id") UUID id);        // "find" — termo de framework
    List<Matricula> findByAlunoId(@Param("alunoId") UUID alunoId);
    void insert(Matricula matricula);
    void updateStatus(@Param("id") UUID id, @Param("status") String status);
    int countDisciplinas(@Param("matriculaId") UUID matriculaId); // expõe contagem SQL
}
```

Sem `@Mapper`, a interface não funciona — o domínio está acoplado ao MyBatis. Se a equipe trocar de framework de persistência, precisa alterar a interface de repositório: uma interface que, pelo nome, deveria ser responsabilidade do domínio.

Arquivo completo: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/repository/MatriculaRepository.java`

---

## Aplicação no módulo DDD (DEPOIS)

Em `erp-matricula-ddd`, `MatriculaRepositorio` é uma interface pura no pacote de domínio — sem nenhum import de framework:

```java
// DEPOIS: interface no domínio — sem imports de infraestrutura, sem anotações de framework
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java
// Imports presentes: apenas tipos do próprio domínio (Matricula, AlunoId, MatriculaId, PeriodoLetivo)
// Zero imports: sem @Mapper, sem org.apache.ibatis, sem org.springframework.data
public interface MatriculaRepositorio {

    Optional<Matricula> buscarPorId(MatriculaId id);               // recebe tipo de domínio, não UUID cru
    List<Matricula> buscarPorAluno(AlunoId alunoId);               // parâmetro com semântica
    boolean existeMatriculaAtiva(AlunoId alunoId, PeriodoLetivo periodo); // frase de negócio
    void salvar(Matricula matricula);                              // persiste o Aggregate inteiro
}
```

A implementação `MatriculaRepositorioMyBatis` está em `infraestrutura/persistencia/` — a seta de dependência vai de `infraestrutura/` → `dominio/`, nunca o contrário. O domínio define o contrato; a infraestrutura implementa.

Arquivo completo: `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java`

---

## O que foi ganho

UseCases testáveis sem banco real — basta implementar `MatriculaRepositorio` com um `Map` em memória. Interface expressa perguntas de negócio (`existeMatriculaAtiva?`), não operações de banco (`count`). O domínio não sabe nada sobre MyBatis, PostgreSQL, ou qualquer tecnologia de persistência — a troca de framework afeta apenas `infraestrutura/`.
