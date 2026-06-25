package br.com.escola.matricula.dominio.modelo;

import br.com.escola.matricula.dominio.evento.AlunoMatriculado;
import br.com.escola.matricula.dominio.evento.DisciplinaAdicionada;
import br.com.escola.matricula.dominio.evento.MatriculaCancelada;
import br.com.escola.matricula.dominio.excecao.DisciplinaJaMatriculadaException;
import br.com.escola.matricula.dominio.excecao.LimiteDisciplinasExcedidoException;
import br.com.escola.matricula.dominio.excecao.MatriculaCanceladaException;
import br.com.escola.matricula.dominio.vo.NomeDisciplina;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate Root do Bounded Context Matrícula.
 *
 * <p><strong>Responsabilidade central:</strong> toda lógica de negócio de matrícula
 * é encapsulada aqui. Nunca modificar estado externamente — usar apenas os métodos
 * de domínio ({@code adicionarDisciplina()}, {@code cancelar()}).</p>
 *
 * <p><strong>Invariantes protegidas:</strong>
 * <ol>
 *   <li><em>Limite de disciplinas:</em> máximo {@code LIMITE_DISCIPLINAS} (6) por matrícula</li>
 *   <li><em>Sem duplicidade:</em> a mesma disciplina não pode aparecer duas vezes</li>
 *   <li><em>Estado terminal:</em> matrícula cancelada não recebe novas disciplinas</li>
 * </ol>
 * Todas as verificações acontecem dentro do método — sem janela de concorrência entre
 * "verificar" e "modificar". Ver docs/02-design-tatico/agregados.md.</p>
 *
 * <p><strong>Domain Events sem Spring:</strong> o Aggregate coleta eventos em
 * {@code List<Object> eventos}. O UseCase publica via {@code ApplicationEventPublisher}
 * após {@code repositorio.salvar()} — nunca antes. Ver D-09 e D-10 em 03-CONTEXT.md.</p>
 *
 * <p><strong>Zero dependências de framework:</strong> este arquivo não importa nada de
 * {@code org.springframework.*}, {@code org.mybatis.*} ou {@code jakarta.*}.
 * Verificável com: {@code grep "import org.springframework" Matricula.java}.</p>
 */
public class Matricula {

    /**
     * Limite máximo de disciplinas por matrícula — configurável pela instituição.
     * Definido como constante nomeada para deixar a intenção explícita no código.
     */
    private static final int LIMITE_DISCIPLINAS = 6;

    // --- Campos imutáveis (identidade e contexto da matrícula) ---

    /** Identificador único desta matrícula. */
    private final UUID id;

    /** UUID do Aluno matriculado — referência por ID sem carregar o Aggregate completo. */
    private final UUID alunoId;

    /** UUID da Turma — referência por ID sem carregar o Aggregate completo. */
    private final UUID turmaId;

    /** Período letivo desta matrícula. */
    private final PeriodoLetivo periodoLetivo;

    // --- Campos mutáveis (estado que evolui durante o ciclo de vida) ---

    /** Estado atual da matrícula — pode transicionar Ativa → Cancelada ou Concluida. */
    private StatusMatricula status;

    /** Disciplinas incluídas nesta matrícula — gerenciadas pelos métodos de domínio. */
    private final List<ItemMatricula> disciplinas;

    /**
     * Eventos de domínio coletados durante operações — publicados pelo UseCase após salvar.
     *
     * <p>Sem interface base, sem import de Spring — apenas {@code List<Object>}.
     * {@code ApplicationEventPublisher.publishEvent(Object)} aceita qualquer tipo.
     * Decisão D-11: simples e pedagógico, sem abstração prematura.</p>
     */
    private final List<Object> eventos;

    // =========================================================================
    // Construtores
    // =========================================================================

    /**
     * Construtor de criação — instancia uma nova matrícula para um aluno em uma turma.
     *
     * <p>Uso: {@code Matricula.criar(alunoId, turmaId, periodoLetivo)}</p>
     *
     * <p>Gera automaticamente:
     * <ul>
     *   <li>Um novo {@code MatriculaId} via {@code UUID.randomUUID()}</li>
     *   <li>Status inicial {@code Ativa}</li>
     *   <li>Lista vazia de disciplinas</li>
     *   <li>Evento {@code AlunoMatriculado} coletado</li>
     * </ul>
     * </p>
     */
    private Matricula(UUID alunoId, UUID turmaId, PeriodoLetivo periodoLetivo) {
        this.id = UUID.randomUUID();
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.periodoLetivo = periodoLetivo;
        this.status = new StatusMatricula.Ativa();
        this.disciplinas = new ArrayList<>();
        this.eventos = new ArrayList<>();

        // Evento de criação coletado imediatamente — publicado pelo UseCase após salvar
        this.eventos.add(new AlunoMatriculado(this.id, this.alunoId, this.turmaId,
            this.periodoLetivo, LocalDateTime.now()));
    }

    /**
     * Construtor de reconstituição — reconstrói uma Matrícula a partir de dados persistidos.
     *
     * <p>Usado exclusivamente pelo {@code MatriculaRowMapper} na camada de infraestrutura
     * para reconstruir o Aggregate a partir do banco de dados.</p>
     *
     * <p><strong>CRÍTICO:</strong> {@code this.eventos = new ArrayList<>()} é OBRIGATÓRIO.
     * Sem isso, {@code adicionarDisciplina()} lança {@code NullPointerException} ao tentar
     * {@code eventos.add()} — o campo seria null após a deserialização.
     * Ver RESEARCH.md Pitfall 9.</p>
     *
     * @param id           identificador da matrícula existente
     * @param alunoId      referência ao aluno
     * @param turmaId      referência à turma
     * @param periodoLetivo período letivo
     * @param status       status atual (pode ser Ativa, Cancelada, Concluida)
     * @param disciplinas  disciplinas já incluídas (cópia defensiva é feita internamente)
     */
    public Matricula(UUID id, UUID alunoId, UUID turmaId,
                     PeriodoLetivo periodoLetivo, StatusMatricula status,
                     List<ItemMatricula> disciplinas) {
        this.id = id;
        this.alunoId = alunoId;
        this.turmaId = turmaId;
        this.periodoLetivo = periodoLetivo;
        this.status = status;
        this.disciplinas = new ArrayList<>(disciplinas); // cópia defensiva
        this.eventos = new ArrayList<>(); // sempre inicializado — sem isso, NPE em adicionarDisciplina()
    }

    /**
     * Factory method para criar uma nova matrícula.
     *
     * <p>Usar este método estático em vez do construtor direto — mantém a API
     * do Aggregate expressiva e consistente com a Linguagem Ubíqua.</p>
     *
     * @param alunoId       identificador do aluno a ser matriculado
     * @param turmaId       identificador da turma
     * @param periodoLetivo período letivo da matrícula
     * @return nova instância de {@code Matricula} no estado {@code Ativa}
     */
    public static Matricula criar(UUID alunoId, UUID turmaId, PeriodoLetivo periodoLetivo) {
        // REFD-02: No módulo camadas (MatriculaServiceImpl.matricular()), criação era:
        // new Matricula(); matricula.setStatus("ATIVA"); — dois passos separáveis via setter público.
        // Aqui, criar() garante status Ativa como parte da construção — sem setter público possível.
        return new Matricula(alunoId, turmaId, periodoLetivo);
    }

    // =========================================================================
    // Métodos de domínio — encapsulam invariantes
    // =========================================================================

    /**
     * Adiciona uma disciplina à matrícula, aplicando as três invariantes do Aggregate.
     *
     * <p><strong>Guards aplicados em ordem:</strong>
     * <ol>
     *   <li><em>Estado:</em> matrícula cancelada não aceita novas disciplinas</li>
     *   <li><em>Limite:</em> máximo {@code LIMITE_DISCIPLINAS} disciplinas por matrícula</li>
     *   <li><em>Duplicidade:</em> a mesma disciplina não pode aparecer duas vezes</li>
     * </ol>
     * As três verificações e a adição acontecem no mesmo método — sem janela de
     * concorrência entre "verificar" e "modificar".</p>
     *
     * <p>Coleta um evento {@code DisciplinaAdicionada} para publicação pelo UseCase.</p>
     *
     * @param disciplina nome da disciplina a ser adicionada
     * @throws MatriculaCanceladaException      se a matrícula está cancelada
     * @throws LimiteDisciplinasExcedidoException se o limite de disciplinas foi atingido
     * @throws DisciplinaJaMatriculadaException  se a disciplina já está na matrícula
     */
    public void adicionarDisciplina(NomeDisciplina disciplina) {
        // Guard 1: estado — matrícula cancelada não aceita novas disciplinas
        // REFD-02: No módulo camadas, esta verificação ficava em MatriculaServiceImpl.adicionarDisciplina():
        // if (!"ATIVA".equals(matricula.getStatus())) — executada pelo Service, pulável por código externo.
        // Aqui, o próprio Aggregate verifica seu estado — nenhum Service pode pular esta invariante.
        if (this.status instanceof StatusMatricula.Cancelada) {
            throw new MatriculaCanceladaException(this.id);
        }

        // Guard 2: limite — máximo de LIMITE_DISCIPLINAS disciplinas por matrícula
        // REFD-02: No módulo camadas, a verificação usava itemMatriculaRepository.countByMatriculaId(matriculaId)
        // em MatriculaServiceImpl.adicionarDisciplina() — uma query SQL separada da adição.
        // Aqui, a contagem é em memória (this.disciplinas.size()), atômica com a adição.
        if (this.disciplinas.size() >= LIMITE_DISCIPLINAS) {
            throw new LimiteDisciplinasExcedidoException(LIMITE_DISCIPLINAS, this.disciplinas.size(), this.id);
        }

        // Guard 3: duplicidade — a mesma disciplina não pode aparecer duas vezes
        // REFD-02: No módulo camadas (MatriculaServiceImpl), não havia verificação de duplicidade —
        // era possível adicionar a mesma disciplina duas vezes sem erro. Aqui, o Aggregate
        // protege esta invariante sem precisar de query SQL adicional.
        boolean jaMatriculada = this.disciplinas.stream()
            .anyMatch(item -> item.disciplina().equals(disciplina));
        if (jaMatriculada) {
            throw new DisciplinaJaMatriculadaException(disciplina, this.id);
        }

        this.disciplinas.add(new ItemMatricula(disciplina));
        this.eventos.add(new DisciplinaAdicionada(this.id, this.alunoId, disciplina, LocalDateTime.now()));
    }

    /**
     * Cancela a matrícula, transitando para o estado {@code Cancelada}.
     *
     * <p>Cancelamento é um estado terminal — após cancelar, a matrícula existe
     * apenas como registro histórico. Nenhuma modificação posterior é permitida.</p>
     *
     * <p>Coleta um evento {@code MatriculaCancelada} para publicação pelo UseCase.</p>
     *
     * @throws MatriculaCanceladaException se a matrícula já está cancelada
     */
    public void cancelar() {
        if (this.status instanceof StatusMatricula.Cancelada) {
            throw new MatriculaCanceladaException(this.id);
        }

        LocalDateTime agora = LocalDateTime.now();
        this.status = new StatusMatricula.Cancelada(agora);
        this.eventos.add(new MatriculaCancelada(this.id, this.alunoId, this.periodoLetivo, agora));
    }

    /**
     * Coleta os eventos de domínio acumulados para publicação pelo UseCase.
     *
     * <p><strong>Contrato:</strong>
     * <ul>
     *   <li>Retorna uma cópia imutável dos eventos coletados ({@code List.copyOf})</li>
     *   <li>Limpa a lista interna após copiar</li>
     *   <li>Chamado pelo UseCase APÓS {@code repositorio.salvar()} — per D-10</li>
     * </ul>
     * </p>
     *
     * <p>Exemplo de uso no UseCase:
     * <pre>{@code
     * repositorio.salvar(matricula);                           // (3) persistir
     * matricula.coletarEventos().forEach(pub::publishEvent);   // (4) publicar
     * }</pre>
     * </p>
     *
     * @return lista imutável dos eventos coletados; lista interna é limpa
     */
    public List<Object> coletarEventos() {
        List<Object> copia = List.copyOf(this.eventos);
        this.eventos.clear();
        return copia;
    }

    // =========================================================================
    // Getters — sem setters públicos (encapsulamento do Aggregate)
    // =========================================================================

    /** Retorna o identificador único desta matrícula. */
    public UUID getId() {
        return id;
    }

    /** Retorna o UUID do aluno matriculado. */
    public UUID getAlunoId() {
        return alunoId;
    }

    /** Retorna o UUID da turma. */
    public UUID getTurmaId() {
        return turmaId;
    }

    /** Retorna o período letivo desta matrícula. */
    public PeriodoLetivo getPeriodoLetivo() {
        return periodoLetivo;
    }

    /** Retorna o status atual da matrícula. */
    public StatusMatricula getStatus() {
        return status;
    }

    /**
     * Retorna uma cópia imutável da lista de disciplinas.
     *
     * <p>Retorna {@code List.copyOf} para prevenir modificação externa da lista interna —
     * parte da proteção T-03-02 (Threat Register: tampering via getter).</p>
     */
    public List<ItemMatricula> getDisciplinas() {
        // REFD-02 (DDD-04): Sem setter público e retornando List.copyOf, código externo não pode
        // modificar a lista interna do Aggregate. No módulo camadas, Matricula expunha setters e
        // listas mutáveis — invariantes dependiam de disciplina dos chamadores, não do próprio objeto.
        return List.copyOf(disciplinas);
    }
}
