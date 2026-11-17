package br.com.fiap.climacidadao.controller;

import br.com.fiap.climacidadao.dao.ContatoDao;
import br.com.fiap.climacidadao.dto.ContatoEntradaDTO;
import br.com.fiap.climacidadao.dto.ContatoSaidaDTO;
import br.com.fiap.climacidadao.exception.RecursoNaoEncontradoException;
import br.com.fiap.climacidadao.model.Contato;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
public class ContatoController {
    private final ContatoDao dao;
    private final ModelMapper mapper;
    private final String chaveAdmin;

    public ContatoController(ContatoDao dao, ModelMapper mapper,
                             @Value("${CONTATOS_ADMIN_KEY:}") String chaveAdmin) {
        this.dao = dao;
        this.mapper = mapper;
        this.chaveAdmin = chaveAdmin;
    }

    // O formulário React pode cadastrar contatos sem chave administrativa.
    @PostMapping("/contatos")
    public ResponseEntity<ContatoSaidaDTO> cadastrar(@Valid @RequestBody ContatoEntradaDTO entrada) {
        Contato contato = preparar(entrada);
        dao.criar(contato);
        return ResponseEntity.created(URI.create("/contatos/" + contato.getId()))
                .body(mapper.map(contato, ContatoSaidaDTO.class));
    }

    @GetMapping("/contatos")
    public List<ContatoSaidaDTO> listar(
            @RequestHeader(value = "X-Admin-Key", required = false) String chave) {
        verificarAdmin(chave);
        List<ContatoSaidaDTO> lista = new ArrayList<>();
        for (Contato contato : dao.listar()) {
            lista.add(mapper.map(contato, ContatoSaidaDTO.class));
        }
        return lista;
    }

    @GetMapping("/contatos/{id}")
    public ContatoSaidaDTO buscarPorId(@PathVariable long id,
                                       @RequestHeader(value = "X-Admin-Key", required = false) String chave) {
        verificarAdmin(chave);
        return mapper.map(buscar(id), ContatoSaidaDTO.class);
    }

    @PutMapping("/contatos/{id}")
    public ContatoSaidaDTO atualizar(@PathVariable long id,
                                     @Valid @RequestBody ContatoEntradaDTO entrada,
                                     @RequestHeader(value = "X-Admin-Key", required = false) String chave) {
        verificarAdmin(chave);
        Contato contato = preparar(entrada);
        contato.setId(id);
        if (!dao.atualizar(contato)) {
            throw new RecursoNaoEncontradoException("Contato nao encontrado.");
        }
        return mapper.map(contato, ContatoSaidaDTO.class);
    }

    @DeleteMapping("/contatos/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id,
                                        @RequestHeader(value = "X-Admin-Key", required = false) String chave) {
        verificarAdmin(chave);
        if (!dao.excluir(id)) {
            throw new RecursoNaoEncontradoException("Contato nao encontrado.");
        }
        return ResponseEntity.noContent().build();
    }

    private Contato buscar(long id) {
        Contato contato = dao.buscarPorId(id);
        if (contato == null) {
            throw new RecursoNaoEncontradoException("Contato nao encontrado.");
        }
        return contato;
    }

    private Contato preparar(ContatoEntradaDTO entrada) {
        Contato contato = mapper.map(entrada, Contato.class);
        contato.setNome(contato.getNome().trim());
        contato.setEmail(contato.getEmail().trim().toLowerCase(Locale.ROOT));
        contato.setMensagem(contato.getMensagem().trim());
        return contato;
    }

    // Consulta, atualização e exclusão exigem a mesma chave usada no Render.
    private void verificarAdmin(String chave) {
        if (chaveAdmin.isBlank() || chave == null ||
                !MessageDigest.isEqual(chaveAdmin.getBytes(StandardCharsets.UTF_8),
                        chave.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito.");
        }
    }
}
