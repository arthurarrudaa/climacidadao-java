package br.com.fiap.climacidadao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class FavoritoEntradaDTO {
    @Positive(message = "O usuarioId deve ser positivo.")
    private Long usuarioId;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 100, message = "A cidade deve ter no maximo 100 caracteres.")
    private String cidade;

    public FavoritoEntradaDTO() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }
}
