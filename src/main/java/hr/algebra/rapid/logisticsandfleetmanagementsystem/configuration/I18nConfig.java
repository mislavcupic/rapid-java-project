package hr.algebra.rapid.logisticsandfleetmanagementsystem.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * Definira gdje se nalaze datoteke s prijevodima i postavlja UTF-8 kodiranje.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Postavite putanju do osnovne datoteke s porukama (bez sufiksa jezika)
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8"); // Ključno za podršku hrvatskim i drugim Unicode znakovima
        messageSource.setCacheSeconds(10); // Opcionalno: Cache osvježavanje (za razvoj)
        return messageSource;
    }

    /**
     * Definira kako će aplikacija odrediti trenutni jezik (Locale).
     * Koristimo CookieLocaleResolver da bi se odabir jezika zapamtio u pregledniku.
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver("lang");

        localeResolver.setDefaultLocale(new Locale("hr"));
        localeResolver.setCookieMaxAge(Duration.ofHours(1)); // Trajanje cookieja (u sekundama, ovdje 1 sat)
        return localeResolver;
    }

    /**
     * Omogućava promjenu lokaliteta putem URL parametra (npr. ?lang=fr).
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        // Ime parametra u URL-u koji će promijeniti jezik
        localeChangeInterceptor.setParamName("lang");
        registry.addInterceptor(localeChangeInterceptor);
    }
}