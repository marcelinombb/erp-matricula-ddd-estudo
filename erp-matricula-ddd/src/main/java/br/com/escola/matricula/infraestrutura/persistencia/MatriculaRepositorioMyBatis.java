package br.com.escola.matricula.infraestrutura.persistencia;

import br.com.escola.matricula.dominio.modelo.Matricula;
import br.com.escola.matricula.dominio.repositorio.MatriculaRepositorio;
import br.com.escola.matricula.dominio.vo.PeriodoLetivo;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação de {@link MatriculaRepositorio} usando MyBatis.
 *
 * <p><strong>Dependency Inversion em ação:</strong> a interface {@code MatriculaRepositorio}
 * está no pacote {@code dominio.repositorio} — o domínio define o contrato. Esta classe está
 * em {@code infraestrutura.persistencia} — a infraestrutura implementa.</p>
 *
 * <p><strong>Estratégia replace-all para {@code salvar()} (D-12):</strong>
 * A coleção de {@code ItemMatricula} é tratada como um todo atômico.</p>
 */
@Repository
public class MatriculaRepositorioMyBatis implements MatriculaRepositorio {

    private final MatriculaMapper mapper;
    private final MatriculaRowMapper rowMapper;

    public MatriculaRepositorioMyBatis(MatriculaMapper mapper, MatriculaRowMapper rowMapper) {
        this.mapper    = mapper;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<Matricula> buscarPorId(UUID id) {
        MatriculaRow row = mapper.buscarPorId(id);
        return Optional.ofNullable(row).map(rowMapper::toDomain);
    }

    @Override
    public List<Matricula> buscarPorAluno(UUID alunoId) {
        return mapper.buscarPorAluno(alunoId).stream()
            .map(rowMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existeMatriculaAtiva(UUID alunoId, PeriodoLetivo periodo) {
        LocalDate inicio = LocalDate.of(periodo.ano(), periodo.semestre() == 1 ? 2 : 8, 1);
        LocalDate fim    = LocalDate.of(periodo.ano(), periodo.semestre() == 1 ? 7 : 12, 31);
        return mapper.existeMatriculaAtiva(alunoId, inicio, fim);
    }

    @Override
    public void salvar(Matricula matricula) {
        MatriculaRow row = rowMapper.fromDomain(matricula);

        if (mapper.atualizarMatricula(row) == 0) {
            mapper.inserirMatricula(row);
        }

        mapper.deletarItensPorMatriculaId(matricula.getId());

        List<ItemMatriculaRow> itens = rowMapper.itemsFromDomain(
            matricula.getId(), matricula.getDisciplinas());
        if (!itens.isEmpty()) {
            mapper.inserirItens(itens);
        }
    }
}
