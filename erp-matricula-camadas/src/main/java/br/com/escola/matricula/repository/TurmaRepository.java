package br.com.escola.matricula.repository;

import br.com.escola.matricula.model.Turma;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório MyBatis direto para Turma.
 *
 * <p>No módulo camadas, o @Mapper é o repositório — sem separação entre interface de domínio
 * e implementação de infraestrutura. Contraste com o módulo DDD onde a interface de domínio
 * ficaria no pacote {@code dominio.repositorio} sem @Mapper.</p>
 *
 * <p>Namespace XML: {@code br.com.escola.matricula.repository.TurmaRepository}
 * em {@code src/main/resources/mapper/TurmaMapper.xml}</p>
 */
@Mapper
public interface TurmaRepository {

    Optional<Turma> findById(@Param("id") UUID id);

}
