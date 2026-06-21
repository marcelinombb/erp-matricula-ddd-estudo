package br.com.escola.matricula.repository;

import br.com.escola.matricula.model.Matricula;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório MyBatis direto — no módulo DDD esta interface seria o MatriculaRepositorio
 * (domínio puro) separado do MatriculaMapper (infraestrutura). Aqui são um só — sem
 * separação camadas de domínio / infraestrutura.
 *
 * <p>No módulo DDD existe uma hierarquia de três camadas:
 * <ol>
 *   <li>{@code MatriculaRepositorio} — interface pura no domínio (sem @Mapper)</li>
 *   <li>{@code MatriculaMapper} — @Mapper de infraestrutura separado</li>
 *   <li>{@code MatriculaRepositorioMyBatis} — implementa MatriculaRepositorio usando MatriculaMapper</li>
 * </ol>
 * Aqui, este @Mapper é diretamente o repositório consumido pelo Service.
 * A separação existe apenas por convenção de pacotes — não por compilação.</p>
 *
 * <p>Namespace XML: {@code br.com.escola.matricula.repository.MatriculaRepository}
 * em {@code src/main/resources/mapper/MatriculaMapper.xml}</p>
 */
@Mapper
public interface MatriculaRepository {

    Optional<Matricula> findById(@Param("id") UUID id);

    List<Matricula> findByAlunoId(@Param("alunoId") UUID alunoId);

    void insert(Matricula matricula);

    void updateStatus(@Param("id") UUID id, @Param("status") String status);

    // ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
    // A regra "máximo 6 disciplinas" não existe no modelo de domínio.
    // Ela existe como uma query SQL. Se você ler Matricula.java, não encontra essa regra.
    // Contraste: erp-matricula-ddd/.../dominio/modelo/Matricula.java protege
    // a invariante de limite de disciplinas internamente, sem consultar o banco.
    int countDisciplinas(@Param("matriculaId") UUID matriculaId);

}
