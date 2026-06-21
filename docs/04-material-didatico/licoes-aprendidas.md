# Lições Aprendidas — DDD na Prática

Este documento não é marketing de DDD. É uma análise honesta do que funcionou, do que custou mais do que esperávamos, e do que faríamos diferente.

---

## 1. O que DDD resolveu bem

### Invariantes do Aggregate sempre protegidas

`Matricula.adicionarDisciplina()` é o único lugar no projeto onde as três regras são verificadas: matrícula não cancelada, limite de disciplinas, sem duplicidade. Essas verificações acontecem atomicamente — sem janela entre "verificar" e "modificar".

Em uma arquitetura tradicional, essa lógica provavelmente estaria em `MatriculaService.adicionarDisciplina()`. Com o tempo, um segundo ponto de entrada (`MatriculaAdminService`, por exemplo) poderia adicionar disciplinas sem passar pelas mesmas verificações. Com o Aggregate, isso é estruturalmente impossível: a única forma de adicionar uma disciplina é via `adicionarDisciplina()`.

Ver: [`Matricula.java` método `adicionarDisciplina()`](../erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java)

### Exceções com semântica — dados chegam ao HTTP sem parsing

`LimiteDisciplinasExcedidoException` carrega `getLimite()` e `getAtual()` como campos tipados. O `ExcecaoHandler` usa esses dados diretamente para construir um response 422 com payload estruturado:

```json
{
  "codigo": "LIMITE_DISCIPLINAS_EXCEDIDO",
  "limite": 6,
  "atual": 6
}
```

Com `RuntimeException("Limite excedido")`, o handler teria que parsear a string de mensagem para extrair esses valores — frágil e acoplado ao formato do texto. Com a exceção tipada, o contrato entre domínio e interface é explícito.

Ver: [`LimiteDisciplinasExcedidoException.java`](../erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/excecao/LimiteDisciplinasExcedidoException.java) e [`ExcecaoHandler.java`](../erp-matricula-ddd/src/main/java/br/com/escola/matricula/interfaces/ExcecaoHandler.java)

### Separação domínio/persistência — mudança cirúrgica no banco

`MatriculaRowMapper.java` é o único arquivo que conhece tanto `MatriculaRow` (modelo relacional) quanto `Matricula` (modelo de domínio). O Javadoc do arquivo nomeia explicitamente: "ÚNICO arquivo que conhece tanto MatriculaRow quanto Matricula."

Adicionar um campo ao banco é cirúrgico: só muda `MatriculaRow` (o campo), `MatriculaRowMapper` (a conversão), e o XML MyBatis (o alias no SELECT). `Matricula.java` não é tocado.

Ver: [`MatriculaRowMapper.java`](../erp-matricula-ddd/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRowMapper.java)

### Domain Events para integração sem acoplamento

`FinanceiroEventListener` e `AcademicoEventListener` recebem `AlunoMatriculado` via `@TransactionalEventListener` sem que `Matricula.java` conheça a existência de nenhum deles. A classe `Matricula` coleta o evento em uma `List<Object>` simples — sem import de Spring, sem interface de evento específica.

A ordem de execução é garantida: `@TransactionalEventListener` usa `AFTER_COMMIT` por padrão. Os listeners só são chamados após o commit da transação do UseCase — o BC Financeiro nunca processa um evento de uma operação que ainda pode sofrer rollback.

Ver: [`FinanceiroEventListener.java`](../erp-matricula-ddd/src/main/java/br/com/escola/matricula/infraestrutura/eventos/FinanceiroEventListener.java) e [`AcademicoEventListener.java`](../erp-matricula-ddd/src/main/java/br/com/escola/matricula/infraestrutura/eventos/AcademicoEventListener.java)

---

## 2. O que custou mais do que o esperado

### Construção de objetos de domínio no Controller

O `MatricularAlunoCommand` recebe objetos `Aluno` e `Turma` completos — não apenas IDs. Isso significa que o `MatriculaController` precisa construir um `Aluno` placeholder com um CPF e um nome fictícios apenas para criar o Command.

```java
// MatriculaController.java — custo real da decisão
var aluno = new Aluno(
    new AlunoId(UUID.fromString(request.alunoId())),
    new Cpf("52998224725"), // CPF placeholder
    "N/A",
    true
);
```

Em um projeto de produção, seria necessário um passo de busca no repositório de Alunos — ou um Command mais simples com IDs primitivos. A decisão de usar objetos de domínio no Command foi pedagógica; em produção seria repensada.

### MyBatis é mais verboso que JPA — o custo cresce com o número de campos

Cada campo do banco precisa ser mapeado explicitamente: um alias no SQL (`i.adicionada_em AS item_adicionada_em`), um `<result>` no ResultMap, um campo na classe Row, um getter no RowMapper. Com 5 campos, é gerenciável. Com 20 campos e 3 tabelas, o volume de XML e código de mapeamento cresce linearmente.

O ganho pedagógico é alto — o desenvolvedor vê exatamente como o banco se conecta ao domínio. O custo de manutenção também é real: sem geração de código (MapStruct, Lombok), cada mudança de schema exige mudanças em múltiplos arquivos.

### Sem Lombok significa mais boilerplate para Entidades

Java 21 records eliminaram o problema para Value Objects (`AlunoId`, `Cpf`, `PeriodoLetivo`) e Commands (`MatricularAlunoCommand`). Mas Entidades como `Aluno` ainda requerem construtores, getters e `equals`/`hashCode` escritos manualmente — porque precisam de identidade por ID, não por igualdade estrutural.

A decisão de excluir Lombok (documentada no CLAUDE.md) foi pedagógica: o boilerplate que Lombok esconde é o mesmo boilerplate que demonstra a diferença entre Entidade e Value Object. Mas o custo é real para quem está escrevendo o código.

### Aggregate boundary é difícil de acertar de primeira

A decisão de não incluir vagas disponíveis no Aggregate Matrícula (registrada nas decisões abertas do projeto) foi tomada deliberadamente para manter o foco. A pergunta "quantas vagas restam na turma?" cruza o Aggregate Matrícula e o Aggregate Turma — uma regra cross-Aggregate que pertence ao Domain Service, não ao Aggregate.

Em produção, essa decisão requereria análise mais profunda: o `VerificadorElegibilidadeMatricula` precisaria de um repositório de Turma para verificar vagas, adicionando complexidade de carregamento. A fronteira do Aggregate é uma das decisões mais difíceis do DDD — e acertar na primeira tentativa é raro.

---

## 3. O que faríamos diferente em produção

### Optimistic locking (coluna `version`) para concorrência

`MatriculaRepositorioMyBatis` não implementa controle de concorrência otimista. Dois requests simultâneos para adicionar disciplinas à mesma matrícula podem ler o mesmo estado e ambos persistirem sem detectar o conflito.

O padrão seria:
```sql
UPDATE matriculas SET ... version = version + 1
WHERE id = ? AND version = ?
-- Se rowsAffected = 0: alguém modificou entre a leitura e a escrita
```

Documentado como `PROD-01` nos requisitos de produção. Não implementado no v1 por ser uma complexidade de infraestrutura que não acrescenta conceitos novos ao DDD.

### Commands com IDs primitivos em vez de objetos de domínio completos

`MatricularAlunoCommand` recebe `Aluno` e `Turma` completos. Em produção, um Command mais simples com `AlunoId` e `TurmaId` simplificaria o Controller: não seria necessário construir objetos placeholder com dados fictícios. O `UseCase` carregaria os objetos completos pelos repositórios antes de delegar ao Domain Service.

### Testes unitários do Aggregate antes de implementar (TDD)

A ausência de testes unitários significa que as invariantes de `Matricula` são verificadas apenas pelo `DemoRunner` na inicialização — um teste de integração informal. Com TDD, os testes de `adicionarDisciplina()` existiriam antes da implementação, garantindo que todos os casos limites (cancelada, limite exato, duplicidade) fossem cobertos sem depender de dados de banco.

### Separar leitura de escrita explicitamente (CQRS simples)

Operações de leitura atualmente reconstituem o Aggregate completo (`MatriculaRowMapper.toDomain()`) mesmo quando o cliente precisa apenas de um DTO para exibição. Um padrão CQRS simples permitiria queries de leitura direta via `MatriculaMapper` retornando `MatriculaDto` sem passar pelo `MatriculaRepositorio` e sem reconstituir o Aggregate.

---

## Conclusão

DDD agrega valor em domínios ricos em comportamento e complexidade de negócio. Para CRUD simples, a estrutura adicional não se paga. Matrícula escolar é um bom caso de uso: regras de negócio claras (invariantes do Aggregate), estados bem definidos (sealed interface), integração entre contextos (Domain Events com listeners stub).

O trade-off é real: mais arquivos, mais mapeamento manual, mais decisões explícitas de design. O ganho é um código que comunica intenção — onde cada arquivo tem um propósito único e identificável, e onde as regras de negócio têm um único lugar para morar.
