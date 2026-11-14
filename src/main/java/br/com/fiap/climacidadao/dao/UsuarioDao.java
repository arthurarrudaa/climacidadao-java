package br.com.fiap.climacidadao.dao;

import br.com.fiap.climacidadao.model.Usuario;
import br.com.fiap.climacidadao.exception.*;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioDao {
    private final DataSource dataSource;

    public UsuarioDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // O Oracle gera o proximo identificador usando a sequence.
    private long proximoId(Connection conexao) throws SQLException {
        String sql = "SELECT sq_tb_usuario.NEXTVAL FROM dual";
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            resultado.next();
            return resultado.getLong(1);
        }
    }

    public Usuario criar(Usuario objeto) {
        String sql = "INSERT INTO tb_usuario (cd_usuario, nm_usuario, ds_email) VALUES (?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            objeto.setId(proximoId(conexao));
            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setLong(1, objeto.getId());
                comando.setString(2, objeto.getNome());
                comando.setString(3, objeto.getEmail());
                comando.executeUpdate();
            }
            return objeto;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new ConflitoException("E-mail ja cadastrado.");
            }
            throw new FalhaBancoException("Nao foi possivel criar usuario.", e);
        }
    }

    public List<Usuario> listar() {
        String sql = "SELECT cd_usuario, nm_usuario, ds_email FROM tb_usuario ORDER BY cd_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                lista.add(ler(resultado));
            }
            return lista;
        } catch (SQLException e) {
            throw new FalhaBancoException("Nao foi possivel listar tb_usuario.", e);
        }
    }

    public Usuario buscarPorId(long id) {
        String sql = "SELECT cd_usuario, nm_usuario, ds_email FROM tb_usuario WHERE cd_usuario = ?";
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
            throw new FalhaBancoException("Nao foi possivel buscar tb_usuario.", e);
        }
    }

    public boolean atualizar(Usuario objeto) {
        String sql = "UPDATE tb_usuario SET nm_usuario = ?, ds_email = ? WHERE cd_usuario = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, objeto.getNome());
            comando.setString(2, objeto.getEmail());
            comando.setLong(3, objeto.getId());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new ConflitoException("E-mail ja cadastrado.");
            }
            throw new FalhaBancoException("Nao foi possivel atualizar usuario.", e);
        }
    }

    public boolean excluir(long id) {
        String sql = "DELETE FROM tb_usuario WHERE cd_usuario = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new ConflitoException("E-mail ja cadastrado.");
            }
            if (e.getErrorCode() == 2292) {
                throw new ConflitoException("Exclua os favoritos e pesquisas antes de excluir o usuario.");
            }
            throw new FalhaBancoException("Nao foi possivel excluir usuario.", e);
        }
    }

    private Usuario ler(ResultSet resultado) throws SQLException {
        Usuario objeto = new Usuario();
        objeto.setId(resultado.getLong("cd_usuario"));
        objeto.setNome(resultado.getString("nm_usuario"));
        objeto.setEmail(resultado.getString("ds_email"));
        return objeto;
    }
}
