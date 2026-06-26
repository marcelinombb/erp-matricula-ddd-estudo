# Linguagem Ubíqua — Aplicada na Arquitetura Tradicional

## O que é

Linguagem Ubíqua é o vocabulário compartilhado entre especialistas do domínio e
desenvolvedores. Os mesmos termos aparecem no código, nas reuniões e nos documentos.
Quando uma secretária diz "vou matricular o aluno", o código deve ter `matricularAluno` —
não `createRegistration` nem `insertEnrollmentRecord`.

O benefício prático: um especialista do domínio consegue ler o código e apontar
inconsistências sem precisar de tradução mental entre o vocabulário do negócio e o
vocabulário técnico. Quando o código fala "find" e o negócio fala "buscar", cada leitura
exige uma camada extra de interpretação.

Para aprofundamento teórico, ver `docs/01-design-estrategico/linguagem-ubiqua.md`.

---

## Manifestação no módulo camadas (ANTES)

Arquivo de referência:
`erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java`

```java
// ANTES: Nomenclatura técnica, não de domínio
// Arquivo: erp-matricula-camadas/src/main/java/.../service/MatriculaServiceImpl.java

// "impl" e "service" são papéis técnicos — não termos da secretaria escolar
@Service
public class MatriculaServiceImpl implements MatriculaService {

    // "findById" — terminologia de framework/banco ("find" vem de SQL finder methods)
    // "UUID alunoId" — tipo primitivo sem semântica de domínio
    public UUID matricular(UUID alunoId, UUID turmaId,
                           String periodoInicio, String periodoFim) { ... }

    // Campos como String — valor mágico sem tipo:
    // "ATIVA", "CANCELADA", "CONCLUIDA" — digitação livre, sem compilador
    private String status;
}
```

Arquivo Java completo:
`erp-matricula-camadas/src/main/java/br/com/escola/matricula/service/MatriculaServiceImpl.java`

---

## Aplicação no módulo DDD (DEPOIS)

Arquivos de referência:
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java`
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java`
- `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/modelo/StatusMatricula.java`

```java
// DEPOIS: Linguagem da secretaria escolar
// Arquivo: erp-matricula-ddd/src/main/java/.../aplicacao/MatricularAlunoUseCase.java

// "matricular aluno" é como a secretaria nomeia a operação — não "impl", não "service"
// "executar(command)" — o UseCase executa uma intenção de negócio
public UUID executar(MatricularAlunoCommand command) { ... }

// Arquivo: .../dominio/repositorio/MatriculaRepositorio.java
// "buscarPorId" — português do domínio, não "findById"
// "buscarPorAluno" — semântica de negócio, não "findByAlunoId"
Optional<Matricula> buscarPorId(UUID id);
List<Matricula> buscarPorAluno(UUID alunoId);

// Arquivo: .../dominio/modelo/StatusMatricula.java
// Tipos selados — não strings. O compilador rejeita estado inválido.
sealed interface StatusMatricula permits Ativa, Cancelada, Concluida { ... }

// Arquivo: .../dominio/modelo/Aluno.java
// "estaAtivo()" com semântica de negócio — não getter booleano "isAtivo()"
public boolean estaAtivo() { return this.ativo; }
```

---

## O que foi ganho

Um especialista do domínio consegue ler `MatricularAlunoUseCase.executar()` e reconhecer a
operação sem tradução mental — "matricular aluno" é exatamente o que a secretaria chama de
matricular um aluno.

O compilador rejeita `StatusMatricula` inválido: uma string `"ACTIVA"` (erro de digitação)
passa pela compilação; um `StatusMatricula` inexistente não compila. Quando um novo estado for
adicionado à `sealed interface`, o compilador aponta cada `switch` que precisa ser atualizado
— sem silent bug em produção.
