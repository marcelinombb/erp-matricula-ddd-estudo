# Bounded Contexts — Matrícula Escolar

Este documento cobre dois níveis de análise estratégica: a classificação dos **subdomínios** do negócio (Core, Supporting, Generic) e a definição dos **Bounded Contexts** que implementam cada um desses subdomínios.

---

## Subdomínio vs Bounded Context

Estes dois conceitos descrevem perspectivas diferentes sobre o mesmo sistema — e confundi-los é um dos erros mais comuns em projetos DDD.

**Subdomínio** é uma partição do **problema de negócio**: o "o quê". Matrícula é um subdomínio porque "vincular alunos a turmas em um período letivo" é uma área de responsabilidade do negócio. O subdomínio existe independentemente de qualquer software.

**Bounded Context** é uma partição da **solução de software**: o "como". O BC Matrícula é o software que implementa as regras do subdomínio Matrícula — com seu modelo de domínio próprio, sua linguagem própria e seus dados próprios.

Neste projeto, o mapeamento é **1:1**: cada subdomínio tem um Bounded Context correspondente. Isso é comum em projetos menores, mas não é uma regra do DDD — em sistemas maiores, um subdomínio pode ser implementado por múltiplos Bounded Contexts, ou um único BC pode cobrir mais de um subdomínio menor. O que importa é reconhecer que o subdomínio é o problema e o BC é a solução.

---

## Classificação de Subdomínios

| Subdomínio | Tipo | Justificativa |
|------------|------|---------------|
| **Matrícula** | Core Domain | É a operação central da instituição. Sem matrícula, não existe aluno ativo, não existe cobrança, não existe vínculo acadêmico. As regras de matrícula são o diferencial competitivo — nenhum sistema genérico as captura com a mesma fidelidade. É aqui que a instituição se diferencia de seus concorrentes e onde o investimento em qualidade de software tem maior retorno. |
| **Financeiro** | Supporting Domain | Importante para o funcionamento, mas não é o diferencial da instituição. As regras de cobrança e contrato existem em outros tipos de negócio. Poderia ser delegado a um sistema de terceiros (ex: ERP financeiro) sem perda de identidade institucional — a instituição continuaria sendo reconhecida pelo processo de matrícula, não pelo sistema de cobrança. |
| **Acadêmico** | Supporting Domain | Gestão de notas, frequência e histórico é essencial para o funcionamento, mas não é onde a instituição se diferencia. Sistemas de gestão acadêmica existem prontos no mercado (ex: SIGAA, Moodle). O valor não está em construir esse sistema, mas em integrá-lo corretamente com o processo de matrícula. |
| **Secretaria** | Supporting Domain | Processos administrativos de suporte (documentação, comunicados, agendamentos). Amplamente genérico — pode ser terceirizado ou adquirido como produto pronto sem impacto na identidade da instituição. Existe como contexto separado porque tem suas próprias regras administrativas, mas não é fonte de vantagem competitiva. |
| **Autenticação** | Generic Domain | Problema completamente resolvido pelo mercado (OAuth2, LDAP, Keycloak, Spring Security). Construir internamente é desperdício do tempo de engenharia que deveria estar concentrado no Core Domain. A autenticação não tem regras específicas da instituição — qualquer implementação padrão atende. |
| **Notificações/E-mail** | Generic Domain | Infraestrutura de comunicação sem regras de negócio específicas da instituição. Delegado a serviços como SendGrid, AWS SES ou similares. O conteúdo de um e-mail pode ser específico do domínio, mas o mecanismo de envio é completamente genérico. |

---

## Bounded Contexts

### BC Matrícula (Core Domain — Implementado)

**Responsabilidade:** O BC Matrícula é o único responsável por criar, modificar e cancelar matrículas. Protege as invariantes do vínculo aluno-turma-período. Nenhum outro contexto pode modificar uma matrícula diretamente — a modificação sempre passa pelas operações expostas por este contexto.

**Limites:** Qualquer regra sobre "o que é uma matrícula válida" vive aqui. Isso inclui:
- Se um aluno pode se matricular (está ativo? já tem matrícula neste período?)
- Se uma turma aceita mais alunos (tem vagas disponíveis?)
- Se o período letivo permite novas matrículas (está aberto?)
- O que pode acontecer após o cancelamento (nada — matrícula cancelada é terminal)

Outros contextos **não podem** consultar as tabelas de Matrícula diretamente nem chamar métodos internos de suas entidades. A interface pública do BC Matrícula são seus eventos de domínio.

**Linguagem própria:**
- **Aluno** — neste contexto: representado apenas por `AlunoId`. O que importa é a elegibilidade (ativo? sem impedimentos?). Dados completos do aluno ficam em outro contexto.
- **Turma** — referenciada por `TurmaId`. Relevante pela sua capacidade (vagas disponíveis) e vínculo com o período letivo.
- **Matrícula** — o Aggregate Root. Vínculo de um Aluno a um PeriodoLetivo, com zero ou mais disciplinas escolhidas.
- **PeriodoLetivo** — Value Object imutável: par (ano, semestre). Ex: `2026-1`. Determina quando a matrícula ocorre e se ainda pode ser modificada.
- **Vaga** — disponibilidade restante em uma Turma. A Turma tem capacidade máxima; Vaga representa o espaço não ocupado.
- **StatusMatricula** — ciclo de vida: `ATIVA` → `CANCELADA`. Matrícula cancelada não pode receber novas disciplinas.

**Dados próprios:** Tabelas `matricula`, `matricula_disciplina` e referência a `turma`. Não compartilha tabelas com outros Bounded Contexts — cada contexto é dono de seus dados.

**Regras-chave protegidas pelo Aggregate:**
- Máximo de N disciplinas por matrícula (invariante de capacidade)
- Sem disciplina duplicada na mesma matrícula
- Sem adição de disciplina após cancelamento
- Aluno deve estar ativo no momento da matrícula
- Período letivo deve estar aberto para novas matrículas

---

### BC Financeiro (Supporting — Stub no v1)

**Responsabilidade:** Gestão da relação financeira com o aluno — contratos, mensalidades, inadimplência, bolsas. Define o que é um "aluno devedor", o que é uma "bolsa de estudos" e quais ações tomar em caso de inadimplência. Esses conceitos não existem no BC Matrícula.

**O que consome de Matrícula:**
- `AlunoMatriculado` → cria um contrato financeiro para o aluno no período
- `MatriculaCancelada` → cancela ou suspende o contrato financeiro correspondente

**Status no v1:** Implementado como listener stub (`@TransactionalEventListener`) que demonstra o contrato sem implementar a lógica financeira real. O stub serve como prova de que a integração existe e que o evento chegou ao destino correto.

---

### BC Acadêmico (Supporting — Stub no v1)

**Responsabilidade:** Gestão do percurso acadêmico do aluno — notas, frequência, histórico, vínculos com disciplinas. Define o que significa "cursar uma disciplina", como o histórico acadêmico é construído e quais são os critérios de aprovação.

**O que consome de Matrícula:**
- `AlunoMatriculado` → cria o vínculo acadêmico do aluno para o período letivo
- `DisciplinaAdicionada` → reserva a vaga do aluno na disciplina e cria o registro de frequência/nota
- `MatriculaCancelada` → libera as vagas nas disciplinas e encerra os vínculos acadêmicos do período

**Status no v1:** Implementado como listener stub demonstrando os três contratos de evento. O aluno vê que o Acadêmico "sabe" sobre a matrícula através dos eventos, mas a lógica de notas e frequência não é implementada.

---

### BC Secretaria (Supporting — Isolado no v1)

**Responsabilidade:** Processos administrativos da instituição — emissão de documentos, comunicados, agendamentos de atendimento, registros de protocolo. A Secretaria é o contexto responsável pela "burocracia" institucional que não se enquadra nos demais contextos.

**Por que não integra com Matrícula no v1:** A Secretaria existe como subdomínio separado, mas não há eventos de Matrícula que gerem ações de Secretaria no escopo atual do projeto. Uma integração futura (exemplo: `AlunoMatriculado` gerando emissão automática de declaração de matrícula) faria sentido operacional, mas seria escopo de v2. A ausência de conexão no diagrama e no código é intencional — não uma omissão. Ver [Context Map](context-map.md#secretaria-no-v1) para detalhes.

---

## O Aluno em cada contexto

O conceito de Aluno é o exemplo mais poderoso de por que Bounded Contexts existem. Cada contexto mantém seu próprio modelo de Aluno, com os dados que importam para suas responsabilidades — e não mais que isso. Um modelo único e compartilhado de Aluno forçaria todos os contextos a evoluir juntos, criando acoplamento e dificultando a manutenção independente.

Ver [Linguagem Ubíqua — Conceitos Ambíguos](linguagem-ubiqua.md#conceitos-ambíguos) para a tabela completa mostrando como Aluno, Matrícula e outros conceitos mudam de significado conforme o contexto.
