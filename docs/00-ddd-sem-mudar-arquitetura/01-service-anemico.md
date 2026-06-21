# Anti-padrão: Service Anêmico

## O que é

Toda regra de negócio do sistema concentrada no Service. As entidades existem, mas são passivas — apenas carregam dados. Quem decide, valida e protege invariantes é o Service.

O nome "anêmico" vem do modelo anêmico de Martin Fowler: quando o objeto de negócio não tem comportamento, ele é um container de dados sem vida própria.

---

## Manifestação no módulo camadas

Em `MatriculaServiceImpl.matricular()`, toda a lógica de negócio está no método do Service. A entidade `Aluno` existe mas não tem opinião sobre se pode ser matriculada — quem decide é o Service:

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java

// ANTI-PADRAO: Service Anêmico (DIAG-01)
// A regra "aluno inativo não pode ser matriculado" vive aqui, no Service.
// A entidade Aluno tem isAtivo() — um getter booleano — mas não tem
// estaAtivo() com semântica de negócio. Quem interpreta o boolean é o Service.
@Service
@Transactional
public class MatriculaServiceImpl implements MatriculaService {

    public UUID matricular(UUID alunoId, UUID turmaId, String periodoInicio, String periodoFim) {
        Aluno aluno = alunoRepository.findById(alunoId)
            .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + alunoId));

        // Regra de negócio no Service — não no objeto Aluno
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo não pode ser matriculado");
        }

        // Regra de duplicidade no Service — não na Matrícula
        boolean jaExiste = matriculaRepository.existsByAlunoIdAndPeriodo(alunoId, periodoInicio);
        if (jaExiste) {
            throw new RuntimeException("Aluno já matriculado neste período");
        }

        // ... lógica de criação e persistência
    }
}
```

Arquivo Java completo: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java`

---

## Consequência

Quando a regra "aluno inativo não pode ser matriculado" muda — por exemplo, alunos em período de adaptação podem se matricular condicionalmente — o desenvolvedor precisa:

1. Localizar todos os lugares no código que verificam `aluno.isAtivo()` antes de uma operação
2. Avaliar cada um individualmente para decidir se a exceção se aplica
3. Atualizar cada ocorrência de forma coerente

Com o modelo anêmico, essa busca traversa todos os Services que lidam com alunos. A regra está espalhada onde for conveniente para o método que precisou dela.

---

## Contraste DDD

No módulo DDD, a verificação de elegibilidade vive em `VerificadorElegibilidadeMatricula` — um Domain Service sem dependências de framework. Os UseCases não tomam essa decisão:

```java
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java

// A regra de elegibilidade encapsulada no Domain Service.
// Qualquer UseCase que precise verificar se um aluno pode ser matriculado
// chama este serviço — não reimplementa a regra.
public void verificar(Aluno aluno, Turma turma, PeriodoLetivo periodo) {
    if (!aluno.estaAtivo()) {
        throw new AlunoInativoException(aluno.getId());
    }
    // ... demais verificações de elegibilidade
}
```

Arquivo Java: `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java`

Quando a regra de elegibilidade muda no módulo DDD, apenas `VerificadorElegibilidadeMatricula` muda. Os UseCases que o chamam não são modificados.

---

## Relação com outros anti-padrões

Service Anêmico e Service Deus (DIAG-03) coexistem neste módulo. O Service está inchado com 200+ linhas exatamente porque o modelo não tem comportamento — cada regra que deveria viver no objeto migra para o Service. Os dois anti-padrões se alimentam: modelo sem comportamento força lógica para o Service, Service cresce, equipe acrescenta mais lógica no Service porque é onde tudo já está.
