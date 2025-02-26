package us.yarik.CloudFileStorage.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class I18nConfig {
     public static final String DEFAULT_LOCALE = "en";
     public static final String LOCALE_COOKIE_NAME = "lang";
     public static final String LOCALE_CHANGE_PARAM = "lang";
}
