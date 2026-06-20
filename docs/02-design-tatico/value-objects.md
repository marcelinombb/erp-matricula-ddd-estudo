# Value Objects — Matrícula Escolar

Imagine que CPF seja representado como uma `String` comum. Em um service de matrícula típico, o código que verifica se o CPF é válido precisaria ser duplicado em todo lugar que recebe um CPF como parâmetro: no service que matricula o aluno, no service que consulta histórico, no controller que valida a entrada, talvez até no repositório. Cada ponto de validação pode ter uma regra ligeiramente diferente — um verifica 11 dígitos, outro verifica com a máscara, um terceiro esquece de verificar o dígito verificador. O resultado: um CPF inválido entra no sistema, passa pela compilação sem problema, e só gera erro em produção, em um ponto distante de onde foi criado.

Além disso, uma `String` não carrega intenção. O método `matricular(String cpfAluno, String periodoLetivo)` recebe duas Strings — o compilador não tem como saber se o chamador passou os argumentos na ordem certa. Alguém pode chamar `matricular("2026-1", "123.456.789-09")` e o compilador aceita sem reclamar.

O problema não é a ausência de validação — é que a validação não tem um lar. Em vez de viver em um único lugar, ela está espalhada por todo o sistema, duplicada e sujeita a divergências. DDD resolve isso com um padrão chamado **Value Object**: um tipo que encapsula um valor junto com suas regras de validade, é imutável, e é comparado por valor — não por identidade.

## Value Objects do Domínio de Matrícula

| VO | Tipo Java 21 | Validação no Construtor Compacto | Complexidade |
|----|-------------|----------------------------------|-------------|
| `Cpf` | `record Cpf(String valor)` | Remove máscara, verifica 11 dígitos, algoritmo dígito verificador | ALTA |
| `PeriodoLetivo` | `record PeriodoLetivo(int ano, int semestre)` | `ano >= 2000`, `semestre 1..2` | BAIXA |
| `MatriculaId` | `record MatriculaId(UUID valor)` | `Objects.requireNonNull(valor)` | MÍNIMA |
| `AlunoId` | `record AlunoId(UUID valor)` | `Objects.requireNonNull(valor)` | MÍNIMA |
| `TurmaId` | `record TurmaId(UUID valor)` | `Objects.requireNonNull(valor)` | MÍNIMA |
| `NomeDisciplina` | `record NomeDisciplina(String valor)` | não nulo, não branco, máx 100 chars | BAIXA |

> **Lição:** Value Objects não têm identidade — dois `PeriodoLetivo(2026, 1)` são iguais porque representam o mesmo período. O Java 21 `record` implementa `equals`/`hashCode` automaticamente por todos os campos — exatamente o comportamento que DDD espera de um VO.

---

## `Cpf`

### Por que `Cpf` não é uma `String`

Um CPF tem invariantes de negócio: 11 dígitos numéricos, algoritmo de dígito verificador específico. Se essas regras vivem fora do tipo, qualquer parte do sistema pode criar um CPF inválido — e o compilador não vai reclamar. Com um `record`, a criação de um `Cpf` inválido é impossível em tempo de execução: o construtor lança exceção antes que o objeto exista.

```java
// Java 21: construtor compacto de record — validação sem boilerplate de construtor explícito
// DDD fit: imutabilidade garantida pelo compilador = sem setter possível, sem mutação acidental
public record Cpf(String valor) {

    public Cpf {  // construtor compacto — `valor` já está disponível, sem parâmetros explícitos
        Objects.requireNonNull(valor, "CPF não pode ser nulo");

        String apenasDigitos = valor.replaceAll("[^0-9]", "");

        if (apenasDigitos.length() != 11) {
            throw new IllegalArgumentException(
                "CPF deve ter 11 dígitos numéricos. Recebido: " + valor
            );
        }

        // Verificação do dígito verificador:
        // A lógica completa do algoritmo módulo 11 será implementada na Fase 3.
        // Aqui, o placeholder deixa a intenção explícita sem implementação prematura.
        if (!cpfComDigitoVerificadorValido(apenasDigitos)) {
            throw new IllegalArgumentException(
                "CPF com dígito verificador inválido: " + apenasDigitos
            );
        }

        // Normalização: armazena apenas os dígitos, sem máscara
        valor = apenasDigitos;
    }

    private static boolean cpfComDigitoVerificadorValido(String digitos) {
        // TODO Fase 3: implementar algoritmo módulo 11
        // A lógica de validação real estará na implementação da Fase 3.
        // Por ora, retorna true para não bloquear o desenvolvimento dos demais padrões.
        return true;
    }

    // Formato de exibição com máscara — separado do armazenamento
    public String formatado() {
        return String.format("%s.%s.%s-%s",
            valor.substring(0, 3),
            valor.substring(3, 6),
            valor.substring(6, 9),
            valor.substring(9, 11)
        );
    }
}
```

Dois `Cpf` com o mesmo valor são iguais — o Java 21 `record` gera `equals`/`hashCode` automaticamente por todos os campos:

```java
Cpf cpf1 = new Cpf("123.456.789-09");
Cpf cpf2 = new Cpf("123.456.789-09");
cpf1.equals(cpf2); // true — sem @Override necessário, record faz isso automaticamente
```

---

## `PeriodoLetivo`

### Por que `PeriodoLetivo` não é uma `String`

Um período letivo como `"2026-1"` parece simples. Mas como `String`, nada impede `"2026-3"` (semestre inválido), `"99-1"` (ano impossível) ou `"primeiro semestre de 2026"` (formato inconsistente). Como `record`, as regras de validade vivem no construtor — e uma vez criado, um `PeriodoLetivo` é sempre válido.

```java
// Java 21: record com construtor compacto para validação de múltiplos campos relacionados
// DDD fit: Value Object de dois campos — equals por (ano, semestre) juntos, imutável por design
public record PeriodoLetivo(int ano, int semestre) {

    public PeriodoLetivo {  // construtor compacto
        if (ano < 2000) {
            throw new IllegalArgumentException(
                "Ano do período letivo inválido: " + ano + ". Mínimo: 2000."
            );
        }
        if (semestre < 1 || semestre > 2) {
            throw new IllegalArgumentException(
                "Semestre inválido: " + semestre + ". Valores permitidos: 1 ou 2."
            );
        }
    }

    // Representação canônica do período — usada em exibição e logs
    public String descricao() {
        return ano + "-" + semestre;
    }

    // Verifica se o período ainda está aberto para matrículas
    // (lógica real depende de regras institucionais — placeholder para Fase 3)
    public boolean estaAberto() {
        // TODO Fase 3: comparar com data de encerramento configurável do período
        return true;
    }
}
```

---

## `NomeDisciplina`

### Por que `NomeDisciplina` não é uma `String`

O nome de uma disciplina tem restrições claras: não pode ser nulo, não pode ser em branco, e tem um comprimento máximo (100 caracteres — limite da coluna no banco). Com um VO, essas restrições são verificadas uma vez, no construtor. Qualquer `NomeDisciplina` que existe no sistema já passou por essa verificação.

```java
// Java 21: record com validação de String — restrições de comprimento e conteúdo centralizadas
// DDD fit: impossível ter NomeDisciplina em branco ou maior que o limite no banco
public record NomeDisciplina(String valor) {

    public NomeDisciplina {
        Objects.requireNonNull(valor, "Nome da disciplina não pode ser nulo");
        if (valor.isBlank()) {
            throw new IllegalArgumentException("Nome da disciplina não pode ser em branco");
        }
        if (valor.length() > 100) {
            throw new IllegalArgumentException(
                "Nome da disciplina excede 100 caracteres: " + valor.length()
            );
        }
        valor = valor.strip(); // normaliza espaços nas bordas
    }
}
```

---

### IDs Tipados

`MatriculaId`, `AlunoId` e `TurmaId` seguem o mesmo padrão: um `record` com um único campo `UUID valor` e validação de nulo. A razão de existirem como tipos separados é demonstrada no ADR-003 — com UUID cru, o compilador não detecta parâmetros trocados; com IDs tipados, a troca é um erro de compilação.

> Os IDs tipados (`AlunoId`, `TurmaId`, `MatriculaId`) são Value Objects que funcionam como chaves de referência entre Aggregates — ver [ADR-003](../adrs/ADR-003-referencia-por-id.md).

```java
// Java 21: record como ID tipado — estrutura mínima para máxima segurança de tipos
// DDD fit: AlunoId e TurmaId são tipos distintos; o compilador impede confusão entre eles

public record MatriculaId(UUID valor) {
    public MatriculaId {
        Objects.requireNonNull(valor, "MatriculaId não pode ser nulo");
    }
}

public record AlunoId(UUID valor) {
    public AlunoId {
        Objects.requireNonNull(valor, "AlunoId não pode ser nulo");
    }
}

public record TurmaId(UUID valor) {
    public TurmaId {
        Objects.requireNonNull(valor, "TurmaId não pode ser nulo");
    }
}
```

Sem IDs tipados:

```java
// Compilador aceita — mas a ordem dos parâmetros está errada
matricular(turmaId, alunoId); // UUID, UUID — silenciosamente incorreto
```

Com IDs tipados:

```java
// Compilador rejeita — AlunoId esperado, TurmaId fornecido
matricular(turmaId, alunoId); // erro de compilação: incompatible types
```

---

## Erros Comuns

### Erro 1: String primitiva vaza validação por todo o sistema

O desenvolvedor acostumado com arquitetura em camadas naturalmente usa `String` para representar CPF — afinal, CPF é "só um texto". O problema aparece quando a validação precisa ser feita em vários lugares.

```java
// ERRADO — String crua: validação duplicada e espalhada, impossível garantir consistência
@Service
public class MatriculaService {

    @Autowired
    private MatriculaRepository matriculaRepository;

    public void matricular(String cpfAluno, String periodo) {
        // Validação duplicada aqui — e provavelmente diferente do controller
        if (cpfAluno == null || cpfAluno.replaceAll("[^0-9]", "").length() != 11) {
            throw new RuntimeException("CPF inválido");
        }
        // "periodo" pode ser "2026-3", "segundo semestre", "2026/1" — tudo aceito
        // O método não tem como saber se "periodo" é um valor válido
        // ...
    }
}
```

```java
// CERTO — tipos de domínio: validação concentrada no VO, impossível criar valor inválido
public class MatricularAlunoUseCase {

    public void executar(Cpf cpf, PeriodoLetivo periodo) {
        // Se chegou aqui, Cpf e PeriodoLetivo já são válidos.
        // É impossível passar Cpf inválido — o construtor do record teria lançado exceção.
        // É impossível passar PeriodoLetivo com semestre 3 — o construtor rejeitaria.
        // Sem validação duplicada, sem if espalhado.
    }
}
```

### Erro 2: Comparação de Value Objects pelo valor interno, não pelo VO

Com tipos primitivos, o desenvolvedor compara os valores internos diretamente. Com `record`, isso é desnecessário — e revela que o desenvolvedor não confia que o `equals` gerado pelo `record` é por valor.

```java
// ERRADO — comparação interna desnecessária; esconde a semântica de "mesmo CPF"
if (cpf1.valor().equals(cpf2.valor())) {
    // isso funciona, mas é verboso e não usa o contrato do VO
}

// ERRADO — comparação com == (compara referências, não valores)
if (cpf1 == cpf2) {
    // apenas verdadeiro se for literalmente o mesmo objeto — quase nunca o que se quer
}
```

```java
// CERTO — record Java 21 gera equals/hashCode automaticamente por todos os campos
if (cpf1.equals(cpf2)) {
    // true se e somente se cpf1.valor().equals(cpf2.valor())
    // O record implementa isso automaticamente — sem @Override necessário
}
```
