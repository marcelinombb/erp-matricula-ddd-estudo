# Problema de Negócio — Matrícula Escolar

## O que o sistema resolve

Uma instituição de ensino precisa vincular alunos a turmas e disciplinas em períodos letivos específicos. Esse processo parece simples à primeira vista — registrar que "o aluno X está na turma Y" — mas esconde uma complexidade que não aparece em um CRUD genérico. Para que uma matrícula seja válida, o sistema precisa garantir simultaneamente que o aluno está ativo, que o período letivo está aberto para matrículas, que não existe matrícula duplicada para o mesmo aluno no mesmo período e que o número de disciplinas não ultrapassa o limite definido pela instituição.

O que torna esse domínio ainda mais complexo é que o mesmo dado — um "aluno" — precisa ser visto de formas completamente diferentes dependendo do contexto. Para a Secretaria, o que importa é se o aluno está matriculado e em qual turma. Para o Financeiro, o que importa é se esse aluno tem contrato ativo e mensalidades em dia. Para o Acadêmico, o que importa são as disciplinas que ele está cursando e seu histórico de notas. Essas visões coexistem no mesmo sistema, mas seguem regras e ciclos de evolução independentes.

É exatamente essa complexidade — regras de consistência fortes, múltiplos contextos com visões diferentes do mesmo conceito, e eventos que cruzam fronteiras entre contextos — que justifica a aplicação de Domain-Driven Design (DDD) neste projeto. A arquitetura tradicional em camadas (Controller → Service → Repository) não oferece ferramentas para expressar essas fronteiras de forma explícita no código.

## Usuários do sistema

| Usuário | Papel no Domínio | Principal Interesse |
|---------|-----------------|---------------------|
| Secretaria | Registra e gerencia matrículas, turmas e períodos letivos | Garantir que matrículas sejam válidas e que turmas não excedam capacidade |
| Aluno | É matriculado em turmas e disciplinas | Consultar sua situação de matrícula, disciplinas cursadas e status atual |
| Responsável Financeiro | Acompanha contratos e mensalidades vinculados às matrículas | Verificar valores, vencimentos e situação financeira do aluno |
| Coordenador Acadêmico | Monitora ocupação de turmas e acompanha histórico dos alunos | Visibilidade sobre vagas disponíveis, fluxo de matrículas e desempenho acadêmico |

## Fluxos Principais

### Realizar Matrícula

O fluxo central do sistema. A Secretaria (ou o sistema automatizado) inicia uma matrícula para um aluno em uma turma em um período letivo específico. Antes de registrar o vínculo, o sistema precisa validar que o aluno está ativo e sem impedimentos, que o período letivo está aberto para novas matrículas, que não existe matrícula ativa para esse aluno no mesmo período e que a turma possui vagas disponíveis. Se todas as condições estiverem satisfeitas, a matrícula é registrada e o evento `AlunoMatriculado` é publicado — notificando os contextos de Financeiro (para criar o contrato) e Acadêmico (para criar o vínculo acadêmico).

### Adicionar Disciplina

Após a matrícula ser realizada, o aluno pode adicionar disciplinas à sua matrícula no mesmo período letivo. Para que a adição seja válida, a matrícula precisa estar ativa (não cancelada), a disciplina não pode já estar incluída na matrícula (sem duplicidade) e o número total de disciplinas não pode ultrapassar o limite estabelecido. Quando uma disciplina é adicionada com sucesso, o evento `DisciplinaAdicionada` é publicado para o contexto Acadêmico, que reserva a vaga na disciplina correspondente.

### Cancelar Matrícula

O encerramento do vínculo entre aluno e turma em um período letivo. O cancelamento pode ser iniciado pela Secretaria ou solicitado pelo próprio aluno. Uma matrícula cancelada não pode receber novas disciplinas nem ser reativada (é um estado terminal). Ao cancelar, o sistema publica o evento `MatriculaCancelada` para os contextos dependentes: Financeiro (para cancelar ou ajustar o contrato financeiro) e Acadêmico (para liberar as vagas nas disciplinas e encerrar o vínculo acadêmico).

## Por que DDD para este domínio

**Regras de negócio complexas que precisam de encapsulamento no domínio.** As validações de matrícula — aluno ativo, período aberto, sem duplicidade, limite de disciplinas — não são simples checagens que cabem em um `if` num Service. São invariantes que devem sempre ser verdadeiras enquanto uma matrícula existe. DDD oferece o padrão Aggregate para encapsular essas regras no próprio objeto de domínio, tornando impossível criar uma matrícula inválida por esquecimento.

**Múltiplos contextos com modelos diferentes do mesmo conceito.** "Aluno" no BC Matrícula é apenas uma referência (o que importa: pode se matricular?) — veja [Linguagem Ubíqua §Conceitos Ambíguos](linguagem-ubiqua.md#conceitos-ambíguos) para a comparação completa entre contextos. Se o sistema usasse uma única classe `Aluno` compartilhada, cada mudança no modelo financeiro ou acadêmico quebraria o código de matrícula, e vice-versa. DDD resolve isso com Bounded Contexts — cada contexto tem seu próprio modelo, mantido de forma independente.

**Eventos que cruzam fronteiras entre contextos.** A matrícula gera consequências em outros contextos: o Financeiro precisa criar um contrato e o Acadêmico precisa criar um vínculo. Mas a lógica de matrícula não deve conhecer os detalhes de cobrança ou de gestão acadêmica. Domain Events (`AlunoMatriculado`, `DisciplinaAdicionada`, `MatriculaCancelada`) permitem essa comunicação desacoplada — Matrícula publica o evento, e cada contexto consumidor decide o que fazer com ele.

**O diferencial competitivo está nas regras de Matrícula, não na infraestrutura.** Uma instituição de ensino não se diferencia pelo framework de persistência que usa. Ela se diferencia pela qualidade e fidelidade das regras de negócio que seu sistema aplica. DDD direciona o esforço de engenharia para o lugar certo: o modelo de domínio que expressa essas regras, isolado de frameworks e banco de dados.
