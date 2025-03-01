package us.yarik.CloudFileStorage.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

@Configuration
public class I18nConfig {
    public static final Locale DEFAULT_LOCALE = Locale.of("en");
    public static final String LOCALE_COOKIE_NAME = "lang";
    public static final String LOCALE_CHANGE_PARAM = "lang";

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        messageSource.setDefaultLocale(DEFAULT_LOCALE);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver(LOCALE_COOKIE_NAME);
        cookieLocaleResolver.setDefaultLocale(DEFAULT_LOCALE);
        cookieLocaleResolver.setCookieMaxAge(Duration.ofDays(Integer.MAX_VALUE));
        return cookieLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName(LOCALE_CHANGE_PARAM);
        return localeChangeInterceptor;
    }
}