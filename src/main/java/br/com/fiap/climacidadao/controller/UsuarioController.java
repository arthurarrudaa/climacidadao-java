package br.com.fiap.climacidadao.controller;

import br.com.fiap.climacidadao.dao.UsuarioDao;
import br.com.fiap.climacidadao.dto.UsuarioEntradaDTO;
import br.com.fiap.climacidadao.dto.UsuarioSaidaDTO;
import br.com.fiap.climacidadao.exception.RecursoNaoEncontradoException;
import br.com.fiap.climacidadao.model.Usuario;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
public class UsuarioController {
    private final UsuarioDao usuarioDao;
    private final ModelMapper mapper;

    public UsuarioController(UsuarioDao usuarioDao, ModelMapper mapper) {
        this.usuarioDao = usuarioDao;
        this.mapper = mapper;
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioSaidaDTO> cadastrar(@Valid @RequestBody UsuarioEntradaDTO entrada) {
        Usuario usuario = mapper.map(entrada, Usuario.class);
        normalizar(usuario);
        usuarioDao.criar(usuario);
        UsuarioSaidaDTO saida = mapper.map(usuario, UsuarioSaidaDTO.class);
        return ResponseEntity.created(URI.create("/usuarios/" + usuario.getId())).body(saida);
    }

    @GetMapping("/usuarios")
    public List<UsuarioSaidaDTO> listar() {
        List<UsuarioSaidaDTO> saida = new ArrayList<>();
        for (Usuario usuario : usuarioDao.listar()) {
            saida.add(mapper.map(usuario, UsuarioSaidaDTO.class));
        }
        return saida;
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioSaidaDTO buscarPorId(@PathVariable long id) {
        return mapper.map(buscarUsuario(id), UsuarioSaidaDTO.class);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioSaidaDTO atualizar(@PathVariable long id, @Valid @RequestBody UsuarioEntradaDTO entrada) {
        buscarUsuario(id);
        Usuario usuario = mapper.map(entrada, Usuario.class);
        usuario.setId(id);
        normalizar(usuario);
        if (!usuarioDao.atualizar(usuario)) {
            throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
        }
        return mapper.map(usuario, UsuarioSaidaDTO.class);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        if (!usuarioDao.excluir(id)) {
            throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
        }
        // O banco impede excluir um usuario que ainda possui filhos (409).
        return ResponseEntity.noContent().build();
    }

    private Usuario buscarUsuario(long id) {
        Usuario usuario = usuarioDao.buscarPorId(id);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
        }
        return usuario;
    }

    private void normalizar(Usuario usuario) {
        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase(Locale.ROOT));
    }
}
