
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

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(
            RecursoNaoEncontradoException e,
            HttpServletRequest requisicao) {

        return erro(HttpStatus.NOT_FOUND, e.getMessage(), requisicao);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<Map<String, Object>> conflito(
            ConflitoException e,
            HttpServletRequest requisicao) {

        return erro(HttpStatus.CONFLICT, e.getMessage(), requisicao);
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> invalida(
            RequisicaoInvalidaException e,
            HttpServletRequest requisicao) {

        return erro(HttpStatus.BAD_REQUEST, e.getMessage(), requisicao);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> camposInvalidos(
            MethodArgumentNotValidException e,
            HttpServletRequest requisicao) {

        String mensagem = "Dados invalidos.";

        if (!e.getBindingResult().getFieldErrors().isEmpty()) {
            var campo = e.getBindingResult().getFieldErrors().get(0);

            mensagem = campo.getField() + ": "
                    + campo.getDefaultMessage();
        }

        return erro(HttpStatus.BAD_REQUEST, mensagem, requisicao);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Map<String, Object>> formatoInvalido(
            Exception e,
            HttpServletRequest requisicao) {

        return erro(
                HttpStatus.BAD_REQUEST,
                "JSON ou identificador invalido.",
                requisicao
        );
    }

    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<Map<String, Object>> rotaInexistente(
            Exception e,
            HttpServletRequest requisicao) {

        return erro(
                HttpStatus.NOT_FOUND,
                "Rota nao encontrada.",
                requisicao
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodoInvalido(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest requisicao) {

        return erro(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Metodo HTTP nao permitido nesta rota.",
                requisicao
        );
    }

    // Preserva o status definido pelos controllers,
    // inclusive o erro 502 do OpenWeatherMap.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> statusPersonalizado(
            ResponseStatusException e,
            HttpServletRequest requisicao) {

        HttpStatus status = HttpStatus.valueOf(
                e.getStatusCode().value()
        );

        String mensagem = e.getReason() != null
                ? e.getReason()
                : "Nao foi possivel processar a requisicao.";

        return erro(status, mensagem, requisicao);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(
            Exception e,
            HttpServletRequest requisicao) {

        return erro(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno. Tente novamente mais tarde.",
                requisicao
        );
    }

    private ResponseEntity<Map<String, Object>> erro(
            HttpStatus status,
            String mensagem,
            HttpServletRequest requisicao) {

        Map<String, Object> corpo = new LinkedHashMap<>();

        corpo.put("dataHora", LocalDateTime.now().toString());
        corpo.put("status", status.value());
        corpo.put("erro", status.getReasonPhrase());
        corpo.put("mensagem", mensagem);
        corpo.put("caminho", requisicao.getRequestURI());

        return ResponseEntity.status(status).body(corpo);
    }
}
