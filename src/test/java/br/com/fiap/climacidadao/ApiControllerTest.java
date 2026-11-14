package br.com.fiap.climacidadao;

import br.com.fiap.climacidadao.config.ModelMapperConfig;
import br.com.fiap.climacidadao.controller.FavoritoController;
import br.com.fiap.climacidadao.controller.PesquisaController;
import br.com.fiap.climacidadao.controller.UsuarioController;
import br.com.fiap.climacidadao.dao.FavoritoDao;
import br.com.fiap.climacidadao.dao.PesquisaDao;
import br.com.fiap.climacidadao.dao.UsuarioDao;
import br.com.fiap.climacidadao.exception.ConflitoException;
import br.com.fiap.climacidadao.exception.FalhaBancoException;
import br.com.fiap.climacidadao.model.Favorito;
import br.com.fiap.climacidadao.model.Pesquisa;
import br.com.fiap.climacidadao.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Estes testes simulam os DAOs; nao precisam acessar o Oracle.
@WebMvcTest(controllers = {UsuarioController.class, FavoritoController.class, PesquisaController.class})
@Import(ModelMapperConfig.class)
class ApiControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UsuarioDao usuarioDao;

    @MockitoBean
    private FavoritoDao favoritoDao;

    @MockitoBean
    private PesquisaDao pesquisaDao;

    @Test
    void listarUsuariosRetorna200() throws Exception {
        when(usuarioDao.listar()).thenReturn(List.of(usuario()));
        mvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Ana Silva"));
    }

    @Test
    void cadastrarUsuarioRetorna201ELocation() throws Exception {
        when(usuarioDao.criar(any(Usuario.class))).thenAnswer(chamada -> {
            Usuario usuario = chamada.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });
        mvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana Silva\",\"email\":\"ana@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/usuarios/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void usuarioInvalidoRetorna400() throws Exception {
        mvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void usuarioInexistenteRetorna404() throws Exception {
        mvc.perform(get("/usuarios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void metodoInvalidoRetorna405() throws Exception {
        mvc.perform(patch("/usuarios/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\"}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void favoritoDuplicadoRetorna409() throws Exception {
        when(usuarioDao.buscarPorId(1L)).thenReturn(usuario());
        when(favoritoDao.existeDuplicado(eq(1L), eq("Sao Paulo"), eq(-1L))).thenReturn(true);
        mvc.perform(post("/usuarios/1/favoritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cidade\":\"Sao Paulo\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void listarFavoritosDoUsuarioRetorna200() throws Exception {
        when(usuarioDao.buscarPorId(1L)).thenReturn(usuario());
        Favorito favorito = new Favorito();
        favorito.setId(2L);
        favorito.setUsuarioId(1L);
        favorito.setCidade("Cotia");
        when(favoritoDao.listarPorUsuario(1L)).thenReturn(List.of(favorito));
        mvc.perform(get("/usuarios/1/favoritos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cidade").value("Cotia"));
    }

    @Test
    void cadastrarPesquisaRetorna201EDataDeConsulta() throws Exception {
        when(usuarioDao.buscarPorId(1L)).thenReturn(usuario());
        when(pesquisaDao.criar(any(Pesquisa.class))).thenAnswer(chamada -> {
            Pesquisa pesquisa = chamada.getArgument(0);
            pesquisa.setId(3L);
            return pesquisa;
        });
        mvc.perform(post("/usuarios/1/pesquisas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cidade\":\"Cotia\",\"temperatura\":24.5}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/pesquisas/3"))
                .andExpect(jsonPath("$.dataConsulta").exists());
    }

    @Test
    void listarHistoricoDoUsuarioRetorna200() throws Exception {
        when(usuarioDao.buscarPorId(1L)).thenReturn(usuario());
        Pesquisa pesquisa = new Pesquisa();
        pesquisa.setId(3L);
        pesquisa.setUsuarioId(1L);
        pesquisa.setCidade("Cotia");
        pesquisa.setTemperatura(new BigDecimal("24.50"));
        pesquisa.setDataConsulta(LocalDateTime.of(2026, 1, 1, 12, 0));
        when(pesquisaDao.listarPorUsuario(1L)).thenReturn(List.of(pesquisa));
        mvc.perform(get("/usuarios/1/pesquisas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cidade").value("Cotia"));
    }

    @Test
    void excluirUsuarioComFilhosRetorna409() throws Exception {
        when(usuarioDao.excluir(1L)).thenThrow(new ConflitoException("Possui favoritos ou pesquisas."));
        mvc.perform(delete("/usuarios/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void excluirFavoritoRetorna204() throws Exception {
        when(favoritoDao.excluir(2L)).thenReturn(true);
        mvc.perform(delete("/favoritos/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void erroInternoNaoRevelaSql() throws Exception {
        when(usuarioDao.listar()).thenThrow(new FalhaBancoException(
                "Falha SQL", new SQLException("ORA-xxxxx SENHA TESTE")));
        mvc.perform(get("/usuarios"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensagem").value("Erro interno. Tente novamente mais tarde."));
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana Silva");
        usuario.setEmail("ana@example.com");
        return usuario;
    }
}
