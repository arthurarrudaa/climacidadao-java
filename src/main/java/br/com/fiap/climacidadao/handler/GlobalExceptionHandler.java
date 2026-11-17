package br.com.fiap.climacidadao.handler;

import br.com.fiap.climacidadao.exception.ConflitoException;
import br.com.fiap.climacidadao.exception.RecursoNaoEncontradoException;
import br.com.fiap.climacidadao.exception.RequisicaoInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Erros das regras de negócio.
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(
            RecursoNaoEncontradoException e, HttpServletRequest req) {
        return erro(HttpStatus.NOT_FOUND, e.getMessage(), req);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Map<String, Object>> conflito(
            ConflitoException e, HttpServletRequest req) {
        return erro(HttpStatus.CONFLICT, e.getMessage(), req);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> invalida(
            RequisicaoInvalidaException e, HttpServletRequest req) {
        return erro(HttpStatus.BAD_REQUEST, e.getMessage(), req);
    }

    // Campos que não passaram pelas validações dos DTOs.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> camposInvalidos(
            MethodArgumentNotValidException e, HttpServletRequest req) {
        var campos = e.getBindingResult().getFieldErrors();
        String mensagem = campos.isEmpty()
                ? "Dados invalidos."
                : campos.get(0).getField() + ": " + campos.get(0).getDefaultMessage();
        return erro(HttpStatus.BAD_REQUEST, mensagem, req);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> formatoInvalido(HttpServletRequest req) {
        return erro(HttpStatus.BAD_REQUEST, "JSON ou identificador invalido.", req);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<Map<String, Object>> rotaInexistente(HttpServletRequest req) {
        return erro(HttpStatus.NOT_FOUND, "Rota nao encontrada.", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodoInvalido(HttpServletRequest req) {
        return erro(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP nao permitido nesta rota.", req);
    }

    // Respeita os status definidos pelos controllers, como 403 e 502.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> statusPersonalizado(
            ResponseStatusException e, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String mensagem = e.getReason() != null
                ? e.getReason()
                : "Nao foi possivel processar a requisicao.";
        return erro(status, mensagem, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(HttpServletRequest req) {
        return erro(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente mais tarde.", req);
    }

    // Todas as respostas de erro têm os mesmos cinco campos JSON.
    private ResponseEntity<Map<String, Object>> erro(
            HttpStatus status, String mensagem, HttpServletRequest req) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("dataHora", LocalDateTime.now().toString());
        corpo.put("status", status.value());
        corpo.put("erro", status.getReasonPhrase());
        corpo.put("mensagem", mensagem);
        corpo.put("caminho", req.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }
}
