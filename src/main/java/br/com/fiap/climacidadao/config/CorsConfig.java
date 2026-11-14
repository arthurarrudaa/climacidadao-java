package br.com.fiap.climacidadao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String origensPermitidas;

    public CorsConfig(@Value("${app.cors.allowed-origins}") String origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Override
    public void addCorsMappings(CorsRegistry registro) {
        String[] origens = origensPermitidas.split(",");
        for (int i = 0; i < origens.length; i++) {
            origens[i] = origens[i].trim();
        }
        registro.addMapping("/**")
                .allowedOrigins(origens)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
