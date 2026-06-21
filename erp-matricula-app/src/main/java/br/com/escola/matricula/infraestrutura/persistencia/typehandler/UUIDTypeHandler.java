package br.com.escola.matricula.infraestrutura.persistencia.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * TypeHandler MyBatis para {@code java.util.UUID}.
 *
 * <p>MyBatis não possui um UUIDTypeHandler embutido. Este handler é necessário para:
 * <ul>
 *   <li><strong>Leitura</strong> (ResultMap): converter colunas UUID do PostgreSQL para {@code UUID} Java</li>
 *   <li><strong>Escrita</strong> (parâmetros): o XML já usa {@code jdbcType=OTHER} nos parâmetros UUID,
 *       que instrui o PostgreSQL a tratar o valor como UUID nativo</li>
 * </ul>
 *
 * <p>Registrado automaticamente via {@code mybatis.type-handlers-package} no {@code application.yml}.
 * {@code includeNullJdbcType = true} garante que o handler é selecionado quando nenhum
 * {@code jdbcType} é especificado no ResultMap — que é o caso padrão para leitura de UUIDs.</p>
 */
@MappedTypes(UUID.class)
@MappedJdbcTypes(value = {JdbcType.OTHER, JdbcType.VARCHAR, JdbcType.CHAR}, includeNullJdbcType = true)
public class UUIDTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value == null ? null : UUID.fromString(value);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value == null ? null : UUID.fromString(value);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value == null ? null : UUID.fromString(value);
    }
}
