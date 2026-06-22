# Exercício: Regras de Domínio ou de Aplicação?

Para cada regra abaixo, decida mentalmente: "Esta regra é de **Domínio** ou de
**Aplicação**?" Depois expanda o gabarito para conferir e ler a justificativa.
Todas as regras vêm do domínio de matrícula escolar implementado no módulo
`erp-matricula-ddd`.

Tente responder antes de ler o gabarito — o objetivo é praticar o raciocínio,
não memorizar as respostas.

---

## Critério de Classificação

Uma regra é de **Domínio** se seria verdadeira independentemente de qual banco de dados,
framework, canal de entrega (HTTP, batch, CLI) ou sistema externo o sistema use. Uma regra
é de **Aplicação** se só existe porque o sistema tem uma arquitetura específica, orquestra
múltiplos contextos, ou existe por requisito de infraestrutura.

---

## As 10 Regras

---

### Regra 1: Matrícula cancelada não aceita novas disciplinas

<details>
<summary>Ver resposta</summary>

**Classificação: Domínio**

**Justificativa:** Esta regra seria verdadeira independentemente de canal de entrega ou
banco de dados. A secretaria não aceita disciplinas em matrículas canceladas — não porque
o sistema tem um banco relacional, mas porque o negócio assim define. Está implementada em
`Matricula.adicionarDisciplina()` como Guard 1:
`if (this.status instanceof StatusMatricula.Cancelada)`.
</details>

---

### Regra 2: Máximo 6 disciplinas por matrícula

<details>
<summary>Ver resposta</summary>

**Classificação: Domínio**

**Justificativa:** O limite de 6 disciplinas é uma decisão acadêmica da instituição — não
uma restrição de banco de dados ou de performance. Seria verdadeira mesmo em um sistema de
papel. Está implementada como `LIMITE_DISCIPLINAS = 6` em `Matricula.java` e verificada
em Guard 2 de `adicionarDisciplina()`: `if (this.disciplinas.size() >= LIMITE_DISCIPLINAS)`.
</details>

---

### Regra 3: A mesma disciplina não pode aparecer duas vezes na mesma matrícula

<details>
<summary>Ver resposta</summary>

**Classificação: Domínio**

**Justificativa:** A regra de não duplicidade é uma decisão pedagógica da instituição —
não depende de banco de dados (não é uma UNIQUE constraint que define a regra; a regra
existe antes do banco). Está implementada em Guard 3 de `Matricula.adicionarDisciplina()`
com `stream().anyMatch(item -> item.disciplina().equals(disciplina))` — verificação em
memória, sem query SQL.
</details>

---

### Regra 4: Aluno inativo não pode ser matriculado

<details>
<summary>Ver resposta</summary>

**Classificação: Domínio**

**Justificativa:** A academia não matricula alunos inativos independentemente de qual
sistema, canal ou banco de dados o suporta. Está implementada em
`VerificadorElegibilidadeMatricula.verificar()`: `if (!aluno.estaAtivo())`. Compare com
o módulo camadas: a mesma regra estava em `MatriculaServiceImpl.matricular()` —
no lugar certo, mas no objeto errado.
</details>

---

### Regra 5: Abrir transação para garantir atomicidade

<details>
<summary>Ver resposta</summary>

**Classificação: Aplicação**

**Justificativa:** Esta regra existe porque o sistema usa um banco de dados transacional.
Em um sistema de arquivo plano ou em memória, transações não existem — mas as regras de
negócio das outras categorias continuariam válidas. Está implementada como `@Transactional`
em `MatricularAlunoUseCase` — anotação de infraestrutura no Application Service, não no
domínio.
</details>

---

### Regra 6: Enviar e-mail de confirmação após matricular

<details>
<summary>Ver resposta</summary>

**Classificação: Aplicação**

**Justificativa:** Esta regra existe porque o sistema tem um canal de comunicação
(e-mail). O domínio de matrícula não sabe sobre e-mail — ele publica um evento
`AlunoMatriculado`. Um listener de evento (na infraestrutura) reage ao evento e envia o
e-mail. Se o canal mudar para SMS ou push notification, o domínio não muda — apenas o
listener muda.
</details>

---

### Regra 7: Verificar se o período letivo da turma está aberto

<details>
<summary>Ver resposta</summary>

**Classificação: Domínio**

**Justificativa:** Esta regra parece "validação de entrada", mas é uma decisão acadêmica:
a secretaria não aceita matrículas em períodos fechados — verdadeiro em qualquer canal de
entrega. Está em `VerificadorElegibilidadeMatricula.verificar()`:
`if (!turma.periodoEstaAberto())`. A encapsulação em `turma.periodoEstaAberto()` (em vez
de verificar datas no Service) é exatamente o que diferencia Entidade com comportamento
de Entidade Anêmica.
</details>

---

### Regra 8: Validar formato de CPF (11 dígitos, dígito verificador)

<details>
<summary>Ver resposta</summary>

**Classificação: Domínio**

**Justificativa:** Esta parece uma regra técnica (algoritmo de dígito verificador), mas
um CPF com formato inválido é simplesmente um dado inconsistente — a regra seria
verdadeira mesmo sem sistema de software. A Receita Federal define o formato de CPF; o
domínio o valida. Está implementada no construtor do record `Cpf.java` — se você tentar
criar `new Cpf("123")`, o construtor lança exceção antes que o objeto exista. Compare com
o módulo camadas: `String cpf` aceita qualquer string em tempo de compilação.
</details>

---

### Regra 9: Coordenar Financeiro para criar cobrança após matrícula

<details>
<summary>Ver resposta</summary>

**Classificação: Aplicação**

**Justificativa:** Esta parece uma regra de negócio ("aluno matriculado deve pagar"), mas
a **coordenação entre Bounded Contexts** é responsabilidade da camada de aplicação. O
Bounded Context de Matrícula não sabe como cobrar — ele publica um evento `AlunoMatriculado`
e o Bounded Context Financeiro reage. Se o Financeiro mudar sua lógica de cobrança, o
domínio de Matrícula não muda. A regra "aluno matriculado gera cobrança" pertence ao
Financeiro; a regra "matrícula ativa tem aluno elegível" pertence ao Domínio de Matrícula.
</details>

---

### Regra 10: Não pode existir matrícula ativa duplicada para o mesmo aluno e período

<details>
<summary>Ver resposta</summary>

**Classificação: Domínio**

**Justificativa:** Esta parece uma "validação de banco" (UNIQUE constraint), mas a regra
existe porque a academia decide que um aluno só pode ter uma matrícula ativa por período —
independentemente de banco. A UNIQUE constraint no banco é apenas o reforço de infraestrutura
para uma regra que já existe no domínio. Está implementada em
`VerificadorElegibilidadeMatricula.verificar()`:
`if (repositorio.existeMatriculaAtiva(aluno.getId(), periodo))`. O método
`existeMatriculaAtiva` usa linguagem de negócio — não `countWhere` ou `findDuplicate`.
</details>

---

## Onde encontrar cada regra no código

| Classificação | Onde no módulo DDD |
|---------------|--------------------|
| Domínio | `Matricula.java` — Guards em `adicionarDisciplina()` e `cancelar()` |
| Domínio | `VerificadorElegibilidadeMatricula.java` — elegibilidade e duplicidade |
| Domínio | `Cpf.java` — validação no construtor do record |
| Aplicação | `MatricularAlunoUseCase.java` — `@Transactional`, publicação de eventos |
| Aplicação | Listeners de evento na infraestrutura — e-mail, cobrança |
