package br.com.escola.matricula.service;

import br.com.escola.matricula.model.Aluno;
import br.com.escola.matricula.model.ItemMatricula;
import br.com.escola.matricula.model.Matricula;
import br.com.escola.matricula.repository.AlunoRepository;
import br.com.escola.matricula.repository.ItemMatriculaRepository;
import br.com.escola.matricula.repository.MatriculaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * ANTI-PADRAO: Duplicação de Regras (DIAG-04)
 * =============================================
 * Esta classe existe separada de MatriculaServiceImpl mas duplica as mesmas validações.
 * A validação "aluno inativo não pode adicionar disciplinas" já existe em
 * MatriculaServiceImpl.adicionarDisciplina(). Daqui a 6 meses, alguém adiciona uma
 * exceção em um lugar e esquece do outro — as regras divergem silenciosamente.
 *
 * Causa raiz: sem um modelo de domínio rico (Aluno.podeMatricular(),
 * Matricula.adicionarDisciplina()), as regras vivem no Service e são copiadas quando
 * novos Services surgem. Cada feature nova cria um novo Service; cada novo Service
 * copia as validações do Service mais próximo; as validações divergem com o tempo.
 *
 * Contraste: erp-matricula-ddd/.../aplicacao/AdicionarDisciplinaUseCase.java delega
 * ao aggregate Matricula.adicionarDisciplina() — não revalida o aluno, pois o próprio
 * aggregate protege sua invariante. A validação existe em exatamente um lugar.
 */
@Service
@Transactional
public class DisciplinaServiceImpl {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final ItemMatriculaRepository itemMatriculaRepository;

    // Injeção por construtor — sem @Autowired.
    public DisciplinaServiceImpl(MatriculaRepository matriculaRepository,
                                 AlunoRepository alunoRepository,
                                 ItemMatriculaRepository itemMatriculaRepository) {
        this.matriculaRepository = matriculaRepository;
        this.alunoRepository = alunoRepository;
        this.itemMatriculaRepository = itemMatriculaRepository;
    }

    /**
     * Adiciona uma disciplina extra a uma matrícula existente.
     *
     * <p>Este método duplica a lógica de {@link MatriculaServiceImpl#adicionarDisciplina}
     * porque DisciplinaServiceImpl foi criado para uma feature nova que precisava de
     * um ponto de extensão separado. A duplicação parecia razoável no momento da criação.
     * Seis meses depois, MatriculaServiceImpl adicionou exceção para "alunos em período
     * de teste" — DisciplinaServiceImpl não foi atualizado.</p>
     *
     * @param matriculaId   ID da matrícula
     * @param nomeDisciplina nome da disciplina a adicionar
     */
    public void adicionarDisciplinaExtra(UUID matriculaId, String nomeDisciplina) {

        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada: " + matriculaId));

        // Verificação de status — mesma verificação que existe em MatriculaServiceImpl.adicionarDisciplina()
        // e em MatriculaServiceImpl.cancelar(). Três lugares, mesma regra.
        if (!"ATIVA".equals(matricula.getStatus())) {
            throw new RuntimeException("Matrícula não está ativa. Status atual: " + matricula.getStatus());
        }

        Aluno aluno = alunoRepository.findById(matricula.getAlunoId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + matricula.getAlunoId()));

        // ANTI-PADRAO: Duplicação de Regras (DIAG-04)
        // Cópia exata da validação em MatriculaServiceImpl.adicionarDisciplina().
        // Em projetos reais, esta cópia foi feita quando DisciplinaServiceImpl foi criada
        // para uma feature nova. A duplicação parecia razoável. Seis meses depois,
        // MatriculaServiceImpl adicionou exceção para "alunos em período de teste" —
        // DisciplinaServiceImpl não foi atualizado. Os alunos em período de teste podem
        // adicionar disciplinas extras (bug silencioso).
        if (!aluno.isAtivo()) {
            throw new RuntimeException("Aluno inativo");
        }

        // ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
        // Mesma query countDisciplinas() de MatriculaServiceImpl.adicionarDisciplina().
        // A regra "máximo 6 disciplinas" está no repositório (matriculaRepository.countDisciplinas),
        // não no modelo de domínio.
        int qtd = matriculaRepository.countDisciplinas(matriculaId);
        if (qtd >= 6) {
            throw new RuntimeException("Limite atingido");
        }

        ItemMatricula item = new ItemMatricula();
        item.setId(UUID.randomUUID());
        item.setMatriculaId(matriculaId);
        item.setDisciplina(nomeDisciplina);
        item.setAdicionadaEm(LocalDateTime.now());

        itemMatriculaRepository.insert(item);
    }

    /**
     * Verifica se uma matrícula está ativa.
     *
     * <p>ANTI-PADRAO: Duplicação de Regras (DIAG-04)<br>
     * Verificação duplicada. {@code MatriculaServiceImpl.cancelar()} faz a mesma verificação
     * inline. Este método foi criado aqui para "reutilização", mas MatriculaServiceImpl não o
     * usa — ele está duplicado de outra forma.</p>
     *
     * <p>A proliferação de métodos verificadores (verificarStatus, verificarElegibilidade,
     * isAtivo, estaAtiva) é um sintoma: sem um modelo de domínio rico que encapsule estado
     * e comportamento, os Services criam seus próprios helpers auxiliares que duplicam a lógica
     * uns dos outros.</p>
     *
     * @param matriculaId ID da matrícula a verificar
     * @return {@code true} se a matrícula estiver com status "ATIVA"
     */
    public boolean verificarStatusMatricula(UUID matriculaId) {
        // ANTI-PADRAO: Duplicação de Regras (DIAG-04)
        // MatriculaServiceImpl.cancelar() e MatriculaServiceImpl.adicionarDisciplina()
        // fazem a mesma verificação !"ATIVA".equals(status) inline.
        // Este método foi criado para reutilização, mas nenhum dos outros Services o usa.
        return "ATIVA".equals(
                matriculaRepository.findById(matriculaId)
                        .map(Matricula::getStatus)
                        .orElse("")
        );
    }

}
