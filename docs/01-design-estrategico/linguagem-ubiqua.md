# Linguagem Ubíqua — Matrícula Escolar

Em DDD, Linguagem Ubíqua é a linguagem compartilhada entre especialistas de negócio e desenvolvedores. Não é um dicionário separado da documentação técnica — é a linguagem que aparece nas conversas, nos documentos de requisitos e, principalmente, no código. Quando o desenvolvedor lê o código e entende imediatamente o que `matricula.adicionarDisciplina()` faz sem precisar consultar nenhuma documentação externa, a Linguagem Ubíqua está funcionando. A ausência dela cria uma camada de tradução invisível que gera bugs por interpretação errada.

Por isso, todo o código deste projeto está em português. Nomes de classes, métodos e variáveis refletem os termos do domínio: `Matricula.criar()`, `buscarPorAlunoId()`, `PeriodoLetivo`, `adicionarDisciplina()`. Isso não é uma convenção cosmética — é uma decisão arquitetural. A Linguagem Ubíqua não vive apenas na documentação; ela vive no código.

## Glossário

| Termo | Definição | BC Dono | Não usar |
|-------|-----------|---------|----------|
| Aluno | Pessoa física matriculável. No BC Matrícula, o que importa é se pode se matricular (está ativo? tem impedimentos?). Referenciado apenas por UUID (alunoId) — o BC Matrícula não carrega os dados completos do aluno. | Matrícula | `StudentEntity`, `Student`, `Usuario` |
| Turma | Oferta de um conjunto de disciplinas em um PeriodoLetivo, com capacidade máxima de vagas. Referenciada por UUID (turmaId) nos demais contextos. | Matrícula | `Class`, `CourseGroup`, `TurmaEntity` |
| Matrícula | Vínculo de um Aluno a um PeriodoLetivo. É o Aggregate Root do sistema — protege as invariantes: limite de disciplinas por matrícula, impossibilidade de disciplinas duplicadas, impossibilidade de adição após cancelamento. | Matrícula | `Enrollment`, `Registration`, `MatriculaEntity` |
| PeriodoLetivo | Par (ano + semestre) que identifica quando uma matrícula ocorre. Value Object: imutável, validado no construtor. Exemplo: 2026-1. | Matrícula | `Semester`, `Term`, `Period` |
| Vaga | Slot disponível em uma Turma. Representa a capacidade restante — quantos alunos ainda podem ser matriculados na turma. | Matrícula | `Slot`, `Seat`, `AvailableSpot` |
| Responsável Financeiro | Pessoa física ou jurídica responsável pelo pagamento das mensalidades do Aluno. Relevante para o BC Financeiro; no BC Matrícula existe apenas como referência (sem dados detalhados). | Financeiro | `Payer`, `Guardian`, `FinancialContact` |

## Conceitos Ambíguos

O mesmo termo pode ter significados — e modelos de dados — completamente diferentes dependendo do Bounded Context em que é usado. Este é um dos insights centrais do DDD: a Linguagem Ubíqua é **local** ao Bounded Context. Dois contextos podem usar a mesma palavra para conceitos diferentes, e isso é intencional e correto, não um problema a ser resolvido com uma classe compartilhada.

### Aluno

| Contexto | O que importa sobre o Aluno | Dados que o BC mantém |
|----------|-----------------------------|-----------------------|
| **Matrícula** | Pode se matricular? Está ativo? Já tem matrícula neste período? | UUID alunoId, status ativo/inativo |
| **Financeiro** | Tem mensalidades em aberto? Possui bolsa de estudos? Qual o contrato vigente? | histórico de pagamentos, contratos, bolsas |
| **Acadêmico** | Está cursando quais disciplinas? Qual é seu histórico de notas e frequência? | disciplinas cursadas, notas, frequência |

### Matrícula

| Contexto | O que importa sobre a Matrícula | Dados que o BC mantém |
|----------|---------------------------------|-----------------------|
| **Matrícula** | Vínculo entre Aluno e PeriodoLetivo; conjunto de disciplinas escolhidas; status ativo/cancelado; invariantes protegidas | `id`, `alunoId`, `turmaId`, `periodoLetivo`, `status`, lista de disciplinas |
| **Financeiro** | Base para criação de contrato financeiro; qual é o valor? por quanto tempo? | referência ao contrato, mensalidades geradas |
| **Acadêmico** | Quais disciplinas o aluno está cursando neste período? | registro de notas e frequência por disciplina |

> **Lição:** "Aluno" no BC Matrícula e "Aluno" no BC Acadêmico são modelos diferentes, mantidos por equipes diferentes, com ciclos de evolução independentes. Compartilhar uma única classe `Aluno` entre os três contextos criaria acoplamento e impediria que cada contexto evoluísse no seu próprio ritmo.
