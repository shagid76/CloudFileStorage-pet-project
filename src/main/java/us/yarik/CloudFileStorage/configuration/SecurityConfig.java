package us.yarik.CloudFileStorage.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import us.yarik.CloudFileStorage.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/registration", "/static/**", "/all")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/redirect_to_directory", true)
                        .permitAll())
                .rememberMe(me -> me
                        .rememberMeParameter("rememberMe")
                        .tokenValiditySeconds(120000))

                .logout(log -> log
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .clearAuthentication(true)
                        .invalidateHttpSession(true)
                        .permitAll())
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/login"))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.userDetailsService(userDetailService).passwordEncoder(passwordEncoder());
        return auth.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
