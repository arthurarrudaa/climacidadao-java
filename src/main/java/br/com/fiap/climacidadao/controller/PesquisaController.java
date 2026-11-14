package br.com.fiap.climacidadao.controller;

import br.com.fiap.climacidadao.dao.PesquisaDao;
import br.com.fiap.climacidadao.dao.UsuarioDao;
import br.com.fiap.climacidadao.dto.PesquisaEntradaDTO;
import br.com.fiap.climacidadao.dto.PesquisaSaidaDTO;
import br.com.fiap.climacidadao.exception.RecursoNaoEncontradoException;
import br.com.fiap.climacidadao.exception.RequisicaoInvalidaException;
import br.com.fiap.climacidadao.model.Pesquisa;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class PesquisaController {
    private final PesquisaDao pesquisaDao;
    private final UsuarioDao usuarioDao;
    private final ModelMapper mapper;

    public PesquisaController(PesquisaDao pesquisaDao, UsuarioDao usuarioDao, ModelMapper mapper) {
        this.pesquisaDao = pesquisaDao;
        this.usuarioDao = usuarioDao;
        this.mapper = mapper;
    }

    @PostMapping("/pesquisas")
    public ResponseEntity<PesquisaSaidaDTO> cadastrar(@Valid @RequestBody PesquisaEntradaDTO entrada) {
        validarUsuario(entrada.getUsuarioId());
        Pesquisa pesquisa = mapper.map(entrada, Pesquisa.class);
        pesquisa.setCidade(pesquisa.getCidade().trim());
        if (pesquisa.getDataConsulta() == null) {
            pesquisa.setDataConsulta(LocalDateTime.now());
        }
        pesquisaDao.criar(pesquisa);
        return ResponseEntity.created(URI.create("/pesquisas/" + pesquisa.getId()))
                .body(mapper.map(pesquisa, PesquisaSaidaDTO.class));
    }

    @PostMapping("/usuarios/{usuarioId}/pesquisas")
    public ResponseEntity<PesquisaSaidaDTO> cadastrarParaUsuario(
            @PathVariable long usuarioId, @Valid @RequestBody PesquisaEntradaDTO entrada) {
        conferirUsuarioDaRota(usuarioId, entrada.getUsuarioId());
        entrada.setUsuarioId(usuarioId);
        return cadastrar(entrada);
    }

    @GetMapping("/pesquisas")
    public List<PesquisaSaidaDTO> listar() {
        return converterLista(pesquisaDao.listar());
    }

    @GetMapping("/pesquisas/{id}")
    public PesquisaSaidaDTO buscarPorId(@PathVariable long id) {
        return mapper.map(buscarPesquisa(id), PesquisaSaidaDTO.class);
    }

    @GetMapping("/usuarios/{usuarioId}/pesquisas")
    public List<PesquisaSaidaDTO> listarPorUsuario(@PathVariable long usuarioId) {
        validarUsuario(usuarioId);
        return converterLista(pesquisaDao.listarPorUsuario(usuarioId));
    }

    @PutMapping("/pesquisas/{id}")
    public PesquisaSaidaDTO atualizar(@PathVariable long id, @Valid @RequestBody PesquisaEntradaDTO entrada) {
        Pesquisa anterior = buscarPesquisa(id);
        if (entrada.getUsuarioId() == null) {
            entrada.setUsuarioId(anterior.getUsuarioId());
        }
        validarUsuario(entrada.getUsuarioId());
        Pesquisa pesquisa = mapper.map(entrada, Pesquisa.class);
        pesquisa.setId(id);
        pesquisa.setCidade(pesquisa.getCidade().trim());
        if (pesquisa.getDataConsulta() == null) {
            pesquisa.setDataConsulta(anterior.getDataConsulta());
        }
        if (!pesquisaDao.atualizar(pesquisa)) {
            throw new RecursoNaoEncontradoException("Pesquisa nao encontrada.");
        }
        return mapper.map(pesquisa, PesquisaSaidaDTO.class);
    }

    @DeleteMapping("/pesquisas/{id}")
    public ResponseEntity<Void> excluir(@PathVariable long id) {
        if (!pesquisaDao.excluir(id)) {
            throw new RecursoNaoEncontradoException("Pesquisa nao encontrada.");
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

    private Pesquisa buscarPesquisa(long id) {
        Pesquisa pesquisa = pesquisaDao.buscarPorId(id);
        if (pesquisa == null) {
            throw new RecursoNaoEncontradoException("Pesquisa nao encontrada.");
        }
        return pesquisa;
    }

    private List<PesquisaSaidaDTO> converterLista(List<Pesquisa> pesquisas) {
        List<PesquisaSaidaDTO> saida = new ArrayList<>();
        for (Pesquisa pesquisa : pesquisas) {
            saida.add(mapper.map(pesquisa, PesquisaSaidaDTO.class));
        }
        return saida;
    }
}
