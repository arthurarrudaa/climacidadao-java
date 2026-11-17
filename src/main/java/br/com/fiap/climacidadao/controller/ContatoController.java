
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

    private final ContatoDao contatoDao;
    private final ModelMapper mapper;
    private final String chaveAdmin;

    public ContatoController(
            ContatoDao contatoDao,
            ModelMapper mapper,
            @Value("${CONTATOS_ADMIN_KEY:}") String chaveAdmin) {

        this.contatoDao = contatoDao;
        this.mapper = mapper;
        this.chaveAdmin = chaveAdmin;
    }

    // Cadastro público: utilizado pelo formulário React.
    @PostMapping("/contatos")
    public ResponseEntity<ContatoSaidaDTO> cadastrar(
            @Valid @RequestBody ContatoEntradaDTO entrada) {

        Contato contato = prepararContato(entrada);
        contatoDao.criar(contato);

        ContatoSaidaDTO saida =
                mapper.map(contato, ContatoSaidaDTO.class);

        return ResponseEntity
                .created(URI.create("/contatos/" + contato.getId()))
                .body(saida);
    }

    // Listagem restrita.
    @GetMapping("/contatos")
    public List<ContatoSaidaDTO> listar(
            @RequestHeader(value = "X-Admin-Key", required = false)
            String chaveRecebida) {

        verificarAdmin(chaveRecebida);

        List<ContatoSaidaDTO> resultado = new ArrayList<>();

        for (Contato contato : contatoDao.listar()) {
            resultado.add(
                    mapper.map(contato, ContatoSaidaDTO.class)
            );
        }

        return resultado;
    }

    // Consulta por ID restrita.
    @GetMapping("/contatos/{id}")
    public ContatoSaidaDTO buscarPorId(
            @PathVariable long id,
            @RequestHeader(value = "X-Admin-Key", required = false)
            String chaveRecebida) {

        verificarAdmin(chaveRecebida);

        Contato contato = buscarContato(id);

        return mapper.map(contato, ContatoSaidaDTO.class);
    }

    // Atualização restrita.
    @PutMapping("/contatos/{id}")
    public ContatoSaidaDTO atualizar(
            @PathVariable long id,
            @Valid @RequestBody ContatoEntradaDTO entrada,
            @RequestHeader(value = "X-Admin-Key", required = false)
            String chaveRecebida) {

        verificarAdmin(chaveRecebida);

        Contato contato = prepararContato(entrada);
        contato.setId(id);

        if (!contatoDao.atualizar(contato)) {
            throw new RecursoNaoEncontradoException(
                    "Contato nao encontrado.");
        }

        return mapper.map(contato, ContatoSaidaDTO.class);
    }

    // Exclusão restrita.
    @DeleteMapping("/contatos/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable long id,
            @RequestHeader(value = "X-Admin-Key", required = false)
            String chaveRecebida) {

        verificarAdmin(chaveRecebida);

        if (!contatoDao.excluir(id)) {
            throw new RecursoNaoEncontradoException(
                    "Contato nao encontrado.");
        }

        return ResponseEntity.noContent().build();
    }

    private Contato buscarContato(long id) {
        Contato contato = contatoDao.buscarPorId(id);

        if (contato == null) {
            throw new RecursoNaoEncontradoException(
                    "Contato nao encontrado.");
        }

        return contato;
    }

    private Contato prepararContato(ContatoEntradaDTO entrada) {
        Contato contato = mapper.map(entrada, Contato.class);

        contato.setNome(contato.getNome().trim());
        contato.setEmail(
                contato.getEmail().trim().toLowerCase(Locale.ROOT)
        );
        contato.setMensagem(contato.getMensagem().trim());

        return contato;
    }

    private void verificarAdmin(String chaveRecebida) {
        if (chaveAdmin.isBlank() || chaveRecebida == null ||
                !MessageDigest.isEqual(
                        chaveAdmin.getBytes(StandardCharsets.UTF_8),
                        chaveRecebida.getBytes(StandardCharsets.UTF_8))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso restrito.");
        }
    }
}
