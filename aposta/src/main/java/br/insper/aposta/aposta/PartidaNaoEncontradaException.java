package br.insper.aposta.aposta;

public class PartidaNaoEncontradaException extends RuntimeException{
    public PartidaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }

}
