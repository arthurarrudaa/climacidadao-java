package br.com.fiap.climacidadao.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/clima")
public class ClimaController {

    private final String apiKey;
    private final RestClient cliente = RestClient.create();

    public ClimaController(
            @Value("${OPENWEATHER_API_KEY}") String apiKey) {
        this.apiKey = apiKey;
    }

    @GetMapping
    public Map<String, Object> consultar(
            @RequestParam String cidade) {

        if (cidade.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe uma cidade.");
        }

        URI endereco = UriComponentsBuilder
                .fromUriString("https://api.openweathermap.org/data/2.5/weather")
                .queryParam("q", cidade.trim())
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "pt_br")
                .encode()
                .build()
                .toUri();

        JsonNode dados;

        try {
            dados = cliente.get()
                    .uri(endereco)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            System.err.println("Erro OpenWeatherMap: " + e.getClass().getSimpleName());

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Nao foi possivel consultar o servico meteorologico.");
        }

        if (dados == null || dados.path("main").isMissingNode()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Resposta meteorologica invalida.");
        }

        return Map.of(
                "cidade", dados.path("name").asText(),
                "temperatura", dados.path("main").path("temp").asDouble(),
                "sensacaoTermica", dados.path("main").path("feels_like").asDouble(),
                "umidade", dados.path("main").path("humidity").asInt(),
                "vento", dados.path("wind").path("speed").asDouble(),
                "descricao", dados.path("weather").path(0).path("description").asText()
        );
    }

    @GetMapping("/previsao")
    public Map<String, Object> consultarPrevisao(
            @RequestParam String cidade) {

        if (cidade.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe uma cidade.");
        }

        URI endereco = UriComponentsBuilder
                .fromUriString("https://api.openweathermap.org/data/2.5/forecast")
                .queryParam("q", cidade.trim())
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .queryParam("lang", "pt_br")
                .encode()
                .build()
                .toUri();

        JsonNode dados;

        try {
            dados = cliente.get()
                    .uri(endereco)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Nao foi possivel consultar a previsao.");
        }

        if (dados == null || !dados.path("list").isArray()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Resposta de previsao invalida.");
        }

        List<Map<String, Object>> previsoes = new ArrayList<>();

        for (JsonNode item : dados.path("list")) {
            previsoes.add(Map.of(
                    "horarioUtc", item.path("dt").asLong(),
                    "temperatura", item.path("main").path("temp").asDouble(),
                    "descricao", item.path("weather")
                            .path(0).path("description").asText(),
                    "probabilidadeChuva",
                    Math.round(item.path("pop").asDouble() * 100),
                    "vento", item.path("wind").path("speed").asDouble()
            ));
        }

        return Map.of(
                "cidade", dados.path("city").path("name").asText(),
                "fusoHorarioSegundos",
                dados.path("city").path("timezone").asInt(),
                "previsoes", previsoes
        );
    }
}
