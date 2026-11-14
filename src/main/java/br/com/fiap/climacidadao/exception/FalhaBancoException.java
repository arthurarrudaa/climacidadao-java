package br.com.fiap.climacidadao.exception;

public class FalhaBancoException extends RuntimeException {
    public FalhaBancoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
