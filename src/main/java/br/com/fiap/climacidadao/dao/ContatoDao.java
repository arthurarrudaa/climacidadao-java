
package br.com.fiap.climacidadao.dao;

import br.com.fiap.climacidadao.exception.FalhaBancoException;
import br.com.fiap.climacidadao.model.Contato;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ContatoDao {

    private final DataSource dataSource;

    public ContatoDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private long proximoId(Connection conexao) throws SQLException {
        String sql = "SELECT sq_tb_contato.NEXTVAL FROM dual";

        try (PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            resultado.next();
            return resultado.getLong(1);
        }
    }

    public Contato criar(Contato contato) {
        String sql = "INSERT INTO tb_contato " +
                "(cd_contato, nm_contato, ds_email, ds_mensagem) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conexao = dataSource.getConnection()) {

            contato.setId(proximoId(conexao));

            try (PreparedStatement comando = conexao.prepareStatement(sql)) {
                comando.setLong(1, contato.getId());
                comando.setString(2, contato.getNome());
                comando.setString(3, contato.getEmail());
                comando.setString(4, contato.getMensagem());

                comando.executeUpdate();
            }

            return contato;

        } catch (SQLException e) {
            throw new FalhaBancoException(
                    "Nao foi possivel cadastrar o contato.", e);
        }
    }

    public List<Contato> listar() {
        String sql = "SELECT cd_contato, nm_contato, ds_email, ds_mensagem " +
                "FROM tb_contato ORDER BY cd_contato";

        List<Contato> contatos = new ArrayList<>();

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                contatos.add(converter(resultado));
            }

            return contatos;

        } catch (SQLException e) {
            throw new FalhaBancoException(
                    "Nao foi possivel listar os contatos.", e);
        }
    }

    public Contato buscarPorId(long id) {
        String sql = "SELECT cd_contato, nm_contato, ds_email, ds_mensagem " +
                "FROM tb_contato WHERE cd_contato = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return converter(resultado);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new FalhaBancoException(
                    "Nao foi possivel consultar o contato.", e);
        }
    }

    public boolean atualizar(Contato contato) {
        String sql = "UPDATE tb_contato SET " +
                "nm_contato = ?, ds_email = ?, ds_mensagem = ? " +
                "WHERE cd_contato = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, contato.getNome());
            comando.setString(2, contato.getEmail());
            comando.setString(3, contato.getMensagem());
            comando.setLong(4, contato.getId());

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new FalhaBancoException(
                    "Nao foi possivel atualizar o contato.", e);
        }
    }

    public boolean excluir(long id) {
        String sql = "DELETE FROM tb_contato WHERE cd_contato = ?";

        try (Connection conexao = dataSource.getConnection();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setLong(1, id);

            return comando.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new FalhaBancoException(
                    "Nao foi possivel excluir o contato.", e);
        }
    }

    private Contato converter(ResultSet resultado)
            throws SQLException {

        Contato contato = new Contato();

        contato.setId(resultado.getLong("cd_contato"));
        contato.setNome(resultado.getString("nm_contato"));
        contato.setEmail(resultado.getString("ds_email"));
        contato.setMensagem(resultado.getString("ds_mensagem"));

        return contato;
    }
}
