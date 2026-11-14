package br.com.fiap.climacidadao.dao;

import br.com.fiap.climacidadao.model.Favorito;
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
public class FavoritoDao {
    private final DataSource dataSource;

    public FavoritoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // O Oracle gera o proximo identificador usando a sequence.
    private long proximoId(Connection conexao) throws SQLException {
        String sql = "SELECT sq_tb_favorito.NEXTVAL FROM dual";
        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            resultado.next();
            return resultado.getLong(1);
        }
    }

    public Favorito criar(Favorito objeto) {
        String sql = "INSERT INTO tb_favorito (cd_favorito, cd_usuario, nm_cidade) VALUES (?, ?, ?)";
        try (Connection conexao = dataSource.getConnection()) {
            objeto.setId(proximoId(conexao));
            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setLong(1, objeto.getId());
                comando.setLong(2, objeto.getUsuarioId());
                comando.setString(3, objeto.getCidade());
                comando.executeUpdate();
            }
            return objeto;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new ConflitoException("Esta cidade ja esta nos favoritos do usuario.");
            }
            if (e.getErrorCode() == 2291) {
                throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
            }
            throw new FalhaBancoException("Nao foi possivel criar favorito.", e);
        }
    }

    public List<Favorito> listar() {
        String sql = "SELECT cd_favorito, cd_usuario, nm_cidade FROM tb_favorito ORDER BY cd_favorito";
        List<Favorito> lista = new ArrayList<>();
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                lista.add(ler(resultado));
            }
            return lista;
        } catch (SQLException e) {
            throw new FalhaBancoException("Nao foi possivel listar tb_favorito.", e);
        }
    }

    public Favorito buscarPorId(long id) {
        String sql = "SELECT cd_favorito, cd_usuario, nm_cidade FROM tb_favorito WHERE cd_favorito = ?";
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
            throw new FalhaBancoException("Nao foi possivel buscar tb_favorito.", e);
        }
    }

    public boolean atualizar(Favorito objeto) {
        String sql = "UPDATE tb_favorito SET cd_usuario = ?, nm_cidade = ? WHERE cd_favorito = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, objeto.getUsuarioId());
            comando.setString(2, objeto.getCidade());
            comando.setLong(3, objeto.getId());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new ConflitoException("Esta cidade ja esta nos favoritos do usuario.");
            }
            if (e.getErrorCode() == 2291) {
                throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
            }
            throw new FalhaBancoException("Nao foi possivel atualizar favorito.", e);
        }
    }

    public boolean excluir(long id) {
        String sql = "DELETE FROM tb_favorito WHERE cd_favorito = ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                throw new ConflitoException("Esta cidade ja esta nos favoritos do usuario.");
            }
            throw new FalhaBancoException("Nao foi possivel excluir favorito.", e);
        }
    }

    public List<Favorito> listarPorUsuario(long usuarioId) {
        String sql = "SELECT cd_favorito, cd_usuario, nm_cidade FROM tb_favorito WHERE cd_usuario = ? ORDER BY cd_favorito";
        List<Favorito> lista = new ArrayList<>();
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

    // Ignora maiusculas, minusculas e espacos no inicio e fim da cidade.
    public boolean existeDuplicado(long usuarioId, String cidade, long idIgnorado) {
        String sql = "SELECT 1 FROM tb_favorito WHERE cd_usuario = ? "
                   + "AND UPPER(TRIM(nm_cidade)) = UPPER(TRIM(?)) AND cd_favorito <> ?";
        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setLong(1, usuarioId);
            comando.setString(2, cidade);
            comando.setLong(3, idIgnorado);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        } catch (SQLException e) {
            throw new FalhaBancoException("Nao foi possivel verificar favorito duplicado.", e);
        }
    }

    private Favorito ler(ResultSet resultado) throws SQLException {
        Favorito objeto = new Favorito();
        objeto.setId(resultado.getLong("cd_favorito"));
        objeto.setUsuarioId(resultado.getLong("cd_usuario"));
        objeto.setCidade(resultado.getString("nm_cidade"));
        return objeto;
    }
}
