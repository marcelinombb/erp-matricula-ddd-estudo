# Agregados — Aplicados na Arquitetura Tradicional

## O que é

Aggregate é um cluster de objetos tratado como unidade de consistência. O Aggregate Root controla o acesso — toda modificação passa pelos métodos do Root, nunca diretamente nos objetos internos. O princípio fundamental: as invariantes do grupo são protegidas pelo próprio Aggregate, não por um Service externo.

Isso elimina um problema sutil: quando a verificação e a modificação vivem em métodos separados de um Service, existe uma janela onde o estado pode mudar entre as duas operações. Dois usuários simultâneos podem executar a verificação ao mesmo tempo, obter "OK", e depois ambos fazer a modificação — resultando em estado inválido.

No Aggregate, verificação e modificação são o mesmo método no mesmo objeto. Não há janela. Para aprofundamento, ver `docs/02-design-tatico/agregados.md`.

---

## Manifestação no módulo camadas (ANTES)

Em `MatriculaServiceImpl.adicionarDisciplina()`, as regras de negócio vivem no Service. A entidade `Matricula` não tem opinião sobre o que pode ou não acontecer com ela:

```java
// ANTES: regras espalhadas no Service — sem garantia de consistência
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java
public void adicionarDisciplina(UUID matriculaId, String nomeDisciplina) {
    Matricula matricula = matriculaRepository.findById(matriculaId)...;

    // Verificação de estado no Service — entidade aceita qualquer String como status
    if (!"ATIVA".equals(matricula.getStatus())) {
        throw new RuntimeException("Matrícula não está ativa");
    }

    // Regra de limite como query SQL — separada da adição que vem depois
    int qtd = itemMatriculaRepository.countByMatriculaId(matriculaId);
    if (qtd >= 6) {
        throw new RuntimeException("Limite de disciplinas atingido");
    }

    // ... persistir disciplina (operação separada da verificação)
}
```

O problema de concorrência é real: dois usuários simultâneos podem cada um chamar `countByMatriculaId` e obter 5, ambos passarem pela verificação, e ambos adicionarem — resultado final: 7 disciplinas. A verificação e a adição são operações separadas no Service.

Arquivo completo: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java`

---

## Aplicação no módulo DDD (DEPOIS)

Em `Matricula.adicionarDisciplina()`, os três guards e a adição são o mesmo método no mesmo objeto. Não há como pular a verificação:

```java
// DEPOIS: invariantes protegidas pelo Aggregate — sem janela de concorrência
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java
public void adicionarDisciplina(NomeDisciplina disciplina) {
    // Guard 1: estado — cancelada não aceita novas disciplinas
    if (this.status instanceof StatusMatricula.Cancelada) {
        throw new MatriculaCanceladaException(this.id);
    }
    // Guard 2: limite — em memória, sem query SQL; atômico com a adição abaixo
    if (this.disciplinas.size() >= LIMITE_DISCIPLINAS) {
        throw new LimiteDisciplinasExcedidoException(LIMITE_DISCIPLINAS, this.disciplinas.size(), this.id);
    }
    // Guard 3: duplicidade — a mesma disciplina não pode aparecer duas vezes
    boolean jaMatriculada = this.disciplinas.stream()
        .anyMatch(item -> item.disciplina().equals(disciplina));
    if (jaMatriculada) {
        throw new DisciplinaJaMatriculadaException(disciplina, this.id);
    }
    this.disciplinas.add(new ItemMatricula(disciplina));
    this.eventos.add(new DisciplinaAdicionada(this.id, this.alunoId, disciplina, LocalDateTime.now()));
}
```

Os três guards e a adição são o mesmo método, na mesma chamada, no mesmo objeto. Um Service externo não tem como pular o Guard 1 e chamar `disciplinas.add()` diretamente — não existe acesso público à lista interna.

Arquivo completo: `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java`

---

## O que foi ganho

Verificação e modificação no mesmo objeto, na mesma transação — sem janela de concorrência. Regras não podem ser puladas por chamador externo: sem `setStatus("ATIVA")` público, sem acesso direto à lista `disciplinas`. Exceções específicas de domínio (`MatriculaCanceladaException`, `LimiteDisciplinasExcedidoException`) em vez de `RuntimeException` genérica.
