package br.com.escola.matricula.repository;

import br.com.escola.matricula.model.ItemMatricula;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repositório MyBatis direto para ItemMatricula.
 *
 * <p>No módulo camadas, o @Mapper é o repositório — sem separação entre interface de domínio
 * e implementação de infraestrutura. Contraste com o módulo DDD onde os itens de matrícula
 * são persistidos como parte do Aggregate Root Matricula (sem repositório separado).</p>
 *
 * <p>Namespace XML: {@code br.com.escola.matricula.repository.ItemMatriculaRepository}
 * em {@code src/main/resources/mapper/ItemMatriculaMapper.xml}</p>
 */
@Mapper
public interface ItemMatriculaRepository {

    List<ItemMatricula> findByMatriculaId(@Param("matriculaId") UUID matriculaId);

    void insert(ItemMatricula item);

    // ANTI-PADRAO: Acoplamento ao Banco (DIAG-06)
    // Contagem de disciplinas via SQL no repositório, não via invariante no objeto Matricula.
    // A regra "máximo 6 disciplinas" deveria viver em Matricula.adicionarDisciplina() —
    // lá, a entidade pode proteger sua própria invariante sem depender de uma query ao banco.
    // Contraste: erp-matricula-ddd/.../dominio/modelo/Matricula.java verifica o limite
    // internamente dentro do método adicionarDisciplina() sem consultar o repositório.
    int countByMatriculaId(@Param("matriculaId") UUID matriculaId);

}
