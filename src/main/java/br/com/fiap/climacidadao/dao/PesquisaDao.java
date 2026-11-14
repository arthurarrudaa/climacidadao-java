package br.com.fiap.climacidadao.dao;

import br.com.fiap.climacidadao.model.Pesquisa;
import br.com.fiap.climacidadao.exception.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PesquisaDao {
    private final DataSource dataSource;

    public PesquisaDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // O Oracle gera o proximo identificador usando a sequence.
    private long proximoId(Connection conexao) throws SQLException {
        String sql = "SELECT sq_tb_pesquisa.NEXTVAL FROM dual";
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            resultado.next();
            return resultado.getLong(1);
        }
    }

    public Pesquisa criar(Pesquisa objeto) {
        String sql = "INSERT INTO tb_pesquisa (cd_pesquisa, cd_usuario, nm_cidade, vl_temperatura, dt_consulta) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            objeto.setId(proximoId(conexao));
            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setLong(1, objeto.getId());
                comando.setLong(2, objeto.getUsuarioId());
                comando.setString(3, objeto.getCidade());
                comando.setBigDecimal(4, objeto.getTemperatura());
                comando.setTimestamp(5, Timestamp.valueOf(objeto.getDataConsulta()));
                comando.executeUpdate();
            }
            return objeto;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2291) {
                throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
            }
            if (e.getErrorCode() == 2290) {
                throw new RequisicaoInvalidaException("Temperatura fora do limite permitido.");
            }
            throw new FalhaBancoException("Nao foi possivel criar pesquisa.", e);
        }
    }

    public List<Pesquisa> listar() {
        String sql = "SELECT cd_pesquisa, cd_usuario, nm_cidade, vl_temperatura, dt_consulta FROM tb_pesquisa ORDER BY cd_pesquisa";
        List<Pesquisa> lista = new ArrayList<>();
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                lista.add(ler(resultado));
            }
            return lista;
        } catch (SQLException e) {
            throw new FalhaBancoException("Nao foi possivel listar tb_pesquisa.", e);
        }
    }

    public Pesquisa buscarPorId(long id) {
        String sql = "SELECT cd_pesquisa, cd_usuario, nm_cidade, vl_temperatura, dt_consulta FROM tb_pesquisa WHERE cd_pesquisa = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return ler(resultado);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new FalhaBancoException("Nao foi possivel buscar tb_pesquisa.", e);
        }
    }

    public boolean atualizar(Pesquisa objeto) {
        String sql = "UPDATE tb_pesquisa SET cd_usuario = ?, nm_cidade = ?, vl_temperatura = ?, dt_consulta = ? WHERE cd_pesquisa = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, objeto.getUsuarioId());
            comando.setString(2, objeto.getCidade());
            comando.setBigDecimal(3, objeto.getTemperatura());
            comando.setTimestamp(4, Timestamp.valueOf(objeto.getDataConsulta()));
            comando.setLong(5, objeto.getId());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2291) {
                throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
            }
            if (e.getErrorCode() == 2290) {
                throw new RequisicaoInvalidaException("Temperatura fora do limite permitido.");
            }
            throw new FalhaBancoException("Nao foi possivel atualizar pesquisa.", e);
        }
    }

    public boolean excluir(long id) {
        String sql = "DELETE FROM tb_pesquisa WHERE cd_pesquisa = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new FalhaBancoException("Nao foi possivel excluir pesquisa.", e);
        }
    }

    public List<Pesquisa> listarPorUsuario(long usuarioId) {
        String sql = "SELECT cd_pesquisa, cd_usuario, nm_cidade, vl_temperatura, dt_consulta FROM tb_pesquisa WHERE cd_usuario = ? ORDER BY cd_pesquisa";
        List<Pesquisa> lista = new ArrayList<>();
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, usuarioId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    lista.add(ler(resultado));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new FalhaBancoException("Nao foi possivel listar os registros do usuario.", e);
        }
    }

    private Pesquisa ler(ResultSet resultado) throws SQLException {
        Pesquisa objeto = new Pesquisa();
        objeto.setId(resultado.getLong("cd_pesquisa"));
        objeto.setUsuarioId(resultado.getLong("cd_usuario"));
        objeto.setCidade(resultado.getString("nm_cidade"));
        objeto.setTemperatura(resultado.getBigDecimal("vl_temperatura"));
        objeto.setDataConsulta(resultado.getTimestamp("dt_consulta").toLocalDateTime());
        return objeto;
    }
}
