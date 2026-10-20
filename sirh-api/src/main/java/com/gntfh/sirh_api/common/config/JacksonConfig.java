package com.gntfh.sirh_api.common.config;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Jackson pour la sérialisation JSON.
 * Fournit un bean JsonMapper utilisé par AuditService pour sérialiser
 * les valeurs avant/après dans le journal d'audit.
 * Le bean nommé "jsonMapper" ne remplace pas le ObjectMapper auto-configuré
 * par Spring MVC pour les réponses HTTP.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder().build();
    }
}
