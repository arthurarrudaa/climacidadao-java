package br.com.fiap.climacidadao.dao;

import br.com.fiap.climacidadao.exception.FalhaBancoException;
import br.com.fiap.climacidadao.model.Contato;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
