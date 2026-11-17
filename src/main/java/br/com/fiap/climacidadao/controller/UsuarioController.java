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
        Usuario usuario = preparar(entrada);
        usuarioDao.criar(usuario);
        return ResponseEntity.created(URI.create("/usuarios/" + usuario.getId()))
                .body(mapper.map(usuario, UsuarioSaidaDTO.class));
    }

    @GetMapping("/usuarios")
    public List<UsuarioSaidaDTO> listar() {
        List<UsuarioSaidaDTO> resultado = new ArrayList<>();
        for (Usuario usuario : usuarioDao.listar()) {
            resultado.add(mapper.map(usuario, UsuarioSaidaDTO.class));
        }
        return resultado;
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioSaidaDTO buscarPorId(@PathVariable long id) {
        Usuario usuario = usuarioDao.buscarPorId(id);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuario nao encontrado.");
        }
        return mapper.map(usuario, UsuarioSaidaDTO.class);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioSaidaDTO atualizar(@PathVariable long id, @Valid @RequestBody UsuarioEntradaDTO entrada) {
        Usuario usuario = preparar(entrada);
        usuario.setId(id);
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
        return ResponseEntity.noContent().build();
    }

    // Prepara os dados tanto para cadastrar quanto para atualizar.
    private Usuario preparar(UsuarioEntradaDTO entrada) {
        Usuario usuario = mapper.map(entrada, Usuario.class);
        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase(Locale.ROOT));
        return usuario;
    }
}
