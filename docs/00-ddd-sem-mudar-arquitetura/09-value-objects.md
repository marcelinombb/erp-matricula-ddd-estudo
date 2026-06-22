# Value Objects — Aplicados na Arquitetura Tradicional

## O que é

Value Object é um objeto definido pelos seus valores, não por identidade. Dois `PeriodoLetivo(2026, 1)` são o mesmo período — `equals` compara valores, não referências. Não faz sentido perguntar "qual desses dois períodos é o original?": se os valores são iguais, são o mesmo objeto.

A propriedade mais importante é a imutabilidade. Após criado, um Value Object não muda — se precisar de um valor diferente, cria-se um novo objeto. Isso elimina toda uma classe de bugs: efeitos colaterais por compartilhamento de referência não existem em objetos imutáveis.

A consequência prática: toda validação vive no construtor. Se o objeto foi criado, é válido — sem precisar verificar em nenhum outro lugar. Para a teoria completa, ver `docs/02-design-tatico/value-objects.md`.

---

## Manifestação no módulo camadas (ANTES)

Em `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java`, o período letivo é representado como duas `String`s soltas. Não há validação — qualquer valor é aceito:

```java
// ANTES: primitivos sem validação — qualquer valor é aceito em tempo de compilação
// erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java
public class Matricula {

    private String periodoInicio;   // "2026-01-01" ou "" ou null — compile OK
    private String periodoFim;      // "31/99/2026" (data inválida) — compile OK
    private UUID alunoId;           // UUID sem semântica: e se passar um turmaId por engano?
    private String status;          // "ATIVA" ou "ACTIVA" (erro de digitação) — compile OK

    // setters sem validação — o chamador decide se os dados fazem sentido
    public void setPeriodoInicio(String periodoInicio) { this.periodoInicio = periodoInicio; }
    public void setStatus(String status) { this.status = status; }
}
```

Um `periodoInicio = ""` ou `status = "ACTIVA"` (erro de digitação) passa pela compilação sem nenhuma reclamação. O erro só aparece em runtime, horas depois, em um log de exceção genérico.

Arquivo completo: `erp-matricula-camadas/src/main/java/br/com/escola/matricula/model/Matricula.java`

---

## Aplicação no módulo DDD (DEPOIS)

No módulo DDD, cada conceito com regras próprias se torna um record Java 21. O construtor compacto é o único lugar onde a validação vive:

```java
// DEPOIS: records imutáveis com validação no construtor — válido se existe
// erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/PeriodoLetivo.java
public record PeriodoLetivo(int ano, int semestre) {
    public PeriodoLetivo {  // construtor compacto — executado a cada new PeriodoLetivo(...)
        if (ano < 2000)
            throw new IllegalArgumentException("Ano inválido: " + ano + ". Mínimo: 2000.");
        if (semestre < 1 || semestre > 2)
            throw new IllegalArgumentException("Semestre inválido: " + semestre + ". Permitidos: 1 ou 2.");
    }
}

// erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/NomeDisciplina.java
public record NomeDisciplina(String valor) {
    public NomeDisciplina {
        Objects.requireNonNull(valor, "Nome da disciplina não pode ser nulo");
        valor = valor.strip();  // normaliza espaços antes de validar
        if (valor.isBlank())
            throw new IllegalArgumentException("Nome da disciplina não pode ser em branco");
        if (valor.length() > 100)
            throw new IllegalArgumentException("Nome excede 100 caracteres: " + valor.length());
    }
}
```

Um `PeriodoLetivo(2026, 3)` é impossível de criar — o construtor lança `IllegalArgumentException` antes que o objeto exista. Com `String periodoFim`, um semestre 3 passa pela compilação e só falha em runtime, horas depois. O record garante que, se o objeto existe, é válido — sem checagem adicional em nenhum Service.

Arquivos completos: `erp-matricula-ddd/src/main/java/br/com/escola/matricula/dominio/vo/`

---

## O que foi ganho

Erros de dados são impossíveis em tempo de compilação: se o tipo existe, o valor é válido. A validação vive no construtor do record — sem duplicação em múltiplos Services. Records Java 21 são naturalmente imutáveis: sem setters, sem efeitos colaterais por compartilhamento de referência.
