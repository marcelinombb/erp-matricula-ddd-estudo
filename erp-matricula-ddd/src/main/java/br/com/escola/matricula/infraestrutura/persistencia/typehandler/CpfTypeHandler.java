package br.com.escola.matricula.infraestrutura.persistencia.typehandler;

import br.com.escola.matricula.dominio.vo.Cpf;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TypeHandler MyBatis para converter entre {@link Cpf} (Value Object do domínio)
 * e {@code VARCHAR} (coluna do banco de dados).
 *
 * <p><strong>O que é um TypeHandler?</strong> É o mecanismo do MyBatis para converter
 * tipos Java ↔ tipos JDBC. Sem este handler, MyBatis não saberia como setar um {@code Cpf}
 * em um {@code PreparedStatement} ou como ler um {@code String} do {@code ResultSet}
 * e convertê-lo em {@code Cpf}.</p>
 *
 * <p><strong>Registro automático:</strong> este handler é detectado pelo MyBatis via
 * package scanning. No {@code application.yml}:
 * <pre>
 * mybatis:
 *   type-handlers-package: br.com.escola.matricula.infraestrutura.persistencia.typehandler
 * </pre>
 * Não é necessário registro manual via {@code @Bean} em {@code MyBatisConfig}.
 * Ver RESEARCH.md "Don't Hand-Roll — Registro de TypeHandlers".</p>
 *
 * <p><strong>Nota sobre PeriodoLetivo:</strong> {@code PeriodoLetivo(ano, semestre)} tem dois campos
 * que mapeiam para duas colunas DATE ({@code periodo_inicio}, {@code periodo_fim}).
 * TypeHandlers mapeiam um VO para UMA coluna. Para PeriodoLetivo, a conversão é feita
 * inline em {@link br.com.escola.matricula.infraestrutura.persistencia.MatriculaRowMapper}
 * (Opção C do RESEARCH.md — sem TypeHandler customizado).</p>
 *
 * <p>Referências: RESEARCH.md Seção 2, INF-05 (.planning/REQUIREMENTS.md)</p>
 */
@MappedTypes(Cpf.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class CpfTypeHandler extends BaseTypeHandler<Cpf> {

    /**
     * Seta o {@code Cpf} no {@code PreparedStatement} como String VARCHAR.
     * Chama {@code cpf.valor()} para obter os 11 dígitos normalizados (sem máscara).
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Cpf cpf, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, cpf.valor());
    }

    /**
     * Lê o valor da coluna por nome e converte para {@code Cpf}.
     * Retorna {@code null} se a coluna for NULL — {@code BaseTypeHandler} garante
     * que este método só é chamado quando o valor não é nulo.
     */
    @Override
    public Cpf getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String valor = rs.getString(columnName);
        return valor != null ? new Cpf(valor) : null;
    }

    /**
     * Lê o valor da coluna por índice e converte para {@code Cpf}.
     */
    @Override
    public Cpf getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String valor = rs.getString(columnIndex);
        return valor != null ? new Cpf(valor) : null;
    }

    /**
     * Lê o valor de um {@code CallableStatement} (stored procedures) e converte para {@code Cpf}.
     */
    @Override
    public Cpf getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String valor = cs.getString(columnIndex);
        return valor != null ? new Cpf(valor) : null;
    }
}
