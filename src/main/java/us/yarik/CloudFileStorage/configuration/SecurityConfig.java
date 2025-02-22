package us.yarik.CloudFileStorage.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession
@RequiredArgsConstructor
public class SecurityConfig {
    public static final int T_30_DAYS_IN_SECONDS = 2592000;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/login").permitAll();
                    auth.requestMatchers("/registration").permitAll();
                    auth.requestMatchers("/static/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(formLogin -> {
                    formLogin.loginPage("/login").permitAll();
                    formLogin.usernameParameter("email");
                    formLogin.defaultSuccessUrl("/directory");
                })
                .rememberMe(me -> {
                    me.tokenValiditySeconds(T_30_DAYS_IN_SECONDS);
                })

                .logout(log -> {
                    log.logoutUrl("/logout").permitAll();
                    log.logoutSuccessUrl("/login").permitAll();
                    log.clearAuthentication(true);
                    log.invalidateHttpSession(true);
                })
                .sessionManagement(session -> {
                    session.maximumSessions(1).expiredUrl("/login");
                })
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
