package com.BuonoRiccitiello.twitter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.HiddenHttpMethodFilter;

/**
 * Classe di configurazione per Spring Web MVC.
 * <p>
 * Questa classe gestisce la configurazione dei componenti web dell'applicazione,
 * inclusa l'esposizione delle risorse statiche caricate dagli utenti e il supporto
 * ai metodi HTTP non nativi dei form HTML standard (come PUT e DELETE).
 * </p>
 *
 * @author BuonoRiccitiello
 * @version 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura i gestori delle risorse statiche.
     * <p>
     * Mappa l'URL relativo {@code /uploads/**} alla cartella fisica {@code uploads/}
     * presente nella radice del progetto, permettendo al client di accedere direttamente
     * ai file caricati (es. immagini, allegati).
     * </p>
     *
     * @param registry il registro dei gestori delle risorse a cui aggiungere la mappatura
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

    /**
     * Registra il filtro per il supporto dei metodi HTTP nascosti nei form HTML.
     * <p>
     * Poiché i form HTML standard supportano nativamente solo i metodi {@code GET} e {@code POST},
     * questo filtro intercetta un parametro speciale (solitamente un campo nascosto chiamato {@code _method})
     * per convertire la richiesta HTTP {@code POST} nel rispettivo metodo {@code PUT}, {@code DELETE} o {@code PATCH}
     * atteso dai controller.
     * </p>
     *
     * @return un'istanza configurata di {@link HiddenHttpMethodFilter}
     */
    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}