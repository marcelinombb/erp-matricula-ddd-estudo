# Guia de Consulta: Conceito DDD → Arquivo

Consulte esta tabela quando quiser ver um conceito DDD em código real.

| Conceito DDD | Arquivo | O que observar |
|---|---|---|
| Aggregate Root | [Matricula.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/modelo/Matricula.java) | Métodos adicionarDisciplina() e cancelar() encapsulam invariantes — nenhuma regra de negócio fora daqui |
| Value Object (tipado) | [AlunoId.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/vo/AlunoId.java) | Record Java 21 com validação no construtor compacto |
| Value Object (com lógica) | [Cpf.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/vo/Cpf.java) | Validação de dígito verificador embutida — substitui String primitivo |
| Value Object (período) | [PeriodoLetivo.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/vo/PeriodoLetivo.java) | Abstração de (ano, semestre) — sem acoplamento a datas do calendário |
| Entidade | [Aluno.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/modelo/Aluno.java) | equals/hashCode por identidade (AlunoId), não por atributos |
| Estado (Sealed Interface) | [StatusMatricula.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/modelo/StatusMatricula.java) | Pattern matching exaustivo sem default — compilador verifica todos os estados |
| Domain Event | [AlunoMatriculado.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/evento/AlunoMatriculado.java) | Record imutável com dados do fato — publicado após persistência |
| Interface de Repositório | [MatriculaRepositorio.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/repositorio/MatriculaRepositorio.java) | Zero imports de framework — pertence ao domínio, não à infraestrutura |
| Domain Service | [VerificadorElegibilidadeMatricula.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/servico/VerificadorElegibilidadeMatricula.java) | Lógica que cruza múltiplos Aggregates (Aluno, Turma, Matricula) |
| Exceção tipada | [LimiteDisciplinasExcedidoException.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/dominio/excecao/LimiteDisciplinasExcedidoException.java) | Campos getLimite() e getAtual() — dados estruturados no erro |
| Application Service (UseCase) | [MatricularAlunoUseCase.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoUseCase.java) | Orquestra sem decidir — Javadoc explica cada passo da sequência |
| Command | [MatricularAlunoCommand.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatricularAlunoCommand.java) | Intenção de escrita em objeto imutável — sem ambiguidade de parâmetros |
| DTO de leitura | [MatriculaDto.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/aplicacao/MatriculaDto.java) | Factory method de(Matricula) — centraliza conversão Aggregate → DTO |
| Implementação do Repositório | [MatriculaRepositorioMyBatis.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRepositorioMyBatis.java) | Inversão de Dependência: domínio define o contrato, infraestrutura implementa |
| TypeHandler MyBatis | [CpfTypeHandler.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/typehandler/CpfTypeHandler.java) | Conversão Cpf ↔ VARCHAR(11) — sem anotações no Value Object |
| RowMapper (fronteira domínio/persistência) | [MatriculaRowMapper.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRowMapper.java) | ÚNICO arquivo que conhece MatriculaRow E Matricula — ponto pedagógico central |
| Modelo relacional (Row) | [MatriculaRow.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/persistencia/MatriculaRow.java) | Espelho plano do banco — sem comportamento, sem anotações de domínio |
| Domain Event Listener (stub) | [FinanceiroEventListener.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/infraestrutura/eventos/FinanceiroEventListener.java) | @TransactionalEventListener — executado APÓS o commit, nunca antes |
| Controller REST | [MatriculaController.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/MatriculaController.java) | Traduz HTTP → Command; zero lógica de negócio |
| Handler de Exceções | [ExcecaoHandler.java](../erp-matricula-app/src/main/java/br/com/escola/matricula/interfaces/ExcecaoHandler.java) | Única fronteira onde exceções de domínio se tornam respostas HTTP |

Para o fluxo completo de "Realizar Matrícula" em sequence diagram, consulte [docs/02-design-tatico/modelagem.md](../02-design-tatico/modelagem.md).
