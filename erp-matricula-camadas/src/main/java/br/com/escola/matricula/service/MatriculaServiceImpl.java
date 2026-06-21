package br.com.escola.matricula.service;

import br.com.escola.matricula.model.Aluno;
import br.com.escola.matricula.model.ItemMatricula;
import br.com.escola.matricula.model.Matricula;
import br.com.escola.matricula.model.Turma;
import br.com.escola.matricula.repository.AlunoRepository;
import br.com.escola.matricula.repository.ItemMatriculaRepository;
import br.com.escola.matricula.repository.MatriculaRepository;
import br.com.escola.matricula.repository.TurmaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/*
 * ANTI-PADRAO: Service Deus (DIAG-03)
 * =====================================
 * Esta classe tem 200+ linhas e cresce sem parar. Toda nova regra de matrícula
 * vem parar aqui. Um Service Deus viola o Princípio da Responsabilidade Única —
 * ele sabe de tudo, faz de tudo, muda por qualquer razão.
 *
 * Relação com DIAG-01: o service está inchado exatamente porque o modelo não tem
 * comportamento — como Matricula.java é anêmica, toda lógica migrou para cá.
 * Se Matricula tivesse adicionarDisciplina(), cancelar() e protegesse suas próprias
 * invariantes, este Service teria apenas 4-5 linhas por método de orquestração.
 *
 * Contraste: erp-matricula-ddd/.../aplicacao/MatricularAlunoUseCase.java tem 109 linhas
 * para o mesmo fluxo — e boa parte são comentários pedagógicos. Os três UseCases DDD
 * (MatricularAluno, AdicionarDisciplina, CancelarMatricula) somados têm menos linhas
 * que esta única classe.
 */
@Service
@Transactional
public class MatriculaServiceImpl implements MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final TurmaRepository turmaRepository;
    private final ItemMatriculaRepository itemMatriculaRepository;

    // Injeção por construtor — sem @Autowired (Spring 4.3+ injeta automaticamente
    // quando há apenas um construtor).
    public MatriculaServiceImpl(MatriculaRepository matriculaRepository,
                                AlunoRepository alunoRepository,
                                TurmaRepository turmaRepository,
                                ItemMatriculaRepository itemMatriculaRepository) {
        this.matriculaRepository = matriculaRepository;
        this.alunoRepository = alunoRepository;
        this.turmaRepository = turmaRepository;
        this.itemMatriculaRepository = itemMatriculaRepository;
    }

    /**
     * Matricula um aluno em uma turma.
     *
     * <p>Este método contém três problemas concentrados em ~35 linhas:
     * validação de aluno (DIAG-01), validação de período (DIAG-01) e
     * construção do objeto Matricula sem nenhuma proteção do próprio objeto.</p>
     */
    @Override
    public UUID matricular(UUID alunoId, UUID turmaId, String periodoInicio, String periodoFim) {

        // Busca o aluno — lança RuntimeException genérica (sem tipo específico).
        // No módulo DDD: MatriculaNaoEncontradaException tipada.
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + alunoId));

        // ANTI-PADRAO: Service Anêmico (DIAG-01)
        // Regra de negócio (aluno inativo não matricula) vivendo no Service, não na entidade Aluno.
        // No módulo DDD: aluno.estaAtivo() encapsula a mesma verificação no próprio objeto.
        // Aqui, se você ler Aluno.java, não encontrará essa regra — precisa ler o Service.
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo não pode ser matriculado");
        }

        // Busca a turma — mesma exceção genérica.
        Turma turma = turmaRepository.findById(turmaId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada: " + turmaId));

        // ANTI-PADRAO: Service Anêmico (DIAG-01)
        // Verificação do período de início no Service. No módulo DDD: turma.periodoEstaAberto()
        // encapsula essa regra dentro da própria entidade Turma.
        // Problema: esta verificação só acontece via matrícula. Se outro fluxo precisar
        // verificar o período, vai reescrever a mesma lógica em outro Service.
        if (periodoInicio == null || periodoInicio.isBlank()) {
            throw new RuntimeException("Período de início é obrigatório");
        }

        // Cria o objeto Matricula com setters — sem validação interna.
        // No módulo DDD, o construtor ou factory method de Matricula garante invariantes.
        Matricula matricula = new Matricula();
        matricula.setId(UUID.randomUUID());
        matricula.setAlunoId(alunoId);
        matricula.setTurmaId(turmaId);
        matricula.setPeriodoInicio(periodoInicio);
        matricula.setPeriodoFim(periodoFim);
        // String "ATIVA" — sem tipo seguro. No módulo DDD é StatusMatricula (sealed interface).
        matricula.setStatus("ATIVA");

        matriculaRepository.insert(matricula);

        return matricula.getId();
    }

    /**
     * Adiciona uma disciplina a uma matrícula ativa.
     *
     * <p>Este método concentra quatro problemas: verificação de status (DIAG-01),
     * duplicação da validação de aluno ativo (DIAG-04), contagem via banco (DIAG-06),
     * e crescimento do Service Deus (DIAG-03).</p>
     */
    @Override
    public void adicionarDisciplina(UUID matriculaId, String nomeDisciplina) {

        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada: " + matriculaId));

        // ANTI-PADRAO: Service Anêmico (DIAG-01)
        // Verificação de status no Service. No módulo DDD: a invariante "cancelada não aceita
        // disciplinas" é protegida dentro de Matricula.adicionarDisciplina(). A entidade
        // se auto-protege — não depende de um Service externo para manter sua consistência.
        if (!"ATIVA".equals(matricula.getStatus())) {
            throw new RuntimeException("Matrícula não está ativa. Status atual: " + matricula.getStatus());
        }

        Aluno aluno = alunoRepository.findById(matricula.getAlunoId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + matricula.getAlunoId()));

        // ANTI-PADRAO: Duplicação de Regras (DIAG-04)
        // Cópia exata da validação em matricular() acima.
        // A mesma validação também existe em DisciplinaServiceImpl.adicionarDisciplinaExtra().
        // Com o tempo, as implementações divergem: uma adiciona exceção para "alunos em período
        // de teste", a outra não é atualizada. A regra existe em três lugares distintos e
        // nenhum deles é o modelo de domínio.
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo não pode adicionar disciplinas");
        }

        // ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
        // A regra "máximo 6 disciplinas" não existe no modelo de domínio.
        // Ela existe como uma query SQL no repositório — COUNT(*) FROM itens_matricula.
        // Se você ler Matricula.java, não encontrará essa regra. Para descobri-la, precisa
        // ler este Service e rastrear até matriculaRepository.countDisciplinas().
        // No módulo DDD: Matricula.adicionarDisciplina() verifica o limite internamente
        // via this.itens.size() — sem consultar o banco, sem depender de repositório.
        int qtd = matriculaRepository.countDisciplinas(matriculaId);
        if (qtd >= 6) {
            throw new RuntimeException("Limite de 6 disciplinas atingido. Quantidade atual: " + qtd);
        }

        // Cria o ItemMatricula com setters — sem validação interna.
        ItemMatricula item = new ItemMatricula();
        item.setId(UUID.randomUUID());
        item.setMatriculaId(matriculaId);
        // String simples — no módulo DDD é NomeDisciplina (Value Object com validação).
        item.setDisciplina(nomeDisciplina);
        item.setAdicionadaEm(LocalDateTime.now());

        itemMatriculaRepository.insert(item);
    }

    /**
     * Cancela uma matrícula ativa.
     */
    @Override
    public void cancelar(UUID matriculaId) {

        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada: " + matriculaId));

        // ANTI-PADRAO: Service Anêmico (DIAG-01)
        // Verificação de status no Service em vez de dentro da entidade Matricula.
        // No módulo DDD: Matricula.cancelar() valida internamente se a matrícula pode ser
        // cancelada, registra o evento MatriculaCancelada e protege a transição de estado.
        // Aqui, a entidade é passiva — o Service faz tudo e a entidade aceita qualquer estado.
        if (!"ATIVA".equals(matricula.getStatus())) {
            throw new RuntimeException("Apenas matrículas ativas podem ser canceladas. Status atual: " + matricula.getStatus());
        }

        matriculaRepository.updateStatus(matriculaId, "CANCELADA");
    }

    /**
     * Retorna todas as matrículas de um aluno.
     */
    @Override
    public List<Matricula> buscarPorAluno(UUID alunoId) {
        // Método simples de consulta — repassa para o repositório.
        // Em um sistema maior, este método cresceria com filtros, ordenação e paginação,
        // contribuindo ainda mais para o Service Deus (DIAG-03).
        return matriculaRepository.findByAlunoId(alunoId);
    }

    /**
     * Verifica elegibilidade do aluno para matricular ou adicionar disciplinas.
     *
     * <p>ANTI-PADRAO: Service Deus (DIAG-03)<br>
     * Este método existe mas está duplicado na prática: tanto matricular() quanto
     * adicionarDisciplina() revalidam as mesmas condições sem usar este método
     * consistentemente. O método foi criado com boa intenção de eliminar duplicação,
     * mas sem um modelo de domínio rico que encapsule as regras, a disciplina de
     * delegar para métodos privados é frágil — cada novo desenvolvedor ignora o método
     * e adiciona a validação inline "por segurança".</p>
     *
     * <p>Contraste: no módulo DDD, a elegibilidade é verificada dentro do próprio objeto
     * {@code Aluno.estaAtivo()} — não há necessidade de um método privado no UseCase
     * porque a entidade se auto-protege.</p>
     *
     * @param alunoId ID do aluno a verificar
     * @throws RuntimeException se o aluno não for encontrado ou estiver inativo
     */
    private void verificarElegibilidade(UUID alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + alunoId));

        // Regra de negócio no Service — mesma regra que já está em matricular()
        // e adicionarDisciplina(). A extração para método privado não eliminou a duplicação
        // porque os outros métodos não o utilizam consistentemente.
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo: " + alunoId);
        }
    }

}
