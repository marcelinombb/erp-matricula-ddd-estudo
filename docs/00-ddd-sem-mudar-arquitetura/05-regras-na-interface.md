# Anti-padrão: Regras na Interface

## O que é

Validações de regra de negócio executadas apenas no Controller HTTP. A regra existe como código na camada de interface, não na camada de serviço ou no modelo de domínio. Para que a regra seja aplicada, o sistema precisa receber a requisição pelo protocolo HTTP — não existe proteção equivalente para outros pontos de entrada.

---

## Manifestação no módulo camadas

`MatriculaController` verifica se o período de matrícula está dentro do limite aceitável. Essa validação não existe em `MatriculaServiceImpl` — se o Service for chamado diretamente, a regra não é aplicada:

```java
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/MatriculaController.java

@RestController
@RequestMapping("/matriculas")
public class MatriculaController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse matricular(@RequestBody @Valid MatricularRequest request) {

        // ANTI-PADRAO: Regras na Interface (DIAG-05)
        // Esta validação existe APENAS aqui no Controller.
        // Um batch job que chama MatriculaService.matricular() diretamente
        // não passa por esta validação — o período pode estar fechado.
        // A regra de negócio depende do protocolo HTTP para existir.
        if (request.periodoInicio().isBefore(LocalDate.now().minusMonths(6))) {
            return ResponseEntity.badRequest().body("Período muito antigo para matrícula");
        }

        UUID matriculaId = matriculaService.matricular(
            request.alunoId(),
            request.turmaId(),
            request.periodoInicio().toString(),
            request.periodoFim().toString()
        );

        return new MatriculaResponse(matriculaId);
    }
}
```

Arquivo Java completo: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/controller/MatriculaController.java`

---

## Quando a regra desaparece

A validação acima é invisível para qualquer código que não passe pelo Controller HTTP:

**Batch job noturno:** um processo que importa matrículas de um sistema legado chama `MatriculaServiceImpl.matricular()` diretamente. O período antigo passa sem validação.

**Teste de integração:** um teste que chama o Service diretamente para verificar uma regra de cancelamento pode, inadvertidamente, criar matrículas com período inválido porque a barreira do Controller não existe no contexto de teste.

**Chamada interna entre serviços:** um segundo contexto do sistema que usa o `MatriculaService` como dependência não passa pelo filtro HTTP.

Em todos esses casos, a regra "não aceitar matrículas com período anterior a 6 meses" simplesmente não existe.

---

## Sutil mas difícil de detectar

Este é o mais difícil dos seis anti-padrões de encontrar em code review. Os testes de contrato HTTP passam — o endpoint rejeita períodos antigos corretamente. O comportamento só diverge quando o Service é invocado fora do contexto HTTP.

A pergunta que revela o anti-padrão: "Se eu chamar `MatriculaService.matricular()` diretamente em um teste de integração, a regra de período ainda é aplicada?" Se a resposta for não, a regra está no lugar errado.

---

## Contraste DDD

No módulo DDD, o Controller não contém regras de negócio. Ele recebe a requisição, converte os dados para os tipos do domínio, e delega para o UseCase:

```java
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java

@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public MatriculaResponse matricular(@RequestBody @Valid MatricularRequest request) {
    // O Controller não decide nada de negócio.
    // Converte os dados da requisição e delega.
    var command = new MatricularAlunoCommand(
        UUID.fromString(request.alunoId()),
        UUID.fromString(request.turmaId()),
        PeriodoLetivo.de(request.periodoInicio(), request.periodoFim())
    );
    UUID matriculaId = matricularAlunoUseCase.executar(command);
    return new MatriculaResponse(matriculaId);
}
```

A validação de período vive no UseCase ou no Domain Service `VerificadorElegibilidadeMatricula` — ativa independente de como o UseCase for chamado.
