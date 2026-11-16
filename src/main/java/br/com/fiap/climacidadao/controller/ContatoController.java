package br.com.fiap.climacidadao.controller;

import br.com.fiap.climacidadao.dao.ContatoDao;
import br.com.fiap.climacidadao.dto.ContatoEntradaDTO;
import br.com.fiap.climacidadao.dto.ContatoSaidaDTO;
import br.com.fiap.climacidadao.model.Contato;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class ContatoController {

    private final ContatoDao contatoDao;
    private final ModelMapper mapper;

    public ContatoController(ContatoDao contatoDao, ModelMapper mapper) {
        this.contatoDao = contatoDao;
        this.mapper = mapper;
    }

    @PostMapping("/contatos")
    public ResponseEntity<ContatoSaidaDTO> cadastrar(
            @Valid @RequestBody ContatoEntradaDTO entrada) {

        Contato contato = mapper.map(entrada, Contato.class);

        contato.setNome(contato.getNome().trim());
        contato.setEmail(contato.getEmail().trim().toLowerCase());
        contato.setMensagem(contato.getMensagem().trim());

        contatoDao.criar(contato);

        ContatoSaidaDTO saida = mapper.map(contato, ContatoSaidaDTO.class);

        return ResponseEntity
                .created(URI.create("/contatos/" + contato.getId()))
                .body(saida);
    }
}

