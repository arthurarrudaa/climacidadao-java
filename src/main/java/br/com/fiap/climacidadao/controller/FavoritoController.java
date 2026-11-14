package br.com.fiap.climacidadao.controller;

import br.com.fiap.climacidadao.dao.FavoritoDao;
import br.com.fiap.climacidadao.dao.UsuarioDao;
import br.com.fiap.climacidadao.dto.FavoritoEntradaDTO;
import br.com.fiap.climacidadao.dto.FavoritoSaidaDTO;
import br.com.fiap.climacidadao.exception.ConflitoException;
import br.com.fiap.climacidadao.exception.RecursoNaoEncontradoException;
import br.com.fiap.climacidadao.exception.RequisicaoInvalidaException;
import br.com.fiap.climacidadao.model.Favorito;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FavoritoController {
    private final FavoritoDao favoritoDao;
    private final UsuarioDao usuarioDao;
    private final ModelMapper mapper;

    public FavoritoController(FavoritoDao favoritoDao, UsuarioDao usuarioDao, ModelMapper mapper) {
        this.favoritoDao = favoritoDao;
        this.usuarioDao = usuarioDao;
        this.mapper = mapper;
    }

    @PostMapping("/favoritos")
    public ResponseEntity<FavoritoSaidaDTO> cadastrar(@Valid @RequestBody FavoritoEntradaDTO entrada) {
        validarUsuario(entrada.getUsuarioId());
        Favorito favorito = mapper.map(entrada, Favorito.class);
        favorito.setCidade(favorito.getCidade().trim());
        verificarDuplicado(favorito.getUsuarioId(), favorito.getCidade(), -1);
        favoritoDao.criar(favorito);
        return ResponseEntity.created(URI.create("/favoritos/" + favorito.getId()))
                .body(mapper.map(favorito, FavoritoSaidaDTO.class));
    }

    @PostMapping("/usuarios/{usuarioId}/favoritos")
    public ResponseEntity<FavoritoSaidaDTO> cadastrarParaUsuario(
            @PathVariable long usuarioId, @Valid @RequestBody FavoritoEntradaDTO entrada) {
        conferirUsuarioDaRota(usuarioId, entrada.getUsuarioId());
        entrada.setUsuarioId(usuarioId);
        return cadastrar(entrada);
    }

    @GetMapping("/favoritos")
    public List<FavoritoSaidaDTO> listar() {
        return converterLista(favoritoDao.listar());
    }

    @GetMapping("/favoritos/{id}")
    public FavoritoSaidaDTO buscarPorId(@PathVariable long id) {
        return mapper.map(buscarFavorito(id), FavoritoSaidaDTO.class);
    }

    @GetMapping("/usuarios/{usuarioId}/favoritos")
    public List<FavoritoSaidaDTO> listarPorUsuario(@PathVariable long usuarioId) {
        validarUsuario(usuarioId);
        return converterLista(favoritoDao.listarPorUsuario(usuarioId));
    }

    @PutMapping("/favoritos/{id}")
    public FavoritoSaidaDTO atualizar(@PathVariable long id, @Valid @RequestBody FavoritoEntradaDTO entrada) {
        Favorito anterior = buscarFavorito(id);
        if (entrada.getUsuarioId() == null) {
            entrada.setUsuarioId(anterior.getUsuarioId());
        }
        validarUsuario(entrada.getUsuarioId());
        Favorito favorito = mapper.map(entrada, Favorito.class);
        favorito.setId(id);
        favorito.setCidade(favorito.getCidade().trim());
        verificarDuplicado(favorito.getUsuarioId(), favorito.getCidade(), id);
        if (!favoritoDao.atualizar(favorito)) {
            throw new RecursoNaoEncontradoException("Favorito nao encontrado.");
        }
        return mapper.map(favorito, FavoritoSaidaDTO.class);
    }

    @DeleteMapping("/favoritos/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        if (!favoritoDao.excluir(id)) {
            throw new RecursoNaoEncontradoException("Favorito nao encontrado.");
        }
        return ResponseEntity.noContent().build();
    }

    private void validarUsuario(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new RequisicaoInvalidaException("Informe um usuarioId positivo.");
        }
        if (usuarioDao.buscarPorId(usuarioId) == null) {
            throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
        }
    }

    private void conferirUsuarioDaRota(long usuarioId, Long usuarioIdDoCorpo) {
        if (usuarioIdDoCorpo != null && usuarioIdDoCorpo != usuarioId) {
            throw new RequisicaoInvalidaException("O usuarioId do corpo difere do usuarioId da rota.");
        }
    }

    private void verificarDuplicado(long usuarioId, String cidade, long idIgnorado) {
        if (favoritoDao.existeDuplicado(usuarioId, cidade, idIgnorado)) {
            throw new ConflitoException("Esta cidade ja esta nos favoritos do usuario.");
        }
    }

    private Favorito buscarFavorito(long id) {
        Favorito favorito = favoritoDao.buscarPorId(id);
        if (favorito == null) {
            throw new RecursoNaoEncontradoException("Favorito nao encontrado.");
        }
        return favorito;
    }

    private List<FavoritoSaidaDTO> converterLista(List<Favorito> favoritos) {
        List<FavoritoSaidaDTO> saida = new ArrayList<>();
        for (Favorito favorito : favoritos) {
            saida.add(mapper.map(favorito, FavoritoSaidaDTO.class));
        }
        return saida;
    }
}
