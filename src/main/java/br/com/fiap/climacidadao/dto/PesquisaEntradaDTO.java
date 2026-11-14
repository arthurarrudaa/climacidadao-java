package br.com.fiap.climacidadao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class PesquisaEntradaDTO {
    @Positive(message = "O usuarioId deve ser positivo.")
    private Long usuarioId;

    @NotBlank(message = "Informe a cidade.")
    @Size(max = 100, message = "A cidade deve ter no maximo 100 caracteres.")
    private String cidade;

    @NotNull(message = "Informe a temperatura.")
    @DecimalMin(value = "-100.0", message = "Temperatura abaixo do limite aceito.")
    @DecimalMax(value = "70.0", message = "Temperatura acima do limite aceito.")
    private BigDecimal temperatura;

    @PastOrPresent(message = "A data da consulta nao pode ser futura.")
    private LocalDateTime dataConsulta;

    public PesquisaEntradaDTO() {
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

    public BigDecimal getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(BigDecimal temperatura) {
        this.temperatura = temperatura;
    }

    public LocalDateTime getDataConsulta() {
        return dataConsulta;
    }

    public void setDataConsulta(LocalDateTime dataConsulta) {
        this.dataConsulta = dataConsulta;
    }
}
