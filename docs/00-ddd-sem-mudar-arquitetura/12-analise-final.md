# Análise Final — Quais Benefícios Obtivemos Aplicando DDD sem Alterar a Arquitetura?

Se você chegou até aqui, leu os seis anti-padrões do módulo camadas (docs 01–06), acompanhou a refatoração DDD passo a passo (docs 07–11) e praticou com o exercício de classificação. Agora é hora de avaliar o resultado: o investimento valeu? Este documento responde com os dados do próprio projeto — sem marketing de DDD. Os custos são tão reais quanto os benefícios.

---

## Resumo

| Aspecto | Avaliação qualitativa | Evidência no projeto | Para saber mais |
|---------|----------------------|---------------------|-----------------|
| Complexidade introduzida | Alta no início — concentrada na construção do modelo de domínio; estabiliza depois | 42 arquivos no módulo DDD vs 18 no módulo camadas; 3.514 LOC Java; mapeamento explícito em 4 lugares por campo | [licoes-aprendidas.md §2](../04-material-didatico/licoes-aprendidas.md) |
| Benefícios obtidos | Tangíveis e verificáveis — invariantes protegidas, exceções com dados, separação cirúrgica | 3 invariantes de `Matricula` verificadas atomicamente em `adicionarDisciplina()`; `getLimite()` e `getAtual()` sem parsear String | [10-agregados.md](10-agregados.md), [ddd-vs-camadas.md](../04-material-didatico/ddd-vs-camadas.md) |
| Curva de aprendizado | Gradual — os 5 conceitos chegam em sequência sem exigir nova stack | 5 conceitos DDD introduzidos nos docs 07–11; exercício de classificação com 10 regras reais do projeto | [exercicio-classificacao.md](exercicio-classificacao.md) |
| Facilidade de adoção | Alta para equipes Spring Boot — a stack não muda | 6 anti-padrões diagnosticáveis em código Spring Boot existente; Controller → Service → Repository permanece | [guia-leitura-comparativo.md](guia-leitura-comparativo.md) |

---

## Complexidade Introduzida

Observamos que a complexidade do módulo DDD é maior em termos brutos: 42 arquivos versus 18 no módulo camadas, e 3.514 LOC Java contra um número menor no módulo tradicional. Esse custo é real e não deve ser minimizado.

O custo mais concreto foi o mapeamento MyBatis. Cada campo do banco precisa aparecer em quatro lugares: um alias no SQL, um `<result>` no ResultMap, um campo na classe Row e um getter no RowMapper. Com o tamanho deste projeto, isso é gerenciável. Em um domínio com 20 entidades e 30 campos cada, o volume cresce linearmente e a manutenção fica pesada sem geração de código.

A ausência de Lombok também tem preço. Java 21 records eliminaram o problema para Value Objects e Commands, mas Entidades como `Aluno` ainda requerem construtores, getters e `equals`/`hashCode` escritos à mão — porque identidade por ID não é igualdade estrutural. O boilerplate existe por uma razão pedagógica boa, mas o custo de tempo real também existe.

Descobrimos ainda que a fronteira do Aggregate é uma das decisões mais difíceis de acertar de primeira. A pergunta "vagas disponíveis na turma pertencem ao Aggregate Matrícula ou Turma?" não tem resposta óbvia e exige análise de invariantes, transações e consistência antes de ser respondida. O campo `vagasMaximas` existe em `Turma` mas não é verificado no Aggregate — uma decisão deliberada para v1, documentada como tech debt.

Para a análise completa dos custos operacionais, ver [licoes-aprendidas.md §2](../04-material-didatico/licoes-aprendidas.md).

---

## Benefícios Obtidos

### Invariantes sempre protegidas — sem janela de concorrência

`Matricula.adicionarDisciplina()` é o único ponto no projeto onde as três regras de negócio são verificadas: matrícula não cancelada, disciplinas abaixo do limite, sem duplicidade. As três verificações e a adição são o mesmo método, no mesmo objeto. Um Service externo não pode pular nenhum dos guards — a lista interna `disciplinas` não tem acesso público.

No módulo camadas, o `MatriculaServiceImpl.adicionarDisciplina()` faz a verificação de limite com uma query SQL separada da adição. Dois usuários simultâneos podem cada um chamar `countByMatriculaId`, obter 5, passar pela verificação, e ambos adicionarem — resultado: 7 disciplinas. Com o Aggregate, isso é estruturalmente impossível.

Ver: [10-agregados.md](10-agregados.md) — Guards 1, 2 e 3 do método `adicionarDisciplina()`.

### Exceções tipadas — dados chegam ao HTTP sem parsing

`LimiteDisciplinasExcedidoException` carrega `getLimite()` e `getAtual()` como campos tipados. O `ExcecaoHandler` usa esses dados diretamente para construir um response 422 com payload estruturado (`"limite": 6, "atual": 6`). Com `RuntimeException("Limite excedido")`, o handler precisaria parsear a String de mensagem para extrair esses valores — frágil e acoplado ao formato do texto.

Ver: [ddd-vs-camadas.md §5](../04-material-didatico/ddd-vs-camadas.md) — comparativo `RuntimeException` genérica vs. exceção tipada com dados estruturados.

### Separação domínio/persistência — mudança cirúrgica no banco

`MatriculaRowMapper.java` é o único arquivo que conhece tanto `MatriculaRow` (modelo relacional) quanto `Matricula` (modelo de domínio). Adicionar um campo ao banco exige mudança em apenas três lugares: `MatriculaRow` (o campo), `MatriculaRowMapper` (a conversão) e o XML MyBatis (o alias no SELECT). `Matricula.java` não é tocado.

Ver: [11-repositorios.md](11-repositorios.md) — contraste `@Mapper` com anotações de infraestrutura vs. interface pura no domínio.

### Domain Events sem acoplamento entre Bounded Contexts

`Matricula.java` coleta eventos em uma `List<Object>` simples — sem import de Spring, sem interface de evento específica. `FinanceiroEventListener` e `AcademicoEventListener` recebem `AlunoMatriculado` via `@TransactionalEventListener` sem que `Matricula.java` saiba da existência de nenhum deles. A integração entre contextos existe sem acoplamento estrutural.

Ver: [licoes-aprendidas.md §1](../04-material-didatico/licoes-aprendidas.md) — análise completa dos quatro benefícios observados em produção.

---

## Curva de Aprendizado

A curva existe, mas é estruturada. Os cinco conceitos DDD foram introduzidos neste módulo em sequência deliberada, sem exigir mudança de stack:

1. **Linguagem Ubíqua** — nomes do código alinhados com o negócio (doc 07)
2. **Entidades** — identidade e ciclo de vida, não apenas dados (doc 08)
3. **Value Objects** — imutabilidade e igualdade por valor (doc 09)
4. **Agregados** — limites de consistência e invariantes (doc 10)
5. **Repositórios** — interface no domínio, implementação na infraestrutura (doc 11)

Cada conceito parte do que um desenvolvedor Spring Boot já conhece: `@Entity` → Aggregate sem anotações, `@Repository extends JpaRepository` → interface de domínio implementada pelo MyBatis Mapper. O paralelo sempre existe.

O exercício de classificação ([exercicio-classificacao.md](exercicio-classificacao.md)) serviu como ativação do aprendizado: 10 regras reais do projeto, cada uma com a pergunta "domínio ou aplicação?". Resolver o exercício sem consultar o gabarito é o teste real de compreensão — não basta ler, é preciso classificar com critério.

A progressão dos 12 documentos foi desenhada como trilha: os docs 01–06 criam o incômodo ("esse Service está crescendo demais"), os docs 07–11 apresentam a solução conceito por conceito, e este doc 12 fecha o ciclo com a pergunta estratégica.

---

## Facilidade de Adoção pela Equipe

O ponto de partida familiar é a maior vantagem desta abordagem: Controller → Service → Repository não desaparece. O `MatricularAlunoUseCase` ainda é injetado no Controller, ainda recebe um DTO da requisição, ainda persiste via repositório. A estrutura que a equipe conhece permanece — o que muda é o que vive dentro do Service.

Os seis anti-padrões diagnosticados no módulo camadas ([00-introducao.md](00-introducao.md)) são reconhecíveis por qualquer desenvolvedor que trabalhou em projetos Spring Boot de porte médio: o Service que começa com 50 linhas e chega a 227, a entidade que tem apenas getters e setters, a validação que aparece no Controller e no Service ao mesmo tempo. Não é necessário convencer a equipe de que o problema existe — o código-exemplo demonstra.

A mesma stack (Spring Boot 3.x, MyBatis, PostgreSQL, Docker) elimina a barreira de adoção técnica. Não há novo framework para aprender, nenhuma dependência nova no `pom.xml`, nenhuma mudança na pipeline de CI. Para uma equipe que já opera com essa stack, a proposta é: "mesmos ingredientes, nova receita."

Ver [guia-leitura-comparativo.md](guia-leitura-comparativo.md) para a transformação completa de uma operação — do `MatriculaServiceImpl.java` com 227 linhas ao `MatricularAlunoUseCase` orquestrador com responsabilidade única.

---

## Quando Vale a Pena

### Aplique quando

- **Se você reconhece que a mesma validação aparece em mais de um Service**: a regra de período letivo aberto está em `MatriculaService.matricular()` e em `DisciplinaService.adicionarDisciplina()`. Esse é o sinal mais claro de que a regra pertence ao domínio, não ao Service. DDD sem mudar a arquitetura resolve isso: a regra vai para o Aggregate ou Domain Service e deixa de ser duplicada.

- **Se sua equipe domina Spring Boot e pode absorver 5–7 conceitos novos gradualmente**: a curva de aprendizado é real, mas é estruturada. Se a equipe entende `@Transactional`, entende por que a transação deve terminar antes dos listeners de evento. Se entende `@Component`, entende por que o Domain Service não tem `@Service`. O vocabulário Spring já existe — o vocabulário DDD se adiciona a ele.

- **Se você tem um God Service crescendo sem parar**: `MatriculaServiceImpl.java` tem 227 linhas e ainda cresce a cada nova funcionalidade. Quando um Service chega nesse ponto, a próxima feature vai em qual método? Com DDD, a resposta é determinística: a regra de negócio vai para o Aggregate ou Domain Service, a orquestração vai para o UseCase. O crescimento linear do Service para.

### Considere adiar quando

- **Se o prazo não permite o ciclo completo de aprendizado**: não existe "DDD parcial bem-feito". Um Aggregate sem invariantes é apenas uma classe com um nome diferente. Uma interface de repositório que expõe `countByMatriculaId` em vez de `existeMatriculaAtiva` é um DAO com outro nome. Aplicar os padrões sem entender os motivos produz complexidade sem benefício — pior do que o código original.

- **Se o domínio é CRUD puro sem invariantes complexas**: um cadastro de usuários com campos opcionais e sem regras de transição de estado não tem invariantes para proteger. O Aggregate adicionaria camadas de estrutura sem nada para verificar. O custo de 42 arquivos em vez de 18 só se paga quando existe comportamento de domínio real para encapsular.

- **Se a equipe nunca leu DDD antes desta exposição**: a curva de "o que é Aggregate" e "o que vai no domínio vs. no UseCase" consome tempo real — em média, vários dias de discussão e revisão de design antes de render valor. Uma equipe sem nenhuma exposição prévia vai lutar com a fronteira Aggregate/Service em cada feature nova. Se o contexto é prazo curto e time sem experiência prévia, investir em documentação de code review e conventions dentro da arquitetura em camadas pode entregar mais no curto prazo.

---

## Próximo passo

Para quem quer aprofundar a análise operacional — o que funcionou, o que custou mais, o que faríamos diferente em produção — o ponto de referência é [licoes-aprendidas.md](../04-material-didatico/licoes-aprendidas.md). Para quem prefere o comparativo técnico lado a lado — de `@Service` para UseCase, de `@Entity` para Aggregate Root sem anotações, de `RuntimeException` para exceção tipada — o ponto de referência é [ddd-vs-camadas.md](../04-material-didatico/ddd-vs-camadas.md). Para quem quer validar o próprio entendimento antes de sair, o exercício de classificação ([exercicio-classificacao.md](exercicio-classificacao.md)) ainda está disponível.
